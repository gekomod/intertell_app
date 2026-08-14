package pl.intertell.technik.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import pl.intertell.technik.TechnicianUiState
import pl.intertell.technik.TechnicianViewModel
import pl.intertell.technik.data.TechStatus
import pl.intertell.technik.ui.components.Card
import pl.intertell.technik.ui.components.LabeledTextField
import pl.intertell.technik.ui.components.SolidButton
import pl.intertell.technik.ui.theme.IntertellColors
import pl.intertell.technik.ui.theme.IntertellType

@Composable
fun AdminScreen(viewModel: TechnicianViewModel, state: TechnicianUiState) {
    val team by viewModel.team.collectAsState()
    var name by remember { mutableStateOf("Paweł Nowicki") }
    var email by remember { mutableStateOf("p.nowicki@intertell.pl") }
    var role by remember { mutableStateOf("Technik") }
    var area by remember { mutableStateOf("Ostrów") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp, bottom = 20.dp),
    ) {
        Text("Zespół", style = IntertellType.display, color = IntertellColors.TextPrimary)
        Text(
            "Widok administratora, zsynchronizowany z panelem intertell.pl. Dodanie technika wysyła zaproszenie i tworzy konto serwisowe.",
            style = IntertellType.bodySmall,
            color = IntertellColors.Text55,
            modifier = Modifier.padding(top = 6.dp),
        )

        Card(modifier = Modifier.padding(top = 18.dp)) {
            Text("NOWY TECHNIK", style = IntertellType.label, color = IntertellColors.Text45)
            Column(modifier = Modifier.padding(top = 12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                LabeledTextField("Imię i nazwisko", name, { name = it })
                LabeledTextField("E-mail służbowy", email, { email = it })
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    LabeledTextField("Rola", role, { role = it }, modifier = Modifier.weight(1f))
                    LabeledTextField("Obszar", area, { area = it }, modifier = Modifier.weight(1f))
                }
            }
            SolidButton(
                text = if (state.teamMemberInvited) "Zaproszenie wysłane" else "Dodaj technika i wyślij zaproszenie",
                onClick = { viewModel.addTeamMember(name, email, role, area) },
                background = IntertellColors.Navy,
                enabled = !state.teamMemberInvited,
                modifier = Modifier.padding(top = 14.dp),
                height = 50,
            )
        }

        Text(
            "Technicy (${team.size})",
            style = IntertellType.bodyBold,
            color = IntertellColors.TextPrimary,
            modifier = Modifier.padding(top = 22.dp, bottom = 12.dp),
        )
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            team.forEach { member ->
                val (fg, bg) = when (member.status) {
                    TechStatus.NA_SLUZBIE -> IntertellColors.Green to IntertellColors.GreenChipBg
                    TechStatus.ZAPROSZONY -> IntertellColors.Amber to IntertellColors.AmberChipBg
                    TechStatus.WOLNE -> IntertellColors.Text5 to IntertellColors.HairlineOnLightFaint
                }
                Card(radius = 16, padding = 14) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier.size(36.dp).clip(CircleShape).background(IntertellColors.ToggleTrackOffFaint),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(member.initials, style = IntertellType.chip, color = IntertellColors.TextPrimary)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(member.name, style = IntertellType.titleBold, color = IntertellColors.TextPrimary)
                            Text("${member.id} · ${member.area}", style = IntertellType.monoSmall, color = IntertellColors.Text5, modifier = Modifier.padding(top = 2.dp))
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(5.dp))
                                .background(bg)
                                .padding(horizontal = 7.dp, vertical = 3.dp),
                        ) {
                            Text(statusLabel(member.status), style = IntertellType.monoSmall, color = fg)
                        }
                    }
                }
            }
        }
    }
}

private fun statusLabel(status: TechStatus): String = when (status) {
    TechStatus.NA_SLUZBIE -> "NA SŁUŻBIE"
    TechStatus.WOLNE -> "WOLNE"
    TechStatus.ZAPROSZONY -> "ZAPROSZONY"
}
