package com.android.ark.daydreamer.di

import android.content.Context
import com.android.ark.daydreamer.utils.NetworkConnectivityObserver
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {
    @Provides
    @Singleton
    fun provideNetworkConnectivityObserver(
        @ApplicationContext context: Context
    ) = NetworkConnectivityObserver(context)
}