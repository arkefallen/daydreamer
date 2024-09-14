package com.android.ark.daydreamer.presentation.screens.write

import android.net.Uri
import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.ark.daydreamer.data.database.ImagesToDeleteDAO
import com.android.ark.daydreamer.data.database.dao.ImagesToUploadDAO
import com.android.ark.daydreamer.data.database.entity.ImageToDelete
import com.android.ark.daydreamer.data.database.entity.ImageToUpload
import com.android.ark.daydreamer.data.repository.MongoDB
import com.android.ark.daydreamer.domain.GetImagesFromFirebaseUseCase
import com.android.ark.daydreamer.model.Diary
import com.android.ark.daydreamer.model.GalleryImage
import com.android.ark.daydreamer.model.Mood
import com.android.ark.daydreamer.presentation.components.GalleryState
import com.android.ark.daydreamer.utils.RequestState
import com.android.ark.daydreamer.utils.toRealmInstant
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import io.realm.kotlin.types.RealmInstant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mongodb.kbson.ObjectId
import java.time.ZonedDateTime
import javax.inject.Inject

@HiltViewModel
class WriteViewmodel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val imagesToUploadDAO: ImagesToUploadDAO,
    private val imagesToDeleteDAO: ImagesToDeleteDAO
) : ViewModel() {
    val firebaseUseCase = GetImagesFromFirebaseUseCase()
    val galleryState = GalleryState()
    var uiState by mutableStateOf(WriteUiState())
        private set

    init {
        getDiaryIdArgument()
        fetchSelectedDiary()
    }

    private fun getDiaryIdArgument() {
        uiState = uiState.copy(
            selectedDiaryId = savedStateHandle.get<String>(key = "diaryId")
        )
    }

    private fun fetchSelectedDiary() {
        if (uiState.selectedDiaryId != null) {
            viewModelScope.launch(Dispatchers.Main) {
                MongoDB.getSelectedDiary(diaryId = ObjectId.invoke(uiState.selectedDiaryId!!))
                    .catch {
                        emit(RequestState.Error(message = it.message.toString()))
                    }
                    .collect { result ->
                    when(result) {
                        is RequestState.Success -> {
                            result.data.let {
                                uiState = uiState.copy(
                                    title = it.title,
                                    description = it.description,
                                    mood = Mood.valueOf(it.mood),
                                    selectedDiary = it
                                )
                                firebaseUseCase(
                                    remoteImagePaths = it.images,
                                    onSuccessFetched = { imageUri ->
                                        galleryState.addImage(
                                            GalleryImage(
                                                image = imageUri,
                                                remoteImagePath = generateImagePath(imageUri.toString())
                                            )
                                        )
                                        Log.d("firebase fetch", "gallerystate: ${galleryState.images.toList()}")
                                    }
                                )
                            }
                        }
                        is RequestState.Error -> {
                            Log.d("WriteViewmodel", result.message)
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    private fun insertDiary(
        diary: Diary,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            MongoDB.insertDiary(diary = diary).collect { result ->
                when (result) {
                    is RequestState.Success -> {
                        uploadImagesToFirebase()
                        withContext(Dispatchers.Main) {
                            onSuccess()
                        }
                    }

                    is RequestState.Error -> {
                        withContext(Dispatchers.Main) {
                            onError(result.message)
                        }
                    }

                    else -> {}
                }
            }
        }
    }

    private fun updateDiary(
        updatedDiary: Diary,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            MongoDB.updateDiary(
                updatedDiary = updatedDiary.apply {
                    this._id = ObjectId.invoke(uiState.selectedDiaryId!!)
                    if (uiState.updatedDateTime != null) {
                        this.date = uiState.updatedDateTime!!
                    } else {
                        this.date = uiState.selectedDiary?.date!!
                    }
                }
            ).collect { result ->
                when(result) {
                    is RequestState.Success -> {
                        uploadImagesToFirebase()
                        withContext(Dispatchers.Main) {
                            onSuccess()
                        }
                    }
                    is RequestState.Error -> {
                        withContext(Dispatchers.Main) {
                            onError(result.message)
                        }
                    }
                    else -> {}
                }
            }
        }
    }

    fun upsertDiary(
        diary: Diary,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            if (uiState.selectedDiaryId != null) {
                updateDiary(
                    updatedDiary = diary,
                    onSuccess = onSuccess,
                    onError = onError
                )
                if (galleryState.imagesToBeDeleted.isNotEmpty()) {
                    removeImagesInFirebase(diary.images.toList())
                    galleryState.clearImagesToBeDeleted()
                }
            } else {
                insertDiary(diary, onSuccess, onError)
            }
        }
    }

    fun deleteDiary(
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch {
            if (uiState.selectedDiaryId != null) {
                MongoDB.deleteDiary(
                    diaryId = ObjectId.invoke(uiState.selectedDiaryId!!)
                ).collect { result ->
                    when(result) {
                        is RequestState.Success -> {
                            uiState.selectedDiary?.let {
                                removeImagesInFirebase(it.images.toList())
                            }
                            withContext(Dispatchers.Main) {
                                onSuccess()
                            }
                        }
                        is RequestState.Error -> {
                            withContext(Dispatchers.Main) {
                                onError(result.message)
                            }
                        }
                        else -> {}
                    }
                }
            }
        }
    }

    fun addImage(image: Uri, imageType: String) {
        val remoteImagePath = "images/${FirebaseAuth.getInstance().currentUser?.uid}/" +
                "${image.lastPathSegment}-${System.currentTimeMillis()}.$imageType"
        galleryState.addImage(
            GalleryImage(
                image = image,
                remoteImagePath = remoteImagePath
            )
        )
    }

    private fun uploadImagesToFirebase() {
        val storage = FirebaseStorage.getInstance().reference
        galleryState.images.forEach { galleryImage ->
            val imagePath = storage.child(galleryImage.remoteImagePath)
            imagePath.putFile(galleryImage.image)
                // Check the progress of image uploading
                .addOnProgressListener { taskSnapshot ->
                    // Take the upload task session
                    val sessionUri = taskSnapshot.uploadSessionUri
                    // If the uploading process is failed or interrupted,then retry
                    // the uploading process by resuming the upload session to Firebase
                    if (sessionUri != null) {
                        viewModelScope.launch(Dispatchers.IO) {
                            imagesToUploadDAO.addImageToUpload(
                                ImageToUpload(
                                    remoteImagePath = galleryImage.remoteImagePath,
                                    imageUri = galleryImage.image.toString(),
                                    sessionUri = sessionUri.toString()
                                )
                            )
                        }
                    }
                }
        }
    }

    private fun removeImagesInFirebase(images: List<String>) {
        val storage = FirebaseStorage.getInstance().reference
        images.forEach { path ->
            storage.child(path).delete()
                .addOnFailureListener {
                    viewModelScope.launch(Dispatchers.IO) {
                        imagesToDeleteDAO.storeImageToDelete(
                            imageToDelete = ImageToDelete(
                                remoteImagePath = path
                            )
                        )
                    }
                }
        }
    }

    private fun generateImagePath(imageUri: String) : String {
        val chunks = imageUri.split("%2F")
        val imageName = chunks[2].split("?").first()
        val firebaseUser = Firebase.auth.currentUser?.uid
        return "images/$firebaseUser/$imageName"
    }

    fun setTitle(title: String) {
        uiState = uiState.copy(title = title)
    }

    fun setDescription(description: String) {
        uiState = uiState.copy(description = description)
    }

    fun updateDateTime(zonedDateTime: ZonedDateTime) {
        uiState = uiState.copy(updatedDateTime = zonedDateTime.toInstant().toRealmInstant())
    }
}

data class WriteUiState(
    val title: String = "",
    val description: String = "",
    val mood: Mood = Mood.Neutral,
    val selectedDiaryId : String? = null,
    val selectedDiary: Diary? = null,
    val updatedDateTime: RealmInstant? = null
)