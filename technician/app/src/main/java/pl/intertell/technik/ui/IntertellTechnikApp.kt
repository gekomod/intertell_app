package pl.intertell.technik.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import pl.intertell.technik.TechScreen
import pl.intertell.technik.TechnicianViewModel
import pl.intertell.technik.ui.components.FullScreenOutcome
import pl.intertell.technik.ui.screens.AdminScreen
import pl.intertell.technik.ui.screens.CustomerScreen
import pl.intertell.technik.ui.screens.JobDetailScreen
import pl.intertell.technik.ui.screens.JobsScreen
import pl.intertell.technik.ui.screens.LoginScreen
import pl.intertell.technik.ui.screens.ReportScreen
import pl.intertell.technik.ui.screens.RouterScreen
import pl.intertell.technik.ui.screens.SearchScreen

@Composable
fun IntertellTechnikApp(viewModel: TechnicianViewModel) {
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
                TechScreen.LOGIN -> LoginScreen(viewModel, state)
                TechScreen.JOBS -> JobsScreen(viewModel)
                TechScreen.JOB -> JobDetailScreen(viewModel)
                TechScreen.REPORT -> ReportScreen(viewModel, state)
                TechScreen.SEARCH -> SearchScreen(viewModel, state)
                TechScreen.CUST -> CustomerScreen(viewModel)
                TechScreen.ROUTER -> RouterScreen(viewModel, state)
                TechScreen.ADMIN -> AdminScreen(viewModel, state)
            }

            if (state.jobDone) {
                FullScreenOutcome(
                    title = "Zlecenie zamknięte",
                    body = "Raport i pomiary wysłane do LMS. Klient dostał potwierdzenie w aplikacji.",
                    ctaText = "Następne zlecenie",
                    onCta = viewModel::closeJobDone,
                )
            }
        }
    }
}
