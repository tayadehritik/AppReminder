package com.hritik.appreminder.ui

import android.content.Context
import android.graphics.PixelFormat
import android.os.Build
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.view.ContextThemeWrapper
import com.hritik.appreminder.R

class OverlayWindow(val context:Context) {

    private val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    private val layoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    val rootView = layoutInflater.inflate(R.layout.overlay_window_layout,  null)

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


    private fun calculateSizeAndPosition(
        params: WindowManager.LayoutParams,
        widthInDp: Int,
        heightInDp: Int
    ) {

        val dm = getCurrentDisplayMetrics()
        params.gravity = Gravity.BOTTOM or Gravity.LEFT
        params.width = (dm.widthPixels).toInt()
        params.height = (dm.heightPixels/2).toInt()
        params.x = (dm.widthPixels - params.width) / 2
        params.y = 0
        params.dimAmount = 0.5f

    }

    private fun initWindowParams() {
        calculateSizeAndPosition(windowParams, 300, 80)
    }

    private fun initWindow() {
        rootView.findViewById<Button>(R.id.close_button).setOnClickListener {
            close()
        }


    }

    init {
        initWindowParams()
        initWindow()
    }

    fun open() {

        try {
            windowManager.addView(rootView, windowParams)
        } catch (e: Exception) {
            // Ignore exception for now, but in production, you should have some
            // warning for the user here.
            println(e.localizedMessage)
        }
    }

    fun close() {
        try {
            windowManager.removeView(rootView)
        } catch (e: Exception) {
            // Ignore exception for now, but in production, you should have some
            // warning for the user here.
        }
    }

}