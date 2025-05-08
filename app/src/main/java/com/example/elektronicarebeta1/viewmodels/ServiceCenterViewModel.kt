package com.example.elektronicarebeta1.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.elektronicarebeta1.data.Result
import com.example.elektronicarebeta1.data.repositories.ServiceCenterRepository
import com.example.elektronicarebeta1.models.ServiceCenter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ServiceCenterViewModel(
    private val repository: ServiceCenterRepository
) : ViewModel() {

    private val _serviceCentersState = MutableStateFlow<ServiceCentersState>(ServiceCentersState.Initial)
    val serviceCentersState: StateFlow<ServiceCentersState> = _serviceCentersState

    private val _selectedCenter = MutableStateFlow<ServiceCenter?>(null)
    val selectedCenter: StateFlow<ServiceCenter?> = _selectedCenter

    init {
        loadServiceCenters()
    }

    fun loadServiceCenters() {
        viewModelScope.launch {
            repository.getServiceCenters().collect { result ->
                _serviceCentersState.value = when (result) {
                    is Result.Success -> ServiceCentersState.Success(result.data)
                    is Result.Error -> ServiceCentersState.Error(result.exception.message ?: "Failed to load service centers")
                    is Result.Loading -> ServiceCentersState.Loading
                }
            }
        }
    }

    fun loadServiceCentersByCategory(category: String) {
        viewModelScope.launch {
            repository.getServiceCentersByCategory(category).collect { result ->
                _serviceCentersState.value = when (result) {
                    is Result.Success -> ServiceCentersState.Success(result.data)
                    is Result.Error -> ServiceCentersState.Error(result.exception.message ?: "Failed to load service centers")
                    is Result.Loading -> ServiceCentersState.Loading
                }
            }
        }
    }

    fun selectServiceCenter(centerId: String) {
        viewModelScope.launch {
            when (val result = repository.getServiceCenterById(centerId)) {
                is Result.Success -> _selectedCenter.value = result.data
                is Result.Error -> _serviceCentersState.value = ServiceCentersState.Error(result.exception.message ?: "Failed to load service center")
                is Result.Loading -> _serviceCentersState.value = ServiceCentersState.Loading
            }
        }
    }
}

sealed class ServiceCentersState {
    object Initial : ServiceCentersState()
    object Loading : ServiceCentersState()
    data class Success(val centers: List<ServiceCenter>) : ServiceCentersState()
    data class Error(val message: String) : ServiceCentersState()
}

class ServiceCenterViewModelFactory(
    private val repository: ServiceCenterRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ServiceCenterViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ServiceCenterViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}