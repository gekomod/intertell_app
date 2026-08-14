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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import pl.intertell.technik.TechnicianViewModel
import pl.intertell.technik.data.JobStatus
import pl.intertell.technik.ui.components.Card
import pl.intertell.technik.ui.theme.IntertellColors
import pl.intertell.technik.ui.theme.IntertellType

@Composable
fun JobsScreen(viewModel: TechnicianViewModel) {
    val jobs = viewModel.jobs
    val urgentCount = jobs.count { it.status == JobStatus.PILNE }

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
                    "${viewModel.todayLabel} · ${viewModel.technicianId}",
                    style = IntertellType.mono,
                    color = IntertellColors.Text5,
                    modifier = Modifier.padding(top = 5.dp),
                )
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

        Row(modifier = Modifier.fillMaxWidth().padding(top = 18.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatTile("${jobs.size}", "zlecenia dziś", Modifier.weight(1f))
            StatTile("$urgentCount", "awaria pilna", Modifier.weight(1f))
            StatTile("38", "km trasy", Modifier.weight(1f))
        }

        Column(modifier = Modifier.padding(top = 18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            jobs.forEachIndexed { index, job ->
                val customer = viewModel.customers[job.customerIndex]
                Card(radius = 18, padding = 16, modifier = Modifier.clickable { viewModel.openJob(index) }) {
                    Row(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.width(52.dp)) {
                            Text(job.time, style = IntertellType.monoBold, color = IntertellColors.TextPrimary)
                            Text(job.duration, style = IntertellType.label, color = IntertellColors.Text45, modifier = Modifier.padding(top = 2.dp))
                        }
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 14.dp),
                        ) {
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Bottom) {
                                Text(job.type, style = IntertellType.titleBold, color = IntertellColors.TextPrimary)
                                val urgent = job.status == JobStatus.PILNE
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(if (urgent) IntertellColors.DangerChipBg else IntertellColors.ToggleTrackOffFaint)
                                        .padding(horizontal = 6.dp, vertical = 2.dp),
                                ) {
                                    Text(
                                        if (urgent) "PILNE" else "ZAPLANOWANE",
                                        style = IntertellType.monoSmall,
                                        color = if (urgent) IntertellColors.Danger else IntertellColors.Text5,
                                    )
                                }
                            }
                            Text(
                                "${customer.name} · ${customer.address}",
                                style = IntertellType.body,
                                color = IntertellColors.Text6,
                                modifier = Modifier.padding(top = 6.dp),
                            )
                            Text(
                                "${job.id} · ${customer.plan}",
                                style = IntertellType.monoSmall,
                                color = IntertellColors.Text4,
                                modifier = Modifier.padding(top = 6.dp),
                            )
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
