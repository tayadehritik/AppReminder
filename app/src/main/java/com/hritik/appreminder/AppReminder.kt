package com.hritik.appreminder

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AppReminder: Application() {
    override fun onCreate() {
        super.onCreate()
    }
}