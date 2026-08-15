package pl.intertell.client.ui.components

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import pl.intertell.client.ui.theme.IntertellColors
import pl.intertell.client.update.UpdateInfo

@Composable
fun UpdateDialog(update: UpdateInfo, onDismiss: () -> Unit, onInstall: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Dostępna nowa wersja") },
        text = { Text("Wersja ${update.versionLabel} jest gotowa do pobrania. Po pobraniu aplikacja poprosi o instalację.") },
        confirmButton = {
            TextButton(onClick = onInstall) { Text("Pobierz i zainstaluj", color = IntertellColors.Accent) }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Później") }
        },
    )
}
