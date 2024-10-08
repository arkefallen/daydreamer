package com.android.ark.daydreamer.data.database.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.ark.daydreamer.data.database.entity.ImageToUpload

@Dao
interface ImagesToUploadDAO {
    @Query("SELECT * FROM image_to_upload ORDER BY id ASC")
    suspend fun getAllImages(): List<ImageToUpload>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addImageToUpload(imageToUpload: ImageToUpload)

    @Query("DELETE FROM image_to_upload WHERE id=:imageId")
    suspend fun cleanupImage(imageId: Int)
}