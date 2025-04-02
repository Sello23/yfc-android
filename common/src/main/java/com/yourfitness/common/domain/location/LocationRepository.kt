package com.yourfitness.common.domain.location

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class LocationRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    suspend fun lastLocation(): LatLng {
        return try {
            getDeviceLocation().toLatLng()
        } catch (error: Exception) {
            DEFAULT_LOCATION
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun getDeviceLocation(): Location = suspendCoroutine {
        val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
        val locationResult = fusedLocationProviderClient.lastLocation
        locationResult.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                it.resume(task.result)
            } else {
                it.resumeWithException(task.exception!!)
            }
        }
    }
}

private val DEFAULT_LOCATION = LatLng(53.9466932, 27.6793845)

fun Location.toLatLng(): LatLng {
    return LatLng(latitude, longitude)
}
