package com.harounach.roote.rider

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.firebase.geofire.*


import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.harounach.roote.R
import com.harounach.roote.databinding.ActivityRiderMapsBinding
import timber.log.Timber

class RiderMapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityRiderMapsBinding
    private lateinit var map: GoogleMap
    private lateinit var geoQuery: GeoQuery

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

        val geoFire = GeoFire(ref)




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
}
