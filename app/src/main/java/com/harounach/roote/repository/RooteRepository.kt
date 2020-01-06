package com.harounach.roote.repository

import android.content.Context
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth

class RooteRepository(val context: Context) {

    /**
     * Create new user with the specified email and password
     * @param email The user email
     * @param password The generated password
     *
     * @return Task<AuthResult>
     */
    fun createAccountWithEmailAndPassword(email: String, password: String) : Task<AuthResult>{
        return FirebaseAuth.getInstance().createUserWithEmailAndPassword(email, password)
    }

    /**
     * Send the password reset email after creating an account
     *
     * @param email The user email
     *
     * @return Task<Void>
     * */
    fun sendPasswordResetEmail(email: String) : Task<Void> {
        return FirebaseAuth.getInstance().sendPasswordResetEmail(email)
    }

    /**
     * Sign in the user
     *
     * @param email The user email
     * @param password The user password
     *
     * @return Task<AuthResult>
     * */
    fun login(email: String, password: String) : Task<AuthResult> {
        return FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
    }
}