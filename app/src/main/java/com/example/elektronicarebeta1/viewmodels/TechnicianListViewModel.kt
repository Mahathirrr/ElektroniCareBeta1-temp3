package com.example.elektronicarebeta1.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.elektronicarebeta1.models.Technician
import com.example.elektronicarebeta1.repositories.TechnicianRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class TechnicianListViewModel(
    private val technicianRepository: TechnicianRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<TechnicianListUiState>(TechnicianListUiState.Loading)
    val uiState: StateFlow<TechnicianListUiState> = _uiState

    private var currentSpecialization: String? = null

    init {
        loadTechnicians()
    }

    fun loadTechnicians(specialization: String? = null) {
        viewModelScope.launch {
            _uiState.value = TechnicianListUiState.Loading
            currentSpecialization = specialization
            
            try {
                val flow = if (specialization != null) {
                    technicianRepository.getTechniciansBySpecialization(specialization)
                } else {
                    technicianRepository.getTechnicians()
                }
                
                flow.catch { e ->
                    _uiState.value = TechnicianListUiState.Error(
                        e.message ?: "Error loading technicians"
                    )
                }.collect { technicians ->
                    _uiState.value = TechnicianListUiState.Success(technicians)
                }
            } catch (e: Exception) {
                _uiState.value = TechnicianListUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun refresh() {
        loadTechnicians(currentSpecialization)
    }
}

sealed class TechnicianListUiState {
    data object Loading : TechnicianListUiState()
    data class Success(val technicians: List<Technician>) : TechnicianListUiState()
    data class Error(val message: String) : TechnicianListUiState()
}