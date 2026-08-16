package pl.intertell.technik.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import pl.intertell.technik.TechnicianViewModel
import pl.intertell.technik.data.JobKind
import pl.intertell.technik.data.geo.LatLng
import pl.intertell.technik.ui.components.AddressMapPreview
import pl.intertell.technik.ui.components.BackLink
import pl.intertell.technik.ui.components.Card
import pl.intertell.technik.ui.components.OutlineButton
import pl.intertell.technik.ui.components.SolidButton
import pl.intertell.technik.ui.theme.IntertellColors
import pl.intertell.technik.ui.theme.IntertellType

@Composable
fun JobDetailScreen(viewModel: TechnicianViewModel) {
    val context = LocalContext.current
    val job by viewModel.selectedJob.collectAsState()
    val state by viewModel.uiState.collectAsState()
    if (job == null) return
    val j = job!!

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
    ) {
        if (j.address.isNotBlank()) {
            val known = if (j.lat != null && j.lon != null) LatLng(j.lat, j.lon) else null
            AddressMapPreview(
                address = j.address, viewModel = viewModel, height = 210.dp, rounded = false,
                showRoute = true, knownLocation = known,
            )
        }
        Column(modifier = Modifier.padding(20.dp)) {
        BackLink("← Zlecenia", onClick = viewModel::goJobs)
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text(j.title, style = IntertellType.headline, color = IntertellColors.TextPrimary)
            Text(j.statusLabel, style = IntertellType.monoSmall, color = IntertellColors.Text45)
        }
        Text(
            when (j.kind) {
                JobKind.MESSAGE -> "Zapytanie kontaktowe · ${j.createdAt}"
                JobKind.INSTALL -> "Zgłoszenie instalacyjne · ${j.createdAt}"
                JobKind.LMS_OUTAGE -> "Awaria z LMS · ${j.createdAt}"
            },
            style = IntertellType.body,
            color = IntertellColors.Text6,
            modifier = Modifier.padding(top = 6.dp),
        )

        Card(modifier = Modifier.padding(top = 16.dp)) {
            Text(j.clientName, style = IntertellType.titleBold, color = IntertellColors.TextPrimary)
            if (j.address.isNotBlank()) {
                Text(j.address, style = IntertellType.body, color = IntertellColors.Text6, modifier = Modifier.padding(top = 4.dp))
            }
            if (j.customerNo.isNotBlank()) {
                Text("nr klienta ${j.customerNo}", style = IntertellType.mono, color = IntertellColors.Text5, modifier = Modifier.padding(top = 4.dp))
            }
            if (j.detail.isNotBlank()) {
                Text(j.detail, style = IntertellType.body, color = IntertellColors.TextPrimary, modifier = Modifier.padding(top = 10.dp))
            }
            Row(modifier = Modifier.fillMaxWidth().padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                if (j.phone.isNotBlank()) {
                    SolidButton(
                        "Zadzwoń",
                        onClick = { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${j.phone}"))) },
                        background = IntertellColors.Navy,
                        modifier = Modifier.weight(1f),
                        height = 44,
                    )
                }
                if (j.address.isNotBlank()) {
                    OutlineButton(
                        "Nawiguj",
                        onClick = {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${Uri.encode(j.address)}")))
                        },
                        modifier = Modifier.weight(1f),
                        height = 44,
                    )
                }
            }
        }

        if (j.kind != JobKind.INSTALL && j.customerNo.isNotBlank()) {
            OutlineButton(
                "Karta klienta i panel ONT",
                onClick = viewModel::openCustomerForSelectedJob,
                modifier = Modifier.padding(top = 14.dp),
            )
        }

        state.errorMessage?.let { error ->
            Text(
                error,
                style = IntertellType.bodySmall,
                color = IntertellColors.Danger,
                modifier = Modifier.padding(top = 14.dp),
            )
        }

        when (j.status) {
            "done" -> Text(
                "Zgłoszenie zakończone.",
                style = IntertellType.bodyBold,
                color = IntertellColors.Green,
                modifier = Modifier.padding(top = 18.dp),
            )
            "in_progress" -> SolidButton(
                "Zakończ zgłoszenie",
                onClick = viewModel::finishJob,
                enabled = !state.actionInFlight,
                modifier = Modifier.padding(top = 18.dp),
            )
            else -> SolidButton(
                "Rozpocznij zgłoszenie",
                onClick = viewModel::startJob,
                enabled = !state.actionInFlight,
                modifier = Modifier.padding(top = 18.dp),
            )
        }
        }
    }
}
