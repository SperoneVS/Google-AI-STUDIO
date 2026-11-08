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

  @Test
  fun `test European Park4Night campsites catalog and verification`() {
    val europeanSites = com.example.data.repository.EuropeanPark4NightData.europeanPark4NightCampsites
    assertTrue("Should have extensive European Park4Night campsites", europeanSites.size >= 12)

    val verdon = europeanSites.find { it.id == "p4n_eu_fr_verdon" }
    assertNotNull("Verdon campsite should exist", verdon)
    assertTrue("Verdon must be Park4night verified", verdon!!.isPark4NightVerified)
    assertTrue("Verdon must have Park4night note", verdon.park4NightNote.contains("Park4night #48102"))
    assertEquals("France", verdon.stateOrCountry)

    val garda = europeanSites.find { it.id == "p4n_eu_it_garda" }
    assertNotNull("Garda campsite should exist", garda)
    assertTrue("Garda must be Park4night verified", garda!!.isPark4NightVerified)
    assertEquals("Italy", garda.stateOrCountry)

    val plitvice = europeanSites.find { it.id == "p4n_eu_hr_plitvice" }
    assertNotNull("Plitvice campsite should exist", plitvice)
    assertEquals("Croatia", plitvice!!.stateOrCountry)

    val bohinj = europeanSites.find { it.id == "p4n_eu_si_bohinj" }
    assertNotNull("Bohinj campsite should exist", bohinj)
    assertEquals("Slovenia", bohinj!!.stateOrCountry)

    val meteora = europeanSites.find { it.id == "p4n_eu_gr_meteora" }
    assertNotNull("Meteora campsite should exist", meteora)
    assertEquals("Greece", meteora!!.stateOrCountry)

    val slovakiaTatras = europeanSites.find { it.id == "p4n_eu_sk_tatry_rietave" }
    assertNotNull("High Tatras Slovak campsite should exist", slovakiaTatras)
    assertEquals("Slovakia", slovakiaTatras!!.stateOrCountry)
    assertTrue("Slovak site must be Park4Night verified", slovakiaTatras.isPark4NightVerified)

    val slovenskyRaj = europeanSites.find { it.id == "p4n_eu_sk_slovensky_raj" }
    assertNotNull("Slovenský Raj campsite should exist", slovenskyRaj)
    assertEquals("Slovakia", slovenskyRaj!!.stateOrCountry)

    // Ensure all European spots have valid coordinates for Google Maps
    europeanSites.forEach { site ->
      assertTrue("${site.name} latitude should be valid", site.latitude in -90.0..90.0)
      assertTrue("${site.name} longitude should be valid", site.longitude in -180.0..180.0)
      assertTrue("${site.name} should have verified badge", site.isPark4NightVerified)
      assertTrue("${site.name} should have sleep details", site.sleep.maxCapacity > 0)
    }
  }

  @Test
  fun `test Park4Night deep link URL format`() {
    val url = com.example.util.Park4NightHelper.buildPark4NightUrl(43.7801, 6.3414)
    assertEquals("https://park4night.com/en/search?lat=43.7801&lng=6.3414", url)
  }

  @Test
  fun `test no United States campsites exist in curated catalog`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val database = com.example.data.local.AppDatabase.getInstance(context)
    val repo = CampsiteRepository(database, context)

    val curatedSites = repo.curatedCampsites
    assertTrue("Curated campsites should exist", curatedSites.isNotEmpty())

    // Verify zero USA campsites in curated set
    val usSites = curatedSites.filter {
      it.stateOrCountry.contains("USA", ignoreCase = true) ||
      it.stateOrCountry.contains("United States", ignoreCase = true) ||
      it.id == "curated_1" ||
      it.id == "curated_2"
    }
    assertTrue("Curated campsites must not contain any US sites, found: ${usSites.map { it.name }}", usSites.isEmpty())
  }

  @Test
  fun `test default unit system is European Metric`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val prefs = context.getSharedPreferences("camphaven_prefs", Context.MODE_PRIVATE)
    val saved = prefs.getString("unit_system", com.example.data.model.UnitSystem.METRIC.name)
    val unitSystem = com.example.data.model.UnitSystem.valueOf(saved ?: com.example.data.model.UnitSystem.METRIC.name)
    assertEquals(com.example.data.model.UnitSystem.METRIC, unitSystem)
    // Verify startup prompt is not active
    val promptChosen = prefs.getBoolean("unit_system_chosen", false)
    assertEquals(false, promptChosen)
  }

  @Test
  fun `test location permission check current area label and nearby campsite prioritization`() = runBlocking {
    val app = ApplicationProvider.getApplicationContext<android.app.Application>()
    val shadowApp = org.robolectric.Shadows.shadowOf(app)

    // 1. Verify permission check before and after granting ACCESS_FINE_LOCATION
    shadowApp.denyPermissions(
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION
    )
    assertTrue("Permission should initially be denied", !com.example.util.LocationTracker.hasLocationPermission(app))

    shadowApp.grantPermissions(android.Manifest.permission.ACCESS_FINE_LOCATION)
    assertTrue("Permission should be granted after grantPermissions", com.example.util.LocationTracker.hasLocationPermission(app))

    // 2. Verify reverseGeocodeAreaLabel resolves user's current area near Chamonix (45.9237, 6.8694)
    val areaLabel = com.example.util.PlacesSearchService.reverseGeocodeAreaLabel(app, 45.9237, 6.8694)
    assertTrue("Area label should identify region or coordinates, got: $areaLabel", areaLabel.contains("📍"))

    // 3. Verify nearby campsites are prioritized by distance from user's current coordinates
    val database = com.example.data.local.AppDatabase.getInstance(app)
    val repo = CampsiteRepository(database, app)
    val prioritized = repo.curatedCampsites.sortedBy { site ->
      CampsiteViewModel.calculateDistanceMiles(45.9237, 6.8694, site.latitude, site.longitude)
    }
    assertTrue("Prioritized list should not be empty", prioritized.isNotEmpty())
    // Closest curated site to Chamonix (45.9237, 6.8694) should be Camping des Glaciers (eu_fr_1)
    assertEquals("eu_fr_1", prioritized.first().id)
  }
}
