package com.example.elektronicarebeta1.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.elektronicarebeta1.models.RepairRequest
import com.example.elektronicarebeta1.models.User
import com.example.elektronicarebeta1.repositories.RepairRepository
import com.example.elektronicarebeta1.repositories.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class DashboardViewModel(
    private val userRepository: UserRepository,
    private val repairRepository: RepairRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<DashboardUiState>(DashboardUiState.Loading)
    val uiState: StateFlow<DashboardUiState> = _uiState

    init {
        loadDashboardData()
    }

    private fun loadDashboardData() {
        viewModelScope.launch {
            try {
                val user = userRepository.getCurrentUser()
                if (user != null) {
                    repairRepository.getRepairRequests()
                        .catch { e ->
                            _uiState.value = DashboardUiState.Error(e.message ?: "Error loading repairs")
                        }
                        .collect { repairs ->
                            _uiState.value = DashboardUiState.Success(
                                user = user,
                                recentRepairs = repairs.take(5)
                            )
                        }
                } else {
                    _uiState.value = DashboardUiState.Error("User not found")
                }
            } catch (e: Exception) {
                _uiState.value = DashboardUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun refresh() {
        loadDashboardData()
    }
}

sealed class DashboardUiState {
    data object Loading : DashboardUiState()
    data class Success(
        val user: User,
        val recentRepairs: List<RepairRequest>
    ) : DashboardUiState()
    data class Error(val message: String) : DashboardUiState()
}