package com.hritik.appreminder

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AppReminder: Application() {
    override fun onCreate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "MY_CHANNEL"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val mChannel = NotificationChannel("MY_CHANNEL_ID", name, importance)
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }
        super.onCreate()
    }
}

fun Context.getAppNameForPackage(packageName:String?): String? {
    packageName?.let {
        val appInfo = packageManager.getApplicationInfo(packageName,0)
        val appLabel = packageManager.getApplicationLabel(appInfo)
        return appLabel.toString()
    }
    return null
}

