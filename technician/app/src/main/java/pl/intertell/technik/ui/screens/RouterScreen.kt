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
import pl.intertell.technik.data.CustomerState
import pl.intertell.technik.ui.components.BackLink
import pl.intertell.technik.ui.components.Card
import pl.intertell.technik.ui.components.MonoLabelValue
import pl.intertell.technik.ui.components.OutlineButton
import pl.intertell.technik.ui.components.SolidButton
import pl.intertell.technik.ui.components.ToggleSwitch
import pl.intertell.technik.ui.theme.IntertellColors
import pl.intertell.technik.ui.theme.IntertellType

@Composable
fun RouterScreen(viewModel: TechnicianViewModel, state: TechnicianUiState) {
    val customer = viewModel.currentCustomer()
    val netClients = viewModel.networkClients
    val (stateFg, stateBg) = when (customer.state) {
        CustomerState.AWARIA -> IntertellColors.Danger to IntertellColors.DangerChipBg
        CustomerState.ZAWIESZONA -> IntertellColors.Amber to IntertellColors.AmberChipBg
        CustomerState.OK -> IntertellColors.Green to IntertellColors.GreenChipBg
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp, bottom = 20.dp),
    ) {
        BackLink("← ${customer.address}", onClick = viewModel::goCust)
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top,
        ) {
            Column {
                Text("ONT Huawei", style = IntertellType.headline, color = IntertellColors.TextPrimary)
                Text(
                    "${customer.ont} · SN ${customer.sn} · FW 2.14.7",
                    style = IntertellType.mono,
                    color = IntertellColors.Text5,
                    modifier = Modifier.padding(top = 5.dp),
                )
                Text(
                    "${customer.name} · ${customer.contract}",
                    style = IntertellType.body,
                    color = IntertellColors.Text6,
                    modifier = Modifier.padding(top = 4.dp),
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(9.dp))
                    .background(stateBg)
                    .padding(horizontal = 11.dp, vertical = 7.dp),
            ) {
                Text(customer.state.name, style = IntertellType.monoSmall, color = stateFg)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(IntertellColors.Navy)
                .padding(horizontal = 20.dp, vertical = 18.dp),
        ) {
            Text("Łącze PON", style = IntertellType.bodySmall, color = IntertellColors.White.copy(alpha = 0.55f))
            Row(modifier = Modifier.padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                NavyMetric("RX", customer.rx)
                NavyMetric("TX", "2,1 dBm")
                NavyMetric("UPTIME", "88 d")
                NavyMetric("VLAN", "35")
            }
        }

        SectionTitle("Przepustowość i QoS")
        Card {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Profil z LMS", style = IntertellType.bodyBold, color = IntertellColors.Text6)
                Text(customer.plan, style = IntertellType.bodyBold, color = IntertellColors.TextPrimary)
            }
            Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MonoLabelValue("Down / limit", "600 Mb/s", modifier = Modifier.weight(1f))
                MonoLabelValue("Up / limit", "200 Mb/s", modifier = Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MonoLabelValue("Kolejkowanie", "SQM / fq_codel", valueStyle = IntertellType.bodyBold, modifier = Modifier.weight(1f))
                MonoLabelValue("MTU / MSS", "1500 / 1452", modifier = Modifier.weight(1f))
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .clickable(onClick = viewModel::toggleQos),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("Priorytet ruchu głosowego", style = IntertellType.bodyBold, color = IntertellColors.TextPrimary)
                    Text("DSCP EF, kolejka 1", style = IntertellType.label, color = IntertellColors.Text5, modifier = Modifier.padding(top = 2.dp))
                }
                ToggleSwitch(checked = state.qosEnabled, onCheckedChange = { viewModel.toggleQos() }, activeColor = IntertellColors.Accent)
            }
            SolidButton(
                if (state.speedTestRun) "Pomiar: 612 / 208 Mb/s · ping 4 ms" else "Uruchom pomiar przepustowości",
                onClick = viewModel::runSpeedTest,
                modifier = Modifier.padding(top = 14.dp),
                height = 46,
            )
        }

        SectionTitle("Sieć Wi-Fi")
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(IntertellColors.White),
        ) {
            WifiBand(
                title = "2,4 GHz", ssid = "Intertell-88214", channel = "auto · 6",
                width = "20 MHz", extraLabel = "Moc nadawania", extraValue = "100 %",
                enabled = state.wifi24Enabled, onToggle = viewModel::toggleWifi24,
            )
            Divider()
            WifiBand(
                title = "5 GHz", ssid = "Intertell-88214-5G", channel = "auto · 44",
                width = "80 MHz", extraLabel = "Tryb / szyfr.", extraValue = "AX · WPA3",
                enabled = state.wifi5Enabled, onToggle = viewModel::toggleWifi5,
            )
            Divider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = viewModel::toggleMesh)
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("Band steering / mesh", style = IntertellType.titleBold, color = IntertellColors.TextPrimary)
                    Text("Wspólny SSID, 1 punkt dodatkowy", style = IntertellType.label, color = IntertellColors.Text5, modifier = Modifier.padding(top = 2.dp))
                }
                ToggleSwitch(checked = state.meshEnabled, onCheckedChange = { viewModel.toggleMesh() }, activeColor = IntertellColors.Accent)
            }
            Divider()
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text("Sieć dla gości", style = IntertellType.titleBold, color = IntertellColors.TextPrimary)
                Text("WYŁĄCZONA", style = IntertellType.monoSmall, color = IntertellColors.Text5)
            }
        }

        SectionTitle("LAN, DHCP i NAT")
        Card {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MonoLabelValue("IP routera", "192.168.1.1/24", valueStyle = IntertellType.bodyBold, modifier = Modifier.weight(1f))
                MonoLabelValue("Pula DHCP", ".20 – .200", valueStyle = IntertellType.bodyBold, modifier = Modifier.weight(1f))
            }
            Row(modifier = Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                MonoLabelValue("DNS", "89.64.1.1 / .2", valueStyle = IntertellType.bodyBold, modifier = Modifier.weight(1f))
                MonoLabelValue("WAN / IP", "DHCP · 89.64.12.7", valueStyle = IntertellType.bodyBold, modifier = Modifier.weight(1f))
            }
            LanRow("Przekierowania portów", "4 reguły ›", top = 14.dp)
            LanRow("DMZ", "wyłączona ›")
            LanRow("UPnP, IPv6, VPN passthrough", "wł. ›")
            LanRow("Rezerwacje MAC / IP", "6 ›")
        }

        Text(
            "Klienci sieci (${netClients.size})",
            style = IntertellType.bodyBold,
            color = IntertellColors.TextPrimary,
            modifier = Modifier.padding(top = 22.dp, bottom = 10.dp),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(IntertellColors.White),
        ) {
            netClients.forEachIndexed { index, client ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text(client.name, style = IntertellType.titleBold, color = IntertellColors.TextPrimary)
                        Text("${client.ip} · ${client.link}", style = IntertellType.monoSmall, color = IntertellColors.Text5, modifier = Modifier.padding(top = 2.dp))
                    }
                    Text(client.rate, style = IntertellType.monoBold, color = IntertellColors.Text55)
                }
                if (index != netClients.lastIndex) Divider()
            }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlineButton("Restart routera", onClick = {}, modifier = Modifier.weight(1f))
            OutlineButton("Aktualizuj firmware", onClick = {}, modifier = Modifier.weight(1f))
        }
        SolidButton("Wyślij konfigurację do LMS", onClick = {}, background = IntertellColors.Navy, modifier = Modifier.padding(top = 10.dp), height = 50)
        Text(
            "Zmiany wykonane przez technika są logowane na umowie klienta wraz z identyfikatorem ${viewModel.technicianId}.",
            style = IntertellType.monoFootnote,
            color = IntertellColors.Text45,
            modifier = Modifier.padding(top = 14.dp),
        )
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = IntertellType.bodyBold, color = IntertellColors.TextPrimary, modifier = Modifier.padding(top = 22.dp, bottom = 10.dp))
}

