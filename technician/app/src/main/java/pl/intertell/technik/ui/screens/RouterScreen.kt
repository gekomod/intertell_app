package pl.intertell.technik.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import pl.intertell.technik.TechnicianViewModel
import pl.intertell.technik.ui.components.BackLink
import pl.intertell.technik.ui.components.Card
import pl.intertell.technik.ui.theme.IntertellColors
import pl.intertell.technik.ui.theme.IntertellType

@Composable
fun RouterScreen(viewModel: TechnicianViewModel) {
    val customer by viewModel.selectedCustomer.collectAsState()
    val state by viewModel.uiState.collectAsState()
    if (customer == null) return
    val c = customer!!
    val router = c.router
    val routerDevice = c.devices.firstOrNull { it.kind == "router" } ?: c.devices.firstOrNull()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp, bottom = 20.dp),
    ) {
        BackLink("← ${c.address.ifBlank { c.name }}", onClick = viewModel::goCust)
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column {
                Text("Router / ONT", style = IntertellType.headline, color = IntertellColors.TextPrimary)
                if (routerDevice != null) {
                    Text(
                        "${routerDevice.model} · SN ${routerDevice.serial}",
                        style = IntertellType.mono,
                        color = IntertellColors.Text5,
                        modifier = Modifier.padding(top = 5.dp),
                    )
                }
                Text("${c.name} · ${c.customerNo}", style = IntertellType.body, color = IntertellColors.Text6, modifier = Modifier.padding(top = 4.dp))
            }
        }

        if (state.customerDetailLoading && router == null) {
            Row(modifier = Modifier.fillMaxWidth().padding(top = 24.dp), horizontalArrangement = Arrangement.Center) {
                CircularProgressIndicator(color = IntertellColors.Accent)
            }
            return
        }

        if (router == null) {
            Text(
                "Brak zapisanych ustawień routera dla tego klienta.",
                style = IntertellType.body,
                color = IntertellColors.Text5,
                modifier = Modifier.padding(top = 24.dp),
            )
            return
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(IntertellColors.Navy)
                .padding(horizontal = 20.dp, vertical = 18.dp),
        ) {
            Text("Łącze", style = IntertellType.bodySmall, color = IntertellColors.White.copy(alpha = 0.55f))
            Row(modifier = Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                NavyMetric("FIRMWARE", router.firmware)
                NavyMetric("UPTIME", "${router.uptimeDays} d")
                NavyMetric("URZĄDZENIA", "${router.deviceCount}")
            }
            Text(
                "WAN: ${router.wanIp}",
                style = IntertellType.mono,
                color = IntertellColors.White.copy(alpha = 0.7f),
                modifier = Modifier.padding(top = 12.dp),
            )
        }

        val optical = routerDevice?.optical
        if (optical != null) {
            Text("Sygnał optyczny (ONT)", style = IntertellType.bodyBold, color = IntertellColors.TextPrimary, modifier = Modifier.padding(top = 22.dp, bottom = 10.dp))
            Card {
                StatusRow("ONT", if (optical.online) "online" else "offline", optical.online)
                Row(modifier = Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                    OpticalMetric("RX", optical.rxDbm, warn = optical.rxDbm < -27.0)
                    OpticalMetric("TX", optical.txDbm, warn = false)
                    Column {
                        Text("TEMP.", style = IntertellType.monoSmall, color = IntertellColors.Text45)
                        Text("${optical.tempC} °C", style = IntertellType.monoBold, color = IntertellColors.TextPrimary, modifier = Modifier.padding(top = 2.dp))
                    }
                }
            }
        }

        Text("Sieć i zabezpieczenia", style = IntertellType.bodyBold, color = IntertellColors.TextPrimary, modifier = Modifier.padding(top = 22.dp, bottom = 10.dp))
        Card {
            StatusRow("DMZ", if (router.dmzOn) "włączona · ${router.dmzHost}" else "wyłączona", router.dmzOn)
            StatusRow("Proxy", if (router.proxyOn) "${router.proxyHost}:${router.proxyPort}" else "wyłączone", router.proxyOn, top = 12.dp)
            StatusRow("VPN klient", if (router.vpnClientOn) "włączony" else "wyłączony", router.vpnClientOn, top = 12.dp)
            StatusRow("VPN serwer", if (router.vpnServerOn) "włączony" else "wyłączony", router.vpnServerOn, top = 12.dp)
        }

        Text(
            "Zmiana ustawień routera z poziomu aplikacji nie jest jeszcze dostępna — dane są tylko do odczytu z bazy intratell.",
            style = IntertellType.monoFootnote,
            color = IntertellColors.Text45,
            modifier = Modifier.padding(top = 16.dp),
        )
    }
}

@Composable
private fun OpticalMetric(label: String, dbm: Double, warn: Boolean) {
    Column {
        Text(label, style = IntertellType.monoSmall, color = IntertellColors.Text45)
        Text(
            "$dbm dBm",
            style = IntertellType.monoBold,
            color = if (warn) IntertellColors.Danger else IntertellColors.TextPrimary,
            modifier = Modifier.padding(top = 2.dp),
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

@Composable
private fun StatusRow(label: String, value: String, on: Boolean, top: androidx.compose.ui.unit.Dp = 0.dp) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = top),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(label, style = IntertellType.titleBold, color = IntertellColors.TextPrimary)
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(5.dp))
                .background(if (on) IntertellColors.GreenChipBg else IntertellColors.ToggleTrackOffFaint)
                .padding(horizontal = 7.dp, vertical = 3.dp),
        ) {
            Text(value, style = IntertellType.monoSmall, color = if (on) IntertellColors.Green else IntertellColors.Text5)
        }
    }
}
