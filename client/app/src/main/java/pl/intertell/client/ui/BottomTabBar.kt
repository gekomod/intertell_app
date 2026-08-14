package pl.intertell.client.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import pl.intertell.client.ClientScreen
import pl.intertell.client.ClientViewModel
import pl.intertell.client.ui.theme.IntertellColors
import pl.intertell.client.ui.theme.IntertellType

private val settingsGroup = setOf(ClientScreen.SETTINGS, ClientScreen.CONTRACTS, ClientScreen.DMZ, ClientScreen.CONTACT)
private val invoiceGroup = setOf(ClientScreen.INVOICES, ClientScreen.INVOICE)

@Composable
fun BottomTabBar(screen: ClientScreen, viewModel: ClientViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(IntertellColors.White.copy(alpha = 0.94f))
            .border(0.dp, Color.Transparent)
            .padding(top = 10.dp, bottom = 24.dp, start = 8.dp, end = 8.dp),
    ) {
        TabItem("Start", screen == ClientScreen.HOME, Modifier.weight(1f), onClick = viewModel::goHome)
        TabItem("Faktury", screen in invoiceGroup, Modifier.weight(1f), onClick = viewModel::goInvoices)
        TabItem("Pakiet", screen == ClientScreen.PLAN, Modifier.weight(1f), onClick = viewModel::goPlan)
        TabItem("Konto", screen in settingsGroup, Modifier.weight(1f), onClick = viewModel::goSettings)
    }
}

@Composable
private fun TabItem(label: String, active: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val color = if (active) IntertellColors.Accent else IntertellColors.ToggleTrackOff
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(color),
        )
        Text(
            label,
            style = IntertellType.chip,
            color = color,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}
