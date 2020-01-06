package com.harounach.roote.register


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController

import com.harounach.roote.R
import com.harounach.roote.databinding.FragmentRegisterBinding

/**
 * A simple [Fragment] subclass.
 */
class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding
    private lateinit var registerViewModel: RegisterViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_register, container, false)

        // instantiate the ViewModel
        registerViewModel = ViewModelProviders.of(this)[RegisterViewModel::class.java]

        // Setup click listeners
        setUpClickListeners()


        // Setup observers
        setUpObservers()

        // return the root view
        return binding.root
    }

    private fun createAccount() {
        /* Grab the user name and email from the corresponding text fields*/
        val userName = binding.usernameEditText.text.toString().trim()
        val userEmail = binding.emailEditText.text.toString().trim()

        /* Check that user name and email are okay */
         val validCredentials = credentialValidation(userName, userEmail)

        if (validCredentials) {
            // Creating account
            registerViewModel.onCreateAccount(userName, userEmail)
        }

    }

    /**
     * Validate user name and email
     * @param userName The user name
     * @param userEmail The user email
     * */
    private fun credentialValidation(userName: String, userEmail: String) : Boolean {
        val validUserName = registerViewModel.isUserNameValid(userName)
        val validEmail = registerViewModel.isEmailValid(userEmail)

        if (!validUserName) {
            binding.usernameEditText.error =
                getString(R.string.error_cannot_be_empty)
        }

        if (!validEmail) {
            binding.emailEditText.error =
                String.format(getString(R.string.error_invalid_email_not_valid), userEmail)
        }

        return validUserName && validEmail
    }

    /**
     * Set up observation relationships
     * */
    private fun setUpObservers() {
        /* observe account creation */
        registerViewModel.createAccountEvent.observe(this, Observer {accountCreated ->
            if (accountCreated) {
                showToast("Account created successfully!")
                registerViewModel.createAccountEventDone()
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
        // Set on click listener on Create account button
        binding.createAccountButton.setOnClickListener {
            createAccount()
        }

        // navigate to Login fragment
        binding.loginButton.setOnClickListener {
            navigateToLoginFragment()
        }
    }

    /**
     * Navigate to LoginFragment
     * */
    private fun navigateToLoginFragment() {
        this.findNavController().popBackStack()
    }


}
