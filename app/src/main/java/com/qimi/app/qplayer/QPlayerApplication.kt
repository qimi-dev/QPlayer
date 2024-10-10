package com.qimi.app.qplayer

import android.app.Application
import com.qimi.app.qplayer.sync.work.initializers.SyncInitializer
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class QPlayerApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        SyncInitializer.initialize(this)
    }

}
