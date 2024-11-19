package com.hritik.appreminder.viewmodel

import android.app.usage.UsageStatsManager
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hritik.appreminder.data.AppData
import com.hritik.appreminder.data.AppsDAO
import com.hritik.appreminder.viewmodel.data.OverlayActivityState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
class OverlayViewModel @Inject constructor(
    private val appsDAO: AppsDAO,
    private val usageStatsManager: UsageStatsManager
):ViewModel() {

    val startOfTheDay = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }

    val endOfTheDay = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 23)
        set(Calendar.MINUTE, 59)
        set(Calendar.SECOND, 59)
        set(Calendar.MILLISECOND, 999)
    }

    private val _overlayActivityState = MutableStateFlow<OverlayActivityState>(OverlayActivityState())
    val overlayActivityState = _overlayActivityState.asStateFlow()

    fun setOverlayActivityState(packageName:String?) {
        viewModelScope.launch(Dispatchers.IO) {
            packageName?.let {
                _overlayActivityState.value.appData = appsDAO.getAppdata(packageName) ?: AppData("com.hritik.test")
            }
            var usageResult= usageStatsManager.queryAndAggregateUsageStats(startOfTheDay.timeInMillis, endOfTheDay.timeInMillis)
            usageResult[packageName]?.let {
                var roundedTimeInForeground = (it.totalTimeInForeground.milliseconds.inWholeHours*60*60*1000) + (it.totalTimeInForeground.milliseconds.inWholeMinutes*60*1000)
                overlayActivityState.value.timeSpent = roundedTimeInForeground
            }
        }
    }

    fun extendTime(time:Long) {
        viewModelScope.launch(Dispatchers.IO) {
            _overlayActivityState.value.appData.let {
                it.extendedTime += time
                appsDAO.updateAppData(it)
            }
        }
    }
}