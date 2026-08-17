package pl.intertell.client.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import pl.intertell.client.ClientUiState
import pl.intertell.client.ClientViewModel
import pl.intertell.client.data.PlanDirection
import pl.intertell.client.ui.components.FullScreenOutcome
import pl.intertell.client.ui.components.SolidButton
import pl.intertell.client.ui.theme.IntertellColors
import pl.intertell.client.ui.theme.IntertellType

@Composable
fun PlanChangeSheet(viewModel: ClientViewModel, state: ClientUiState) {
    val plan = viewModel.currentPlanSheet() ?: return
    val up = plan.direction == PlanDirection.HIGHER
    val requiresRequest = plan.requiresRequest

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 26.dp, topEnd = 26.dp))
            .background(IntertellColors.Surface)
            .padding(start = 22.dp, top = 24.dp, end = 22.dp, bottom = 44.dp),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .size(width = 44.dp, height = 4.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(IntertellColors.ToggleTrackOff.copy(alpha = 0.15f)),
        )
        Text(
            if (up) "PODWYŻSZENIE PAKIETU" else "OBNIŻENIE PAKIETU",
            style = IntertellType.label,
            color = IntertellColors.Accent,
            modifier = Modifier.padding(top = 20.dp),
        )
        Text(plan.name, style = IntertellType.headline, color = IntertellColors.TextPrimary, modifier = Modifier.padding(top = 8.dp))
        Text(
            sheetBody(requiresRequest),
            style = IntertellType.body,
            color = IntertellColors.Text55,
            modifier = Modifier.padding(top = 10.dp),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(IntertellColors.ScreenBackground)
                .padding(16.dp),
        ) {
            SheetLine("Nowy abonament", "${plan.priceLabel} / mies.")
            SheetLine("Obowiązuje od", if (requiresRequest) "01.09.2026" else "od razu", top = 8.dp)
            SheetLine("Umowa", "bez zmian, do 12.03.2027", top = 8.dp)
        }

        SolidButton(
            text = when {
                state.actionInFlight -> "Wysyłanie…"
                requiresRequest -> "Złóż wniosek o zmianę"
                else -> "Potwierdzam zmianę"
            },
            onClick = viewModel::confirmPlanChange,
            enabled = !state.actionInFlight,
            modifier = Modifier.padding(top = 18.dp),
        )
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
                .height(48.dp)
                .clickable(onClick = viewModel::closeSheet),
            contentAlignment = Alignment.Center,
        ) {
            Text("Anuluj", style = IntertellType.bodyBold, color = IntertellColors.Text50)
        }
    }
}

private fun sheetBody(requiresRequest: Boolean): String = if (requiresRequest) {
    "Wniosek trafi do Biura Obsługi Klienta. Zmiana wchodzi w życie od nowego okresu rozliczeniowego, po potwierdzeniu w LMS."
} else {
    "Zmiana działa od razu — prędkość zwiększymy zdalnie w ciągu kilku minut, bez wizyty technika."
}

@Composable
private fun SheetLine(label: String, value: String, top: Dp = 0.dp) {
    Row(modifier = Modifier.fillMaxWidth().padding(top = top), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, style = IntertellType.bodyBold, color = IntertellColors.TextPrimary)
        Text(value, style = IntertellType.bodyBold, color = IntertellColors.TextPrimary)
    }
}

@Composable
fun PlanChangeDoneOverlay(viewModel: ClientViewModel, state: ClientUiState) {
    val requiresRequest = !state.planChangeApplied
    FullScreenOutcome(
        title = if (requiresRequest) "Wniosek przyjęty" else "Pakiet zmieniony",
        body = when {
            state.planChangeLimited -> state.planChangeMessage
                ?: "Zmianę pakietu z aplikacji można wykonać raz w miesiącu. Ten wniosek trafił do BOK — zmiana zostanie dokonana po weryfikacji zgłoszenia."
            requiresRequest -> "Numer wniosku W-2026/1187. Status znajdziesz w zakładce Pakiet, potwierdzenie wyślemy e-mailem."
            else -> "Nowa prędkość jest już aktywna. Faktura zostanie skorygowana proporcjonalnie."
        },
        ctaText = "Wróć do pakietu",
        onCta = viewModel::closeDone,
    )
}
