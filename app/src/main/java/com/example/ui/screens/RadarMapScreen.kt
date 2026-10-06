package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Campsite
import com.example.ui.viewmodel.ActivePillarFilter
import com.example.ui.viewmodel.CampsiteViewModel
import com.example.ui.viewmodel.ScreenDestination
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadarMapScreen(
    viewModel: CampsiteViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateBack()
    }

    val context = androidx.compose.ui.platform.LocalContext.current
    val campsites by viewModel.filteredCampsites.collectAsState()
    val userCoords by viewModel.userCoordinates.collectAsState()
    var selectedPinCampsite by remember { mutableStateOf<Campsite?>(null) }
    var mapFilterMode by remember { mutableStateOf(ActivePillarFilter.ALL) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Wilderness Radar Map", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text("Topographic Campsite Range", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("radar_back_btn")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            com.example.util.GoogleMapsHelper.searchNearbyCampingOnGoogleMaps(
                                context = context,
                                latitude = userCoords.first,
                                longitude = userCoords.second
                            )
                        },
                        modifier = Modifier.testTag("radar_gmaps_search_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Map,
                            contentDescription = "Search on Google Maps",
                            tint = Color(0xFF0288D1)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Filter Row
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = mapFilterMode == ActivePillarFilter.ALL,
                        onClick = { mapFilterMode = ActivePillarFilter.ALL },
                        label = { Text("All Pins") }
                    )
                    FilterChip(
                        selected = mapFilterMode == ActivePillarFilter.SLEEP,
                        onClick = { mapFilterMode = ActivePillarFilter.SLEEP },
                        label = { Text("🛏️ Sleep") }
                    )
                    FilterChip(
                        selected = mapFilterMode == ActivePillarFilter.WATER,
                        onClick = { mapFilterMode = ActivePillarFilter.WATER },
                        label = { Text("💧 Water") }
                    )
                    FilterChip(
                        selected = mapFilterMode == ActivePillarFilter.ENERGY,
                        onClick = { mapFilterMode = ActivePillarFilter.ENERGY },
                        label = { Text("⚡ Energy") }
                    )
                }

                // Interactive Radar Canvas
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(Color(0xFF131D18))
                ) {
                    // Coordinates projection state to catch taps
                    val pinScreenPositions = remember { mutableStateMapOf<String, Offset>() }

                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(campsites, mapFilterMode) {
                                detectTapGestures { tapOffset ->
                                    // Check if clicked close to any pin
                                    val hit = pinScreenPositions.entries.find { (_, pos) ->
                                        val dist = (pos - tapOffset).getDistance()
                                        dist < 40f
                                    }
                                    if (hit != null) {
                                        selectedPinCampsite = campsites.find { it.id == hit.key }
                                    } else {
                                        selectedPinCampsite = null
                                    }
                                }
                            }
                    ) {
                        val centerX = size.width / 2f
                        val centerY = size.height / 2f
                        val maxRadius = min(size.width, size.height) * 0.44f

                        // Topo Rings
                        val ringCount = 4
                        for (i in 1..ringCount) {
                            val r = (maxRadius / ringCount) * i
                            drawCircle(
                                color = Color(0xFF2E453A),
                                radius = r,
                                center = Offset(centerX, centerY),
                                style = Stroke(
                                    width = 1.2f,
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 6f), 0f)
                                )
                            )
                        }

                        // Crosshairs (North, South, East, West axes)
                        drawLine(
                            color = Color(0xFF263D33),
                            start = Offset(centerX, centerY - maxRadius - 20f),
                            end = Offset(centerX, centerY + maxRadius + 20f),
                            strokeWidth = 1f
                        )
                        drawLine(
                            color = Color(0xFF263D33),
                            start = Offset(centerX - maxRadius - 20f, centerY),
                            end = Offset(centerX + maxRadius + 20f, centerY),
                            strokeWidth = 1f
                        )

                        // Center User Marker (Pulse dot)
                        drawCircle(
                            color = Color(0x404CAF50),
                            radius = 20f,
                            center = Offset(centerX, centerY)
                        )
                        drawCircle(
                            color = Color(0xFF4CAF50),
                            radius = 7f,
                            center = Offset(centerX, centerY)
                        )

                        // Project campsites relative to user
                        // Range: ~600 miles max map scale
                        val maxScaleMiles = 600.0

                        pinScreenPositions.clear()

                        campsites.forEach { site ->
                            // Check map filter
                            val matchesFilter = when (mapFilterMode) {
                                ActivePillarFilter.ALL -> true
                                ActivePillarFilter.SLEEP -> site.sleep.shadeRating >= 4 || site.sleep.hammockFriendly
                                ActivePillarFilter.WATER -> site.water.sourceType.isPotable
                                ActivePillarFilter.ENERGY -> site.energy.sourceType.hasGridPower || site.energy.solarExposureIndex >= 8
                                else -> true
                            }

                            if (matchesFilter) {
                                val latDiff = site.latitude - userCoords.first
                                val lonDiff = site.longitude - userCoords.second
                                val milesY = latDiff * 69.0 // ~69 miles per degree lat
                                val milesX = lonDiff * 54.6 // approx for 37 deg N

                                val normX = (milesX / maxScaleMiles).coerceIn(-1.0, 1.0)
                                val normY = (-milesY / maxScaleMiles).coerceIn(-1.0, 1.0) // Y inverted on screen

                                val posX = centerX + (normX * maxRadius).toFloat()
                                val posY = centerY + (normY * maxRadius).toFloat()

                                pinScreenPositions[site.id] = Offset(posX, posY)

                                val isSelected = selectedPinCampsite?.id == site.id

                                // Determine pin color by predominant attribute
                                val pinColor = when {
                                    site.water.sourceType.isPotable -> Color(0xFF29B6F6)
                                    site.energy.sourceType.hasGridPower -> Color(0xFFFFB300)
                                    else -> Color(0xFFAB47BC)
                                }

                                if (isSelected) {
                                    drawCircle(
                                        color = Color.White,
                                        radius = 16f,
                                        center = Offset(posX, posY),
                                        style = Stroke(width = 2.5f)
                                    )
                                }

                                // Outer pin glow
                                drawCircle(
                                    color = pinColor.copy(alpha = 0.35f),
                                    radius = if (isSelected) 14f else 9f,
                                    center = Offset(posX, posY)
                                )
                                // Inner pin solid
                                drawCircle(
                                    color = pinColor,
                                    radius = if (isSelected) 8f else 5.5f,
                                    center = Offset(posX, posY)
                                )
                            }
                        }
                    }

                    // Legend & Cardinal Marks
                    Text(
                        text = "▲ N (High Sierra / Cascades)",
                        color = Color(0xFF749688),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 10.dp)
                    )

                    // Pin Legend in corner
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomStart)
                            .padding(12.dp),
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.6f)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            LegendRow(color = Color(0xFF29B6F6), label = "Potable Water")
                            Spacer(modifier = Modifier.height(3.dp))
                            LegendRow(color = Color(0xFFFFB300), label = "Power Hookup")
                            Spacer(modifier = Modifier.height(3.dp))
                            LegendRow(color = Color(0xFFAB47BC), label = "Sleep / Wilderness")
                        }
                    }
                }
            }

            // Bottom Selected Campsite Card Sheet
            AnimatedVisibility(
                visible = selectedPinCampsite != null,
                enter = slideInVertically { it },
                exit = slideOutVertically { it },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp)
            ) {
                selectedPinCampsite?.let { site ->
                    val distance = viewModel.getDistanceToSiteMiles(site)
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("radar_preview_card"),
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(8.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = site.name,
                                        fontSize = 17.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${site.region} • $distance mi away",
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                IconButton(onClick = { selectedPinCampsite = null }) {
                                    Icon(Icons.Default.Close, contentDescription = "Close")
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFEDE7F6)
                                ) {
                                    Text(
                                        text = "🛏️ ${site.sleep.type.label}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF5E35B1),
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(6.dp)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFE1F5FE)
                                ) {
                                    Text(
                                        text = "💧 ${site.water.sourceType.label}",
                                        fontSize = 11.sp,
                                        color = Color(0xFF0288D1),
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(6.dp)
                                    )
                                }
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFFFFF3E0)
                                ) {
                                    Text(
                                        text = "⚡ ${site.energy.sourceType.label}",
                                        fontSize = 11.sp,
                                        color = Color(0xFFF57C00),
                                        fontWeight = FontWeight.SemiBold,
                                        modifier = Modifier.padding(6.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Button(
                                onClick = {
                                    viewModel.navigateTo(ScreenDestination.Detail(site.id))
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("radar_view_site_btn"),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("Inspect Sleeping, Water & Power Details")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendRow(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = Color.White
        )
    }
}
