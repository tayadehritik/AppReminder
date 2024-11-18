package com.hritik.appreminder.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.ProgressIndicatorDefaults
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.hritik.appreminder.data.AppData
import com.hritik.appreminder.viewmodel.OverlayViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.runtime.getValue
import com.hritik.appreminder.ui.theme.AppReminderTheme
import kotlin.time.Duration.Companion.milliseconds

@AndroidEntryPoint
class OverlayActivity : ComponentActivity() {

    private val overlayViewModel:OverlayViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val packageName: String? = intent.getStringExtra("packageName")
        overlayViewModel.getAppData(packageName)
        enableEdgeToEdge()
        setContent {
            AppReminderTheme {
                Surface (
                    modifier = Modifier.fillMaxSize(),
                    color = Color.Transparent
                ) {
                    val appData by overlayViewModel.appData.collectAsState()
                    val sheetState = rememberModalBottomSheetState(
                        confirmValueChange = { it ->
                            !(it == SheetValue.Hidden || it == SheetValue.Expanded)
                        }
                    )
                    ModalBottomSheet(
                        sheetState = sheetState,
                        onDismissRequest = {},
                        windowInsets = WindowInsets(0,0,0,0),
                        dragHandle = {}
                    ) {
                        Column(
                            modifier = Modifier.padding(15.dp,20.dp,15.dp,40.dp),
                            verticalArrangement = Arrangement.spacedBy(15.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            UsageCard(appData = appData)
                            Text(
                                text = "How long do you want to use?",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            ButtonGrid(
                                appData = appData,
                                onTimeExtended = {
                                    overlayViewModel.extendTime(it)
                                    finish()
                                 },
                                onClose = { closeApplication() }
                            )
                        }
                    }
                }
            }
        }
    }

    fun closeApplication() {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        this.startActivity(homeIntent)
        finishAffinity()
    }
}



@Preview
@Composable
fun UsageCard(appData: AppData? = AppData("com.hritik.appreminder")) {

    var appData = appData?: AppData("com.hritik.appreminder")
    val limitLeft:Long = appData.timeLimit - appData.timeSpent
    val progress:Float = if(appData.timeLimit == 0L) 0f else (appData.timeSpent.toFloat() / appData.timeLimit.toFloat())

    Column(
        modifier = Modifier.padding(10.dp).fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text(
            text = appData.packageName,
            style = MaterialTheme.typography.titleLarge
        )
        HorizontalDivider()
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier.fillMaxWidth().height(20.dp).clip(RoundedCornerShape(12.dp)),
            strokeCap = ProgressIndicatorDefaults.LinearStrokeCap
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Absolute.SpaceBetween
        ) {
            Column(
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = "${appData.timeSpent.milliseconds.inWholeHours % 24} hrs ${appData.timeSpent.milliseconds.inWholeMinutes % 60} mins",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Spent today",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Column(
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = "${limitLeft.milliseconds.inWholeHours % 24} hrs ${limitLeft.milliseconds.inWholeMinutes % 60} mins",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Limit left",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

        }
        HorizontalDivider()
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Preview
@Composable
fun ButtonGrid(
    appData: AppData? = AppData("com.hritik.appreminder"),
    onTimeExtended:(Long) -> Unit = {},
    onClose:() -> Unit = {}
) {
    val minutes = listOf(1,2,5,10)
    var appData = appData?: AppData("com.hritik.appreminder")
    val limitLeft:Long = appData.timeLimit - appData.timeSpent

    FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        maxItemsInEachRow = 2
    ) {
        for(time in minutes) {
            Button(
                enabled = limitLeft.milliseconds.inWholeMinutes >= time,
                modifier = Modifier.weight(1f).padding(2.5.dp),
                onClick = { onTimeExtended(time*60*1000L) }
            ) {
                Text("$time mins")
            }
        }
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onClose
        ) {
            Text("Close")
        }
    }

}



