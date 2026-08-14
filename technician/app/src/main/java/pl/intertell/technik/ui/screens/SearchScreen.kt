package pl.intertell.technik.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import pl.intertell.technik.CustomerFilter
import pl.intertell.technik.TechnicianUiState
import pl.intertell.technik.TechnicianViewModel
import pl.intertell.technik.data.CustomerState
import pl.intertell.technik.ui.components.Card
import pl.intertell.technik.ui.theme.IntertellColors
import pl.intertell.technik.ui.theme.IntertellType

@Composable
fun SearchScreen(viewModel: TechnicianViewModel, state: TechnicianUiState) {
    val results = viewModel.filteredCustomers()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp, bottom = 20.dp),
    ) {
        Text("Klienci", style = IntertellType.display, color = IntertellColors.TextPrimary)
        Text(
            "Szukaj po adresie, nazwisku, numerze umowy lub SN urządzenia. Dostęp do ONT tylko dla techników na służbie, każde wejście jest logowane.",
            style = IntertellType.bodySmall,
            color = IntertellColors.Text55,
            modifier = Modifier.padding(top = 6.dp),
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(IntertellColors.White)
                .padding(horizontal = 16.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("Szukaj", style = IntertellType.label, color = IntertellColors.Text5)
                BasicTextField(
                    value = state.searchQuery,
                    onValueChange = viewModel::setSearchQuery,
                    singleLine = true,
                    textStyle = IntertellType.monoBold.copy(color = IntertellColors.TextPrimary),
                    modifier = Modifier.fillMaxWidth().padding(top = 3.dp),
                    decorationBox = { inner ->
                        if (state.searchQuery.isEmpty()) {
                            Text("np. ul. Wrocławska", style = IntertellType.monoBold, color = IntertellColors.Text45)
                        }
                        inner()
                    },
                )
            }
            Text("${results.size} wyniki", style = IntertellType.chip, color = IntertellColors.Green)
        }

        Row(modifier = Modifier.fillMaxWidth().padding(top = 12.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip("Wszyscy", state.searchFilter == CustomerFilter.ALL, IntertellColors.Navy, IntertellColors.White) {
                viewModel.setSearchFilter(CustomerFilter.ALL)
            }
            val awariaCount = viewModel.customers.count { it.state == CustomerState.AWARIA }
            FilterChip(
                "Awarie ($awariaCount)",
                state.searchFilter == CustomerFilter.AWARIA,
                IntertellColors.DangerChipBg,
                IntertellColors.Danger,
                onClick = { viewModel.setSearchFilter(CustomerFilter.AWARIA) },
            )
        }

        Column(modifier = Modifier.padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            results.forEach { (index, customer) ->
                val (fg, bg, dot) = customerColors(customer.state)
                Card(radius = 16, padding = 15, modifier = Modifier.clickable { viewModel.openCustomer(index) }) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(dot))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(customer.address, style = IntertellType.titleBold, color = IntertellColors.TextPrimary)
                            Text(
                                "${customer.name} · ${customer.plan}",
                                style = IntertellType.body,
                                color = IntertellColors.Text6,
                                modifier = Modifier.padding(top = 3.dp),
                            )
                            Text(
                                "${customer.contract} · ${customer.ont}",
                                style = IntertellType.monoSmall,
                                color = IntertellColors.Text45,
                                modifier = Modifier.padding(top = 4.dp),
                            )
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(5.dp))
                                .background(bg)
                                .padding(horizontal = 7.dp, vertical = 3.dp),
                        ) {
                            Text(customer.state.name, style = IntertellType.monoSmall, color = fg)
                        }
                    }
                }
            }
        }
    }
}

private fun customerColors(state: CustomerState): Triple<Color, Color, Color> = when (state) {
    CustomerState.AWARIA -> Triple(IntertellColors.Danger, IntertellColors.DangerChipBg, IntertellColors.Danger)
    CustomerState.ZAWIESZONA -> Triple(IntertellColors.Amber, IntertellColors.AmberChipBg, IntertellColors.Amber)
    CustomerState.OK -> Triple(IntertellColors.Green, IntertellColors.GreenChipBg, IntertellColors.Green)
}

@Composable
private fun FilterChip(label: String, active: Boolean, activeBg: Color, activeFg: Color, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(9.dp))
            .background(if (active) activeBg else IntertellColors.HairlineOnLightFaint)
            .clickable(onClick = onClick)
            .padding(horizontal = 11.dp, vertical = 7.dp),
    ) {
        Text(label, style = IntertellType.chip, color = if (active) activeFg else IntertellColors.Text55)
    }
}
