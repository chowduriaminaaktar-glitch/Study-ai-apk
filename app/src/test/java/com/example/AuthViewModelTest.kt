package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.AuthProvider
import com.example.ui.auth.AuthViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AuthViewModelTest {

    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        viewModel = AuthViewModel(app)
        viewModel.signOut() // start clean
    }

    @Test
    fun `signInWithGoogleDirect sets Google account`() {
        viewModel.signInWithGoogleDirect(
            email = "student@gmail.com",
            displayName = "Student Test"
        )

        val user = viewModel.currentUser.value
        assertNotNull(user)
        assertEquals("student@gmail.com", user?.email)
        assertEquals("Student Test", user?.displayName)
        assertEquals(AuthProvider.GOOGLE, user?.provider)
    }

    @Test
    fun `continueAsGuest creates guest user`() {
        viewModel.continueAsGuest()
        val user = viewModel.currentUser.value
        assertNotNull(user)
        assertEquals(AuthProvider.GUEST, user?.provider)
    }

    @Test
    fun `signOut clears authenticated user`() {
        viewModel.signInWithGoogleDirect("user@gmail.com", "User")
        assertNotNull(viewModel.currentUser.value)

        viewModel.signOut()
        assertNull(viewModel.currentUser.value)
    }

    @Test
    fun `email validation fails for empty fields`() {
        viewModel.onEmailChange("")
        viewModel.onPasswordChange("")
        viewModel.submitEmailAuth()

        assertNotNull(viewModel.errorMessage.value)
    }
}
