package com.harounach.roote.login

import android.app.Application
import android.util.Patterns
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.harounach.roote.repository.RooteRepository
import timber.log.Timber

class LoginViewModel(application: Application) : AndroidViewModel(application) {


    // repository
    private var repository: RooteRepository = RooteRepository(application)

    // LiveData to observe login attempt success
    private val _loginEvent = MutableLiveData<Boolean>()
    val loginEvent: LiveData<Boolean>
        get() = _loginEvent

    // Initialization block
    init {
        _loginEvent.value = false
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
     * Reset loginEvent event
     * */
    fun loginEventDone() {
        _loginEvent.value = false
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
     * Clear the viewModel
     * */
    override fun onCleared() {
        super.onCleared()
        Timber.d("ViewModel Cleared!")
    }
}