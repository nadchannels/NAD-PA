package com.example.nadpa

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.nadpa.theme.Black
import com.example.nadpa.theme.White
import com.example.nadpa.ui.ai.AiScreen
import com.example.nadpa.ui.dashboard.DashboardScreen
import com.example.nadpa.ui.home.HomeScreen
import com.example.nadpa.ui.schedule.ScheduleScreen

// Material Icons (using vector-backed icons)
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Memory

data class NavItem(
    val label: String,
    val icon: ImageVector,
    val index: Int
)

@Composable
fun MainNavigation() {
    var selectedTab by remember { mutableIntStateOf(0) }

    val navItems = listOf(
        NavItem("Home", Icons.Filled.Home, 0),
        NavItem("Schedule", Icons.Filled.CalendarMonth, 1),
        NavItem("Dashboard", Icons.Filled.BarChart, 2),
        NavItem("AI", Icons.Filled.Memory, 3),
    )

    Scaffold(
        bottomBar = {
            NADBottomBar(
                items = navItems,
                selected = selectedTab,
                onSelect = { selectedTab = it }
            )
        },
        containerColor = White
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues)) {
            when (selectedTab) {
                0 -> HomeScreen()
                1 -> ScheduleScreen()
                2 -> DashboardScreen()
                3 -> AiScreen()
            }
        }
    }
}

@Composable
fun NADBottomBar(
    items: List<NavItem>,
    selected: Int,
    onSelect: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Black)
            .navigationBarsPadding()
            .height(64.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEach { item ->
            val isSelected = item.index == selected
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable { onSelect(item.index) },
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = item.icon,
                    contentDescription = item.label,
                    tint = if (isSelected) White else Color(0xFF666666),
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = item.label,
                    color = if (isSelected) White else Color(0xFF666666),
                    fontSize = 10.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
                if (isSelected) {
                    Spacer(modifier = Modifier.height(2.dp))
                    Box(
                        modifier = Modifier
                            .size(4.dp)
                            .background(White, shape = androidx.compose.foundation.shape.CircleShape)
                    )
                }
            }
        }
    }
}
