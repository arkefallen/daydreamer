package com.android.ark.daydreamer.presentation.screens.write

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.ark.daydreamer.data.repository.MongoDB
import com.android.ark.daydreamer.model.Diary
import com.android.ark.daydreamer.model.Mood
import com.android.ark.daydreamer.utils.RequestState
import com.android.ark.daydreamer.utils.toRealmInstant
import io.realm.kotlin.types.RealmInstant
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.mongodb.kbson.ObjectId
import java.time.ZonedDateTime

class WriteViewmodel(
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

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
                    id = ObjectId.invoke(uiState.selectedDiaryId!!)
                ).collect { result ->
                    when(result) {
                        is RequestState.Success -> {
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