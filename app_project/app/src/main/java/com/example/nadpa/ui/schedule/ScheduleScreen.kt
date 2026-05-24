package com.example.nadpa.ui.schedule

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.nadpa.data.Task
import com.example.nadpa.theme.Black
import com.example.nadpa.theme.LightGray
import com.example.nadpa.theme.White

val DAYS = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
val DAY_FULL = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
val HOURS = (0..23).map { h -> "%02d:00".format(h) }
val CELL_HEIGHT = 48.dp
val DAY_COL_WIDTH = 80.dp
val TIME_COL_WIDTH = 52.dp

// ─── Schedule Screen ──────────────────────────────────────────────────────────
@Composable
fun ScheduleScreen(vm: ScheduleViewModel = viewModel()) {
    val weekIndex by vm.weekIndex.collectAsState()
    val tasks by vm.tasks.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
    ) {
        // ── Week Navigator Header ─────────────────────────────────────────
        WeekNavigatorHeader(
            weekIndex = weekIndex,
            onPrev = { vm.navigateWeek(-1) },
            onNext = { vm.navigateWeek(1) }
        )

        // ── Timetable Grid ────────────────────────────────────────────────
        TimetableGrid(tasks = tasks)
    }
}

@Composable
fun WeekNavigatorHeader(weekIndex: Int, onPrev: () -> Unit, onNext: () -> Unit) {
    val canGoBack = weekIndex > -100
    val canGoForward = weekIndex < 52

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Black)
            .padding(top = 24.dp, bottom = 16.dp, start = 20.dp, end = 20.dp)
    ) {
        Text(
            text = "SCHEDULE",
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 3.sp,
            color = Color(0xFF888888)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(
                onClick = onPrev,
                enabled = canGoBack,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Filled.ChevronLeft,
                    contentDescription = "Previous Week",
                    tint = if (canGoBack) White else Color(0xFF444444)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = when {
                        weekIndex == 0 -> "WEEK 0"
                        weekIndex > 0 -> "WEEK +$weekIndex"
                        else -> "WEEK $weekIndex"
                    },
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    color = White,
                    letterSpacing = (-1).sp
                )
                Text(
                    text = when {
                        weekIndex == 0 -> "CURRENT WEEK"
                        weekIndex > 0 -> "$weekIndex WEEK${if (weekIndex == 1) "" else "S"} AHEAD"
                        else -> "${-weekIndex} WEEK${if (weekIndex == -1) "" else "S"} AGO"
                    },
                    fontSize = 10.sp,
                    color = Color(0xFF888888),
                    letterSpacing = 2.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            IconButton(
                onClick = onNext,
                enabled = canGoForward,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    Icons.Filled.ChevronRight,
                    contentDescription = "Next Week",
                    tint = if (canGoForward) White else Color(0xFF444444)
                )
            }
        }
    }
}

@Composable
fun TimetableGrid(tasks: List<Task>) {
    val verticalScroll = rememberScrollState()
    val horizontalScroll = rememberScrollState()

    Box(modifier = Modifier.fillMaxSize()) {
        // Scrollable content
        Column(modifier = Modifier.fillMaxSize()) {
            // Day headers row (sticky + scrollable horizontally)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(White)
                    .border(1.dp, LightGray)
            ) {
                // Empty corner cell
                Box(
                    modifier = Modifier
                        .width(TIME_COL_WIDTH)
                        .height(40.dp)
                        .background(Black),
                    contentAlignment = Alignment.Center
                ) {
                    Text("TIME", fontSize = 8.sp, color = Color(0xFF888888), fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }

                // Day header cells — scrollable
                Row(modifier = Modifier.horizontalScroll(horizontalScroll)) {
                    DAYS.forEach { day ->
                        Box(
                            modifier = Modifier
                                .width(DAY_COL_WIDTH)
                                .height(40.dp)
                                .background(Black)
                                .border(1.dp, Color(0xFF333333)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = day.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = White,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }

            // Time rows + task cells
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(verticalScroll)
            ) {
                // Time column (fixed)
                Column(modifier = Modifier.width(TIME_COL_WIDTH)) {
                    HOURS.forEach { hour ->
                        Box(
                            modifier = Modifier
                                .width(TIME_COL_WIDTH)
                                .height(CELL_HEIGHT)
                                .background(White)
                                .border(0.5.dp, LightGray),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Text(
                                text = hour,
                                fontSize = 10.sp,
                                color = Color(0xFF888888),
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                    }
                }

                // Day columns (scrollable horizontally)
                Row(modifier = Modifier.horizontalScroll(horizontalScroll)) {
                    DAYS.forEachIndexed { dayIndex, _ ->
                        val dayName = DAY_FULL[dayIndex]
                        Column(modifier = Modifier.width(DAY_COL_WIDTH)) {
                            HOURS.forEachIndexed { hourIndex, _ ->
                                val hourTasks = tasks.filter { task ->
                                    task.dayOfWeek == dayName &&
                                    taskCoversHour(task, hourIndex)
                                }

                                Box(
                                    modifier = Modifier
                                        .width(DAY_COL_WIDTH)
                                        .height(CELL_HEIGHT)
                                        .border(0.5.dp, LightGray)
                                        .background(if (hourTasks.isNotEmpty()) Black else White)
                                        .padding(2.dp),
                                    contentAlignment = Alignment.TopStart
                                ) {
                                    if (hourTasks.isNotEmpty()) {
                                        hourTasks.take(2).forEachIndexed { i, task ->
                                            if (isTaskStart(task, hourIndex)) {
                                                Text(
                                                    text = task.title,
                                                    fontSize = 9.sp,
                                                    color = White,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 2,
                                                    overflow = TextOverflow.Ellipsis,
                                                    lineHeight = 12.sp,
                                                    modifier = Modifier.padding(top = (i * 12).dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun taskCoversHour(task: Task, hour: Int): Boolean {
    return try {
        val startH = task.startTime.substringBefore(":").toInt()
        val endH = task.endTime.substringBefore(":").toInt()
        hour in startH until endH
    } catch (e: Exception) { false }
}

fun isTaskStart(task: Task, hour: Int): Boolean {
    return try {
        task.startTime.substringBefore(":").toInt() == hour
    } catch (e: Exception) { false }
}
