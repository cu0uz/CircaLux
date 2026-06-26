package com.example.circalux.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager as AndroidLocationManager
import android.os.Bundle
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

/**
 * Standard Android Location Manager implementation (No Google Play Services dependency).
 */
class LocationManager(private val context: Context) {
    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as AndroidLocationManager

    private val _currentLocation = MutableStateFlow<Location?>(null)
    val currentLocation: StateFlow<Location?> = _currentLocation

    @SuppressLint("MissingPermission")
    suspend fun fetchLocation(): Location? {
        return try {
            val providers = locationManager.getProviders(true)
            var bestLocation: Location? = null
            
            // Try to get last known location from any available provider
            for (provider in providers) {
                val l = locationManager.getLastKnownLocation(provider) ?: continue
                if (bestLocation == null || l.accuracy < bestLocation.accuracy) {
                    bestLocation = l
                }
            }
            
            if (bestLocation != null) {
                _currentLocation.value = bestLocation
                bestLocation
            } else {
                // If no last known location, request a single update
                requestSingleUpdate()
            }
        } catch (e: Exception) {
            CircaLogger.e("Error fetching location", e, "LocationManager")
            null
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun requestSingleUpdate(): Location? = suspendCoroutine { continuation ->
        val provider = if (locationManager.isProviderEnabled(AndroidLocationManager.GPS_PROVIDER)) {
            AndroidLocationManager.GPS_PROVIDER
        } else {
            AndroidLocationManager.NETWORK_PROVIDER
        }

        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                locationManager.removeUpdates(this)
                _currentLocation.value = location
                continuation.resume(location)
            }
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        try {
            locationManager.requestSingleUpdate(provider, listener, null)
        } catch (e: Exception) {
            continuation.resume(null)
        }
    }

    @SuppressLint("MissingPermission")
    fun getLocationUpdates(intervalMillis: Long = 600000): Flow<Location> = callbackFlow {
        if (!hasLocationPermission()) {
            close()
            return@callbackFlow
        }

        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                _currentLocation.value = location
                trySend(location)
            }
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
            override fun onProviderEnabled(provider: String) {}
            override fun onProviderDisabled(provider: String) {}
        }

        val provider = if (locationManager.isProviderEnabled(AndroidLocationManager.GPS_PROVIDER)) {
            AndroidLocationManager.GPS_PROVIDER
        } else {
            AndroidLocationManager.NETWORK_PROVIDER
        }

        try {
            locationManager.requestLocationUpdates(
                provider,
                intervalMillis,
                1000f, // Update if moved > 1km (matching previous behavior)
                listener
            )
        } catch (e: SecurityException) {
            CircaLogger.e("SecurityException in getLocationUpdates", e, "LocationManager")
            close(e)
        }
        
        awaitClose {
            locationManager.removeUpdates(listener)
        }
    }

    private fun hasLocationPermission(): Boolean {
        return context.checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED ||
                context.checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }
}
