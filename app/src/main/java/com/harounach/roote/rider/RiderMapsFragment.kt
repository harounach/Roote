package com.harounach.roote.rider


import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth

import com.harounach.roote.R
import com.harounach.roote.databinding.FragmentRiderMapsBinding
import com.harounach.roote.login.LoginFragmentDirections
import com.harounach.roote.login.LoginViewModel
import timber.log.Timber

/**
 * A simple [Fragment] subclass.
 */
class RiderMapsFragment : Fragment() {

    // Data binding object
    private lateinit var binding: FragmentRiderMapsBinding

    private lateinit var loginViewModel: LoginViewModel

    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_rider_maps, container, false)

        // instantiate the ViewModel
        loginViewModel = ViewModelProviders.of(this)[LoginViewModel::class.java]

        // Find navController
        navController = this.findNavController()

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        // Specify that this fragment will have an option menu
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.rider_maps_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
                R.id.action_log_out -> {
                    logOut()
                    true
                }
                else -> {
                    super.onOptionsItemSelected(item)
                }
            }

    }

    private fun logOut() {
        FirebaseAuth.getInstance().signOut()
    }


    override fun onResume() {
        super.onResume()
        setUpLoginObservers()
    }

    /**
     * Set up observation relationships
     * */
    private fun setUpLoginObservers() {
        /* observe auth login */
        loginViewModel.authenticationState.observe(this, Observer {authenticationState->
            if (authenticationState == LoginViewModel.AuthenticationState.UNAUTHENTICATED) {
                // User is not authenticated, they should login before seeing this screen
                navigateToLoginFragment()
            }
        })
    }

    /**
     * Navigate to LoginFragment
     * */
    private fun navigateToLoginFragment() {
        val action = RiderMapsFragmentDirections.actionRiderMapsFragmentToLoginFragment()
        if (navController.currentDestination!!.id == R.id.riderMapsFragment) {
            navController.navigate(action)
        }

    }
}
