package pl.intertell.client.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import pl.intertell.client.ClientUiState
import pl.intertell.client.ClientViewModel
import pl.intertell.client.data.ChatMessage
import pl.intertell.client.ui.components.BackLink
import pl.intertell.client.ui.components.LabeledTextField
import pl.intertell.client.ui.components.SolidButton
import pl.intertell.client.ui.theme.IntertellColors
import pl.intertell.client.ui.theme.IntertellType

@Composable
fun ChatScreen(viewModel: ClientViewModel, state: ClientUiState) {
    val messages by viewModel.chatMessages.collectAsState()
    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    val itemCount = messages.size + (if (messages.isEmpty()) 1 else 0) + (if (state.chatSending) 1 else 0)
    LaunchedEffect(itemCount) {
        if (itemCount > 0) listState.animateScrollToItem(itemCount - 1)
    }

    fun send() {
        val text = input.trim()
        if (text.isEmpty()) return
        viewModel.sendChatMessage(text)
        input = ""
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 20.dp, bottom = 10.dp),
        ) {
            BackLink("← Kontakt z operatorem", onClick = viewModel::goContact)
            Text("Czat z BOK", style = IntertellType.headline, color = IntertellColors.TextPrimary, modifier = Modifier.padding(top = 14.dp))
            Text(
                "Asystent AI sprawdzi saldo, stan łącza i sygnał ONT. Nie zmienia ustawień konta ani nie restartuje urządzeń — takie sprawy zgłoś przez „Zgłoś problem”.",
                style = IntertellType.bodySmall,
                color = IntertellColors.Text50,
                modifier = Modifier.padding(top = 4.dp),
            )
        }

        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            if (messages.isEmpty()) {
                item {
                    ChatBubble(ChatMessage("assistant", "Cześć! Mogę sprawdzić saldo, stan łącza i sygnał ONT — o co chcesz zapytać?"))
                }
            }
            items(messages) { msg -> ChatBubble(msg) }
            if (state.chatSending) {
                item { ChatBubble(ChatMessage("assistant", "Asystent pisze…"), muted = true) }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 14.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.Bottom,
        ) {
            LabeledTextField(
                label = "Wiadomość",
                value = input,
                onValueChange = { input = it },
                modifier = Modifier.weight(1f),
            )
            SolidButton(
                "Wyślij",
                onClick = ::send,
                enabled = !state.chatSending && input.isNotBlank(),
                modifier = Modifier.width(96.dp),
                height = 54,
            )
        }
    }
}

@Composable
private fun ChatBubble(msg: ChatMessage, muted: Boolean = false) {
    val isUser = msg.role == "user"
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(if (isUser) IntertellColors.Accent else IntertellColors.White)
                .padding(horizontal = 14.dp, vertical = 10.dp),
        ) {
            Text(
                msg.content,
                style = IntertellType.body,
                color = when {
                    isUser -> IntertellColors.White
                    muted -> IntertellColors.Text50
                    else -> IntertellColors.TextPrimary
                },
            )
        }
    }
}
