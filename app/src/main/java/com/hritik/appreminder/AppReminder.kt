package com.hritik.appreminder

import android.app.Application
import android.app.usage.UsageStatsManager
import android.content.Context
import android.os.Build
import android.provider.Settings
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AppReminder: Application() {
    override fun onCreate() {
        super.onCreate()
    }
}

