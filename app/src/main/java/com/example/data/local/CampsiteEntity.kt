package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "custom_campsites")
data class CampsiteEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val region: String,
    val stateOrCountry: String,
    val latitude: Double,
    val longitude: Double,
    val feePerNight: String,
    val rating: Double,
    val reviewCount: Int,
    // Sleep fields
    val sleepType: String,
    val groundType: String,
    val maxCapacity: Int,
    val hammockFriendly: Boolean,
    val shadeRating: Int,
    val quietHours: String,
    val elevationFt: Int,
    // Water fields
    val waterSourceType: String,
    val distanceToSourceMeters: Int,
    val hasHotShowers: Boolean,
    val hasColdShowers: Boolean,
    val hasDishwashingSink: Boolean,
    val flowReliability: String,
    // Energy fields
    val energySourceType: String,
    val solarExposureIndex: Int,
    val generatorAllowed: Boolean,
    val generatorHours: String,
    val campfireRing: Boolean,
    val firewoodPurchasable: Boolean,
    val hasEvCharging: Boolean,
    // General
    val cellReceptionBars: Int,
    val terrainType: String,
    val description: String,
    val insiderTips: String,
    val isUserCreated: Boolean = true
)

@Entity(tableName = "bookmarked_spots")
data class BookmarkEntity(
    @PrimaryKey
    val campsiteId: String,
    val savedAtTimestamp: Long = System.currentTimeMillis(),
    val camperNotes: String = ""
)

@Entity(tableName = "gear_checklist")
data class GearEntity(
    @PrimaryKey
    val id: String,
    val category: String,
    val title: String,
    val subtitle: String,
    val isChecked: Boolean
)
