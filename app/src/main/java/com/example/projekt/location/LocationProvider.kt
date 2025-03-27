package com.example.projekt.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.tasks.await

import java.util.Locale

class LocationProvider(context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)
    private val geocoder = Geocoder(context, Locale.getDefault())

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? {
        return try {
            fusedLocationClient.lastLocation.await()
        } catch (e: Exception) {
            null
        }
    }

    fun getCityFromCoordinates(location: Location?): String {
        location ?: return "Pardubice" // Výchozí město pokud nelze určit polohu

        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
        return addresses?.firstOrNull()?.locality ?: "Pardubice"
    }
}