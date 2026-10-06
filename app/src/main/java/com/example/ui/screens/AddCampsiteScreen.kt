package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.*
import com.example.ui.viewmodel.CampsiteViewModel
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddCampsiteScreen(
    viewModel: CampsiteViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateBack()
    }

    var name by remember { mutableStateOf("") }
    var region by remember { mutableStateOf("") }
    var stateOrCountry by remember { mutableStateOf("USA") }
    var latText by remember { mutableStateOf("37.865") }
    var lonText by remember { mutableStateOf("-119.538") }
    var elevationText by remember { mutableStateOf("4500") }
    var feeText by remember { mutableStateOf("Free / Dispersed") }
    var terrainType by remember { mutableStateOf("Alpine Forest") }

    // Sleep
    var selectedSleepType by remember { mutableStateOf(SleepType.TENT) }
    var selectedGroundType by remember { mutableStateOf(GroundType.PINE_NEEDLES) }
    var hammockFriendly by remember { mutableStateOf(true) }

    // Water
    var selectedWaterType by remember { mutableStateOf(WaterSourceType.NATURAL_SPRING) }
    var waterDistanceText by remember { mutableStateOf("25") }
    var hasHotShowers by remember { mutableStateOf(false) }
    var hasDishwashingSink by remember { mutableStateOf(false) }

    // Energy
    var selectedEnergyType by remember { mutableStateOf(EnergySourceType.SOLAR_CLEARING) }
    var solarExposure by remember { mutableStateOf(8f) }
    var campfireRing by remember { mutableStateOf(true) }
    var firewoodPurchasable by remember { mutableStateOf(false) }

    var description by remember { mutableStateOf("") }
    var insiderTips by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Log New Campsite", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("add_spot_back_btn")
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Record a secret pitch or campsite with Sleep, Water, and Energy specs so campers know what gear to bring.",
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            errorMessage?.let { error ->
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.errorContainer
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(12.dp),
                        fontSize = 13.sp
                    )
                }
            }

            // Section 1: Identity & Location
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Basic Info & Location", fontWeight = FontWeight.Bold, fontSize = 15.sp)

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Campsite Name *") },
                        modifier = Modifier.fillMaxWidth().testTag("add_name_field")
                    )

                    OutlinedTextField(
                        value = region,
                        onValueChange = { region = it },
                        label = { Text("Region / National Forest / Park *") },
                        modifier = Modifier.fillMaxWidth().testTag("add_region_field")
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = latText,
                            onValueChange = { latText = it },
                            label = { Text("Latitude") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = lonText,
                            onValueChange = { lonText = it },
                            label = { Text("Longitude") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = elevationText,
                            onValueChange = { elevationText = it },
                            label = { Text("Elevation (ft)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = feeText,
                            onValueChange = { feeText = it },
                            label = { Text("Nightly Fee") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Section 2: Sleeping Pillar
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Bed, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Pillar 1: Sleeping Comfort", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    Text("Stay / Pitch Type:", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        SleepType.values().take(3).forEach { st ->
                            FilterChip(
                                selected = selectedSleepType == st,
                                onClick = { selectedSleepType = st },
                                label = { Text(st.label.take(10), fontSize = 11.sp) }
                            )
                        }
                    }

                    Text("Ground Pitch Texture:", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        GroundType.values().take(3).forEach { gt ->
                            FilterChip(
                                selected = selectedGroundType == gt,
                                onClick = { selectedGroundType = gt },
                                label = { Text(gt.label.take(12), fontSize = 11.sp) }
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Hammock Friendly Trees", fontSize = 14.sp)
                        Switch(
                            checked = hammockFriendly,
                            onCheckedChange = { hammockFriendly = it },
                            modifier = Modifier.testTag("add_hammock_switch")
                        )
                    }
                }
            }

            // Section 3: Water Pillar
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.WaterDrop, contentDescription = null, tint = Color(0xFF0288D1))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Pillar 2: Water Access", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    Text("Water Source:", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        WaterSourceType.values().forEach { wt ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                RadioButton(
                                    selected = selectedWaterType == wt,
                                    onClick = { selectedWaterType = wt }
                                )
                                Text(wt.label, fontSize = 13.sp)
                            }
                        }
                    }

                    OutlinedTextField(
                        value = waterDistanceText,
                        onValueChange = { waterDistanceText = it },
                        label = { Text("Distance to Source (Meters)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Hot Showers Available", fontSize = 14.sp)
                        Switch(
                            checked = hasHotShowers,
                            onCheckedChange = { hasHotShowers = it }
                        )
                    }
                }
            }

            // Section 4: Energy Pillar
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Bolt, contentDescription = null, tint = Color(0xFFF57C00))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Pillar 3: Energy & Power", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    }

                    Text("Electrical Hookup / Utility:", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        EnergySourceType.values().take(3).forEach { et ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                RadioButton(
                                    selected = selectedEnergyType == et,
                                    onClick = { selectedEnergyType = et }
                                )
                                Text(et.label, fontSize = 13.sp)
                            }
                        }
                    }

                    Text("Solar Viability Index (${solarExposure.toInt()} / 10):", fontSize = 13.sp)
                    Slider(
                        value = solarExposure,
                        onValueChange = { solarExposure = it },
                        valueRange = 1f..10f,
                        steps = 8
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Campfire Ring Allowed", fontSize = 14.sp)
                        Switch(
                            checked = campfireRing,
                            onCheckedChange = { campfireRing = it }
                        )
                    }
                }
            }

            // Description and Tips
            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("Notes & Insider Recommendations", fontWeight = FontWeight.Bold, fontSize = 15.sp)

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Campsite Atmosphere & Scenery") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )

                    OutlinedTextField(
                        value = insiderTips,
                        onValueChange = { insiderTips = it },
                        label = { Text("Camper Insider Tips (e.g. Best sunrise pitch, water filter advice)") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 2
                    )
                }
            }

            // Submit Button
            Button(
                onClick = {
                    if (name.isBlank() || region.isBlank()) {
                        errorMessage = "Please enter both Campsite Name and Region."
                        return@Button
                    }
                    val lat = latText.toDoubleOrNull() ?: 37.8
                    val lon = lonText.toDoubleOrNull() ?: -119.5
                    val elev = elevationText.toIntOrNull() ?: 4000
                    val dist = waterDistanceText.toIntOrNull() ?: 20

                    val newCampsite = Campsite(
                        id = "user_camp_" + UUID.randomUUID().toString().take(8),
                        name = name.trim(),
                        region = region.trim(),
                        stateOrCountry = stateOrCountry.trim(),
                        latitude = lat,
                        longitude = lon,
                        feePerNight = if (feeText.isBlank()) "Free" else feeText.trim(),
                        rating = 5.0,
                        reviewCount = 1,
                        sleep = SleepDetails(
                            type = selectedSleepType,
                            groundType = selectedGroundType,
                            maxCapacity = 6,
                            hammockFriendly = hammockFriendly,
                            shadeRating = 4,
                            quietHours = "10:00 PM - 7:00 AM",
                            elevationFt = elev
                        ),
                        water = WaterDetails(
                            sourceType = selectedWaterType,
                            distanceToSourceMeters = dist,
                            hasHotShowers = hasHotShowers,
                            hasColdShowers = false,
                            hasDishwashingSink = hasDishwashingSink,
                            flowReliability = "Camper verified"
                        ),
                        energy = EnergyDetails(
                            sourceType = selectedEnergyType,
                            solarExposureIndex = solarExposure.toInt(),
                            generatorAllowed = false,
                            generatorHours = "Check local wilderness rules",
                            campfireRing = campfireRing,
                            firewoodPurchasable = firewoodPurchasable,
                            hasEvCharging = false
                        ),
                        cellReceptionBars = 2,
                        terrainType = terrainType,
                        description = if (description.isBlank()) "Wilderness campsite discovered and logged by camper." else description.trim(),
                        insiderTips = if (insiderTips.isBlank()) "Pack out all trash and respect quiet hours." else insiderTips.trim(),
                        isUserCreated = true
                    )

                    viewModel.addCustomCampsite(newCampsite)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .testTag("save_campsite_btn"),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Check, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Save Campsite to Database", fontWeight = FontWeight.Bold)
            }
        }
    }
}
