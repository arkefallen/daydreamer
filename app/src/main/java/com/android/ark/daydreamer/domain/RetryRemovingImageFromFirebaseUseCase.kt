package com.android.ark.daydreamer.domain

import com.android.ark.daydreamer.data.database.entity.ImageToDelete
import com.google.firebase.storage.FirebaseStorage

class RetryRemovingImageFromFirebaseUseCase {
    operator fun invoke(
        imageToDelete: ImageToDelete,
        onSuccess: () -> Unit
    ) {
        val storage = FirebaseStorage.getInstance().reference
        storage.child(imageToDelete.remoteImagePath).delete()
            .addOnSuccessListener { onSuccess() }
    }
}