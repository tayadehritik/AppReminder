package com.hritik.appreminder.ui

import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import com.hritik.appreminder.R
import com.hritik.appreminder.data.AppData
import com.hritik.appreminder.data.AppsDAO
import com.hritik.appreminder.getAppNameForPackage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

class OverlayWindow @Inject constructor(
    val context:Context,
    val appsDAO: AppsDAO
) {
    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    private val rootView = layoutInflater.inflate(R.layout.overlay_window_layout,  null)

    var isWindowsOpen = false

    val buttonTimeMap = mapOf(
        R.id.plus_2_button to 2,
        R.id.plus_5_button to 5,
        R.id.plus_10_button to 10,
        R.id.plus_1_button to 1
    )

    private val windowParams = WindowManager.LayoutParams(
        0,
        0,
        0,
        0,
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            WindowManager.LayoutParams.TYPE_PHONE
        },
        WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                WindowManager.LayoutParams.FLAG_BLUR_BEHIND or
                WindowManager.LayoutParams.FLAG_DIM_BEHIND,
        PixelFormat.OPAQUE
    )

    private fun getCurrentDisplayMetrics(): DisplayMetrics {
        val dm = DisplayMetrics()
        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            context.resources.displayMetrics

        } else {
            windowManager.defaultDisplay.getMetrics(dm)
            dm
        }
    }


    private fun calculateSizeAndPosition(params: WindowManager.LayoutParams) {
        val dm = getCurrentDisplayMetrics()
        params.gravity = Gravity.BOTTOM or Gravity.LEFT
        params.width = (dm.widthPixels).toInt()
        params.height = (dm.heightPixels/2).toInt()
        params.x = (dm.widthPixels - params.width) / 2
        params.y = 0
        params.dimAmount = 0.5f

    }

    private fun initWindowParams() {
        calculateSizeAndPosition(windowParams)
    }

    private fun initWindow(appData: AppData?) {
        appData?.let {
            val spentPercentage = if(it.timeLimit == 0L) 0 else (it.timeSpent * 100) / it.timeLimit
            val limitLeft:Long = it.timeLimit - it.timeSpent

            val spentTodayText = "Spent today: ${it.timeSpent.milliseconds.inWholeHours % 24} hrs ${it.timeSpent.milliseconds.inWholeMinutes % 60} mins"
            val limitLeftText = "Limit left: ${limitLeft.milliseconds.inWholeHours % 24} hrs ${limitLeft.milliseconds.inWholeMinutes % 60} mins"

            rootView.findViewById<TextView>(R.id.spent_today_text).text = spentTodayText
            rootView.findViewById<TextView>(R.id.limit_left_text).text = limitLeftText
            rootView.findViewById<TextView>(R.id.package_name).text = context.getAppNameForPackage(it.packageName)
            rootView.findViewById<ProgressBar>(R.id.progress_indicator).progress = spentPercentage.toInt()


            buttonTimeMap.forEach { (buttonId, minutes) ->
                val button = rootView.findViewById<Button>(buttonId)
                button.isEnabled = limitLeft.milliseconds.inWholeMinutes >= minutes

                button.setOnClickListener { view ->
                    val extendedTime = appData.extendedTime + (minutes * 60 * 1000)
                    CoroutineScope(Dispatchers.IO).launch {
                        appsDAO.updateAppUsage(
                            it.packageName,
                            extendedTime.toLong(),
                            it.timeSpent
                        )
                    }
                    close()
                }
            }
        }

        rootView.findViewById<Button>(R.id.close_button).setOnClickListener {
            close()
            val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_HOME)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(homeIntent)
        }
    }

    init {
        initWindowParams()
        initWindow(null)
    }

    fun open(appData: AppData?) {
        if(!isWindowsOpen) {
            windowManager.addView(rootView, windowParams)
            initWindow(appData)
            isWindowsOpen = true
        }
    }

    fun close() {
        if(isWindowsOpen) {
            windowManager.removeView(rootView)
            isWindowsOpen = false
        }
    }

}