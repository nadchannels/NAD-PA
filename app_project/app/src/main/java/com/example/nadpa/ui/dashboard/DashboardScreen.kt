package com.example.nadpa.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.nadpa.data.Goal
import com.example.nadpa.theme.Black
import com.example.nadpa.theme.LightGray
import com.example.nadpa.theme.White

// ─── Dashboard Screen ─────────────────────────────────────────────────────────
@Composable
fun DashboardScreen(vm: DashboardViewModel = viewModel()) {
    val goals by vm.goals.collectAsState()
    val scrollState = rememberScrollState()

    val achieved = goals.filter { it.status == "Achieved" }
    val pending = goals.filter { it.status == "Pending" }
    val shortTerm = pending.filter { it.type == "Short-Term" }
    val longTerm = pending.filter { it.type == "Long-Term" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(White)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Header
        Column {
            Text(
                text = "DASHBOARD",
                fontSize = 10.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 3.sp,
                color = Color(0xFF666666)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Goals",
                fontSize = 36.sp,
                fontWeight = FontWeight.Black,
                color = Black,
                letterSpacing = (-1).sp
            )
        }

        // Overall Progress
        if (goals.isNotEmpty()) {
            val avgCompletion = goals.map { it.completionPercentage }.average().toInt()
            ProgressSummaryCard(totalGoals = goals.size, avgCompletion = avgCompletion, achieved = achieved.size)
        }

        HorizontalDivider(color = Black, thickness = 2.dp)

        // Current Goals — Short Term
        if (shortTerm.isNotEmpty()) {
            GoalSection(
                title = "SHORT-TERM GOALS",
                goals = shortTerm,
                showProgress = true,
                strikethrough = false
            )
        }

        // Current Goals — Long Term
        if (longTerm.isNotEmpty()) {
            GoalSection(
                title = "LONG-TERM GOALS",
                goals = longTerm,
                showProgress = true,
                strikethrough = false
            )
        }

        HorizontalDivider(color = LightGray, thickness = 1.dp)

        // Achieved Goals
        if (achieved.isNotEmpty()) {
            GoalSection(
                title = "ACHIEVED",
                goals = achieved,
                showProgress = false,
                strikethrough = true
            )
        }

        if (goals.isEmpty()) {
            EmptyGoalsPlaceholder()
        }
    }
}

@Composable
fun ProgressSummaryCard(totalGoals: Int, avgCompletion: Int, achieved: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(Black)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        StatBlock(value = totalGoals.toString(), label = "TOTAL", light = true)
        StatBlock(value = "$avgCompletion%", label = "AVG PROGRESS", light = true)
        StatBlock(value = achieved.toString(), label = "ACHIEVED", light = true)
    }
}

@Composable
fun StatBlock(value: String, label: String, light: Boolean) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = if (light) White else Black
        )
        Text(
            text = label,
            fontSize = 9.sp,
            fontWeight = FontWeight.ExtraBold,
            color = if (light) Color(0xFF888888) else Color(0xFF666666),
            letterSpacing = 1.sp
        )
    }
}

@Composable
fun GoalSection(
    title: String,
    goals: List<Goal>,
    showProgress: Boolean,
    strikethrough: Boolean
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = title,
            fontSize = 10.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = 2.sp,
            color = Color(0xFF666666)
        )
        goals.forEach { goal ->
            GoalCard(goal = goal, showProgress = showProgress, strikethrough = strikethrough)
        }
    }
}

@Composable
fun GoalCard(goal: Goal, showProgress: Boolean, strikethrough: Boolean) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .border(1.dp, if (strikethrough) LightGray else Black, RoundedCornerShape(4.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                imageVector = if (strikethrough) Icons.Filled.CheckCircle else Icons.Filled.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (strikethrough) Color(0xFF666666) else Black,
                modifier = Modifier.size(18.dp)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = goal.title,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (strikethrough) Color(0xFF888888) else Black,
                    textDecoration = if (strikethrough) TextDecoration.LineThrough else TextDecoration.None
                )
                Text(
                    text = goal.type.uppercase(),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF888888),
                    letterSpacing = 1.sp
                )
            }
            if (showProgress) {
                Text(
                    text = "${goal.completionPercentage}%",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black,
                    color = Black
                )
            }
        }

        if (showProgress && goal.completionPercentage > 0) {
            LinearProgressIndicator(
                progress = { goal.completionPercentage / 100f },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = Black,
                trackColor = LightGray
            )
        }
    }
}

@Composable
fun EmptyGoalsPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .border(1.dp, LightGray, RoundedCornerShape(4.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("NO GOALS YET", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF888888), letterSpacing = 2.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("Chat with the AI to create your first goal.", fontSize = 13.sp, color = Color(0xFF888888))
        }
    }
}
