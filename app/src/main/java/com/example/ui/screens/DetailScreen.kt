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

    val currentUser by viewModel.currentUser.collectAsState()
    val reviews by viewModel.getReviewsForCampsite(campsite.id).collectAsState(initial = emptyList())

    var showReviewDialog by remember { mutableStateOf(false) }
    var ratingStars by remember { mutableStateOf(5) }
    var isWaterAvailable by remember { mutableStateOf(campsite.water.sourceType != com.example.data.model.WaterSourceType.NO_WATER) }
    var waterStatusLabel by remember { mutableStateOf(campsite.water.sourceType.label) }
    var isEnergyAvailable by remember { mutableStateOf(campsite.energy.sourceType.hasGridPower) }
    var energyStatusLabel by remember { mutableStateOf(campsite.energy.sourceType.label) }
    var reviewNotes by remember { mutableStateOf("") }

    var showLimitsDialog by remember { mutableStateOf(false) }
    var testRigHeightText by remember { mutableStateOf(currentUser?.vehicleHeight ?: "") }
    var testRigWeightText by remember { mutableStateOf(currentUser?.vehicleWeight ?: "") }
    var testGroupSize by remember { mutableStateOf(2) }
    var testStayNights by remember { mutableStateOf(2) }
    var customEvaluationResult by remember { mutableStateOf<com.example.ui.viewmodel.CampsiteLimitsEvaluation?>(null) }

    val defaultEvaluation = remember(campsite, currentUser) {
        viewModel.evaluateCampsiteLimits(campsite)
    }

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

            // ----------------------------------------------------
            // SECTION: CAMPSITE LIMITS & RIG FIT CHECKER
            // ----------------------------------------------------
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("campsite_limits_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.DirectionsCar,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Campsite Limits & Rig Clearance",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        TextButton(
                            onClick = {
                                testRigHeightText = currentUser?.vehicleHeight ?: ""
                                testRigWeightText = currentUser?.vehicleWeight ?: ""
                                testGroupSize = 2
                                testStayNights = 2
                                customEvaluationResult = null
                                showLimitsDialog = true
                            },
                            modifier = Modifier.testTag("check_limits_btn")
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Test Rig Fit", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Text(
                        text = "Official terrain and pitch restrictions for vehicles, rigs, stay duration, and party size.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // Limits Grid
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Max Height", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${campsite.limits.maxVehicleHeightFt} ft", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("Clearance limit", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Max Weight", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${campsite.limits.maxVehicleWeightLbs} lbs", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("Pad rating", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }

                        Surface(
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("Max Stay", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${campsite.limits.maxStayNights} Days", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                Text("Limit nights", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    // Secondary limits row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Max Group: ${campsite.limits.maxPeople} campers | Pad Length: ${campsite.limits.maxVehicleLengthFt} ft",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "Quiet: ${campsite.limits.quietHours}",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                    // Live rig comparison evaluation banner
                    val evalToDisplay = customEvaluationResult ?: defaultEvaluation
                    val bannerColor = if (evalToDisplay.isAllCleared) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
                    val contentColor = if (evalToDisplay.isAllCleared) Color(0xFF2E7D32) else Color(0xFFE65100)

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = bannerColor,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (evalToDisplay.isAllCleared) Icons.Default.CheckCircle else Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = contentColor,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = evalToDisplay.summaryVerdict,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = contentColor
                                )
                            }

                            currentUser?.vehicleModel?.let { model ->
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Your Rig: $model ${currentUser?.licensePlate?.let { "[$it]" } ?: ""}",
                                    fontSize = 11.sp,
                                    color = contentColor.copy(alpha = 0.9f)
                                )
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            evalToDisplay.items.forEach { item ->
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${item.title}:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = item.rigSpec,
                                            fontSize = 11.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Icon(
                                            imageVector = if (item.isCompliant) Icons.Default.Check else Icons.Default.Close,
                                            contentDescription = null,
                                            tint = if (item.isCompliant) Color(0xFF2E7D32) else Color(0xFFD32F2F),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // ----------------------------------------------------
            // SECTION: CAMPER RATINGS & POST-VISIT AMENITIES VERIFICATION
            // ----------------------------------------------------
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .testTag("campsite_reviews_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Star,
                                    contentDescription = null,
                                    tint = Color(0xFFFFB300),
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Post-Visit Ratings & Reports",
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Text(
                                text = "Verified reports on water and energy hookups",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = { showReviewDialog = true },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("rate_campsite_btn")
                        ) {
                            Icon(Icons.Default.RateReview, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Rate Visit", fontSize = 12.sp)
                        }
                    }

                    if (reviews.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(
                                modifier = Modifier.padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "No field visit reviews logged yet.",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Have you camped here? Add your rating and confirm if water and energy were available!",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 15.sp
                                )
                            }
                        }
                    } else {
                        reviews.forEach { rev ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = rev.camperName,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )

                                        // Stars display
                                        Row {
                                            repeat(rev.ratingStars) {
                                                Icon(
                                                    imageVector = Icons.Default.Star,
                                                    contentDescription = null,
                                                    tint = Color(0xFFFFB300),
                                                    modifier = Modifier.size(14.dp)
                                                )
                                            }
                                        }
                                    }

                                    // Water and energy verification badges
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (rev.isWaterAvailable) Color(0xFFE1F5FE) else Color(0xFFFFEBEE)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = if (rev.isWaterAvailable) Icons.Default.WaterDrop else Icons.Default.WaterDrop,
                                                    contentDescription = null,
                                                    tint = if (rev.isWaterAvailable) Color(0xFF0288D1) else Color(0xFFC62828),
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = if (rev.isWaterAvailable) "Water: Available (${rev.waterStatusLabel})" else "Water: Dry / None",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = if (rev.isWaterAvailable) Color(0xFF0288D1) else Color(0xFFC62828)
                                                )
                                            }
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(6.dp),
                                            color = if (rev.isEnergyAvailable) Color(0xFFFFF8E1) else Color(0xFFECEFF1)
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Icon(
                                                    imageVector = if (rev.isEnergyAvailable) Icons.Default.Bolt else Icons.Default.PowerOff,
                                                    contentDescription = null,
                                                    tint = if (rev.isEnergyAvailable) Color(0xFFF57F17) else Color(0xFF546E7A),
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = if (rev.isEnergyAvailable) "Energy: Available (${rev.energyStatusLabel})" else "Energy: Off-Grid",
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.SemiBold,
                                                    color = if (rev.isEnergyAvailable) Color(0xFFF57F17) else Color(0xFF546E7A)
                                                )
                                            }
                                        }
                                    }

                                    if (rev.notes.isNotBlank()) {
                                        Text(
                                            text = "\"${rev.notes}\"",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

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

    // ----------------------------------------------------
    // DIALOG: POST-VISIT RATING & WATER/ENERGY QUESTIONS
    // ----------------------------------------------------
    if (showReviewDialog) {
        AlertDialog(
            onDismissRequest = { showReviewDialog = false },
            title = {
                Text(
                    text = "Rate Visit to ${campsite.name}",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "1. Overall Rating after your visit:",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )

                    // Star Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        (1..5).forEach { star ->
                            IconButton(
                                onClick = { ratingStars = star },
                                modifier = Modifier.size(40.dp)
                            ) {
                                Icon(
                                    imageVector = if (star <= ratingStars) Icons.Default.Star else Icons.Outlined.StarBorder,
                                    contentDescription = "$star stars",
                                    tint = if (star <= ratingStars) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }

                    HorizontalDivider()

                    // QUESTION 1: WATER AVAILABILITY
                    Text(
                        text = "2. Was clean water available during your stay?",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isWaterAvailable) "Water WAS Available" else "Water UNAVAILABLE / Dry",
                            fontSize = 12.sp,
                            color = if (isWaterAvailable) Color(0xFF0288D1) else Color(0xFFC62828),
                            fontWeight = FontWeight.Bold
                        )
                        Switch(
                            checked = isWaterAvailable,
                            onCheckedChange = { isWaterAvailable = it }
                        )
                    }

                    if (isWaterAvailable) {
                        Text("Water Source Status:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("Potable Tap", "Alpine Spring", "River (Filter)", "Bring Own").forEach { label ->
                                FilterChip(
                                    selected = waterStatusLabel.contains(label, ignoreCase = true),
                                    onClick = { waterStatusLabel = label },
                                    label = { Text(label, fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    HorizontalDivider()

                    // QUESTION 2: ENERGY AVAILABILITY
                    Text(
                        text = "3. Was electricity / energy hookup available?",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isEnergyAvailable) "Energy Hookup WAS Available" else "Off-Grid / No Electricity",
                            fontSize = 12.sp,
                            color = if (isEnergyAvailable) Color(0xFFF57F17) else Color(0xFF546E7A),
                            fontWeight = FontWeight.Bold
                        )
                        Switch(
                            checked = isEnergyAvailable,
                            onCheckedChange = { isEnergyAvailable = it }
                        )
                    }

                    if (isEnergyAvailable) {
                        Text("Hookup Type Status:", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("30A/50A Shore", "120V Outlet", "Solar Prime", "USB Station").forEach { label ->
                                FilterChip(
                                    selected = energyStatusLabel.contains(label, ignoreCase = true),
                                    onClick = { energyStatusLabel = label },
                                    label = { Text(label, fontSize = 11.sp) }
                                )
                            }
                        }
                    }

                    HorizontalDivider()

                    // Field notes
                    OutlinedTextField(
                        value = reviewNotes,
                        onValueChange = { reviewNotes = it },
                        label = { Text("Camper Field Notes & Tips") },
                        placeholder = { Text("e.g. Bring extra water filter; shady spot on site 4") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.submitCampsiteReview(
                            campsiteId = campsite.id,
                            ratingStars = ratingStars,
                            isWaterAvailable = isWaterAvailable,
                            waterStatusLabel = waterStatusLabel,
                            isEnergyAvailable = isEnergyAvailable,
                            energyStatusLabel = energyStatusLabel,
                            notes = reviewNotes
                        )
                        showReviewDialog = false
                    },
                    modifier = Modifier.testTag("submit_review_dialog_btn")
                ) {
                    Text("Submit Review")
                }
            },
            dismissButton = {
                TextButton(onClick = { showReviewDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // ----------------------------------------------------
    // DIALOG: TEST RIG LIMITS & TRIP RESTRICTIONS
    // ----------------------------------------------------
    if (showLimitsDialog) {
        AlertDialog(
            onDismissRequest = { showLimitsDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Tune, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Test Rig Limits Compatibility", fontSize = 17.sp, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Enter your vehicle specs to check clearance and weight limits at ${campsite.name}.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = testRigHeightText,
                        onValueChange = { testRigHeightText = it },
                        label = { Text("Vehicle Height") },
                        placeholder = { Text("e.g. 8 ft 4 in or 9.0") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("test_rig_height_input")
                    )

                    OutlinedTextField(
                        value = testRigWeightText,
                        onValueChange = { testRigWeightText = it },
                        label = { Text("Vehicle Weight") },
                        placeholder = { Text("e.g. 6,500 lbs") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("test_rig_weight_input")
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = testStayNights.toString(),
                            onValueChange = { testStayNights = it.toIntOrNull() ?: 1 },
                            label = { Text("Nights Stay") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = testGroupSize.toString(),
                            onValueChange = { testGroupSize = it.toIntOrNull() ?: 1 },
                            label = { Text("Campers") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Text(
                                text = "Site Limits: Height max ${campsite.limits.maxVehicleHeightFt} ft | Weight max ${campsite.limits.maxVehicleWeightLbs} lbs | Stay max ${campsite.limits.maxStayNights} nights",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsedHeight = testRigHeightText.replace("ft", "").replace("'", ".").filter { it.isDigit() || it == '.' }.toDoubleOrNull()
                        val parsedWeight = testRigWeightText.filter { it.isDigit() }.toIntOrNull()

                        val result = viewModel.evaluateCampsiteLimits(
                            campsite = campsite,
                            customHeightFt = parsedHeight,
                            customWeightLbs = parsedWeight,
                            groupSize = testGroupSize,
                            stayNights = testStayNights
                        )
                        customEvaluationResult = result

                        // If user has no vehicle info saved yet or updated it, save to profile
                        viewModel.updateVehicleInfo(
                            vehicleHeight = testRigHeightText.ifBlank { null },
                            vehicleWeight = testRigWeightText.ifBlank { null },
                            vehicleModel = currentUser?.vehicleModel,
                            licensePlate = currentUser?.licensePlate
                        )

                        showLimitsDialog = false
                    },
                    modifier = Modifier.testTag("apply_limits_test_btn")
                ) {
                    Text("Verify Rig")
                }
            },
            dismissButton = {
                TextButton(onClick = { showLimitsDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
