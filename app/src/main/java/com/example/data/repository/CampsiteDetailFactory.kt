package com.example.data.repository

import com.example.data.model.*

object CampsiteDetailFactory {

    fun createDetailedInfo(campsite: Campsite): CampsiteDetailedInfo {
        val photos = generatePhotosForCampsite(campsite)
        val amenities = generateAmenitiesForCampsite(campsite)

        val latDeg = campsite.latitude.toInt()
        val latMin = ((Math.abs(campsite.latitude) - Math.abs(latDeg)) * 60).toInt()
        val lonDeg = campsite.longitude.toInt()
        val lonMin = ((Math.abs(campsite.longitude) - Math.abs(lonDeg)) * 60).toInt()
        val dms = "${latDeg}°${latMin}'N, ${Math.abs(lonDeg)}°${lonMin}'W"

        return CampsiteDetailedInfo(
            campsite = campsite,
            photos = photos,
            amenities = amenities,
            seasonality = if (campsite.sleep.elevationFt > 6000) "May 15 - October 31 (Snowpack dependent)" else "Year-Round Access",
            maxStayNights = 14,
            petPolicy = if (campsite.terrainType == "Desert Canyon") "Pets restricted on rim trails for safety" else "Leashed pets welcome (6-ft max leash)",
            wasteDisposal = if (campsite.feePerNight.contains("Free")) "Pack-it-in, Pack-it-out (Leave No Trace)" else "Bear-proof waste dumpsters & recycling on-site",
            restroomType = if (campsite.water.hasHotShowers) "Heated flush restrooms with private stalls" else if (campsite.water.sourceType == WaterSourceType.NO_WATER) "Primitive pack-out bags or remote pit toilet" else "Clean ventilated vault toilets",
            checkInCheckOut = "Check-in: 2:00 PM • Check-out: 11:00 AM",
            coordinatesFormatted = dms,
            permitRequired = campsite.feePerNight.contains("permit") || campsite.terrainType.contains("Glacial"),
            permitInfo = if (campsite.feePerNight.contains("permit")) "Self-issue backcountry permit at ranger station kiosk" else "First-come, first-served or online reservation"
        )
    }

    private fun generatePhotosForCampsite(campsite: Campsite): List<CampsitePhoto> {
        val isWaterRich = campsite.water.sourceType != WaterSourceType.NO_WATER
        val isDesert = campsite.terrainType.contains("Desert")
        val isCoast = campsite.terrainType.contains("Coastal")

        return listOf(
            CampsitePhoto(
                id = "${campsite.id}_p1",
                caption = "Level ${campsite.sleep.groundType.label.lowercase()} pitch with direct mountain sunrise view",
                photographer = "Elena Rostova • Verified Camper",
                tag = PhotoTag.TENT_PITCH,
                timeOfDay = "Golden Hour (7:15 AM)",
                gradientColors = if (isDesert) listOf(0xFF8B4513, 0xFFD2691E, 0xFFF4A460)
                                 else if (isCoast) listOf(0xFF1D3557, 0xFF457B9D, 0xFFA8DADC)
                                 else listOf(0xFF1B4332, 0xFF2D6A4F, 0xFF52B788),
                dominantIcon = "tent"
            ),
            CampsitePhoto(
                id = "${campsite.id}_p2",
                caption = if (isWaterRich) "${campsite.water.sourceType.label} flowing crisp and cold near site boundary"
                          else "Dry canyon canyonlands view with hydration staging station",
                photographer = "Marcus Vance • Backpacker",
                tag = PhotoTag.WATER_SOURCE,
                timeOfDay = "Midday (12:30 PM)",
                gradientColors = if (isWaterRich) listOf(0xFF0077B6, 0xFF0096C7, 0xFF48CAE4)
                                 else listOf(0xFFB08968, 0xFFDDB892, 0xFFEDE0D4),
                dominantIcon = "water"
            ),
            CampsitePhoto(
                id = "${campsite.id}_p3",
                caption = if (campsite.energy.sourceType.hasGridPower) "Pedestal hookup with dual 120V GFI and shore power outlet"
                          else "Unobstructed southern solar exposure index ${campsite.energy.solarExposureIndex}/10",
                photographer = "Dave Sterling • Overlander",
                tag = PhotoTag.ENERGY_SOLAR,
                timeOfDay = "Afternoon (3:45 PM)",
                gradientColors = listOf(0xFFD97706, 0xFFF59E0B, 0xFFFCD34D),
                dominantIcon = "bolt"
            ),
            CampsitePhoto(
                id = "${campsite.id}_p4",
                caption = "Campfire circle under certified International Dark-Sky Milky Way canopy",
                photographer = "Aria Chen • Astrophotographer",
                tag = PhotoTag.SCENIC_VIEWS,
                timeOfDay = "Night Sky (10:15 PM)",
                gradientColors = listOf(0xFF0B0C10, 0xFF1F2833, 0xFF45A29E),
                dominantIcon = "stars"
            )
        )
    }

