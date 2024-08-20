package com.android.ark.daydreamer.presentation.screens.write

import android.annotation.SuppressLint
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import com.android.ark.daydreamer.model.Diary
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.PagerState

@OptIn(ExperimentalPagerApi::class)
@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun WriteScreen(
    onBackPressed : () -> Unit,
    onDeleteClick: () -> Unit,
    selectedDiary: Diary?,
    pagerState: PagerState
) {
    Scaffold(
        topBar = {
            WriteTopBar(
                onBackPressed = onBackPressed,
                onDeleteClick = onDeleteClick,
                selectedDiary = selectedDiary
            )
        },
    ) {
        WriteContent(
            paddingValues = it,
            pagerState = pagerState,
            title = "",
            onTitleChanged = {},
            description = ""
        ) {

        }
    }
}