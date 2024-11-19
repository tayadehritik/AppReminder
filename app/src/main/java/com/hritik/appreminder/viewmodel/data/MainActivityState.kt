package com.hritik.appreminder.viewmodel.data

import com.hritik.appreminder.data.AppData

data class MainActivityState(
    var trackedApps:Map<String,AppData> = mapOf(),
    var usageStatsPermissionGranted: Boolean = false,
    var overlayPermissionGranted: Boolean = false,
    var notificationPermissionGranted: Boolean = false,
    var dialogState: DialogState = DialogState()
)

data class DialogState(
    var enabled: Boolean = false,
    var packageName: String = "com.hritik.test"
)