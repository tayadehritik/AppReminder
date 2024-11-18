package com.hritik.appreminder.viewmodel

import android.Manifest
import android.app.Application
import android.app.usage.UsageStatsManager
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import android.widget.Toast
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TimePickerState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hritik.appreminder.data.AppData
import com.hritik.appreminder.data.AppsDAO
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val app: Application,
    private val appsDAO: AppsDAO,
    private val usageStatsManager: UsageStatsManager
) : ViewModel() {

    private val _mainActivityState = MutableStateFlow<MainActivityState>(MainActivityState())
    val mainActivityState = _mainActivityState.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            appsDAO.getAllAppData().collect {
                _mainActivityState.value = _mainActivityState.value.copy(
                   trackedApps = it.associateBy { it.packageName }
                )
            }
        }
    }

    fun addPackage(packageName:String) {
        viewModelScope.launch(Dispatchers.IO) {
            appsDAO.insertAppData(AppData(packageName))
        }
    }

    fun removePackage(packageName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val appData = appsDAO.getAppdata(packageName)
            appData?.let {
                appsDAO.deleteAppData(appData)
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    fun updateTimeLimit(packageName: String, timePickerState: TimePickerState) {
        viewModelScope.launch(Dispatchers.IO) {
            val appData = appsDAO.getAppdata(packageName)
            appData?.let {
                val newTimeLimit = (timePickerState.hour * 60 * 60 * 1000L) + (timePickerState.minute * 60 * 1000L)
                if(newTimeLimit > appData.timeSpent) {
                    appData.timeLimit = newTimeLimit
                    appsDAO.updateAppData(appData)
                }
                else {
                    withContext(Dispatchers.Main){
                        Toast.makeText(app, "Time limit cannot be less than time spent today", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    fun dailyReset() {
        viewModelScope.launch(Dispatchers.IO) {
            appsDAO.dailyReset()
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
            notificationPermissionGranted = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
                true
            } else {
                app.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            }

        )
    }

    fun setDialogEnabled(
        value: Boolean,
        packageName: String = _mainActivityState.value.dialogState.packageName
    ) {
        _mainActivityState.value = _mainActivityState.value.copy(
            dialogState = DialogState(enabled = value, packageName = packageName)
        )
    }
}