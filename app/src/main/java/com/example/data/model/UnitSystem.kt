package com.example.data.model

import java.util.Locale
import kotlin.math.roundToInt

enum class UnitSystem(val label: String, val shortLabel: String, val flag: String) {
    METRIC("European Metric (km, m, kg)", "Metric (km, m)", "🇪🇺"),
    IMPERIAL("American Imperial (mi, ft, lbs)", "Imperial (mi, ft)", "🇺🇸");

    fun formatDistance(distanceMiles: Double): String {
        return if (this == METRIC) {
            val km = distanceMiles * 1.60934
            if (km < 1.0) {
                "${(km * 1000).roundToInt()} m away"
            } else if (km < 10.0) {
                String.format(Locale.US, "%.1f km away", km)
            } else {
                "${km.roundToInt()} km away"
            }
        } else {
            if (distanceMiles < 10.0) {
                String.format(Locale.US, "%.1f mi away", distanceMiles)
            } else {
                "${distanceMiles.roundToInt()} mi away"
            }
        }
    }

    fun formatElevation(elevationFt: Int): String {
        return if (this == METRIC) {
            val meters = (elevationFt * 0.3048).roundToInt()
            "$meters m"
        } else {
            "$elevationFt ft"
        }
    }

    fun formatVehicleHeight(heightFt: Double): String {
        return if (this == METRIC) {
            val meters = heightFt * 0.3048
            String.format(Locale.US, "%.1f m", meters)
        } else {
            String.format(Locale.US, "%.1f ft", heightFt)
        }
    }

    fun formatVehicleWeight(weightLbs: Int): String {
        return if (this == METRIC) {
            val kg = (weightLbs * 0.453592).roundToInt()
            "$kg kg"
        } else {
            "$weightLbs lbs"
        }
    }

    fun formatWaterDistance(distanceMeters: Int): String {
        return if (this == METRIC) {
            "$distanceMeters m"
        } else {
            val ft = (distanceMeters * 3.28084).roundToInt()
            "$ft ft"
        }
    }
}
