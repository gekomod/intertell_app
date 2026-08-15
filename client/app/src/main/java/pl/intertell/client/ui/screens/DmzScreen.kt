package pl.intertell.client.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.intertell.client.ClientUiState
import pl.intertell.client.ClientViewModel
import pl.intertell.client.ui.components.BackLink
import pl.intertell.client.ui.components.Card
import pl.intertell.client.ui.components.MonoLabelValue
import pl.intertell.client.ui.components.SolidButton
import pl.intertell.client.ui.components.ToggleSwitch
import pl.intertell.client.ui.theme.IntertellColors
import pl.intertell.client.ui.theme.IntertellType

@Composable
fun DmzScreen(viewModel: ClientViewModel, state: ClientUiState) {
    val dmz by viewModel.dmz.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp, bottom = 20.dp),
    ) {
        BackLink("← Konto", onClick = viewModel::goSettings)
        Text("Ustawienia DMZ", style = IntertellType.headline, color = IntertellColors.TextPrimary, modifier = Modifier.padding(top = 14.dp))
        Text(
            "Urządzenie w DMZ jest w pełni widoczne z internetu. Używaj tylko dla sprzętu, który sam zabezpieczasz.",
            style = IntertellType.body,
            color = IntertellColors.Text55,
            modifier = Modifier.padding(top = 6.dp),
        )

        if (state.dmzLoading && dmz == null) {
            Row(modifier = Modifier.fillMaxWidth().padding(top = 24.dp), horizontalArrangement = Arrangement.Center) {
                CircularProgressIndicator(color = IntertellColors.Accent)
            }
            return
        }
        val d = dmz ?: return

        Card(modifier = Modifier.padding(top = 18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text("DMZ włączona", style = IntertellType.titleBold, color = IntertellColors.TextPrimary)
                ToggleSwitch(checked = d.enabled, onCheckedChange = { viewModel.toggleDmz() })
            }
            MonoLabelValue("Adres IP hosta w sieci lokalnej", d.hostIp.ifBlank { "— nie skonfigurowano —" }, modifier = Modifier.padding(top = 16.dp))
            if (d.publicIp.isNotBlank()) {
                MonoLabelValue("Adres publiczny (WAN)", d.publicIp, modifier = Modifier.padding(top = 10.dp))
            }
        }

        Card(background = IntertellColors.AmberBannerBg, border = IntertellColors.AmberBannerBorder, radius = 16, modifier = Modifier.padding(top = 14.dp)) {
            Text(
                "Zmiana wymaga restartu routera (około 60 sekund bez internetu). Operator nie odpowiada za bezpieczeństwo urządzeń w DMZ.",
                style = IntertellType.bodySmall,
                color = IntertellColors.TextPrimary.copy(alpha = 0.7f),
            )
        }

        SolidButton("Zapisz i wróć do konta", onClick = viewModel::goSettings, height = 52, modifier = Modifier.padding(top = 16.dp))
    }
}
