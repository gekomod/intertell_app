package pl.intertell.client.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import pl.intertell.client.ClientScreen
import pl.intertell.client.ClientViewModel
import pl.intertell.client.ui.components.ScrimOverlay
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

@Composable
fun IntertellApp(viewModel: ClientViewModel) {
    val state by viewModel.uiState.collectAsState()

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
            when (state.screen) {
                ClientScreen.LOGIN -> LoginScreen(viewModel, state)
                ClientScreen.HOME -> HomeScreen(viewModel)
                ClientScreen.INVOICES -> InvoicesScreen(viewModel)
                ClientScreen.INVOICE -> InvoiceDetailScreen(viewModel)
                ClientScreen.CONTRACTS -> ContractsScreen(viewModel)
                ClientScreen.PLAN -> PlanScreen(viewModel, state)
                ClientScreen.SETTINGS -> SettingsScreen(viewModel, state)
                ClientScreen.DMZ -> DmzScreen(viewModel, state)
                ClientScreen.CONTACT -> ContactScreen(viewModel)
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
        }
    }
}
