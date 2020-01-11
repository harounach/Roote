package com.harounach.roote.login

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.map
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import com.harounach.roote.repository.RooteRepository
import timber.log.Timber
import java.lang.Exception

class LoginViewModel(application: Application) : AndroidViewModel(application) {


    // repository
    private var repository: RooteRepository = RooteRepository(application)

    // LiveData to observe login attempt success
    private val _loginEvent = MutableLiveData<Boolean>()
    val loginEvent: LiveData<Boolean>
        get() = _loginEvent

    // LiveData to inform user that something went wrong
    private val _errorReportEvent = MutableLiveData<String?>()
    val errorReportEvent: LiveData<String?>
        get() = _errorReportEvent

    val authenticationState = FirebaseUserLiveData().map { user ->
        if (user != null) {
            AuthenticationState.AUTHENTICATED
        } else {
            AuthenticationState.UNAUTHENTICATED
        }
    }

    // Initialization block
    init {
        _loginEvent.value = false
        _errorReportEvent.value = null
    }

    /**
     * Log user in firebase
     * @param email The user email
     * @param password The user password
     * */
    fun login(email: String, password: String) {
        // Login
        val loginTask = repository.login(email, password)
        loginTask.addOnCompleteListener {
            if (it.isSuccessful) {
                Timber.d("Login succeeded!")
                _loginEvent.value = true
            } else {
                Timber.e("Login failed")
            }
        }

    }

    /**
     * Login with google account
     * @param googleAccount The [GoogleSignInAccount] selected
     * */
    fun loginWithGoogle(googleAccount: GoogleSignInAccount) {
        Timber.d("firebaseAuthWithGoogle! ${googleAccount.idToken!!}")
        val googleCredentials = GoogleAuthProvider.getCredential(googleAccount.idToken, null)
        val googleSignInTask = repository.loginWithGoogle(googleCredentials)
        googleSignInTask.addOnCompleteListener {
            if (it.isSuccessful) {
                Timber.d("Login with google succeeded!")
                _loginEvent.value = true

                // create account in firebase database
                val googleUserName = googleAccount.displayName!!
                val googleEmail = googleAccount.email!!
                repository.createUserFromGoogleAccountInFirebase(
                    googleUserName,
                    googleEmail,
                    "rider"
                )
            } else {
                Timber.e("Login with google failed")
                // Handle exception
                it.exception?.apply {
                    handleException(this)
                }
            }
        }
    }

    /**
     * Reset loginEvent event
     * */
    fun loginEventDone() {
        _loginEvent.value = false
    }

    /**
     * Reset errorReportEvent event
     * */
    fun errorReportEventDone() {
        _errorReportEvent.value = null
    }

    /**
     * Check to see if the email is valid email
     * @param email
     *
     * @return Boolean
     * */
    fun isEmailValid(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Check to see if password is not empty
     * @param password
     *
     * @return Boolean
     * */
    fun isPasswordNotEmpty(password: String) : Boolean = password.isNotEmpty()

    /**
     * Check to see if password is at least 6 characters long
     * @param password
     *
     * @return Boolean
     * */
    fun isPasswordLongEnough(password: String) : Boolean = password.length >= 6

    /**
     * Handle different kind of exceptions thrown by firebase
     * @param exception The [Exception] thrown by firebase
     * */
    private fun handleException(exception: Exception) {
        if (exception is FirebaseAuthInvalidCredentialsException) {
            when ((exception as FirebaseAuthInvalidCredentialsException?)!!.errorCode) {
                "ERROR_INVALID_EMAIL" -> {
                    Timber.e("Invalid email!!!!!!!!")
                    // inform user with this error
                    _errorReportEvent.value = "Invalid email"
                }
            }
        } else if (exception is FirebaseAuthUserCollisionException) {
            when ((exception as FirebaseAuthUserCollisionException?)!!.errorCode) {
                "ERROR_EMAIL_ALREADY_IN_USE" -> {
                    Timber.e("Email already in use!")
                    // inform user with this error
                    _errorReportEvent.value = "Email already in use!"
                }
            }
        }
    }

    /**
     * Clear the viewModel
     * */
    override fun onCleared() {
        super.onCleared()
        Timber.d("ViewModel Cleared!")
    }

    /**
     * Enum to define authenticated and unauthenticated states
     * */
    enum class AuthenticationState {
        AUTHENTICATED, UNAUTHENTICATED
    }
}