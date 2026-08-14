package pl.intertell.client.ui.screens

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import pl.intertell.client.ClientViewModel
import pl.intertell.client.ui.components.Card
import pl.intertell.client.ui.theme.IntertellColors
import pl.intertell.client.ui.theme.IntertellType

@Composable
fun HomeScreen(viewModel: ClientViewModel) {
    val account = viewModel.account
    val status = viewModel.serviceStatus
    val invoice = viewModel.invoices.first()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp, bottom = 20.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Cześć, ${account.fullName.substringBefore(' ')}", style = IntertellType.display, color = IntertellColors.TextPrimary)
                Text(
                    "umowa ${account.contractNumber} · ${account.city}",
                    style = IntertellType.mono,
                    color = IntertellColors.Text50,
                    modifier = Modifier.padding(top = 5.dp),
                )
            }
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(IntertellColors.Navy),
                contentAlignment = Alignment.Center,
            ) {
                Text(account.initials, style = IntertellType.titleBold, color = IntertellColors.White)
            }
        }

        Card(background = IntertellColors.Navy, border = null, modifier = Modifier.padding(top = 20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(IntertellColors.GreenLightDot))
                Text(
                    "Usługa aktywna",
                    style = IntertellType.bodyBold,
                    color = IntertellColors.GreenLightDot,
                    modifier = Modifier.padding(start = 8.dp),
                )
            }
            Text(status.planName, style = IntertellType.headline, color = IntertellColors.White, modifier = Modifier.padding(top = 12.dp))
            Row(modifier = Modifier.padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(22.dp)) {
                MetricColumn("POBIERANIE", "${status.downloadMbps} Mb/s")
                MetricColumn("WYSYŁANIE", "${status.uploadMbps} Mb/s")
                MetricColumn("ONT", if (status.ontOnline) "online" else "offline")
            }
            Row(modifier = Modifier.padding(top = 18.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                HomeActionPill("Test prędkości", Modifier.weight(1f))
                HomeActionPill("Zgłoś awarię", Modifier.weight(1f), onClick = viewModel::goContact)
            }
        }

        Card(modifier = Modifier.padding(top = 14.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Do zapłaty do ${invoice.dueDate}", style = IntertellType.bodySmall, color = IntertellColors.Text55)
                    Text(invoice.amountLabel, style = IntertellType.headline, color = IntertellColors.TextPrimary, modifier = Modifier.padding(top = 4.dp))
                }
                Box(
                    modifier = Modifier
                        .height(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(IntertellColors.Accent)
                        .clickable(onClick = viewModel::goInvoices)
                        .padding(horizontal = 20.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Zapłać", style = IntertellType.bodyBold, color = IntertellColors.White)
                }
            }
            Column(modifier = Modifier.padding(top = 14.dp)) {
                Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(IntertellColors.HairlineOnLightSoft))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("BLIK · karta · przelew", style = IntertellType.bodyBold, color = IntertellColors.Text55)
                    Text("Faktura ${invoice.id}", style = IntertellType.bodyBold, color = IntertellColors.Text55)
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickTile("Zmień pakiet", "do 1000 Mb/s", IntertellColors.Accent, Modifier.weight(1f), viewModel::goPlan)
            QuickTile("Umowy", "3 dokumenty", IntertellColors.Navy, Modifier.weight(1f), viewModel::goContracts)
        }

        Card(background = IntertellColors.AmberBannerBg, border = IntertellColors.AmberBannerBorder, radius = 16, modifier = Modifier.padding(top = 14.dp)) {
            Text("Prace serwisowe w Twojej okolicy", style = IntertellType.bodyBold, color = IntertellColors.TextPrimary)
            Text(
                "18.08, 02:00–04:00 — możliwe krótkie przerwy w dostępie.",
                style = IntertellType.bodySmall,
                color = IntertellColors.Text55,
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun MetricColumn(label: String, value: String) {
    Column {
        Text(label, style = IntertellType.monoSmall, color = IntertellColors.White.copy(alpha = 0.5f))
        Text(value, style = IntertellType.titleBold, color = IntertellColors.White, modifier = Modifier.padding(top = 2.dp))
    }
}

@Composable
private fun HomeActionPill(text: String, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Box(
        modifier = modifier
            .height(42.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(IntertellColors.White.copy(alpha = 0.12f))
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(text, style = IntertellType.bodyBold, color = IntertellColors.White)
    }
}

@Composable
private fun QuickTile(title: String, subtitle: String, dotColor: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(modifier = modifier.clickable(onClick = onClick), padding = 16, radius = 18) {
        Box(modifier = Modifier.size(22.dp).clip(RoundedCornerShape(6.dp)).background(dotColor))
        Text(title, style = IntertellType.bodyBold, color = IntertellColors.TextPrimary, modifier = Modifier.padding(top = 12.dp))
        Text(subtitle, style = IntertellType.label, color = IntertellColors.Text50, modifier = Modifier.padding(top = 3.dp))
    }
}
