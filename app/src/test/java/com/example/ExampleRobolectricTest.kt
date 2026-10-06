package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.repository.AuthRepository
import com.example.ui.viewmodel.CampsiteViewModel
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
  fun `test phone otp send and verification flow`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val authRepo = AuthRepository(context)

    var generatedCode = ""
    authRepo.sendPhoneOtp(
        activity = null,
        phoneNumber = "+15550199",
        onCodeSent = { code -> generatedCode = code },
        onAutoVerified = { /* no op */ },
        onError = { /* no op */ }
    )

    assertTrue("Generated OTP code should be 6 digits", generatedCode.length == 6)

    var verifiedUser: com.example.data.model.UserProfile? = null
    authRepo.verifyOtp(
        enteredCode = generatedCode,
        onSuccess = { user -> verifiedUser = user },
        onError = { /* no op */ }
    )
    assertNotNull(verifiedUser)
    assertEquals("+15550199", verifiedUser?.phoneNumber)
  }

  @Test
  fun `test user can login with their own google account`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val authRepo = AuthRepository(context)

    val customEmail = "user.camper@gmail.com"
    val customName = "User Camper"
    val result = authRepo.directGoogleSignIn(customEmail, customName)
    assertTrue("Direct Google sign-in with user account should succeed", result.isSuccess)
    val user = result.getOrNull()
    assertNotNull(user)
    assertEquals(customEmail, user?.email)
    assertEquals(customName, user?.displayName)
    assertEquals(com.example.data.model.AuthMethod.GOOGLE, user?.authMethod)
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
}
