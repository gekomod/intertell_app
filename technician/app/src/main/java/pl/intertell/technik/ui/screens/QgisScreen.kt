package pl.intertell.technik.ui.screens

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import pl.intertell.technik.TechnicianViewModel
import pl.intertell.technik.ui.components.SolidButton
import pl.intertell.technik.ui.theme.IntertellColors
import pl.intertell.technik.ui.theme.IntertellType

private const val QFIELD_PACKAGE = "ch.opengis.qfield"

@Composable
fun QgisScreen(@Suppress("UNUSED_PARAMETER") viewModel: TechnicianViewModel) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
    ) {
        Text("QGIS", style = IntertellType.headline, color = IntertellColors.TextPrimary)
        Text(
            "Mapa infrastruktury sieciowej — przebiegi światłowodów, szafki i punkty dystrybucyjne.",
            style = IntertellType.body,
            color = IntertellColors.Text6,
            modifier = Modifier.padding(top = 6.dp),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(IntertellColors.Navy)
                .padding(20.dp),
        ) {
            Text(
                "Projekt sieci otwiera się w aplikacji QField.",
                style = IntertellType.bodyBold,
                color = IntertellColors.White,
            )
            Text(
                "Zaloguj się tam swoim kontem QFieldCloud, aby zobaczyć aktualną infrastrukturę.",
                style = IntertellType.bodySmall,
                color = IntertellColors.White.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 6.dp),
            )
            SolidButton(
                "Otwórz QField",
                onClick = { openQField(context) },
                modifier = Modifier.padding(top = 16.dp),
            )
        }
    }
}

private fun openQField(context: Context) {
    val launchIntent = context.packageManager.getLaunchIntentForPackage(QFIELD_PACKAGE)
    if (launchIntent != null) {
        context.startActivity(launchIntent)
        return
    }
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$QFIELD_PACKAGE")))
    } catch (e: ActivityNotFoundException) {
        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$QFIELD_PACKAGE")))
    }
}
