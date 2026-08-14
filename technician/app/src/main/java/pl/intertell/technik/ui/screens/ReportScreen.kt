package pl.intertell.technik.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.ui.unit.dp
import pl.intertell.technik.TechnicianUiState
import pl.intertell.technik.TechnicianViewModel
import pl.intertell.technik.ui.components.BackLink
import pl.intertell.technik.ui.components.Card
import pl.intertell.technik.ui.components.MonoLabelValue
import pl.intertell.technik.ui.components.SolidButton
import pl.intertell.technik.ui.theme.IntertellColors
import pl.intertell.technik.ui.theme.IntertellType

@Composable
fun ReportScreen(viewModel: TechnicianViewModel, state: TechnicianUiState) {
    val job = viewModel.currentJob()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp, bottom = 20.dp),
    ) {
        BackLink("← Zlecenie ${job.id}", onClick = viewModel::goJob)
        Text("Raport z instalacji", style = IntertellType.headline, color = IntertellColors.TextPrimary, modifier = Modifier.padding(top = 14.dp))
        Text(
            "Wypełnij przed zamknięciem zlecenia. Dane trafiają do LMS.",
            style = IntertellType.bodySmall,
            color = IntertellColors.Text55,
            modifier = Modifier.padding(top = 5.dp),
        )

        Card(modifier = Modifier.padding(top = 18.dp)) {
            Text("POMIARY", style = IntertellType.label, color = IntertellColors.Text45)
            Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MonoLabelValue("Moc optyczna RX", "−19,4 dBm", modifier = Modifier.weight(1f))
                MonoLabelValue("Pomiar prędkości", "612 Mb/s", modifier = Modifier.weight(1f))
            }
        }

        Card(modifier = Modifier.padding(top = 14.dp)) {
            Text("WYDANY SPRZĘT", style = IntertellType.label, color = IntertellColors.Text45)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(IntertellColors.ScreenBackground)
                    .padding(horizontal = 14.dp, vertical = 13.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("ONT Nokia G-010G", style = IntertellType.bodyBold, color = IntertellColors.TextPrimary)
                    Text("SN 4A21-88F0-1192", style = IntertellType.mono, color = IntertellColors.Text5, modifier = Modifier.padding(top = 3.dp))
                }
                Text("Zeskanowano", style = IntertellType.chip, color = IntertellColors.Green)
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp)
                    .height(46.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(onClick = viewModel::toggleDevice),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    if (state.deviceAdded) "Router Wi-Fi 6 · SN 77B2-0043 — dodany" else "+ Zeskanuj kolejne urządzenie",
                    style = IntertellType.bodyBold,
                    color = IntertellColors.Text55,
                )
            }
        }

        Card(modifier = Modifier.padding(top = 14.dp)) {
            Text("ZDJĘCIA I PODPIS", style = IntertellType.label, color = IntertellColors.Text45)
            Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PhotoPlaceholder("gniazdo", Modifier.weight(1f))
                PhotoPlaceholder("spaw", Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(84.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(IntertellColors.White),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("+", style = IntertellType.headline, color = IntertellColors.Text35)
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .height(88.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(IntertellColors.ScreenBackground),
                contentAlignment = Alignment.Center,
            ) {
                Text("[ podpis klienta ]", style = IntertellType.mono, color = IntertellColors.Text45)
            }
            SolidButton("Zakończ i wyślij do LMS", onClick = viewModel::finishJob, modifier = Modifier.padding(top = 16.dp))
        }
    }
}

@Composable
private fun PhotoPlaceholder(label: String, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .height(84.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(IntertellColors.HairlineOnLightFaint),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, style = IntertellType.monoSmall, color = IntertellColors.Text45)
    }
}
