package com.android.ark.domain

import android.net.Uri
import com.google.firebase.storage.FirebaseStorage

class GetImagesFromFirebaseUseCase {
    operator fun invoke(
        remoteImagePaths: List<String>,
        onSuccessFetched: (Uri) -> Unit = {},
        onFailedFetched: (Exception) -> Unit ={},
        onReadyToDisplay: () -> Unit = {},
    ) {
        if (remoteImagePaths.isNotEmpty()) {
            remoteImagePaths.forEachIndexed { index, remoteImagePath ->
                if (remoteImagePath.trim().isNotEmpty()) {
                    FirebaseStorage.getInstance().reference.child(remoteImagePath.trim()).downloadUrl
                        .addOnSuccessListener {
                            onSuccessFetched(it)
                            if (remoteImagePaths.lastIndex == index) {
                                onReadyToDisplay()
                            }
                        }.addOnFailureListener {
                            onFailedFetched(it)
                        }
                }
            }
        }
    }
}