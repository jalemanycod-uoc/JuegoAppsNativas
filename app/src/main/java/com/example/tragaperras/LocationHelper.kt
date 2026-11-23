package com.example.tragaperras

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

// Helper para gestionar la obtención de la ubicación con FusedLocationProviderClient
class LocationHelper(private val context: Context) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    // Obtiene la ubicación actual utilizando la prioridad PRIORITY_BALANCED_POWER_ACCURACY
    fun getCurrentLocation(onLocationResult: (Double?, Double?) -> Unit) {
        val hasFineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val hasCoarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFineLocation && !hasCoarseLocation) {
            onLocationResult(null, null)
            return
        }

        try {
            val cancellationTokenSource = CancellationTokenSource()
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_BALANCED_POWER_ACCURACY,
                cancellationTokenSource.token
            ).addOnSuccessListener { location: Location? ->
                if (location != null) {
                    onLocationResult(location.latitude, location.longitude)
                } else {
                    fusedLocationClient.lastLocation.addOnSuccessListener { lastLoc: Location? ->
                        if (lastLoc != null) {
                            onLocationResult(lastLoc.latitude, lastLoc.longitude)
                        } else {
                            onLocationResult(null, null)
                        }
                    }.addOnFailureListener {
                        onLocationResult(null, null)
                    }
                }
            }.addOnFailureListener {
                onLocationResult(null, null)
            }
        } catch (_: SecurityException) {
            onLocationResult(null, null)
        }
    }
}
