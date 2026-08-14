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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import pl.intertell.technik.TechnicianViewModel
import pl.intertell.technik.data.TeamMember
import pl.intertell.technik.ui.components.Card
import pl.intertell.technik.ui.components.LabeledTextField
import pl.intertell.technik.ui.components.SolidButton
import pl.intertell.technik.ui.components.ToggleSwitch
import pl.intertell.technik.ui.theme.IntertellColors
import pl.intertell.technik.ui.theme.IntertellType

@Composable
fun AdminScreen(viewModel: TechnicianViewModel) {
    val state by viewModel.uiState.collectAsState()
    val team by viewModel.team.collectAsState()
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var specialization by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var editingTech by remember { mutableStateOf<TeamMember?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp, bottom = 20.dp),
    ) {
        Text("Zespół", style = IntertellType.display, color = IntertellColors.TextPrimary)
        Text(
            "Lista i zarządzanie technikami z bazy intratell. Dodanie technika tworzy jego konto od razu.",
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
                    LabeledTextField("Telefon", phone, { phone = it }, modifier = Modifier.weight(1f))
                    LabeledTextField("Specjalizacja", specialization, { specialization = it }, modifier = Modifier.weight(1f))
                }
                LabeledTextField("Hasło startowe", password, { password = it }, isPassword = true)
            }
            SolidButton(
                text = "Dodaj technika",
                onClick = {
                    viewModel.addTechnician(name, email, phone, specialization, password)
                    name = ""; email = ""; phone = ""; specialization = ""; password = ""
                },
                background = IntertellColors.Navy,
                enabled = !state.actionInFlight,
                modifier = Modifier.padding(top = 14.dp),
                height = 50,
            )
        }

        if (state.errorMessage != null) {
            Text(state.errorMessage, style = IntertellType.bodySmall, color = IntertellColors.Danger, modifier = Modifier.padding(top = 12.dp))
        }
        if (state.infoMessage != null) {
            Text(state.infoMessage, style = IntertellType.bodySmall, color = IntertellColors.Green, modifier = Modifier.padding(top = 12.dp))
        }

        Text(
            "Technicy (${team.size})",
            style = IntertellType.bodyBold,
            color = IntertellColors.TextPrimary,
            modifier = Modifier.padding(top = 22.dp, bottom = 12.dp),
        )
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            team.forEach { member ->
                val (fg, bg) = if (member.active) {
                    IntertellColors.Green to IntertellColors.GreenChipBg
                } else {
                    IntertellColors.Text5 to IntertellColors.HairlineOnLightFaint
                }
                Card(radius = 16, padding = 14, modifier = Modifier.clickable { editingTech = member }) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier.size(36.dp).clip(CircleShape).background(IntertellColors.ToggleTrackOffFaint),
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(member.initials, style = IntertellType.chip, color = IntertellColors.TextPrimary)
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(member.name, style = IntertellType.titleBold, color = IntertellColors.TextPrimary)
                            Text(
                                member.specialization.ifBlank { member.email },
                                style = IntertellType.monoSmall,
                                color = IntertellColors.Text5,
                                modifier = Modifier.padding(top = 2.dp),
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(5.dp))
                                .background(bg)
                                .padding(horizontal = 7.dp, vertical = 3.dp),
                        ) {
                            Text(if (member.active) "AKTYWNY" else "NIEAKTYWNY", style = IntertellType.monoSmall, color = fg)
                        }
                    }
                }
            }
        }
    }

    val editing = editingTech
    if (editing != null) {
        EditTechnicianDialog(
            member = editing,
            busy = state.actionInFlight,
            onDismiss = { editingTech = null },
            onSave = { newName, newPhone, newSpec, newActive ->
                viewModel.updateTechnician(editing.id, newName, newPhone, newSpec, newActive)
                editingTech = null
            },
            onDelete = {
                viewModel.deleteTechnician(editing.id)
                editingTech = null
            },
        )
    }
}

@Composable
private fun EditTechnicianDialog(
    member: TeamMember,
    busy: Boolean,
    onDismiss: () -> Unit,
    onSave: (name: String, phone: String, specialization: String, active: Boolean) -> Unit,
    onDelete: () -> Unit,
) {
    var name by remember(member.id) { mutableStateOf(member.name) }
    var phone by remember(member.id) { mutableStateOf(member.phone) }
    var specialization by remember(member.id) { mutableStateOf(member.specialization) }
    var active by remember(member.id) { mutableStateOf(member.active) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edytuj technika", style = IntertellType.titleBold, color = IntertellColors.TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                LabeledTextField("Imię i nazwisko", name, { name = it })
                LabeledTextField("Telefon", phone, { phone = it })
                LabeledTextField("Specjalizacja", specialization, { specialization = it })
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text("Aktywny", style = IntertellType.bodyBold, color = IntertellColors.TextPrimary)
                    ToggleSwitch(checked = active, onCheckedChange = { active = it })
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onSave(name, phone, specialization, active) }, enabled = !busy) {
                Text("Zapisz", color = IntertellColors.Accent)
            }
        },
        dismissButton = {
            Row {
                TextButton(onClick = onDelete, enabled = !busy) {
                    Text("Usuń", color = IntertellColors.Danger)
                }
                TextButton(onClick = onDismiss) {
                    Text("Anuluj", color = IntertellColors.Text55)
                }
            }
        },
    )
}
