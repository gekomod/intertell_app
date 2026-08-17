package pl.intertell.technik.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assignment
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import pl.intertell.technik.TechScreen
import pl.intertell.technik.TechnicianViewModel
import pl.intertell.technik.ui.theme.IntertellColors
import pl.intertell.technik.ui.theme.IntertellType

private val jobsGroup = setOf(TechScreen.JOBS, TechScreen.JOB, TechScreen.REPORT)
private val searchGroup = setOf(TechScreen.SEARCH, TechScreen.CUST, TechScreen.ROUTER)

@Composable
fun BottomTabBar(screen: TechScreen, viewModel: TechnicianViewModel) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(IntertellColors.Surface.copy(alpha = 0.94f))
            .padding(top = 10.dp, bottom = 24.dp, start = 8.dp, end = 8.dp),
    ) {
        TabItem("Zlecenia", Icons.Default.Assignment, screen in jobsGroup, Modifier.weight(1f), onClick = viewModel::goJobs)
        TabItem("Klienci", Icons.Default.People, screen in searchGroup, Modifier.weight(1f), onClick = viewModel::goSearch)
        TabItem("QGIS", Icons.Default.Map, screen == TechScreen.QGIS, Modifier.weight(1f), onClick = viewModel::goQgis)
        TabItem("Zespół", Icons.Default.Groups, screen == TechScreen.ADMIN, Modifier.weight(1f), onClick = viewModel::goAdmin)
        TabItem("Ustawienia", Icons.Default.Settings, screen == TechScreen.SETTINGS, Modifier.weight(1f), onClick = viewModel::goSettings)
    }
}

@Composable
private fun TabItem(label: String, icon: ImageVector, active: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val color = if (active) IntertellColors.Green else IntertellColors.ToggleTrackOff
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(icon, contentDescription = label, tint = color, modifier = Modifier)
        Text(
            label,
            style = IntertellType.chip,
            color = color,
            modifier = Modifier.padding(top = 4.dp),
        )
    }
}
