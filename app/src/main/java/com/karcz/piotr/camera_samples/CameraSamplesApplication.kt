package com.karcz.piotr.camera_samples

import android.app.Application
import timber.log.Timber.DebugTree
import timber.log.Timber.Forest.plant

class CameraSamplesApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        if (BuildConfig.DEBUG) {
            plant(DebugTree())
        }
    }
}
