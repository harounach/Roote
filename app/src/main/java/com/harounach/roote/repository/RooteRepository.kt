package com.harounach.roote.repository

import android.content.Context
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.AuthCredential
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.harounach.roote.database.model.User
import com.harounach.roote.utils.FIREBASE_LOCATION_USERS
import com.harounach.roote.utils.FIREBASE_URL_USERS
import com.harounach.roote.utils.Utils
import timber.log.Timber

class RooteRepository(val context: Context) {

    val firebaseRootRef = FirebaseDatabase.getInstance().reference

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

    /**
     * Login with google credential
     * @param credential The google [AuthCredential]
     *
     * @return Task<AuthResult>
     * */
    fun loginWithGoogle(credential: AuthCredential) : Task<AuthResult>{
        return FirebaseAuth
                .getInstance()
                .signInWithCredential(credential)
    }

    /**
     * Create user with email/password in firebase database
     * @param userName The user name
     * @param email The user email used as the database's node key
     * @param accountType
     * */
    fun createUserFromEmailPasswordInFirebase(userName: String, email: String, accountType: String) {
        // Encode the email
        val encodedUserEmail = Utils.encodeEmail(email)

        // "users" database reference
        val userLocationRef = FirebaseDatabase
            .getInstance()
            .getReference(FIREBASE_URL_USERS)
            .child(encodedUserEmail)

        /**
         * See if there is already a user (for example, if they already logged in with an associated
         * Google account.
         */
        userLocationRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                /* If there is no user, make one */
                if (snapshot.value == null) {
                    val newUser = User(userName, encodedUserEmail, accountType)

                    // Create user in database
                    firebaseRootRef
                        .child(FIREBASE_LOCATION_USERS)
                        .child(encodedUserEmail)
                        .setValue(newUser)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Timber.e("Creating user in firebase failed")
            }

        })
    }

    /**
     * Create user with Google account in firebase database
     * @param userName The user name
     * @param email The user email used as the database's node key
     * @param accountType
     * */
    fun createUserFromGoogleAccountInFirebase(userName: String, email: String, accountType: String) {
        // Encode the email
        val encodedUserEmail = Utils.encodeEmail(email)

        // "users" database reference
        val userLocationRef = FirebaseDatabase
            .getInstance()
            .getReference(FIREBASE_URL_USERS)
            .child(encodedUserEmail)

        /**
         * See if there is already a user (for example, if they already logged in with an associated
         * Google account.
         */
        userLocationRef.addListenerForSingleValueEvent(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                /* If there is no user, make one */
                if (snapshot.value == null) {
                    val newUser = User(userName, encodedUserEmail, accountType)

                    // Create user in database
                    firebaseRootRef
                        .child(FIREBASE_LOCATION_USERS)
                        .child(encodedUserEmail)
                        .setValue(newUser)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Timber.e("Creating user in firebase failed")
            }

        })
    }
}