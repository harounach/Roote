package com.harounach.roote.rider

import android.app.Application
import android.location.Location
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.firebase.geofire.GeoFire
import com.firebase.geofire.GeoLocation
import com.google.android.gms.location.FusedLocationProviderClient
import com.harounach.roote.R
import com.harounach.roote.repository.RooteRepository
import timber.log.Timber


/**
 * Architecture component ViewModel to handle all logic needed by [RiderMapsFragment]
 * */
class RiderMapsViewModel(application: Application) : AndroidViewModel(application) {

    // App repository
    private var repository: RooteRepository = RooteRepository(application)

    // LiveData to observe rider's location
    private val _riderLocationEvent = MutableLiveData<Location?>()
    val riderLocationEvent: LiveData<Location?>
        get() = _riderLocationEvent

    // Initialization block
    init {
        _riderLocationEvent.value = null
    }

    /**
     * Reset RiderLocation event
     * */
    fun riderLocationEventDone() {
        _riderLocationEvent.value = null
    }

    /**
     * Get Rider's last known location
     * @param fusedLocationProviderClient The [FusedLocationProviderClient]
     * */
    fun getRiderLocation(
        fusedLocationProviderClient: FusedLocationProviderClient
    ) {

        // Get rider's location task
        val locationTask = repository.getRiderLocation(fusedLocationProviderClient)

        locationTask.addOnCompleteListener {
            if (it.isSuccessful && it.result != null) {
                // There is location
                Timber.d("Getting rider location was successful")
                val riderLocation = it.result!!

                _riderLocationEvent.value = riderLocation

                // Send rider location to database
                repository.sendRiderLocation(riderLocation)


            } else {
                // There is no location known
                Timber.e("Getting rider location failed!")
                _riderLocationEvent.value = null

            }
        }

    }

    override fun onCleared() {
        super.onCleared()
        Timber.d("ViewModel cleared!")
    }
}