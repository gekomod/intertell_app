package pl.intertell.technik.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import pl.intertell.technik.ui.theme.IntertellColors
import pl.intertell.technik.ui.theme.IntertellType
import pl.intertell.technik.update.DownloadProgress

/**
 * Non-dismissible progress popup shown while an update APK downloads —
 * mirrors the "new window during downloading and updating" behavior of the
 * Play Store's own update UX, instead of relying only on the system
 * notification-shade progress bar.
 */
@Composable
fun UpdateProgressDialog(progress: DownloadProgress) {
    AlertDialog(
        onDismissRequest = {},
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false),
        title = { Text(if (progress.failed) "Błąd pobierania" else "Pobieranie aktualizacji") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (progress.failed) {
                    Text(
                        "Nie udało się pobrać aktualizacji. Spróbuj ponownie później.",
                        style = IntertellType.body,
                    )
                } else {
                    val fraction = progress.fraction
                    if (fraction != null) {
                        LinearProgressIndicator(
                            progress = fraction,
                            modifier = Modifier.fillMaxWidth().height(6.dp),
                            color = IntertellColors.Green,
                        )
                        Text(
                            "${(fraction * 100).toInt()}%",
                            style = IntertellType.bodyBold,
                            modifier = Modifier.padding(top = 10.dp),
                        )
                    } else {
                        LinearProgressIndicator(
                            modifier = Modifier.fillMaxWidth().height(6.dp),
                            color = IntertellColors.Green,
                        )
                        Text(
                            "Trwa pobieranie…",
                            style = IntertellType.bodySmall,
                            modifier = Modifier.padding(top = 10.dp),
                        )
                    }
                }
            }
        },
        confirmButton = {},
    )
}
