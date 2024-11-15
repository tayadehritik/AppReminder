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
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import com.hritik.appreminder.service.AppReminderService
import com.hritik.appreminder.ui.theme.AppReminderTheme
import com.hritik.appreminder.viewmodel.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel:MainViewModel by viewModels()
    private var installedPackages = listOf<String>()


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
                    val trackedPackages by mainViewModel.trackedPackages.collectAsState()
                    val usageStatsPermissionGranted by mainViewModel.usageStatsPermissionGranted.collectAsState()
                    val overlayPermissionGranted by mainViewModel.overlayPermissionGranted.collectAsState()
                    val notificationPermissionGranted by mainViewModel.notificationPermissionGranted.collectAsState()

                    Column(modifier = Modifier.padding(innerPadding)) {
                        Button(
                            onClick = { grantUsageStatsPermission() },
                            enabled = usageStatsPermissionGranted.not()
                        ) {
                            Text("Grant Usage Stats Permission")
                        }
                        Button(
                            onClick = { grantOverlayPermission() },
                            enabled = overlayPermissionGranted.not()
                        ) {
                            Text("Grant Draw Over Other Apps Permission")
                        }
                        Button(
                            onClick = { requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS),0) },
                            enabled = notificationPermissionGranted.not()
                        ) {
                            Text("Grant Notification Permission")
                        }
                        Button(
                            onClick = { startService(serviceIntent) },
                            enabled = true
                        ) {
                            Text("Start Foreground Service")
                        }
                        LazyColumn {
                            items(installedPackages) { installedPackage ->
                                Row {
                                    Text(installedPackage)
                                    Checkbox(
                                        checked = installedPackage in trackedPackages,
                                        onCheckedChange = { value ->
                                            if(value) mainViewModel.addPackage(installedPackage)
                                            else mainViewModel.removePackage(installedPackage)
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun fetchAllInstalledPackages() {
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

