package pl.intertell.client.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import pl.intertell.client.ClientScreen
import pl.intertell.client.ClientViewModel
import pl.intertell.client.ui.components.FullScreenOutcome
import pl.intertell.client.ui.components.ScrimOverlay
import pl.intertell.client.ui.components.UpdateDialog
import pl.intertell.client.ui.components.UpdateProgressDialog
import pl.intertell.client.ui.screens.ContactScreen
import pl.intertell.client.ui.screens.ContractsScreen
import pl.intertell.client.ui.screens.DmzScreen
import pl.intertell.client.ui.screens.HomeScreen
import pl.intertell.client.ui.screens.InvoiceDetailScreen
import pl.intertell.client.ui.screens.InvoicesScreen
import pl.intertell.client.ui.screens.LoginScreen
import pl.intertell.client.ui.screens.PlanChangeDoneOverlay
import pl.intertell.client.ui.screens.PlanChangeSheet
import pl.intertell.client.ui.screens.PlanScreen
import pl.intertell.client.ui.screens.ResetConfirmSheet
import pl.intertell.client.ui.screens.ResetInProgressOverlay
import pl.intertell.client.ui.screens.SettingsScreen
import pl.intertell.client.ui.theme.IntertellColors
import pl.intertell.client.ui.theme.IntertellType

@Composable
fun IntertellApp(viewModel: ClientViewModel) {
    val state by viewModel.uiState.collectAsState()
    val update by viewModel.updateAvailable.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()

    update?.let {
        UpdateDialog(update = it, onDismiss = viewModel::dismissUpdate, onInstall = viewModel::startUpdateDownload)
    }
    downloadProgress?.let {
        UpdateProgressDialog(progress = it)
    }

    Scaffold(
        bottomBar = {
            if (state.showBottomBar) {
                BottomTabBar(screen = state.screen, viewModel = viewModel)
            }
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize(),
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                state.errorMessage?.let { error ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(IntertellColors.Danger)
                            .clickable(onClick = viewModel::clearError)
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(error, style = IntertellType.bodySmall, color = IntertellColors.White, modifier = Modifier.weight(1f))
                        Text("✕", style = IntertellType.bodyBold, color = IntertellColors.White)
                    }
                }
                Box(modifier = Modifier.fillMaxSize()) {
                    when (state.screen) {
                        ClientScreen.LOGIN -> LoginScreen(viewModel, state)
                        ClientScreen.HOME -> HomeScreen(viewModel, state)
                        ClientScreen.INVOICES -> InvoicesScreen(viewModel, state)
                        ClientScreen.INVOICE -> InvoiceDetailScreen(viewModel, state)
                        ClientScreen.CONTRACTS -> ContractsScreen(viewModel)
                        ClientScreen.PLAN -> PlanScreen(viewModel, state)
                        ClientScreen.SETTINGS -> SettingsScreen(viewModel, state)
                        ClientScreen.DMZ -> DmzScreen(viewModel, state)
                        ClientScreen.CONTACT -> ContactScreen(viewModel, state)
                    }
                }
            }

            if (state.showPlanSheet) {
                ScrimOverlay(onDismiss = viewModel::closeSheet) {
                    PlanChangeSheet(viewModel, state)
                }
            }
            if (state.planChangeDone) {
                PlanChangeDoneOverlay(viewModel, state)
            }
            if (state.resetSheetOpen && !state.resetInProgress) {
                ScrimOverlay(onDismiss = viewModel::closeReset) {
                    ResetConfirmSheet(viewModel)
                }
            }
            if (state.resetInProgress) {
                ResetInProgressOverlay(viewModel)
            }
            if (state.ticketSent) {
                FullScreenOutcome(
                    title = "Zgłoszenie wysłane",
                    body = "Zgłoszenie trafiło do kolejki serwisowej. Technik skontaktuje się w razie potrzeby dodatkowych informacji.",
                    ctaText = "Wróć",
                    onCta = viewModel::closeTicketSent,
                )
            }
        }
    }
}
