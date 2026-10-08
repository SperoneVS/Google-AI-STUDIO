package com.example.data.repository

import com.example.data.local.AppDatabase
import com.example.data.local.BookmarkEntity
import com.example.data.local.CampsiteEntity
import com.example.data.local.GearEntity
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import java.util.UUID

class CampsiteRepository(private val database: AppDatabase) {

    private val dao = database.campsiteDao()

    private val curatedCampsites: List<Campsite> = listOf(
        Campsite(
            id = "curated_1",
            name = "Whispering Pines Ridge",
            region = "Sierra High Country",
            stateOrCountry = "California, USA",
            latitude = 37.8651,
            longitude = -119.5383,
            feePerNight = "$18 / night",
            rating = 4.9,
            reviewCount = 142,
            sleep = SleepDetails(
                type = SleepType.TENT,
                groundType = GroundType.PINE_NEEDLES,
                maxCapacity = 6,
                hammockFriendly = true,
                shadeRating = 5,
                quietHours = "10:00 PM - 7:00 AM",
                elevationFt = 7400
            ),
            water = WaterDetails(
                sourceType = WaterSourceType.NATURAL_SPRING,
                distanceToSourceMeters = 35,
                hasHotShowers = false,
                hasColdShowers = true,
                hasDishwashingSink = true,
                flowReliability = "Year-round alpine spring"
            ),
            energy = EnergyDetails(
                sourceType = EnergySourceType.SOLAR_CLEARING,
                solarExposureIndex = 9,
                generatorAllowed = false,
                generatorHours = "Strictly prohibited (Silent zone)",
                campfireRing = true,
                firewoodPurchasable = true,
                hasEvCharging = false
            ),
            cellReceptionBars = 1,
            terrainType = "Alpine Forest",
            description = "Nestled under towering Jeffrey pines, offering naturally cushioned pine needle sleeping grounds and an icy sweet natural mineral spring.",
            insiderTips = "Sites 7 and 9 have perfect twin pines for hammocks directly facing the sunrise. Spring water is naturally filtered through granite."
        ),
        Campsite(
            id = "curated_2",
            name = "Silver River Hookup & Haven",
            region = "Olympic Foothills",
            stateOrCountry = "Washington, USA",
            latitude = 47.8021,
            longitude = -123.6044,
            feePerNight = "$34 / night",
            rating = 4.8,
            reviewCount = 210,
            sleep = SleepDetails(
                type = SleepType.CAMPERVAN,
                groundType = GroundType.GRAVEL,
                maxCapacity = 8,
                hammockFriendly = false,
                shadeRating = 4,
                quietHours = "10:00 PM - 8:00 AM",
                elevationFt = 820
            ),
            water = WaterDetails(
                sourceType = WaterSourceType.POTABLE_TAP,
                distanceToSourceMeters = 5,
                hasHotShowers = true,
                hasColdShowers = true,
                hasDishwashingSink = true,
                flowReliability = "Continuous municipal fresh tap"
            ),
            energy = EnergyDetails(
                sourceType = EnergySourceType.FULL_HOOKUP_30_50A,
                solarExposureIndex = 6,
                generatorAllowed = true,
                generatorHours = "8:00 AM - 8:00 PM only",
                campfireRing = true,
                firewoodPurchasable = true,
                hasEvCharging = true
            ),
            cellReceptionBars = 4,
            terrainType = "Rainforest Riverbank",
            description = "Prime spot for campervans, roof-tents, and overland rigs requiring shore power, clean running water, and hot showers after rainy hikes.",
            insiderTips = "Each hookup pedestal includes 50A/30A/20A dual GFI breakers. High pressure water connection with pressure regulator advised."
        ),
        Campsite(
            id = "curated_3",
            name = "Canyon Rim Echo Camp",
            region = "Red Rock Canyonlands",
            stateOrCountry = "Utah, USA",
            latitude = 38.5733,
            longitude = -109.5498,
            feePerNight = "Free (BLM Dispersed)",
            rating = 4.7,
            reviewCount = 88,
            sleep = SleepDetails(
                type = SleepType.DISPERSED,
                groundType = GroundType.SAND,
                maxCapacity = 4,
                hammockFriendly = false,
                shadeRating = 1,
                quietHours = "Natural Dark Sky Etiquette",
                elevationFt = 5100
            ),
            water = WaterDetails(
                sourceType = WaterSourceType.NO_WATER,
                distanceToSourceMeters = 0,
                hasHotShowers = false,
                hasColdShowers = false,
                hasDishwashingSink = false,
                flowReliability = "Zero natural water. Bring all water."
            ),
            energy = EnergyDetails(
                sourceType = EnergySourceType.SOLAR_CLEARING,
                solarExposureIndex = 10,
                generatorAllowed = false,
                generatorHours = "Solar only (No engines)",
                campfireRing = false,
                firewoodPurchasable = false,
                hasEvCharging = false
            ),
            cellReceptionBars = 2,
            terrainType = "Desert Canyon",
            description = "Legendary off-grid wilderness perched on the rim. Intense solar radiation makes battery recharge effortless, but hydration planning is critical.",
            insiderTips = "Mandatory rule: pack at least 6 to 8 liters of water per camper per day. Bring sand screw stakes for high evening canyon gusts."
        ),
        Campsite(
            id = "curated_4",
            name = "Emerald Creek Backcountry Shelter",
            region = "North Cascades",
            stateOrCountry = "Washington, USA",
            latitude = 48.7716,
            longitude = -121.2985,
            feePerNight = "$10 / permit",
            rating = 4.9,
            reviewCount = 65,
            sleep = SleepDetails(
                type = SleepType.CABIN,
                groundType = GroundType.WOOD_PLATFORM,
                maxCapacity = 6,
                hammockFriendly = true,
                shadeRating = 5,
                quietHours = "Backcountry quiet 9:00 PM",
                elevationFt = 4800
            ),
            water = WaterDetails(
                sourceType = WaterSourceType.RIVER_FILTER_REQ,
                distanceToSourceMeters = 20,
                hasHotShowers = false,
                hasColdShowers = false,
                hasDishwashingSink = false,
                flowReliability = "Glacial fed stream (High flow)"
            ),
            energy = EnergyDetails(
                sourceType = EnergySourceType.OFF_GRID_ZERO,
                solarExposureIndex = 3,
                generatorAllowed = false,
                generatorHours = "Strictly off-grid quiet sanctuary",
                campfireRing = true,
                firewoodPurchasable = false,
                hasEvCharging = false
            ),
            cellReceptionBars = 0,
            terrainType = "Glacial Valley",
            description = "Solid cedar three-sided wilderness shelter protecting campers from mountain storms. Fast-running glacial stream provides unlimited fresh cold water.",
            insiderTips = "Stream is sediment-free 50m upstream of bridge. A gravity 0.1 micron filter processes 4L in under 3 minutes here."
        ),
        Campsite(
            id = "curated_5",
            name = "Lakeside Pines & Electric Harbor",
            region = "Lake Tahoe Basin",
            stateOrCountry = "Nevada / California",
            latitude = 39.0968,
            longitude = -120.0324,
            feePerNight = "$28 / night",
            rating = 4.8,
            reviewCount = 340,
            sleep = SleepDetails(
                type = SleepType.TENT,
                groundType = GroundType.SOFT_TURF,
                maxCapacity = 8,
                hammockFriendly = true,
                shadeRating = 4,
                quietHours = "10:00 PM - 7:00 AM",
                elevationFt = 6225
            ),
            water = WaterDetails(
                sourceType = WaterSourceType.POTABLE_TAP,
                distanceToSourceMeters = 15,
                hasHotShowers = true,
                hasColdShowers = true,
                hasDishwashingSink = true,
                flowReliability = "Heated facilities & continuous taps"
            ),
            energy = EnergyDetails(
                sourceType = EnergySourceType.STANDARD_15A_OUTLET,
                solarExposureIndex = 7,
                generatorAllowed = false,
                generatorHours = "No generators needed - pedestal AC available",
                campfireRing = true,
                firewoodPurchasable = true,
                hasEvCharging = true
            ),
            cellReceptionBars = 5,
            terrainType = "Alpine Lake",
            description = "The ultimate balance of wilderness beauty and camping utilities. Soft grass for sleeping mats, filtered potable fountains, and charging pillars.",
            insiderTips = "Sites along the north shoreline (12-18) get direct lake breeze keeping tents bug-free all summer."
        ),
        Campsite(
            id = "curated_6",
            name = "Starfall Mesa Hammock Haven",
            region = "Coconino High Forest",
            stateOrCountry = "Arizona, USA",
            latitude = 35.1983,
            longitude = -111.6513,
            feePerNight = "$12 / night",
            rating = 4.6,
            reviewCount = 92,
            sleep = SleepDetails(
                type = SleepType.HAMMOCK,
                groundType = GroundType.PINE_NEEDLES,
                maxCapacity = 4,
                hammockFriendly = true,
                shadeRating = 4,
                quietHours = "9:30 PM - 6:00 AM",
                elevationFt = 6900
            ),
            water = WaterDetails(
                sourceType = WaterSourceType.SEASONAL_STREAM,
                distanceToSourceMeters = 60,
                hasHotShowers = false,
                hasColdShowers = false,
                hasDishwashingSink = false,
                flowReliability = "Active March to August (Boil / Filter)"
            ),
            energy = EnergyDetails(
                sourceType = EnergySourceType.RANGER_CHARGING_STATION,
                solarExposureIndex = 8,
                generatorAllowed = false,
                generatorHours = "No generators",
                campfireRing = true,
                firewoodPurchasable = true,
                hasEvCharging = false
            ),
            cellReceptionBars = 3,
            terrainType = "Ponderosa Plateau",
            description = "Stupendous canopy of mature Ponderosa pines specifically maintained for hammock straps, paired with a solar ranger charging kiosk for batteries.",
            insiderTips = "Ranger station has a 12-port USB-C fast charging locker accessible between 7am and 8pm."
        ),
        Campsite(
            id = "curated_7",
            name = "Mistwood Cove Surf Camp",
            region = "Big Sur Coastline",
            stateOrCountry = "California, USA",
            latitude = 36.2704,
            longitude = -121.8081,
            feePerNight = "$30 / night",
            rating = 4.9,
            reviewCount = 180,
            sleep = SleepDetails(
                type = SleepType.CAMPERVAN,
                groundType = GroundType.WOOD_PLATFORM,
                maxCapacity = 6,
                hammockFriendly = true,
                shadeRating = 3,
                quietHours = "10:00 PM - 7:00 AM",
                elevationFt = 150
            ),
            water = WaterDetails(
                sourceType = WaterSourceType.POTABLE_TAP,
                distanceToSourceMeters = 25,
                hasHotShowers = true,
                hasColdShowers = true,
                hasDishwashingSink = true,
                flowReliability = "Year-round coastal potable system"
            ),
            energy = EnergyDetails(
                sourceType = EnergySourceType.STANDARD_15A_OUTLET,
                solarExposureIndex = 7,
                generatorAllowed = false,
                generatorHours = "No loud generators",
                campfireRing = true,
                firewoodPurchasable = true,
                hasEvCharging = false
            ),
            cellReceptionBars = 3,
            terrainType = "Coastal Cliffs",
            description = "Wake up to Pacific mist and waves. Elevated redwood sleeping decks keep tents dry, with fresh tap water and warm rinse showers.",
            insiderTips = "Evening coastal fog reduces solar after 5pm, so charge daytime power packs early between 11am and 3pm."
        )
    )

