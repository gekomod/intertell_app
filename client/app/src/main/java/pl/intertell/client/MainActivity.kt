package pl.intertell.client

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import pl.intertell.client.ui.IntertellApp
import pl.intertell.client.ui.theme.IntertellColors
import pl.intertell.client.ui.theme.IntertellTheme

class MainActivity : ComponentActivity() {

    private val viewModel: ClientViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IntertellTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = IntertellColors.AppBackground) {
                    IntertellApp(viewModel)
                }
            }
        }
    }
}
