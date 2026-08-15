package pl.intertell.client

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import pl.intertell.client.crash.CrashHandler
import pl.intertell.client.crash.CrashScreen
import pl.intertell.client.ui.IntertellApp
import pl.intertell.client.ui.theme.IntertellColors
import pl.intertell.client.ui.theme.IntertellTheme

class MainActivity : ComponentActivity() {

    private val viewModel: ClientViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        CrashHandler.install(this)
        val lastCrash = CrashHandler.readAndClearLastCrash(this)
        setContent {
            var crashText by remember { mutableStateOf(lastCrash) }
            IntertellTheme {
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
}
