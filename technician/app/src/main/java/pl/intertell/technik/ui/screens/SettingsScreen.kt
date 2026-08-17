package pl.intertell.technik.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import pl.intertell.technik.TechnicianUiState
import pl.intertell.technik.TechnicianViewModel
import pl.intertell.technik.auth.isBiometricAvailable
import pl.intertell.technik.ui.components.Card
import pl.intertell.technik.ui.components.LabeledTextField
import pl.intertell.technik.ui.components.OutlineButton
import pl.intertell.technik.ui.components.SolidButton
import pl.intertell.technik.ui.components.ToggleSwitch
import pl.intertell.technik.ui.theme.IntertellColors
import pl.intertell.technik.ui.theme.IntertellType
import pl.intertell.technik.ui.theme.ThemeMode

@Composable
fun SettingsScreen(viewModel: TechnicianViewModel, state: TechnicianUiState) {
    val me by viewModel.me.collectAsState()
    val themeMode by viewModel.themeMode.collectAsState()
    val context = LocalContext.current
    val biometricAvailable = remember { isBiometricAvailable(context) }

    var currentPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    LaunchedEffect(state.passwordChangeDone) {
        if (state.passwordChangeDone) {
            currentPassword = ""; newPassword = ""; confirmPassword = ""
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp, bottom = 20.dp),
    ) {
        Text("Konto", style = IntertellType.display, color = IntertellColors.TextPrimary)

        if (me != null) {
            Card(modifier = Modifier.padding(top = 16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                    Box(
                        modifier = Modifier.size(48.dp).clip(CircleShape).background(IntertellColors.Navy),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(me!!.initials, style = IntertellType.titleBold, color = IntertellColors.White)
                    }
                    Column {
                        Text(me!!.name, style = IntertellType.titleBold, color = IntertellColors.TextPrimary)
                        Text(
                            me!!.specialization.ifBlank { me!!.code },
                            style = IntertellType.mono,
                            color = IntertellColors.Text5,
                            modifier = Modifier.padding(top = 3.dp),
                        )
                    }
                }
            }
        }

        Text(
            "Zmień hasło",
            style = IntertellType.bodyBold,
            color = IntertellColors.TextPrimary,
            modifier = Modifier.padding(top = 22.dp, bottom = 12.dp),
        )
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            LabeledTextField(
                label = "Obecne hasło",
                value = currentPassword,
                onValueChange = { currentPassword = it },
                isPassword = true,
            )
            LabeledTextField(
                label = "Nowe hasło",
                value = newPassword,
                onValueChange = { newPassword = it },
                isPassword = true,
            )
            LabeledTextField(
                label = "Powtórz nowe hasło",
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                isPassword = true,
            )
        }

        val mismatch = newPassword.isNotEmpty() && confirmPassword.isNotEmpty() && newPassword != confirmPassword
        if (mismatch) {
            Text(
                "Nowe hasła nie są identyczne.",
                style = IntertellType.bodySmall,
                color = IntertellColors.Danger,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
        if (state.passwordChangeError != null) {
            Text(
                state.passwordChangeError,
                style = IntertellType.bodySmall,
                color = IntertellColors.Danger,
                modifier = Modifier.padding(top = 10.dp),
            )
        }
        if (state.passwordChangeDone) {
            Text(
                "Hasło zostało zmienione.",
                style = IntertellType.bodySmall,
                color = IntertellColors.Green,
                modifier = Modifier.padding(top = 10.dp),
            )
        }

        SolidButton(
            text = if (state.passwordChangeInFlight) "Zapisywanie…" else "Zmień hasło",
            onClick = { viewModel.changePassword(currentPassword, newPassword) },
            enabled = !state.passwordChangeInFlight && currentPassword.isNotBlank() && newPassword.length >= 6 && newPassword == confirmPassword,
            modifier = Modifier.padding(top = 16.dp),
        )

        Text(
            "Wygląd",
            style = IntertellType.bodyBold,
            color = IntertellColors.TextPrimary,
            modifier = Modifier.padding(top = 26.dp, bottom = 12.dp),
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

        if (biometricAvailable) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 22.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(IntertellColors.Surface),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { viewModel.toggleBiometric() }
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column {
                        Text("Logowanie odciskiem palca", style = IntertellType.titleBold, color = IntertellColors.TextPrimary)
                        Text(
                            "Szybszy dostęp bez wpisywania hasła",
                            style = IntertellType.label,
                            color = IntertellColors.Text5,
                            modifier = Modifier.padding(top = 2.dp),
                        )
                    }
                    ToggleSwitch(checked = state.biometricEnabled, onCheckedChange = { viewModel.toggleBiometric() })
                }
            }
        }

        OutlineButton("Wyloguj", onClick = viewModel::logout, modifier = Modifier.padding(top = 22.dp))
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
