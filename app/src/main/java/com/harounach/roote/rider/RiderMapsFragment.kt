package com.harounach.roote.rider


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.firebase.geofire.GeoQuery
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.harounach.roote.BuildConfig

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

    // ViewModels
    private lateinit var loginViewModel: LoginViewModel
    private lateinit var riderMapsViewModel: RiderMapsViewModel

    // NavController to navigate between destinations
    private lateinit var navController: NavController

    // GoogleMap object obtained when the map is ready
    private lateinit var googleMap: GoogleMap

    // Fused location provider client to communicate with Google location services
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    // We need a reference to the root view to show Snackbar
    private lateinit var rootView: View

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_rider_maps, container, false)

        // instantiate the ViewModels
        loginViewModel = ViewModelProviders.of(this)[LoginViewModel::class.java]
        riderMapsViewModel = ViewModelProviders.of(this)[RiderMapsViewModel::class.java]

        // Find navController
        navController = this.findNavController()

        // Prepare MapView for asynchronous map update
        binding.riderMapView.getMapAsync(this)

        // Forward onCreate method to MapView
        binding.riderMapView.onCreate(savedInstanceState)

        // We need the rootView to show Snackbar
        rootView = binding.root

        // Initialize database variables
        initFirebaseDatabase()

        // Setup click listeners
        setUpClickListeners()

        // Setup observers
        setUpObservers()

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

    /**
     * Log rider out
     * */
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
     * Set up login observation
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
     * Set up observation relationship
     * */
    private fun setUpObservers() {
        /* Observe rider location */
        riderMapsViewModel.riderLocationEvent.observe(this, Observer {location ->
            location?.let {
                showToast("Location was saved successfully")
                // Add Current location marker on the map
                val currentLatLng = LatLng(it.latitude, it.longitude)
                googleMap.addMarker(
                    MarkerOptions()
                        .position(currentLatLng)
                        .title("My Location")
                )
                // Initialize this event
                riderMapsViewModel.riderLocationEventDone()
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

    /**
     * Setup click listeners
     * */
    private fun setUpClickListeners() {
        // setup click listener for My Location Button
        binding.myLocationButton.setOnClickListener {
            // send rider's location to database
            if (!checkPermissions()) {
                requestPermissions()
            } else {
                getLastLocation()
            }
        }
    }

    /**
     * Initialize Firebase Real time database variables
     * */
    private fun initFirebaseDatabase() {
        // Location services client
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
    }

    /**
     * Get the last known location
     * */
    private fun getLastLocation() {
        // Get the rider location and save it to the data base
        riderMapsViewModel.getRiderLocation(fusedLocationProviderClient)
    }

    /**
     * Show information to the user with [Snackbar]
     * */
    private fun showSnackBar(
        snackStrId: Int,
        actionStrId: Int = 0,
        listener: View.OnClickListener? = null
    ) {
        val snackbar = Snackbar.make(
            rootView,
            getString(snackStrId), Snackbar.LENGTH_INDEFINITE
        )
        if (actionStrId != 0 && listener != null) {
            snackbar.setAction(getString(actionStrId), listener)
        }
        snackbar.show()
    }

    /**
     * Show information to the user with [Toast]
     *
     * @param message The message supplied
     * */
    private fun showToast(message: String) {
        Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Check if the permissions needed are granted
     * */
    private fun checkPermissions() =
        ActivityCompat.checkSelfPermission(
            requireActivity(),
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

    /**
     * Request permissions
     * */
    private fun startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            REQUEST_PERMISSIONS_REQUEST_CODE
        )
    }

    /**
     * Start the permissions request flow
     * */
    private fun requestPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )) {
            Timber.i("Displaying permission rationale to provide additional context.")
            showSnackBar(
                R.string.permission_rationale,
                android.R.string.ok,
                View.OnClickListener {
                    // Request permission
                    startLocationPermissionRequest()
                }
            )
        } else {
            Timber.i("Requesting permission")
            startLocationPermissionRequest()
        }
    }

    /**
     * Override onRequestPermissionsResult method to check if the permissions are granted
     * */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        Timber.i("")
        if (requestCode == REQUEST_PERMISSIONS_REQUEST_CODE) {
            when {
                grantResults.isEmpty() -> {
                    Timber.i("User interaction was cancelled.")
                }
                (grantResults[0] == PackageManager.PERMISSION_GRANTED) -> {
                    getLastLocation()
                }

                else -> {
                    showSnackBar(
                        R.string.permission_denied_explanation,
                        R.string.settings,
                        View.OnClickListener {
                            val intent = Intent().apply {
                                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                data = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null)
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            startActivity(intent)
                        }
                    )
                }
            }
        }

    }

    companion object {
        const val REQUEST_PERMISSIONS_REQUEST_CODE = 34
    }

}
