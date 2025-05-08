```kotlin
package com.example.elektronicarebeta1.viewmodels

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.elektronicarebeta1.data.Result
import com.example.elektronicarebeta1.data.repositories.UserRepository
import com.example.elektronicarebeta1.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class UserProfileViewModel(
    private val repository: UserRepository
) : ViewModel() {

    private val _profileState = MutableStateFlow<ProfileState>(ProfileState.Initial)
    val profileState: StateFlow<ProfileState> = _profileState

    private val _selectedImage = MutableStateFlow<Uri?>(null)
    val selectedImage: StateFlow<Uri?> = _selectedImage

    init {
        loadUserProfile()
    }

    fun loadUserProfile() {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            
            when (val result = repository.getCurrentUser()) {
                is Result.Success -> _profileState.value = ProfileState.Success(result.data)
                is Result.Error -> _profileState.value = ProfileState.Error(
                    result.exception.message ?: "Failed to load profile"
                )
                is Result.Loading -> _profileState.value = ProfileState.Loading
            }
        }
    }

    fun updateProfile(
        fullName: String,
        mobile: String,
        address: String,
        city: String
    ) {
        viewModelScope.launch {
            _profileState.value = ProfileState.Loading
            
            when (val result = repository.updateUserProfile(
                fullName = fullName,
                mobile = mobile,
                address = address,
                city = city,
                profileImage = selectedImage.value
            )) {
                is Result.Success -> {
                    _profileState.value = ProfileState.Success(result.data)
                    clearSelectedImage()
                }
                is Result.Error -> _profileState.value = ProfileState.Error(
                    result.exception.message ?: "Failed to update profile"
                )
                is Result.Loading -> _profileState.value = ProfileState.Loading
            }
        }
    }

    fun setProfileImage(uri: Uri) {
        _selectedImage.value = uri
    }

    private fun clearSelectedImage() {
        _selectedImage.value = null
    }
}

sealed class ProfileState {
    object Initial : ProfileState()
    object Loading : ProfileState()
    data class Success(val user: User) : ProfileState()
    data class Error(val message: String) : ProfileState()
}

class UserProfileViewModelFactory(
    private val repository: UserRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(UserProfileViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return UserProfileViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
```