package com.hfad.pet_scheduling.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class AuthViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()

    private val _currentUser = MutableLiveData<FirebaseUser?>()
    val currentUser: LiveData<FirebaseUser?> = _currentUser

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _isAuthenticated = MutableLiveData<Boolean>()
    val isAuthenticated: LiveData<Boolean> = _isAuthenticated

    init {
        // Observe auth state changes
        auth.addAuthStateListener { firebaseAuth ->
            val user = firebaseAuth.currentUser
            _currentUser.value = user
            _isAuthenticated.value = user != null
        }
    }

    /**
     * Sign in with email and password
     */
    fun signIn(email: String, password: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                val result = auth.signInWithEmailAndPassword(email, password).await()
                _currentUser.value = result.user
                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = "Sign in failed: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    /**
     * Sign up with email and password
     */
    fun signUp(email: String, password: String, displayName: String? = null) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                val result = auth.createUserWithEmailAndPassword(email, password).await()
                
                // Update display name if provided
                displayName?.let { name ->
                    result.user?.updateProfile(
                        com.google.firebase.auth.UserProfileChangeRequest.Builder()
                            .setDisplayName(name)
                            .build()
                    )?.await()
                }
                
                _currentUser.value = result.user
                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = "Sign up failed: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    /**
     * Sign out
     */
    fun signOut() {
        viewModelScope.launch {
            try {
                auth.signOut()
                _currentUser.value = null
                _isAuthenticated.value = false
            } catch (e: Exception) {
                _errorMessage.value = "Sign out failed: ${e.message}"
            }
        }
    }

    /**
     * Send password reset email
     */
    fun sendPasswordResetEmail(email: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                auth.sendPasswordResetEmail(email).await()
                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = "Failed to send reset email: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    /**
     * Update user display name
     */
    fun updateDisplayName(displayName: String) {
        viewModelScope.launch {
            try {
                val user = auth.currentUser
                if (user != null) {
                    _isLoading.value = true
                    user.updateProfile(
                        com.google.firebase.auth.UserProfileChangeRequest.Builder()
                            .setDisplayName(displayName)
                            .build()
                    ).await()
                    _currentUser.value = user
                    _isLoading.value = false
                } else {
                    _errorMessage.value = "No user signed in"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update display name: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    /**
     * Get current user ID
     */
    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }

    /**
     * Get current user email
     */
    fun getCurrentUserEmail(): String? {
        return auth.currentUser?.email
    }

    /**
     * Check if user is authenticated
     */
    fun checkAuthentication(): Boolean {
        return auth.currentUser != null
    }


    /**
     * Sign in with Apple
     * Note: Apple Sign-In requires additional setup in Firebase Console
     * and works best on devices with iOS 13+ or Android 5.0+
     */
    fun signInWithApple(idToken: String, nonce: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                val credential = com.google.firebase.auth.OAuthProvider.newCredentialBuilder("apple.com")
                    .setIdToken(idToken)
                    .build()
                
                val result = auth.signInWithCredential(credential).await()
                _currentUser.value = result.user
                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = "Apple sign in failed: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }
}

