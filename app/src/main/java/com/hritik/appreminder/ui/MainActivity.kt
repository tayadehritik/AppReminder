package com.hritik.appreminder.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TimeInput
import androidx.compose.material3.TimePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.lifecycleScope
import com.hritik.appreminder.service.AppReminderService
import com.hritik.appreminder.ui.theme.AppReminderTheme
import com.hritik.appreminder.viewmodel.MainViewModel
import com.hritik.appreminder.viewmodel.MainActivityState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds


@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel:MainViewModel by viewModels()
    private var installedPackages = listOf<String>()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val serviceIntent = Intent(this, AppReminderService::class.java)

        enableEdgeToEdge()
        lifecycleScope.launch(Dispatchers.IO) {
            fetchAllInstalledPackages()
        }
        setContent {
            AppReminderTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->

                    val mainActivityState by mainViewModel.mainActivityState.collectAsState()

                    Column(modifier = Modifier.padding(innerPadding)) {
                        ButtonGrid(
                            mainActivityState = mainActivityState,
                            grantUsageStatsPermission = { grantUsageStatsPermission() },
                            grantOverlayPermission = { grantOverlayPermission() },
                            grantNotificationPermission = { requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS),0) },
                            startForegroundService = { startService(serviceIntent) },
                            dailyReset = { mainViewModel.dailyReset() }
                        )
                        HorizontalDivider()
                        LazyColumn(
                            modifier = Modifier.padding(10.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(installedPackages) { installedPackage ->
                                appItem(
                                    name = installedPackage,
                                    timeLimit = mainActivityState.trackedApps.get(installedPackage)?.timeLimit ?: 0 ,
                                    checked = installedPackage in mainActivityState.trackedApps.keys,
                                    onCheckChanged = { value ->
                                        if(value) mainViewModel.addPackage(installedPackage)
                                        else mainViewModel.removePackage(installedPackage)
                                    },
                                    onClicked = {
                                        mainViewModel.setDialogEnabled(true, installedPackage)
                                    }
                                )
                            }
                        }
                        if(mainActivityState.dialogState.enabled) {
                            val localContext = LocalContext.current
                            timeDialog(
                                timeLimit = mainActivityState.trackedApps.get(mainActivityState.dialogState.packageName)?.timeLimit ?: 0,
                                onDismissRequest = {
                                    mainViewModel.setDialogEnabled(false)
                                },
                                onConfirmRequest = { timePickerState ->
                                    mainViewModel.updateTimeLimit(packageName = mainActivityState.dialogState.packageName, timePickerState = timePickerState,)
                                    mainViewModel.setDialogEnabled(false)
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    fun fetchAllInstalledPackages() {
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        installedPackages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.queryIntentActivities(
                mainIntent,
                PackageManager.ResolveInfoFlags.of(0L)
            )
        } else {
            packageManager.queryIntentActivities(mainIntent, 0)
        }.map { it.activityInfo.packageName }
    }

    private fun grantUsageStatsPermission() {
        val usageAccessIntent = Intent(
            Settings.ACTION_USAGE_ACCESS_SETTINGS,
            Uri.parse("package:$packageName")
        )

        startActivity(usageAccessIntent)
    }

    private fun grantOverlayPermission() {
        val overlayIntent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        startActivity(overlayIntent)
    }

    override fun onResume() {
        super.onResume()
        mainViewModel.checkUsageStatsPermissionGranted()
        mainViewModel.checkOverlayPermissionGranted()
        mainViewModel.checkNotificationPermissionGranted()

    }
}

@Preview
@Composable
fun ButtonGrid(
    mainActivityState: MainActivityState = MainActivityState(),
    grantUsageStatsPermission:() -> Unit = {},
    grantOverlayPermission:() -> Unit = {},
    grantNotificationPermission:() -> Unit = {},
    startForegroundService:() -> Unit = {},
    dailyReset:() -> Unit = {}
) {
    Column(
        modifier = Modifier.padding(10.dp)
    ){
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = grantUsageStatsPermission,
            enabled = mainActivityState.usageStatsPermissionGranted.not()
        ) {
            Text("Grant Usage Stats Permission")
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = grantOverlayPermission,
            enabled = mainActivityState.overlayPermissionGranted.not()
        ) {
            Text("Grant Draw Over Other Apps Permission")
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = grantNotificationPermission,
            enabled = mainActivityState.notificationPermissionGranted.not()
        ) {
            Text("Grant Notification Permission")
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = startForegroundService,
            enabled = (
                    mainActivityState.usageStatsPermissionGranted &&
                            mainActivityState.overlayPermissionGranted &&
                            mainActivityState.notificationPermissionGranted
                    )
        ) {
            Text("Start Foreground Service")
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = dailyReset,
            enabled = true
        ) {
            Text("Daily Reset")
        }
    }
}


@Preview
@Composable
fun appItem(
    name:String = "com.spotify.music",
    timeLimit:Long = 0,
    checked: Boolean = false,
    onCheckChanged:(Boolean) -> Unit = {},
    onClicked:() -> Unit = {}
) {
    OutlinedCard(onClick = onClicked) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(5.dp)) {
                    Text(
                        modifier = Modifier.fillMaxWidth(0.8f),
                        style = MaterialTheme.typography.titleMedium,
                        text =name)
                    Text(
                        style = MaterialTheme.typography.labelMedium,
                        text = "${timeLimit.milliseconds.inWholeHours % 24} hrs ${timeLimit.milliseconds.inWholeMinutes % 60} mins Limit")
                }
                Checkbox(
                    checked = checked,
                    onCheckedChange = onCheckChanged
                )

            }
            HorizontalDivider()

        }
    }
}

@Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun timeDialog(
    timeLimit: Long = 0,
    onDismissRequest:() -> Unit = {},
    onConfirmRequest:(TimePickerState) -> Unit = {}
) {
    val timePickerState = rememberTimePickerState(
        initialHour = timeLimit.milliseconds.inWholeHours.toInt() % 24,
        initialMinute = timeLimit.milliseconds.inWholeMinutes.toInt() % 60,
        is24Hour = true,
    )

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                TimeInput(timePickerState)
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                ) {
                    Button(
                        onClick = onDismissRequest,
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text("Dismiss")
                    }
                    Button(
                        onClick = {onConfirmRequest(timePickerState)},
                        modifier = Modifier.padding(8.dp),
                    ) {
                        Text("Confirm")
                    }
                }
            }
        }
    }
}

