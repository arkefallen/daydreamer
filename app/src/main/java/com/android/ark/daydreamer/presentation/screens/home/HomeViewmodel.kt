package com.android.ark.daydreamer.presentation.screens.home

import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.ark.daydreamer.data.database.ImagesToDeleteDAO
import com.android.ark.daydreamer.data.database.entity.ImageToDelete
import com.android.ark.daydreamer.data.repository.Diaries
import com.android.ark.daydreamer.data.repository.MongoDB
import com.android.ark.daydreamer.domain.GetImagesFromFirebaseUseCase
import com.android.ark.daydreamer.utils.ConnectivityObserver
import com.android.ark.daydreamer.utils.NetworkConnectivityObserver
import com.android.ark.daydreamer.utils.RequestState
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.storage.FirebaseStorage
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class HomeViewmodel @Inject constructor(
    private val connectivityObserver: NetworkConnectivityObserver,
    private val imagesToDeleteDAO: ImagesToDeleteDAO
) : ViewModel() {
    var diaries: MutableState<Diaries> = mutableStateOf(RequestState.Idle)
    val firebaseUseCase = GetImagesFromFirebaseUseCase()
    private var networkStatus by mutableStateOf(ConnectivityObserver.Status.UNAVAILABLE)

    private var _requestLoading : MutableStateFlow<Boolean> = MutableStateFlow(false)
    val requestLoading : StateFlow<Boolean> = _requestLoading.asStateFlow()


    init {
        observeAllDiaries()
        viewModelScope.launch {
            connectivityObserver.observe().collect { networkStatus = it }
        }
    }

    private fun observeAllDiaries() {
        viewModelScope.launch {
            diaries.value = RequestState.Loading
            MongoDB.getAllDiaries().debounce(1000).collect { result ->
                diaries.value = result
            }
        }
    }

    fun fetchImagesFromDatabase(
        remoteImagePaths: List<String>,
        onSuccessFetched: (Uri) -> Unit = {},
        onFailedFetched: (Exception) -> Unit = {},
        onReadyToDisplay: () -> Unit = {},
    ) {
        viewModelScope.launch {
            firebaseUseCase(
                remoteImagePaths, onSuccessFetched, onFailedFetched, onReadyToDisplay
            )
        }
    }

    fun deleteAllDiaries(
        onSuccess: () -> Unit,
        onFailed: (Throwable) -> Unit,
        onLoading: () -> Unit
    ) {
        if (networkStatus == ConnectivityObserver.Status.AVAILABLE) {
            _requestLoading.value = true
            val userId = FirebaseAuth.getInstance().currentUser?.uid
            val imagesDirectory = "images/${userId}"
            val storage = FirebaseStorage.getInstance().reference
            storage.child(imagesDirectory)
                .listAll()
                .addOnSuccessListener {
                    it.items.forEach { ref ->
                        val imagePath = "images/${userId}/${ref.name}"
                        storage.child(imagePath).delete()
                            .addOnFailureListener {
                                viewModelScope.launch(Dispatchers.IO) {
                                    imagesToDeleteDAO.storeImageToDelete(
                                        ImageToDelete(
                                            remoteImagePath = imagePath
                                        )
                                    )
                                }
                            }
                    }
                    viewModelScope.launch(Dispatchers.IO) {
                        MongoDB.deleteAllDiaries().collect { result ->
                            when (result) {
                                is RequestState.Success -> {
                                    _requestLoading.value = false
                                    withContext(Dispatchers.Main) {
                                        onSuccess()
                                    }
                                }
                                is RequestState.Error -> {
                                    _requestLoading.value = false
                                    withContext(Dispatchers.Main) {
                                        onFailed(Exception(result.message))
                                    }
                                }
                                is RequestState.Loading -> {
                                    withContext(Dispatchers.Main) {
                                        onLoading()
                                    }
                                }
                                else -> {}
                            }
                        }
                    }
                }
                .addOnFailureListener { onFailed(it) }
        } else {
            onFailed(Exception("No Internet Connection."))
        }
    }
}