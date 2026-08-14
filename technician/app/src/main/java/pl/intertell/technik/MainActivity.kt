package pl.intertell.technik

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import pl.intertell.technik.ui.IntertellTechnikApp
import pl.intertell.technik.ui.theme.IntertellColors
import pl.intertell.technik.ui.theme.IntertellTheme

class MainActivity : ComponentActivity() {

    private val viewModel: TechnicianViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            IntertellTheme {
                Surface(modifier = Modifier.fillMaxSize(), color = IntertellColors.AppBackground) {
                    IntertellTechnikApp(viewModel)
                }
            }
        }
    }
}