    fun getCampsitesFlow(): Flow<List<Campsite>> {
        val customFlow = dao.getAllCustomCampsites().map { entities ->
            entities.map { it.toDomainModel() }
        }
        val bookmarkFlow = dao.getAllBookmarks().map { bookmarks ->
            bookmarks.map { it.campsiteId }.toSet()
        }

        return combine(customFlow, bookmarkFlow) { customList, bookmarkedIds ->
            val all = curatedCampsites + customList
            all.map { site ->
                site.copy(isBookmarked = bookmarkedIds.contains(site.id))
            }
        }
    }

    suspend fun toggleBookmark(campsiteId: String, currentStatus: Boolean) {
        if (currentStatus) {
            dao.removeBookmark(campsiteId)
        } else {
            dao.insertBookmark(BookmarkEntity(campsiteId = campsiteId))
        }
    }

    suspend fun addCustomCampsite(campsite: Campsite) {
        val cleanSite = campsite.copy(name = cleanCampsiteName(campsite.name))
        dao.insertCustomCampsite(cleanSite.toEntity())
        dao.insertCampsite(cleanSite.toRoomCampsite())
    }

    suspend fun deleteCustomCampsite(id: String) {
        dao.deleteCustomCampsite(id)
        dao.deleteCampsite(id)
    }

