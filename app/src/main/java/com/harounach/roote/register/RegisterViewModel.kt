package com.harounach.roote.register

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.FirebaseError
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthEmailException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.harounach.roote.repository.RooteRepository
import timber.log.Timber
import java.lang.Exception
import java.math.BigInteger
import java.security.SecureRandom

class RegisterViewModel(application: Application): AndroidViewModel(application) {

    /**
     * a Random password generated after creating account for the first time
     * */
    private var password: String? = null

    // Random to generate random password
    private var random = SecureRandom()

    // repository
    private var repository: RooteRepository = RooteRepository(application)

    // LiveData to observe account creation
    private val _createAccountEvent = MutableLiveData<Boolean>()
    val createAccountEvent: LiveData<Boolean>
            get() = _createAccountEvent

    // Initialization block
    init {
        _createAccountEvent.value = false
    }

    /**
     * Create account using userName and email supplied
     * @param userName The user name
     * @param email The email
     * */
    fun onCreateAccount(userName: String, email: String) {
        // Generate random password
        password = BigInteger(130, random).toString(32)

        //create account
        val accountTask = repository.createAccountWithEmailAndPassword(email, "haroun")

        accountTask.addOnCompleteListener {
            if (it.isSuccessful) {
                Timber.d("Creating account successfully!")
                // create account successfully
                _createAccountEvent.value = true


            } else {
                // Creating account failed
                Timber.e("Creating account failed! : ${it.exception?.message} : $email")
                handleException(accountTask.exception)
            }
        }



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
     * Check to see if user name is valid
     * @param userName
     *
     * @return Boolean
     * */
    fun isUserNameValid(userName: String): Boolean =
        userName.isNotEmpty()

    /**
     * Reset createAccountEvent event
     * */
    fun createAccountEventDone() {
        _createAccountEvent.value = false
    }

    private fun handleException(exception: Exception?) {
        if (exception is FirebaseAuthInvalidCredentialsException?) {
            when ((exception as FirebaseAuthInvalidCredentialsException?)!!.errorCode) {
                "ERROR_INVALID_EMAIL" -> {
                    Timber.e("Invalid email!!!!!!!!")
                }
            }
        } else if (exception is FirebaseAuthUserCollisionException?) {
            when ((exception as FirebaseAuthUserCollisionException?)!!.errorCode) {
                "ERROR_EMAIL_ALREADY_IN_USE" -> {
                    Timber.e("Email already in use!")
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

    enum class FirebaseAuthExceptionType {

    }
}