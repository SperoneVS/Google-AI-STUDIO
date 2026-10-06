package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Campsite
import com.example.data.repository.CampsiteDetailFactory
import com.example.ui.components.CampsiteDetailedInfoComponent
import com.example.ui.components.EnergySectionCard
import com.example.ui.components.SleepSectionCard
import com.example.ui.components.WaterSectionCard
import com.example.ui.viewmodel.CampsiteViewModel
import com.example.ui.viewmodel.ScreenDestination
import com.example.util.GoogleMapsHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    campsiteId: String,
    viewModel: CampsiteViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateBack()
    }

    val context = LocalContext.current
    val campsites by viewModel.filteredCampsites.collectAsState()
    val campsite = campsites.find { it.id == campsiteId }

    if (campsite == null) {
        Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Campsite not found", fontSize = 16.sp)
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = { viewModel.navigateBack() }) {
                    Text("Go Back")
                }
            }
        }
        return
    }

    val distance = viewModel.getDistanceToSiteMiles(campsite)
    val detailedInfo = remember(campsite) { CampsiteDetailFactory.createDetailedInfo(campsite) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(campsite.name, maxLines = 1) },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("detail_back_btn")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleBookmark(campsite) },
                        modifier = Modifier.testTag("detail_bookmark_btn")
                    ) {
                        Icon(
                            imageVector = if (campsite.isBookmarked) Icons.Filled.Bookmark else Icons.Outlined.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (campsite.isBookmarked) Color(0xFFFFD54F) else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    if (campsite.isUserCreated) {
                        IconButton(
                            onClick = { viewModel.deleteCustomCampsite(campsite.id) },
                            modifier = Modifier.testTag("detail_delete_btn")
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
        ) {
            // Scenic Banner
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFF14382A),
                                Color(0xFF26533F)
                            )
                        )
                    )
                    .padding(18.dp)
            ) {
                Column(modifier = Modifier.align(Alignment.BottomStart)) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color.Black.copy(alpha = 0.4f)
                    ) {
                        Text(
                            text = "${campsite.terrainType} • ${campsite.region}",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = campsite.name,
                        color = Color.White,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${campsite.stateOrCountry} • $distance mi away",
                        color = Color.White.copy(alpha = 0.9f),
                        fontSize = 13.sp
                    )
                }

                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Text(
                        text = campsite.feePerNight,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    )
                }
            }

            // Action Buttons Strip (Google Maps & Pack checklist)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = {
                        GoogleMapsHelper.navigateWithGoogleMaps(
                            context = context,
                            latitude = campsite.latitude,
                            longitude = campsite.longitude
                        )
                    },
                    modifier = Modifier
                        .weight(1.2f)
                        .testTag("directions_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Google Maps Route", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = {
                        GoogleMapsHelper.openCampsiteInGoogleMaps(
                            context = context,
                            latitude = campsite.latitude,
                            longitude = campsite.longitude,
                            campsiteName = campsite.name
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("view_gmaps_btn"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFF0277BD))
                ) {
                    Icon(Icons.Default.Map, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color(0xFF0277BD))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Map View", fontSize = 12.sp)
                }

                OutlinedButton(
                    onClick = { viewModel.navigateTo(ScreenDestination.GearChecklist) },
                    modifier = Modifier
                        .weight(0.9f)
                        .testTag("pack_gear_btn"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.ChecklistRtl, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Pack", fontSize = 12.sp)
                }
            }

            // Description & Insider Tip
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Overview & Atmosphere",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = campsite.description,
                        fontSize = 13.sp,
                        lineHeight = 19.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.45f)
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "Camp Insider Tip",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                                Text(
                                    text = campsite.insiderTips,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onTertiaryContainer
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Verified Field Photos & Amenities Directory Component
            CampsiteDetailedInfoComponent(
                detailedInfo = detailedInfo,
                onNavigateGoogleMaps = {
                    GoogleMapsHelper.navigateWithGoogleMaps(
                        context = context,
                        latitude = campsite.latitude,
                        longitude = campsite.longitude
                    )
                },
                onOpenGoogleMaps = {
                    GoogleMapsHelper.openCampsiteInGoogleMaps(
                        context = context,
                        latitude = campsite.latitude,
                        longitude = campsite.longitude,
                        campsiteName = campsite.name
                    )
                },
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // THE 3 PILLARS:
            // 1. Sleeping Section
            Text(
                text = "PILLAR 1: SLEEPING & REST",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            SleepSectionCard(
                sleep = campsite.sleep,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Water Section
            Text(
                text = "PILLAR 2: WATER & HYDRATION",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF0288D1),
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            WaterSectionCard(
                water = campsite.water,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 3. Energy Section
            Text(
                text = "PILLAR 3: POWER & ENERGY",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFF57C00),
                modifier = Modifier.padding(horizontal = 20.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            EnergySectionCard(
                energy = campsite.energy,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Connectivity & Coordinates Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Technical Location & Telemetry",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "GPS Coordinates",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${campsite.latitude}, ${campsite.longitude}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Cellular Coverage",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "${campsite.cellReceptionBars} / 5 Bars ",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium
                            )
                            Icon(
                                imageVector = Icons.Default.SignalCellularAlt,
                                contentDescription = null,
                                tint = if (campsite.cellReceptionBars > 2) Color(0xFF2E7D32) else Color(0xFFD32F2F),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}
