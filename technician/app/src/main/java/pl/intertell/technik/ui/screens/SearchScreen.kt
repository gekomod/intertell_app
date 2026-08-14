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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import pl.intertell.technik.TechnicianViewModel
import pl.intertell.technik.data.Customer
import pl.intertell.technik.ui.components.Card
import pl.intertell.technik.ui.theme.IntertellColors
import pl.intertell.technik.ui.theme.IntertellType

@Composable
fun SearchScreen(viewModel: TechnicianViewModel) {
    val state by viewModel.uiState.collectAsState()
    val result by viewModel.searchResult.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 20.dp, bottom = 20.dp),
    ) {
        Text("Klienci", style = IntertellType.display, color = IntertellColors.TextPrimary)
        Text(
            "Szukaj po adresie, nazwisku, numerze klienta lub numerze seryjnym urządzenia — baza intratell, wzbogacona danymi z LMS gdy klient jest połączony.",
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
            if (state.searchLoading) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = IntertellColors.Green, strokeWidth = 2.dp)
            } else {
                Text("${result.customers.size} wyniki", style = IntertellType.chip, color = IntertellColors.Green)
            }
        }

        if (state.errorMessage != null) {
            Text(state.errorMessage, style = IntertellType.bodySmall, color = IntertellColors.Danger, modifier = Modifier.padding(top = 10.dp))
        }

        Column(modifier = Modifier.padding(top = 16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            result.customers.forEach { customer ->
                val (fg, bg, dot) = customerColors(customer)
                Card(radius = 16, padding = 15, modifier = Modifier.clickable { viewModel.openCustomer(customer) }) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                        Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(dot))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(customer.address.ifBlank { customer.name }, style = IntertellType.titleBold, color = IntertellColors.TextPrimary)
                            Text(
                                customer.name,
                                style = IntertellType.body,
                                color = IntertellColors.Text6,
                                modifier = Modifier.padding(top = 3.dp),
                            )
                            Text(
                                "${customer.customerNo}" + if (customer.devices.isNotEmpty()) " · ${customer.devices.first().model}" else "",
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
                            Text(customer.statusLabel.uppercase(), style = IntertellType.monoSmall, color = fg)
                        }
                    }
                }
            }
        }

        if (result.lmsOnly.isNotEmpty()) {
            Text(
                "Znalezieni tylko w LMS (brak lokalnej karty)",
                style = IntertellType.bodyBold,
                color = IntertellColors.TextPrimary,
                modifier = Modifier.padding(top = 22.dp, bottom = 10.dp),
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(IntertellColors.White),
            ) {
                result.lmsOnly.forEach { match ->
                    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 13.dp)) {
                        Text(match.name, style = IntertellType.bodyBold, color = IntertellColors.TextPrimary)
                    }
                }
            }
        }
    }
}

private fun customerColors(customer: Customer): Triple<Color, Color, Color> = when (customer.status) {
    "past_due" -> Triple(IntertellColors.Amber, IntertellColors.AmberChipBg, IntertellColors.Amber)
    "suspended" -> Triple(IntertellColors.Danger, IntertellColors.DangerChipBg, IntertellColors.Danger)
    else -> Triple(IntertellColors.Green, IntertellColors.GreenChipBg, IntertellColors.Green)
}
