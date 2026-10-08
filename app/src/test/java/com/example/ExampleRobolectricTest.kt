package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.AuthMethod
import com.example.data.model.UserProfile
import com.example.data.repository.CampsiteRepository
import com.example.ui.viewmodel.CampsiteViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read app name string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("CampHaven", appName)
  }

  @Test
  fun `test distance calculation between coordinates`() {
    // Yosemite valley (approx 37.74, -119.59) to San Francisco (37.77, -122.41) is ~150-160 miles
    val distance = CampsiteViewModel.calculateDistanceMiles(37.74, -119.59, 37.77, -122.41)
    assertTrue("Distance should be around 155 miles, was $distance", distance in 140.0..170.0)
  }

  @Test
  fun `test 12 hour sync interval constant`() {
    val twelveHoursMillis = CampsiteRepository.TWELVE_HOURS_MILLIS
    assertEquals(12 * 60 * 60 * 1000L, twelveHoursMillis)
  }

  @Test
  fun `test user profile and vehicle specifications`() {
    val user = UserProfile(
        id = "camper_test_1",
        displayName = "Alpine Nomad",
        email = "nomad@camphaven.org",
        authMethod = AuthMethod.GOOGLE,
        vehicleModel = "Ford Transit Custom",
        vehicleHeight = "8 ft 4 in",
        vehicleWeight = "6,200 lbs",
        licensePlate = "CAMP-777"
    )

    assertEquals("camper_test_1", user.id)
    assertEquals("Alpine Nomad", user.displayName)
    assertEquals("nomad@camphaven.org", user.email)
    assertEquals("8 ft 4 in", user.vehicleHeight)
    assertEquals("6,200 lbs", user.vehicleWeight)
    assertEquals("Ford Transit Custom", user.vehicleModel)
    assertEquals("CAMP-777", user.licensePlate)
    assertEquals(AuthMethod.GOOGLE, user.authMethod)
  }

  @Test
  fun `test campsite detailed info model with photos and amenities`() {
    val sampleSite = com.example.data.model.Campsite(
        id = "test_1",
        name = "High Sierra Haven",
        region = "Yosemite Ridge",
        stateOrCountry = "California, USA",
        latitude = 37.865,
        longitude = -119.538,
        feePerNight = "$20 / night",
        rating = 4.9,
        reviewCount = 100,
        sleep = com.example.data.model.SleepDetails(
            type = com.example.data.model.SleepType.TENT,
            groundType = com.example.data.model.GroundType.PINE_NEEDLES,
            maxCapacity = 6,
            hammockFriendly = true,
            shadeRating = 5,
            quietHours = "10 PM",
            elevationFt = 7000
        ),
        water = com.example.data.model.WaterDetails(
            sourceType = com.example.data.model.WaterSourceType.NATURAL_SPRING,
            distanceToSourceMeters = 20,
            hasHotShowers = false,
            hasColdShowers = true,
            hasDishwashingSink = true,
            flowReliability = "Year-round"
        ),
        energy = com.example.data.model.EnergyDetails(
            sourceType = com.example.data.model.EnergySourceType.SOLAR_CLEARING,
            solarExposureIndex = 9,
            generatorAllowed = false,
            generatorHours = "None",
            campfireRing = true,
            firewoodPurchasable = true,
            hasEvCharging = false
        ),
        cellReceptionBars = 2,
        terrainType = "Alpine Forest",
        description = "Scenic haven",
        insiderTips = "Good sunrise"
    )

    val detail = com.example.data.repository.CampsiteDetailFactory.createDetailedInfo(sampleSite)
    assertNotNull(detail)
    assertTrue("Should have multiple photos", detail.photos.size >= 3)
    assertTrue("Should have categorized amenities", detail.amenities.size >= 8)
    assertTrue("Should contain drinking water amenity", detail.amenities.any { it.name == "Drinking Water" })
    assertTrue("Should contain pitch type amenity", detail.amenities.any { it.category == com.example.data.model.AmenityCategory.SLEEP_SETUP })
  }

  @Test
  fun `test Room Campsite entity with name sleeping setup water and energy hookup status`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val database = com.example.data.local.AppDatabase.getInstance(context)
    val dao = database.campsiteDao()

    val roomCampsite = com.example.data.local.Campsite(
        id = "room_camp_1",
        name = "High Sierra Pines",
        sleepingSetup = "Tent Pitch",
        waterAvailability = "Potable Tap",
        energyHookupStatus = "120V Outlet",
        region = "Sierra Foothills",
        maxVehicleHeightFt = 12.0,
        maxVehicleWeightLbs = 10000
    )

    dao.insertCampsite(roomCampsite)
    val all = dao.getAllCampsites().first()
    val inserted = all.find { it.id == "room_camp_1" }

    assertNotNull(inserted)
    assertEquals("High Sierra Pines", inserted?.name)
    assertEquals("Tent Pitch", inserted?.sleepingSetup)
    assertEquals("Potable Tap", inserted?.waterAvailability)
    assertEquals("120V Outlet", inserted?.energyHookupStatus)
  }

  @Test
  fun `test delete live location in campsite name`() {
    val clean1 = com.example.data.repository.cleanCampsiteName("Live Location Alpine Ridge")
    assertTrue("Name must not contain Live Location", !clean1.contains("Live Location", ignoreCase = true))
    assertTrue("Name must not contain Live", !clean1.contains("Live", ignoreCase = true))

    val clean2 = com.example.data.repository.cleanCampsiteName("Live GPS Location")
    assertTrue("Name must not contain Live", !clean2.contains("Live", ignoreCase = true))
    assertTrue("Name should fallback cleanly", clean2.isNotBlank())
  }

  @Test
  fun `test post-visit review rating with water and energy availability`() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val database = com.example.data.local.AppDatabase.getInstance(context)
    val dao = database.campsiteDao()

    val review = com.example.data.local.CampsiteReviewEntity(
        campsiteId = "site_lake_1",
        camperName = "Alex Explorer",
        camperEmail = "alex@camphaven.org",
        ratingStars = 5,
        isWaterAvailable = true,
        waterStatusLabel = "Potable Drinking Tap Active",
        isEnergyAvailable = true,
        energyStatusLabel = "30A Shore Hookup Working",
        notes = "Water pressure was great and power post had 30A working perfectly."
    )

    dao.insertReview(review)
    val reviews = dao.getReviewsForCampsite("site_lake_1").first()
    val inserted = reviews.find { it.camperName == "Alex Explorer" }

    assertNotNull(inserted)
    assertEquals(5, inserted?.ratingStars)
    assertTrue("Water must be recorded as available", inserted?.isWaterAvailable == true)
    assertEquals("Potable Drinking Tap Active", inserted?.waterStatusLabel)
    assertTrue("Energy must be recorded as available", inserted?.isEnergyAvailable == true)
    assertEquals("30A Shore Hookup Working", inserted?.energyStatusLabel)
  }
}
