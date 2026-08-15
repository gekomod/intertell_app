package pl.intertell.technik.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import pl.intertell.technik.ui.theme.IntertellColors
import pl.intertell.technik.ui.theme.IntertellType
import pl.intertell.technik.update.UpdateInfo

@Composable
fun UpdateBanner(update: UpdateInfo, onDismiss: () -> Unit, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(IntertellColors.Green)
            .padding(horizontal = 16.dp, vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            "Dostępna nowa wersja aplikacji (${update.versionLabel})",
            style = IntertellType.bodySmall,
            color = IntertellColors.White,
            modifier = Modifier.weight(1f),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Text(
                "Pobierz",
                style = IntertellType.bodyBold,
                color = IntertellColors.White,
                modifier = Modifier.clickable {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(update.downloadUrl)))
                },
            )
            Text(
                "✕",
                style = IntertellType.bodyBold,
                color = IntertellColors.White,
                modifier = Modifier.clickable(onClick = onDismiss),
            )
        }
    }
}
