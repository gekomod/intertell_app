package pl.intertell.technik.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import pl.intertell.technik.TechnicianViewModel
import pl.intertell.technik.ui.components.BackLink
import pl.intertell.technik.ui.components.OutlineButton
import pl.intertell.technik.ui.components.SolidButton
import pl.intertell.technik.ui.theme.IntertellColors
import pl.intertell.technik.ui.theme.IntertellType

@Composable
fun CustomerScreen(viewModel: TechnicianViewModel) {
    val context = LocalContext.current
    val customer by viewModel.selectedCustomer.collectAsState()
    if (customer == null) return
    val c = customer!!

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp, bottom = 20.dp),
    ) {
        BackLink("← Klienci", onClick = viewModel::goSearch)
        Text(c.address.ifBlank { c.name }, style = IntertellType.headline, color = IntertellColors.TextPrimary, modifier = Modifier.padding(top = 14.dp))
        Text(
            "${c.name} · ${c.customerNo}",
            style = IntertellType.body,
            color = IntertellColors.Text6,
            modifier = Modifier.padding(top = 5.dp),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(IntertellColors.Navy)
                .padding(horizontal = 20.dp, vertical = 18.dp),
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Stan konta", style = IntertellType.bodySmall, color = IntertellColors.White.copy(alpha = 0.55f))
                Text(c.statusLabel.uppercase(), style = IntertellType.monoSmall, color = IntertellColors.GreenCheck)
            }
            if (c.lms != null) {
                Row(modifier = Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    NavyMetric("SALDO (LMS)", c.lms.balanceZl + " zł")
                    NavyMetric("ŁĄCZE", if (c.lms.connectionUp) "aktywne" else "brak")
                }
            } else {
                Text(
                    "Klient nie jest połączony z LMS — brak danych o saldzie/łączu na żywo.",
                    style = IntertellType.bodySmall,
                    color = IntertellColors.White.copy(alpha = 0.5f),
                    modifier = Modifier.padding(top = 10.dp),
                )
            }
        }

        if (c.devices.isNotEmpty()) {
            SolidButton("Otwórz panel ONT / routera", onClick = viewModel::goRouter, modifier = Modifier.padding(top = 14.dp))
        }
        Row(modifier = Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlineButton(
                "Nawiguj",
                onClick = {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${Uri.encode(c.address)}")))
                },
                modifier = Modifier.weight(1f),
                height = 48,
            )
        }

        Text(
            "Urządzenia (${c.devices.size})",
            style = IntertellType.bodyBold,
            color = IntertellColors.TextPrimary,
            modifier = Modifier.padding(top = 22.dp, bottom = 10.dp),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(IntertellColors.Surface),
        ) {
            c.devices.forEachIndexed { index, d ->
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(d.kindLabel, style = IntertellType.titleBold, color = IntertellColors.TextPrimary)
                        Text(d.status, style = IntertellType.monoSmall, color = IntertellColors.Text5)
                    }
                    Text("${d.model} · SN ${d.serial}", style = IntertellType.mono, color = IntertellColors.Text5, modifier = Modifier.padding(top = 4.dp))
                    if (d.location.isNotBlank()) {
                        Text(d.location, style = IntertellType.body, color = IntertellColors.Text55, modifier = Modifier.padding(top = 2.dp))
                    }
                }
                if (index != c.devices.lastIndex) {
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(IntertellColors.HairlineOnLightFaint))
                }
            }
        }

        Text(
            "Historia napraw (${c.history.size})",
            style = IntertellType.bodyBold,
            color = IntertellColors.TextPrimary,
            modifier = Modifier.padding(top = 22.dp, bottom = 10.dp),
        )
        if (c.history.isEmpty()) {
            Text(
                "Brak wcześniejszych zgłoszeń serwisowych.",
                style = IntertellType.body,
                color = IntertellColors.Text55,
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(IntertellColors.Surface),
            ) {
                c.history.forEachIndexed { index, h ->
                    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text(h.topic.ifBlank { "Zgłoszenie" }, style = IntertellType.titleBold, color = IntertellColors.TextPrimary)
                            Text(h.statusLabel, style = IntertellType.monoSmall, color = IntertellColors.Text5)
                        }
                        Text(
                            h.createdAt + (if (h.technicianName.isNotBlank()) " · ${h.technicianName}" else " · nieprzypisane"),
                            style = IntertellType.mono,
                            color = IntertellColors.Text5,
                            modifier = Modifier.padding(top = 4.dp),
                        )
                        if (h.message.isNotBlank()) {
                            Text(h.message, style = IntertellType.body, color = IntertellColors.Text55, modifier = Modifier.padding(top = 6.dp))
                        }
                    }
                    if (index != c.history.lastIndex) {
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(IntertellColors.HairlineOnLightFaint))
                    }
                }
            }
        }

        Text(
            "Dane klienta z bazy intratell; saldo i status łącza — z LMS gdy klient jest połączony.",
            style = IntertellType.monoFootnote,
            color = IntertellColors.Text45,
            modifier = Modifier.padding(top = 14.dp),
        )
    }
}

@Composable
private fun NavyMetric(label: String, value: String) {
    Column {
        Text(label, style = IntertellType.monoSmall, color = IntertellColors.White.copy(alpha = 0.5f))
        Text(value, style = IntertellType.monoBold, color = IntertellColors.White, modifier = Modifier.padding(top = 2.dp))
    }
}
