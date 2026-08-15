package pl.intertell.client.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.intertell.client.ClientUiState
import pl.intertell.client.ClientViewModel
import pl.intertell.client.ui.components.BackLink
import pl.intertell.client.ui.components.Card
import pl.intertell.client.ui.components.OutlineButton
import pl.intertell.client.ui.components.SolidButton
import pl.intertell.client.ui.theme.IntertellColors
import pl.intertell.client.ui.theme.IntertellType

@Composable
fun InvoiceDetailScreen(viewModel: ClientViewModel, state: ClientUiState) {
    val invoice by viewModel.selectedInvoice.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp, bottom = 20.dp),
    ) {
        BackLink("← Faktury", onClick = viewModel::goInvoices)

        if (state.actionInFlight && invoice == null) {
            Row(modifier = Modifier.fillMaxWidth().padding(top = 24.dp), horizontalArrangement = Arrangement.Center) {
                CircularProgressIndicator(color = IntertellColors.Accent)
            }
            return
        }
        val inv = invoice ?: return

        Text(inv.issuedOn, style = IntertellType.headline, color = IntertellColors.TextPrimary, modifier = Modifier.padding(top = 14.dp))
        Text(inv.number, style = IntertellType.mono, color = IntertellColors.Text50, modifier = Modifier.padding(top = 5.dp))

        Card(modifier = Modifier.padding(top = 18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Text("Razem brutto", style = IntertellType.bodyBold, color = IntertellColors.TextPrimary)
                Text(inv.amountZl + " zł", style = IntertellType.headline, color = IntertellColors.TextPrimary)
            }
            Text(
                "Termin płatności ${inv.dueOn}",
                style = IntertellType.bodySmall,
                color = IntertellColors.Text50,
                modifier = Modifier.padding(top = 6.dp),
            )
            Text(
                inv.statusLabel,
                style = IntertellType.chip,
                color = if (inv.paid) IntertellColors.Green else IntertellColors.Amber,
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SolidButton("Zapłać ${inv.amountZl} zł", onClick = {}, modifier = Modifier.weight(1f), height = 50, enabled = !inv.paid)
            OutlineButton("PDF", onClick = {}, modifier = Modifier.width(110.dp), height = 50)
        }

        Text(
            "Dokument wystawiony w LMS. Płatność księgowana na koncie abonenckim w ciągu 15 minut.",
            style = IntertellType.monoFootnote,
            color = IntertellColors.Text45,
            modifier = Modifier.padding(top = 16.dp),
        )
    }
}
