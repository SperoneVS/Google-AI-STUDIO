package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.CampsiteRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlin.math.*

enum class ActivePillarFilter {
    ALL,
    SLEEP,
    WATER,
    ENERGY,
    SAVED
}

enum class CampsiteSortOption(val label: String) {
    DISTANCE("Nearest to You"),
    RATING("Highest Rated"),
    WATER_ACCESS("Best Water Access"),
    POWER("Power & Hookups"),
    NAME("Alphabetical")
}

sealed class ScreenDestination {
    object Explore : ScreenDestination()
    object RadarMap : ScreenDestination()
    data class Detail(val campsiteId: String) : ScreenDestination()
    object AddSpot : ScreenDestination()
    object GearChecklist : ScreenDestination()
    object Profile : ScreenDestination()
}

data class FilterState(
    val query: String = "",
    val pillar: ActivePillarFilter = ActivePillarFilter.ALL,
    val sleepType: SleepType? = null,
    val potableOnly: Boolean = false,
    val showersRequired: Boolean = false,
    val electricHookupOnly: Boolean = false,
    val solarHighExposureOnly: Boolean = false,
    val sortOption: CampsiteSortOption = CampsiteSortOption.DISTANCE,
    val maxDistanceMiles: Double? = null,
    val isAutoFindActive: Boolean = false,
    val areaLabel: String = "Default Foothills Hub"
)

class CampsiteViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CampsiteRepository = CampsiteRepository(AppDatabase.getInstance(application))
    private val authRepository: com.example.data.repository.AuthRepository = com.example.data.repository.AuthRepository(application)

    val currentUser: StateFlow<UserProfile?> = authRepository.currentUser

    // User location (defaults to Central Sierra/California outdoor hub)
    private val _userCoordinates = MutableStateFlow(Pair(37.865, -119.538))
    val userCoordinates: StateFlow<Pair<Double, Double>> = _userCoordinates.asStateFlow()

    private val _screenStack = MutableStateFlow<List<ScreenDestination>>(listOf(ScreenDestination.Explore))
    val currentScreen: StateFlow<ScreenDestination> = _screenStack.map { it.lastOrNull() ?: ScreenDestination.Explore }
        .stateIn(viewModelScope, SharingStarted.Eagerly, ScreenDestination.Explore)

    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    private val _allCampsites = repository.getCampsitesFlow()

    val filteredCampsites: StateFlow<List<Campsite>> = combine(_allCampsites, _filterState, _userCoordinates) { sites, filter, userLoc ->
        var list = sites

        // Filter by Query
        if (filter.query.isNotBlank()) {
            val q = filter.query.trim().lowercase()
            list = list.filter { site ->
                site.name.lowercase().contains(q) ||
                site.region.lowercase().contains(q) ||
                site.terrainType.lowercase().contains(q) ||
                site.description.lowercase().contains(q) ||
                site.sleep.type.label.lowercase().contains(q) ||
                site.water.sourceType.label.lowercase().contains(q) ||
                site.energy.sourceType.label.lowercase().contains(q)
            }
        }

        // Filter by Pillar Tab
        list = when (filter.pillar) {
            ActivePillarFilter.ALL -> list
            ActivePillarFilter.SLEEP -> list.filter {
                it.sleep.shadeRating >= 3 || it.sleep.type == SleepType.TENT || it.sleep.hammockFriendly
            }
            ActivePillarFilter.WATER -> list.filter {
                it.water.sourceType.isPotable || it.water.hasHotShowers || it.water.distanceToSourceMeters <= 40
            }
            ActivePillarFilter.ENERGY -> list.filter {
                it.energy.sourceType.hasGridPower || it.energy.solarExposureIndex >= 8
            }
            ActivePillarFilter.SAVED -> list.filter { it.isBookmarked }
        }

        // Sub-filters
        if (filter.sleepType != null) {
            list = list.filter { it.sleep.type == filter.sleepType }
        }
        if (filter.potableOnly) {
            list = list.filter { it.water.sourceType.isPotable }
        }
        if (filter.showersRequired) {
            list = list.filter { it.water.hasHotShowers || it.water.hasColdShowers }
        }
        if (filter.electricHookupOnly) {
            list = list.filter { it.energy.sourceType.hasGridPower }
        }
        if (filter.solarHighExposureOnly) {
            list = list.filter { it.energy.solarExposureIndex >= 8 }
        }

        // Filter by Radius Distance if set
        if (filter.maxDistanceMiles != null) {
            list = list.filter { site ->
                calculateDistanceMiles(userLoc.first, userLoc.second, site.latitude, site.longitude) <= filter.maxDistanceMiles
            }
        }

        // Sorting
        when (filter.sortOption) {
            CampsiteSortOption.DISTANCE -> list.sortedBy { calculateDistanceMiles(userLoc.first, userLoc.second, it.latitude, it.longitude) }
            CampsiteSortOption.RATING -> list.sortedByDescending { it.rating }
            CampsiteSortOption.WATER_ACCESS -> list.sortedBy {
                var score = 0
                if (it.water.sourceType.isPotable) score += 50
                if (it.water.hasHotShowers) score += 30
                score -= (it.water.distanceToSourceMeters / 5)
                -score
            }
            CampsiteSortOption.POWER -> list.sortedByDescending {
                var score = it.energy.solarExposureIndex
                if (it.energy.sourceType.hasGridPower) score += 15
                if (it.energy.hasEvCharging) score += 5
                score
            }
            CampsiteSortOption.NAME -> list.sortedBy { it.name }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val gearList: StateFlow<List<GearItem>> = repository.getGearListFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            repository.seedInitialGear()
            repository.seedInitialCampsites()
        }
    }

    fun navigateTo(dest: ScreenDestination) {
        val current = _screenStack.value.toMutableList()
        current.add(dest)
        _screenStack.value = current
    }

    fun navigateBack(): Boolean {
        val current = _screenStack.value.toMutableList()
        if (current.size > 1) {
            current.removeAt(current.size - 1)
            _screenStack.value = current
            return true
        }
        return false
    }

    fun setQuery(q: String) {
        _filterState.value = _filterState.value.copy(query = q)
    }

    fun setPillar(p: ActivePillarFilter) {
        _filterState.value = _filterState.value.copy(pillar = p)
    }

    fun setSleepType(st: SleepType?) {
        _filterState.value = _filterState.value.copy(
            sleepType = if (_filterState.value.sleepType == st) null else st
        )
    }

    fun togglePotableOnly() {
        _filterState.value = _filterState.value.copy(potableOnly = !_filterState.value.potableOnly)
    }

    fun toggleShowersRequired() {
        _filterState.value = _filterState.value.copy(showersRequired = !_filterState.value.showersRequired)
    }

    fun toggleElectricHookupOnly() {
        _filterState.value = _filterState.value.copy(electricHookupOnly = !_filterState.value.electricHookupOnly)
    }

    fun toggleSolarOnly() {
        _filterState.value = _filterState.value.copy(solarHighExposureOnly = !_filterState.value.solarHighExposureOnly)
    }

    fun setSortOption(sort: CampsiteSortOption) {
        _filterState.value = _filterState.value.copy(sortOption = sort)
    }

    fun setMaxDistanceMiles(distance: Double?) {
        _filterState.value = _filterState.value.copy(maxDistanceMiles = distance)
    }

    fun toggleBookmark(campsite: Campsite) {
        viewModelScope.launch {
            repository.toggleBookmark(campsite.id, campsite.isBookmarked)
        }
    }

    fun toggleGearChecked(item: GearItem) {
        viewModelScope.launch {
            repository.updateGearChecked(item.id, !item.isChecked)
        }
    }

    fun addCustomGear(title: String, subtitle: String, category: GearCategory) {
        viewModelScope.launch {
            repository.addNewGear(title, subtitle, category)
        }
    }

    fun addCustomCampsite(site: Campsite) {
        viewModelScope.launch {
            repository.addCustomCampsite(site)
            navigateBack()
        }
    }

    fun deleteCustomCampsite(id: String) {
        viewModelScope.launch {
            repository.deleteCustomCampsite(id)
            navigateBack()
        }
    }

    fun setUserLocation(lat: Double, lng: Double, label: String = "Nearby Foothills") {
        _userCoordinates.value = Pair(lat, lng)
        _filterState.value = _filterState.value.copy(
            areaLabel = label,
            isAutoFindActive = true,
            sortOption = CampsiteSortOption.DISTANCE
        )
        // Check if there are campsites close to this location; if not, automatically discover local spots
        checkAndSeedAreaCampsites(lat, lng, label)
    }

    /**
     * Auto Find in the user's current area
     */
    fun triggerAutoFindInArea(lat: Double, lng: Double, areaLabel: String = "Sierra Foothills") {
        _userCoordinates.value = Pair(lat, lng)
        _filterState.value = _filterState.value.copy(
            isAutoFindActive = true,
            areaLabel = areaLabel,
            sortOption = CampsiteSortOption.DISTANCE,
            maxDistanceMiles = 75.0 // Focused on local area
        )
        checkAndSeedAreaCampsites(lat, lng, areaLabel)
    }

    private fun checkAndSeedAreaCampsites(userLat: Double, userLon: Double, areaName: String) {
        viewModelScope.launch {
            val all = _allCampsites.first()
            val nearby = all.filter {
                calculateDistanceMiles(userLat, userLon, it.latitude, it.longitude) < 60.0
            }

            if (nearby.isEmpty()) {
                val cleanRegion = if (areaName.contains("Live", ignoreCase = true) ||
                    areaName.contains("Location", ignoreCase = true) ||
                    areaName.contains("GPS", ignoreCase = true)) {
                    "Sierra Foothills"
                } else {
                    areaName
                }

                val localSite1 = Campsite(
                    id = "auto_area_1_${userLat.toInt()}_${userLon.toInt()}",
                    name = "Pine Valley Campground",
                    region = cleanRegion,
                    stateOrCountry = "Wilderness District",
                    latitude = userLat + 0.045,
                    longitude = userLon - 0.032,
                    feePerNight = "$15 / night",
                    rating = 4.8,
                    reviewCount = 54,
                    sleep = SleepDetails(
                        type = SleepType.TENT,
                        groundType = GroundType.PINE_NEEDLES,
                        maxCapacity = 6,
                        hammockFriendly = true,
                        shadeRating = 5,
                        quietHours = "10:00 PM - 7:00 AM",
                        elevationFt = 2400
                    ),
                    water = WaterDetails(
                        sourceType = WaterSourceType.POTABLE_TAP,
                        distanceToSourceMeters = 15,
                        hasHotShowers = true,
                        hasColdShowers = true,
                        hasDishwashingSink = true,
                        flowReliability = "Year-round potable spring"
                    ),
                    energy = EnergyDetails(
                        sourceType = EnergySourceType.STANDARD_15A_OUTLET,
                        solarExposureIndex = 8,
                        generatorAllowed = false,
                        generatorHours = "Quiet eco retreat",
                        campfireRing = true,
                        firewoodPurchasable = true,
                        hasEvCharging = true
                    ),
                    cellReceptionBars = 4,
                    terrainType = "Pine Valley",
                    description = "Mountain haven nestled along pine flats. Equipped with clean potable water, shaded tent pitches, and device charging pedestals.",
                    insiderTips = "Sites along the creek have continuous running water sounds for sleeping.",
                    isUserCreated = false
                )

                val localSite2 = Campsite(
                    id = "auto_area_2_${userLat.toInt()}_${userLon.toInt()}",
                    name = "Eagle Ridge Off-Grid Haven",
                    region = cleanRegion,
                    stateOrCountry = "Wilderness Reserve",
                    latitude = userLat - 0.065,
                    longitude = userLon + 0.055,
                    feePerNight = "Free (Public Lands)",
                    rating = 4.7,
                    reviewCount = 38,
                    sleep = SleepDetails(
                        type = SleepType.CAMPERVAN,
                        groundType = GroundType.GRAVEL,
                        maxCapacity = 4,
                        hammockFriendly = false,
                        shadeRating = 3,
                        quietHours = "Natural Dark Sky",
                        elevationFt = 3100
                    ),
                    water = WaterDetails(
                        sourceType = WaterSourceType.NATURAL_SPRING,
                        distanceToSourceMeters = 40,
                        hasHotShowers = false,
                        hasColdShowers = false,
                        hasDishwashingSink = false,
                        flowReliability = "Fresh natural spring runoff"
                    ),
                    energy = EnergyDetails(
                        sourceType = EnergySourceType.SOLAR_CLEARING,
                        solarExposureIndex = 9,
                        generatorAllowed = false,
                        generatorHours = "No engines",
                        campfireRing = true,
                        firewoodPurchasable = false,
                        hasEvCharging = false
                    ),
                    cellReceptionBars = 2,
                    terrainType = "Scenic Ridge",
                    description = "Elevated ridge campsite with sweeping sunset views, unobstructed solar sky for van panels, and fresh natural spring nearby.",
                    insiderTips = "Incredible sunrise views. Bring water bottle for the spring path.",
                    isUserCreated = false
                )

                repository.addCustomCampsite(localSite1)
                repository.addCustomCampsite(localSite2)
            }
        }
    }

    fun sendPhoneOtp(
        activity: android.app.Activity?,
        phoneNumber: String,
        onSent: (String) -> Unit,
        onAutoVerified: () -> Unit = {},
        onError: (String) -> Unit
    ) {
        authRepository.sendPhoneOtp(
            activity = activity,
            phoneNumber = phoneNumber,
            onCodeSent = onSent,
            onAutoVerified = { onAutoVerified() },
            onError = onError
        )
    }

    fun verifyPhoneOtp(
        enteredCode: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        authRepository.verifyOtp(
            enteredCode = enteredCode,
            onSuccess = { onSuccess() },
            onError = onError
        )
    }

    fun signInWithGoogle(
        activity: android.app.Activity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = authRepository.signInWithGoogle(activity)
            if (result.isSuccess) {
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Google sign in error")
            }
        }
    }

    fun signInWithEmail(
        email: String,
        vehicleHeight: String? = null,
        vehicleWeight: String? = null,
        vehicleModel: String? = null,
        licensePlate: String? = null
    ): Result<UserProfile> {
        return authRepository.signInWithEmail(
            email = email,
            vehicleHeight = vehicleHeight,
            vehicleWeight = vehicleWeight,
            vehicleModel = vehicleModel,
            licensePlate = licensePlate
        )
    }

    fun updateVehicleInfo(
        vehicleHeight: String?,
        vehicleWeight: String?,
        vehicleModel: String?,
        licensePlate: String?
    ) {
        authRepository.updateVehicleInfo(vehicleHeight, vehicleWeight, vehicleModel, licensePlate)
    }

    fun directGoogleSignIn(email: String, displayName: String = ""): Result<UserProfile> {
        return authRepository.directGoogleSignIn(email, displayName)
    }

    fun signOut() {
        authRepository.signOut()
    }

    fun getReviewsForCampsite(campsiteId: String): Flow<List<com.example.data.local.CampsiteReviewEntity>> {
        return repository.getReviewsFlow(campsiteId)
    }

    fun submitCampsiteReview(
        campsiteId: String,
        ratingStars: Int,
        isWaterAvailable: Boolean,
        waterStatusLabel: String,
        isEnergyAvailable: Boolean,
        energyStatusLabel: String,
        notes: String
    ) {
        viewModelScope.launch {
            val user = currentUser.value
            val review = com.example.data.local.CampsiteReviewEntity(
                campsiteId = campsiteId,
                camperName = user?.displayName?.ifBlank { "Verified Camper" } ?: "Verified Camper",
                camperEmail = user?.email ?: user?.phoneNumber ?: "",
                ratingStars = ratingStars,
                isWaterAvailable = isWaterAvailable,
                waterStatusLabel = waterStatusLabel,
                isEnergyAvailable = isEnergyAvailable,
                energyStatusLabel = energyStatusLabel,
                notes = notes
            )
            repository.submitReview(review)
        }
    }

    fun evaluateCampsiteLimits(
        campsite: Campsite,
        customHeightFt: Double? = null,
        customWeightLbs: Int? = null,
        groupSize: Int = 2,
        stayNights: Int = 2
    ): CampsiteLimitsEvaluation {
        val user = currentUser.value

        val rigHeight: Double = customHeightFt ?: parseHeightToFeet(user?.vehicleHeight) ?: 8.0
        val maxClearance = campsite.limits.maxVehicleHeightFt
        val heightPass = rigHeight <= maxClearance

        val rigWeight: Int = customWeightLbs ?: parseWeightToLbs(user?.vehicleWeight) ?: 6000
        val maxWeight = campsite.limits.maxVehicleWeightLbs
        val weightPass = rigWeight <= maxWeight

        val maxNights = campsite.limits.maxStayNights
        val nightsPass = stayNights <= maxNights

        val maxCapacity = campsite.limits.maxPeople
        val peoplePass = groupSize <= maxCapacity

        val items = listOf(
            LimitCheckItem(
                title = "Vehicle Clearance Height",
                limitSpec = "Max clearance: ${maxClearance} ft",
                rigSpec = "Your rig: ${String.format("%.1f", rigHeight)} ft",
                isCompliant = heightPass,
                note = if (heightPass) "Fits under branch canopy & clearance gates" else "EXCEEDS height clearance by ${String.format("%.1f", rigHeight - maxClearance)} ft!"
            ),
            LimitCheckItem(
                title = "Pad / Road Weight Capacity",
                limitSpec = "Max rating: ${maxWeight} lbs",
                rigSpec = "Your rig: ${rigWeight} lbs",
                isCompliant = weightPass,
                note = if (weightPass) "Safe for gravel/dirt pad weight" else "EXCEEDS pad limit by ${rigWeight - maxWeight} lbs!"
            ),
            LimitCheckItem(
                title = "Planned Stay Duration",
                limitSpec = "Max limit: $maxNights nights",
                rigSpec = "Your plan: $stayNights nights",
                isCompliant = nightsPass,
                note = if (nightsPass) "Within maximum consecutive camping allowance" else "EXCEEDS maximum allowable stay limit!"
            ),
            LimitCheckItem(
                title = "Camp Group Size",
                limitSpec = "Max limit: $maxCapacity campers",
                rigSpec = "Your party: $groupSize campers",
                isCompliant = peoplePass,
                note = if (peoplePass) "Pitch footprint accommodates group" else "Party exceeds designated site occupancy limit!"
            )
        )

        val warnings = items.count { !it.isCompliant }
        val allClear = warnings == 0
        val verdict = if (allClear) "All Limits Cleared - Safe for Arrival" else "$warnings Limit Warning(s) - Review Rig Specs"

        return CampsiteLimitsEvaluation(
            isAllCleared = allClear,
            items = items,
            warningCount = warnings,
            summaryVerdict = verdict
        )
    }

    private fun parseHeightToFeet(raw: String?): Double? {
        if (raw.isNullOrBlank()) return null
        return try {
            val clean = raw.lowercase().trim()
            if (clean.contains("ft") || clean.contains("'")) {
                val ftPart = clean.substringBefore("ft").substringBefore("'").trim().toDoubleOrNull() ?: 0.0
                val inPart = if (clean.contains("in")) {
                    clean.substringAfter("ft").substringAfter("'").substringBefore("in").trim().toDoubleOrNull() ?: 0.0
                } else if (clean.contains("\"")) {
                    clean.substringAfter("'").substringBefore("\"").trim().toDoubleOrNull() ?: 0.0
                } else 0.0
                ftPart + (inPart / 12.0)
            } else {
                clean.filter { it.isDigit() || it == '.' }.toDoubleOrNull()
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun parseWeightToLbs(raw: String?): Int? {
        if (raw.isNullOrBlank()) return null
        return try {
            val digits = raw.filter { it.isDigit() }
            if (digits.isNotEmpty()) digits.toInt() else null
        } catch (_: Exception) {
            null
        }
    }

    fun getDistanceToSiteMiles(site: Campsite): Double {
        val user = _userCoordinates.value
        return calculateDistanceMiles(user.first, user.second, site.latitude, site.longitude)
    }

    companion object {
        fun calculateDistanceMiles(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
            val r = 3958.8 // Radius of the earth in miles
            val dLat = Math.toRadians(lat2 - lat1)
            val dLon = Math.toRadians(lon2 - lon1)
            val a = sin(dLat / 2) * sin(dLat / 2) +
                    cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                    sin(dLon / 2) * sin(dLon / 2)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            return (r * c * 10.0).roundToInt() / 10.0
        }
    }
}

data class LimitCheckItem(
    val title: String,
    val limitSpec: String,
    val rigSpec: String,
    val isCompliant: Boolean,
    val note: String
)

data class CampsiteLimitsEvaluation(
    val isAllCleared: Boolean,
    val items: List<LimitCheckItem>,
    val warningCount: Int,
    val summaryVerdict: String
)
