package com.example.nadpa

import androidx.navigation3.runtime.NavKey
import kotlinx.serialization.Serializable

@Serializable data object Main : NavKey

// Bottom nav destinations
@Serializable data object HomeDestination : NavKey
@Serializable data object ScheduleDestination : NavKey
@Serializable data object DashboardDestination : NavKey
@Serializable data object AiDestination : NavKey
