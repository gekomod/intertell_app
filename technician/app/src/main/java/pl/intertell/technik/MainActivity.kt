package pl.intertell.technik

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import pl.intertell.technik.crash.CrashHandler
import pl.intertell.technik.crash.CrashScreen
import pl.intertell.technik.ui.IntertellTechnikApp
import pl.intertell.technik.ui.theme.IntertellColors
import pl.intertell.technik.ui.theme.IntertellTheme

class MainActivity : ComponentActivity() {

    private val viewModel: TechnicianViewModel by viewModels()

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { /* no-op either way — just no notifications if denied */ }

    private val requestLocationPermission =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { /* no-op — just no route distance/time on the job map without it */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        askForNotificationPermission()
        askForLocationPermission()
        askToIgnoreBatteryOptimizations()
        val lastCrash = CrashHandler.readAndClearLastCrash(this)
        setContent {
            var crashText by remember { mutableStateOf(lastCrash) }
            IntertellTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = IntertellColors.AppBackground) {
                    val crash = crashText
                    if (crash != null) {
                        CrashScreen(crashText = crash, onDismiss = { crashText = null })
                    } else {
                        IntertellTechnikApp(viewModel)
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

    // Powers the "trasa X km · Y min" route estimate on the job map header —
    // without it, the map still shows, just without a live distance/time.
    private fun askForLocationPermission() {
        val fineGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        val coarseGranted = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        if (!fineGranted && !coarseGranted) {
            requestLocationPermission.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            )
        }
    }

    // Without this, MIUI/Samsung/etc.-style battery managers routinely kill the
    // WorkManager periodic poll outright, so "new zlecenie" notifications never
    // arrive even though the code behind them is correct — this is the single
    // biggest real-world cause of "missing" notifications on Android in Poland.
    private fun askToIgnoreBatteryOptimizations() {
        val powerManager = getSystemService(PowerManager::class.java) ?: return
        if (powerManager.isIgnoringBatteryOptimizations(packageName)) return
        try {
            startActivity(
                Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS, Uri.parse("package:$packageName")),
            )
        } catch (e: android.content.ActivityNotFoundException) {
            // some OEM ROMs strip this screen — nothing more we can do from here
        }
    }
}
