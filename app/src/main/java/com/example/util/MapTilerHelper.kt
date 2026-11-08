package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.BuildConfig

object MapTilerHelper {

    /**
     * MapTiler Static Map Styles
     */
    enum class MapTilerStyle(val styleId: String, val label: String) {
        OUTDOOR("outdoor-v2", "Outdoor Topo"),
        TOPOGRAPHIC("topo-v2", "Topographic"),
        SATELLITE("satellite", "High-Res Satellite"),
        HYBRID("hybrid", "Satellite Hybrid"),
        VOYAGER("voyager", "Voyager Road"),
        WINTER("winter-v2", "Winter / Ski")
    }

    /**
     * Retrieves the MapTiler API key from BuildConfig (via Secrets panel / .env)
     */
    fun getValidApiKey(): String? {
        val key = BuildConfig.MAPTILER_API_KEY.trim()
        return if (
            key.isBlank() ||
            key == "DEFAULT_MAPTILER_KEY" ||
            key.contains("PLACEHOLDER", ignoreCase = true) ||
            key.contains("YOUR_", ignoreCase = true)
        ) {
            null
        } else {
            key
        }
    }

    fun hasValidApiKey(): Boolean = getValidApiKey() != null

    /**
     * Builds a MapTiler Static Map API URL centered on the given coordinates.
     * https://docs.maptiler.com/cloud/api/static-maps/
     * Format: https://api.maptiler.com/maps/{style}/static/{lon},{lat},{zoom}/{width}x{height}@2x.png?key={key}&markers={lon},{lat}
     */
    fun getStaticMapUrl(
        latitude: Double,
        longitude: Double,
        zoom: Double = 13.0,
        style: MapTilerStyle = MapTilerStyle.OUTDOOR,
        width: Int = 800,
        height: Int = 450,
        includeMarker: Boolean = true
    ): String? {
        val apiKey = getValidApiKey() ?: return null
        val markerParam = if (includeMarker) "&markers=$longitude,$latitude,pin-s-camp+2e7d32" else ""
        return "https://api.maptiler.com/maps/${style.styleId}/static/$longitude,$latitude,$zoom/${width}x${height}@2x.png?key=$apiKey$markerParam"
    }

    /**
     * Builds a MapTiler web link to view the topographic map for coordinates
     */
    fun openInMapTiler(context: Context, latitude: Double, longitude: Double, zoom: Int = 13) {
        try {
            val apiKey = getValidApiKey()
            val uri = if (apiKey != null) {
                Uri.parse("https://cloud.maptiler.com/maps/outdoor-v2/#$zoom/$latitude/$longitude")
            } else {
                Uri.parse("https://www.maptiler.com/maps/#outdoor//$zoom/$latitude/$longitude")
            }
            context.startActivity(Intent(Intent.ACTION_VIEW, uri).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK })
        } catch (_: Exception) {}
    }
}