    suspend fun seedInitialCampsites() {
        val roomEntities = curatedCampsites.map { it.toRoomCampsite() }
        dao.insertAllCampsites(roomEntities)
    }

    fun getAllRoomCampsitesFlow(): Flow<List<com.example.data.local.Campsite>> {
        return dao.getAllCampsites()
    }

    // Default Gear Items
    val defaultGearList: List<GearEntity> = listOf(
        // Sleep
        GearEntity("g_sleep_1", GearCategory.SLEEP.name, "3-Season Tent / Rainfly", "Seam sealed with wind stakes", false),
        GearEntity("g_sleep_2", GearCategory.SLEEP.name, "Insulated Sleeping Pad", "R-value 3.5+ for ground insulation", false),
        GearEntity("g_sleep_3", GearCategory.SLEEP.name, "Sleeping Bag (Down/Synthetic)", "Rated 5°C below expected low", false),
        GearEntity("g_sleep_4", GearCategory.SLEEP.name, "Ground Tarp & Footprint", "Protects tent floor from damp ground", false),
        GearEntity("g_sleep_5", GearCategory.SLEEP.name, "Hammock & Tree-Saver Straps", "Minimum 1-inch webbing for tree health", false),
        // Water
        GearEntity("g_water_1", GearCategory.WATER.name, "Gravity Water Filter (0.1 Micron)", "Hollow fiber membrane for stream water", false),
        GearEntity("g_water_2", GearCategory.WATER.name, "Wide-Mouth Water Bottles / Bladder", "2L to 4L hydration capacity", false),
        GearEntity("g_water_3", GearCategory.WATER.name, "Collapsible 10L Water Camp Bucket", "For washing & carrying stream water", false),
        GearEntity("g_water_4", GearCategory.WATER.name, "Water Purification Tablets", "Emergency chlorine dioxide backup", false),
        // Energy
        GearEntity("g_energy_1", GearCategory.ENERGY.name, "20,000mAh Rugged Power Bank", "Dual USB-C PD 30W output", false),
        GearEntity("g_energy_2", GearCategory.ENERGY.name, "Foldable 28W - 100W Solar Panel", "SunPower cells with carabiner loops", false),
        GearEntity("g_energy_3", GearCategory.ENERGY.name, "Rechargeable Headlamp (350+ lm)", "With red night-vision LED mode", false),
        GearEntity("g_energy_4", GearCategory.ENERGY.name, "Camp Lantern & Ambient String", "Warm 2700K campsite illumination", false),
        GearEntity("g_energy_5", GearCategory.ENERGY.name, "120V / 30A RV Dogbone Adapter", "Converts 30A pedestal to standard plug", false),
        // Cooking & Fire
        GearEntity("g_fire_1", GearCategory.CAMP_COOKING.name, "Windproof Isobutane Camp Stove", "Fast boiling time in mountain wind", false),
        GearEntity("g_fire_2", GearCategory.CAMP_COOKING.name, "Waterproof Storm Matches & Ferro Rod", "Reliable ignition in damp weather", false),
        GearEntity("g_fire_3", GearCategory.CAMP_COOKING.name, "Anodized Cookware Pot & Spork", "Compact nesting kit", false),
        GearEntity("g_fire_4", GearCategory.CAMP_COOKING.name, "Biodegradable Camp Soap", "Use at least 60m away from water source", false)
    )

