package com.example.ui.screens

import android.Manifest
import android.content.Context
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Campsite
import com.example.data.model.UnitSystem
import com.example.ui.components.CampsiteCard
import com.example.ui.viewmodel.*
import com.example.util.GoogleMapsHelper
import com.example.util.LocationTracker
import com.example.util.Park4NightHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExploreScreen(
    viewModel: CampsiteViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val filterState by viewModel.filterState.collectAsState()
    val campsites by viewModel.filteredCampsites.collectAsState()
    val userCoords by viewModel.userCoordinates.collectAsState()
    val lastSyncInfo by viewModel.lastSyncInfo.collectAsState()
    val unitSystem by viewModel.unitSystem.collectAsState()
    val showUnitPrompt by viewModel.showUnitPrompt.collectAsState()

    var showSortMenu by remember { mutableStateOf(false) }
    var showAreaMenu by remember { mutableStateOf(false) }
    var isLocating by remember { mutableStateOf(false) }
    var isSyncing by remember { mutableStateOf(false) }

    // GPS Permission launcher for Auto-Find
    val locationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineGranted || coarseGranted) {
            isLocating = true
            LocationTracker.requestCurrentLocation(
                context = context,
                onSuccess = { lat, lon ->
                    isLocating = false
                    viewModel.triggerAutoFindInArea(lat, lon, "Your GPS Location")
                    Toast.makeText(context, "Location updated: campsites nearby loaded!", Toast.LENGTH_SHORT).show()
                },
                onFailure = { err ->
                    isLocating = false
                    Toast.makeText(context, "$err Defaulting to Europe Hub.", Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            Toast.makeText(context, "Location permission not granted. You can pick European regions in 'Change Area'.", Toast.LENGTH_LONG).show()
        }
    }

    val triggerGpsAutoFind = {
        if (LocationTracker.hasLocationPermission(context)) {
            isLocating = true
            LocationTracker.requestCurrentLocation(
                context = context,
                onSuccess = { lat, lon ->
                    isLocating = false
                    viewModel.triggerAutoFindInArea(lat, lon, "Your GPS Location")
                    Toast.makeText(context, "Loaded campsites near your position!", Toast.LENGTH_SHORT).show()
                },
                onFailure = { err ->
                    isLocating = false
                    Toast.makeText(context, "$err Showing European camping hub.", Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            locationLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Terrain,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "CampHaven",
                                    fontSize = 19.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = MaterialTheme.colorScheme.secondaryContainer
                                ) {
                                    Text(
                                        text = "Made by Victor",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "European & Global Campsite Finder",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
                    // Unit selector chip toggle
                    TextButton(
                        onClick = {
                            val next = if (unitSystem == UnitSystem.METRIC) UnitSystem.IMPERIAL else UnitSystem.METRIC
                            viewModel.setUnitSystem(next)
                            Toast.makeText(context, "Units: ${next.label}", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("unit_toggle_btn")
                    ) {
                        Text(
                            text = "${unitSystem.flag} ${if (unitSystem == UnitSystem.METRIC) "Metric" else "Imperial"}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Radar Map button
                    IconButton(
                        onClick = { viewModel.navigateTo(ScreenDestination.RadarMap) },
                        modifier = Modifier.testTag("radar_map_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Explore,
                            contentDescription = "Radar Map",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Gear checklist button
                    IconButton(
                        onClick = { viewModel.navigateTo(ScreenDestination.GearChecklist) },
                        modifier = Modifier.testTag("gear_checklist_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.ChecklistRtl,
                            contentDescription = "Pack Checklist",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }

                    // Profile button
                    val user by viewModel.currentUser.collectAsState()
                    IconButton(
                        onClick = { viewModel.navigateTo(ScreenDestination.Profile) },
                        modifier = Modifier.testTag("profile_btn")
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                val initial = user?.displayName?.firstOrNull()?.uppercase() ?: "V"
                                Text(
                                    text = initial,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { viewModel.navigateTo(ScreenDestination.AddSpot) },
                icon = { Icon(Icons.Default.AddLocationAlt, contentDescription = null) },
                text = { Text("Log Camp Spot") },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_spot_fab")
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Search Input
            OutlinedTextField(
                value = filterState.query,
                onValueChange = { viewModel.setQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .testTag("search_input"),
                placeholder = { Text("Search France, Italy, Dolomites, Black Forest, pitch...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                trailingIcon = {
                    if (filterState.query.isNotEmpty()) {
                        IconButton(onClick = { viewModel.setQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear")
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface
                )
            )

            // EUROPEAN AREA & GOOGLE MAPS CONTROLLER CARD
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                )
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Region: ${filterState.areaLabel}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Region Selector Dropdown
                        Box {
                            TextButton(
                                onClick = { showAreaMenu = true },
                                modifier = Modifier.testTag("change_area_btn")
                            ) {
                                Text("Change Region", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                            }

                            DropdownMenu(
                                expanded = showAreaMenu,
                                onDismissRequest = { showAreaMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("📍 Use My Live GPS Location") },
                                    onClick = {
                                        showAreaMenu = false
                                        triggerGpsAutoFind()
                                    }
                                )
                                Divider()
                                DropdownMenuItem(
                                    text = { Text("🇪🇺 All Europe (Central Alps Hub)") },
                                    onClick = {
                                        showAreaMenu = false
                                        viewModel.setUserLocation(46.5197, 9.9534, "🇪🇺 Central Alps")
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("🇫🇷 France (Chamonix, Provence, Pilat)") },
                                    onClick = {
                                        showAreaMenu = false
                                        viewModel.setUserLocation(45.9237, 6.8694, "🇫🇷 France (Mont Blanc)")
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("🇮🇹 Italy (Dolomites, Garda, Sardinia)") },
                                    onClick = {
                                        showAreaMenu = false
                                        viewModel.setUserLocation(46.4302, 11.6983, "🇮🇹 Italy (Dolomites)")
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("🇩🇪 Germany (Black Forest & Bavaria)") },
                                    onClick = {
                                        showAreaMenu = false
                                        viewModel.setUserLocation(47.8542, 7.7125, "🇩🇪 Germany (Black Forest)")
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("🇨🇭 Switzerland (Lauterbrunnen & Valais)") },
                                    onClick = {
                                        showAreaMenu = false
                                        viewModel.setUserLocation(46.5937, 7.9078, "🇨🇭 Switzerland")
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("🇪🇸 Spain (Pyrenees & Andalusia)") },
                                    onClick = {
                                        showAreaMenu = false
                                        viewModel.setUserLocation(42.6642, 0.1236, "🇪🇸 Spain (Pyrenees)")
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("🇦🇹 Austria (Salzburg & Tyrol)") },
                                    onClick = {
                                        showAreaMenu = false
                                        viewModel.setUserLocation(47.5750, 12.7083, "🇦🇹 Austria")
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("🇳🇴 Norway (Geirangerfjord & Lofoten)") },
                                    onClick = {
                                        showAreaMenu = false
                                        viewModel.setUserLocation(62.1015, 7.2065, "🇳🇴 Norway")
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("🇵🇹 Portugal (Algarve Coast)") },
                                    onClick = {
                                        showAreaMenu = false
                                        viewModel.setUserLocation(37.0658, -8.8242, "🇵🇹 Portugal")
                                    }
                                )
                                Divider()
                                DropdownMenuItem(
                                    text = { Text("🇺🇸 USA (High Sierra & Pacific NW)") },
                                    onClick = {
                                        showAreaMenu = false
                                        viewModel.setUserLocation(37.8651, -119.5383, "🇺🇸 California Sierra")
                                    }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // 1. Auto-Find Near Me Button
                        Button(
                            onClick = { triggerGpsAutoFind() },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .testTag("auto_find_area_btn"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary
                            )
                        ) {
                            if (isLocating) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Locating...", fontSize = 12.sp)
                            } else {
                                Icon(Icons.Default.NearMe, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Auto-Find Near Me", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        // 2. Open Google Maps Search
                        OutlinedButton(
                            onClick = {
                                GoogleMapsHelper.searchNearbyCampingOnGoogleMaps(
                                    context = context,
                                    latitude = userCoords.first,
                                    longitude = userCoords.second,
                                    filterTerm = "camping campsites campervan water electricity"
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(40.dp)
                                .testTag("open_google_maps_nearby_btn"),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFF0277BD)
                            )
                        ) {
                            Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF0277BD))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Google Maps", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    // Quick Radius Filter Chips
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Radius:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        val radiusOptions = if (unitSystem == UnitSystem.METRIC) {
                            listOf(
                                null to "All Range",
                                31.0 to "< 50 km",
                                62.0 to "< 100 km",
                                155.0 to "< 250 km",
                                310.0 to "< 500 km"
                            )
                        } else {
                            listOf(
                                null to "All Range",
                                25.0 to "< 25 mi",
                                50.0 to "< 50 mi",
                                100.0 to "< 100 mi",
                                250.0 to "< 250 mi"
                            )
                        }

                        radiusOptions.forEach { (dist, label) ->
                            FilterChip(
                                selected = filterState.maxDistanceMiles == dist,
                                onClick = { viewModel.setMaxDistanceMiles(dist) },
                                label = { Text(label, fontSize = 11.sp) },
                                modifier = Modifier.height(30.dp)
                            )
                        }
                    }
                }
            }

            // CLOUD SYNC BAR
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CloudDone,
                        contentDescription = null,
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = lastSyncInfo,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = {
                        isSyncing = true
                        viewModel.manualSyncCloudData { success ->
                            isSyncing = false
                            Toast.makeText(context, if (success) "Campsites synced!" else "Sync completed", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    if (isSyncing) {
                        CircularProgressIndicator(modifier = Modifier.size(12.dp), strokeWidth = 2.dp)
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Sync Cloud",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(15.dp)
                        )
                    }
                }
            }

            // Primary Tri-Pillar Tabs (All, Sleep, Water, Energy, Saved)
            ScrollableTabRow(
                selectedTabIndex = filterState.pillar.ordinal,
                edgePadding = 16.dp,
                divider = {},
                containerColor = MaterialTheme.colorScheme.background,
                modifier = Modifier.fillMaxWidth()
            ) {
                Tab(
                    selected = filterState.pillar == ActivePillarFilter.ALL,
                    onClick = { viewModel.setPillar(ActivePillarFilter.ALL) },
                    text = { Text("All Sites (${campsites.size})") },
                    icon = { Icon(Icons.Default.Forest, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = filterState.pillar == ActivePillarFilter.SLEEP,
                    onClick = { viewModel.setPillar(ActivePillarFilter.SLEEP) },
                    text = { Text("🛏️ Sleep Focus") },
                    icon = { Icon(Icons.Default.Bed, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = filterState.pillar == ActivePillarFilter.WATER,
                    onClick = { viewModel.setPillar(ActivePillarFilter.WATER) },
                    text = { Text("💧 Water Verified") },
                    icon = { Icon(Icons.Default.WaterDrop, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = filterState.pillar == ActivePillarFilter.ENERGY,
                    onClick = { viewModel.setPillar(ActivePillarFilter.ENERGY) },
                    text = { Text("⚡ Shore / Solar") },
                    icon = { Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = filterState.pillar == ActivePillarFilter.SAVED,
                    onClick = { viewModel.setPillar(ActivePillarFilter.SAVED) },
                    text = { Text("⭐ Bookmarked") },
                    icon = { Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }

            // Quick Filter Chips Row (Including Park4night)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Park4night toggle
                FilterChip(
                    selected = filterState.park4NightOnly,
                    onClick = { viewModel.togglePark4NightOnly() },
                    label = { Text("🌲 Park4night Spots") },
                    leadingIcon = {
                        if (filterState.park4NightOnly) {
                            Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                    }
                )

                FilterChip(
                    selected = filterState.sleepType == com.example.data.model.SleepType.CAMPERVAN,
                    onClick = {
                        viewModel.setSleepType(
                            if (filterState.sleepType == com.example.data.model.SleepType.CAMPERVAN) null else com.example.data.model.SleepType.CAMPERVAN
                        )
                    },
                    label = { Text("🚐 Campervan / RV") }
                )

                FilterChip(
                    selected = filterState.potableOnly,
                    onClick = { viewModel.togglePotableOnly() },
                    label = { Text("Potable Tap") }
                )

                FilterChip(
                    selected = filterState.electricHookupOnly,
                    onClick = { viewModel.toggleElectricHookupOnly() },
                    label = { Text("16A / 30A Hookup") }
                )

                FilterChip(
                    selected = filterState.showersRequired,
                    onClick = { viewModel.toggleShowersRequired() },
                    label = { Text("Hot Showers") }
                )

                // Sort Chip
                Box {
                    AssistChip(
                        onClick = { showSortMenu = true },
                        label = { Text("Sort: ${filterState.sortOption.label}") },
                        leadingIcon = { Icon(Icons.Default.Sort, contentDescription = null, modifier = Modifier.size(14.dp)) }
                    )

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        CampsiteSortOption.values().forEach { option ->
                            DropdownMenuItem(
                                text = { Text(option.label) },
                                onClick = {
                                    viewModel.setSortOption(option)
                                    showSortMenu = false
                                }
                            )
                        }
                    }
                }
            }

            // Campsites List
            if (campsites.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ExploreOff,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "No campsites match your filters",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Try clearing filters or switching to '🇪🇺 All Europe'.",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(onClick = { viewModel.resetFilters() }) {
                            Text("Reset All Filters")
                        }
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 6.dp, bottom = 88.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(items = campsites, key = { it.id }) { site ->
                        val formattedDist = viewModel.getFormattedDistanceToSite(site)
                        val formattedElev = unitSystem.formatElevation(site.sleep.elevationFt)

                        CampsiteCard(
                            campsite = site,
                            formattedDistance = formattedDist,
                            formattedElevation = formattedElev,
                            onClick = { viewModel.navigateTo(ScreenDestination.Detail(site.id)) },
                            onToggleBookmark = { viewModel.toggleBookmark(site) },
                            onOpenGoogleMaps = {
                                GoogleMapsHelper.openCampsiteInGoogleMaps(
                                    context = context,
                                    latitude = site.latitude,
                                    longitude = site.longitude,
                                    campsiteName = site.name
                                )
                            },
                            onOpenPark4Night = {
                                Park4NightHelper.openSpotInPark4Night(
                                    context = context,
                                    latitude = site.latitude,
                                    longitude = site.longitude,
                                    spotName = site.name
                                )
                            }
                        )
                    }

                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "CampHaven • Made by Victor",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }

    // First-launch Unit Prompt: Ask for European or American metrics
    if (showUnitPrompt) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissUnitPrompt() },
            icon = { Icon(Icons.Default.Straighten, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
            title = { Text("Choose Metric System", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        text = "Would you like European Metric or American Imperial units for distances, elevations, and vehicle measurements?",
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Text(
                        text = "🇪🇺 European Metric: Kilometers (km), Meters (m), Kilograms (kg)\n🇺🇸 American Imperial: Miles (mi), Feet (ft), Pounds (lbs)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.setUnitSystem(UnitSystem.METRIC) }
                ) {
                    Text("🇪🇺 European Metric (km / m)")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { viewModel.setUnitSystem(UnitSystem.IMPERIAL) }
                ) {
                    Text("🇺🇸 American Imperial (mi / ft)")
                }
            }
        )
    }
}
