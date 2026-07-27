package com.silverymusic.app

import android.app.Application
import com.silverymusic.app.data.AppContainer

class SilveryApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppContainer.init(this)
        AppContainer.installLiveImplementations()
    }
}
