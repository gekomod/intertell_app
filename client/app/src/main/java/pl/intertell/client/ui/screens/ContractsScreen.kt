package pl.intertell.client.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import pl.intertell.client.ClientViewModel
import pl.intertell.client.ui.components.BackLink
import pl.intertell.client.ui.components.Card
import pl.intertell.client.ui.theme.IntertellColors
import pl.intertell.client.ui.theme.IntertellType

@Composable
fun ContractsScreen(viewModel: ClientViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp, bottom = 20.dp),
    ) {
        BackLink("← Konto", onClick = viewModel::goSettings)
        Text("Umowy", style = IntertellType.display, color = IntertellColors.TextPrimary, modifier = Modifier.padding(top = 14.dp))

        Card(background = IntertellColors.Navy, border = null, modifier = Modifier.padding(top = 16.dp)) {
            Text("Umowa abonencka", style = IntertellType.bodySmall, color = IntertellColors.White.copy(alpha = 0.55f))
            Text(
                "Czas określony do ${viewModel.contractEndDate}",
                style = IntertellType.headline,
                color = IntertellColors.White,
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                "Po tym terminie umowa przechodzi na czas nieokreślony z miesięcznym okresem wypowiedzenia.",
                style = IntertellType.bodySmall,
                color = IntertellColors.White.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        Column(modifier = Modifier.padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            viewModel.documents.forEach { doc ->
                Card(radius = 16, padding = 16) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
                        Box(
                            modifier = Modifier
                                .width(34.dp)
                                .height(42.dp)
                                .clip(RoundedCornerShape(5.dp))
                                .background(IntertellColors.ToggleTrackOffFaint),
                            contentAlignment = Alignment.BottomCenter,
                        ) {
                            Text("PDF", style = IntertellType.monoSmall, color = IntertellColors.Text50, modifier = Modifier.padding(bottom = 5.dp))
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(doc.name, style = IntertellType.titleBold, color = IntertellColors.TextPrimary)
                            Text(doc.meta, style = IntertellType.label, color = IntertellColors.Text50, modifier = Modifier.padding(top = 3.dp))
                        }
                        Text("Pobierz", style = IntertellType.bodyBold, color = IntertellColors.Accent)
                    }
                }
            }
        }
    }
}
