package com.android.ark.daydreamer.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.android.ark.daydreamer.data.database.dao.ImagesToUploadDAO
import com.android.ark.daydreamer.data.database.entity.ImageToUpload

@Database(
    entities = [ImageToUpload::class],
    version = 1,
    exportSchema = false
)
abstract class ImageDatabase : RoomDatabase() {
    abstract fun imageToUploadDao(): ImagesToUploadDAO
}