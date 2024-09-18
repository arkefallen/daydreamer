package com.android.ark.ui.components

import androidx.compose.runtime.mutableStateListOf

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