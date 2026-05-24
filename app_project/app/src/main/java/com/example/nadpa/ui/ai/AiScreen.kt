package com.example.nadpa.ui.ai

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nadpa.data.ChatMessage
import com.example.nadpa.theme.Black
import com.example.nadpa.theme.LightGray
import com.example.nadpa.theme.White

// ─── AI Chat Screen ───────────────────────────────────────────────────────────
@Composable
fun AiScreen(vm: AiViewModel = viewModel()) {
    val messages by vm.messages.collectAsState()
    val mode by vm.mode.collectAsState()
    val isLoading by vm.isLoading.collectAsState()
    var inputText by remember { mutableStateOf("") }
    val listState = rememberLazyListState()

    // Auto-scroll to bottom when new message arrives
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
    ) {
        // ── Status Indicator (Persistent Header) ──────────────────────────
        AiStatusBar(mode = mode)

        // ── Chat History ──────────────────────────────────────────────────
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 16.dp)
        ) {
            items(messages) { message ->
                ChatBubble(message = message)
            }
            if (isLoading) {
                item {
                    TypingIndicator()
                }
            }
        }

        // ── Input Field ───────────────────────────────────────────────────
        ChatInputBar(
            value = inputText,
            onValueChange = { inputText = it },
            onSend = {
                if (inputText.isNotBlank() && !isLoading) {
                    vm.sendMessage(inputText.trim())
                    inputText = ""
                }
            },
            isLoading = isLoading
        )
    }
}

@Composable
fun AiStatusBar(mode: String) {
    val isExecution = mode == "execution"
    val bgColor by animateColorAsState(
        targetValue = if (isExecution) Color(0xFF1A1A1A) else Black,
        animationSpec = tween(300),
        label = "status_bg"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bgColor)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = "NAD PA — AI ASSISTANT",
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp,
                color = Color(0xFF888888)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Microchip Interface",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = White
            )
        }

        // Status Pill
        val statusText = when (mode) {
            "execution" -> "SCHEDULING"
            else -> "BRAINSTORMING"
        }
        val pillColor = if (isExecution) Color(0xFF333333) else Color(0xFF222222)

        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(pillColor)
                .border(1.dp, if (isExecution) White else Color(0xFF444444), RoundedCornerShape(20.dp))
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(if (isExecution) White else Color(0xFF888888))
            )
            Text(
                text = statusText,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 1.sp,
                color = White
            )
        }
    }
}

@Composable
fun ChatBubble(message: ChatMessage) {
    val isUser = message.role == "user"

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
    ) {
        Box(
            modifier = Modifier
                .widthIn(max = 300.dp)
                .clip(
                    RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .background(if (isUser) White else Black)
                .border(
                    width = 1.dp,
                    color = if (isUser) Black else Color.Transparent,
                    shape = RoundedCornerShape(
                        topStart = 16.dp,
                        topEnd = 16.dp,
                        bottomStart = if (isUser) 16.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 16.dp
                    )
                )
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Text(
                text = message.content,
                fontSize = 14.sp,
                color = if (isUser) Black else White,
                lineHeight = 20.sp
            )
        }
    }
}

@Composable
fun TypingIndicator() {
    Row(
        horizontalArrangement = Arrangement.Start,
        modifier = Modifier.fillMaxWidth()
    ) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 4.dp))
                .background(Black)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                repeat(3) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(RoundedCornerShape(3.dp))
                            .background(Color(0xFF888888))
                    )
                }
            }
        }
    }
}

@Composable
fun ChatInputBar(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    isLoading: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(White)
            .border(1.dp, LightGray)
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .imePadding(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            placeholder = {
                Text(
                    text = "Message NAD PA...",
                    color = Color(0xFF888888),
                    fontSize = 14.sp
                )
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = Black,
                unfocusedBorderColor = LightGray,
                focusedTextColor = Black,
                cursorColor = Black
            ),
            shape = RoundedCornerShape(8.dp),
            maxLines = 4,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
            keyboardActions = KeyboardActions(onSend = { onSend() }),
            textStyle = LocalTextStyle.current.copy(fontSize = 14.sp)
        )

        IconButton(
            onClick = onSend,
            enabled = value.isNotBlank() && !isLoading,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(if (value.isNotBlank() && !isLoading) Black else Color(0xFFDDDDDD))
        ) {
            Icon(
                imageVector = Icons.Filled.Send,
                contentDescription = "Send",
                tint = White,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
