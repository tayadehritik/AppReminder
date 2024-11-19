package com.hritik.appreminder.viewmodel.data

import com.hritik.appreminder.data.AppData

data class OverlayActivityState(
    var appData: AppData = AppData("com.hritik.test"),
    var timeSpent: Long = 0L
)
