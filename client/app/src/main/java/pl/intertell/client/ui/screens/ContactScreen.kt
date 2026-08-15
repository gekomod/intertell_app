package pl.intertell.client.ui.screens

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
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
fun ContactScreen(viewModel: ClientViewModel, state: ClientUiState) {
    val context = LocalContext.current
    val account by viewModel.account.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp, bottom = 20.dp),
    ) {
        BackLink("← Konto", onClick = viewModel::goSettings)
        Text("Kontakt z Operatorem", style = IntertellType.headline, color = IntertellColors.TextPrimary, modifier = Modifier.padding(top = 14.dp))

        Card(background = IntertellColors.Navy, border = null, modifier = Modifier.padding(top = 16.dp)) {
            Text("Zgłoszenie techniczne", style = IntertellType.bodySmall, color = IntertellColors.White.copy(alpha = 0.55f))
            Text("Masz problem z usługą?", style = IntertellType.headline, color = IntertellColors.White, modifier = Modifier.padding(top = 4.dp))
            Text(
                "Opisz go — zgłoszenie trafi bezpośrednio do kolejki serwisowej wraz z Twoim numerem klienta.",
                style = IntertellType.bodySmall,
                color = IntertellColors.White.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        Row(modifier = Modifier.fillMaxWidth().padding(top = 14.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SolidButton(
                if (state.actionInFlight) "Wysyłanie…" else "Zgłoś problem",
                onClick = viewModel::reportProblem,
                enabled = !state.actionInFlight,
                modifier = Modifier.weight(1f),
                height = 50,
            )
            OutlineButton("Czat z BOK", onClick = {}, modifier = Modifier.weight(1f), height = 50)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 14.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(IntertellColors.White),
        ) {
            ContactRow("Biuro Obsługi Klienta", "62 737 00 00") {
                context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:627370000")))
            }
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(IntertellColors.HairlineOnLightSoft))
            ContactRow("Awarie 24/7", "62 737 00 11") {
                context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:627370011")))
            }
            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(IntertellColors.HairlineOnLightSoft))
            ContactRow("E-mail", "bok@intertell.pl") {
                context.startActivity(Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:bok@intertell.pl")))
            }
        }

        Text(
            "Zgłoszenia z aplikacji trafiają bezpośrednio do kolejki serwisowej wraz z numerem klienta ${account?.contractNumber.orEmpty()}.",
            style = IntertellType.monoFootnote,
            color = IntertellColors.Text45,
            modifier = Modifier.padding(top = 14.dp),
        )
    }
}

@Composable
private fun ContactRow(label: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 18.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(label, style = IntertellType.titleBold, color = IntertellColors.TextPrimary)
        Text(value, style = IntertellType.mono.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold), color = IntertellColors.Accent)
    }
}
