package com.example.accessbridge.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.accessbridge.data.remote.SupabaseClientProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val isGuestMode: Boolean = false,
    val currentUserEmail: String? = null,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class AuthViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    init {
        checkCurrentUser()
    }

    fun checkCurrentUser() {
        viewModelScope.launch {
            try {
                val supabase = SupabaseClientProvider.client
                val user = supabase.auth.currentUserOrNull()
                if (user != null) {
                    _uiState.update {
                        it.copy(
                            isLoggedIn = true,
                            isGuestMode = false,
                            currentUserEmail = user.email
                        )
                    }
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                // Ignore initialization error if Supabase credentials are placeholder or network is unavailable
            }
        }
    }

    fun signIn(emailInput: String, passwordInput: String, onSuccess: () -> Unit) {
        val trimmedEmail = emailInput.trim()
        if (trimmedEmail.isBlank() || passwordInput.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter both email and password") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            try {
                val supabase = SupabaseClientProvider.client
                supabase.auth.signInWith(Email) {
                    email = trimmedEmail
                    password = passwordInput
                }

                // Assuming email confirmation is disabled, user session is active immediately upon sign in
                val user = supabase.auth.currentUserOrNull()
                val session = supabase.auth.currentSessionOrNull()

                if (user != null || session != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoggedIn = true,
                            isGuestMode = false,
                            currentUserEmail = user?.email ?: trimmedEmail,
                            successMessage = "Signed in successfully"
                        )
                    }
                    onSuccess()
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Sign in failed: Session could not be established."
                        )
                    }
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                val friendlyMessage = mapAuthErrorToUserFriendlyMessage(e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = friendlyMessage
                    )
                }
            }
        }
    }

    fun signUp(emailInput: String, passwordInput: String, onSuccess: () -> Unit) {
        val trimmedEmail = emailInput.trim()
        if (trimmedEmail.isBlank() || passwordInput.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter both email and password") }
            return
        }
        if (passwordInput.length < 6) {
            _uiState.update { it.copy(errorMessage = "Password must be at least 6 characters") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            try {
                val supabase = SupabaseClientProvider.client
                supabase.auth.signUpWith(Email) {
                    email = trimmedEmail
                    password = passwordInput
                }

                // Assuming email confirmation is disabled, user session is active immediately upon sign up
                val user = supabase.auth.currentUserOrNull()
                val session = supabase.auth.currentSessionOrNull()

                if (user != null || session != null) {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isLoggedIn = true,
                            isGuestMode = false,
                            currentUserEmail = user?.email ?: trimmedEmail,
                            successMessage = "Account created successfully"
                        )
                    }
                    onSuccess()
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            errorMessage = "Sign up succeeded but session could not be established. Please try signing in."
                        )
                    }
                }
            } catch (e: Exception) {
                if (e is CancellationException) throw e
                val friendlyMessage = mapAuthErrorToUserFriendlyMessage(e)
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = friendlyMessage
                    )
                }
            }
        }
    }

    private fun mapAuthErrorToUserFriendlyMessage(e: Throwable): String {
        val message = (e.localizedMessage ?: e.message ?: "").lowercase()
        val className = e::class.java.simpleName.lowercase()

        return when {
            message.contains("already registered") ||
            message.contains("already exists") ||
            message.contains("user already") ||
            message.contains("email address already") ||
            message.contains("email provider") ->
                "This email is already registered. Please log in instead."

            message.contains("invalid login") ||
            message.contains("invalid credentials") ||
            message.contains("bad credentials") ||
            message.contains("invalid email or password") ||
            message.contains("invalid_grant") ||
            message.contains("unauthorized") ->
                "Incorrect email or password. Please try again."

            message.contains("password") ||
            message.contains("weak") ||
            message.contains("short") ||
            message.contains("characters") ->
                "Password must be at least 6 characters and secure."

            message.contains("network") ||
            message.contains("host") ||
            message.contains("connection") ||
            message.contains("connect") ||
            message.contains("timeout") ||
            message.contains("offline") ||
            message.contains("unresolved") ||
            className.contains("unknownhost") ||
            className.contains("connectexception") ||
            className.contains("sockettimeout") ->
                "No internet connection. You can continue as a guest."

            else ->
                e.localizedMessage ?: "Authentication failed. Please check your details or network connection."
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                val supabase = SupabaseClientProvider.client
                supabase.auth.signOut()
            } catch (e: Exception) {
                if (e is CancellationException) throw e
            }
            _uiState.update {
                AuthUiState(isLoggedIn = false, isGuestMode = false)
            }
        }
    }

    fun continueAsGuest() {
        _uiState.update {
            it.copy(
                isGuestMode = true,
                isLoggedIn = false,
                currentUserEmail = "Guest (Offline Mode)",
                errorMessage = null
            )
        }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AuthViewModel() as T
            }
        }
    }
}
