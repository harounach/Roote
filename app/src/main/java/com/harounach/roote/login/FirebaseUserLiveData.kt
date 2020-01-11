package com.harounach.roote.login

import androidx.lifecycle.LiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

/**
 * This class observes the current FirebaseUser. If there is no logged in user, FirebaseUser will
 * be null.
 * */
class FirebaseUserLiveData : LiveData<FirebaseUser?>() {

    // Firebase auth
    private val firebaseAuth = FirebaseAuth.getInstance()

    /**
     * Firebase auth state listener
     * */
    private val authStateListener = FirebaseAuth.AuthStateListener {
        value = it.currentUser
    }

    /**
     * Add [FirebaseAuth.AuthStateListener] when active
     * */
    override fun onActive() {
        firebaseAuth.addAuthStateListener(authStateListener)
    }

    /**
     * Remove [FirebaseAuth.AuthStateListener] when inactive
     * */
    override fun onInactive() {
        firebaseAuth.removeAuthStateListener(authStateListener)
    }
}