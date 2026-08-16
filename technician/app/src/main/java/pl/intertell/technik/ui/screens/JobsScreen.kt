package pl.intertell.technik.ui.screens

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import pl.intertell.technik.TechnicianViewModel
import pl.intertell.technik.data.JobKind
import pl.intertell.technik.ui.components.Card
import pl.intertell.technik.ui.theme.IntertellColors
import pl.intertell.technik.ui.theme.IntertellType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val AUTO_REFRESH_INTERVAL_MS = 20_000L

private val todayLabel: String
    get() = SimpleDateFormat("EEEE, dd.MM.yyyy", Locale("pl", "PL")).format(Date())

@Composable
fun JobsScreen(viewModel: TechnicianViewModel) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    val jobs by viewModel.jobs.collectAsState()
    val me by viewModel.me.collectAsState()
    val officePhone by viewModel.officePhone.collectAsState()
    val open = jobs.count { it.status != "done" }
    val urgentCount = jobs.count { it.isUrgent }

    // The list otherwise only refreshes on login or on re-tapping the Zlecenia
    // tab — poll quietly while this screen is visible so a zlecenie assigned
    // while the technician is sitting here shows up without any action.
    LaunchedEffect(Unit) {
        while (true) {
            delay(AUTO_REFRESH_INTERVAL_MS)
            viewModel.refreshTasks()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp, bottom = 20.dp),
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Column {
                Text("Zlecenia", style = IntertellType.display, color = IntertellColors.TextPrimary)
                Text(
                    me?.let { "$todayLabel · ${it.code}" } ?: "",
                    style = IntertellType.mono,
                    color = IntertellColors.Text5,
                    modifier = Modifier.padding(top = 5.dp),
                )
            }
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (officePhone.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(9.dp))
                            .clickable {
                                context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$officePhone")))
                            }
                            .background(IntertellColors.Navy)
                            .padding(9.dp),
                    ) {
                        Icon(Icons.Default.Call, contentDescription = "Zadzwoń do biura", tint = IntertellColors.White, modifier = Modifier.size(16.dp))
                    }
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9.dp))
                        .clickable(enabled = !state.jobsLoading) { viewModel.refreshTasks() }
                        .background(IntertellColors.ToggleTrackOffFaint)
                        .padding(9.dp),
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Odśwież", tint = IntertellColors.Text5, modifier = Modifier.size(16.dp))
                }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(9.dp))
                        .background(IntertellColors.GreenChipBg)
                        .padding(horizontal = 11.dp, vertical = 7.dp),
                ) {
                    Text("NA SŁUŻBIE", style = IntertellType.monoSmall, color = IntertellColors.Green)
                }
            }
        }

        Row(modifier = Modifier.fillMaxWidth().padding(top = 18.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile("$open", "zgłoszenia otwarte", Modifier.weight(1f))
            StatTile("$urgentCount", "nowe (pilne)", Modifier.weight(1f))
            StatTile("${jobs.size}", "łącznie", Modifier.weight(1f))
        }

        state.errorMessage?.let { error ->
            Text(
                error,
                style = IntertellType.bodySmall,
                color = IntertellColors.Danger,
                modifier = Modifier.padding(top = 14.dp),
            )
        }

        if (state.jobsLoading && jobs.isEmpty()) {
            Row(modifier = Modifier.fillMaxWidth().padding(top = 24.dp), horizontalArrangement = Arrangement.Center) {
                CircularProgressIndicator(color = IntertellColors.Accent)
            }
        } else if (jobs.isEmpty()) {
            Text(
                "Brak zgłoszeń przypisanych do Ciebie.",
                style = IntertellType.body,
                color = IntertellColors.Text5,
                modifier = Modifier.padding(top = 24.dp),
            )
        } else {
            Column(modifier = Modifier.padding(top = 18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                jobs.forEach { job ->
                    Card(radius = 18, padding = 16, modifier = Modifier.clickable { viewModel.openJob(job) }) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.width(90.dp)) {
                                Text(
                                    when (job.kind) {
                                        JobKind.MESSAGE -> "ZGŁOSZENIE"
                                        JobKind.INSTALL -> "INSTALACJA"
                                        JobKind.LMS_OUTAGE -> "AWARIA LMS"
                                    },
                                    style = IntertellType.monoSmall,
                                    color = if (job.kind == JobKind.LMS_OUTAGE) IntertellColors.Danger else IntertellColors.Text45,
                                )
                                Text(job.createdAt, style = IntertellType.label, color = IntertellColors.Text45, modifier = Modifier.padding(top = 2.dp))
                            }
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(start = 14.dp),
                            ) {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                                    Text(job.title, style = IntertellType.titleBold, color = IntertellColors.TextPrimary)
                                    val urgent = job.isUrgent
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(5.dp))
                                            .background(if (urgent) IntertellColors.DangerChipBg else IntertellColors.ToggleTrackOffFaint)
                                            .padding(horizontal = 6.dp, vertical = 2.dp),
                                    ) {
                                        Text(
                                            job.statusLabel,
                                            style = IntertellType.monoSmall,
                                            color = if (urgent) IntertellColors.Danger else IntertellColors.Text5,
                                        )
                                    }
                                }
                                Text(
                                    job.clientName,
                                    style = IntertellType.body,
                                    color = IntertellColors.Text6,
                                    modifier = Modifier.padding(top = 6.dp),
                                )
                                if (job.address.isNotBlank()) {
                                    Text(job.address, style = IntertellType.monoSmall, color = IntertellColors.Text4, modifier = Modifier.padding(top = 4.dp))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatTile(value: String, label: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier, radius = 16, padding = 14) {
        Text(value, style = IntertellType.headline, color = IntertellColors.TextPrimary)
        Text(label, style = IntertellType.label, color = IntertellColors.Text5, modifier = Modifier.padding(top = 2.dp))
    }
}
