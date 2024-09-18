package com.android.ark.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.android.ark.util.Constants

@Entity(tableName = Constants.IMAGE_TO_UPLOAD_TABLE)
data class ImageToUpload(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var remoteImagePath: String,
    var imageUri: String,
    val sessionUri: String,
)