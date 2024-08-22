package com.android.ark.daydreamer.presentation.screens.home

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.ark.daydreamer.data.repository.Diaries
import com.android.ark.daydreamer.data.repository.MongoDB
import com.android.ark.daydreamer.utils.RequestState
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch

@OptIn(FlowPreview::class)
class HomeViewmodel : ViewModel() {
    var diaries: MutableState<Diaries> = mutableStateOf(RequestState.Idle)

    init {
        observeAllDiaries()
    }

    private fun observeAllDiaries() {
        viewModelScope.launch {
            diaries.value = RequestState.Loading
            MongoDB.getAllDiaries().debounce(1000).collect { result ->
                diaries.value = result
            }
        }
    }
}