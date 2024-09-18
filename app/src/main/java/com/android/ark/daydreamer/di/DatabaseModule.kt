package com.android.ark.daydreamer.di

import android.content.Context
import androidx.room.Room
import com.android.ark.room.ImageDatabase
import com.android.ark.room.MIGRATION_1_2
import com.android.ark.util.Constants
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context,
    ): ImageDatabase {
        return Room.databaseBuilder(
            context = context,
            klass = ImageDatabase::class.java,
            name = Constants.DATABASE_NAME
        )
            .addMigrations(MIGRATION_1_2)
            .build()
    }

    @Provides
    @Singleton
    fun provideFirstDao(database: ImageDatabase) = database.imageToUploadDao()

    @Provides
    @Singleton
    fun provideSecondDao(database: ImageDatabase) = database.imageToDeleteDao()

}