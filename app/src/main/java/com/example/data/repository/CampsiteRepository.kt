package com.example.data.repository

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import com.example.R
import com.example.data.local.AppDatabase
import com.example.data.local.BookmarkEntity
import com.example.data.local.CampsiteEntity
import com.example.data.local.CampsiteReviewEntity
import com.example.data.local.GearEntity
import com.example.data.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CampsiteRepository(
    private val database: AppDatabase,
    private val context: Context
) {

    private val dao = database.campsiteDao()
    private val prefs: SharedPreferences = context.getSharedPreferences("camphaven_sync_prefs", Context.MODE_PRIVATE)

    private val db: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance(context.getString(R.string.firestore_database_id))
    }
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    private val _firestoreCampsites = MutableStateFlow<List<Campsite>>(emptyList())
    private val _lastSyncInfo = MutableStateFlow(getFormattedLastSyncTime())
    val lastSyncInfo = _lastSyncInfo.asStateFlow()

    private var campsitesListenerRegistration: ListenerRegistration? = null

    companion object {
        private const val TAG = "CampsiteRepository"
        private const val PREF_KEY_LAST_SYNC = "last_sync_timestamp_millis"
        const val TWELVE_HOURS_MILLIS = 12 * 60 * 60 * 1000L
    }

    val curatedCampsites: List<Campsite> = listOf(
        // ==========================================
        // 🇪🇺 EUROPE: FRANCE
        // ==========================================
        Campsite(
            id = "eu_fr_1",
            name = "Camping des Glaciers (Mont Blanc)",
            region = "Chamonix Valley, Haute-Savoie",
            stateOrCountry = "France",
            latitude = 45.9237,
            longitude = 6.8694,
            feePerNight = "€22 / night",
            rating = 4.9,
            reviewCount = 312,
            sleep = SleepDetails(
                type = SleepType.CAMPERVAN,
                groundType = GroundType.SOFT_TURF,
                maxCapacity = 6,
                hammockFriendly = true,
                shadeRating = 4,
                quietHours = "10:00 PM - 7:00 AM",
                elevationFt = 3445
            ),
            water = WaterDetails(
                sourceType = WaterSourceType.NATURAL_SPRING,
                distanceToSourceMeters = 15,
                hasHotShowers = true,
                hasColdShowers = true,
                hasDishwashingSink = true,
                flowReliability = "Pure glacial spring water tap"
            ),
            energy = EnergyDetails(
                sourceType = EnergySourceType.FULL_HOOKUP_30_50A,
                solarExposureIndex = 8,
                generatorAllowed = false,
                generatorHours = "Silent eco zone (No generators)",
                campfireRing = false,
                firewoodPurchasable = false,
                hasEvCharging = true
            ),
            cellReceptionBars = 4,
            terrainType = "Alpine Forest",
            description = "Stunning campervan and tent pitches directly facing the Mont Blanc massif. Fresh potable alpine spring taps, heated sanitary blocks, and 16A electric hookups.",
            insiderTips = "Sites along the glacier stream have the best sunrise views over the Aiguilles. Park4night certified spot.",
            photoUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800",
            photoUrls = listOf(
                "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800",
                "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=800"
            ),
            isPark4NightVerified = true,
            park4NightNote = "Park4night #38291 • Verified camper stop with clean grey/black water dumping and fresh spring water."
        ),
        Campsite(
            id = "eu_fr_2",
            name = "Aire Camping-Car Dune du Pilat",
            region = "Bassin d'Arcachon, Nouvelle-Aquitaine",
            stateOrCountry = "France",
            latitude = 44.5896,
            longitude = -1.2136,
            feePerNight = "€15 / 24h",
            rating = 4.7,
            reviewCount = 284,
            sleep = SleepDetails(
                type = SleepType.CAMPERVAN,
                groundType = GroundType.SAND,
                maxCapacity = 4,
                hammockFriendly = true,
                shadeRating = 5,
                quietHours = "11:00 PM - 7:00 AM",
                elevationFt = 110
            ),
            water = WaterDetails(
                sourceType = WaterSourceType.POTABLE_TAP,
                distanceToSourceMeters = 20,
                hasHotShowers = false,
                hasColdShowers = true,
                hasDishwashingSink = true,
                flowReliability = "Municipal potable water station"
            ),
            energy = EnergyDetails(
                sourceType = EnergySourceType.STANDARD_15A_OUTLET,
                solarExposureIndex = 6,
                generatorAllowed = false,
                generatorHours = "No generators",
                campfireRing = false,
                firewoodPurchasable = false,
                hasEvCharging = false
            ),
            cellReceptionBars = 4,
            terrainType = "Coastal Cliffs",
            description = "Iconic pine forest camper aire beneath Europe's highest sand dune. Shaded under maritime pines with direct footpath to the Atlantic beach and dune summit.",
            insiderTips = "Arrive before 3:00 PM during summer. Automatic barrier entry accepts cards. Flot Bleu camper service terminal on site.",
            photoUrl = "https://images.unsplash.com/photo-1523987355523-c7b5b0dd90a7?w=800",
            photoUrls = listOf(
                "https://images.unsplash.com/photo-1523987355523-c7b5b0dd90a7?w=800"
            ),
            isPark4NightVerified = true,
            park4NightNote = "Park4night #11048 • Famous aire de camping-car. Shaded pitches, quiet night under pines."
        ),
        Campsite(
            id = "eu_fr_3",
            name = "Gorges du Verdon Cliff Haven",
            region = "La Palud-sur-Verdon, Provence",
            stateOrCountry = "France",
            latitude = 43.7801,
            longitude = 6.3414,
            feePerNight = "Free (Wild Camper Pitch)",
            rating = 4.8,
            reviewCount = 145,
            sleep = SleepDetails(
                type = SleepType.DISPERSED,
                groundType = GroundType.GRAVEL,
                maxCapacity = 4,
                hammockFriendly = true,
                shadeRating = 3,
                quietHours = "Natural Dark Sky Silence",
                elevationFt = 2950
            ),
            water = WaterDetails(
                sourceType = WaterSourceType.POTABLE_TAP,
                distanceToSourceMeters = 80,
                hasHotShowers = false,
                hasColdShowers = false,
                hasDishwashingSink = false,
                flowReliability = "Village spring fountain 80m away"
            ),
            energy = EnergyDetails(
                sourceType = EnergySourceType.SOLAR_CLEARING,
                solarExposureIndex = 9,
                generatorAllowed = false,
                generatorHours = "Zero noise zone",
                campfireRing = false,
                firewoodPurchasable = false,
                hasEvCharging = false
            ),
            cellReceptionBars = 2,
            terrainType = "Desert Canyon",
            description = "Perched right above Europe's deepest canyon. Unmatched turquoise river views 700m below with starry dark skies and lavender scent on the evening breeze.",
            insiderTips = "Fill water containers at the La Palud village fountain before driving up Route des Crêtes.",
            photoUrl = "https://images.unsplash.com/photo-1470246973918-29a93221c455?w=800",
            photoUrls = listOf("https://images.unsplash.com/photo-1470246973918-29a93221c455?w=800"),
            isPark4NightVerified = true,
            park4NightNote = "Park4night #9214 • Spectacular canyon sunset spot. Level gravel ground, strictly pack out trash."
        ),

        // ==========================================
        // 🇮🇹 EUROPE: ITALY
        // ==========================================
        Campsite(
            id = "eu_it_1",
            name = "Camping Vidor Family Resort (Dolomites)",
            region = "Val di Fassa, Trentino-Alto Adige",
            stateOrCountry = "Italy",
            latitude = 46.4302,
            longitude = 11.6983,
            feePerNight = "€28 / night",
            rating = 4.9,
            reviewCount = 420,
            sleep = SleepDetails(
                type = SleepType.CAMPERVAN,
                groundType = GroundType.PINE_NEEDLES,
                maxCapacity = 8,
                hammockFriendly = true,
                shadeRating = 4,
                quietHours = "10:30 PM - 7:30 AM",
                elevationFt = 4750
            ),
            water = WaterDetails(
                sourceType = WaterSourceType.NATURAL_SPRING,
                distanceToSourceMeters = 10,
                hasHotShowers = true,
                hasColdShowers = true,
                hasDishwashingSink = true,
                flowReliability = "Continuous Dolomite spring water"
            ),
            energy = EnergyDetails(
                sourceType = EnergySourceType.FULL_HOOKUP_30_50A,
                solarExposureIndex = 8,
                generatorAllowed = false,
                generatorHours = "No generators",
                campfireRing = true,
                firewoodPurchasable = true,
                hasEvCharging = true
            ),
            cellReceptionBars = 5,
            terrainType = "Alpine Forest",
            description = "The crown jewel of the Italian Dolomites. Nestled under jagged limestone peaks with full campervan hookups, high-speed WiFi, hot mountain showers, and direct hiking trailheads.",
            insiderTips = "Sites in Sector C are right against the larch forest with panoramic vistas of Catinaccio peak alpenglow.",
            photoUrl = "https://images.unsplash.com/photo-1533873984035-25970ab07461?w=800",
            photoUrls = listOf(
                "https://images.unsplash.com/photo-1533873984035-25970ab07461?w=800",
                "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=800"
            ),
            isPark4NightVerified = true,
            park4NightNote = "Park4night #44120 • 5-star mountain camper resort. Heated facilities, perfect for summer hikes and winter ski camper."
        ),
        Campsite(
            id = "eu_it_2",
            name = "Agricamping Garda Lake Relax",
            region = "Lake Garda, Veneto",
            stateOrCountry = "Italy",
            latitude = 45.4851,
            longitude = 10.7324,
            feePerNight = "€24 / night",
            rating = 4.8,
            reviewCount = 198,
            sleep = SleepDetails(
                type = SleepType.CAMPERVAN,
                groundType = GroundType.SOFT_TURF,
                maxCapacity = 6,
                hammockFriendly = true,
                shadeRating = 4,
                quietHours = "10:00 PM - 8:00 AM",
                elevationFt = 260
            ),
            water = WaterDetails(
                sourceType = WaterSourceType.POTABLE_TAP,
                distanceToSourceMeters = 8,
                hasHotShowers = true,
                hasColdShowers = true,
                hasDishwashingSink = true,
                flowReliability = "Municipal filtered tap"
            ),
            energy = EnergyDetails(
                sourceType = EnergySourceType.STANDARD_15A_OUTLET,
                solarExposureIndex = 9,
                generatorAllowed = false,
                generatorHours = "No generators",
                campfireRing = false,
                firewoodPurchasable = false,
                hasEvCharging = true
            ),
            cellReceptionBars = 5,
            terrainType = "Alpine Lake",
            description = "Tranquil pitches nestled inside a centuries-old olive grove 800m from Lake Garda beaches. Fresh local organic olive oil and wine tastings available from host farm.",
            insiderTips = "Walk down to the lake promenade at sunset. Excellent electric hookup and fresh water point beside every pitch.",
            photoUrl = "https://images.unsplash.com/photo-1510312305653-8ed496efae75?w=800",
            photoUrls = listOf("https://images.unsplash.com/photo-1510312305653-8ed496efae75?w=800"),
            isPark4NightVerified = true,
            park4NightNote = "Park4night #19052 • Friendly farm stay. Huge level pitches under olive trees, quiet night."
        ),
        Campsite(
            id = "eu_it_3",
            name = "Cala Luna Wild Camper Pitch",
            region = "Golfo di Orosei, Sardinia",
            stateOrCountry = "Italy",
            latitude = 40.2250,
            longitude = 9.6278,
            feePerNight = "Free (Nature Pitch)",
            rating = 4.9,
            reviewCount = 115,
            sleep = SleepDetails(
                type = SleepType.TENT,
                groundType = GroundType.SAND,
                maxCapacity = 4,
                hammockFriendly = true,
                shadeRating = 3,
                quietHours = "Natural Coastal Waves",
                elevationFt = 20
            ),
            water = WaterDetails(
                sourceType = WaterSourceType.NATURAL_SPRING,
                distanceToSourceMeters = 45,
                hasHotShowers = false,
                hasColdShowers = false,
                hasDishwashingSink = false,
                flowReliability = "Fresh stream emptying behind beach"
            ),
            energy = EnergyDetails(
                sourceType = EnergySourceType.SOLAR_CLEARING,
                solarExposureIndex = 10,
                generatorAllowed = false,
                generatorHours = "Pure nature sanctuary",
                campfireRing = false,
                firewoodPurchasable = false,
                hasEvCharging = false
            ),
            cellReceptionBars = 1,
            terrainType = "Coastal Cliffs",
            description = "Pristine white sand cove nestled between towering limestone sea caves and oleander groves. Turquoise water ideal for wild swimming and night stargazing.",
            insiderTips = "Carry a portable solar shower and reef-safe soap. Access via hiking trail or campervan park at Cala Gonone.",
            photoUrl = "https://images.unsplash.com/photo-1544644181-1484b3fdfc62?w=800",
            photoUrls = listOf("https://images.unsplash.com/photo-1544644181-1484b3fdfc62?w=800"),
            isPark4NightVerified = true,
            park4NightNote = "Park4night #6014 • Wild Mediterranean dream spot. Zero light pollution."
        ),

        // ==========================================
        // 🇩🇪 EUROPE: GERMANY
        // ==========================================
        Campsite(
            id = "eu_de_1",
            name = "Camping Münstertal (Schwarzwald)",
            region = "Black Forest, Baden-Württemberg",
            stateOrCountry = "Germany",
            latitude = 47.8542,
            longitude = 7.7125,
            feePerNight = "€26 / night",
            rating = 4.9,
            reviewCount = 370,
            sleep = SleepDetails(
                type = SleepType.CAMPERVAN,
                groundType = GroundType.SOFT_TURF,
                maxCapacity = 6,
                hammockFriendly = true,
                shadeRating = 4,
                quietHours = "10:00 PM - 7:00 AM",
                elevationFt = 1250
            ),
            water = WaterDetails(
                sourceType = WaterSourceType.POTABLE_TAP,
                distanceToSourceMeters = 5,
                hasHotShowers = true,
                hasColdShowers = true,
                hasDishwashingSink = true,
                flowReliability = "Black forest spring tap network"
            ),
            energy = EnergyDetails(
                sourceType = EnergySourceType.FULL_HOOKUP_30_50A,
                solarExposureIndex = 7,
                generatorAllowed = false,
                generatorHours = "Strict quiet resort",
                campfireRing = true,
                firewoodPurchasable = true,
                hasEvCharging = true
            ),
            cellReceptionBars = 5,
            terrainType = "Alpine Forest",
            description = "Multi-award winning German eco-campsite in the lush Black Forest valley. Features heated natural stone wellness washrooms, fresh bakery delivery, and direct forest trails.",
            insiderTips = "KONUS guest card included gives free regional train and bus travel throughout the entire Black Forest.",
            photoUrl = "https://images.unsplash.com/photo-1448375240586-882707db888b?w=800",
            photoUrls = listOf("https://images.unsplash.com/photo-1448375240586-882707db888b?w=800"),
            isPark4NightVerified = true,
            park4NightNote = "Park4night #29831 • Top German camper site. Immaculate facilities, level turf, heated sanitary."
        ),
        Campsite(
            id = "eu_de_2",
            name = "Alpen-Campingplatz Königssee (Bavaria)",
            region = "Berchtesgaden Alps, Bavaria",
            stateOrCountry = "Germany",
            latitude = 47.5925,
            longitude = 12.9878,
            feePerNight = "€25 / night",
            rating = 4.8,
            reviewCount = 265,
            sleep = SleepDetails(
                type = SleepType.CAMPERVAN,
                groundType = GroundType.GRAVEL,
                maxCapacity = 6,
                hammockFriendly = true,
                shadeRating = 4,
                quietHours = "10:00 PM - 7:00 AM",
                elevationFt = 2030
            ),
            water = WaterDetails(
                sourceType = WaterSourceType.POTABLE_TAP,
                distanceToSourceMeters = 12,
                hasHotShowers = true,
                hasColdShowers = true,
                hasDishwashingSink = true,
                flowReliability = "Mountain spring potable system"
            ),
            energy = EnergyDetails(
                sourceType = EnergySourceType.STANDARD_15A_OUTLET,
                solarExposureIndex = 8,
                generatorAllowed = false,
                generatorHours = "No generators",
                campfireRing = false,
                firewoodPurchasable = true,
                hasEvCharging = true
            ),
            cellReceptionBars = 4,
            terrainType = "Alpine Lake",
            description = "Located only 500m from Germany's cleanest emerald-green fjord lake, surrounded by sheer 2000m cliffs of Mount Watzmann. Crystal clean air and alpine serenity.",
            insiderTips = "Rent an electric boat early morning on Lake Königssee before tourist ferries begin.",
            photoUrl = "https://images.unsplash.com/photo-1501785888041-af3ef285b470?w=800",
            photoUrls = listOf("https://images.unsplash.com/photo-1501785888041-af3ef285b470?w=800"),
            isPark4NightVerified = true,
            park4NightNote = "Park4night #31204 • Prime Bavarian location for campervans. Super quiet after 10pm."
        ),
        Campsite(
            id = "eu_de_3",
            name = "Wohnmobilstellplatz Moselblick",
            region = "Mosel Valley, Rhineland-Palatinate",
            stateOrCountry = "Germany",
            latitude = 50.1542,
            longitude = 7.1725,
            feePerNight = "€14 / night",
            rating = 4.7,
            reviewCount = 210,
            sleep = SleepDetails(
                type = SleepType.CAMPERVAN,
                groundType = GroundType.GRAVEL,
                maxCapacity = 4,
                hammockFriendly = false,
                shadeRating = 3,
                quietHours = "10:00 PM - 7:00 AM",
                elevationFt = 320
            ),
            water = WaterDetails(
                sourceType = WaterSourceType.POTABLE_TAP,
                distanceToSourceMeters = 15,
                hasHotShowers = true,
                hasColdShowers = true,
                hasDishwashingSink = true,
                flowReliability = "Municipal fresh water column"
            ),
            energy = EnergyDetails(
                sourceType = EnergySourceType.STANDARD_15A_OUTLET,
                solarExposureIndex = 8,
                generatorAllowed = false,
                generatorHours = "No generators",
                campfireRing = false,
                firewoodPurchasable = false,
                hasEvCharging = false
            ),
            cellReceptionBars = 5,
            terrainType = "Rainforest Riverbank",
            description = "Terraced camper van pitch right on the banks of the winding Mosel River, directly below world-famous steep slate Riesling vineyards. Watch river barges cruise past.",
            insiderTips = "Coin machine for 16A electricity: €1 per 2kWh. Wine taverns (Straußwirtschaften) within 5 minute walk.",
            photoUrl = "https://images.unsplash.com/photo-1499793983690-e29da59ef1c2?w=800",
            photoUrls = listOf("https://images.unsplash.com/photo-1499793983690-e29da59ef1c2?w=800"),
            isPark4NightVerified = true,
            park4NightNote = "Park4night #15882 • Authentic German Stellplatz. Beautiful Mosel riverfront views, electric columns."
        ),

        // ==========================================
        // 🇨🇭 EUROPE: SWITZERLAND
        // ==========================================
        Campsite(
            id = "eu_ch_1",
            name = "Camping Jungfrau Lauterbrunnen",
            region = "Bernese Oberland",
            stateOrCountry = "Switzerland",
            latitude = 46.5937,
            longitude = 7.9078,
            feePerNight = "€32 / night",
            rating = 4.9,
            reviewCount = 510,
            sleep = SleepDetails(
                type = SleepType.CAMPERVAN,
                groundType = GroundType.SOFT_TURF,
                maxCapacity = 6,
                hammockFriendly = true,
                shadeRating = 4,
                quietHours = "10:00 PM - 7:00 AM",
                elevationFt = 2625
            ),
            water = WaterDetails(
                sourceType = WaterSourceType.NATURAL_SPRING,
                distanceToSourceMeters = 8,
                hasHotShowers = true,
                hasColdShowers = true,
                hasDishwashingSink = true,
                flowReliability = "Swiss mountain glacier spring"
            ),
            energy = EnergyDetails(
                sourceType = EnergySourceType.FULL_HOOKUP_30_50A,
                solarExposureIndex = 8,
                generatorAllowed = false,
                generatorHours = "Silent Swiss valley",
                campfireRing = true,
                firewoodPurchasable = true,
                hasEvCharging = true
            ),
            cellReceptionBars = 5,
            terrainType = "Glacial Valley",
            description = "The valley of 72 waterfalls. Pitch your van directly under the 300m illuminated Staubbach Falls with sheer limestone cliffs and the snow-capped Jungfrau massif overhead.",
            insiderTips = "Sites along the Lütschine river offer constant soothing glacial water white noise. Top-tier sanitary and camp kitchen facilities.",
            photoUrl = "https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=800",
            photoUrls = listOf("https://images.unsplash.com/photo-1464822759023-fed622ff2c3b?w=800"),
            isPark4NightVerified = true,
            park4NightNote = "Park4night #50210 • Iconic Swiss camp. Unbelievable waterfall views, heated facilities."
        ),
        Campsite(
            id = "eu_ch_2",
            name = "Arolla Alpine High Sanctuary",
            region = "Val d'Hérens, Valais",
            stateOrCountry = "Switzerland",
            latitude = 46.0275,
            longitude = 7.4819,
            feePerNight = "€20 / night",
            rating = 4.8,
            reviewCount = 188,
            sleep = SleepDetails(
                type = SleepType.TENT,
                groundType = GroundType.PINE_NEEDLES,
                maxCapacity = 4,
                hammockFriendly = true,
                shadeRating = 5,
                quietHours = "9:30 PM - 6:30 AM",
                elevationFt = 6400
            ),
            water = WaterDetails(
                sourceType = WaterSourceType.NATURAL_SPRING,
                distanceToSourceMeters = 20,
                hasHotShowers = true,
                hasColdShowers = true,
                hasDishwashingSink = true,
                flowReliability = "High alpine spring (Zero chlorine)"
            ),
            energy = EnergyDetails(
                sourceType = EnergySourceType.SOLAR_CLEARING,
                solarExposureIndex = 9,
                generatorAllowed = false,
                generatorHours = "Off-grid sanctuary",
                campfireRing = true,
                firewoodPurchasable = true,
                hasEvCharging = false
            ),
            cellReceptionBars = 2,
            terrainType = "Alpine Forest",
            description = "Europe's highest official campsite at 1,950m elevation. Ancient Swiss stone pine forest overlooking glaciers and 4000m peaks. The crispest air you will ever breathe.",
            insiderTips = "Pack a warm 0°C rated sleeping bag even in July. Natural pine needle forest ground offers natural cushioning.",
            photoUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800",
            photoUrls = listOf("https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800"),
            isPark4NightVerified = true,
            park4NightNote = "Park4night #18712 • Highest campsite in Europe (1950m). Pristine nature, glacier views."
        ),

        // ==========================================
        // 🇪🇸 EUROPE: SPAIN
        // ==========================================
        Campsite(
            id = "eu_es_1",
            name = "Camping Pineta Monte Perdido (Pyrenees)",
            region = "Ordesa National Park, Huesca",
            stateOrCountry = "Spain",
            latitude = 42.6642,
            longitude = 0.1236,
            feePerNight = "€19 / night",
            rating = 4.8,
            reviewCount = 230,
            sleep = SleepDetails(
                type = SleepType.CAMPERVAN,
                groundType = GroundType.SOFT_TURF,
                maxCapacity = 6,
                hammockFriendly = true,
                shadeRating = 4,
                quietHours = "11:00 PM - 7:30 AM",
                elevationFt = 3940
            ),
            water = WaterDetails(
                sourceType = WaterSourceType.NATURAL_SPRING,
                distanceToSourceMeters = 15,
                hasHotShowers = true,
                hasColdShowers = true,
                hasDishwashingSink = true,
                flowReliability = "Pyrenean mountain river & tap"
            ),
            energy = EnergyDetails(
                sourceType = EnergySourceType.STANDARD_15A_OUTLET,
                solarExposureIndex = 8,
                generatorAllowed = false,
                generatorHours = "No generators",
                campfireRing = false,
                firewoodPurchasable = true,
                hasEvCharging = true
            ),
            cellReceptionBars = 3,
            terrainType = "Glacial Valley",
            description = "Set in the majestic Pineta glacial cirque surrounded by 1000m vertical rock amphitheaters, beech woods, and roaring mountain waterfalls in the Aragonese Pyrenees.",
            insiderTips = "Trailhead for the Balcón de Pineta begins directly behind pitch 42. Excellent campervan water emptying station.",
            photoUrl = "https://images.unsplash.com/photo-1486870591958-9b9d0d1dda99?w=800",
            photoUrls = listOf("https://images.unsplash.com/photo-1486870591958-9b9d0d1dda99?w=800"),
            isPark4NightVerified = true,
            park4NightNote = "Park4night #27914 • Epic Pyrenees valley. Level grassy pitches, mountain fresh water."
        ),
        Campsite(
            id = "eu_es_2",
            name = "Cabo de Gata Van Haven",
            region = "Almería, Andalusia",
            stateOrCountry = "Spain",
            latitude = 36.7825,
            longitude = -2.2458,
            feePerNight = "€12 / night",
            rating = 4.7,
            reviewCount = 175,
            sleep = SleepDetails(
                type = SleepType.CAMPERVAN,
                groundType = GroundType.SAND,
                maxCapacity = 4,
                hammockFriendly = false,
                shadeRating = 2,
                quietHours = "Natural Coastal Calm",
                elevationFt = 45
            ),
            water = WaterDetails(
                sourceType = WaterSourceType.POTABLE_TAP,
                distanceToSourceMeters = 25,
                hasHotShowers = true,
                hasColdShowers = true,
                hasDishwashingSink = true,
                flowReliability = "Desalinated fresh tap"
            ),
            energy = EnergyDetails(
                sourceType = EnergySourceType.SOLAR_CLEARING,
                solarExposureIndex = 10,
                generatorAllowed = false,
                generatorHours = "Solar only",
                campfireRing = false,
                firewoodPurchasable = false,
                hasEvCharging = false
            ),
            cellReceptionBars = 4,
            terrainType = "Desert Canyon",
            description = "Europe's only true desert maritime park. Black volcanic headlands, turquoise secluded coves, and 320 days of blazing sunshine per year. Dream spot for solar campervans.",
            insiderTips = "Wind can pick up in the late afternoon; angle your van pop-top into the sea breeze. 10/10 solar index.",
            photoUrl = "https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800",
            photoUrls = listOf("https://images.unsplash.com/photo-1507525428034-b723cf961d3e?w=800"),
            isPark4NightVerified = true,
            park4NightNote = "Park4night #8841 • Wild coastal campervan stop. Unlimited solar power, crystal clear sea."
        ),

        // ==========================================
        // 🇦🇹 EUROPE: AUSTRIA
        // ==========================================
        Campsite(
            id = "eu_at_1",
            name = "Camping Grubhof (Salzburger Land)",
            region = "St. Martin bei Lofer, Salzburg",
            stateOrCountry = "Austria",
            latitude = 47.5750,
            longitude = 12.7083,
            feePerNight = "€27 / night",
            rating = 4.9,
            reviewCount = 385,
            sleep = SleepDetails(
                type = SleepType.CAMPERVAN,
                groundType = GroundType.SOFT_TURF,
                maxCapacity = 8,
                hammockFriendly = true,
                shadeRating = 4,
                quietHours = "10:00 PM - 7:00 AM",
                elevationFt = 2080
            ),
            water = WaterDetails(
                sourceType = WaterSourceType.POTABLE_TAP,
                distanceToSourceMeters = 5,
                hasHotShowers = true,
                hasColdShowers = true,
                hasDishwashingSink = true,
                flowReliability = "Alpine mineral spring tap"
            ),
            energy = EnergyDetails(
                sourceType = EnergySourceType.FULL_HOOKUP_30_50A,
                solarExposureIndex = 8,
                generatorAllowed = false,
                generatorHours = "Silent nature resort",
                campfireRing = true,
                firewoodPurchasable = true,
                hasEvCharging = true
            ),
            cellReceptionBars = 5,
            terrainType = "Alpine Forest",
            description = "Sprawling riverside meadow pitches along the roaring Saalach river with mountain panorama of the Loferer Steinberge. Luxury timber washhouses and organic farm store.",
            insiderTips = "Riverside pitches (XXL format) are up to 180m² with individual fresh water and grey water drain connections.",
            photoUrl = "https://images.unsplash.com/photo-1476514525535-07fb3b4ae5f1?w=800",
            photoUrls = listOf("https://images.unsplash.com/photo-1476514525535-07fb3b4ae5f1?w=800"),
            isPark4NightVerified = true,
            park4NightNote = "Park4night #36102 • Outstanding Austrian campsite. Huge riverside grass pitches, luxury showers."
        ),

        // ==========================================
        // 🇳🇴 EUROPE: NORWAY
        // ==========================================
        Campsite(
            id = "eu_no_1",
            name = "Geiranger Fjord Wild Camp",
            region = "Møre og Romsdal",
            stateOrCountry = "Norway",
            latitude = 62.1015,
            longitude = 7.2065,
            feePerNight = "Free (Allemannsretten)",
            rating = 4.9,
            reviewCount = 290,
            sleep = SleepDetails(
                type = SleepType.DISPERSED,
                groundType = GroundType.SOFT_TURF,
                maxCapacity = 4,
                hammockFriendly = true,
                shadeRating = 3,
                quietHours = "Midnight Sun Serenity",
                elevationFt = 1050
            ),
            water = WaterDetails(
                sourceType = WaterSourceType.RIVER_FILTER_REQ,
                distanceToSourceMeters = 15,
                hasHotShowers = false,
                hasColdShowers = false,
                hasDishwashingSink = false,
                flowReliability = "Glacial waterfall runoff"
            ),
            energy = EnergyDetails(
                sourceType = EnergySourceType.SOLAR_CLEARING,
                solarExposureIndex = 9,
                generatorAllowed = false,
                generatorHours = "Quiet wild wilderness",
                campfireRing = false,
                firewoodPurchasable = false,
                hasEvCharging = false
            ),
            cellReceptionBars = 3,
            terrainType = "Glacial Valley",
            description = "Pitch legal Norwegian wild camp above the UNESCO Geirangerfjord. Seven Sisters waterfall cascades directly opposite your camp setup with midnight sun illumination.",
            insiderTips = "Under Norway's Allemannsretten (Right to Roam), camp at least 150m away from nearest inhabited cabin and leave no trace.",
            photoUrl = "https://images.unsplash.com/photo-1519681393784-d120267933ba?w=800",
            photoUrls = listOf("https://images.unsplash.com/photo-1519681393784-d120267933ba?w=800"),
            isPark4NightVerified = true,
            park4NightNote = "Park4night #62019 • Fjord wilderness spot. Breathtaking views, pristine mountain stream."
        ),

        // ==========================================
        // 🇵🇹 EUROPE: PORTUGAL
        // ==========================================
        Campsite(
            id = "eu_pt_1",
            name = "Salema Eco Camp (Algarve)",
            region = "Costa Vicentina, Algarve",
            stateOrCountry = "Portugal",
            latitude = 37.0658,
            longitude = -8.8242,
            feePerNight = "€18 / night",
            rating = 4.8,
            reviewCount = 205,
            sleep = SleepDetails(
                type = SleepType.CAMPERVAN,
                groundType = GroundType.SOFT_TURF,
                maxCapacity = 6,
                hammockFriendly = true,
                shadeRating = 5,
                quietHours = "10:30 PM - 8:00 AM",
                elevationFt = 220
            ),
            water = WaterDetails(
                sourceType = WaterSourceType.POTABLE_TAP,
                distanceToSourceMeters = 10,
                hasHotShowers = true,
                hasColdShowers = true,
                hasDishwashingSink = true,
                flowReliability = "Fresh spring tap system"
            ),
            energy = EnergyDetails(
                sourceType = EnergySourceType.STANDARD_15A_OUTLET,
                solarExposureIndex = 9,
                generatorAllowed = false,
                generatorHours = "Eco sanctuary",
                campfireRing = false,
                firewoodPurchasable = false,
                hasEvCharging = true
            ),
            cellReceptionBars = 4,
            terrainType = "Coastal Cliffs",
            description = "Hidden eco-campsite inside Parque Natural da Costa Vicentina under cork oak trees. Walking distance to wild Atlantic surf beaches and fishing village restaurants.",
            insiderTips = "Lots of shade from mature cork oaks and eucalyptus trees. Excellent campervan service bay and community organic garden.",
            photoUrl = "https://images.unsplash.com/photo-1518495973542-4542c06a5843?w=800",
            photoUrls = listOf("https://images.unsplash.com/photo-1518495973542-4542c06a5843?w=800"),
            isPark4NightVerified = true,
            park4NightNote = "Park4night #14220 • Green eco camp in Portugal Algarve. Shaded van spots, hot showers."
        ),

        // ==========================================
        // 🇺🇸 NORTH AMERICA (CLASSIC HUBS)
        // ==========================================
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
            insiderTips = "Sites 7 and 9 have perfect twin pines for hammocks directly facing the sunrise. Spring water is naturally filtered through granite.",
            photoUrl = "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800"
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
            insiderTips = "Each hookup pedestal includes 50A/30A/20A dual GFI breakers. High pressure water connection with pressure regulator advised.",
            photoUrl = "https://images.unsplash.com/photo-1510312305653-8ed496efae75?w=800"
        )
    )

    init {
        // Start listening to shared cloud campsites
        startFirestoreCampsiteListener()
        // Check 12-hour sync
        checkAndPerform12HourSync()
    }

    fun startFirestoreCampsiteListener() {
        if (campsitesListenerRegistration != null) return

        try {
            campsitesListenerRegistration = db.collection("campsites")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(TAG, "Listen error on shared campsites: ${error.message}")
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val cloudSites = snapshot.documents.mapNotNull { doc ->
                            try {
                                val data = doc.data ?: return@mapNotNull null
                                parseCampsiteFromFirestore(doc.id, data)
                            } catch (e: Exception) {
                                Log.w(TAG, "Error parsing cloud campsite ${doc.id}: ${e.message}")
                                null
                            }
                        }
                        _firestoreCampsites.value = cloudSites
                        markSyncCompleted()
                        Log.d(TAG, "Received ${cloudSites.size} shared campsites from Firestore.")
                    }
                }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to start Firestore campsite listener: ${e.message}")
        }
    }

    /**
     * Checks if 12 hours have elapsed since the last sync. If so, triggers full cloud refresh.
     * Fulfills: "update the app data each 12 hours"
     */
    fun checkAndPerform12HourSync() {
        val lastSync = prefs.getLong(PREF_KEY_LAST_SYNC, 0L)
        val now = System.currentTimeMillis()

        if (now - lastSync >= TWELVE_HOURS_MILLIS || lastSync == 0L) {
            Log.d(TAG, "12 hours elapsed since last sync. Performing automated 12-hour cloud refresh...")
            CoroutineScope(Dispatchers.IO).launch {
                refreshCloudData()
            }
        } else {
            val remainingHours = ((TWELVE_HOURS_MILLIS - (now - lastSync)) / (1000 * 60 * 60)).toInt()
            Log.d(TAG, "Sync is fresh. Next automated 12-hour sync in approx $remainingHours hours.")
        }
    }

    suspend fun refreshCloudData(): Boolean {
        return try {
            val snapshot = db.collection("campsites").get().await()
            val cloudSites = snapshot.documents.mapNotNull { doc ->
                val data = doc.data ?: return@mapNotNull null
                parseCampsiteFromFirestore(doc.id, data)
            }
            _firestoreCampsites.value = cloudSites

            // Also cache in local Room database
            val roomEntities = cloudSites.map { it.toRoomCampsite() }
            dao.insertAllCampsites(roomEntities)

            markSyncCompleted()
            true
        } catch (e: Exception) {
            Log.w(TAG, "12-hour sync failed: ${e.message}")
            false
        }
    }

    private fun markSyncCompleted() {
        val now = System.currentTimeMillis()
        prefs.edit().putLong(PREF_KEY_LAST_SYNC, now).apply()
        _lastSyncInfo.value = getFormattedLastSyncTime(now)
    }

    private fun getFormattedLastSyncTime(timestamp: Long = prefs.getLong(PREF_KEY_LAST_SYNC, System.currentTimeMillis())): String {
        return if (timestamp == 0L) {
            "Never synced"
        } else {
            val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
            "Synced ${sdf.format(Date(timestamp))} (Auto-syncs every 12h)"
        }
    }

    fun getCampsitesFlow(): Flow<List<Campsite>> {
        val localCustomFlow = dao.getAllCustomCampsites().map { entities ->
            entities.map { it.toDomainModel() }
        }
        val bookmarkFlow = dao.getAllBookmarks().map { bookmarks ->
            bookmarks.map { it.campsiteId }.toSet()
        }

        return combine(localCustomFlow, _firestoreCampsites, bookmarkFlow) { localList, cloudList, bookmarkedIds ->
            val combinedMap = linkedMapOf<String, Campsite>()

            // 1. Curated European & international base campsites
            curatedCampsites.forEach { combinedMap[it.id] = it }
            // 2. Cloud shared (shared with everyone on the app)
            cloudList.forEach { combinedMap[it.id] = it }
            // 3. Local custom spots
            localList.forEach { combinedMap[it.id] = it }

            combinedMap.values.map { site ->
                site.copy(isBookmarked = bookmarkedIds.contains(site.id))
            }
        }
    }

    suspend fun toggleBookmark(campsiteId: String, currentStatus: Boolean) {
        val user = auth.currentUser
        if (currentStatus) {
            dao.removeBookmark(campsiteId)
            if (user != null) {
                try {
                    db.collection("users").document(user.uid)
                        .collection("bookmarks").document(campsiteId).delete().await()
                } catch (e: Exception) {
                    Log.w(TAG, "Error removing cloud bookmark: ${e.message}")
                }
            }
        } else {
            dao.insertBookmark(BookmarkEntity(campsiteId = campsiteId))
            if (user != null) {
                try {
                    db.collection("users").document(user.uid)
                        .collection("bookmarks").document(campsiteId)
                        .set(mapOf("campsiteId" to campsiteId, "savedAt" to FieldValue.serverTimestamp())).await()
                } catch (e: Exception) {
                    Log.w(TAG, "Error adding cloud bookmark: ${e.message}")
                }
            }
        }
    }

    /**
     * Adds a campsite and shares it with everyone on the app via Firestore collection 'campsites'.
     * CRITICAL PRIVACY: Never publishes user login or vehicle specifications.
     */
    suspend fun addCustomCampsite(campsite: Campsite) {
        val user = auth.currentUser
        val userId = user?.uid ?: "shared_camper"
        val cleanSite = campsite.copy(
            name = cleanCampsiteName(campsite.name),
            isUserCreated = true
        )

        // 1. Save to local Room for instant feedback & offline capability
        dao.insertCustomCampsite(cleanSite.toEntity())
        dao.insertCampsite(cleanSite.toRoomCampsite())

        // 2. Publish to Cloud Firestore so everyone on the app gets it
        val firestoreData = cleanSite.toFirestoreMap(userId)
        try {
            db.collection("campsites").document(cleanSite.id)
                .set(firestoreData, SetOptions.merge())
                .await()
            Log.d(TAG, "Campsite ${cleanSite.name} published to Firestore and shared with all campers!")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to upload campsite to shared Firestore: ${e.message}", e)
        }
    }

    suspend fun deleteCustomCampsite(id: String) {
        dao.deleteCustomCampsite(id)
        dao.deleteCampsite(id)

        val user = auth.currentUser
        if (user != null) {
            try {
                db.collection("campsites").document(id).delete().await()
            } catch (e: Exception) {
                Log.w(TAG, "Error deleting campsite from cloud: ${e.message}")
            }
        }
    }

    suspend fun seedInitialCampsites() {
        val roomEntities = curatedCampsites.map { it.toRoomCampsite() }
        dao.insertAllCampsites(roomEntities)
    }

    // Default Gear Items
    val defaultGearList: List<GearEntity> = listOf(
        GearEntity("g_sleep_1", GearCategory.SLEEP.name, "3-Season Tent / Rainfly", "Seam sealed with wind stakes", false),
        GearEntity("g_sleep_2", GearCategory.SLEEP.name, "Insulated Sleeping Pad", "R-value 3.5+ for ground insulation", false),
        GearEntity("g_sleep_3", GearCategory.SLEEP.name, "Sleeping Bag (Down/Synthetic)", "Rated 5°C below expected low", false),
        GearEntity("g_sleep_4", GearCategory.SLEEP.name, "Ground Tarp & Footprint", "Protects tent floor from damp ground", false),
        GearEntity("g_sleep_5", GearCategory.SLEEP.name, "Hammock & Tree-Saver Straps", "Minimum 1-inch webbing for tree health", false),
        GearEntity("g_water_1", GearCategory.WATER.name, "Gravity Water Filter (0.1 Micron)", "Hollow fiber membrane for stream water", false),
        GearEntity("g_water_2", GearCategory.WATER.name, "Wide-Mouth Water Bottles / Bladder", "2L to 4L hydration capacity", false),
        GearEntity("g_water_3", GearCategory.WATER.name, "Collapsible 10L Water Camp Bucket", "For washing & carrying stream water", false),
        GearEntity("g_water_4", GearCategory.WATER.name, "Water Purification Tablets", "Emergency chlorine dioxide backup", false),
        GearEntity("g_energy_1", GearCategory.ENERGY.name, "20,000mAh Rugged Power Bank", "Dual USB-C PD 30W output", false),
        GearEntity("g_energy_2", GearCategory.ENERGY.name, "Foldable 28W - 100W Solar Panel", "SunPower cells with carabiner loops", false),
        GearEntity("g_energy_3", GearCategory.ENERGY.name, "Rechargeable Headlamp (350+ lm)", "With red night-vision LED mode", false),
        GearEntity("g_energy_4", GearCategory.ENERGY.name, "Camp Lantern & Ambient String", "Warm 2700K campsite illumination", false),
        GearEntity("g_energy_5", GearCategory.ENERGY.name, "CEE Euro Campervan Cable & Adapter", "Standard European 16A campsite hookup plug", false),
        GearEntity("g_fire_1", GearCategory.CAMP_COOKING.name, "Windproof Isobutane Camp Stove", "Fast boiling time in mountain wind", false),
        GearEntity("g_fire_2", GearCategory.CAMP_COOKING.name, "Waterproof Storm Matches & Ferro Rod", "Reliable ignition in damp weather", false),
        GearEntity("g_fire_3", GearCategory.CAMP_COOKING.name, "Anodized Cookware Pot & Spork", "Compact nesting kit", false),
        GearEntity("g_fire_4", GearCategory.CAMP_COOKING.name, "Biodegradable Camp Soap", "Use at least 60m away from water source", false)
    )

    fun getGearListFlow(): Flow<List<GearItem>> {
        return dao.getAllGearItems().map { entities: List<GearEntity> ->
            if (entities.isEmpty()) {
                defaultGearList.map { it.toGearItem() }
            } else {
                entities.map { it.toGearItem() }
            }
        }
    }

    suspend fun toggleGearItem(id: String, isChecked: Boolean) {
        val existing = dao.getGearItem(id)
        if (existing != null) {
            dao.updateGearChecked(id, isChecked)
        } else {
            val def = defaultGearList.find { it.id == id }
            if (def != null) {
                dao.insertGearItem(def.copy(isChecked = isChecked))
            } else {
                dao.updateGearChecked(id, isChecked)
            }
        }
    }

    suspend fun addCustomGearItem(title: String, category: GearCategory) {
        val newId = "gear_user_${System.currentTimeMillis()}"
        val item = GearEntity(
            id = newId,
            category = category.name,
            title = title,
            subtitle = "Custom camper item",
            isChecked = false
        )
        dao.insertGearItem(item)
    }

    suspend fun removeGearItem(id: String) {
        dao.deleteGearItem(id)
    }

    fun getReviewsFlow(campsiteId: String): Flow<List<CampsiteReviewEntity>> {
        return dao.getReviewsForCampsite(campsiteId)
    }

    suspend fun submitReview(review: CampsiteReviewEntity) {
        dao.insertReview(review)
        val user = auth.currentUser
        if (user != null) {
            try {
                db.collection("campsites").document(review.campsiteId)
                    .collection("reviews").document(review.id)
                    .set(review).await()
            } catch (e: Exception) {
                Log.w(TAG, "Error syncing review to Firestore: ${e.message}")
            }
        }
    }

    private fun parseCampsiteFromFirestore(docId: String, data: Map<String, Any>): Campsite {
        val name = cleanCampsiteName(data["name"] as? String ?: "Community Campsite")
        val region = data["region"] as? String ?: "Open Wilderness"
        val stateOrCountry = data["stateOrCountry"] as? String ?: "Europe"
        val lat = (data["latitude"] as? Number)?.toDouble() ?: 46.5
        val lon = (data["longitude"] as? Number)?.toDouble() ?: 9.9
        val fee = data["feePerNight"] as? String ?: "Free"
        val rating = (data["rating"] as? Number)?.toDouble() ?: 4.8
        val reviewCount = (data["reviewCount"] as? Number)?.toInt() ?: 1

        val sleepTypeStr = data["sleepType"] as? String ?: "TENT"
        val groundTypeStr = data["groundType"] as? String ?: "SOFT_TURF"
        val maxCap = (data["maxCapacity"] as? Number)?.toInt() ?: 4
        val hammock = data["hammockFriendly"] as? Boolean ?: true
        val shade = (data["shadeRating"] as? Number)?.toInt() ?: 4
        val quiet = data["quietHours"] as? String ?: "10:00 PM - 7:00 AM"
        val elevation = (data["elevationFt"] as? Number)?.toInt() ?: 2500

        val waterTypeStr = data["waterSourceType"] as? String ?: "POTABLE_TAP"
        val waterDist = (data["distanceToSourceMeters"] as? Number)?.toInt() ?: 20
        val hotShowers = data["hasHotShowers"] as? Boolean ?: true
        val coldShowers = data["hasColdShowers"] as? Boolean ?: true
        val dishSink = data["hasDishwashingSink"] as? Boolean ?: true
        val flowReliability = data["flowReliability"] as? String ?: "Potable fresh water"

        val energyTypeStr = data["energySourceType"] as? String ?: "STANDARD_15A_OUTLET"
        val solarIndex = (data["solarExposureIndex"] as? Number)?.toInt() ?: 8
        val genAllowed = data["generatorAllowed"] as? Boolean ?: false
        val genHours = data["generatorHours"] as? String ?: "No generators"
        val ring = data["campfireRing"] as? Boolean ?: false
        val firewood = data["firewoodPurchasable"] as? Boolean ?: false
        val ev = data["hasEvCharging"] as? Boolean ?: false

        val cell = (data["cellReceptionBars"] as? Number)?.toInt() ?: 4
        val terrain = data["terrainType"] as? String ?: "Alpine Forest"
        val desc = data["description"] as? String ?: "Community added campsite shared with all CampHaven campers."
        val tips = data["insiderTips"] as? String ?: "Shared via CampHaven Cloud."
        val photoUrl = data["photoUrl"] as? String ?: "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800"
        val isP4N = data["isPark4NightVerified"] as? Boolean ?: false

        return Campsite(
            id = docId,
            name = name,
            region = region,
            stateOrCountry = stateOrCountry,
            latitude = lat,
            longitude = lon,
            feePerNight = fee,
            rating = rating,
            reviewCount = reviewCount,
            sleep = SleepDetails(
                type = runCatching { SleepType.valueOf(sleepTypeStr) }.getOrDefault(SleepType.TENT),
                groundType = runCatching { GroundType.valueOf(groundTypeStr) }.getOrDefault(GroundType.SOFT_TURF),
                maxCapacity = maxCap,
                hammockFriendly = hammock,
                shadeRating = shade,
                quietHours = quiet,
                elevationFt = elevation
            ),
            water = WaterDetails(
                sourceType = runCatching { WaterSourceType.valueOf(waterTypeStr) }.getOrDefault(WaterSourceType.POTABLE_TAP),
                distanceToSourceMeters = waterDist,
                hasHotShowers = hotShowers,
                hasColdShowers = coldShowers,
                hasDishwashingSink = dishSink,
                flowReliability = flowReliability
            ),
            energy = EnergyDetails(
                sourceType = runCatching { EnergySourceType.valueOf(energyTypeStr) }.getOrDefault(EnergySourceType.STANDARD_15A_OUTLET),
                solarExposureIndex = solarIndex,
                generatorAllowed = genAllowed,
                generatorHours = genHours,
                campfireRing = ring,
                firewoodPurchasable = firewood,
                hasEvCharging = ev
            ),
            cellReceptionBars = cell,
            terrainType = terrain,
            description = desc,
            insiderTips = tips,
            photoUrl = photoUrl,
            photoUrls = listOf(photoUrl),
            isPark4NightVerified = isP4N,
            isUserCreated = true
        )
    }

    private fun Campsite.toFirestoreMap(userId: String): Map<String, Any> {
        return hashMapOf(
            "name" to name,
            "region" to region,
            "stateOrCountry" to stateOrCountry,
            "latitude" to latitude,
            "longitude" to longitude,
            "feePerNight" to feePerNight,
            "rating" to rating,
            "reviewCount" to reviewCount,
            "sleepType" to sleep.type.name,
            "groundType" to sleep.groundType.name,
            "maxCapacity" to sleep.maxCapacity,
            "hammockFriendly" to sleep.hammockFriendly,
            "shadeRating" to sleep.shadeRating,
            "quietHours" to sleep.quietHours,
            "elevationFt" to sleep.elevationFt,
            "waterSourceType" to water.sourceType.name,
            "distanceToSourceMeters" to water.distanceToSourceMeters,
            "hasHotShowers" to water.hasHotShowers,
            "hasColdShowers" to water.hasColdShowers,
            "hasDishwashingSink" to water.hasDishwashingSink,
            "flowReliability" to water.flowReliability,
            "energySourceType" to energy.sourceType.name,
            "solarExposureIndex" to energy.solarExposureIndex,
            "generatorAllowed" to energy.generatorAllowed,
            "generatorHours" to energy.generatorHours,
            "campfireRing" to energy.campfireRing,
            "firewoodPurchasable" to energy.firewoodPurchasable,
            "hasEvCharging" to energy.hasEvCharging,
            "cellReceptionBars" to cellReceptionBars,
            "terrainType" to terrainType,
            "description" to description,
            "insiderTips" to insiderTips,
            "photoUrl" to photoUrl,
            "isPark4NightVerified" to isPark4NightVerified,
            "userId" to userId,
            "isUserCreated" to true,
            "updatedAt" to FieldValue.serverTimestamp()
        )
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
        photoUrl = photoUrl.ifBlank { "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800" },
        photoUrls = listOf(photoUrl.ifBlank { "https://images.unsplash.com/photo-1506744038136-46273834b3fb?w=800" }),
        isPark4NightVerified = isPark4NightVerified,
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
        photoUrl = photoUrl,
        isPark4NightVerified = isPark4NightVerified,
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
