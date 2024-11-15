package com.hritik.appreminder.viewmodel

import android.Manifest
import android.app.Application
import android.app.usage.UsageStatsManager
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hritik.appreminder.data.AppData
import com.hritik.appreminder.data.AppsDatabase
import com.hritik.appreminder.viewmodel.data.DialogState
import com.hritik.appreminder.viewmodel.data.MainActivityState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val app: Application,
    private val appsDatabase: AppsDatabase,
    private val usageStatsManager: UsageStatsManager
) : ViewModel() {

    private val _mainActivityState = MutableStateFlow<MainActivityState>(MainActivityState())
    val mainActivityState = _mainActivityState.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            appsDatabase.appsDAO().getAllAppData().collect {
                _mainActivityState.value = _mainActivityState.value.copy(
                   trackedApps = it.associateBy { it.packageName }
                )
            }
        }
    }

    fun addPackage(packageName:String) {
        viewModelScope.launch(Dispatchers.IO) {
            appsDatabase.appsDAO().insertAppData(
                AppData(
                    packageName = packageName,
                    timeLimit = 0,
                    timeSpent = null,
                    extendedTime = null
                )
            )
        }
    }

    fun removePackage(packageName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val appData = _mainActivityState.value.trackedApps.get(packageName)
            appData?.let {
                appsDatabase.appsDAO().deleteAppData(it)
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    fun updateTimeLimit(packageName: String?, timePickerState: TimePickerState) {
        viewModelScope.launch(Dispatchers.IO) {
            val appData = _mainActivityState.value.trackedApps.get(packageName)
            appData?.let {
                it.timeLimit = (timePickerState.hour * 60 * 60 * 1000L) + (timePickerState.minute * 60 * 1000L)
                println(it)
                appsDatabase.appsDAO().updateAppData(it)
            }
        }
    }

    fun checkUsageStatsPermissionGranted() {
        val usageStats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_BEST,
            0,
            System.currentTimeMillis()
        )
        _mainActivityState.value = _mainActivityState.value.copy(
            usageStatsPermissionGranted = usageStats.isNotEmpty()
        )
    }

    fun checkOverlayPermissionGranted() {
        _mainActivityState.value = _mainActivityState.value.copy(
            overlayPermissionGranted = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                true
            } else {
                Settings.canDrawOverlays(app)
            }
        )
    }

    fun checkNotificationPermissionGranted() {
        _mainActivityState.value = _mainActivityState.value.copy(
            notificationPermissionGranted = app.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        )
    }

    fun setDialogEnabled(
        value: Boolean,
        packageName: String? = _mainActivityState.value.dialogState.packageName
    ) {
        _mainActivityState.value = _mainActivityState.value.copy(
            dialogState = DialogState(enabled = value, packageName = packageName)
        )
    }

}