    fun getGearListFlow(): Flow<List<GearItem>> {
        return dao.getAllGearItems().map { list ->
            if (list.isEmpty()) {
                defaultGearList.map { it.toGearItem() }
            } else {
                list.map { it.toGearItem() }
            }
        }
    }

    suspend fun initializeDefaultGearIfEmpty() {
        // Will check in ViewModel and seed if empty
    }

    suspend fun updateGearChecked(id: String, isChecked: Boolean) {
        dao.updateGearChecked(id, isChecked)
    }

    suspend fun seedInitialGear() {
        dao.insertAllGearItems(defaultGearList)
    }

    suspend fun addNewGear(title: String, subtitle: String, category: GearCategory) {
        val newEntity = GearEntity(
            id = "custom_gear_" + UUID.randomUUID().toString().take(8),
            category = category.name,
            title = title,
            subtitle = subtitle,
            isChecked = false
        )
        dao.insertGearItem(newEntity)
    }

    fun getReviewsFlow(campsiteId: String): Flow<List<com.example.data.local.CampsiteReviewEntity>> {
        return dao.getReviewsForCampsite(campsiteId)
    }

    suspend fun submitReview(review: com.example.data.local.CampsiteReviewEntity) {
        dao.insertReview(review)
    }
}

fun cleanCampsiteName(rawName: String): String {
    return rawName
        .replace(Regex("(?i)live\\s*gps\\s*location"), "Pine Valley")
        .replace(Regex("(?i)live\\s*location"), "Pine Valley")
        .replace(Regex("(?i)live\\s*gps"), "Pine Valley")
        .replace(Regex("(?i)live"), "")
        .trim()
        .ifBlank { "Wilderness Haven" }
}

