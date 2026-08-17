package pl.intertell.technik.ui

import androidx.activity.compose.BackHandler
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
import pl.intertell.technik.ui.components.UpdateDialog
import pl.intertell.technik.ui.components.UpdateProgressDialog
import pl.intertell.technik.ui.screens.AdminScreen
import pl.intertell.technik.ui.screens.CustomerScreen
import pl.intertell.technik.ui.screens.JobDetailScreen
import pl.intertell.technik.ui.screens.JobsScreen
import pl.intertell.technik.ui.screens.LoginScreen
import pl.intertell.technik.ui.screens.QgisScreen
import pl.intertell.technik.ui.screens.ReportScreen
import pl.intertell.technik.ui.screens.RouterScreen
import pl.intertell.technik.ui.screens.SearchScreen
import pl.intertell.technik.ui.screens.SettingsScreen

// System back button/gesture exits the app by default on any screen (there's
// no Fragment/Navigation-Compose backstack here, just a flat `screen`
// field) — these are the only screens where that default is actually
// correct. Everywhere else the BackHandler below steps back one level
// instead, mirroring each screen's own on-screen "←" link.
private val rootScreens = setOf(TechScreen.LOGIN, TechScreen.JOBS, TechScreen.SETTINGS)

@Composable
fun IntertellTechnikApp(viewModel: TechnicianViewModel) {
    val state by viewModel.uiState.collectAsState()
    val update by viewModel.updateAvailable.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()

    BackHandler(enabled = state.jobDone || state.screen !in rootScreens) {
        when {
            state.jobDone -> viewModel.closeJobDone()
            else -> when (state.screen) {
                TechScreen.JOB -> viewModel.goJobs()
                TechScreen.REPORT -> viewModel.goJob()
                TechScreen.CUST -> viewModel.goSearch()
                TechScreen.ROUTER -> viewModel.goCust()
                TechScreen.SEARCH, TechScreen.QGIS, TechScreen.ADMIN -> viewModel.goJobs()
                else -> Unit // LOGIN/JOBS — excluded via `enabled` above, unreachable
            }
        }
    }

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
            when (state.screen) {
                TechScreen.LOGIN -> LoginScreen(viewModel, state)
                TechScreen.JOBS -> JobsScreen(viewModel)
                TechScreen.JOB -> JobDetailScreen(viewModel)
                TechScreen.REPORT -> ReportScreen(viewModel)
                TechScreen.SEARCH -> SearchScreen(viewModel)
                TechScreen.CUST -> CustomerScreen(viewModel)
                TechScreen.ROUTER -> RouterScreen(viewModel)
                TechScreen.ADMIN -> AdminScreen(viewModel)
                TechScreen.QGIS -> QgisScreen(viewModel)
                TechScreen.SETTINGS -> SettingsScreen(viewModel, state)
            }

            if (state.jobDone) {
                FullScreenOutcome(
                    title = "Zgłoszenie zakończone",
                    body = "Status zapisany w bazie intratell.",
                    ctaText = "Następne zlecenie",
                    onCta = viewModel::closeJobDone,
                )
            }
        }
    }
}
