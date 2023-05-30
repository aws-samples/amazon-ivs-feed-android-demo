package com.amazonaws.ivs.player.scrollablefeed.di

import android.content.Context
import com.amazonaws.ivs.player.scrollablefeed.common.JSON_FILE_NAME
import com.amazonaws.ivs.player.scrollablefeed.common.asObject
import com.amazonaws.ivs.player.scrollablefeed.common.readJsonAsset
import com.amazonaws.ivs.player.scrollablefeed.models.StreamsModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object MainModule {
    @Provides
    @Singleton
    fun provideProductsModel(@ApplicationContext context: Context) =
        context.readJsonAsset(JSON_FILE_NAME).asObject<StreamsModel>().streams
}
