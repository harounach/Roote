package com.harounach.roote.rider


import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth

import com.harounach.roote.R
import com.harounach.roote.databinding.FragmentRiderMapsBinding
import com.harounach.roote.login.LoginFragmentDirections
import com.harounach.roote.login.LoginViewModel
import timber.log.Timber

/**
 * A simple [Fragment] subclass.
 */
class RiderMapsFragment : Fragment(), OnMapReadyCallback {

    // Data binding object
    private lateinit var binding: FragmentRiderMapsBinding

    private lateinit var loginViewModel: LoginViewModel

    private lateinit var navController: NavController

    // GoogleMap object obtained when the map is ready
    private lateinit var googleMap: GoogleMap

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

        // Prepare MapView for asynchronous map update
        binding.riderMapView.getMapAsync(this)

        // Forward onCreate method to MapView
        binding.riderMapView.onCreate(savedInstanceState)

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


    /**
     *  Forward all life cycle methods to MapView
     * */

    override fun onStart() {
        super.onStart()

        // Forward onStart method to MapView
        binding.riderMapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        setUpLoginObservers()
        // Forward onResume to MapView
        binding.riderMapView.onResume()
    }

    override fun onPause() {
        super.onPause()

        // Forward onPause method to MapView
        binding.riderMapView.onPause()
    }

    override fun onStop() {
        super.onStop()

        // Forward onStop method to MapView
        binding.riderMapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()

        // Forward onDestroy method to MapView
        binding.riderMapView.onDestroy()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        // Forward onSaveInstanceState method to MapView
        binding.riderMapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()

        // Forward onLowMemory method to MapView
        binding.riderMapView.onLowMemory()
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

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        googleMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))

    }
}
