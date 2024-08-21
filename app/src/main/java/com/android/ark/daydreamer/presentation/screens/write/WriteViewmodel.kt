package com.android.ark.daydreamer.presentation.screens.write

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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.mongodb.kbson.ObjectId

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
                MongoDB.getSelectedDiary(diaryId = ObjectId.invoke(uiState.selectedDiaryId!!)).collect { result ->
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
}

data class WriteUiState(
    val title: String = "",
    val description: String = "",
    val mood: Mood = Mood.Neutral,
    val selectedDiaryId : String? = null,
    val selectedDiary: Diary? = null
)