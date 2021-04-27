package com.amazonaws.ivs.player.scrollablefeed

import android.app.Application
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import com.amazonaws.ivs.player.scrollablefeed.common.LineNumberDebugTree
import timber.log.Timber

open class App : Application(), ViewModelStoreOwner {

    override fun getViewModelStore() = appViewModelStore

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.DEBUG) {
            Timber.plant(LineNumberDebugTree("Amazon_IVS"))
        }
    }

    companion object {
        private val appViewModelStore: ViewModelStore by lazy {
            ViewModelStore()
        }
    }
}
