package pl.intertell.technik.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import pl.intertell.technik.TechnicianViewModel
import pl.intertell.technik.data.CustomerState
import pl.intertell.technik.ui.components.BackLink
import pl.intertell.technik.ui.components.OutlineButton
import pl.intertell.technik.ui.components.SolidButton
import pl.intertell.technik.ui.theme.IntertellColors
import pl.intertell.technik.ui.theme.IntertellType

@Composable
fun CustomerScreen(viewModel: TechnicianViewModel) {
    val context = LocalContext.current
    val customer = viewModel.currentCustomer()
    val stateColor = if (customer.state == CustomerState.OK) IntertellColors.GreenCheck else IntertellColors.Danger

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp, bottom = 20.dp),
    ) {
        BackLink("← Klienci", onClick = viewModel::goSearch)
        Text(customer.address, style = IntertellType.headline, color = IntertellColors.TextPrimary, modifier = Modifier.padding(top = 14.dp))
        Text(
            "${customer.name} · ${customer.contract}",
            style = IntertellType.body,
            color = IntertellColors.Text6,
            modifier = Modifier.padding(top = 5.dp),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(IntertellColors.Navy)
                .padding(horizontal = 20.dp, vertical = 18.dp),
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Stan usługi", style = IntertellType.bodySmall, color = IntertellColors.White.copy(alpha = 0.55f))
                Text(customer.state.name, style = IntertellType.monoSmall, color = stateColor)
            }
            Text(customer.plan, style = IntertellType.headline, color = IntertellColors.White, modifier = Modifier.padding(top = 6.dp))
            Row(modifier = Modifier.padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                NavyMetric("ONT", customer.ont)
                NavyMetric("RX", customer.rx)
                NavyMetric("OLT / PORT", customer.olt)
            }
        }

        SolidButton("Otwórz panel ONT Huawei", onClick = viewModel::goRouter, modifier = Modifier.padding(top = 14.dp))
        Row(modifier = Modifier.fillMaxWidth().padding(top = 10.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlineButton(
                "Zadzwoń",
                onClick = { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${customer.phone}"))) },
                modifier = Modifier.weight(1f),
                height = 48,
            )
            OutlineButton(
                "Nawiguj",
                onClick = {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${Uri.encode(customer.address + ", Ostrów")}")))
                },
                modifier = Modifier.weight(1f),
                height = 48,
            )
            OutlineButton("Nowe zlecenie", onClick = {}, modifier = Modifier.weight(1f), height = 48)
        }

        Text(
            "Historia awarii i wizyt",
            style = IntertellType.bodyBold,
            color = IntertellColors.TextPrimary,
            modifier = Modifier.padding(top = 22.dp, bottom = 10.dp),
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(IntertellColors.White),
        ) {
            customer.history.forEachIndexed { index, entry ->
                Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(entry.what, style = IntertellType.titleBold, color = IntertellColors.TextPrimary)
                        Text(entry.date, style = IntertellType.monoSmall, color = IntertellColors.Text5)
                    }
                    Text(entry.note, style = IntertellType.body, color = IntertellColors.Text55, modifier = Modifier.padding(top = 4.dp))
                }
                if (index != customer.history.lastIndex) {
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(IntertellColors.HairlineOnLightFaint))
                }
            }
        }

        Text(
            "Dane abonenta pobrane z LMS. Podgląd nie obejmuje faktur ani danych płatniczych.",
            style = IntertellType.monoFootnote,
            color = IntertellColors.Text45,
            modifier = Modifier.padding(top = 14.dp),
        )
    }
}

@Composable
private fun NavyMetric(label: String, value: String) {
    Column {
        Text(label, style = IntertellType.monoSmall, color = IntertellColors.White.copy(alpha = 0.5f))
        Text(value, style = IntertellType.monoBold, color = IntertellColors.White, modifier = Modifier.padding(top = 2.dp))
    }
}
