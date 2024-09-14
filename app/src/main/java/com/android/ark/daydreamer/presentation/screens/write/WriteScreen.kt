package com.android.ark.daydreamer.presentation.screens.write

import android.annotation.SuppressLint
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.PagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import coil.request.ImageRequest
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
    galleryState: GalleryState
) {
    var selectedGalleryImage by remember {
        mutableStateOf<GalleryImage?>(null)
    }

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
            galleryState = galleryState,
            onImageClicked = { selectedImage ->
                selectedGalleryImage = selectedImage
            }
        )
        AnimatedVisibility(visible = selectedGalleryImage != null) {
            selectedGalleryImage?.let { image ->
                Dialog(onDismissRequest = { selectedGalleryImage = null }) {
                    ZoomableImage(
                        selectedGalleryImage = image,
                        onCloseClicked = { selectedGalleryImage = null },
                        onDeleteClicked = {}
                    )
                }
            }

        }
    }
}

@Composable
fun ZoomableImage(
    selectedGalleryImage: GalleryImage,
    onCloseClicked: () -> Unit,
    onDeleteClicked: () -> Unit
) {
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
    var scale by remember { mutableFloatStateOf(1f) }
    Box(
        modifier = Modifier
            .pointerInput(Unit) {
                detectTransformGestures { _, pan, zoom, _ ->
                    scale = maxOf(1f, minOf(scale * zoom, 5f))
                    val maxX = (size.width * (scale - 1)) / 2
                    val minX = -maxX
                    offsetX = maxOf(minX, minOf(maxX, offsetX + pan.x))
                    val maxY = (size.height * (scale - 1)) / 2
                    val minY = -maxY
                    offsetY = maxOf(minY, minOf(maxY, offsetY + pan.y))
                }
            }
    ) {
        AsyncImage(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = maxOf(.5f, minOf(3f, scale)),
                    scaleY = maxOf(.5f, minOf(3f, scale)),
                    translationX = offsetX,
                    translationY = offsetY
                ),
            model = ImageRequest.Builder(LocalContext.current)
                .data(selectedGalleryImage.image.toString())
                .crossfade(true)
                .build(),
            contentScale = ContentScale.Fit,
            contentDescription = "Gallery Image"
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 24.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(onClick = onCloseClicked) {
                Icon(imageVector = Icons.Rounded.Close, contentDescription = "Close Icon")
                Text(text = "Close")
            }
            Button(onClick = onDeleteClicked) {
                Icon(imageVector = Icons.Rounded.Delete, contentDescription = "Delete Icon")
                Text(text = "Delete")
            }
        }
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