package com.hritik.appreminder.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.hritik.appreminder.ui.MainActivity
import java.util.Timer
import kotlin.concurrent.timerTask
import com.hritik.appreminder.R
import com.hritik.appreminder.data.AppData
import com.hritik.appreminder.data.AppsDatabase
import com.hritik.appreminder.ui.OverlayWindow
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AppReminderService:Service() {
    var timer = Timer()
    var currentlyOpenedApp = "NA"

    @Inject
    lateinit var appsDatabase: AppsDatabase

    val trackedApps = MutableStateFlow<List<AppData>>(listOf())
    val trackedPackages = MutableStateFlow<List<String>>(listOf())


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        CoroutineScope(Dispatchers.IO).launch {
            appsDatabase.appsDAO().getAllAppData().collect { appsData ->
                trackedApps.value = appsData
                trackedPackages.value = appsData.map { it.packageName }
            }
        }

        try {
            val notificationManager = this.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val notificationIntent = Intent(this, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(this,
                0,notificationIntent,PendingIntent.FLAG_IMMUTABLE)

            val notification = NotificationCompat.Builder(this, "MY_CHANNEL_ID")
                    .setContentTitle("App Reminder Service")
                    .setContentText(currentlyOpenedApp)
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .setContentIntent(pendingIntent)
                    .setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)
                    .setOngoing(true)

            startForeground(1, notification.build())

            //create a timer
            //update ContextText with currently opened app
            // notification.setContextText()
            // notificationManager.notify("MY_CHANNEL_ID", notificationBuilder.build())
            val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
            val overylayWindow = OverlayWindow(this)

            timer.schedule(timerTask {
                val events = usageStatsManager.queryEvents(System.currentTimeMillis()-1000, System.currentTimeMillis())
                val event = UsageEvents.Event()
                println("---------------------------")
                while (events.hasNextEvent()) {
                    events.getNextEvent(event)
                    if(event.packageName in trackedPackages.value && event.eventType.equals(UsageEvents.Event.ACTIVITY_RESUMED)) {
                        CoroutineScope(Dispatchers.Main).launch {
                            overylayWindow.open()
                        }
                    }
                    else if(event.packageName in trackedPackages.value && event.eventType.equals(UsageEvents.Event.ACTIVITY_STOPPED)) {
                        CoroutineScope(Dispatchers.Main).launch {
                            overylayWindow.close()
                        }
                    }
                    if(event.eventType.equals(UsageEvents.Event.ACTIVITY_RESUMED))
                        println("Activity Resumed ${event.packageName}")
                    else if(event.eventType.equals(UsageEvents.Event.ACTIVITY_STOPPED))
                        println("Activity Stopped ${event.packageName}")
                }
                println("---------------------------")
            }, 0,  1000)
        }
        catch (e:Exception) {
            println(e.message)
        }
        finally {

        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }
}