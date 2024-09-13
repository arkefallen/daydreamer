package com.android.ark.daydreamer.presentation.screens.write

import android.annotation.SuppressLint
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.tooling.preview.Preview
import com.android.ark.daydreamer.model.Diary
import com.android.ark.daydreamer.model.GalleryImage
import com.android.ark.daydreamer.model.Mood
import com.android.ark.daydreamer.presentation.components.GalleryState
import java.time.ZonedDateTime

@OptIn(ExperimentalFoundationApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun WriteScreen(
    onBackPressed : () -> Unit,
    onDeleteClick: () -> Unit,
    pagerState: PagerState,
    uiState: WriteUiState,
    writeViewmodel: WriteViewmodel,
    onSaveClicked: (Diary) -> Unit,
    onUpdatedDateTime: (ZonedDateTime) -> Unit,
    onImageSelected: (Uri) -> Unit,
    onImageClicked: (GalleryImage) -> Unit,
    onAddImageClicked: () -> Unit,
    galleryState: GalleryState
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
            selectedDiary = uiState.selectedDiary,
            onSaveClicked = onSaveClicked,
            onUpdatedDateTime = onUpdatedDateTime,
            onImageSelected = onImageSelected,
            onImageClicked = onImageClicked,
            onAddImageClicked = onAddImageClicked,
            galleryState = galleryState
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
@Preview(showBackground = true)
fun WriteScreenPreview() {
//    WriteScreen(
//        onBackPressed = {},
//        onDeleteClick = {},
//        pagerState = rememberPagerState(pageCount = {1}),
//        uiState = WriteUiState(selectedDiary = Diary(), mood = Mood.Happy),
//        writeViewmodel = viewModel(),
//        onSaveClicked = {
//
//        },
//        onUpdatedDateTime = {}
//    )
}