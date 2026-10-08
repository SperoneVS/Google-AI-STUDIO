package com.example.data.model

enum class SleepType(val label: String, val iconDesc: String) {
    TENT("Tent Pitch", "Pitch tent on ground or platform"),
    CAMPERVAN("Campervan / RV", "Level parking pad for vehicles"),
    HAMMOCK("Hammock Ready", "Spaced mature trees suited for hanging"),
    CABIN("Rustic Cabin / Shelter", "Wooden shelter or lean-to structure"),
    DISPERSED("Dispersed Wild", "Off-grid backcountry pitch")
}

enum class GroundType(val label: String) {
    SOFT_TURF("Soft Grassy Turf"),
    PINE_NEEDLES("Cushioned Pine Needles"),
    SAND("Pack Sand"),
    GRAVEL("Compacted Fine Gravel"),
    WOOD_PLATFORM("Elevated Wooden Deck")
}

enum class WaterSourceType(val label: String, val isPotable: Boolean) {
    POTABLE_TAP("Potable Drinking Tap", true),
    NATURAL_SPRING("Tested Fresh Spring", true),
    RIVER_FILTER_REQ("Stream / River (Filter Req)", false),
    SEASONAL_STREAM("Seasonal Runoff (Boil Req)", false),
    NO_WATER("Dry Camp (Pack in Water)", false)
}

enum class EnergySourceType(val label: String, val hasGridPower: Boolean) {
    FULL_HOOKUP_30_50A("30A / 50A Shore Hookup", true),
    STANDARD_15A_OUTLET("Standard 120V / 15A Outlet", true),
    SOLAR_CLEARING("Prime Solar Sky (Off-Grid)", false),
    RANGER_CHARGING_STATION("Station USB / Device Bank", true),
    OFF_GRID_ZERO("Zero Power (Dark Sky Zone)", false)
}

data class SleepDetails(
    val type: SleepType,
    val groundType: GroundType,
    val maxCapacity: Int,
    val hammockFriendly: Boolean,
    val shadeRating: Int, // 1 - 5 stars
    val quietHours: String,
    val elevationFt: Int
)

data class WaterDetails(
    val sourceType: WaterSourceType,
    val distanceToSourceMeters: Int,
    val hasHotShowers: Boolean,
    val hasColdShowers: Boolean,
    val hasDishwashingSink: Boolean,
    val flowReliability: String // e.g. "Year-round", "Spring through Fall"
)

data class EnergyDetails(
    val sourceType: EnergySourceType,
    val solarExposureIndex: Int, // 1 to 10
    val generatorAllowed: Boolean,
    val generatorHours: String,
    val campfireRing: Boolean,
    val firewoodPurchasable: Boolean,
    val hasEvCharging: Boolean
)

data class CampsiteLimits(
    val maxVehicleHeightFt: Double = 12.0, // Clearance in feet
    val maxVehicleWeightLbs: Int = 10000, // Pad limit in lbs
    val maxVehicleLengthFt: Int = 30, // Max RV/van pad length in feet
    val maxStayNights: Int = 14,
    val maxPeople: Int = 6,
    val quietHours: String = "10:00 PM - 7:00 AM",
    val generatorAllowed: Boolean = false,
    val generatorHours: String = "8:00 AM - 8:00 PM",
    val fireRestrictions: String = "Designated steel rings only"
)

data class Campsite(
    val id: String,
    val name: String,
    val region: String,
    val stateOrCountry: String,
    val latitude: Double,
    val longitude: Double,
    val feePerNight: String,
    val rating: Double,
    val reviewCount: Int,
    val sleep: SleepDetails,
    val water: WaterDetails,
    val energy: EnergyDetails,
    val cellReceptionBars: Int, // 0 to 5
    val terrainType: String, // Mountain, Alpine Lake, Redwoods, Canyon, Desert
    val description: String,
    val insiderTips: String,
    val photoUrl: String = "",
    val photoUrls: List<String> = emptyList(),
    val isPark4NightVerified: Boolean = false,
    val park4NightNote: String = "",
    val limits: CampsiteLimits = CampsiteLimits(),
    val isBookmarked: Boolean = false,
    val isUserCreated: Boolean = false
)

data class GearItem(
    val id: String,
    val category: GearCategory,
    val title: String,
    val subtitle: String,
    val isChecked: Boolean = false,
    val isRecommendedByCampSite: Boolean = false
)

enum class GearCategory(val label: String) {
    SLEEP("Sleep & Shelter"),
    WATER("Water & Hydration"),
    ENERGY("Energy & Lighting"),
    CAMP_COOKING("Cooking & Fire")
}
