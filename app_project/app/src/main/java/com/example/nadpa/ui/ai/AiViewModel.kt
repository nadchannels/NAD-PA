package com.example.nadpa.ui.ai

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nadpa.data.ApiClient
import com.example.nadpa.data.ChatMessage
import com.example.nadpa.data.ChatRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AiViewModel : ViewModel() {

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages

    private val _mode = MutableStateFlow("brainstorming")
    val mode: StateFlow<String> = _mode

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        // Greet the user on first open
        _messages.value = listOf(
            ChatMessage(
                role = "assistant",
                content = "As-salamu alaykum! I'm NAD PA — your personal assistant.\n\nI'm here to help you plan your week, refine your goals, and organize your thoughts.\n\nTell me what's on your mind, and when you're ready to schedule, just say \"schedule the plan\"."
            )
        )
    }

    fun sendMessage(text: String) {
        val userMsg = ChatMessage(role = "user", content = text)
        val currentHistory = _messages.value.toMutableList()
        currentHistory.add(userMsg)
        _messages.value = currentHistory

        _isLoading.value = true
        // Optimistically switch to scheduling mode if trigger detected
        val triggerPhrases = listOf("schedule the plan", "schedule this plan", "execute the plan", "add to schedule", "book it", "confirm the schedule", "lock it in", "finalize the schedule")
        if (triggerPhrases.any { text.lowercase().contains(it) }) {
            _mode.value = "execution"
        }

        viewModelScope.launch {
            try {
                val history = _messages.value
                    .dropLast(1) // exclude the message we just added
                    .map { ChatMessage(role = it.role, content = it.content) }

                val response = ApiClient.api.sendChatMessage(
                    ChatRequest(message = text, history = history)
                )

                val assistantMsg = ChatMessage(role = "assistant", content = response.reply)
                val updatedHistory = _messages.value.toMutableList()
                updatedHistory.add(assistantMsg)
                _messages.value = updatedHistory
                _mode.value = response.mode

            } catch (e: Exception) {
                val errorMsg = ChatMessage(
                    role = "assistant",
                    content = "⚠️ I'm having trouble connecting to the backend. Please ensure the server is running.\n\nError: ${e.localizedMessage}"
                )
                val updatedHistory = _messages.value.toMutableList()
                updatedHistory.add(errorMsg)
                _messages.value = updatedHistory
                _mode.value = "brainstorming"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
