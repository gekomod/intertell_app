package pl.intertell.client.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.intertell.client.ClientViewModel
import pl.intertell.client.ui.components.BackLink
import pl.intertell.client.ui.components.Card
import pl.intertell.client.ui.theme.IntertellColors
import pl.intertell.client.ui.theme.IntertellType

@Composable
fun ContractsScreen(viewModel: ClientViewModel) {
    val account by viewModel.account.collectAsState()

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
                "Numer klienta ${account?.contractNumber.orEmpty()}",
                style = IntertellType.headline,
                color = IntertellColors.White,
                modifier = Modifier.padding(top = 4.dp),
            )
            Text(
                "Skany umowy, aneksów i regulaminów udostępnia Biuro Obsługi Klienta.",
                style = IntertellType.bodySmall,
                color = IntertellColors.White.copy(alpha = 0.6f),
                modifier = Modifier.padding(top = 8.dp),
            )
        }

        Text(
            "Dokumenty w wersji elektronicznej pojawią się tutaj, gdy zostaną udostępnione w LMS.",
            style = IntertellType.body,
            color = IntertellColors.Text50,
            modifier = Modifier.padding(top = 16.dp),
        )
    }
}
