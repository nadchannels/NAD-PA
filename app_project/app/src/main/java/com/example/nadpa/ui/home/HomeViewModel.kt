package com.example.nadpa.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nadpa.data.ApiClient
import com.example.nadpa.data.Inspiration
import com.example.nadpa.data.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class HomeViewModel : ViewModel() {

    private val _inspiration = MutableStateFlow<Inspiration?>(null)
    val inspiration: StateFlow<Inspiration?> = _inspiration

    private val _currentTask = MutableStateFlow<Task?>(null)
    val currentTask: StateFlow<Task?> = _currentTask

    private val _upcomingTask = MutableStateFlow<Task?>(null)
    val upcomingTask: StateFlow<Task?> = _upcomingTask

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                _inspiration.value = ApiClient.api.getTodayInspiration()
            } catch (e: Exception) {
                // Inspiration not yet fetched — use null (shows "Loading" state)
            }

            try {
                val tasks = ApiClient.api.getTasks(0)
                val now = LocalTime.now()
                val fmt = DateTimeFormatter.ofPattern("HH:mm")

                // Find current session (task running right now)
                _currentTask.value = tasks.firstOrNull { task ->
                    try {
                        val start = LocalTime.parse(task.startTime, fmt)
                        val end = LocalTime.parse(task.endTime, fmt)
                        now.isAfter(start) && now.isBefore(end)
                    } catch (e: Exception) { false }
                }

                // Find upcoming session (next task after now)
                _upcomingTask.value = tasks
                    .filter { task ->
                        try {
                            val start = LocalTime.parse(task.startTime, fmt)
                            start.isAfter(now)
                        } catch (e: Exception) { false }
                    }
                    .minByOrNull { task ->
                        try { LocalTime.parse(task.startTime, fmt).toSecondOfDay() }
                        catch (e: Exception) { Int.MAX_VALUE }
                    }

            } catch (e: Exception) {
                // No tasks or network error — show empty state
            }
        }
    }
}
