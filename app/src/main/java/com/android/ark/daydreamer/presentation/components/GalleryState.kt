package com.android.ark.daydreamer.presentation.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import com.android.ark.daydreamer.model.GalleryImage

class GalleryState {
    val images = mutableStateListOf<GalleryImage>()
    val imagesToBeDeleted = mutableStateListOf<GalleryImage>()

    fun addImage(image: GalleryImage) {
        images.add(image)
    }

    fun removeImage(image: GalleryImage) {
        images.remove(image)
    }

    fun clearImagesToBeDeleted() {
        imagesToBeDeleted.clear()
    }
}