@Composable
private fun NavyMetric(label: String, value: String) {
    Column {
        Text(label, style = IntertellType.monoSmall, color = IntertellColors.White.copy(alpha = 0.5f))
        Text(value, style = IntertellType.monoBold, color = IntertellColors.White, modifier = Modifier.padding(top = 2.dp))
    }
}

@Composable
private fun WifiBand(
    title: String,
    ssid: String,
    channel: String,
    width: String,
    extraLabel: String,
    extraValue: String,
    enabled: Boolean,
    onToggle: () -> Unit,
) {
    Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 16.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text(title, style = IntertellType.titleBold, color = IntertellColors.TextPrimary)
            ToggleSwitch(checked = enabled, onCheckedChange = { onToggle() }, activeColor = IntertellColors.Accent)
        }
        Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MonoLabelValue("SSID", ssid, valueStyle = IntertellType.bodyBold, modifier = Modifier.weight(1f))
            MonoLabelValue("Kanał", channel, modifier = Modifier.weight(1f))
        }
        Row(modifier = Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            MonoLabelValue("Szerokość", width, modifier = Modifier.weight(1f))
            MonoLabelValue(extraLabel, extraValue, valueStyle = IntertellType.bodyBold, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun LanRow(label: String, value: String, top: androidx.compose.ui.unit.Dp = 10.dp) {
    Row(modifier = Modifier.fillMaxWidth().padding(top = top), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = IntertellType.bodyBold, color = IntertellColors.TextPrimary)
        Text(value, style = IntertellType.bodyBold, color = IntertellColors.Text5)
    }
}

@Composable
private fun Divider() {
    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(IntertellColors.HairlineOnLightFaint))
}
