```kotlin
package com.example.elektronicarebeta1.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.elektronicarebeta1.data.Result
import com.example.elektronicarebeta1.data.repositories.RepairRequestRepository
import com.example.elektronicarebeta1.models.RepairRequest
import com.example.elektronicarebeta1.models.RepairStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class RepairRequestViewModel(
    private val repository: RepairRequestRepository
) : ViewModel() {

    private val _repairRequestsState = MutableStateFlow<RepairRequestsState>(RepairRequestsState.Initial)
    val repairRequestsState: StateFlow<RepairRequestsState> = _repairRequestsState

    private val _selectedRequest = MutableStateFlow<RepairRequest?>(null)
    val selectedRequest: StateFlow<RepairRequest?> = _selectedRequest

    private val _selectedImages = MutableStateFlow<List<Uri>>(emptyList())
    val selectedImages: StateFlow<List<Uri>> = _selectedImages

    init {
        loadRepairRequests()
    }

    fun loadRepairRequests() {
        viewModelScope.launch {
            repository.getRepairRequests().collect { result ->
                _repairRequestsState.value = when (result) {
                    is Result.Success -> RepairRequestsState.Success(result.data)
                    is Result.Error -> RepairRequestsState.Error(result.exception.message ?: "Failed to load repair requests")
                    is Result.Loading -> RepairRequestsState.Loading
                }
            }
        }
    }

    fun createRepairRequest(
        deviceType: String,
        deviceModel: String,
        issue: String,
        scheduledDate: Long,
        scheduledTime: String,
        serviceCenterId: String
    ) {
        viewModelScope.launch {
            _repairRequestsState.value = RepairRequestsState.Loading

            val request = RepairRequest(
                deviceType = deviceType,
                deviceModel = deviceModel,
                issue = issue,
                scheduledDate = scheduledDate,
                scheduledTime = scheduledTime,
                serviceCenterId = serviceCenterId
            )

            when (val result = repository.createRepairRequest(request, selectedImages.value)) {
                is Result.Success -> {
                    _repairRequestsState.value = RepairRequestsState.RequestCreated(result.data)
                    clearSelectedImages()
                }
                is Result.Error -> _repairRequestsState.value = RepairRequestsState.Error(
                    result.exception.message ?: "Failed to create repair request"
                )
                is Result.Loading -> _repairRequestsState.value = RepairRequestsState.Loading
            }
        }
    }

    fun loadRepairRequest(requestId: String) {
        viewModelScope.launch {
            when (val result = repository.getRepairRequestById(requestId)) {
                is Result.Success -> _selectedRequest.value = result.data
                is Result.Error -> _repairRequestsState.value = RepairRequestsState.Error(
                    result.exception.message ?: "Failed to load repair request"
                )
                is Result.Loading -> _repairRequestsState.value = RepairRequestsState.Loading
            }
        }
    }

    fun cancelRepairRequest(requestId: String) {
        viewModelScope.launch {
            when (val result = repository.cancelRepairRequest(requestId)) {
                is Result.Success -> {
                    _repairRequestsState.value = RepairRequestsState.RequestCancelled
                    loadRepairRequests()
                }
                is Result.Error -> _repairRequestsState.value = RepairRequestsState.Error(
                    result.exception.message ?: "Failed to cancel repair request"
                )
                is Result.Loading -> _repairRequestsState.value = RepairRequestsState.Loading
            }
        }
    }

    fun addImage(uri: Uri) {
        _selectedImages.value = _selectedImages.value + uri
    }

    fun removeImage(uri: Uri) {
        _selectedImages.value = _selectedImages.value - uri
    }

    private fun clearSelectedImages() {
        _selectedImages.value = emptyList()
    }
}

sealed class RepairRequestsState {
    object Initial : RepairRequestsState()
    object Loading : RepairRequestsState()
    data class Success(val requests: List<RepairRequest>) : RepairRequestsState()
    data class RequestCreated(val request: RepairRequest) : RepairRequestsState()
    object RequestCancelled : RepairRequestsState()
    data class Error(val message: String) : RepairRequestsState()
}

class RepairRequestViewModelFactory(
    private val repository: RepairRequestRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RepairRequestViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RepairRequestViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
```