package com.android.ark.daydreamer.presentation.screens.write

import android.annotation.SuppressLint
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.ark.daydreamer.model.Diary
import com.android.ark.daydreamer.model.Mood
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState
import com.google.accompanist.pager.rememberPagerState

@OptIn(ExperimentalPagerApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun WriteScreen(
    onBackPressed : () -> Unit,
    onDeleteClick: () -> Unit,
    pagerState: PagerState,
    uiState: WriteUiState,
    writeViewmodel: WriteViewmodel
) {
    LaunchedEffect(key1 = uiState.mood) {
        pagerState.scrollToPage(Mood.valueOf(uiState.mood.name).ordinal)
    }

    Scaffold(
        topBar = {
            WriteTopBar(
                onBackPressed = onBackPressed,
                onDeleteClick = onDeleteClick,
                selectedDiary = uiState.selectedDiary
            )
        },
    ) {
        WriteContent(
            paddingValues = it,
            pagerState = pagerState,
            title = uiState.title,
            onTitleChanged = { title ->
                writeViewmodel.setTitle(title)
            },
            description = uiState.description,
            onDescriptionChanged = { desc ->
                writeViewmodel.setDescription(description = desc)
            },
            selectedDiary = uiState.selectedDiary
        )
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
@Preview(showBackground = true)
fun WriteScreenPreview() {
    WriteScreen(
        onBackPressed = {},
        onDeleteClick = {},
        pagerState = rememberPagerState(),
        uiState = WriteUiState(selectedDiary = Diary(), mood = Mood.Happy),
        writeViewmodel = viewModel()
    )
}