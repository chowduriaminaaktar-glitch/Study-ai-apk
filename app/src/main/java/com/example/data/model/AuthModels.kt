package com.example.data.model

enum class AuthProvider {
    GOOGLE,
    EMAIL_PASSWORD,
    GUEST
}

data class UserAccount(
    val id: String,
    val email: String,
    val displayName: String,
    val photoUrl: String? = null,
    val provider: AuthProvider = AuthProvider.GOOGLE,
    val createdAt: Long = System.currentTimeMillis()
)

sealed interface AuthState {
    data object Unauthenticated : AuthState
    data object Loading : AuthState
    data class Authenticated(val user: UserAccount) : AuthState
    data class Error(val message: String) : AuthState
}
