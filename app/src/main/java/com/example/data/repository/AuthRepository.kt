package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.AuthProvider
import com.example.data.model.UserAccount
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.UUID

class AuthRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _currentUser = MutableStateFlow<UserAccount?>(null)
    val currentUser: StateFlow<UserAccount?> = _currentUser.asStateFlow()

    init {
        loadSavedUser()
    }

    private fun loadSavedUser() {
        val isLoggedIn = prefs.getBoolean(KEY_IS_LOGGED_IN, false)
        if (isLoggedIn) {
            val id = prefs.getString(KEY_USER_ID, UUID.randomUUID().toString()) ?: UUID.randomUUID().toString()
            val email = prefs.getString(KEY_EMAIL, "") ?: ""
            val name = prefs.getString(KEY_NAME, "User") ?: "User"
            val photoUrl = prefs.getString(KEY_PHOTO_URL, null)
            val providerStr = prefs.getString(KEY_PROVIDER, AuthProvider.GOOGLE.name) ?: AuthProvider.GOOGLE.name
            val provider = try { AuthProvider.valueOf(providerStr) } catch (e: Exception) { AuthProvider.GOOGLE }

            if (email.isNotBlank() || provider == AuthProvider.GUEST) {
                _currentUser.value = UserAccount(
                    id = id,
                    email = email,
                    displayName = name,
                    photoUrl = photoUrl,
                    provider = provider
                )
            }
        }
    }

    fun signInWithGoogle(
        email: String,
        displayName: String,
        photoUrl: String? = null,
        googleId: String? = null
    ): UserAccount {
        val userId = googleId ?: "google_${UUID.randomUUID().toString().take(12)}"
        val account = UserAccount(
            id = userId,
            email = email,
            displayName = displayName.ifBlank { email.substringBefore("@") },
            photoUrl = photoUrl,
            provider = AuthProvider.GOOGLE
        )
        saveUser(account)
        return account
    }

    fun signInWithEmail(email: String, password: String): Result<UserAccount> {
        if (!email.contains("@") || !email.contains(".")) {
            return Result.failure(IllegalArgumentException("Please enter a valid email address."))
        }
        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("Password must be at least 6 characters."))
        }

        val storedAccounts = prefs.getStringSet(KEY_REGISTERED_EMAILS, emptySet()) ?: emptySet()
        val displayName = email.substringBefore("@").replaceFirstChar { it.uppercase() }

        val account = UserAccount(
            id = "email_${UUID.randomUUID().toString().take(12)}",
            email = email,
            displayName = displayName,
            provider = AuthProvider.EMAIL_PASSWORD
        )
        saveUser(account)

        val updated = storedAccounts.toMutableSet().apply { add(email) }
        prefs.edit().putStringSet(KEY_REGISTERED_EMAILS, updated).apply()

        return Result.success(account)
    }

    fun signUpWithEmail(email: String, password: String, displayName: String): Result<UserAccount> {
        if (!email.contains("@") || !email.contains(".")) {
            return Result.failure(IllegalArgumentException("Please enter a valid email address."))
        }
        if (password.length < 6) {
            return Result.failure(IllegalArgumentException("Password must be at least 6 characters."))
        }
        if (displayName.isBlank()) {
            return Result.failure(IllegalArgumentException("Please enter your name."))
        }

        val account = UserAccount(
            id = "email_${UUID.randomUUID().toString().take(12)}",
            email = email,
            displayName = displayName.trim(),
            provider = AuthProvider.EMAIL_PASSWORD
        )
        saveUser(account)
        return Result.success(account)
    }

    fun signInAsGuest(): UserAccount {
        val account = UserAccount(
            id = "guest_${UUID.randomUUID().toString().take(8)}",
            email = "guest@studyai.app",
            displayName = "Guest Student",
            provider = AuthProvider.GUEST
        )
        saveUser(account)
        return account
    }

    fun signOut() {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, false)
            .remove(KEY_USER_ID)
            .remove(KEY_EMAIL)
            .remove(KEY_NAME)
            .remove(KEY_PHOTO_URL)
            .remove(KEY_PROVIDER)
            .apply()
        _currentUser.value = null
    }

    private fun saveUser(account: UserAccount) {
        prefs.edit()
            .putBoolean(KEY_IS_LOGGED_IN, true)
            .putString(KEY_USER_ID, account.id)
            .putString(KEY_EMAIL, account.email)
            .putString(KEY_NAME, account.displayName)
            .putString(KEY_PHOTO_URL, account.photoUrl)
            .putString(KEY_PROVIDER, account.provider.name)
            .apply()
        _currentUser.value = account
    }

    companion object {
        private const val PREFS_NAME = "study_ai_auth_prefs"
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_EMAIL = "email"
        private const val KEY_NAME = "display_name"
        private const val KEY_PHOTO_URL = "photo_url"
        private const val KEY_PROVIDER = "auth_provider"
        private const val KEY_REGISTERED_EMAILS = "registered_emails"
    }
}
