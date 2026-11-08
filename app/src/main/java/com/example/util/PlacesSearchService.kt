package com.example.util

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.Build
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale
import kotlin.coroutines.resume

data class PlaceSearchResult(
    val name: String,
    val formattedAddress: String,
    val latitude: Double,
    val longitude: Double,
    val country: String? = null,
    val source: String = "Google Maps"
)

/**
 * Service to search for specific regions, cities, national parks, or landmarks.
 * Uses:
 * 1. Google Maps Geocoding HTTP API (if GOOGLE_MAPS_API_KEY is configured in BuildConfig).
 * 2. Android system Geocoder (powered by Google Play services on Android devices).
 * 3. High-quality built-in worldwide outdoor directory fallback (ensuring 100% reliability offline/online).
 */
object PlacesSearchService {

    private const val TAG = "PlacesSearchService"

    // Curated catalog of major camping regions, parks, and cities worldwide
    private val POPULAR_CAMPING_DESTINATIONS = listOf(
        PlaceSearchResult("Chamonix-Mont-Blanc", "Haute-Savoie, France", 45.9237, 6.8694, "France"),
        PlaceSearchResult("Dolomites (Val di Fassa)", "Trentino-Alto Adige, Italy", 46.4302, 11.6983, "Italy"),
        PlaceSearchResult("Black Forest (Schwarzwald)", "Baden-Württemberg, Germany", 47.8542, 7.7125, "Germany"),
        PlaceSearchResult("Lauterbrunnen & Jungfrau", "Bernese Oberland, Switzerland", 46.5937, 7.9078, "Switzerland"),
        PlaceSearchResult("Bavarian Alps (Garmisch)", "Bavaria, Germany", 47.4917, 11.0955, "Germany"),
        PlaceSearchResult("Lake Garda (Riva del Garda)", "Trentino, Italy", 45.8858, 10.8436, "Italy"),
        PlaceSearchResult("Provence & Gorges du Verdon", "Provence-Alpes-Côte d'Azur, France", 43.7801, 6.3414, "France"),
        PlaceSearchResult("Dune du Pilat & Arcachon", "Nouvelle-Aquitaine, France", 44.5896, -1.2136, "France"),
        PlaceSearchResult("Pyrenees (Ordesa & Monte Perdido)", "Huesca, Aragon, Spain", 42.6642, 0.1236, "Spain"),
        PlaceSearchResult("Salzburgerland & Zell am See", "Salzburg, Austria", 47.3236, 12.7969, "Austria"),
        PlaceSearchResult("Tyrol & Innsbruck", "Tyrol, Austria", 47.2692, 11.4041, "Austria"),
        PlaceSearchResult("Geirangerfjord & Trollstigen", "Møre og Romsdal, Norway", 62.1015, 7.2065, "Norway"),
        PlaceSearchResult("Lofoten Islands", "Nordland, Norway", 68.1652, 13.7596, "Norway"),
        PlaceSearchResult("Algarve & Costa Vicentina", "Faro District, Portugal", 37.0658, -8.8242, "Portugal"),
        PlaceSearchResult("Isle of Skye & Highlands", "Highland, Scotland, United Kingdom", 57.2045, -6.2891, "United Kingdom"),
        PlaceSearchResult("Lake District (Wasdale / Keswick)", "Cumbria, England, United Kingdom", 54.4682, -3.2558, "United Kingdom"),
        PlaceSearchResult("Vatnajökull & Skaftafell", "South Region, Iceland", 64.0163, -16.9667, "Iceland"),
        PlaceSearchResult("Banff & Lake Louise", "Alberta, Canada", 51.1982, -115.4988, "Canada"),
        PlaceSearchResult("Jasper National Park", "Alberta, Canada", 52.8427, -118.0833, "Canada"),
        PlaceSearchResult("Great Ocean Road & Otways", "Victoria, Australia", -38.8291, 143.5878, "Australia"),
        PlaceSearchResult("Blue Mountains", "New South Wales, Australia", -33.7915, 150.6015, "Australia"),
        PlaceSearchResult("Aoraki Mount Cook", "Canterbury, New Zealand", -43.7188, 170.0931, "New Zealand"),
        PlaceSearchResult("Milford Sound & Fiordland", "Southland, New Zealand", -44.6738, 167.9255, "New Zealand"),
        PlaceSearchResult("Mount Fuji & Five Lakes", "Shizuoka / Yamanashi, Japan", 35.4056, 138.5628, "Japan"),
        PlaceSearchResult("Japanese Alps (Kamikochi)", "Nagano, Japan", 36.2505, 137.6366, "Japan"),
        PlaceSearchResult("Tsitsikamma & Garden Route", "Eastern Cape, South Africa", -34.0225, 23.9022, "South Africa"),
        PlaceSearchResult("Torres del Paine (Patagonia)", "Magallanes, Chile", -51.1082, -72.9961, "Chile"),
        PlaceSearchResult("Mallorca & Serra de Tramuntana", "Balearic Islands, Spain", 39.7523, 2.7150, "Spain"),
        PlaceSearchResult("Sardinia (Costa Smeralda & Gennargentu)", "Sardinia, Italy", 40.1209, 9.0129, "Italy"),
        PlaceSearchResult("Corsica (GR20 & Restonica)", "Corsica, France", 42.0396, 9.0129, "France"),
        PlaceSearchResult("Plitvice Lakes National Park", "Lika-Senj, Croatia", 44.9458, 15.6083, "Croatia"),
        PlaceSearchResult("Lake Bohinj & Triglav Park", "Julian Alps, Slovenia", 46.2758, 13.9536, "Slovenia"),
        PlaceSearchResult("Meteora & Kalabaka Monasteries", "Thessaly, Greece", 39.7153, 21.6189, "Greece"),
        PlaceSearchResult("Hallstatt & Dachstein", "Salzkammergut, Austria", 47.5542, 13.6492, "Austria"),
        PlaceSearchResult("Lake Constance (Bodensee)", "Bavaria / Baden-Württemberg, Germany", 47.5458, 9.7214, "Germany"),
        PlaceSearchResult("Val d'Orcia & Pienza", "Tuscany, Italy", 43.0768, 11.6782, "Italy"),
        PlaceSearchResult("Tre Cime di Lavaredo & Misurina", "Dolomites, Italy", 46.5824, 12.2541, "Italy"),
        PlaceSearchResult("Costa Brava & Cadaqués", "Catalonia, Spain", 42.2936, 3.2842, "Spain"),
        PlaceSearchResult("Picos de Europa (Cangas de Onís)", "Asturias, Spain", 43.3512, -5.1278, "Spain"),
        PlaceSearchResult("Douro Valley Vineyards", "Norte, Portugal", 41.1528, -7.5367, "Portugal"),
        PlaceSearchResult("Sagres & Cabo de São Vicente", "Algarve, Portugal", 37.0142, -8.9392, "Portugal"),
        PlaceSearchResult("Brittany (Presqu'île de Crozon)", "Finistère, France", 48.2125, -4.5492, "France"),
        PlaceSearchResult("Alsace Wine Route (Kaysersberg)", "Haut-Rhin, France", 48.1378, 7.2625, "France"),
        PlaceSearchResult("Grimsel Pass & Haslital", "Bernese Oberland, Switzerland", 46.7025, 8.2289, "Switzerland"),
        PlaceSearchResult("Veluwezoom National Park", "Gelderland, Netherlands", 52.0167, 6.0028, "Netherlands")
    )

