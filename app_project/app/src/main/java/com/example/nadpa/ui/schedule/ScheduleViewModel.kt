package com.example.nadpa.ui.schedule

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nadpa.data.ApiClient
import com.example.nadpa.data.Task
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ScheduleViewModel : ViewModel() {

    private val _weekIndex = MutableStateFlow(0)
    val weekIndex: StateFlow<Int> = _weekIndex

    private val _tasks = MutableStateFlow<List<Task>>(emptyList())
    val tasks: StateFlow<List<Task>> = _tasks

    init {
        loadWeekTasks()
    }

    fun navigateWeek(delta: Int) {
        val next = (_weekIndex.value + delta).coerceIn(-100, 52)
        _weekIndex.value = next
        loadWeekTasks()
    }

    private fun loadWeekTasks() {
        viewModelScope.launch {
            try {
                _tasks.value = ApiClient.api.getTasks(_weekIndex.value)
            } catch (e: Exception) {
                _tasks.value = emptyList()
            }
        }
    }
}
