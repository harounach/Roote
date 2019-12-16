package com.harounach.roote.rider


import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.databinding.DataBindingUtil
import com.firebase.geofire.*
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices

import com.google.android.material.snackbar.Snackbar.LENGTH_INDEFINITE
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.harounach.roote.BuildConfig.APPLICATION_ID
import com.harounach.roote.R
import com.harounach.roote.databinding.ActivityRiderMapsBinding
import timber.log.Timber

class RiderMapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityRiderMapsBinding
    private lateinit var map: GoogleMap
    private lateinit var geoQuery: GeoQuery
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var geoFire: GeoFire


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_rider_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // database reference
        val database = Firebase.database
        val ref: DatabaseReference = database.getReference("rider/geofire")

        geoFire = GeoFire(ref)

        // Location services client
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)



        // Handle send location button click
        binding.sendLocation.setOnClickListener {
            geoFire.setLocation(
                "haroun-hq",
                GeoLocation(35.195016, 4.918147)
            ) { key, error ->
                if (error != null) {
                    Timber.d("There was an error saving the location to GeoFire: $error")
                } else {
                    Timber.d("Location saved on server successfully!")
                }
            }
        }
        // Hand send another location button click
        binding.sendAnotherLocation.setOnClickListener {
            geoFire.setLocation(
                "haroun-another-hq",
                GeoLocation(35.195015, 4.918146)
            ) { key, error ->
                if (error != null) {
                    Timber.d("There was an error saving the location to GeoFire: $error")
                } else {
                    Timber.d("Location saved on server successfully!")
                }
            }
        }

        // Handle get location button click
        binding.getLocation.setOnClickListener {
            geoFire.getLocation(
                "haroun-hq",
                object : LocationCallback {
                    override fun onLocationResult(key: String?, location: GeoLocation?) {

                        if (location != null) {
                            Timber.d(
                                String.format(
                                    "The location for key %s is [%f,%f]",
                                    key,
                                    location.latitude,
                                    location.longitude)
                            )
                        } else {
                            Timber.d("No location found for key: $key")
                        }

                    }

                    override fun onCancelled(databaseError: DatabaseError?) {
                        Timber.d(String.format("There was an error getting the GeoFire location: $databaseError"))
                    }

                }
                )
        }
        // Handle query locations button click
        binding.queryLocations.setOnClickListener {
            geoQuery = geoFire.queryAtLocation(
                GeoLocation(35.195017, 4.918148),
                0.6
            )

            geoQuery.addGeoQueryEventListener(object: GeoQueryEventListener {
                override fun onGeoQueryReady() {
                    Timber.d("All initial data has been loaded and events have been fired!")
                }

                override fun onKeyEntered(key: String?, location: GeoLocation?) {
                    location?.let {
                        Timber.d(
                            String.format(
                                "Key %s entered the search area at [%f,%f]",
                                key,
                                it.latitude,
                                it.longitude)
                        )
                    }
                }

                override fun onKeyMoved(key: String?, location: GeoLocation?) {
                    location?.let {
                        Timber.d(
                            String.format(
                                "Key %s moved within the search area to [%f,%f]",
                                key,
                                it.latitude,
                                it.longitude)
                        )
                    }
                }

                override fun onKeyExited(key: String?) {
                    Timber.d(String.format("Key %s is no longer in the search area", key))
                }

                override fun onGeoQueryError(error: DatabaseError?) {
                    Timber.e("There was an error with this query: $error")
                }

            }
            )

        }


        // Send my location to database
        binding.myLocation.setOnClickListener {
            if (!checkPermissions()) {
                requestPermissions()
            } else {
                getLastLocation()
            }
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
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        map.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        map.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    override fun onDestroy() {
        super.onDestroy()
        geoQuery?.removeAllListeners()
    }


    private fun getLastLocation() {
        fusedLocationProviderClient.lastLocation.addOnCompleteListener { taskLocation ->
            if (taskLocation.isSuccessful && taskLocation.result != null) {
                val location = taskLocation.result
                location?.let {
                    geoFire.setLocation(
                        "myLocation",
                        GeoLocation(it.latitude, it.longitude)
                    ) { key, error ->
                        if (error != null) {
                            Timber.d("There was an error saving the location to GeoFire: $error")
                        } else {
                            Timber.d("Location saved on server successfully!")
                        }
                    }
                }


            } else {
                Timber.w("getLastLocation:${taskLocation.exception}")
                showSnackBar(R.string.no_location_detected)
            }
        }
    }


    private fun showSnackBar(
        snackStrId: Int,
        actionStrId: Int = 0,
        listener: View.OnClickListener? = null
    ) {
        val snackbar = Snackbar.make(
            findViewById<ViewGroup>(android.R.id.content),
            getString(snackStrId), LENGTH_INDEFINITE)
        if (actionStrId != 0 && listener != null) {
            snackbar.setAction(getString(actionStrId), listener)
        }
        snackbar.show()
    }

    private fun checkPermissions() =
        ActivityCompat.checkSelfPermission(
            this,
            ACCESS_FINE_LOCATION
        ) == PERMISSION_GRANTED

    private fun startLocationPermissionRequest() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(ACCESS_FINE_LOCATION),
            REQUEST_PERMISSIONS_REQUEST_CODE
        )
    }

    private fun requestPermissions() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(this, ACCESS_FINE_LOCATION)) {
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
                (grantResults[0] == PERMISSION_GRANTED) -> {
                    getLastLocation()
                }

                else -> {
                    showSnackBar(
                        R.string.permission_denied_explanation,
                        R.string.settings,
                        View.OnClickListener {
                            val intent = Intent().apply {
                                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                data = Uri.fromParts("package", APPLICATION_ID, null)
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
