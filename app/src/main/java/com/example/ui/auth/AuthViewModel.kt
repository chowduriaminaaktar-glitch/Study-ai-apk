package com.example.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.AuthState
import com.example.data.model.UserAccount
import com.example.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    private val authRepository = AuthRepository(application)
    val currentUser: StateFlow<UserAccount?> = authRepository.currentUser

    val authState: StateFlow<AuthState> = currentUser.map { user ->
        if (user != null) AuthState.Authenticated(user) else AuthState.Idle
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AuthState.Idle)

    private val _isSignUpMode = MutableStateFlow(false)
    val isSignUpMode: StateFlow<Boolean> = _isSignUpMode.asStateFlow()

    private val _nameInput = MutableStateFlow("")
    val nameInput: StateFlow<String> = _nameInput.asStateFlow()

    private val _emailInput = MutableStateFlow("")
    val emailInput: StateFlow<String> = _emailInput.asStateFlow()

    private val _passwordInput = MutableStateFlow("")
    val passwordInput: StateFlow<String> = _passwordInput.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun toggleSignUpMode() {
        _isSignUpMode.value = !_isSignUpMode.value
        _errorMessage.value = null
    }

    fun onNameChange(value: String) { _nameInput.value = value }
    fun onEmailChange(value: String) { _emailInput.value = value }
    fun onPasswordChange(value: String) { _passwordInput.value = value }
    fun dismissError() { _errorMessage.value = null }

    fun signInWithGoogleDirect(email: String = "student@studyai.org", displayName: String = "Study Scholar") {
        _isLoading.value = true
        viewModelScope.launch {
            authRepository.signInWithGoogle(email, displayName)
            _isLoading.value = false
        }
    }

    fun submitEmailAuth() {
        val email = _emailInput.value.trim()
        val password = _passwordInput.value
        val name = _nameInput.value.trim()

        _isLoading.value = true
        _errorMessage.value = null

        val result = if (_isSignUpMode.value) {
            authRepository.signUpWithEmail(email, password, name)
        } else {
            authRepository.signInWithEmail(email, password)
        }

        result.onSuccess {
            _isLoading.value = false
            _passwordInput.value = ""
        }.onFailure { err ->
            _isLoading.value = false
            _errorMessage.value = err.message ?: "Authentication failed"
        }
    }

    fun continueAsGuest() {
        authRepository.signInAsGuest()
    }

    fun signOut() {
        authRepository.signOut()
    }
}
