package com.example.nadpa.data

// ─── Task / Session ───────────────────────────────────────────────────────────
data class Task(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val dayOfWeek: String = "Monday",
    val startTime: String = "09:00",
    val endTime: String = "10:00",
    val relativeWeekIndex: Int = 0,
    val status: String = "Pending",
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class TaskCreate(
    val title: String,
    val description: String = "",
    val dayOfWeek: String,
    val startTime: String,
    val endTime: String,
    val relativeWeekIndex: Int = 0,
    val status: String = "Pending"
)

// ─── Goal ─────────────────────────────────────────────────────────────────────
data class Goal(
    val id: String = "",
    val title: String = "",
    val type: String = "Short-Term",
    val status: String = "Pending",
    val completionPercentage: Int = 0,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

data class GoalCreate(
    val title: String,
    val type: String,
    val status: String = "Pending",
    val completionPercentage: Int = 0
)

data class GoalUpdate(
    val title: String? = null,
    val type: String? = null,
    val status: String? = null,
    val completionPercentage: Int? = null
)

// ─── Daily Inspiration ────────────────────────────────────────────────────────
data class Inspiration(
    val date: String = "",
    val ayahText: String = "",
    val ayahTranslation: String = "",
    val hadithText: String = "",
    val hadithTranslation: String = "",
    val aiCommentary: String = "",
    val fetchedAt: String? = null
)

// ─── AI Chat ──────────────────────────────────────────────────────────────────
data class ChatMessage(
    val role: String,  // "user" or "assistant"
    val content: String
)

data class ChatRequest(
    val message: String,
    val history: List<ChatMessage> = emptyList()
)

data class ChatResponse(
    val reply: String,
    val mode: String,  // "brainstorming" or "execution"
    val scheduled_items: Int? = null
)
