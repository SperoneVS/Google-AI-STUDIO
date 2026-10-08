package com.example.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Room database entity for 'Campsite' that includes fields for:
 * - name
 * - sleepingSetup
 * - waterAvailability
 * - energyHookupStatus
 */
@Entity(tableName = "campsites")
data class Campsite(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "sleeping_setup")
    val sleepingSetup: String,
    
    @ColumnInfo(name = "water_availability")
    val waterAvailability: String,
    
    @ColumnInfo(name = "energy_hookup_status")
    val energyHookupStatus: String,
    
    @ColumnInfo(name = "region")
    val region: String = "",
    
    @ColumnInfo(name = "state_or_country")
    val stateOrCountry: String = "",
    
    @ColumnInfo(name = "latitude")
    val latitude: Double = 0.0,
    
    @ColumnInfo(name = "longitude")
    val longitude: Double = 0.0,
    
    @ColumnInfo(name = "fee_per_night")
    val feePerNight: String = "$0",
    
    @ColumnInfo(name = "rating")
    val rating: Double = 5.0,
    
    @ColumnInfo(name = "review_count")
    val reviewCount: Int = 0,
    
    @ColumnInfo(name = "terrain_type")
    val terrainType: String = "Wilderness",
    
    @ColumnInfo(name = "description")
    val description: String = "",

    @ColumnInfo(name = "max_vehicle_height_ft")
    val maxVehicleHeightFt: Double = 12.0,
    
    @ColumnInfo(name = "max_vehicle_weight_lbs")
    val maxVehicleWeightLbs: Int = 10000,
    
    @ColumnInfo(name = "max_vehicle_length_ft")
    val maxVehicleLengthFt: Int = 30,
    
    @ColumnInfo(name = "max_stay_nights")
    val maxStayNights: Int = 14,
    
    @ColumnInfo(name = "max_people")
    val maxPeople: Int = 6,
    
    @ColumnInfo(name = "is_user_created")
    val isUserCreated: Boolean = false
)