    private fun generateAmenitiesForCampsite(campsite: Campsite): List<CampsiteAmenity> {
        val list = mutableListOf<CampsiteAmenity>()
        val isWaterRich = campsite.water.sourceType != WaterSourceType.NO_WATER

        // 1. Essentials
        list.add(
            CampsiteAmenity(
                id = "${campsite.id}_am_water",
                name = "Drinking Water",
                category = AmenityCategory.ESSENTIALS,
                isAvailable = campsite.water.sourceType.isPotable,
                badgeLabel = if (campsite.water.sourceType.isPotable) "Potable Tap" else "Filter Required",
                detailNote = "${campsite.water.sourceType.label} (${campsite.water.distanceToSourceMeters}m distance)",
                icon = AmenityIcon.POTABLE_WATER
            )
        )

        list.add(
            CampsiteAmenity(
                id = "${campsite.id}_am_showers",
                name = "Showers",
                category = AmenityCategory.ESSENTIALS,
                isAvailable = campsite.water.hasHotShowers || campsite.water.hasColdShowers,
                badgeLabel = if (campsite.water.hasHotShowers) "Hot Showers" else if (campsite.water.hasColdShowers) "Cold Rinse" else "None",
                detailNote = if (campsite.water.hasHotShowers) "Heated token-operated stalls" else "No showers on-site",
                icon = AmenityIcon.SHOWERS
            )
        )

        list.add(
            CampsiteAmenity(
                id = "${campsite.id}_am_toilets",
                name = "Restrooms",
                category = AmenityCategory.ESSENTIALS,
                isAvailable = true,
                badgeLabel = if (campsite.water.hasHotShowers) "Flush Toilets" else "Vault Toilets",
                detailNote = "Maintained daily by forest rangers",
                icon = AmenityIcon.TOILETS
            )
        )

        list.add(
            CampsiteAmenity(
                id = "${campsite.id}_am_trash",
                name = "Bear-Proof Storage",
                category = AmenityCategory.ESSENTIALS,
                isAvailable = true,
                badgeLabel = "Lockers Included",
                detailNote = "Metal food storage lockers at every campsite",
                icon = AmenityIcon.TRASH_LOCKER
            )
        )

        // 2. Sleep Setup
        list.add(
            CampsiteAmenity(
                id = "${campsite.id}_am_tentpad",
                name = "Pitch Type",
                category = AmenityCategory.SLEEP_SETUP,
                isAvailable = true,
                badgeLabel = campsite.sleep.groundType.label,
                detailNote = "Level cleared surface for up to ${campsite.sleep.maxCapacity} campers",
                icon = AmenityIcon.TENT_PAD
            )
        )

        list.add(
            CampsiteAmenity(
                id = "${campsite.id}_am_hammock",
                name = "Hammock Trees",
                category = AmenityCategory.SLEEP_SETUP,
                isAvailable = campsite.sleep.hammockFriendly,
                badgeLabel = if (campsite.sleep.hammockFriendly) "Hammock Ready" else "No Trees",
                detailNote = if (campsite.sleep.hammockFriendly) "Sturdy mature pine trees with 12-15ft spacing" else "Sparse brush / open desert",
                icon = AmenityIcon.HAMMOCK_TREES
            )
        )

        list.add(
            CampsiteAmenity(
                id = "${campsite.id}_am_picnic",
                name = "Picnic Table",
                category = AmenityCategory.SLEEP_SETUP,
                isAvailable = true,
                badgeLabel = "Heavy Timber",
                detailNote = "8-foot wooden or concrete park table with benches",
                icon = AmenityIcon.PICNIC_TABLE
            )
        )

        // 3. Power & Utilities
        list.add(
            CampsiteAmenity(
                id = "${campsite.id}_am_power",
                name = "Electric Hookup",
                category = AmenityCategory.POWER_UTILITIES,
                isAvailable = campsite.energy.sourceType.hasGridPower,
                badgeLabel = campsite.energy.sourceType.label,
                detailNote = if (campsite.energy.sourceType.hasGridPower) "Direct breaker pedestal at pitch" else "Off-grid battery or solar required",
                icon = AmenityIcon.SHORE_POWER
            )
        )

        list.add(
            CampsiteAmenity(
                id = "${campsite.id}_am_solar",
                name = "Solar Exposure",
                category = AmenityCategory.POWER_UTILITIES,
                isAvailable = campsite.energy.solarExposureIndex >= 6,
                badgeLabel = "${campsite.energy.solarExposureIndex} / 10 Solar Rating",
                detailNote = "Clear southern sky for portable solar panels",
                icon = AmenityIcon.SOLAR_ZONE
            )
        )

        list.add(
            CampsiteAmenity(
                id = "${campsite.id}_am_fire",
                name = "Campfire Ring",
                category = AmenityCategory.POWER_UTILITIES,
                isAvailable = campsite.energy.campfireRing,
                badgeLabel = if (campsite.energy.campfireRing) "Allowed in Ring" else "Fire Ban Active",
                detailNote = if (campsite.energy.firewoodPurchasable) "Seasoned wood bundle $7 at camp host" else "Bring your own firewood",
                icon = AmenityIcon.FIRE_PIT
            )
        )

        list.add(
            CampsiteAmenity(
                id = "${campsite.id}_am_ev",
                name = "EV / Rig Charging",
                category = AmenityCategory.POWER_UTILITIES,
                isAvailable = campsite.energy.hasEvCharging,
                badgeLabel = if (campsite.energy.hasEvCharging) "EV Compatible" else "None",
                detailNote = if (campsite.energy.hasEvCharging) "NEMA 14-50 240V Level 2 station" else "Standard vehicle parking only",
                icon = AmenityIcon.EV_CHARGER
            )
        )

        // 4. Recreation & Nature
        list.add(
            CampsiteAmenity(
                id = "${campsite.id}_am_trail",
                name = "Trailhead Access",
                category = AmenityCategory.RECREATION_NATURE,
                isAvailable = true,
                badgeLabel = "Direct Trailhead",
                detailNote = "Connects into national wilderness trail network",
                icon = AmenityIcon.TRAILHEAD
            )
        )

        list.add(
            CampsiteAmenity(
                id = "${campsite.id}_am_river",
                name = "Water Recreation",
                category = AmenityCategory.RECREATION_NATURE,
                isAvailable = isWaterRich,
                badgeLabel = if (isWaterRich) "River / Lake Access" else "Desert Vistas",
                detailNote = if (isWaterRich) "Swimming hole and trout fishing within walking distance" else "Expansive panoramic vista points",
                icon = AmenityIcon.RIVER_ACCESS
            )
        )

        list.add(
            CampsiteAmenity(
                id = "${campsite.id}_am_pets",
                name = "Pet Policy",
                category = AmenityCategory.RECREATION_NATURE,
                isAvailable = true,
                badgeLabel = "Pet Friendly",
                detailNote = "Dogs allowed on leash on campground perimeter",
                icon = AmenityIcon.PET_FRIENDLY
            )
        )

        return list
    }
}
