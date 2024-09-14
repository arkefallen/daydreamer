package com.android.ark.daydreamer.presentation.components

import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.android.ark.daydreamer.model.Diary
import com.android.ark.daydreamer.model.Mood
import com.android.ark.daydreamer.presentation.screens.home.HomeViewmodel
import com.android.ark.daydreamer.utils.Elevation
import com.android.ark.daydreamer.utils.toInstant
import io.realm.kotlin.ext.realmListOf
import java.text.SimpleDateFormat
import java.time.Instant
import java.util.Date
import java.util.Locale

@Composable
fun DiaryHolder(
    diary: Diary,
    onClick: (String) -> Unit,
    viewmodel: HomeViewmodel,
) {
    var componentHeight by remember { mutableStateOf(0.dp) }
    val localDensity = LocalDensity.current
    val localContext = LocalContext.current
    val mutableInteractionSource by remember { mutableStateOf(MutableInteractionSource()) }
    var galleryButtonOpened by remember { mutableStateOf(false) }
    var galleryLoading by remember { mutableStateOf(false) }
    val downloadedImages = remember {
        mutableStateListOf<Uri>()
    }

    LaunchedEffect(key1 = galleryButtonOpened) {
        if (galleryButtonOpened && downloadedImages.isEmpty()) {
            galleryLoading = true
            viewmodel.fetchImagesFromDatabase(
                remoteImagePaths = diary.images,
                onSuccessFetched = {
                    downloadedImages.add(it)
                },
                onFailedFetched = {
                    Toast.makeText(
                        localContext,
                        "Failed to download images. Try to re-download",
                        Toast.LENGTH_SHORT
                    ).show()
                    galleryLoading = false
                    galleryButtonOpened = false
                },
                onReadyToDisplay = {
                    galleryLoading = false
                }
            )
        }
    }

    Row(
        modifier = Modifier.clickable(
            indication = null,
            interactionSource = mutableInteractionSource
        ) {
            onClick(diary._id.toHexString())
        }
    ) {
        Spacer(modifier = Modifier.width(14.dp))
        Surface(
            modifier = Modifier
                .width(2.dp)
                .height(componentHeight + 14.dp),
            tonalElevation = Elevation.Level1
        ) {}
        Spacer(modifier = Modifier.width(20.dp))
        Surface(
            modifier = Modifier
                .clip(shape = Shapes().medium)
                .animateContentSize()
                .onGloballyPositioned {
                    componentHeight = with(localDensity) {
                        it.size.height.toDp()
                    }
                },
            tonalElevation = Elevation.Level1
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
            ) {
                DiaryHeader(moodName = diary.mood, time = diary.date.toInstant())
                Text(
                    text = diary.description,
                    modifier = Modifier.padding(14.dp),
                    style = TextStyle(fontSize = MaterialTheme.typography.bodyLarge.fontSize),
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis
                )
                if (diary.images.isNotEmpty()) {
                    Row(
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier
                            .padding(horizontal = 14.dp)
                            .fillMaxWidth()
                    ) {
                        ShowGalleryButton(
                            galleryOpened = galleryButtonOpened,
                            onClick = { galleryButtonOpened = !galleryButtonOpened },
                            galleryLoading = galleryLoading
                        )
                    }
                    AnimatedVisibility(
                        visible = galleryButtonOpened,
                        enter = fadeIn() + expandVertically(
                            animationSpec = spring(
                                dampingRatio = Spring.DampingRatioMediumBouncy,
                                stiffness = Spring.StiffnessLow
                            )
                        )
                    ) {
                        Column(modifier = Modifier.padding(horizontal = 14.dp)) {
                            Gallery(images = downloadedImages.toList())
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DiaryHeader(
    moodName: String,
    time: Instant,
) {
    val mood by remember { mutableStateOf(Mood.valueOf(moodName)) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(mood.containerColor)
            .padding(horizontal = 14.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                painter = painterResource(id = mood.icon),
                contentDescription = "Mood Icon",
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(7.dp))
            Text(
                text = mood.name,
                color = mood.contentColor,
                style = TextStyle(fontSize = MaterialTheme.typography.bodyMedium.fontSize)
            )
        }
        Text(
            text = SimpleDateFormat("hh:mm a", Locale.US).format(Date.from(time)),
            color = mood.contentColor,
            style = TextStyle(fontSize = MaterialTheme.typography.bodyMedium.fontSize)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun DiaryHolderPreview() {
    DiaryHolder(
        diary = Diary().apply {
            title = "Title"
            description =
                "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
            mood = Mood.Happy.name
            images = realmListOf("", "")
        },
        onClick = {},
        viewmodel = viewModel()
    )
}