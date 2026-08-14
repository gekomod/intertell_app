package pl.intertell.technik.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.intertell.technik.TechnicianViewModel
import pl.intertell.technik.ui.components.BackLink
import pl.intertell.technik.ui.components.Card
import pl.intertell.technik.ui.components.SolidButton
import pl.intertell.technik.ui.theme.IntertellColors
import pl.intertell.technik.ui.theme.IntertellType

@Composable
fun ReportScreen(viewModel: TechnicianViewModel) {
    val job by viewModel.selectedJob.collectAsState()
    val state by viewModel.uiState.collectAsState()
    if (job == null) return
    val j = job!!

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
    ) {
        BackLink("← ${j.title}", onClick = viewModel::goJob)
        Text("W trakcie realizacji", style = IntertellType.headline, color = IntertellColors.TextPrimary, modifier = Modifier.padding(top = 14.dp))
        Text(
            "Status zgłoszenia jest zapisywany na bieżąco w bazie intratell.",
            style = IntertellType.bodySmall,
            color = IntertellColors.Text55,
            modifier = Modifier.padding(top = 5.dp),
        )

        Card(modifier = Modifier.padding(top = 18.dp)) {
            Text(j.clientName, style = IntertellType.titleBold, color = IntertellColors.TextPrimary)
            if (j.address.isNotBlank()) {
                Text(j.address, style = IntertellType.body, color = IntertellColors.Text6, modifier = Modifier.padding(top = 4.dp))
            }
            if (j.detail.isNotBlank()) {
                Text(j.detail, style = IntertellType.body, color = IntertellColors.TextPrimary, modifier = Modifier.padding(top = 10.dp))
            }
        }

        state.errorMessage?.let { error ->
            Text(
                error,
                style = IntertellType.bodySmall,
                color = IntertellColors.Danger,
                modifier = Modifier.padding(top = 14.dp),
            )
        }

        SolidButton(
            "Zakończ i wyślij do bazy",
            onClick = viewModel::finishJob,
            enabled = !state.actionInFlight,
            modifier = Modifier.padding(top = 18.dp),
        )
    }
}
