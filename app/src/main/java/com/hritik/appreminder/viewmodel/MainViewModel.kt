package com.hritik.appreminder.viewmodel

import android.app.usage.UsageStatsManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hritik.appreminder.data.AppData
import com.hritik.appreminder.data.AppsDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val appsDatabase: AppsDatabase,
    private val usageStatsManager: UsageStatsManager
) : ViewModel() {

    private val _trackedPackages = MutableStateFlow<List<String>>(listOf())
    val trackedPackages = _trackedPackages.asStateFlow()

    private val _usageStatsPermissionGranted = MutableStateFlow<Boolean>(false)
    val usageStatsPermissionGranted = _usageStatsPermissionGranted.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.IO) {
            appsDatabase.appsDAO().getAllPackages().collect {
                _trackedPackages.value = it
            }
        }
    }

    fun addPackage(packageName:String) {
        viewModelScope.launch(Dispatchers.IO) {
            appsDatabase.appsDAO().insertAppData(
                AppData(
                    packageName = packageName,
                    timeLimit = null,
                    timeSpent = null,
                    extendedTime = null
                )
            )
        }
    }

    fun removePackage(packageName: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val appData = appsDatabase.appsDAO().getAppdata(packageName)
            appData?.let {
                appsDatabase.appsDAO().deleteAppData(it)
            }
        }
    }

    fun checkUsageStatsPermissionGranted() {
        val usageStats = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_BEST,
            0,
            System.currentTimeMillis()
        )
        _usageStatsPermissionGranted.value = usageStats.isNotEmpty()
    }

}