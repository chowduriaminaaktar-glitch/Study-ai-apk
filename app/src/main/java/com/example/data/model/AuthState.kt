package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

enum class AuthProvider {
    GOOGLE,
    EMAIL_PASSWORD,
    GUEST
}

@IgnoreExtraProperties
data class UserAccount(
    @DocumentId val id: String = "",
    val uid: String = "",
    val email: String = "",
    val displayName: String = "",
    val photoURL: String? = null,
    val dateOfBirth: String = "",
    val provider: AuthProvider = AuthProvider.GOOGLE,
    val streakDays: Int = 5,
    val solvedQuestionsCount: Int = 18,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
    @ServerTimestamp val lastLoginAt: Timestamp? = null
) {
    // Interoperability property for UI
    val photoUrl: String? get() = photoURL
}

sealed interface AuthState {
    data object Loading : AuthState
    data object Unauthenticated : AuthState
    data class NeedsRegistration(
        val uid: String,
        val email: String,
        val displayName: String,
        val photoUrl: String? = null
    ) : AuthState
    data class Authenticated(val user: UserAccount) : AuthState
    data class Error(val message: String) : AuthState
}
