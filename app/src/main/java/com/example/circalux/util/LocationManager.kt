package com.example.circalux.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Enhanced location manager to handle GPS updates and continuous tracking.
 */
class LocationManager(context: Context) {
    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation

    @SuppressLint("MissingPermission")
    suspend fun fetchLocation(): Location? {
        return try {
            val location = fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                null
            ).await()
            _currentLocation.value = location
            location
        } catch (e: Exception) {
            CircaLogger.e("Error fetching location", e, "LocationManager")
            null
        }
    }

    @SuppressLint("MissingPermission")
    fun getLocationUpdates(intervalMillis: Long = 600000): Flow<Location> = callbackFlow {
        val request = LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, intervalMillis)
            .setMinUpdateDistanceMeters(1000f) // Only update if moved > 1km
            .build()

        val callback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                result.lastLocation?.let {
                    _currentLocation.value = it
                    trySend(it)
                }
            }
        }

        fusedLocationClient.requestLocationUpdates(request, callback, Looper.getMainLooper())
        
        awaitClose {
            fusedLocationClient.removeLocationUpdates(callback)
        }
    }
}