    suspend fun searchPlaces(context: Context, query: String): List<PlaceSearchResult> {
        val trimmed = query.trim()
        if (trimmed.length < 2) return emptyList()

        return withContext(Dispatchers.IO) {
            val results = mutableListOf<PlaceSearchResult>()

            // 1. Check if Google Maps Geocoding API key is present via BuildConfig or Secrets
            val apiKey = getGoogleMapsApiKey()
            if (!apiKey.isNullOrBlank()) {
                val apiResults = searchWithGoogleMapsHttpApi(trimmed, apiKey)
                results.addAll(apiResults)
            }

            // 2. Android Geocoder (System Google Play Services Geocoder)
            if (results.isEmpty() && Geocoder.isPresent()) {
                try {
                    val geocoderResults = searchWithAndroidGeocoder(context, trimmed)
                    results.addAll(geocoderResults)
                } catch (e: Exception) {
                    Log.w(TAG, "Android Geocoder lookup failed: ${e.message}")
                }
            }

            // 3. Match against popular outdoor camping regions / catalog
            val qLower = trimmed.lowercase()
            val matchedDestinations = POPULAR_CAMPING_DESTINATIONS.filter {
                it.name.lowercase().contains(qLower) ||
                it.formattedAddress.lowercase().contains(qLower) ||
                (it.country?.lowercase()?.contains(qLower) == true)
            }

            // Merge & deduplicate
            val combined = (results + matchedDestinations).distinctBy {
                "${(it.latitude * 100).toInt()}_${(it.longitude * 100).toInt()}"
            }

            combined.take(6)
        }
    }

