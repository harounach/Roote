package com.harounach.roote.login


import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.addCallback
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException

import com.harounach.roote.R
import com.harounach.roote.databinding.FragmentLoginBinding
import com.harounach.roote.rider.RiderMapsFragmentDirections
import timber.log.Timber

/**
 * A simple [Fragment] subclass.
 */
class LoginFragment : Fragment() {

    private lateinit var binding: FragmentLoginBinding
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)

        // instantiate the ViewModel
        loginViewModel = ViewModelProviders.of(this)[LoginViewModel::class.java]

        // Find navController
        navController = this.findNavController()

        // Setup click listeners
        setUpClickListeners()

        // Setup observers
        setUpObservers()

        // Exit the app if user ignore login
        setUpOnBackButtonPressed()


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

        /* Observe login state */
        loginViewModel.authenticationState.observe(this, Observer {authenticationState ->
            if (authenticationState == LoginViewModel.AuthenticationState.AUTHENTICATED) {
                // user is authenticated navigate to rider maps fragment
                navigateToRiderMapsFragment()
            }
        })

        /* observe error reporting */
        loginViewModel.errorReportEvent.observe(this, Observer { errorMessage->
            errorMessage?.let {
                showToast(it)
                loginViewModel.errorReportEventDone()
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

        // handle google sign in button click
        binding.loginWithGoogleButton.setOnClickListener {
            onSignInGooglePressed()
        }
    }

    /**
     * Exit the app if user ignore login
     * */
    private fun setUpOnBackButtonPressed() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            requireActivity().finish()
        }
    }

    /**
     * Launch google sign in flow
     * */
    private fun onSignInGooglePressed() {
        // Configure Google Sign In
        val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.server_client_id))
            .requestEmail()
            .build()

        val googleSignInClient = GoogleSignIn.getClient(activity!!, googleSignInOptions)
        val signInIntent = googleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_GOOGLE_LOGIN)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // Result returned from launching the Intent from GoogleSignInApi.signInIntent
        if (requestCode == RC_GOOGLE_LOGIN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data!!)
            try {
                val googleAccount = task.getResult(ApiException::class.java)
                loginWithGoogle(googleAccount!!)
            } catch (e: ApiException) {
                Timber.w("sign In With Google failed : ${e.message}")
            }
        }
    }

    private fun loginWithGoogle(googleAccount: GoogleSignInAccount) {
        loginViewModel.loginWithGoogle(googleAccount)
    }

    /**
     * Navigate to RegisterFragment
     * */
    private fun navigateToRegisterFragment() {
        val action = LoginFragmentDirections.actionLoginFragmentToRegisterFragment()
        this.findNavController().navigate(action)
    }

    /**
     * Navigate to RiderMapsFragment
     * */
    private fun navigateToRiderMapsFragment() {
        navController.popBackStack(R.id.riderMapsFragment, false)
    }

    companion object {
        /**
         * Request code for google sign in
         * */
        const val RC_GOOGLE_LOGIN = 1
    }


}
