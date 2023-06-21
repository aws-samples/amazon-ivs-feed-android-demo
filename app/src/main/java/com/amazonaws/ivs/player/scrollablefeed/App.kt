package com.amazonaws.ivs.player.scrollablefeed

import android.app.Application
import com.amazonaws.ivs.player.scrollablefeed.common.LineNumberDebugTree
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
open class App : Application() {
    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            Timber.plant(LineNumberDebugTree())
        }
    }
}