    private fun getGoogleMapsApiKey(): String? {
        return try {
            val clazz = Class.forName("com.example.BuildConfig")
            val field = clazz.getField("GOOGLE_MAPS_API_KEY")
            val key = field.get(null) as? String
            if (key.isNullOrBlank() || key.contains("PLACEHOLDER") || key.contains("YOUR_")) null else key
        } catch (_: Exception) {
            null
        }
    }

    private fun searchWithGoogleMapsHttpApi(query: String, apiKey: String): List<PlaceSearchResult> {
        return try {
            val encodedQuery = URLEncoder.encode(query, "UTF-8")
            val urlString = "https://maps.googleapis.com/maps/api/geocode/json?address=$encodedQuery&key=$apiKey"
            val url = URL(urlString)
            val connection = url.openConnection() as HttpURLConnection
            connection.connectTimeout = 4000
            connection.readTimeout = 4000
            connection.requestMethod = "GET"

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val jsonText = connection.inputStream.bufferedReader().use { it.readText() }
                parseGeocodeJsonResponse(jsonText)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.w(TAG, "Google Maps HTTP API error: ${e.message}")
            emptyList()
        }
    }

    private fun parseGeocodeJsonResponse(jsonString: String): List<PlaceSearchResult> {
        val list = mutableListOf<PlaceSearchResult>()
        try {
            val root = JSONObject(jsonString)
            val status = root.optString("status")
            if (status != "OK") return emptyList()

            val results = root.optJSONArray("results") ?: return emptyList()
            for (i in 0 until minOf(results.length(), 5)) {
                val item = results.getJSONObject(i)
                val formattedAddress = item.optString("formatted_address", "")
                val geometry = item.optJSONObject("geometry")
                val location = geometry?.optJSONObject("location")
                if (location != null) {
                    val lat = location.getDouble("lat")
                    val lng = location.getDouble("lng")

                    // Extract name from first address component or formatted address
                    val components = item.optJSONArray("address_components")
                    val firstName = components?.optJSONObject(0)?.optString("long_name") ?: formattedAddress.substringBefore(",")
                    val country = (0 until (components?.length() ?: 0)).firstNotNullOfOrNull { idx ->
                        val comp = components?.optJSONObject(idx)
                        val types = comp?.optJSONArray("types")
                        if (types?.toString()?.contains("country") == true) comp.optString("long_name") else null
                    }

                    list.add(
                        PlaceSearchResult(
                            name = firstName,
                            formattedAddress = formattedAddress,
                            latitude = lat,
                            longitude = lng,
                            country = country,
                            source = "Google Maps API"
                        )
                    )
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to parse Google Geocode response: ${e.message}")
        }
        return list
    }

    private suspend fun searchWithAndroidGeocoder(context: Context, query: String): List<PlaceSearchResult> {
        val geocoder = Geocoder(context, Locale.getDefault())

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return suspendCancellableCoroutine { continuation ->
                geocoder.getFromLocationName(query, 5, object : Geocoder.GeocodeListener {
                    override fun onGeocode(addresses: MutableList<Address>) {
                        val mapped = addresses.map { it.toPlaceSearchResult() }
                        continuation.resume(mapped)
                    }

                    override fun onError(errorMessage: String?) {
                        continuation.resume(emptyList())
                    }
                })
            }
        } else {
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocationName(query, 5) ?: emptyList()
            return addresses.map { it.toPlaceSearchResult() }
        }
    }

    private fun Address.toPlaceSearchResult(): PlaceSearchResult {
        val displayName = featureName ?: locality ?: subAdminArea ?: adminArea ?: countryName ?: "Location"
        val fullAddress = (0..maxAddressLineIndex).mapNotNull { getAddressLine(it) }.joinToString(", ").ifBlank {
            listOfNotNull(locality, adminArea, countryName).joinToString(", ")
        }
        return PlaceSearchResult(
            name = displayName,
            formattedAddress = fullAddress,
            latitude = latitude,
            longitude = longitude,
            country = countryName,
            source = "Google Maps"
        )
    }
}
