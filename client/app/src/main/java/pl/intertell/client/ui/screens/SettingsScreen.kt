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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import pl.intertell.client.ClientUiState
import pl.intertell.client.ClientViewModel
import pl.intertell.client.auth.isBiometricAvailable
import pl.intertell.client.ui.components.Card
import pl.intertell.client.ui.components.OutlineButton
import pl.intertell.client.ui.components.ToggleSwitch
import pl.intertell.client.ui.theme.IntertellColors
import pl.intertell.client.ui.theme.IntertellType
import pl.intertell.client.ui.theme.ThemeMode

@Composable
fun SettingsScreen(viewModel: ClientViewModel, state: ClientUiState) {
    val account by viewModel.account.collectAsState()
    val status by viewModel.serviceStatus.collectAsState()
    val dmz by viewModel.dmz.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val context = LocalContext.current
    val biometricAvailable = remember { isBiometricAvailable(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp, bottom = 20.dp),
    ) {
        Text("Konto", style = IntertellType.display, color = IntertellColors.TextPrimary)

        if (account != null) {
            Card(modifier = Modifier.padding(top = 16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Box(
                        modifier = Modifier.size(48.dp).clip(CircleShape).background(IntertellColors.Navy),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(account!!.initials, style = IntertellType.titleBold, color = IntertellColors.White)
                    }
                    Column {
                        Text(account!!.fullName, style = IntertellType.titleBold, color = IntertellColors.TextPrimary)
                        Text(account!!.email, style = IntertellType.mono, color = IntertellColors.Text50, modifier = Modifier.padding(top = 3.dp))
                    }
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(IntertellColors.Surface),
        ) {
            ToggleRow("e-Faktura", "Faktury tylko elektronicznie", state.eFakturaOnly, viewModel::toggleEFaktura, divider = true)
            ToggleRow("Powiadomienia push", null, state.pushNotifications, viewModel::togglePush, divider = true)
            ToggleRow("SMS o awariach", null, state.smsAlerts, viewModel::toggleSms, divider = biometricAvailable)
            if (biometricAvailable) {
                ToggleRow(
                    "Logowanie odciskiem palca",
                    "Szybszy dostęp bez wpisywania hasła",
                    state.biometricEnabled,
                    viewModel::toggleBiometric,
                    divider = false,
                )
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(IntertellColors.Surface),
        ) {
            LinkRow("Umowy i regulaminy", onClick = viewModel::goContracts, divider = true)
            LinkRow("Dane do płatności", onClick = {}, divider = true)
            LinkRow("Zmień hasło w LMS", onClick = {}, divider = true)
            LinkRow("Pomoc i kontakt", onClick = viewModel::goContact, divider = false)
        }

        Text(
            "Wygląd",
            style = IntertellType.bodyBold,
            color = IntertellColors.TextPrimary,
            modifier = Modifier.padding(top = 22.dp, bottom = 12.dp),
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(IntertellColors.Surface)
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            ThemeModeOption("Jasny", ThemeMode.LIGHT, themeMode, viewModel::setThemeMode, Modifier.weight(1f))
            ThemeModeOption("Ciemny", ThemeMode.DARK, themeMode, viewModel::setThemeMode, Modifier.weight(1f))
            ThemeModeOption("Automatyczny", ThemeMode.SYSTEM, themeMode, viewModel::setThemeMode, Modifier.weight(1f))
        }

        Text(
            "Urządzenie i sieć",
            style = IntertellType.bodyBold,
            color = IntertellColors.TextPrimary,
            modifier = Modifier.padding(top = 22.dp, bottom = 12.dp),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(IntertellColors.Surface),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("Status działania", style = IntertellType.titleBold, color = IntertellColors.TextPrimary)
                    Text(
                        connectionStatusText(status?.connectionUp, status?.routerUptimeDays),
                        style = IntertellType.mono,
                        color = IntertellColors.Text50,
                        modifier = Modifier.padding(top = 3.dp),
                    )
                }
                if (status?.connectionUp == true) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(IntertellColors.Green))
                        Text("Sprawne", style = IntertellType.chip, color = IntertellColors.Green, modifier = Modifier.padding(start = 7.dp))
                    }
                }
            }
            Divider()
            LinkRow2(
                "Ustawienia DMZ", "Host wystawiony do internetu",
                trailing = if (dmz?.enabled == true) "Włączona" else "Wyłączona",
                onClick = viewModel::goDmz, divider = true,
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(IntertellColors.Navy.copy(alpha = 0.03f))
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("VPN", style = IntertellType.titleBold, color = IntertellColors.Text38)
                    Text(
                        "Konfiguracja tunelu i profile urządzeń",
                        style = IntertellType.label,
                        color = IntertellColors.Text38,
                        modifier = Modifier.padding(top = 3.dp),
                    )
                    Box(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(IntertellColors.ToggleTrackOffFaint)
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                    ) {
                        Text("TRWAJĄ PRACE NAD TĄ FUNKCJONALNOŚCIĄ", style = IntertellType.monoSmall, color = IntertellColors.Text50)
                    }
                }
                ToggleSwitch(checked = false, onCheckedChange = {}, enabled = false, inactiveColor = IntertellColors.ToggleTrackOffFaint)
            }
            Divider()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = viewModel::openReset)
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column {
                    Text("Restart do ustawień fabrycznych operatora", style = IntertellType.titleBold, color = IntertellColors.Danger)
                    Text("Przywraca konfigurację Intertell", style = IntertellType.label, color = IntertellColors.Text50, modifier = Modifier.padding(top = 2.dp))
                }
                Text("›", style = IntertellType.titleBold, color = IntertellColors.Text35)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(IntertellColors.Navy)
                .clickable(onClick = viewModel::goContact)
                .padding(horizontal = 20.dp, vertical = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text("Kontakt z Operatorem", style = IntertellType.titleBold, color = IntertellColors.White)
                Text(
                    "Wsparcie techniczne · pon.–sob. 8:00–20:00",
                    style = IntertellType.bodySmall,
                    color = IntertellColors.White.copy(alpha = 0.6f),
                    modifier = Modifier.padding(top = 3.dp),
                )
            }
            Text("›", style = IntertellType.titleBold, color = IntertellColors.White.copy(alpha = 0.5f))
        }

        OutlineButton("Wyloguj", onClick = viewModel::logout, modifier = Modifier.padding(top = 16.dp))
    }
}

