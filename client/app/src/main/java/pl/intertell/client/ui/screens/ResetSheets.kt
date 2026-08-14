package pl.intertell.client.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import pl.intertell.client.ClientViewModel
import pl.intertell.client.ui.components.FullScreenOutcome
import pl.intertell.client.ui.components.SolidButton
import pl.intertell.client.ui.theme.IntertellColors
import pl.intertell.client.ui.theme.IntertellType

@Composable
fun ResetConfirmSheet(viewModel: ClientViewModel) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp))
            .background(IntertellColors.White)
            .padding(start = 22.dp, top = 24.dp, end = 22.dp, bottom = 44.dp),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(width = 44.dp, height = 4.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(IntertellColors.ToggleTrackOff.copy(alpha = 0.15f)),
        )
        Text(
            "RESTART DO USTAWIEŃ OPERATORA",
            style = IntertellType.label,
            color = IntertellColors.Danger,
            modifier = Modifier.padding(top = 20.dp),
        )
        Text(
            "Przywrócić konfigurację Intertell?",
            style = IntertellType.headline,
            color = IntertellColors.TextPrimary,
            modifier = Modifier.padding(top = 8.dp),
        )
        Text(
            "Router wróci do ustawień fabrycznych operatora: nazwa i hasło Wi-Fi z naklejki, DMZ i przekierowania portów zostaną usunięte. Internet wróci w ciągu 2–3 minut.",
            style = IntertellType.body,
            color = IntertellColors.Text55,
            modifier = Modifier.padding(top = 10.dp),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(IntertellColors.ScreenBackground)
                .padding(16.dp),
        ) {
            Text(
                "Urządzenie: router Wi-Fi 6 · SN 77B2-0043",
                style = IntertellType.body,
                color = IntertellColors.Text55,
            )
            Text(
                "Operacja jest odnotowana w LMS na Twojej umowie.",
                style = IntertellType.body,
                color = IntertellColors.Text55,
            )
        }
        SolidButton(
            "Tak, zrestartuj router",
            onClick = viewModel::confirmReset,
            background = IntertellColors.Danger,
            modifier = Modifier.padding(top = 18.dp),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
                .height(48.dp)
                .clickable(onClick = viewModel::closeReset),
            contentAlignment = Alignment.Center,
        ) {
            Text("Anuluj", style = IntertellType.bodyBold, color = IntertellColors.Text50)
        }
    }
}

@Composable
fun ResetInProgressOverlay(viewModel: ClientViewModel) {
    FullScreenOutcome(
        title = "Router się restartuje",
        body = "Połącz się z siecią Wi-Fi o nazwie z naklejki na urządzeniu. Za 2–3 minuty status wróci do „Sprawne”.",
        ctaText = "Wróć do konta",
        onCta = viewModel::closeResetDone,
        icon = "↻",
        iconBackground = IntertellColors.White.copy(alpha = 0.14f),
    )
}
