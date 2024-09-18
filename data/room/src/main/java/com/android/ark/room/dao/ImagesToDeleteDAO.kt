package com.android.ark.room.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.android.ark.room.entity.ImageToDelete

@Dao
interface ImagesToDeleteDAO {
    @Query("SELECT * FROM image_to_delete ORDER BY id ASC")
    suspend fun getAllImages() : List<ImageToDelete>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun storeImageToDelete(imageToDelete: ImageToDelete)

    @Query("DELETE FROM image_to_delete WHERE id=:imageId")
    suspend fun removeImageToDelete(imageId: Int)
}