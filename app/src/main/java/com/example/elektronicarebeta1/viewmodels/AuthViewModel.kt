package com.example.elektronicarebeta1.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.elektronicarebeta1.data.Result
import com.example.elektronicarebeta1.data.repositories.AuthRepository
import com.example.elektronicarebeta1.models.User
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState

    fun signInWithEmail(email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            when (val result = repository.signInWithEmail(email, password)) {
                is Result.Success -> _authState.value = AuthState.Success(result.data)
                is Result.Error -> _authState.value = AuthState.Error(result.exception.message ?: "Authentication failed")
                is Result.Loading -> _authState.value = AuthState.Loading
            }
        }
    }

    fun signUpWithEmail(email: String, password: String, fullName: String, mobile: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            when (val result = repository.signUpWithEmail(email, password, fullName, mobile)) {
                is Result.Success -> _authState.value = AuthState.Success(result.data)
                is Result.Error -> _authState.value = AuthState.Error(result.exception.message ?: "Registration failed")
                is Result.Loading -> _authState.value = AuthState.Loading
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            
            when (val result = repository.signInWithGoogle(idToken)) {
                is Result.Success -> _authState.value = AuthState.Success(result.data)
                is Result.Error -> _authState.value = AuthState.Error(result.exception.message ?: "Google sign in failed")
                is Result.Loading -> _authState.value = AuthState.Loading
            }
        }
    }

    fun signOut() {
        repository.signOut()
        _authState.value = AuthState.Initial
    }

    fun getCurrentUser(): User? = repository.getCurrentUser()
}

sealed class AuthState {
    object Initial : AuthState()
    object Loading : AuthState()
    data class Success(val user: User) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModelFactory(
    private val repository: AuthRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}