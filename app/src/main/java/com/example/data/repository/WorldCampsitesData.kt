package com.example.data.repository

import com.example.data.model.*

/**
 * World-wide campsites covering major global camping destinations across:
 * - 🇬🇧 United Kingdom (Scotland & Lake District)
 * - 🇮🇸 Iceland (South Coast & Golden Circle)
 * - 🇨🇦 Canada (Banff & Jasper, Canadian Rockies)
 * - 🇦🇺 Australia (Great Ocean Road & Blue Mountains)
 * - 🇳🇿 New Zealand (Fiordland & South Island)
 * - 🇯🇵 Japan (Mount Fuji & Japanese Alps)
 * - 🇿🇦 South Africa (Kruger & Garden Route)
 * - 🇨🇱 Chile & Argentina (Patagonia Torres del Paine)
 */
object WorldCampsitesData {

    val worldCampsites: List<Campsite> = listOf(
        // ==========================================
        // 🇬🇧 UNITED KINGDOM: SCOTLAND & LAKE DISTRICT
        // ==========================================
        Campsite(
            id = "world_uk_1",
            name = "Glenbrittle Campsite & Fairy Pools",
            region = "Isle of Skye, Highlands",
            stateOrCountry = "United Kingdom",
            latitude = 57.2045,
            longitude = -6.2891,
            feePerNight = "£16 / night",
            rating = 4.9,
            reviewCount = 380,
            sleep = SleepDetails(
                type = SleepType.CAMPERVAN,
                groundType = GroundType.SOFT_TURF,
                maxCapacity = 6,
                hammockFriendly = false,
                shadeRating = 2,
                quietHours = "10:30 PM - 7:00 AM",
                elevationFt = 120
            ),
            water = WaterDetails(
                sourceType = WaterSourceType.NATURAL_SPRING,
                distanceToSourceMeters = 15,
                hasHotShowers = true,
                hasColdShowers = true,
                hasDishwashingSink = true,
                flowReliability = "Cuillin mountain spring tap"
            ),
            energy = EnergyDetails(
                sourceType = EnergySourceType.FULL_HOOKUP_30_50A,
                solarExposureIndex = 6,
                generatorAllowed = false,
                generatorHours = "No generators",
                campfireRing = false,
                firewoodPurchasable = false,
                hasEvCharging = true
            ),
            cellReceptionBars = 3,
            terrainType = "Coastal Cliffs",
            description = "At the foot of the dramatic Black Cuillin mountains on the edge of Loch Brittle beach. Famous artisan coffee shop on site, direct trailheads to the crystalline Fairy Pools waterfalls.",
            insiderTips = "Pitches right against the sea loch have unmatched Atlantic sunsets. Fresh sourdough bread and Skye coffee roasted on site every morning.",
            photoUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800",
            photoUrls = listOf(
                "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800",
                "https://images.unsplash.com/photo-1470246973918-29a93221c455?w=800"
            ),
            isPark4NightVerified = true,
            park4NightNote = "Park4night #29401 • Iconic Isle of Skye camp. Beachfront van pitches, warm power showers, Cuillin mountain backdrop."
        ),
        Campsite(
            id = "world_uk_2",
            name = "Wasdale Head National Trust Camp",
            region = "Lake District, Cumbria",
            stateOrCountry = "United Kingdom",
            latitude = 54.4682,
            longitude = -3.2558,
            feePerNight = "£18 / night",
            rating = 4.8,
            reviewCount = 265,
            sleep = SleepDetails(
                type = SleepType.TENT,
                groundType = GroundType.SOFT_TURF,
                maxCapacity = 4,
                hammockFriendly = false,
                shadeRating = 3,
                quietHours = "10:00 PM - 7:00 AM",
                elevationFt = 260
            ),
            water = WaterDetails(
                sourceType = WaterSourceType.POTABLE_TAP,
                distanceToSourceMeters = 20,
                hasHotShowers = true,
                hasColdShowers = true,
                hasDishwashingSink = true,
                flowReliability = "Pure lakeland stream tap"
            ),
            energy = EnergyDetails(
                sourceType = EnergySourceType.STANDARD_15A_OUTLET,
                solarExposureIndex = 7,
                generatorAllowed = false,
                generatorHours = "Dark sky nature reserve",
                campfireRing = false,
                firewoodPurchasable = false,
                hasEvCharging = false
            ),
            cellReceptionBars = 1,
            terrainType = "Glacial Valley",
            description = "The birthplace of British rock climbing under England's highest peak Scafell Pike and beside Wastwater, England's deepest lake. Ancient stone walls and dramatic fells.",
            insiderTips = "Historic Wasdale Head Inn is a short stroll away for hearty local ales. Trailhead to Scafell Pike starts right beside the camp.",
            photoUrl = "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=800",
            photoUrls = listOf("https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=800"),
            isPark4NightVerified = true,
            park4NightNote = "Park4night #48123 • National Trust lakeside haven. Breathtaking fell views, perfect Scafell base."
        ),

        // ==========================================
        // 🇮🇸 ICELAND: SOUTH COAST & VOLCANOES
        // ==========================================
        Campsite(
            id = "world_is_1",
            name = "Skaftafell Glacier National Camp",
            region = "Vatnajökull National Park",
            stateOrCountry = "Iceland",
            latitude = 64.0163,
            longitude = -16.9667,
            feePerNight = "2,500 ISK (€17)",
            rating = 4.9,
            reviewCount = 410,
            sleep = SleepDetails(
                type = SleepType.CAMPERVAN,
                groundType = GroundType.GRAVEL,
                maxCapacity = 6,
                hammockFriendly = false,
                shadeRating = 2,
                quietHours = "11:00 PM - 7:00 AM",
                elevationFt = 330
            ),
            water = WaterDetails(
                sourceType = WaterSourceType.NATURAL_SPRING,
                distanceToSourceMeters = 25,
                hasHotShowers = true,
                hasColdShowers = true,
                hasDishwashingSink = true,
                flowReliability = "Sub-glacial volcanic spring tap"
            ),
            energy = EnergyDetails(
                sourceType = EnergySourceType.FULL_HOOKUP_30_50A,
                solarExposureIndex = 9,
                generatorAllowed = false,
                generatorHours = "Strictly no generators",
                campfireRing = false,
                firewoodPurchasable = false,
                hasEvCharging = true
            ),
            cellReceptionBars = 5,
            terrainType = "Glacial Valley",
            description = "Protected in a birch forest oasis underneath the gigantic Vatnajökull ice cap. Direct hikes to Svartifoss black basalt column waterfall and Skaftafellsjökull glacier tongue.",
            insiderTips = "Section D is reserved for campervans with European CEE electrical hookups. Heated shower pavilion and year-round visitor center.",
            photoUrl = "https://images.unsplash.com/photo-1519681393784-d120267933ba?w=800",
            photoUrls = listOf("https://images.unsplash.com/photo-1519681393784-d120267933ba?w=800"),
            isPark4NightVerified = true,
            park4NightNote = "Park4night #72011 • Top rated Iceland camper stop. Glacier hiking base, clean heated facilities."
        ),
        Campsite(
            id = "world_is_2",
            name = "Hamragarðar Gljúfrabúi Waterfall Camp",
            region = "South Region, Hvolsvöllur",
            stateOrCountry = "Iceland",
            latitude = 63.6208,
            longitude = -19.9882,
            feePerNight = "2,000 ISK (€14)",
            rating = 4.8,
            reviewCount = 330,
            sleep = SleepDetails(
                type = SleepType.CAMPERVAN,
                groundType = GroundType.SOFT_TURF,
                maxCapacity = 4,
                hammockFriendly = false,
                shadeRating = 2,
                quietHours = "Midnight Sun Serenity",
                elevationFt = 190
            ),
            water = WaterDetails(
                sourceType = WaterSourceType.POTABLE_TAP,
                distanceToSourceMeters = 15,
                hasHotShowers = true,
                hasColdShowers = true,
                hasDishwashingSink = true,
                flowReliability = "Fresh spring tap"
            ),
            energy = EnergyDetails(
                sourceType = EnergySourceType.STANDARD_15A_OUTLET,
                solarExposureIndex = 9,
                generatorAllowed = false,
                generatorHours = "Quiet hours enforced",
                campfireRing = false,
                firewoodPurchasable = false,
                hasEvCharging = false
            ),
            cellReceptionBars = 4,
            terrainType = "Coastal Cliffs",
            description = "Camp directly in front of the hidden canyon waterfall Gljúfrabúi and a 5-minute walk from world-famous Seljalandsfoss where you can walk behind the roaring cascade.",
            insiderTips = "Waterproof gear is mandatory to wade into the canyon to see Gljúfrabúi. Grassy level parking for campervans with fresh water filling station.",
            photoUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800",
            photoUrls = listOf("https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800"),
            isPark4NightVerified = true,
            park4NightNote = "Park4night #19208 • Wake up directly next to Seljalandsfoss waterfall. Clean toilets and hot showers."
        ),

        // ==========================================
        // 🇨🇦 CANADA: BANFF, JASPER & CANADIAN ROCKIES
        // ==========================================
        Campsite(
            id = "world_ca_1",
            name = "Two Jack Lakeside Campground (Banff)",
            region = "Banff National Park, Alberta",
            stateOrCountry = "Canada",
            latitude = 51.1982,
            longitude = -115.4988,
            feePerNight = "CAD $30 / night",
            rating = 4.9,
            reviewCount = 490,
            sleep = SleepDetails(
                type = SleepType.TENT,
                groundType = GroundType.PINE_NEEDLES,
                maxCapacity = 6,
                hammockFriendly = true,
                shadeRating = 5,
                quietHours = "11:00 PM - 7:00 AM",
                elevationFt = 4850
            ),
            water = WaterDetails(
                sourceType = WaterSourceType.POTABLE_TAP,
                distanceToSourceMeters = 10,
                hasHotShowers = true,
                hasColdShowers = true,
                hasDishwashingSink = true,
                flowReliability = "Continuous mountain glacial tap"
            ),
            energy = EnergyDetails(
                sourceType = EnergySourceType.SOLAR_CLEARING,
                solarExposureIndex = 8,
                generatorAllowed = false,
                generatorHours = "Strictly generator-free sanctuary",
                campfireRing = true,
                firewoodPurchasable = true,
                hasEvCharging = false
            ),
            cellReceptionBars = 3,
            terrainType = "Alpine Forest",
            description = "Nestled right along the emerald waters of Two Jack Lake with Mount Rundle reflecting in the still morning glass. World-class canoeing, elk watching, and campfire evenings under starlit skies.",
            insiderTips = "Walk-in waterfront tent sites (Sites 20-35) have private lake beach access. Firewood permit includes unlimited dry split lodgepole pine logs.",
            photoUrl = "https://images.unsplash.com/photo-1533873984035-25970ab07461?w=800",
            photoUrls = listOf("https://images.unsplash.com/photo-1533873984035-25970ab07461?w=800"),
            isPark4NightVerified = true,
            park4NightNote = "Park4night #64190 • Prime Banff lakeside camp. Bear-proof lockers, heated washhouses, pristine alpine lake."
        ),
        Campsite(
            id = "world_ca_2",
            name = "Whistlers Campground (Jasper)",
            region = "Jasper National Park, Alberta",
            stateOrCountry = "Canada",
            latitude = 52.8427,
            longitude = -118.0833,
            feePerNight = "CAD $38 / night",
            rating = 4.8,
            reviewCount = 370,
            sleep = SleepDetails(
                type = SleepType.CAMPERVAN,
                groundType = GroundType.GRAVEL,
                maxCapacity = 8,
                hammockFriendly = true,
                shadeRating = 4,
                quietHours = "11:00 PM - 7:00 AM",
                elevationFt = 3600
            ),
            water = WaterDetails(
                sourceType = WaterSourceType.POTABLE_TAP,
                distanceToSourceMeters = 8,
                hasHotShowers = true,
                hasColdShowers = true,
                hasDishwashingSink = true,
                flowReliability = "Full service chlorinated municipal spring"
            ),
            energy = EnergyDetails(
                sourceType = EnergySourceType.FULL_HOOKUP_30_50A,
                solarExposureIndex = 8,
                generatorAllowed = true,
                generatorHours = "9:00 AM - 10:30 AM & 5:00 PM - 7:00 PM",
                campfireRing = true,
                firewoodPurchasable = true,
                hasEvCharging = true
            ),
            cellReceptionBars = 5,
            terrainType = "Alpine Forest",
            description = "Recently modernized massive park camp with full RV/Campervan 50A hookups, state-of-the-art washrooms, and wild elk roaming peacefully through the spruce trees.",
            insiderTips = "Short drive to Jasper SkyTram and Maligne Canyon. Located inside the world's second-largest Dark Sky Preserve.",
            photoUrl = "https://images.unsplash.com/photo-1510312305653-8ed496efae75?w=800",
            photoUrls = listOf("https://images.unsplash.com/photo-1510312305653-8ed496efae75?w=800"),
            isPark4NightVerified = true,
            park4NightNote = "Park4night #81023 • Best full-service rig park in Jasper. 50A hookup, heated showers, incredible mountain peaks."
        ),

        // ==========================================
        // 🇦🇺 AUSTRALIA: GREAT OCEAN ROAD & BLUE MOUNTAINS
        // ==========================================
        Campsite(
            id = "world_au_1",
            name = "Blanket Bay Campground (Great Ocean Road)",
            region = "Great Otway National Park, Victoria",
            stateOrCountry = "Australia",
            latitude = -38.8291,
            longitude = 143.5878,
            feePerNight = "AUD $28 / night",
            rating = 4.9,
            reviewCount = 310,
            sleep = SleepDetails(
                type = SleepType.CAMPERVAN,
                groundType = GroundType.SOFT_TURF,
                maxCapacity = 6,
                hammockFriendly = true,
                shadeRating = 5,
                quietHours = "10:00 PM - 7:00 AM",
                elevationFt = 80
            ),
            water = WaterDetails(
                sourceType = WaterSourceType.SEASONAL_STREAM,
                distanceToSourceMeters = 30,
                hasHotShowers = false,
                hasColdShowers = false,
                hasDishwashingSink = false,
                flowReliability = "Rainwater tank (Boil/Filter before drinking)"
            ),
            energy = EnergyDetails(
                sourceType = EnergySourceType.SOLAR_CLEARING,
                solarExposureIndex = 9,
                generatorAllowed = false,
                generatorHours = "Strictly zero generators",
                campfireRing = true,
                firewoodPurchasable = false,
                hasEvCharging = false
            ),
            cellReceptionBars = 2,
            terrainType = "Coastal Cliffs",
            description = "Secluded ocean cove beneath towering manna gum trees alive with wild koalas. Direct access to a protected golden sand beach on the Southern Ocean along the Great Ocean Walk.",
            insiderTips = "Look up in the eucalyptus trees directly above camp pitches for snoozing koalas. Bring drinking water as tanks rely on rainwater.",
            photoUrl = "https://images.unsplash.com/photo-1518495973542-4542c06a5843?w=800",
            photoUrls = listOf("https://images.unsplash.com/photo-1518495973542-4542c06a5843?w=800"),
            isPark4NightVerified = true,
            park4NightNote = "Park4night #95012 • Unbelievable Australian bush and ocean camp. Wild koalas in trees, private beach cove."
        ),
        Campsite(
            id = "world_au_2",
            name = "Euroka Clearing Campground",
            region = "Blue Mountains National Park, New South Wales",
            stateOrCountry = "Australia",
            latitude = -33.7915,
            longitude = 150.6015,
            feePerNight = "AUD $24 / night",
            rating = 4.8,
            reviewCount = 220,
            sleep = SleepDetails(
                type = SleepType.TENT,
                groundType = GroundType.SOFT_TURF,
                maxCapacity = 6,
                hammockFriendly = true,
                shadeRating = 4,
                quietHours = "10:00 PM - 7:00 AM",
                elevationFt = 580
            ),
            water = WaterDetails(
                sourceType = WaterSourceType.NO_WATER,
                distanceToSourceMeters = 0,
                hasHotShowers = false,
                hasColdShowers = false,
                hasDishwashingSink = false,
                flowReliability = "Pack in all drinking and cooking water"
            ),
            energy = EnergyDetails(
                sourceType = EnergySourceType.OFF_GRID_ZERO,
                solarExposureIndex = 8,
                generatorAllowed = false,
                generatorHours = "No generators",
                campfireRing = true,
                firewoodPurchasable = false,
                hasEvCharging = false
            ),
            cellReceptionBars = 3,
            terrainType = "Alpine Forest",
            description = "A natural volcanic clearing surrounded by huge blue gums where dozens of wild eastern grey kangaroos graze peacefully at sunrise and sunset. Deep Blue Mountains bush immersion.",
            insiderTips = "Sites in Red Gum camping area have the best kangaroo sightings. Bring your own drinking water and fire wood.",
            photoUrl = "https://images.unsplash.com/photo-1523987355523-c7b5b0dd90a7?w=800",
            photoUrls = listOf("https://images.unsplash.com/photo-1523987355523-c7b5b0dd90a7?w=800"),
            isPark4NightVerified = true,
            park4NightNote = "Park4night #33190 • Camp with wild kangaroos! Peaceful bush clearing near Glenbrook gorge."
        ),

        // ==========================================
        // 🇳🇿 NEW ZEALAND: SOUTH ISLAND & FIORDLAND
        // ==========================================
        Campsite(
            id = "world_nz_1",
            name = "White Horse Hill DOC Camp (Aoraki Mount Cook)",
            region = "Mount Cook National Park, Canterbury",
            stateOrCountry = "New Zealand",
            latitude = -43.7188,
            longitude = 170.0931,
            feePerNight = "NZD $15 / night",
            rating = 4.9,
            reviewCount = 520,
            sleep = SleepDetails(
                type = SleepType.CAMPERVAN,
                groundType = GroundType.GRAVEL,
                maxCapacity = 6,
                hammockFriendly = false,
                shadeRating = 1,
                quietHours = "10:00 PM - 6:30 AM",
                elevationFt = 2490
            ),
            water = WaterDetails(
                sourceType = WaterSourceType.POTABLE_TAP,
                distanceToSourceMeters = 15,
                hasHotShowers = false,
                hasColdShowers = false,
                hasDishwashingSink = true,
                flowReliability = "Pure glacial DOC shelter tap"
            ),
            energy = EnergyDetails(
                sourceType = EnergySourceType.SOLAR_CLEARING,
                solarExposureIndex = 9,
                generatorAllowed = false,
                generatorHours = "Strictly zero generators",
                campfireRing = false,
                firewoodPurchasable = false,
                hasEvCharging = false
            ),
            cellReceptionBars = 4,
            terrainType = "Glacial Valley",
            description = "Pitch your campervan directly beneath the towering glacier-clad peak of Aoraki / Mount Cook. Trailhead for Hooker Valley Track with swinging suspension bridges over iceberg lakes.",
            insiderTips = "Heated communal cooking shelter with boiling water taps. Part of the Aoraki Mackenzie International Dark Sky Reserve with unreal Milky Way views.",
            photoUrl = "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=800",
            photoUrls = listOf("https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=800"),
            isPark4NightVerified = true,
            park4NightNote = "Park4night #10294 • The most spectacular camper stop in New Zealand. Mount Cook views from your bed."
        ),
        Campsite(
            id = "world_nz_2",
            name = "Milford Sound Rainforest Lodge & Camp",
            region = "Fiordland National Park, Southland",
            stateOrCountry = "New Zealand",
            latitude = -44.6738,
            longitude = 167.9255,
            feePerNight = "NZD $35 / night",
            rating = 4.8,
            reviewCount = 340,
            sleep = SleepDetails(
                type = SleepType.CAMPERVAN,
                groundType = GroundType.GRAVEL,
                maxCapacity = 6,
                hammockFriendly = true,
                shadeRating = 5,
                quietHours = "10:30 PM - 7:00 AM",
                elevationFt = 100
            ),
            water = WaterDetails(
                sourceType = WaterSourceType.NATURAL_SPRING,
                distanceToSourceMeters = 10,
                hasHotShowers = true,
                hasColdShowers = true,
                hasDishwashingSink = true,
                flowReliability = "Mountain rainforest spring system"
            ),
            energy = EnergyDetails(
                sourceType = EnergySourceType.FULL_HOOKUP_30_50A,
                solarExposureIndex = 6,
                generatorAllowed = false,
                generatorHours = "Silent wilderness park",
                campfireRing = false,
                firewoodPurchasable = false,
                hasEvCharging = true
            ),
            cellReceptionBars = 1,
            terrainType = "Rainforest Riverbank",
            description = "The only accommodation and campervan park inside legendary Milford Sound. Towering native beech trees along the roaring Cleddau River beneath sheer mile-high glacier peaks.",
            insiderTips = "Book months in advance during summer. Underfloor heated luxury bathrooms and drying rooms for wet gear after fiord cruises.",
            photoUrl = "https://images.unsplash.com/photo-1476514525535-07fb3b4ae5f1?w=800",
            photoUrls = listOf("https://images.unsplash.com/photo-1476514525535-07fb3b4ae5f1?w=800"),
            isPark4NightVerified = true,
            park4NightNote = "Park4night #55124 • Inside Milford Sound! Heated campervan sites, incredible rainforest river."
        ),

        // ==========================================
        // 🇯🇵 JAPAN: MOUNT FUJI & JAPANESE ALPS
        // ==========================================
        Campsite(
            id = "world_jp_1",
            name = "Fumotoppara Campground (Mount Fuji)",
            region = "Fujinomiya, Shizuoka",
            stateOrCountry = "Japan",
            latitude = 35.4056,
            longitude = 138.5628,
            feePerNight = "¥3,000 (€18)",
            rating = 4.9,
            reviewCount = 610,
            sleep = SleepDetails(
                type = SleepType.CAMPERVAN,
                groundType = GroundType.SOFT_TURF,
                maxCapacity = 6,
                hammockFriendly = false,
                shadeRating = 1,
                quietHours = "10:00 PM - 6:00 AM",
                elevationFt = 2720
            ),
            water = WaterDetails(
                sourceType = WaterSourceType.NATURAL_SPRING,
                distanceToSourceMeters = 20,
                hasHotShowers = true,
                hasColdShowers = true,
                hasDishwashingSink = true,
                flowReliability = "Fuji underground mineral water tap"
            ),
            energy = EnergyDetails(
                sourceType = EnergySourceType.SOLAR_CLEARING,
                solarExposureIndex = 10,
                generatorAllowed = false,
                generatorHours = "Silent nature reverence",
                campfireRing = true,
                firewoodPurchasable = true,
                hasEvCharging = false
            ),
            cellReceptionBars = 5,
            terrainType = "Alpine Forest",
            description = "Japan's most iconic camping field. Vast open grasslands directly in front of the unobstructed, full silhouette of Mount Fuji. Watch the sunrise 'Diamond Fuji' glow right from your pitch.",
            insiderTips = "Pure Mount Fuji melted spring water flows freely from all camp taps and tastes sweeter than bottled water. Campfire stands mandatory to protect the grass.",
            photoUrl = "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=800",
            photoUrls = listOf("https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=800"),
            isPark4NightVerified = true,
            park4NightNote = "Park4night #88120 • Unrivaled Mount Fuji view. Huge grass fields, pure volcanic drinking water."
        ),
        Campsite(
            id = "world_jp_2",
            name = "Kamikochi Konashidaira Camp",
            region = "Chubu-Sangaku National Park, Nagano",
            stateOrCountry = "Japan",
            latitude = 36.2505,
            longitude = 137.6366,
            feePerNight = "¥1,200 (€8)",
            rating = 4.9,
            reviewCount = 280,
            sleep = SleepDetails(
                type = SleepType.TENT,
                groundType = GroundType.PINE_NEEDLES,
                maxCapacity = 4,
                hammockFriendly = true,
                shadeRating = 5,
                quietHours = "9:00 PM - 6:00 AM",
                elevationFt = 4920
            ),
            water = WaterDetails(
                sourceType = WaterSourceType.NATURAL_SPRING,
                distanceToSourceMeters = 10,
                hasHotShowers = true,
                hasColdShowers = true,
                hasDishwashingSink = true,
                flowReliability = "Azusa river alpine spring"
            ),
            energy = EnergyDetails(
                sourceType = EnergySourceType.STANDARD_15A_OUTLET,
                solarExposureIndex = 8,
                generatorAllowed = false,
                generatorHours = "No vehicles/generators allowed in valley",
                campfireRing = false,
                firewoodPurchasable = false,
                hasEvCharging = false
            ),
            cellReceptionBars = 4,
            terrainType = "Glacial Valley",
            description = "Nestled beside the crystal-clear turquoise Azusa River overlooking the jagged Hotaka mountain range. Car-free valley accessible only by eco-buses, with an onsen bath house right next to camp.",
            insiderTips = "Enjoy hot mineral onsen soak at Konashidaira Bathhouse after trekking. Wild Japanese macaques often seen along the riverbanks.",
            photoUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800",
            photoUrls = listOf("https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800"),
            isPark4NightVerified = true,
            park4NightNote = "Park4night #60144 • Japanese Alps paradise. Riverside tent pitches, natural onsen hot spring bath."
        ),

        // ==========================================
        // 🇿🇦 SOUTH AFRICA: GARDEN ROUTE & KRUGER
        // ==========================================
        Campsite(
            id = "world_za_1",
            name = "Storms River Mouth Rest Camp",
            region = "Tsitsikamma National Park, Eastern Cape",
            stateOrCountry = "South Africa",
            latitude = -34.0225,
            longitude = 23.9022,
            feePerNight = "ZAR 420 (€21)",
            rating = 4.9,
            reviewCount = 440,
            sleep = SleepDetails(
                type = SleepType.CAMPERVAN,
                groundType = GroundType.SOFT_TURF,
                maxCapacity = 6,
                hammockFriendly = false,
                shadeRating = 3,
                quietHours = "10:00 PM - 7:00 AM",
                elevationFt = 50
            ),
            water = WaterDetails(
                sourceType = WaterSourceType.POTABLE_TAP,
                distanceToSourceMeters = 10,
                hasHotShowers = true,
                hasColdShowers = true,
                hasDishwashingSink = true,
                flowReliability = "Municipal fresh mountain tap"
            ),
            energy = EnergyDetails(
                sourceType = EnergySourceType.FULL_HOOKUP_30_50A,
                solarExposureIndex = 9,
                generatorAllowed = false,
                generatorHours = "Silent national park",
                campfireRing = true,
                firewoodPurchasable = true,
                hasEvCharging = false
            ),
            cellReceptionBars = 4,
            terrainType = "Coastal Cliffs",
            description = "Oceanfront camp pitches perched literally meters above crashing Indian Ocean breakers along the famous Garden Route. Walking trail to the suspension bridge over Storms River gorge.",
            insiderTips = "Watch Southern Right Whales and dolphins directly from your campsite deck between June and November. Each site has its own braai (BBQ) pit.",
            photoUrl = "https://images.unsplash.com/photo-1470246973918-29a93221c455?w=800",
            photoUrls = listOf("https://images.unsplash.com/photo-1470246973918-29a93221c455?w=800"),
            isPark4NightVerified = true,
            park4NightNote = "Park4night #79102 • World-famous Tsitsikamma ocean camp. Crashing waves, private braai, whale spotting."
        ),

        // ==========================================
        // 🇨🇱 CHILE & PATAGONIA: TORRES DEL PAINE
        // ==========================================
        Campsite(
            id = "world_cl_1",
            name = "Camping Pehoe & Paine Grande",
            region = "Torres del Paine National Park, Magallanes",
            stateOrCountry = "Chile",
            latitude = -51.1082,
            longitude = -72.9961,
            feePerNight = "$22 / night",
            rating = 4.9,
            reviewCount = 380,
            sleep = SleepDetails(
                type = SleepType.TENT,
                groundType = GroundType.SOFT_TURF,
                maxCapacity = 4,
                hammockFriendly = false,
                shadeRating = 2,
                quietHours = "10:00 PM - 6:00 AM",
                elevationFt = 160
            ),
            water = WaterDetails(
                sourceType = WaterSourceType.NATURAL_SPRING,
                distanceToSourceMeters = 15,
                hasHotShowers = true,
                hasColdShowers = true,
                hasDishwashingSink = true,
                flowReliability = "Glacial runoff filtered spring"
            ),
            energy = EnergyDetails(
                sourceType = EnergySourceType.SOLAR_CLEARING,
                solarExposureIndex = 8,
                generatorAllowed = false,
                generatorHours = "Zero noise zone",
                campfireRing = false,
                firewoodPurchasable = false,
                hasEvCharging = false
            ),
            cellReceptionBars = 2,
            terrainType = "Glacial Valley",
            description = "Perched beside turquoise Lake Pehoé facing the legendary granite spires of Los Cuernos. The crown jewel of Patagonian wilderness along the 'W' Trek.",
            insiderTips = "High wind pegs are essential due to famous Patagonian gusts. Heated shelter available for hot dinners with views of floating icebergs.",
            photoUrl = "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=800",
            photoUrls = listOf("https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=800"),
            isPark4NightVerified = true,
            park4NightNote = "Park4night #91021 • Patagonia Torres del Paine camp. Unbelievable Cuernos peaks view over turquoise lake."
        )
    )
}
