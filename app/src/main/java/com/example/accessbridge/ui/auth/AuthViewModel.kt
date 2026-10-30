package com.example.accessbridge.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.accessbridge.data.remote.SupabaseClientProvider
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.Email
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
            } catch (_: Exception) {
                // Ignore initialization error if Supabase credentials are placeholder
            }
        }
    }

    fun signIn(emailInput: String, passwordInput: String, onSuccess: () -> Unit) {
        if (emailInput.isBlank() || passwordInput.isBlank()) {
            _uiState.update { it.copy(errorMessage = "Please enter both email and password") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null, successMessage = null) }
            try {
                val supabase = SupabaseClientProvider.client
                supabase.auth.signInWith(Email) {
                    email = emailInput.trim()
                    password = passwordInput
                }
                val user = supabase.auth.currentUserOrNull()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        isGuestMode = false,
                        currentUserEmail = user?.email ?: emailInput,
                        successMessage = "Signed in successfully"
                    )
                }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.localizedMessage ?: "Sign in failed. Check network or credentials."
                    )
                }
            }
        }
    }

    fun signUp(emailInput: String, passwordInput: String, onSuccess: () -> Unit) {
        if (emailInput.isBlank() || passwordInput.isBlank()) {
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
                    email = emailInput.trim()
                    password = passwordInput
                }
                val user = supabase.auth.currentUserOrNull()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        isGuestMode = false,
                        currentUserEmail = user?.email ?: emailInput,
                        successMessage = "Account created successfully"
                    )
                }
                onSuccess()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = e.localizedMessage ?: "Sign up failed. Check network or try again."
                    )
                }
            }
        }
    }

    fun signOut() {
        viewModelScope.launch {
            try {
                val supabase = SupabaseClientProvider.client
                supabase.auth.signOut()
            } catch (_: Exception) {
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
