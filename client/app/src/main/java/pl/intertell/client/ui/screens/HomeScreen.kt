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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.SignalCellularAlt
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import pl.intertell.client.ClientUiState
import pl.intertell.client.ClientViewModel
import pl.intertell.client.ui.components.Card
import pl.intertell.client.ui.theme.IntertellColors
import pl.intertell.client.ui.theme.IntertellType

@Composable
fun HomeScreen(viewModel: ClientViewModel, state: ClientUiState) {
    val account by viewModel.account.collectAsState()
    val status by viewModel.serviceStatus.collectAsState()
    val invoices by viewModel.invoices.collectAsState()
    val unpaid = invoices.firstOrNull { !it.paid }

    if (state.homeLoading && account == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = IntertellColors.Accent)
        }
        return
    }
    val acc = account ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp, bottom = 20.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Cześć, ${acc.fullName.substringBefore(' ')}", style = IntertellType.display, color = IntertellColors.TextPrimary)
                Text(
                    "umowa ${acc.contractNumber} · ${acc.city}",
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
                Text(acc.initials, style = IntertellType.titleBold, color = IntertellColors.White)
            }
        }

        if (status != null) {
            val s = status!!
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
                Text(s.planName, style = IntertellType.headline, color = IntertellColors.White, modifier = Modifier.padding(top = 12.dp))
                Row(modifier = Modifier.padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(22.dp)) {
                    MetricColumn("PRĘDKOŚĆ", s.speedLabel.ifBlank { "—" })
                    MetricColumn("ŁĄCZE", connectionLabel(s.connectionUp))
                    s.routerUptimeDays?.let { MetricColumn("ROUTER", "$it dni") }
                }
                s.signalQuality?.let { quality ->
                    Row(modifier = Modifier.padding(top = 14.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.SignalCellularAlt, contentDescription = null, tint = signalColor(quality), modifier = Modifier.size(18.dp))
                        Text(
                            signalLabel(quality) + (s.signalRxDbm?.let { " · %.1f dBm".format(it) } ?: ""),
                            style = IntertellType.bodyBold,
                            color = signalColor(quality),
                            modifier = Modifier.padding(start = 8.dp),
                        )
                    }
                }
                Row(modifier = Modifier.padding(top = 18.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    HomeActionPill("Test prędkości", Modifier.weight(1f))
                    HomeActionPill("Zgłoś awarię", Modifier.weight(1f), onClick = viewModel::goContact)
                }
            }
        }

        Card(modifier = Modifier.padding(top = 14.dp)) {
            if (unpaid != null) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("Do zapłaty do ${unpaid.dueOn}", style = IntertellType.bodySmall, color = IntertellColors.Text55)
                        Text(unpaid.amountZl + " zł", style = IntertellType.headline, color = IntertellColors.TextPrimary, modifier = Modifier.padding(top = 4.dp))
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
                        Text("Faktura ${unpaid.number}", style = IntertellType.bodyBold, color = IntertellColors.Text55)
                    }
                }
            } else {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(IntertellColors.Green))
                    Text("Brak zaległości płatniczych", style = IntertellType.bodyBold, color = IntertellColors.TextPrimary)
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            QuickTile("Zmień pakiet", status?.planName ?: "", Icons.Default.SwapHoriz, IntertellColors.Accent, Modifier.weight(1f), viewModel::goPlan)
            QuickTile("Umowy", "dokumenty i regulaminy", Icons.Default.Description, IntertellColors.Navy, Modifier.weight(1f), viewModel::goContracts)
        }
    }
}

private fun connectionLabel(connectionUp: Boolean?): String = when (connectionUp) {
    true -> "aktywne"
    false -> "brak"
    null -> "—"
}

// Matches the server's handlers/signal.go classification ("good"/"ok"/"bad") —
// same colors used everywhere else in the app for "fine"/"degraded"/"broken".
private fun signalColor(quality: String): Color = when (quality) {
    "good" -> IntertellColors.GreenLightDot
    "bad" -> IntertellColors.Danger
    else -> IntertellColors.Amber
}

private fun signalLabel(quality: String): String = when (quality) {
    "good" -> "Sygnał bardzo dobry"
    "bad" -> "Sygnał wymaga uwagi"
    else -> "Sygnał dobry"
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
private fun QuickTile(title: String, subtitle: String, icon: ImageVector, tint: Color, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(modifier = modifier.clickable(onClick = onClick), padding = 16, radius = 18) {
        Box(
            modifier = Modifier.size(36.dp).clip(RoundedCornerShape(10.dp)).background(tint.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        }
        Text(title, style = IntertellType.bodyBold, color = IntertellColors.TextPrimary, modifier = Modifier.padding(top = 12.dp))
        Text(subtitle, style = IntertellType.label, color = IntertellColors.Text50, modifier = Modifier.padding(top = 3.dp))
    }
}
