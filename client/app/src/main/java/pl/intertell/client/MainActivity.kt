package pl.intertell.client

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import pl.intertell.client.crash.CrashHandler
import pl.intertell.client.crash.CrashScreen
import pl.intertell.client.ui.IntertellApp
import pl.intertell.client.ui.theme.IntertellColors
import pl.intertell.client.ui.theme.IntertellTheme

class MainActivity : ComponentActivity() {

    private val viewModel: ClientViewModel by viewModels()

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op either way — just no notifications if denied */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        askForNotificationPermission()
        val lastCrash = CrashHandler.readAndClearLastCrash(this)
        setContent {
            var crashText by remember { mutableStateOf(lastCrash) }
            val themeMode by viewModel.themeMode.collectAsState()
            IntertellTheme(themeMode = themeMode) {
                Surface(modifier = Modifier.fillMaxSize(), color = IntertellColors.AppBackground) {
                    val crash = crashText
                    if (crash != null) {
                        CrashScreen(crashText = crash, onDismiss = { crashText = null })
                    } else {
                        IntertellApp(viewModel)
                    }
                }
            }
        }
    }

    private fun askForNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) return
        val granted = ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED
        if (!granted) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
