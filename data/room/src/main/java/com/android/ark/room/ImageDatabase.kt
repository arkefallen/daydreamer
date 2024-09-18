package com.android.ark.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.android.ark.room.dao.ImagesToDeleteDAO
import com.android.ark.room.dao.ImagesToUploadDAO
import com.android.ark.room.entity.ImageToDelete
import com.android.ark.room.entity.ImageToUpload

@Database(
    entities = [ImageToUpload::class, ImageToDelete::class],
    version = 2,
    exportSchema = true,
)
abstract class ImageDatabase : RoomDatabase() {
    abstract fun imageToUploadDao(): ImagesToUploadDAO
    abstract fun imageToDeleteDao(): ImagesToDeleteDAO
}

val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // Create the new table for `image_to_delete`
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `image_to_delete` (
                `id` INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT,
                `remoteImagePath` TEXT NOT NULL
            )
            """.trimIndent()
        )
    }
}