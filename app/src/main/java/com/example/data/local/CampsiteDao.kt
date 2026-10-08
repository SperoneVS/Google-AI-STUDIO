package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CampsiteDao {
    @Query("SELECT * FROM campsites ORDER BY name ASC")
    fun getAllCampsites(): Flow<List<Campsite>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCampsite(campsite: Campsite)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllCampsites(campsites: List<Campsite>)

    @Query("DELETE FROM campsites WHERE id = :id")
    suspend fun deleteCampsite(id: String)

    @Query("SELECT * FROM custom_campsites ORDER BY name ASC")
    fun getAllCustomCampsites(): Flow<List<CampsiteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCustomCampsite(campsite: CampsiteEntity)

    @Query("DELETE FROM custom_campsites WHERE id = :id")
    suspend fun deleteCustomCampsite(id: String)

    @Query("SELECT * FROM bookmarked_spots")
    fun getAllBookmarks(): Flow<List<BookmarkEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookmark(bookmark: BookmarkEntity)

    @Query("DELETE FROM bookmarked_spots WHERE campsiteId = :campsiteId")
    suspend fun removeBookmark(campsiteId: String)

    @Query("SELECT * FROM gear_checklist")
    fun getAllGearItems(): Flow<List<GearEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGearItem(item: GearEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllGearItems(items: List<GearEntity>)

    @Query("UPDATE gear_checklist SET isChecked = :isChecked WHERE id = :id")
    suspend fun updateGearChecked(id: String, isChecked: Boolean)

    @Query("SELECT * FROM campsite_reviews WHERE campsite_id = :campsiteId ORDER BY created_at_timestamp DESC")
    fun getReviewsForCampsite(campsiteId: String): Flow<List<CampsiteReviewEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: CampsiteReviewEntity)

    @Query("SELECT * FROM campsite_reviews ORDER BY created_at_timestamp DESC")
    fun getAllReviews(): Flow<List<CampsiteReviewEntity>>
}
