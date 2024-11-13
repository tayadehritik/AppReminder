package com.hritik.appreminder.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.lifecycle.lifecycleScope
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
        enableEdgeToEdge()
        lifecycleScope.launch(Dispatchers.IO) {
            fetchAllInstalledPackages()
        }
        setContent {
            AppReminderTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    println(innerPadding)
                    val trackedPackages by mainViewModel.trackedPackages.collectAsState()
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
}

