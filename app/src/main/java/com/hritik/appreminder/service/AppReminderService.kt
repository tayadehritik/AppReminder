package com.hritik.appreminder.service

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.hritik.appreminder.ui.MainActivity
import java.util.Timer
import kotlin.concurrent.timerTask
import com.hritik.appreminder.R
import com.hritik.appreminder.data.AppData
import com.hritik.appreminder.data.AppsDAO
import com.hritik.appreminder.ui.OverlayActivity
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class AppReminderService :Service() {

    @Inject lateinit var appsDAO: AppsDAO
    @Inject lateinit var usageStatsManager: UsageStatsManager

    var timer = Timer()
    var trackedApps = mapOf<String, AppData>()
    var activeApp:AppData? = null

    override fun onCreate() {
        super.onCreate()
        CoroutineScope(Dispatchers.IO).launch {
            appsDAO.getAllAppData().collect { appsData ->
                trackedApps = appsData.associateBy { it.packageName }
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val overlayIntent = Intent(this, OverlayActivity::class.java)
        overlayIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS or Intent.FLAG_ACTIVITY_CLEAR_TASK

        val notificationIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(this,
            0,notificationIntent,PendingIntent.FLAG_IMMUTABLE)

        val notification = NotificationCompat.Builder(this, "MY_CHANNEL_ID")
                .setContentTitle("App Reminder Service")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentIntent(pendingIntent)
                .setForegroundServiceBehavior(Notification.FOREGROUND_SERVICE_IMMEDIATE)
                .setOngoing(true)

        startForeground(1, notification.build())

        timer.schedule(timerTask {
            CoroutineScope(Dispatchers.IO).launch {
                monitorEvents()
                logUsage(overlayIntent)
            }
        }, 0,  1000)

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    fun monitorEvents() {
        val events = usageStatsManager.queryEvents(System.currentTimeMillis()-(1000), System.currentTimeMillis())
        val event = UsageEvents.Event()
        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            val appData = trackedApps[event.packageName]
            if(appData != null){
                when(event.eventType) {
                    UsageEvents.Event.ACTIVITY_RESUMED -> {
                        activeApp = appData
                    }
                    UsageEvents.Event.ACTIVITY_PAUSED -> {
                        activeApp = null
                    }
                }
            }
        }
    }

    suspend fun logUsage(overlayIntent:Intent) {
        activeApp?.let { activeApp ->
            if(activeApp.extendedTime > 0){
                activeApp.extendedTime -= 1000L
                appsDAO.updateAppData(activeApp)
            }
            else{
                overlayIntent.putExtra("packageName",activeApp.packageName)
                startActivity(overlayIntent)
            }
        }
    }
}