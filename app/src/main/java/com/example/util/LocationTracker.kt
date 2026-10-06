package com.example.util

import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import androidx.core.content.ContextCompat
import java.util.function.Consumer

object LocationTracker {

    fun hasLocationPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    @SuppressLint("MissingPermission")
    fun requestCurrentLocation(
        context: Context,
        onSuccess: (Double, Double) -> Unit,
        onFailure: (String) -> Unit
    ) {
        if (!hasLocationPermission(context)) {
            onFailure("Location permission not granted.")
            return
        }

        val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        if (lm == null) {
            onFailure("Location service not available.")
            return
        }

        // Try getting last known location first for immediate speed
        val lastGps = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER)
        val lastNetwork = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
        val bestLast = when {
            lastGps != null && lastNetwork != null -> if (lastGps.time > lastNetwork.time) lastGps else lastNetwork
            lastGps != null -> lastGps
            else -> lastNetwork
        }

        if (bestLast != null) {
            onSuccess(bestLast.latitude, bestLast.longitude)
        }

        // Also fetch fresh location
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val signal = CancellationSignal()
            lm.getCurrentLocation(
                LocationManager.FUSED_PROVIDER.takeIf { lm.allProviders.contains("fused") } ?: LocationManager.NETWORK_PROVIDER,
                signal,
                context.mainExecutor,
                Consumer { loc: Location? ->
                    if (loc != null) {
                        onSuccess(loc.latitude, loc.longitude)
                    } else if (bestLast == null) {
                        onFailure("Could not obtain GPS fix.")
                    }
                }
            )
        } else {
            val listener = object : LocationListener {
                override fun onLocationChanged(loc: Location) {
                    onSuccess(loc.latitude, loc.longitude)
                    lm.removeUpdates(this)
                }
                @Deprecated("Deprecated in Java")
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }

            val provider = if (lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                LocationManager.NETWORK_PROVIDER
            } else if (lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                LocationManager.GPS_PROVIDER
            } else {
                null
            }

            if (provider != null) {
                lm.requestSingleUpdate(provider, listener, context.mainLooper)
            } else if (bestLast == null) {
                onFailure("GPS and Network providers are disabled.")
            }
        }
    }
}
