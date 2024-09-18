package com.android.ark.ui.components

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.android.ark.util.Elevation
import kotlin.math.max

@Composable
fun GalleryUploader(
    modifier: Modifier = Modifier,
    galleryState: GalleryState,
    imageSize: Dp = 60.dp,
    imageShape: CornerBasedShape = MaterialTheme.shapes.small,
    spaceBetween: Dp = 10.dp,
    onImageSelected: (Uri) -> Unit,
    onAddImageClicked: () -> Unit,
    onImageClicked: (GalleryImage) -> Unit,
) {
    val multiplePhotoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(maxItems = 8)
    ) { images ->
        images.forEach { imageUri ->
            onImageSelected(imageUri)
        }
    }

    BoxWithConstraints(modifier = modifier.padding(vertical = 14.dp)) {
        // DerivedStateOf -> define calculation that value changes overtime without making Composable to recompose
        val numberOfVisibleImages = remember {
            derivedStateOf {
                max(
                    a = 0,
                    // Count the maximum width of the screen divided by the size of the image
                    // and subtract with 2 to give space for the last image overlay component
                    // & the add image button
                    b = this.maxWidth.div(spaceBetween + imageSize).toInt().minus(2)
                )
            }
        }

        val remainingImages = remember {
            derivedStateOf {
                // Calculate the number of remaining images by subtracting the total of all images
                // with the number of visible images
                galleryState.images.size - numberOfVisibleImages.value
            }
        }

        Row {
            AddImageButton(
                imageSize = imageSize,
                imageShape = imageShape,
                onClick = {
                    onAddImageClicked()
                    multiplePhotoPicker.launch(
                        PickVisualMediaRequest(
                            ActivityResultContracts.PickVisualMedia.ImageOnly
                        )
                    )
                }
            )
            Spacer(modifier = Modifier.width(spaceBetween))
            galleryState.images.take(numberOfVisibleImages.value).forEach { galleryImage ->
                AsyncImage(
                    modifier = Modifier
                        .clip(imageShape)
                        .size(imageSize)
                        .clickable { onImageClicked(galleryImage) }
                    ,
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(galleryImage.image)
                        .crossfade(true)
                        .build(),
                    contentScale = ContentScale.Crop,
                    contentDescription = "Gallery Image"
                )
                Spacer(modifier = Modifier.width(spaceBetween))
            }
            if (remainingImages.value > 0) {
                LastImageOverlay(
                    imageSize = imageSize,
                    imageShape = imageShape,
                    remainingImages = remainingImages.value
                )
            }
        }
    }
}

@Composable
fun AddImageButton(
    imageSize: Dp,
    imageShape: CornerBasedShape,
    onClick: () -> Unit,
) {
    Surface(
        modifier = Modifier
            .size(imageSize)
            .clip(imageShape),
        onClick = onClick,
        tonalElevation = Elevation.Level1
    ) {
        Box(
            modifier = Modifier
                .background(color = MaterialTheme.colorScheme.secondaryContainer)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Add Image Icon",
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}

@Composable
fun LastImageOverlay(
    imageSize: Dp,
    remainingImages: Int,
    imageShape: CornerBasedShape,
) {
    Box(contentAlignment = Alignment.Center) {
        Surface(
            modifier = Modifier
                .clip(imageShape)
                .size(imageSize),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {}
        Text(
            text = "+$remainingImages",
            style = TextStyle(
                fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                fontWeight = FontWeight.Medium
            ),
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}