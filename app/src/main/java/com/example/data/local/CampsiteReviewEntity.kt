package com.example.data.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "campsite_reviews")
data class CampsiteReviewEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    
    @ColumnInfo(name = "campsite_id")
    val campsiteId: String,
    
    @ColumnInfo(name = "camper_name")
    val camperName: String,
    
    @ColumnInfo(name = "camper_email")
    val camperEmail: String = "",
    
    @ColumnInfo(name = "rating_stars")
    val ratingStars: Int, // 1 to 5
    
    @ColumnInfo(name = "is_water_available")
    val isWaterAvailable: Boolean,
    
    @ColumnInfo(name = "water_status_label")
    val waterStatusLabel: String,
    
    @ColumnInfo(name = "is_energy_available")
    val isEnergyAvailable: Boolean,
    
    @ColumnInfo(name = "energy_status_label")
    val energyStatusLabel: String,
    
    @ColumnInfo(name = "notes")
    val notes: String = "",
    
    @ColumnInfo(name = "created_at_timestamp")
    val createdAtTimestamp: Long = System.currentTimeMillis()
)
