package pl.intertell.client

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import pl.intertell.client.auth.authenticateBiometric
import pl.intertell.client.crash.CrashHandler
import pl.intertell.client.crash.CrashScreen
import pl.intertell.client.ui.IntertellApp
import pl.intertell.client.ui.theme.IntertellColors
import pl.intertell.client.ui.theme.IntertellTheme

// FragmentActivity (not plain ComponentActivity) — androidx.biometric.BiometricPrompt
// requires it (see auth/BiometricAuth.kt). FragmentActivity is itself a
// ComponentActivity, so viewModels()/setContent/registerForActivityResult
// below are unaffected.
class MainActivity : FragmentActivity() {

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
            val uiState by viewModel.uiState.collectAsState()

            // Fires once each time awaitingBiometric flips true (a stored
            // session exists and the user opted into biometric unlock —
            // see ClientViewModel's splash-time logic) — shows the OS
            // fingerprint/face prompt and routes the result back into the
            // ViewModel, which decides where to navigate from there.
            LaunchedEffect(uiState.awaitingBiometric) {
                if (uiState.awaitingBiometric) {
                    authenticateBiometric(
                        activity = this@MainActivity,
                        onSuccess = viewModel::onBiometricSuccess,
                        onError = viewModel::onBiometricFailed,
                    )
                }
            }

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
