package pl.intertell.client.ui.screens

import androidx.compose.foundation.background
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
import pl.intertell.client.ui.components.Card
import pl.intertell.client.ui.theme.IntertellColors
import pl.intertell.client.ui.theme.IntertellType

@Composable
fun InvoicesScreen(viewModel: ClientViewModel, state: ClientUiState) {
    val invoices by viewModel.invoices.collectAsState()
    val unpaid = invoices.filter { !it.paid }
    val balanceLabel = if (unpaid.isEmpty()) "0,00 zł" else unpaid.first().amountZl + " zł"

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp, bottom = 20.dp),
    ) {
        Text("Faktury", style = IntertellType.display, color = IntertellColors.TextPrimary)

        Card(background = IntertellColors.Navy, border = null, radius = 18, modifier = Modifier.padding(top = 16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Column {
                    Text("Saldo do zapłaty", style = IntertellType.bodySmall, color = IntertellColors.White.copy(alpha = 0.55f))
                    Text(balanceLabel, style = IntertellType.headline, color = IntertellColors.White, modifier = Modifier.padding(top = 3.dp))
                }
                Box(
                    modifier = Modifier
                        .height(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(IntertellColors.Accent)
                        .padding(horizontal = 18.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text("Zapłać BLIK", style = IntertellType.bodyBold, color = IntertellColors.White)
                }
            }
        }

        if (state.invoicesLoading && invoices.isEmpty()) {
            Row(modifier = Modifier.fillMaxWidth().padding(top = 24.dp), horizontalArrangement = Arrangement.Center) {
                CircularProgressIndicator(color = IntertellColors.Accent)
            }
        } else if (invoices.isEmpty()) {
            Text(
                "Brak faktur.",
                style = IntertellType.body,
                color = IntertellColors.Text50,
                modifier = Modifier.padding(top = 24.dp),
            )
        } else {
            Column(modifier = Modifier.padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                invoices.forEach { invoice ->
                    Card(radius = 16, padding = 16, modifier = Modifier.clickable { viewModel.openInvoice(invoice.id) }) {
                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text(invoice.issuedOn, style = IntertellType.titleBold, color = IntertellColors.TextPrimary)
                                Text(invoice.number, style = IntertellType.mono, color = IntertellColors.Text45, modifier = Modifier.padding(top = 4.dp))
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(invoice.amountZl + " zł", style = IntertellType.titleBold, color = IntertellColors.TextPrimary)
                                Text(
                                    invoice.statusLabel,
                                    style = IntertellType.chip,
                                    color = if (invoice.paid) IntertellColors.Green else IntertellColors.Amber,
                                    modifier = Modifier.padding(top = 4.dp),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
