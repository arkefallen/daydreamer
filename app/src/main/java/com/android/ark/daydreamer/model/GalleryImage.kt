package com.android.ark.daydreamer.model

import android.net.Uri

/**
 *
 * A class that represent single image in Gallery
 * @param image - store the URI of selected image
 * @param remoteImagePath - store the path of image in firebase storage
 *
 */

data class GalleryImage(
    val image: Uri,
    val remoteImagePath: String
)