private fun connectionStatusText(connectionUp: Boolean?, uptimeDays: Int?): String {
    val ont = when (connectionUp) {
        true -> "ONT online"
        false -> "ONT offline"
        null -> "status ONT nieznany"
    }
    return if (uptimeDays != null) "$ont · router $uptimeDays dni bez restartu" else ont
}

@Composable
private fun ToggleRow(title: String, subtitle: String?, checked: Boolean, onToggle: () -> Unit, divider: Boolean) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(title, style = IntertellType.titleBold, color = IntertellColors.TextPrimary)
                if (subtitle != null) {
                    Text(subtitle, style = IntertellType.label, color = IntertellColors.Text50, modifier = Modifier.padding(top = 2.dp))
                }
            }
            ToggleSwitch(checked = checked, onCheckedChange = { onToggle() })
        }
        if (divider) Divider()
    }
}

@Composable
private fun LinkRow(title: String, onClick: () -> Unit, divider: Boolean) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(title, style = IntertellType.titleBold, color = IntertellColors.TextPrimary)
            Text("›", style = IntertellType.titleBold, color = IntertellColors.Text35)
        }
        if (divider) Divider()
    }
}

@Composable
private fun LinkRow2(title: String, subtitle: String, trailing: String, onClick: () -> Unit, divider: Boolean) {
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = 18.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column {
                Text(title, style = IntertellType.titleBold, color = IntertellColors.TextPrimary)
                Text(subtitle, style = IntertellType.label, color = IntertellColors.Text50, modifier = Modifier.padding(top = 2.dp))
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(trailing, style = IntertellType.chip, color = IntertellColors.Text45, modifier = Modifier.padding(end = 8.dp))
                Text("›", style = IntertellType.titleBold, color = IntertellColors.Text35)
            }
        }
        if (divider) Divider()
    }
}

@Composable
private fun ThemeModeOption(label: String, mode: ThemeMode, current: ThemeMode, onSelect: (ThemeMode) -> Unit, modifier: Modifier = Modifier) {
    val selected = mode == current
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(11.dp))
            .background(if (selected) IntertellColors.Accent else Color.Transparent)
            .clickable { onSelect(mode) }
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            style = IntertellType.bodyBold,
            color = if (selected) IntertellColors.White else IntertellColors.TextPrimary,
        )
    }
}

@Composable
private fun Divider() {
    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(IntertellColors.HairlineOnLightSoft))
}
