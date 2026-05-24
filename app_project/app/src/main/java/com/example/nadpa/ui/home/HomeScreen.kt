package com.example.nadpa.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nadpa.data.Inspiration
import com.example.nadpa.data.Task
import com.example.nadpa.theme.Black
import com.example.nadpa.theme.CharcoalBlack
import com.example.nadpa.theme.LightGray
import com.example.nadpa.theme.White
import kotlinx.coroutines.delay
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

// ─── Home Screen ─────────────────────────────────────────────────────────────
@Composable
fun HomeScreen(vm: HomeViewModel = viewModel()) {
    val scrollState = rememberScrollState()
    val inspiration by vm.inspiration.collectAsState()
    val currentTask by vm.currentTask.collectAsState()
    val upcomingTask by vm.upcomingTask.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // ── Header: Date & Clock ──────────────────────────────────────────
        HeaderSection()

        HorizontalDivider(color = Black, thickness = 2.dp)

        // ── Session Tracker ───────────────────────────────────────────────
        SessionSection(currentTask = currentTask, upcomingTask = upcomingTask)

        HorizontalDivider(color = LightGray, thickness = 1.dp)

        // ── Daily Inspiration ─────────────────────────────────────────────
        InspirationSection(inspiration = inspiration)

        HorizontalDivider(color = LightGray, thickness = 1.dp)

        // ── Assistant Note ────────────────────────────────────────────────
        AssistantNoteSection(commentary = inspiration?.aiCommentary)
    }
}

@Composable
fun HeaderSection() {
    var time by remember { mutableStateOf(LocalTime.now()) }
    val date = LocalDate.now()

    LaunchedEffect(Unit) {
        while (true) {
            time = LocalTime.now()
            delay(1000L)
        }
    }

    Column {
        Text(
            text = date.format(DateTimeFormatter.ofPattern("EEEE, MMMM d yyyy")).uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF666666),
            letterSpacing = 2.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = time.format(DateTimeFormatter.ofPattern("HH:mm:ss")),
            fontSize = 48.sp,
            fontWeight = FontWeight.Black,
            color = Black,
            letterSpacing = (-2).sp,
            lineHeight = 50.sp
        )
        Text(
            text = "NAD PA",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF666666),
            letterSpacing = 4.sp
        )
    }
}

@Composable
fun SessionSection(currentTask: Task?, upcomingTask: Task?) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "SESSIONS",
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 3.sp,
            color = Color(0xFF666666)
        )

        SessionCard(
            label = "CURRENT",
            task = currentTask,
            isActive = true
        )

        SessionCard(
            label = "UPCOMING",
            task = upcomingTask,
            isActive = false
        )
    }
}

@Composable
fun SessionCard(label: String, task: Task?, isActive: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(if (isActive) Black else White)
            .border(1.dp, Black, RoundedCornerShape(4.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontSize = 9.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp,
                color = if (isActive) Color(0xFFAAAAAA) else Color(0xFF888888)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = task?.title ?: "— Nothing scheduled —",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = if (isActive) White else Black,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (task != null) {
            Spacer(modifier = Modifier.width(12.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = task.startTime,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = if (isActive) White else Black
                )
                Text(
                    text = "→ ${task.endTime}",
                    fontSize = 11.sp,
                    color = if (isActive) Color(0xFFAAAAAA) else Color(0xFF666666)
                )
            }
        }
    }
}

@Composable
fun InspirationSection(inspiration: Inspiration?) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = "DAILY INSPIRATION",
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 3.sp,
            color = Color(0xFF666666)
        )

        InspirationCard(
            title = "AYAH OF THE DAY",
            arabicText = inspiration?.ayahText ?: "Loading Ayah...",
            translationText = inspiration?.ayahTranslation ?: ""
        )

        InspirationCard(
            title = "HADITH OF THE DAY",
            arabicText = inspiration?.hadithText ?: "Loading Hadith...",
            translationText = inspiration?.hadithTranslation ?: ""
        )
    }
}

@Composable
fun InspirationCard(title: String, arabicText: String, translationText: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .border(1.dp, LightGray, RoundedCornerShape(4.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = title,
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 2.sp,
            color = Color(0xFF888888)
        )
        if (arabicText.isNotBlank()) {
            Text(
                text = arabicText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Black,
                lineHeight = 28.sp
            )
        }
        if (translationText.isNotBlank()) {
            Text(
                text = translationText,
                fontSize = 13.sp,
                color = Color(0xFF444444),
                lineHeight = 20.sp,
                fontWeight = FontWeight.Normal
            )
        }
    }
}

@Composable
fun AssistantNoteSection(commentary: String?) {
    if (commentary.isNullOrBlank()) return

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(Black)
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Text(
            text = "ASSISTANT NOTE",
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 2.sp,
            color = Color(0xFF888888)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = commentary,
            fontSize = 14.sp,
            color = White,
            lineHeight = 22.sp,
            fontWeight = FontWeight.Normal
        )
    }
}
