package pl.intertell.client.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import pl.intertell.client.ClientUiState
import pl.intertell.client.ClientViewModel
import pl.intertell.client.R
import pl.intertell.client.ui.components.LabeledTextField
import pl.intertell.client.ui.components.SolidButton
import pl.intertell.client.ui.theme.IntertellColors
import pl.intertell.client.ui.theme.IntertellType

@Composable
fun LoginScreen(viewModel: ClientViewModel, state: ClientUiState) {
    var contractOrEmail by remember { mutableStateOf("88214/OST") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 28.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Image(
            painter = painterResource(R.drawable.logo_intertell),
            contentDescription = "Intertell",
            contentScale = ContentScale.Fit,
            modifier = Modifier
                .width(212.dp)
                .align(Alignment.CenterHorizontally),
        )
        Text(
            "Twoje konto abonenckie",
            style = IntertellType.display,
            color = IntertellColors.TextPrimary,
            modifier = Modifier
                .padding(top = 30.dp)
                .align(Alignment.CenterHorizontally),
        )
        Text(
            "Internet światłowodowy — faktury, umowy i pakiet w jednym miejscu.",
            style = IntertellType.body,
            color = IntertellColors.Text55,
            modifier = Modifier
                .padding(top = 8.dp, bottom = 30.dp)
                .align(Alignment.CenterHorizontally),
        )

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            LabeledTextField(
                label = "Numer umowy lub e-mail",
                value = contractOrEmail,
                onValueChange = { contractOrEmail = it },
            )
            LabeledTextField(
                label = "Hasło",
                value = password,
                onValueChange = { password = it },
                isPassword = true,
            )
        }

        if (state.loginError != null) {
            Text(
                state.loginError,
                style = IntertellType.bodySmall,
                color = IntertellColors.Danger,
                modifier = Modifier.padding(top = 10.dp),
            )
        }

        SolidButton(
            text = if (state.loginLoading) "Logowanie…" else "Zaloguj przez LMS",
            onClick = { viewModel.login(contractOrEmail, password) },
            enabled = !state.loginLoading,
            modifier = Modifier.padding(top = 22.dp),
        )
        if (state.loginLoading) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator(modifier = Modifier.width(18.dp), color = IntertellColors.Accent, strokeWidth = 2.dp)
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Text("Nie pamiętam hasła", style = IntertellType.bodyBold, color = IntertellColors.Text55)
            Text("Aktywuj konto", style = IntertellType.bodyBold, color = IntertellColors.Text55)
        }

        Text(
            "Uwierzytelnianie i dane abonenta pochodzą z systemu LMS. Aplikacja nie przechowuje haseł.",
            style = IntertellType.monoFootnote,
            color = IntertellColors.Text42,
            modifier = Modifier.padding(top = 40.dp),
        )
    }
}
