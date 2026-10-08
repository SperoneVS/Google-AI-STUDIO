package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AuthMethod
import com.example.ui.viewmodel.CampsiteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: CampsiteViewModel,
    modifier: Modifier = Modifier
) {
    BackHandler {
        viewModel.navigateBack()
    }

    val user by viewModel.currentUser.collectAsState()
    val campsites by viewModel.filteredCampsites.collectAsState()
    val savedCount = campsites.count { it.isBookmarked }
    val userCreatedCount = campsites.count { it.isUserCreated }

    var showEditVehicleDialog by remember { mutableStateOf(false) }
    var editModel by remember(user) { mutableStateOf(user?.vehicleModel ?: "") }
    var editHeight by remember(user) { mutableStateOf(user?.vehicleHeight ?: "") }
    var editWeight by remember(user) { mutableStateOf(user?.vehicleWeight ?: "") }
    var editPlate by remember(user) { mutableStateOf(user?.licensePlate ?: "") }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text("Camper Profile", fontSize = 18.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(
                        onClick = { viewModel.navigateBack() },
                        modifier = Modifier.testTag("profile_back_btn")
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
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Avatar
            Surface(
                shape = CircleShape,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(80.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    val initial = user?.displayName?.firstOrNull()?.uppercase() ?: "C"
                    Text(
                        text = initial,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = user?.displayName ?: "Camper",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                val identifier = user?.phoneNumber ?: user?.email ?: "Verified Explorer"
                Text(
                    text = identifier,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFE8F5E9)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF2E7D32),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = when (user?.authMethod) {
                                AuthMethod.PHONE_OTP -> "Phone Verified (Firebase SMS)"
                                AuthMethod.EMAIL_OTP -> "Email OTP Verified"
                                AuthMethod.GOOGLE -> "Google Authenticated"
                                null -> "Verified Camper"
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }
            }

            // Stats Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$savedCount",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Saved Spots",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "$userCreatedCount",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0288D1)
                        )
                        Text(
                            text = "Logged Spots",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "Level 1",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFF57C00)
                        )
                        Text(
                            text = "Camp Scout",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Authentication Details Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Account & Security", fontWeight = FontWeight.Bold, fontSize = 15.sp)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Sign-In Method", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(
                            text = when (user?.authMethod) {
                                AuthMethod.PHONE_OTP -> "Firebase Phone SMS OTP"
                                AuthMethod.EMAIL_OTP -> "Email OTP"
                                AuthMethod.GOOGLE -> "Google Identity"
                                null -> "CampHaven Member"
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Member Since", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(user?.memberSince ?: "2026", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Google Maps Sync", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Active", fontSize = 13.sp, color = Color(0xFF2E7D32), fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Camper Rig & Vehicle Specifications Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("vehicle_specs_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
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
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Camper Rig Specifications", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        }

                        TextButton(
                            onClick = {
                                editModel = user?.vehicleModel ?: ""
                                editHeight = user?.vehicleHeight ?: ""
                                editWeight = user?.vehicleWeight ?: ""
                                editPlate = user?.licensePlate ?: ""
                                showEditVehicleDialog = true
                            },
                            modifier = Modifier.testTag("edit_rig_btn")
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Edit Specs", fontSize = 12.sp)
                        }
                    }

                    Text(
                        text = "Used to check road clearance, bridge height, and pad weight limits at campsites.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Vehicle Model", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(user?.vehicleModel ?: "Not specified", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Clearance Height", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(user?.vehicleHeight ?: "Not specified", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Vehicle Weight", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(user?.vehicleWeight ?: "Not specified", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("License Plate", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(user?.licensePlate ?: "Not specified", fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Sign Out Button
            Button(
                onClick = {
                    viewModel.signOut()
                    viewModel.navigateBack()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("sign_out_btn"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Icon(Icons.Default.ExitToApp, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Sign Out", fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showEditVehicleDialog) {
        AlertDialog(
            onDismissRequest = { showEditVehicleDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.DirectionsCar, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Edit Rig Specifications", fontSize = 17.sp, fontWeight = FontWeight.Bold)
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
                        text = "Specify your vehicle parameters to verify campsite height and weight restrictions.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    OutlinedTextField(
                        value = editModel,
                        onValueChange = { editModel = it },
                        label = { Text("Vehicle Model") },
                        placeholder = { Text("e.g. Ford Transit Custom") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("edit_model_input")
                    )

                    OutlinedTextField(
                        value = editHeight,
                        onValueChange = { editHeight = it },
                        label = { Text("Vehicle Height") },
                        placeholder = { Text("e.g. 8 ft 4 in") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("edit_height_input")
                    )

                    OutlinedTextField(
                        value = editWeight,
                        onValueChange = { editWeight = it },
                        label = { Text("Vehicle Weight") },
                        placeholder = { Text("e.g. 6,200 lbs") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("edit_weight_input")
                    )

                    OutlinedTextField(
                        value = editPlate,
                        onValueChange = { editPlate = it },
                        label = { Text("License Plate") },
                        placeholder = { Text("e.g. CAMP-777") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().testTag("edit_plate_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.updateVehicleInfo(
                            vehicleHeight = editHeight.ifBlank { null },
                            vehicleWeight = editWeight.ifBlank { null },
                            vehicleModel = editModel.ifBlank { null },
                            licensePlate = editPlate.ifBlank { null }
                        )
                        showEditVehicleDialog = false
                    },
                    modifier = Modifier.testTag("save_rig_specs_btn")
                ) {
                    Text("Save Specs")
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditVehicleDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}
