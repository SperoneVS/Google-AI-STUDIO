package com.example.data.model

enum class PhotoTag(val label: String) {
    ALL("All Photos"),
    TENT_PITCH("Pitches & Sleep"),
    WATER_SOURCE("Water & Streams"),
    ENERGY_SOLAR("Power & Solar"),
    SCENIC_VIEWS("Scenic Views")
}

data class CampsitePhoto(
    val id: String,
    val caption: String,
    val photographer: String,
    val tag: PhotoTag,
    val timeOfDay: String,
    val gradientColors: List<Long>, // For rendering rich scenic photo illustrations
    val dominantIcon: String = "landscape"
)

enum class AmenityCategory(val title: String) {
    ESSENTIALS("Essentials & Sanitation"),
    SLEEP_SETUP("Sleeping & Ground Comfort"),
    POWER_UTILITIES("Power & Utilities"),
    RECREATION_NATURE("Trails & Natural Features")
}

enum class AmenityIcon {
    POTABLE_WATER,
    SHOWERS,
    TOILETS,
    TRASH_LOCKER,
    FIRE_PIT,
    PICNIC_TABLE,
    HAMMOCK_TREES,
    TENT_PAD,
    SHORE_POWER,
    SOLAR_ZONE,
    EV_CHARGER,
    PET_FRIENDLY,
    SWIMMING,
    TRAILHEAD,
    CELL_SIGNAL,
    RIVER_ACCESS
}

data class CampsiteAmenity(
    val id: String,
    val name: String,
    val category: AmenityCategory,
    val isAvailable: Boolean,
    val badgeLabel: String,
    val detailNote: String,
    val icon: AmenityIcon
)

data class CampsiteDetailedInfo(
    val campsite: Campsite,
    val photos: List<CampsitePhoto>,
    val amenities: List<CampsiteAmenity>,
    val seasonality: String,
    val maxStayNights: Int,
    val petPolicy: String,
    val wasteDisposal: String,
    val restroomType: String,
    val checkInCheckOut: String,
    val coordinatesFormatted: String,
    val permitRequired: Boolean,
    val permitInfo: String
)
