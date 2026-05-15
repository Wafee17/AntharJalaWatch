package com.example.anthar_jalawatch.util

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.tasks.await

class LocationHelper(private val context: Context) {

    private val fusedClient = LocationServices.getFusedLocationProviderClient(context)

    fun hasLocationPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    suspend fun getCurrentLocation(): Result<Location> {
        if (!hasLocationPermission()) {
            return Result.failure(SecurityException("Location permission not granted"))
        }
        return try {
            val cancellationToken = CancellationTokenSource()
            val location = fusedClient.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                cancellationToken.token
            ).await()
            if (location != null) {
                Result.success(location)
            } else {
                @Suppress("DEPRECATION")
                val last = fusedClient.lastLocation.await()
                if (last != null) Result.success(last)
                else Result.failure(Exception("Unable to determine location"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
