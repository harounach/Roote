package com.harounach.roote.login


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController

import com.harounach.roote.R
import com.harounach.roote.databinding.FragmentLoginBinding

/**
 * A simple [Fragment] subclass.
 */
class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var loginViewModel: LoginViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)

        // instantiate the ViewModel
        loginViewModel = ViewModelProviders.of(this)[LoginViewModel::class.java]

        // Setup click listeners
        setUpClickListeners()

        // Setup observers
        setUpObservers()

        // Return root view
        return binding.root
    }

    private fun login() {
        // Grab the email and password from the corresponding text fields
        val userEmail = binding.emailEditText.text.toString().trim()
        val userPassword = binding.passwordEditText.text.toString().trim()

        /* Check that email and password are okay */
        val validCredentials = credentialValidation(userEmail, userPassword)

        if (validCredentials) {
            // Log user in
            loginViewModel.login(userEmail, userPassword)
        }
    }

    /**
     * Validate user name and email
     * @param email The user email
     * @param password The user password
     * */
    private fun credentialValidation(email: String, password: String) : Boolean {

        val validEmail = loginViewModel.isEmailValid(email)
        val isPasswordNotEmpty = loginViewModel.isPasswordNotEmpty(password)
        val isPasswordLongEnough = loginViewModel.isPasswordLongEnough(password)
        val validPassword = isPasswordNotEmpty && isPasswordLongEnough

        if (!validEmail) {
            binding.emailEditText.error =
                String.format(getString(R.string.error_invalid_email_not_valid), email)
        }

        if (!isPasswordLongEnough) {
            binding.passwordEditText.error =
                getString(R.string.error_invalid_password_not_valid)
        }

        if (!isPasswordNotEmpty) {
            binding.passwordEditText.error =
                getString(R.string.error_cannot_be_empty)
        }

        return validEmail && validPassword
    }

    /**
     * Set up observation relationships
     * */
    private fun setUpObservers() {
        /* observe user login */
        loginViewModel.loginEvent.observe(this, Observer {isLogin->
            if (isLogin) {
               showToast("Login successfully!")
                loginViewModel.loginEventDone()
            }
        })
    }

    private fun showToast(message: String) {
        Toast.makeText(activity!!, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Set up click listeners
     * */
    private fun setUpClickListeners() {
        // handle Login button click
        binding.loginButton.setOnClickListener {
            login()
        }

        // navigate to Registration fragment
        binding.createAccountButton.setOnClickListener {
            navigateToRegisterFragment()
        }
    }

    /**
     * Navigate to RegisterFragment
     * */
    private fun navigateToRegisterFragment() {
        val action = LoginFragmentDirections.actionLoginFragmentToRegisterFragment()
        this.findNavController().navigate(action)
    }


}
