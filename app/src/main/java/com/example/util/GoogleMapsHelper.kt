package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast

object GoogleMapsHelper {

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
