package com.harounach.roote

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.ActionBar
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupWithNavController
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.PlacesClient
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.harounach.roote.databinding.ActivityMainBinding
import com.harounach.roote.login.LoginFragmentDirections
import com.harounach.roote.login.LoginViewModel
import com.harounach.roote.rider.RiderMapsFragmentDirections
import timber.log.Timber
import java.util.*


class MainActivity : AppCompatActivity() {

    // Login viewModel
    private lateinit var loginViewModel: LoginViewModel

    // NavController
    private lateinit var navController: NavController

    // AppBarConfiguration
    private lateinit var appBarConfiguration: AppBarConfiguration

    // DataBinding object
    lateinit var binding: ActivityMainBinding

    // Places API client
    private lateinit var placesClient: PlacesClient


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        // instantiate the ViewModel
        loginViewModel = ViewModelProviders.of(this)[LoginViewModel::class.java]

        // Get the NavController
        navController = findNavController(R.id.myNavHostFragment)

        appBarConfiguration = AppBarConfiguration(navController.graph)

        // Set up Toolbar with NavController
        binding.toolBar.setupWithNavController(navController, appBarConfiguration)


        // Setup ActionBar
        val toolbar = binding.toolBar
        setSupportActionBar(toolbar)

        // Initialize the Places API
        initPlacesApi()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.action_search -> {
                // Launch the Places Autocomplete Activity
                launchPlacesAutocompleteActivity()
                true
            }
            else -> {
                super.onOptionsItemSelected(item)
            }
        }


    }

    private fun initPlacesApi() {
        // Inittialize the SDK
        Places.initialize(applicationContext, getString(R.string.google_maps_key))

        // Create a new Places client instance
        placesClient = Places.createClient(this)
    }

    /**
     * Start the Places API Autocomplete widget
     * */
    private fun launchPlacesAutocompleteActivity() {

        // Set the fields to specify which types of place data to
        // return after the user has made a selection.
        val fields: List<Place.Field> =
            listOf(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG)

        // Start the autocomplete intent.
        val intent =
            Autocomplete.IntentBuilder(AutocompleteActivityMode.OVERLAY, fields)
                .build(this)

        startActivityForResult(intent, AUTOCOMPLETE_REQUEST_CODE)

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
            when (resultCode) {
                Activity.RESULT_OK -> {
                    val place = Autocomplete.getPlaceFromIntent(data!!)
                    Timber.d("Place: Name[${place.name}], ID[${place.id}], LatLng[${place.latLng}]")
                }
                AutocompleteActivity.RESULT_ERROR -> {
                    // Handle the error
                    val status = Autocomplete.getStatusFromIntent(data!!)
                    Timber.d("Autocomplete Status: ${status.statusMessage!!}")
                }
                Activity.RESULT_CANCELED -> {
                    // The user canceled the operation.
                }
            }
        }
    }

    companion object {
        const val AUTOCOMPLETE_REQUEST_CODE = 1
    }
}
