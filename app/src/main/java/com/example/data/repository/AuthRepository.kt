package com.example.data.repository

import android.app.Activity
import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.data.model.AuthProvider
import com.example.data.model.AuthState
import com.example.data.model.UserAccount
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.studyai.app.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthRepository(context: Context) {
    private val appContext = context.applicationContext
    private val coroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val databaseId: String = appContext.getString(R.string.firestore_database_id)
    val db: FirebaseFirestore = FirebaseFirestore.getInstance(databaseId)

    private val _authState = MutableStateFlow<AuthState>(AuthState.Loading)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<UserAccount?>(null)
    val currentUser: StateFlow<UserAccount?> = _currentUser.asStateFlow()

    private val authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
        val user = firebaseAuth.currentUser
        if (user == null) {
            _currentUser.value = null
            _authState.value = AuthState.Unauthenticated
        } else {
            coroutineScope.launch {
                checkAndLoadUserProfile(user)
            }
        }
    }

    init {
        auth.addAuthStateListener(authListener)
    }

    /**
     * Check if user profile already exists in Firestore users/{uid}.
     * If yes -> load profile, update lastLoginAt, set Authenticated state.
     * If no -> set NeedsRegistration state so user completes DOB and profile setup.
     */
    suspend fun checkAndLoadUserProfile(user: FirebaseUser) {
        try {
            _authState.value = AuthState.Loading
            val userDocRef = db.collection("users").document(user.uid)
            val snapshot = userDocRef.get().await()

            if (snapshot.exists()) {
                // Update last login timestamp
                try {
                    userDocRef.update("lastLoginAt", FieldValue.serverTimestamp()).await()
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to update lastLoginAt: ${e.message}")
                }

                val account = snapshot.toObject(
                    UserAccount::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                ) ?: UserAccount(
                    id = user.uid,
                    uid = user.uid,
                    email = user.email ?: "",
                    displayName = user.displayName ?: (user.email?.substringBefore("@") ?: "Student"),
                    photoURL = user.photoUrl?.toString(),
                    dateOfBirth = snapshot.getString("dateOfBirth") ?: ""
                )

                _currentUser.value = account
                _authState.value = AuthState.Authenticated(account)
            } else {
                // New user - needs first-time registration form
                _currentUser.value = null
                _authState.value = AuthState.NeedsRegistration(
                    uid = user.uid,
                    email = user.email ?: "",
                    displayName = user.displayName ?: (user.email?.substringBefore("@") ?: ""),
                    photoUrl = user.photoUrl?.toString()
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error fetching user profile from Firestore", e)
            _authState.value = AuthState.Error(e.localizedMessage ?: "Failed to load user profile")
        }
    }

    /**
     * Initiates Google Sign-In using Android CredentialManager and GetSignInWithGoogleOption.
     */
    suspend fun signInWithGoogle(activityContext: Context): Result<Unit> {
        return try {
            val credentialManager = CredentialManager.create(activityContext)
            val webClientId = activityContext.getString(R.string.default_web_client_id)

            val googleIdOption = GetSignInWithGoogleOption.Builder(webClientId)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val result = credentialManager.getCredential(activityContext, request)
            val credential = result.credential

            if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken
                val authCredential = GoogleAuthProvider.getCredential(idToken, null)
                auth.signInWithCredential(authCredential).await()
                Result.success(Unit)
            } else {
                Result.failure(IllegalArgumentException("Unsupported credential type returned"))
            }
        } catch (e: GetCredentialCancellationException) {
            Log.w(TAG, "User cancelled Google Sign-In prompt")
            Result.failure(e)
        } catch (e: Exception) {
            Log.e(TAG, "Google Sign-In failed", e)
            Result.failure(e)
        }
    }

    /**
     * First-time registration after Google authentication.
     * Writes document to users/{uid} with mandatory DOB, editable name, and profile picture.
     */
    suspend fun completeRegistration(
        uid: String,
        displayName: String,
        email: String,
        dateOfBirth: String,
        photoURL: String?
    ): Result<UserAccount> {
        if (displayName.isBlank()) {
            return Result.failure(IllegalArgumentException("Display name cannot be empty."))
        }
        if (dateOfBirth.isBlank()) {
            return Result.failure(IllegalArgumentException("Date of birth is mandatory."))
        }

        return try {
            val userMap = mutableMapOf<String, Any>(
                "uid" to uid,
                "displayName" to displayName.trim(),
                "email" to email.trim(),
                "dateOfBirth" to dateOfBirth.trim(),
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp(),
                "lastLoginAt" to FieldValue.serverTimestamp()
            )
            if (!photoURL.isNullOrBlank()) {
                userMap["photoURL"] = photoURL
            }

            db.collection("users").document(uid).set(userMap, SetOptions.merge()).await()

            val account = UserAccount(
                id = uid,
                uid = uid,
                email = email,
                displayName = displayName.trim(),
                photoURL = photoURL,
                dateOfBirth = dateOfBirth.trim(),
                provider = AuthProvider.GOOGLE
            )
            _currentUser.value = account
            _authState.value = AuthState.Authenticated(account)
            Result.success(account)
        } catch (e: Exception) {
            Log.e(TAG, "Error saving registration profile", e)
            Result.failure(e)
        }
    }

    /**
     * Update existing user profile in Firestore users/{uid}.
     */
    suspend fun updateProfile(
        displayName: String,
        dateOfBirth: String,
        photoURL: String?
    ): Result<UserAccount> {
        val uid = auth.currentUser?.uid
            ?: return Result.failure(IllegalStateException("User is not authenticated."))
        val existing = _currentUser.value
            ?: return Result.failure(IllegalStateException("No local user account profile found."))

        if (displayName.isBlank()) {
            return Result.failure(IllegalArgumentException("Display name cannot be empty."))
        }
        if (dateOfBirth.isBlank()) {
            return Result.failure(IllegalArgumentException("Date of birth cannot be empty."))
        }

        return try {
            val updates = mutableMapOf<String, Any>(
                "displayName" to displayName.trim(),
                "dateOfBirth" to dateOfBirth.trim(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            if (photoURL != null) {
                updates["photoURL"] = photoURL
            }

            db.collection("users").document(uid).update(updates).await()

            val updatedAccount = existing.copy(
                displayName = displayName.trim(),
                dateOfBirth = dateOfBirth.trim(),
                photoURL = photoURL ?: existing.photoURL
            )
            _currentUser.value = updatedAccount
            _authState.value = AuthState.Authenticated(updatedAccount)
            Result.success(updatedAccount)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating user profile in Firestore", e)
            Result.failure(e)
        }
    }

    fun signOut() {
        auth.signOut()
        _currentUser.value = null
        _authState.value = AuthState.Unauthenticated
    }

    companion object {
        private const val TAG = "AuthRepository"
    }
}
