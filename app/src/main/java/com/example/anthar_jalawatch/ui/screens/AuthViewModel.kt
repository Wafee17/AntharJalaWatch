package com.example.anthar_jalawatch.ui.screens

import android.content.Context
import android.util.Log
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.anthar_jalawatch.BuildConfig
import com.example.anthar_jalawatch.data.repository.AuthRepository
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.security.MessageDigest
import java.util.UUID

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthViewModel : ViewModel() {
    private val authRepository = AuthRepository()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    fun resetState() {
        _authState.value = AuthState.Idle
    }

    fun loginWithEmail(email: String, pass: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = authRepository.signInWithEmail(email, pass)
            if (result.isSuccess) {
                _authState.value = AuthState.Success
            } else {
                _authState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "Login failed")
            }
        }
    }

    fun signUpWithEmail(email: String, pass: String, name: String, village: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            val result = authRepository.signUpWithEmail(email, pass, name, village)
            if (result.isSuccess) {
                _authState.value = AuthState.Success
            } else {
                _authState.value = AuthState.Error(result.exceptionOrNull()?.message ?: "Signup failed")
            }
        }
    }

    fun signInWithGoogle(context: Context) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            try {
                val credentialManager = CredentialManager.create(context)
                
                // Using a random nonce as recommended for security
                val rawNonce = UUID.randomUUID().toString()
                val bytes = rawNonce.toByteArray()
                val md = MessageDigest.getInstance("SHA-256")
                val digest = md.digest(bytes)
                val hashedNonce = digest.fold("") { str, it -> str + "%02x".format(it) }

                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(BuildConfig.WEB_CLIENT_ID)
                    .setNonce(hashedNonce)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(context, request)
                val credential = result.credential
                
                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                    val idToken = googleIdTokenCredential.idToken
                    
                    val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                    val authResult = authRepository.auth.signInWithCredential(firebaseCredential).await()
                    
                    val user = authResult.user
                    if (user != null) {
                        authRepository.saveGoogleUserToFirestoreIfNew(
                            uid = user.uid,
                            email = user.email ?: "",
                            fullName = user.displayName ?: ""
                        )
                        _authState.value = AuthState.Success
                    } else {
                        _authState.value = AuthState.Error("Google Sign-In failed: Null user")
                    }
                } else {
                    _authState.value = AuthState.Error("Unexpected credential type")
                }
            } catch (e: Exception) {
                Log.e("AuthViewModel", "Google Sign In Error", e)
                _authState.value = AuthState.Error("Google Sign-In failed: ${e.localizedMessage}")
            }
        }
    }
}
