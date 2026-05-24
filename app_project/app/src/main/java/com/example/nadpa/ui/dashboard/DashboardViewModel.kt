package com.example.nadpa.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nadpa.data.ApiClient
import com.example.nadpa.data.Goal
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DashboardViewModel : ViewModel() {

    private val _goals = MutableStateFlow<List<Goal>>(emptyList())
    val goals: StateFlow<List<Goal>> = _goals

    init {
        loadGoals()
    }

    private fun loadGoals() {
        viewModelScope.launch {
            try {
                _goals.value = ApiClient.api.getGoals()
            } catch (e: Exception) {
                _goals.value = emptyList()
            }
        }
    }

    fun refresh() {
        loadGoals()
    }
}
