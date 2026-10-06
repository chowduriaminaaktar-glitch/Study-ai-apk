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
    val streakDays: Int = 5,
    val solvedQuestionsCount: Int = 18,
    val createdAt: Long = System.currentTimeMillis()
)

sealed interface AuthState {
    data object Idle : AuthState
    data object Loading : AuthState
    data class Authenticated(val user: UserAccount) : AuthState
    data class Error(val message: String) : AuthState
}
