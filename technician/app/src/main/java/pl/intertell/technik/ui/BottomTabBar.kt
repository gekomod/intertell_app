package pl.intertell.technik.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
            .background(IntertellColors.White.copy(alpha = 0.94f))
            .padding(top = 10.dp, bottom = 24.dp, start = 8.dp, end = 8.dp),
    ) {
        TabItem("Zlecenia", screen in jobsGroup, Modifier.weight(1f), onClick = viewModel::goJobs)
        TabItem("Klienci", screen in searchGroup, Modifier.weight(1f), onClick = viewModel::goSearch)
        TabItem("Zespół", screen == TechScreen.ADMIN, Modifier.weight(1f), onClick = viewModel::goAdmin)
        TabItem("Wyloguj", false, Modifier.weight(1f), onClick = viewModel::logout)
    }
}

@Composable
private fun TabItem(label: String, active: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val color = if (active) IntertellColors.Green else IntertellColors.ToggleTrackOff
    Column(
        modifier = modifier.clickable(onClick = onClick),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier
                .size(18.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(color),
        )
        Text(
            label,
            style = IntertellType.chip,
            color = color,
            modifier = Modifier.padding(top = 6.dp),
        )
    }
}
