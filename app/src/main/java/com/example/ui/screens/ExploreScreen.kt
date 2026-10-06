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
import com.example.ui.components.CampsiteCard
import com.example.ui.viewmodel.*
import com.example.util.GoogleMapsHelper
import com.example.util.LocationTracker

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

    var showSortMenu by remember { mutableStateOf(false) }
    var showAreaMenu by remember { mutableStateOf(false) }
    var isLocating by remember { mutableStateOf(false) }

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
                    viewModel.triggerAutoFindInArea(lat, lon, "Your GPS Area")
                    Toast.makeText(context, "Auto-found camping in your area!", Toast.LENGTH_SHORT).show()
                },
                onFailure = { err ->
                    isLocating = false
                    Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
                }
            )
        } else {
            Toast.makeText(context, "Location permission needed to auto-find in your area.", Toast.LENGTH_LONG).show()
        }
    }

    val triggerGpsAutoFind = {
        if (LocationTracker.hasLocationPermission(context)) {
            isLocating = true
            LocationTracker.requestCurrentLocation(
                context = context,
                onSuccess = { lat, lon ->
                    isLocating = false
                    viewModel.triggerAutoFindInArea(lat, lon, "Your GPS Area")
                    Toast.makeText(context, "Found campsites near your location!", Toast.LENGTH_SHORT).show()
                },
                onFailure = { err ->
                    isLocating = false
                    Toast.makeText(context, err, Toast.LENGTH_SHORT).show()
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
                            Text(
                                text = "CampHaven",
                                fontSize = 19.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Sleep • Water • Energy Essentials",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                actions = {
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

                    // Profile / Account button
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
                                val initial = user?.displayName?.firstOrNull()?.uppercase() ?: "C"
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
                placeholder = { Text("Search park, river, power hookup, tent...") },
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

            // AUTO-FIND IN AREA & GOOGLE MAPS ACTION CARD
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
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Area: ${filterState.areaLabel}",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }

                        // Area Switcher button
                        Box {
                            TextButton(
                                onClick = { showAreaMenu = true },
                                modifier = Modifier.testTag("change_area_btn")
                            ) {
                                Text("Change Area", fontSize = 11.sp)
                                Icon(Icons.Default.ArrowDropDown, contentDescription = null, modifier = Modifier.size(16.dp))
                            }

                            DropdownMenu(
                                expanded = showAreaMenu,
                                onDismissRequest = { showAreaMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("📍 Live Device GPS") },
                                    onClick = {
                                        showAreaMenu = false
                                        triggerGpsAutoFind()
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("🌲 Sierra Nevada (Yosemite)") },
                                    onClick = {
                                        showAreaMenu = false
                                        viewModel.setUserLocation(37.8651, -119.5383, "High Sierra")
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("🌧️ Pacific Northwest (Olympic)") },
                                    onClick = {
                                        showAreaMenu = false
                                        viewModel.setUserLocation(47.8021, -123.6044, "Pacific Northwest")
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("🏜️ Red Rock Deserts (Moab / Zion)") },
                                    onClick = {
                                        showAreaMenu = false
                                        viewModel.setUserLocation(38.5733, -109.5498, "Red Rock Canyonlands")
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("🏔️ Rocky Mountains (Colorado)") },
                                    onClick = {
                                        showAreaMenu = false
                                        viewModel.setUserLocation(40.3428, -105.6836, "Rocky Mountains")
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
                        // 1. Auto-Find in Area Button
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

                        // 2. Open Google Maps Camping Search
                        OutlinedButton(
                            onClick = {
                                GoogleMapsHelper.searchNearbyCampingOnGoogleMaps(
                                    context = context,
                                    latitude = userCoords.first,
                                    longitude = userCoords.second,
                                    filterTerm = "camping campsites with water and electricity"
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

                    // Radius Filter Chips (Nearby distance range)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Radius:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

                        listOf(
                            null to "All Range",
                            25.0 to "< 25 mi",
                            50.0 to "< 50 mi",
                            100.0 to "< 100 mi",
                            250.0 to "< 250 mi"
                        ).forEach { (dist, label) ->
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
                    text = { Text("All Sites") },
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
                    text = { Text("💧 Water Focus") },
                    icon = { Icon(Icons.Default.WaterDrop, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = filterState.pillar == ActivePillarFilter.ENERGY,
                    onClick = { viewModel.setPillar(ActivePillarFilter.ENERGY) },
                    text = { Text("⚡ Energy Focus") },
                    icon = { Icon(Icons.Default.Bolt, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
                Tab(
                    selected = filterState.pillar == ActivePillarFilter.SAVED,
                    onClick = { viewModel.setPillar(ActivePillarFilter.SAVED) },
                    text = { Text("⭐ Saved") },
                    icon = { Icon(Icons.Default.Bookmark, contentDescription = null, modifier = Modifier.size(18.dp)) }
                )
            }

            // Secondary Quick Filter Chips & Sort
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sort Menu Trigger
                Box {
                    AssistChip(
                        onClick = { showSortMenu = true },
                        label = { Text(filterState.sortOption.label) },
                        leadingIcon = { Icon(Icons.Default.Sort, contentDescription = null, modifier = Modifier.size(16.dp)) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                        ),
                        modifier = Modifier.testTag("sort_menu_chip")
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
                                },
                                leadingIcon = {
                                    if (filterState.sortOption == option) {
                                        Icon(Icons.Default.Check, contentDescription = null)
                                    }
                                }
                            )
                        }
                    }
                }

                FilterChip(
                    selected = filterState.potableOnly,
                    onClick = { viewModel.togglePotableOnly() },
                    label = { Text("Potable Water") },
                    leadingIcon = { Icon(Icons.Default.Water, contentDescription = null, modifier = Modifier.size(14.dp)) }
                )

                FilterChip(
                    selected = filterState.showersRequired,
                    onClick = { viewModel.toggleShowersRequired() },
                    label = { Text("Showers") },
                    leadingIcon = { Icon(Icons.Default.Shower, contentDescription = null, modifier = Modifier.size(14.dp)) }
                )

                FilterChip(
                    selected = filterState.electricHookupOnly,
                    onClick = { viewModel.toggleElectricHookupOnly() },
                    label = { Text("Electric Hookups") },
                    leadingIcon = { Icon(Icons.Default.Power, contentDescription = null, modifier = Modifier.size(14.dp)) }
                )

                FilterChip(
                    selected = filterState.solarHighExposureOnly,
                    onClick = { viewModel.toggleSolarOnly() },
                    label = { Text("Solar Prime") },
                    leadingIcon = { Icon(Icons.Default.WbSunny, contentDescription = null, modifier = Modifier.size(14.dp)) }
                )
            }

            // Main List or Empty State
            if (campsites.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.size(68.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.SearchOff,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(34.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "No campsites in current radius filter",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Expand your radius or search directly on Google Maps",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = {
                                    viewModel.setMaxDistanceMiles(null)
                                    viewModel.setQuery("")
                                }
                            ) {
                                Text("Expand Radius")
                            }

                            OutlinedButton(
                                onClick = {
                                    GoogleMapsHelper.searchNearbyCampingOnGoogleMaps(
                                        context,
                                        userCoords.first,
                                        userCoords.second
                                    )
                                }
                            ) {
                                Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Search Google Maps")
                            }
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
                        val distance = viewModel.getDistanceToSiteMiles(site)
                        CampsiteCard(
                            campsite = site,
                            distanceMiles = distance,
                            onClick = { viewModel.navigateTo(ScreenDestination.Detail(site.id)) },
                            onToggleBookmark = { viewModel.toggleBookmark(site) },
                            onOpenGoogleMaps = {
                                GoogleMapsHelper.openCampsiteInGoogleMaps(
                                    context = context,
                                    latitude = site.latitude,
                                    longitude = site.longitude,
                                    campsiteName = site.name
                                )
                            }
                        )
                    }
                }
            }
        }
    }
}
