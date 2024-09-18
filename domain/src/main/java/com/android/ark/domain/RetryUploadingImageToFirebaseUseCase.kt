package com.android.ark.domain

import androidx.core.net.toUri
import com.android.ark.room.entity.ImageToUpload
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storageMetadata

class RetryUploadingImageToFirebaseUseCase {
    operator fun invoke(
        imageToUpload: ImageToUpload,
        onSuccess: () -> Unit
    ) {
        val storage = FirebaseStorage.getInstance().reference
        storage.child(imageToUpload.remoteImagePath).putFile(
            imageToUpload.imageUri.toUri(),
            storageMetadata {  },
            imageToUpload.sessionUri.toUri()
        ).addOnSuccessListener {
            onSuccess()
        }
    }
}