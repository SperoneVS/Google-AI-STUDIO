package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.AuthRepository
import com.example.data.repository.CampsiteRepository
import com.example.util.Park4NightHelper
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
    val park4NightOnly: Boolean = false,
    val sortOption: CampsiteSortOption = CampsiteSortOption.DISTANCE,
    val maxDistanceMiles: Double? = null,
    val isAutoFindActive: Boolean = false,
    val areaLabel: String = "🇪🇺 Europe (Central Alps Hub)"
)

data class LimitCheckItem(
    val title: String,
    val limitSpec: String,
    val rigSpec: String,
    val isCompliant: Boolean,
    val note: String
)

data class CampsiteLimitsEvaluation(
    val isFullyCompliant: Boolean,
    val passedCount: Int,
    val totalCount: Int,
    val summaryText: String,
    val items: List<LimitCheckItem>
) {
    val isAllCleared: Boolean get() = isFullyCompliant
    val summaryVerdict: String get() = summaryText
}

class CampsiteViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CampsiteRepository = CampsiteRepository(AppDatabase.getInstance(application), application)
    private val authRepository: AuthRepository = AuthRepository(application)
    private val prefs = application.getSharedPreferences("camphaven_prefs", Context.MODE_PRIVATE)

    val currentUser: StateFlow<UserProfile?> = authRepository.currentUser
    val lastSyncInfo: StateFlow<String> = repository.lastSyncInfo

    // Metric System Preference: European Metric (km, m, kg) vs American Imperial (mi, ft, lbs)
    private val _unitSystem = MutableStateFlow(
        runCatching {
            val saved = prefs.getString("unit_system", UnitSystem.METRIC.name)
            UnitSystem.valueOf(saved ?: UnitSystem.METRIC.name)
        }.getOrDefault(UnitSystem.METRIC)
    )
    val unitSystem: StateFlow<UnitSystem> = _unitSystem.asStateFlow()

    // Prompt camper to confirm European or American metrics on first launch
    private val _showUnitPrompt = MutableStateFlow(!prefs.contains("unit_system_chosen"))
    val showUnitPrompt: StateFlow<Boolean> = _showUnitPrompt.asStateFlow()

    // Default coordinates: Central Europe (Alps outdoor hub: St. Moritz / Chamonix corridor)
    private val _userCoordinates = MutableStateFlow(Pair(46.5197, 9.9534))
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
                site.stateOrCountry.lowercase().contains(q) ||
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
        if (filter.park4NightOnly) {
            list = list.filter { it.isPark4NightVerified }
        }

        // Radius distance filter
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
        // Initialize private on-device Park4night profile
        Park4NightHelper.initPrivateAccount(application)
    }

    fun setUnitSystem(system: UnitSystem) {
        _unitSystem.value = system
        _showUnitPrompt.value = false
        prefs.edit()
            .putString("unit_system", system.name)
            .putBoolean("unit_system_chosen", true)
            .apply()
    }

    fun dismissUnitPrompt() {
        _showUnitPrompt.value = false
        prefs.edit().putBoolean("unit_system_chosen", true).apply()
    }

    fun setQuery(q: String) {
        _filterState.update { it.copy(query = q) }
    }

    fun setPillar(p: ActivePillarFilter) {
        _filterState.update { it.copy(pillar = p) }
    }

    fun setSleepType(type: SleepType?) {
        _filterState.update { it.copy(sleepType = type) }
    }

    fun togglePotableOnly() {
        _filterState.update { it.copy(potableOnly = !it.potableOnly) }
    }

    fun toggleShowersRequired() {
        _filterState.update { it.copy(showersRequired = !it.showersRequired) }
    }

    fun toggleElectricHookupOnly() {
        _filterState.update { it.copy(electricHookupOnly = !it.electricHookupOnly) }
    }

    fun toggleSolarHighExposureOnly() {
        _filterState.update { it.copy(solarHighExposureOnly = !it.solarHighExposureOnly) }
    }

    fun togglePark4NightOnly() {
        _filterState.update { it.copy(park4NightOnly = !it.park4NightOnly) }
    }

    fun setSortOption(sort: CampsiteSortOption) {
        _filterState.update { it.copy(sortOption = sort) }
    }

    fun setMaxDistanceMiles(dist: Double?) {
        _filterState.update { it.copy(maxDistanceMiles = dist) }
    }

    fun setUserLocation(lat: Double, lon: Double, areaLabel: String = "Selected Area") {
        _userCoordinates.value = Pair(lat, lon)
        _filterState.update { it.copy(areaLabel = areaLabel, isAutoFindActive = false) }
    }

    fun triggerAutoFindInArea(lat: Double, lon: Double, areaLabel: String = "Your GPS Area") {
        _userCoordinates.value = Pair(lat, lon)
        _filterState.update {
            it.copy(
                areaLabel = areaLabel,
                isAutoFindActive = true,
                sortOption = CampsiteSortOption.DISTANCE
            )
        }
    }

    fun resetFilters() {
        _filterState.value = FilterState(areaLabel = _filterState.value.areaLabel)
    }

    fun navigateTo(destination: ScreenDestination) {
        val current = _screenStack.value
        _screenStack.value = current + destination
    }

    fun navigateBack(): Boolean {
        val current = _screenStack.value
        return if (current.size > 1) {
            _screenStack.value = current.dropLast(1)
            true
        } else {
            false
        }
    }

    fun getDistanceToSiteMiles(campsite: Campsite): Double {
        val coords = _userCoordinates.value
        return calculateDistanceMiles(coords.first, coords.second, campsite.latitude, campsite.longitude)
    }

    fun getFormattedDistanceToSite(campsite: Campsite): String {
        val dist = getDistanceToSiteMiles(campsite)
        return _unitSystem.value.formatDistance(dist)
    }

    fun toggleBookmark(campsite: Campsite) {
        viewModelScope.launch {
            repository.toggleBookmark(campsite.id, campsite.isBookmarked)
        }
    }

    fun addCustomCampsite(campsite: Campsite) {
        viewModelScope.launch {
            repository.addCustomCampsite(campsite)
        }
    }

    fun deleteCustomCampsite(id: String) {
        viewModelScope.launch {
            repository.deleteCustomCampsite(id)
            if (_screenStack.value.lastOrNull() is ScreenDestination.Detail) {
                navigateBack()
            }
        }
    }

    fun trySilentSignIn(activity: android.app.Activity) {
        viewModelScope.launch {
            val result = authRepository.trySilentSignIn(activity)
            if (result.isSuccess) {
                repository.startFirestoreCampsiteListener()
            }
        }
    }

    fun signInWithGoogle(
        activity: android.app.Activity,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            val result = authRepository.signInWithGoogle(activity)
            if (result.isSuccess) {
                repository.startFirestoreCampsiteListener()
                onSuccess()
            } else {
                onError(result.exceptionOrNull()?.message ?: "Google Sign-In failed")
            }
        }
    }

    fun updateCamperVehicleProfile(
        vehicleModel: String,
        vehicleHeight: String,
        vehicleWeight: String,
        licensePlate: String
    ) {
        viewModelScope.launch {
            authRepository.updateVehicleInfo(
                vehicleHeight = vehicleHeight,
                vehicleWeight = vehicleWeight,
                vehicleModel = vehicleModel,
                licensePlate = licensePlate
            )
        }
    }

    fun updateVehicleInfo(
        vehicleHeight: String?,
        vehicleWeight: String?,
        vehicleModel: String?,
        licensePlate: String?
    ) {
        viewModelScope.launch {
            authRepository.updateVehicleInfo(
                vehicleHeight = vehicleHeight,
                vehicleWeight = vehicleWeight,
                vehicleModel = vehicleModel,
                licensePlate = licensePlate
            )
        }
    }

    fun signOut() {
        authRepository.signOut()
    }

    fun manualSyncCloudData(onCompleted: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            val success = repository.refreshCloudData()
            onCompleted(success)
        }
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
                camperEmail = "", // Keep user email private
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
        val units = _unitSystem.value

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
                limitSpec = "Max clearance: ${units.formatVehicleHeight(maxClearance)}",
                rigSpec = "Your rig: ${units.formatVehicleHeight(rigHeight)}",
                isCompliant = heightPass,
                note = if (heightPass) "Fits under branch canopy & clearance gates" else "EXCEEDS height clearance!"
            ),
            LimitCheckItem(
                title = "Pad / Road Weight Capacity",
                limitSpec = "Max rating: ${units.formatVehicleWeight(maxWeight)}",
                rigSpec = "Your rig: ${units.formatVehicleWeight(rigWeight)}",
                isCompliant = weightPass,
                note = if (weightPass) "Safe for gravel/dirt pad weight" else "EXCEEDS pad limit!"
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

        val passCount = items.count { it.isCompliant }
        val allPass = passCount == items.size

        return CampsiteLimitsEvaluation(
            isFullyCompliant = allPass,
            passedCount = passCount,
            totalCount = items.size,
            summaryText = if (allPass) "100% Fit: Rig & trip parameters verified for this campsite." else "Warning: $passCount of ${items.size} checks pass.",
            items = items
        )
    }

    private fun parseHeightToFeet(heightStr: String?): Double? {
        if (heightStr.isNullOrBlank()) return null
        val digits = heightStr.filter { it.isDigit() || it == '.' }.toDoubleOrNull() ?: return null
        return if (heightStr.lowercase().contains("m")) {
            digits * 3.28084 // Convert meters to feet
        } else {
            digits
        }
    }

    private fun parseWeightToLbs(weightStr: String?): Int? {
        if (weightStr.isNullOrBlank()) return null
        val digits = weightStr.filter { it.isDigit() }.toIntOrNull() ?: return null
        return if (weightStr.lowercase().contains("kg")) {
            (digits * 2.20462).toInt() // Convert kg to lbs
        } else {
            digits
        }
    }

    fun toggleGearItem(id: String, isChecked: Boolean) {
        viewModelScope.launch {
            repository.toggleGearItem(id, isChecked)
        }
    }

    fun toggleGearChecked(item: GearItem) {
        viewModelScope.launch {
            repository.toggleGearItem(item.id, !item.isChecked)
        }
    }

    fun addCustomGearItem(title: String, category: GearCategory) {
        viewModelScope.launch {
            repository.addCustomGearItem(title, category)
        }
    }

    fun addCustomGear(title: String, subtitle: String = "", category: GearCategory) {
        viewModelScope.launch {
            repository.addCustomGearItem(title, category)
        }
    }

    fun removeGearItem(id: String) {
        viewModelScope.launch {
            repository.removeGearItem(id)
        }
    }

    private fun calculateDistanceMiles(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val r = 3958.8 // Radius of the earth in miles
        val dLat = Math.toRadians(lat2 - lat1)
        val dLon = Math.toRadians(lon2 - lon1)
        val a = sin(dLat / 2).pow(2.0) +
                cos(Math.toRadians(lat1)) * cos(Math.toRadians(lat2)) *
                sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))
        val distance = r * c
        return (distance * 10).roundToInt() / 10.0
    }
}
