package com.hritik.appreminder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hritik.appreminder.data.AppData
import com.hritik.appreminder.data.AppsDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val appsDatabase: AppsDatabase
) : ViewModel() {

    private val _trackedPackages = MutableStateFlow<List<String>>(listOf())
    val trackedPackages = _trackedPackages.asStateFlow()

    init {
        viewModelScope.launch {
            appsDatabase.appsDAO().getAllPackages().collect {
                _trackedPackages.value = it
            }
        }
    }

    fun addPackage(packageName:String) {
        viewModelScope.launch {
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
        viewModelScope.launch {
            val appData = appsDatabase.appsDAO().getAppdata(packageName)

            appData?.let {
                appsDatabase.appsDAO().deleteAppData(it)
            }
        }
    }

}