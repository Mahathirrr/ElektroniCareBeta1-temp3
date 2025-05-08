class ServiceViewModel(
    private val serviceRepository: ServiceRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ServiceUiState>(ServiceUiState.Initial)
    val uiState: StateFlow<ServiceUiState> = _uiState

    private val _serviceCenters = MutableStateFlow<List<ServiceCenter>>(emptyList())
    val serviceCenters: StateFlow<List<ServiceCenter>> = _serviceCenters

    private val _selectedImages = MutableStateFlow<List<Uri>>(emptyList())
    val selectedImages: StateFlow<List<Uri>> = _selectedImages

    init {
        loadServiceCenters()
    }

    private fun loadServiceCenters() {
        viewModelScope.launch {
            try {
                serviceRepository.getServiceCenters()
                    .catch { e ->
                        _uiState.value = ServiceUiState.Error(e.message ?: "Failed to load service centers")
                    }
                    .collect { centers ->
                        _serviceCenters.value = centers
                    }
            } catch (e: Exception) {
                _uiState.value = ServiceUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun createServiceRequest(
        deviceType: String,
        deviceModel: String,
        issue: String,
        scheduledDate: Long,
        scheduledTime: String,
        serviceCenterId: String
    ) {
        viewModelScope.launch {
            _uiState.value = ServiceUiState.Loading

            try {
                // First upload all images
                val uploadedImageUrls = selectedImages.value.mapNotNull { uri ->
                    uri.toByteArray()?.let { bytes ->
                        serviceRepository.uploadServiceImage(bytes).getOrNull()
                    }
                }

                // Create service request with uploaded image URLs
                val request = ServiceRequest(
                    deviceType = deviceType,
                    deviceModel = deviceModel,
                    issue = issue,
                    images = uploadedImageUrls,
                    scheduledDate = scheduledDate,
                    scheduledTime = scheduledTime,
                    serviceCenterId = serviceCenterId
                )

                val result = serviceRepository.createServiceRequest(request)
                
                if (result.isSuccess) {
                    _uiState.value = ServiceUiState.Success(result.getOrNull()!!)
                } else {
                    _uiState.value = ServiceUiState.Error(
                        result.exceptionOrNull()?.message ?: "Failed to create service request"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = ServiceUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun addImage(uri: Uri) {
        _selectedImages.value = _selectedImages.value + uri
    }

    fun removeImage(uri: Uri) {
        _selectedImages.value = _selectedImages.value - uri
    }

    fun clearState() {
        _uiState.value = ServiceUiState.Initial
        _selectedImages.value = emptyList()
    }
}

sealed class ServiceUiState {
    data object Initial : ServiceUiState()
    data object Loading : ServiceUiState()
    data class Success(val request: ServiceRequest) : ServiceUiState()
    data class Error(val message: String) : ServiceUiState()
}

private fun Uri.toByteArray(): ByteArray? {
    return try {
        val inputStream = contentResolver.openInputStream(this)
        inputStream?.readBytes()
    } catch (e: Exception) {
        null
    }
}