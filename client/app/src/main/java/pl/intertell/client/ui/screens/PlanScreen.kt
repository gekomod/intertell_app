package pl.intertell.client.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import pl.intertell.client.ClientUiState
import pl.intertell.client.ClientViewModel
import pl.intertell.client.data.PlanDirection
import pl.intertell.client.ui.components.Card
import pl.intertell.client.ui.theme.IntertellColors
import pl.intertell.client.ui.theme.IntertellType

@Composable
fun PlanScreen(viewModel: ClientViewModel, state: ClientUiState) {
    val plans by viewModel.plans.collectAsState()
    val current = plans.firstOrNull { it.direction == PlanDirection.CURRENT }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp, bottom = 20.dp),
    ) {
        Text("Pakiet", style = IntertellType.display, color = IntertellColors.TextPrimary)

        if (state.plansLoading && plans.isEmpty()) {
            Row(modifier = Modifier.fillMaxWidth().padding(top = 24.dp), horizontalArrangement = Arrangement.Center) {
                CircularProgressIndicator(color = IntertellColors.Accent)
            }
            return
        }

        if (current != null) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(IntertellColors.Surface)
                    .border(1.5.dp, IntertellColors.Accent, RoundedCornerShape(20.dp))
                    .padding(20.dp),
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column {
                        Text("PAKIET AKTUALNY", style = IntertellType.label, color = IntertellColors.Accent)
                        Text(current.name, style = IntertellType.headline, color = IntertellColors.TextPrimary, modifier = Modifier.padding(top = 6.dp))
                        Text(
                            "${current.speedLabel} · router Wi-Fi 6 w cenie",
                            style = IntertellType.body,
                            color = IntertellColors.Text55,
                            modifier = Modifier.padding(top = 3.dp),
                        )
                    }
                    Column(horizontalAlignment = Alignment.End) {
                        Text(current.priceLabel, style = IntertellType.headline, color = IntertellColors.TextPrimary)
                        Text("/ mies.", style = IntertellType.label, color = IntertellColors.Text50)
                    }
                }
            }
        }

        Text("Dostępne pakiety", style = IntertellType.bodyBold, color = IntertellColors.TextPrimary, modifier = Modifier.padding(top = 22.dp, bottom = 12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            plans.forEach { plan ->
                val badgeColor = when (plan.direction) {
                    PlanDirection.CURRENT -> IntertellColors.Accent
                    PlanDirection.HIGHER -> IntertellColors.Green
                    PlanDirection.LOWER -> IntertellColors.Text50
                }
                Card(
                    radius = 18,
                    padding = 18,
                    modifier = Modifier.clickable(enabled = plan.direction != PlanDirection.CURRENT) { viewModel.openPlanSheet(plan.id) },
                ) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(plan.name, style = IntertellType.titleBold, color = IntertellColors.TextPrimary)
                                Box(
                                    modifier = Modifier
                                        .padding(start = 8.dp)
                                        .clip(RoundedCornerShape(5.dp))
                                        .background(IntertellColors.ToggleTrackOffFaint)
                                        .padding(horizontal = 6.dp, vertical = 2.dp),
                                ) {
                                    Text(plan.badge, style = IntertellType.monoSmall, color = badgeColor)
                                }
                            }
                            Text(plan.speedLabel, style = IntertellType.label, color = IntertellColors.Text50, modifier = Modifier.padding(top = 5.dp))
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(plan.priceLabel, style = IntertellType.titleBold, color = IntertellColors.TextPrimary)
                            Text(plan.deltaLabel, style = IntertellType.label, color = IntertellColors.Text45)
                        }
                    }
                }
            }
        }

        Text(
            "Podwyższenie pakietu działa od razu. Obniżenie wymaga wniosku i wchodzi w życie z nowym okresem rozliczeniowym.",
            style = IntertellType.monoFootnote,
            color = IntertellColors.Text45,
            modifier = Modifier.padding(top = 16.dp),
        )
    }
}
