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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.Locale
import pl.intertell.client.ClientViewModel
import pl.intertell.client.ui.components.BackLink
import pl.intertell.client.ui.components.Card
import pl.intertell.client.ui.components.OutlineButton
import pl.intertell.client.ui.components.SolidButton
import pl.intertell.client.ui.theme.IntertellColors
import pl.intertell.client.ui.theme.IntertellType

@Composable
fun InvoiceDetailScreen(viewModel: ClientViewModel) {
    val invoice = viewModel.selectedInvoice()
    val status = viewModel.serviceStatus

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp, bottom = 20.dp),
    ) {
        BackLink("← Faktury", onClick = viewModel::goInvoices)
        Text(invoice.period, style = IntertellType.headline, color = IntertellColors.TextPrimary, modifier = Modifier.padding(top = 14.dp))
        Text(invoice.id, style = IntertellType.mono, color = IntertellColors.Text50, modifier = Modifier.padding(top = 5.dp))

        Card(modifier = Modifier.padding(top = 18.dp)) {
            LineRow("Abonament ${status.planName}", formatZl(invoice.subscriptionZl))
            LineRow("Dzierżawa routera", formatZl(invoice.routerLeaseZl), topPadding = 10.dp)

            Box(modifier = Modifier.fillMaxWidth().padding(top = 14.dp).height(1.dp).background(IntertellColors.HairlineOnLightSoft))
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Text("Razem brutto", style = IntertellType.bodyBold, color = IntertellColors.TextPrimary)
                Text(invoice.amountLabel, style = IntertellType.headline, color = IntertellColors.TextPrimary)
            }
            Text(
                "Termin płatności ${invoice.dueDate}",
                style = IntertellType.bodySmall,
                color = IntertellColors.Text50,
                modifier = Modifier.padding(top = 6.dp),
            )
        }

        Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SolidButton("Zapłać ${invoice.amountLabel}", onClick = {}, modifier = Modifier.weight(1f), height = 50)
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

@Composable
private fun LineRow(label: String, value: String, topPadding: androidx.compose.ui.unit.Dp = 0.dp) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(top = topPadding),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = IntertellType.body, color = IntertellColors.Text55)
        Text(value, style = IntertellType.body, color = IntertellColors.Text55)
    }
}

private fun formatZl(amount: Double): String = String.format(Locale("pl", "PL"), "%,.2f zł", amount)
