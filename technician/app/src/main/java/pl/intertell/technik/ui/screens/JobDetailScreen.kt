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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import pl.intertell.technik.TechnicianViewModel
import pl.intertell.technik.ui.components.BackLink
import pl.intertell.technik.ui.components.Card
import pl.intertell.technik.ui.components.OutlineButton
import pl.intertell.technik.ui.components.SolidButton
import pl.intertell.technik.ui.theme.IntertellColors
import pl.intertell.technik.ui.theme.IntertellType

@Composable
fun JobDetailScreen(viewModel: TechnicianViewModel) {
    val context = LocalContext.current
    val job = viewModel.currentJob()
    val customer = viewModel.currentCustomer()

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(210.dp)
                .background(IntertellColors.HairlineOnLightFaint),
            contentAlignment = Alignment.BottomStart,
        ) {
            Text(
                "[ mapa · trasa 8,4 km · 14 min ]",
                style = IntertellType.mono,
                color = IntertellColors.Text5,
                modifier = Modifier.padding(16.dp),
            )
        }
        Column(modifier = Modifier.padding(20.dp)) {
            BackLink("← Zlecenia dnia", onClick = viewModel::goJobs)
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom,
            ) {
                Text(job.type, style = IntertellType.headline, color = IntertellColors.TextPrimary)
                Text(job.id, style = IntertellType.monoSmall, color = IntertellColors.Text45)
            }
            Text(
                "${job.time} · ${job.duration}",
                style = IntertellType.body,
                color = IntertellColors.Text6,
                modifier = Modifier.padding(top = 6.dp),
            )

            Card(modifier = Modifier.padding(top = 16.dp)) {
                Text(customer.name, style = IntertellType.titleBold, color = IntertellColors.TextPrimary)
                Text(
                    "${customer.address}, Ostrów",
                    style = IntertellType.body,
                    color = IntertellColors.Text6,
                    modifier = Modifier.padding(top = 4.dp),
                )
                Text(
                    "${customer.phone} · umowa ${customer.contract}",
                    style = IntertellType.mono,
                    color = IntertellColors.Text5,
                    modifier = Modifier.padding(top = 8.dp),
                )
                Row(modifier = Modifier.fillMaxWidth().padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    SolidButton(
                        "Zadzwoń",
                        onClick = { context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${customer.phone}"))) },
                        background = IntertellColors.Navy,
                        modifier = Modifier.weight(1f),
                        height = 44,
                    )
                    OutlineButton(
                        "Nawiguj",
                        onClick = {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("geo:0,0?q=${Uri.encode(customer.address + ", Ostrów")}")))
                        },
                        modifier = Modifier.weight(1f),
                        height = 44,
                    )
                }
            }

            Card(modifier = Modifier.padding(top = 14.dp)) {
                Text("ZAKRES", style = IntertellType.label, color = IntertellColors.Text45)
                Text(job.scope, style = IntertellType.body, color = IntertellColors.TextPrimary, modifier = Modifier.padding(top = 8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Pakiet", style = IntertellType.bodyBold, color = IntertellColors.Text6)
                    Text(customer.plan, style = IntertellType.bodyBold, color = IntertellColors.TextPrimary)
                }
                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text("Sprzęt do wydania", style = IntertellType.bodyBold, color = IntertellColors.Text6)
                    Text("ONT + router Wi-Fi 6", style = IntertellType.bodyBold, color = IntertellColors.TextPrimary)
                }
            }

            OutlineButton("Panel ONT Huawei tego klienta", onClick = viewModel::goRouter, modifier = Modifier.padding(top = 16.dp))
            SolidButton("Rozpocznij zlecenie", onClick = viewModel::goReport, modifier = Modifier.padding(top = 10.dp))
        }
    }
}
