package com.example.ui.auth

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AuthState
import com.example.data.model.UserAccount
import com.example.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    val authRepository = AuthRepository(application)

    val authState: StateFlow<AuthState> = authRepository.authState
    val currentUser: StateFlow<UserAccount?> = authRepository.currentUser

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun dismissError() {
        _errorMessage.value = null
    }

    /**
     * Trigger real Google Sign-In with CredentialManager
     */
    fun signInWithGoogle(context: Context) {
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            val result = authRepository.signInWithGoogle(context)
            _isLoading.value = false
            result.onFailure { error ->
                if (error !is androidx.credentials.exceptions.GetCredentialCancellationException) {
                    _errorMessage.value = error.localizedMessage ?: "Google Sign-In failed"
                }
            }
        }
    }

    /**
     * Complete first-time user registration
     */
    fun completeRegistration(
        uid: String,
        displayName: String,
        email: String,
        dateOfBirth: String,
        photoURL: String?,
        onSuccess: () -> Unit = {}
    ) {
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            val result = authRepository.completeRegistration(
                uid = uid,
                displayName = displayName,
                email = email,
                dateOfBirth = dateOfBirth,
                photoURL = photoURL
            )
            _isLoading.value = false
            result.onSuccess {
                onSuccess()
            }.onFailure { error ->
                _errorMessage.value = error.localizedMessage ?: "Failed to create account"
            }
        }
    }

    /**
     * Update existing user profile settings
     */
    fun updateProfile(
        displayName: String,
        dateOfBirth: String,
        photoURL: String?,
        onSuccess: () -> Unit = {}
    ) {
        _isLoading.value = true
        _errorMessage.value = null
        viewModelScope.launch {
            val result = authRepository.updateProfile(
                displayName = displayName,
                dateOfBirth = dateOfBirth,
                photoURL = photoURL
            )
            _isLoading.value = false
            result.onSuccess {
                onSuccess()
            }.onFailure { error ->
                _errorMessage.value = error.localizedMessage ?: "Failed to update profile"
            }
        }
    }

    fun signOut() {
        authRepository.signOut()
    }
}
