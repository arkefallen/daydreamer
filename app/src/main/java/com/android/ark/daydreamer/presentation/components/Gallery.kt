package com.android.ark.daydreamer.presentation.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import kotlin.math.max

@Composable
fun Gallery(
    modifier: Modifier = Modifier,
    images: List<String>,
    imageSize: Dp = 40.dp,
    spaceBetween: Dp = 10.dp,
    imageShape: CornerBasedShape = Shapes().small,
) {
    BoxWithConstraints(modifier = modifier) {
        // DerivedStateOf -> define calculation that value changes overtime without making Composable to recompose
        val numberOfVisibleImages = remember {
            derivedStateOf {
                max(
                    a = 0,
                    // Count the maximum width of the screen divided by the size of the image
                    // and subtract with 1 to give space for the last image overlay component
                    b = this.maxWidth.div(spaceBetween + imageSize).toInt().minus(1)
                )
            }
        }

        val remainingImages = remember {
            derivedStateOf {
                // Calculate the number of remaining images by subtracting the total of all images
                // with the number of visible images
                images.size - numberOfVisibleImages.value
            }
        }

        Row {
            images.take(numberOfVisibleImages.value).forEach { image ->
                AsyncImage(
                    modifier = Modifier
                        .clip(imageShape)
                        .size(imageSize),
                    model = ImageRequest.Builder(LocalContext.current)
                        .data(image)
                        .crossfade(true)
                        .build(),
                    contentDescription = "Gallery Image"
                )
                Spacer(modifier = Modifier.width(spaceBetween))
            }
        }
        if (remainingImages.value > 0) {
            LastImageOverlay(
                imageSize = imageSize,
                remainingImages = remainingImages.value,
                imageShape = imageShape
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

@Composable
fun ShowGalleryButton(
    galleryOpened: Boolean,
    onClick: () -> Unit,
) {
    TextButton(
        onClick = onClick
    ) {
        Text(
            text = if (galleryOpened) "Close Gallery" else "Show Gallery",
            style = TextStyle(fontSize = MaterialTheme.typography.bodySmall.fontSize)
        )
    }
}