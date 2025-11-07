package com.hfad.pet_scheduling.utils

import android.app.Activity
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

class GoogleSignInHelper(private val activity: Activity) {
    private var googleSignInClient: GoogleSignInClient? = null

    /**
     * Initialize Google Sign-In with web client ID
     * Get this from Firebase Console > Authentication > Sign-in method > Google > Web SDK configuration
     */
    fun initialize(webClientId: String) {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(webClientId)
            .requestEmail()
            .build()
        
        googleSignInClient = GoogleSignIn.getClient(activity, gso)
    }

    /**
     * Get the sign-in intent
     */
    fun getSignInIntent(): Intent? {
        return googleSignInClient?.signInIntent
    }

    /**
     * Handle the sign-in result
     */
    fun handleSignInResult(data: Intent?): GoogleSignInAccount? {
        return try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            task.getResult(ApiException::class.java)
        } catch (e: ApiException) {
            null
        }
    }

    /**
     * Sign out from Google
     */
    fun signOut() {
        googleSignInClient?.signOut()
    }
}

