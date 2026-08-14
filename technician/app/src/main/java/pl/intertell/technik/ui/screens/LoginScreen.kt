package pl.intertell.technik.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import pl.intertell.technik.R
import pl.intertell.technik.TechnicianUiState
import pl.intertell.technik.TechnicianViewModel
import pl.intertell.technik.ui.components.LabeledTextField
import pl.intertell.technik.ui.components.SolidButton
import pl.intertell.technik.ui.theme.IntertellColors
import pl.intertell.technik.ui.theme.IntertellType

@Composable
fun LoginScreen(viewModel: TechnicianViewModel, state: TechnicianUiState) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var editingServer by remember { mutableStateOf(false) }
    var serverDraft by remember(state.serverUrl) { mutableStateOf(state.serverUrl) }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.technik),
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .width(186.dp)
                .align(Alignment.BottomEnd),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 28.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            Image(
                painter = painterResource(R.drawable.logo_intertell),
                contentDescription = "Intertell",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .width(212.dp)
                    .align(Alignment.CenterHorizontally),
            )
            Box(
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(top = 26.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(IntertellColors.GreenChipBg)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
            ) {
                Text("PANEL TECHNIKA", style = IntertellType.monoSmall, color = IntertellColors.Green)
            }
            Text(
                "Serwis w terenie",
                style = IntertellType.display,
                color = IntertellColors.TextPrimary,
                modifier = Modifier
                    .padding(top = 14.dp)
                    .align(Alignment.CenterHorizontally),
            )
            Text(
                "Zlecenia, instalacje i dostęp do klientów z bazy intratell.",
                style = IntertellType.body,
                color = IntertellColors.Text55,
                modifier = Modifier
                    .padding(top = 8.dp, bottom = 30.dp)
                    .align(Alignment.CenterHorizontally),
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                LabeledTextField(
                    label = "E-mail służbowy",
                    value = email,
                    onValueChange = { email = it },
                )
                LabeledTextField(
                    label = "Hasło",
                    value = password,
                    onValueChange = { password = it },
                    isPassword = true,
                )
            }

            if (state.loginError != null) {
                Text(
                    state.loginError,
                    style = IntertellType.bodySmall,
                    color = IntertellColors.Danger,
                    modifier = Modifier.padding(top = 10.dp),
                )
            }

            SolidButton(
                text = if (state.loginLoading) "Logowanie…" else "Zaloguj się",
                onClick = { viewModel.login(email, password) },
                enabled = !state.loginLoading,
                modifier = Modifier.padding(top = 22.dp),
            )
            if (state.loginLoading) {
                Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.Center) {
                    CircularProgressIndicator(modifier = Modifier.width(18.dp), color = IntertellColors.Accent, strokeWidth = 2.dp)
                }
            }

            if (!editingServer) {
                Text(
                    "Serwer: ${state.serverUrl} · Zmień",
                    style = IntertellType.monoFootnote,
                    color = IntertellColors.Text42,
                    modifier = Modifier
                        .padding(top = 22.dp)
                        .clickable { editingServer = true },
                )
            } else {
                Column(modifier = Modifier.padding(top = 18.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    LabeledTextField(label = "Adres serwera", value = serverDraft, onValueChange = { serverDraft = it })
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            "Zapisz",
                            style = IntertellType.bodyBold,
                            color = IntertellColors.Accent,
                            modifier = Modifier.clickable {
                                viewModel.setServerUrl(serverDraft)
                                editingServer = false
                            },
                        )
                        Text(
                            "Anuluj",
                            style = IntertellType.bodyBold,
                            color = IntertellColors.Text55,
                            modifier = Modifier.clickable { editingServer = false },
                        )
                    }
                }
            }

            Text(
                "Konta techników zakłada i blokuje administrator. Logowanie wymaga aktywnego statusu pracownika.",
                style = IntertellType.monoFootnote,
                color = IntertellColors.Text42,
                modifier = Modifier
                    .padding(top = 14.dp)
                    .width(240.dp),
            )
        }
    }
}
