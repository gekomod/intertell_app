package pl.intertell.technik.crash

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import pl.intertell.technik.ui.components.SolidButton

/**
 * Shown on launch instead of the normal app when [CrashHandler] found a
 * stack trace from the previous run — this device has no ADB, so this is
 * the only way to actually get the crash text back to whoever's debugging it.
 */
@Composable
fun CrashScreen(crashText: String, onDismiss: () -> Unit) {
    val clipboard = LocalClipboardManager.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0E1B2A))
            .padding(20.dp),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text("Aplikacja się zamknęła", style = androidx.compose.ui.text.TextStyle(fontSize = 22.sp, color = Color.White))
            Text(
                "Skopiuj poniższy tekst i wyślij go, żeby naprawić błąd.",
                style = androidx.compose.ui.text.TextStyle(fontSize = 13.sp, color = Color(0xB3FFFFFF)),
                modifier = Modifier.padding(top = 6.dp, bottom = 16.dp),
            )
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0x1AFFFFFF))
                    .padding(14.dp)
                    .verticalScroll(rememberScrollState()),
            ) {
                Text(
                    crashText,
                    style = androidx.compose.ui.text.TextStyle(fontSize = 11.sp, color = Color.White, fontFamily = FontFamily.Monospace),
                )
            }
            Column(modifier = Modifier.padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                SolidButton(
                    text = "Kopiuj",
                    onClick = { clipboard.setText(AnnotatedString(crashText)) },
                    background = Color(0xFF22A06B),
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(Color(0x1AFFFFFF))
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Zamknij", style = androidx.compose.ui.text.TextStyle(fontSize = 15.sp, color = Color.White))
                }
            }
        }
    }
}