// Extension mappers
fun Campsite.toRoomCampsite(): com.example.data.local.Campsite {
    return com.example.data.local.Campsite(
        id = id,
        name = cleanCampsiteName(name),
        sleepingSetup = sleep.type.label,
        waterAvailability = water.sourceType.label,
        energyHookupStatus = energy.sourceType.label,
        region = region,
        stateOrCountry = stateOrCountry,
        latitude = latitude,
        longitude = longitude,
        feePerNight = feePerNight,
        rating = rating,
        reviewCount = reviewCount,
        terrainType = terrainType,
        description = description,
        maxVehicleHeightFt = limits.maxVehicleHeightFt,
        maxVehicleWeightLbs = limits.maxVehicleWeightLbs,
        maxVehicleLengthFt = limits.maxVehicleLengthFt,
        maxStayNights = limits.maxStayNights,
        maxPeople = limits.maxPeople,
        isUserCreated = isUserCreated
    )
}

private fun CampsiteEntity.toDomainModel(): Campsite {
    val cleanName = cleanCampsiteName(name)

    return Campsite(
        id = id,
        name = cleanName,
        region = region,
        stateOrCountry = stateOrCountry,
        latitude = latitude,
        longitude = longitude,
        feePerNight = feePerNight,
        rating = rating,
        reviewCount = reviewCount,
        sleep = SleepDetails(
            type = runCatching { SleepType.valueOf(sleepType) }.getOrDefault(SleepType.TENT),
            groundType = runCatching { GroundType.valueOf(groundType) }.getOrDefault(GroundType.SOFT_TURF),
            maxCapacity = maxCapacity,
            hammockFriendly = hammockFriendly,
            shadeRating = shadeRating,
            quietHours = quietHours,
            elevationFt = elevationFt
        ),
        water = WaterDetails(
            sourceType = runCatching { WaterSourceType.valueOf(waterSourceType) }.getOrDefault(WaterSourceType.POTABLE_TAP),
            distanceToSourceMeters = distanceToSourceMeters,
            hasHotShowers = hasHotShowers,
            hasColdShowers = hasColdShowers,
            hasDishwashingSink = hasDishwashingSink,
            flowReliability = flowReliability
        ),
        energy = EnergyDetails(
            sourceType = runCatching { EnergySourceType.valueOf(energySourceType) }.getOrDefault(EnergySourceType.SOLAR_CLEARING),
            solarExposureIndex = solarExposureIndex,
            generatorAllowed = generatorAllowed,
            generatorHours = generatorHours,
            campfireRing = campfireRing,
            firewoodPurchasable = firewoodPurchasable,
            hasEvCharging = hasEvCharging
        ),
        cellReceptionBars = cellReceptionBars,
        terrainType = terrainType,
        description = description,
        insiderTips = insiderTips,
        isBookmarked = false,
        isUserCreated = isUserCreated
    )
}

private fun Campsite.toEntity(): CampsiteEntity {
    return CampsiteEntity(
        id = id,
        name = name,
        region = region,
        stateOrCountry = stateOrCountry,
        latitude = latitude,
        longitude = longitude,
        feePerNight = feePerNight,
        rating = rating,
        reviewCount = reviewCount,
        sleepType = sleep.type.name,
        groundType = sleep.groundType.name,
        maxCapacity = sleep.maxCapacity,
        hammockFriendly = sleep.hammockFriendly,
        shadeRating = sleep.shadeRating,
        quietHours = sleep.quietHours,
        elevationFt = sleep.elevationFt,
        waterSourceType = water.sourceType.name,
        distanceToSourceMeters = water.distanceToSourceMeters,
        hasHotShowers = water.hasHotShowers,
        hasColdShowers = water.hasColdShowers,
        hasDishwashingSink = water.hasDishwashingSink,
        flowReliability = water.flowReliability,
        energySourceType = energy.sourceType.name,
        solarExposureIndex = energy.solarExposureIndex,
        generatorAllowed = energy.generatorAllowed,
        generatorHours = energy.generatorHours,
        campfireRing = energy.campfireRing,
        firewoodPurchasable = energy.firewoodPurchasable,
        hasEvCharging = energy.hasEvCharging,
        cellReceptionBars = cellReceptionBars,
        terrainType = terrainType,
        description = description,
        insiderTips = insiderTips,
        isUserCreated = true
    )
}

private fun GearEntity.toGearItem(): GearItem {
    return GearItem(
        id = id,
        category = runCatching { GearCategory.valueOf(category) }.getOrDefault(GearCategory.SLEEP),
        title = title,
        subtitle = subtitle,
        isChecked = isChecked,
        isRecommendedByCampSite = false
    )
}
