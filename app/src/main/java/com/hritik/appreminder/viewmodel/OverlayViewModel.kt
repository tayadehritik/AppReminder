package com.hritik.appreminder.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hritik.appreminder.data.AppData
import com.hritik.appreminder.data.AppsDAO
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OverlayViewModel @Inject constructor(
    private val appsDAO: AppsDAO
):ViewModel() {

    private val _appData = MutableStateFlow<AppData?>(null)
    val  appData = _appData.asStateFlow()

    fun getAppData(packageName:String?) {
        viewModelScope.launch(Dispatchers.IO) {
            packageName?.let {
                _appData.value = appsDAO.getAppdata(packageName)
            }
        }
    }

    fun extendTime(time:Long) {
        viewModelScope.launch(Dispatchers.IO) {
            _appData.value?.let {
                it.extendedTime += time
                appsDAO.updateAppData(it)
            }
        }
    }
}