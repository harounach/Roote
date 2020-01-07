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

    // repository
    private var repository: RooteRepository = RooteRepository(application)

    // LiveData to observe account creation
    private val _createAccountEvent = MutableLiveData<Boolean>()
    val createAccountEvent: LiveData<Boolean>
            get() = _createAccountEvent

    // LiveData to inform user that something went wrong
    private val _errorReportEvent = MutableLiveData<String?>()
    val errorReportEvent: LiveData<String?>
        get() = _errorReportEvent

    // Initialization block
    init {
        _createAccountEvent.value = false
        _errorReportEvent.value = null
    }

    /**
     * Create account using userName and email supplied
     * @param userName The user name
     * @param email The email
     * @param password The password
     * */
    fun onCreateAccount(userName: String, email: String, password: String) {

        //create account
        val accountTask = repository.createAccountWithEmailAndPassword(email, password)

        accountTask.addOnCompleteListener {
            if (it.isSuccessful) {

                // create account in firebase database
                repository.createUserFromEmailPasswordInFirebase(
                    userName,
                    email,
                    "rider"
                )

                Timber.d("Creating account successfully!")
                // create account successfully
                _createAccountEvent.value = true



            } else {
                // Creating account failed
                Timber.e("Creating account failed! : ${it.exception?.message} : $email")
                accountTask.exception?.let {excep->
                    handleException(excep)
                }
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
    fun isPasswordLongEnough(password: String) : Boolean {
        return password.length >= 6
    }



    /**
     * Reset createAccountEvent event
     * */
    fun createAccountEventDone() {
        _createAccountEvent.value = false
    }

    /**
     * Reset errorReportEvent event
     * */
    fun errorReportEventDone() {
        _errorReportEvent.value = null
    }

    private fun handleException(exception: Exception?) {
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

    enum class FirebaseAuthExceptionType {

    }
}