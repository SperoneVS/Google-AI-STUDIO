package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.BuildConfig

object GoogleMapsHelper {

    /**
     * Returns the configured Google Maps API Key from BuildConfig (via Secrets panel / .env),
     * or null if it is empty or set to a placeholder.
     */
    fun getValidApiKey(): String? {
        val key = BuildConfig.GOOGLE_MAPS_API_KEY.trim()
        return if (
            key.isBlank() ||
            key == "DEFAULT_GOOGLE_MAPS_KEY" ||
            key.contains("PLACEHOLDER", ignoreCase = true) ||
            key.contains("YOUR_", ignoreCase = true)
        ) {
            null
        } else {
            key
        }
    }

    /**
     * Builds a Google Maps Static API URL for satellite/hybrid/terrain preview of a campsite.
     * Uses BuildConfig.GOOGLE_MAPS_API_KEY when configured.
     */
    fun getStaticMapUrl(
        latitude: Double,
        longitude: Double,
        zoom: Int = 14,
        mapType: String = "hybrid",
        width: Int = 800,
        height: Int = 400
    ): String? {
        val apiKey = getValidApiKey() ?: return null
        return "https://maps.googleapis.com/maps/api/staticmap?center=$latitude,$longitude&zoom=$zoom&size=${width}x${height}&scale=2&maptype=$mapType&markers=color:green%7Clabel:C%7C$latitude,$longitude&key=$apiKey"
    }

    /**
     * Builds a Google Street View Static API URL for ground-level road/access preview of a campsite.
     */
    fun getStreetViewUrl(
        latitude: Double,
        longitude: Double,
        width: Int = 800,
        height: Int = 400
    ): String? {
        val apiKey = getValidApiKey() ?: return null
        return "https://maps.googleapis.com/maps/api/streetview?size=${width}x${height}&location=$latitude,$longitude&fov=90&pitch=5&key=$apiKey"
    }

    /**
     * Search for camping spots around current coordinates using Google Maps
     */
    fun searchNearbyCampingOnGoogleMaps(
        context: Context,
        latitude: Double,
        longitude: Double,
        filterTerm: String = "camping campsites water electricity"
    ) {
        try {
            // Google Maps Search URI format
            val encodedQuery = Uri.encode(filterTerm)
            val gmmIntentUri = Uri.parse("geo:$latitude,$longitude?q=$encodedQuery")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                setPackage("com.google.android.apps.maps")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            if (mapIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(mapIntent)
            } else {
                // Fallback to browser Google Maps
                val webUri = Uri.parse("https://www.google.com/maps/search/$encodedQuery/@$latitude,$longitude,12z")
                val webIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                context.startActivity(webIntent)
            }
        } catch (e: Exception) {
            val fallbackUri = Uri.parse("https://www.google.com/maps/search/camping/@$latitude,$longitude,12z")
            context.startActivity(Intent(Intent.ACTION_VIEW, fallbackUri).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
        }
    }

    /**
     * Open specific campsite location in Google Maps
     */
    fun openCampsiteInGoogleMaps(
        context: Context,
        latitude: Double,
        longitude: Double,
        campsiteName: String
    ) {
        try {
            val encodedName = Uri.encode(campsiteName)
            val gmmIntentUri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude($encodedName)")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                setPackage("com.google.android.apps.maps")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            if (mapIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(mapIntent)
            } else {
                val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$latitude,$longitude")
                val webIntent = Intent(Intent.ACTION_VIEW, webUri).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                context.startActivity(webIntent)
            }
        } catch (e: Exception) {
            val webUri = Uri.parse("https://www.google.com/maps/search/?api=1&query=$latitude,$longitude")
            context.startActivity(Intent(Intent.ACTION_VIEW, webUri).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
        }
    }

    /**
     * Launch turn-by-turn driving directions in Google Maps
     */
    fun navigateWithGoogleMaps(
        context: Context,
        latitude: Double,
        longitude: Double
    ) {
        try {
            val gmmIntentUri = Uri.parse("google.navigation:q=$latitude,$longitude&mode=d")
            val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri).apply {
                setPackage("com.google.android.apps.maps")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }

            if (mapIntent.resolveActivity(context.packageManager) != null) {
                context.startActivity(mapIntent)
            } else {
                val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$latitude,$longitude")
                val webIntent = Intent(Intent.ACTION_VIEW, webUri).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
                context.startActivity(webIntent)
            }
        } catch (e: Exception) {
            val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$latitude,$longitude")
            context.startActivity(Intent(Intent.ACTION_VIEW, webUri).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
        }
    }
}
