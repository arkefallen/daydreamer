package com.android.ark.daydreamer.presentation.screens.home

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.android.ark.daydreamer.data.repository.Diaries
import com.android.ark.daydreamer.data.repository.MongoDB
import com.android.ark.daydreamer.utils.RequestState
import kotlinx.coroutines.launch

class HomeViewmodel : ViewModel() {
    var diaries: MutableState<Diaries> = mutableStateOf(RequestState.Idle)

    init {
        observeAllDiaries()
    }

    private fun observeAllDiaries() {
        viewModelScope.launch {
            MongoDB.getAllDiaries().collect { result ->
                diaries.value = result
                Log.d("diaries from realm", "${diaries.value}")
            }
        }
    }
}