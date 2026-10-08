package com.example.ui.screens

import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.CampsiteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: CampsiteViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var emailInput by remember { mutableStateOf("") }
    var vehicleModel by remember { mutableStateOf("") }
    var vehicleHeight by remember { mutableStateOf("") }
    var vehicleWeight by remember { mutableStateOf("") }
    var licensePlate by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isSubmitting by remember { mutableStateOf(false) }

    fun submitEmail(emailToUse: String) {
        val trimmed = emailToUse.trim()
        if (trimmed.isBlank()) {
            errorMessage = "Please enter your email address."
            return
        }
        if (!trimmed.contains("@") || !trimmed.contains(".")) {
            errorMessage = "Please enter a valid email address (e.g. camper@example.com)."
            return
        }

        errorMessage = null
        isSubmitting = true
        focusManager.clearFocus()

        val result = viewModel.signInWithEmail(
            email = trimmed,
            vehicleHeight = vehicleHeight,
            vehicleWeight = vehicleWeight,
            vehicleModel = vehicleModel,
            licensePlate = licensePlate
        )
        isSubmitting = false

        if (result.isSuccess) {
            val user = result.getOrNull()
            Toast.makeText(
                context,
                "Welcome to CampHaven, ${user?.displayName ?: "Camper"}!",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            errorMessage = result.exceptionOrNull()?.message ?: "Unable to sign in. Please try again."
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0C1E14),
                        Color(0xFF132F20),
                        Color(0xFF1B3B2B)
                    )
                )
            )
            .imePadding()
            .safeDrawingPadding()
            .testTag("auth_screen")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Wilderness Brand Emblem
            Surface(
                shape = CircleShape,
                color = Color(0xFF2E6347),
                modifier = Modifier.size(72.dp),
                shadowElevation = 8.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Terrain,
                        contentDescription = "CampHaven Emblem",
                        tint = Color(0xFF8CE8B5),
                        modifier = Modifier.size(42.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "CampHaven",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
                letterSpacing = 0.5.sp
            )

            Text(
                text = "Sleep • Water • Energy Essentials",
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFA5D6A7)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Sign in to locate verified campsites, bridge clearances, and GPS routes.",
                fontSize = 12.sp,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Streamlined Email & Vehicle Questions Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Header inside card
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(38.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.Email,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = "Camper Registration",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Enter email and vehicle specifications",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    // Email Input Field
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = {
                            emailInput = it
                            errorMessage = null
                        },
                        label = { Text("Email Address *") },
                        placeholder = { Text("you@example.com") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.AlternateEmail,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                        },
                        trailingIcon = {
                            if (emailInput.isNotEmpty()) {
                                IconButton(onClick = {
                                    emailInput = ""
                                    errorMessage = null
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear email",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("email_input_field")
                    )

                    // Vehicle Specifications Section Header
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsCar,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Vehicle Specifications",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }

                    Text(
                        text = "Used to check low bridge clearance, pitch weight limits, and ranger camp passes.",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Vehicle Model
                    OutlinedTextField(
                        value = vehicleModel,
                        onValueChange = { vehicleModel = it },
                        label = { Text("Vehicle Model") },
                        placeholder = { Text("e.g. Ford Transit, Sprinter, 4Runner") },
                        singleLine = true,
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.RvHookup,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("vehicle_model_input")
                    )

                    // Height & Weight Side by Side
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = vehicleHeight,
                            onValueChange = { vehicleHeight = it },
                            label = { Text("Vehicle Height") },
                            placeholder = { Text("e.g. 9ft 6in / 2.9m") },
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Height,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("vehicle_height_input")
                        )

                        OutlinedTextField(
                            value = vehicleWeight,
                            onValueChange = { vehicleWeight = it },
                            label = { Text("Vehicle Weight") },
                            placeholder = { Text("e.g. 7,500 lbs") },
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Scale,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("vehicle_weight_input")
                        )
                    }

                    // License Plate
                    OutlinedTextField(
                        value = licensePlate,
                        onValueChange = { licensePlate = it },
                        label = { Text("License Plate") },
                        placeholder = { Text("e.g. 7XYZ123") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { submitEmail(emailInput) }),
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Badge,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("license_plate_input")
                    )

                    // Error Message banner
                    errorMessage?.let { errorText ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.errorContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ErrorOutline,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = errorText,
                                    color = MaterialTheme.colorScheme.onErrorContainer,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }
                    }

                    // Continue Button
                    Button(
                        onClick = { submitEmail(emailInput) },
                        enabled = !isSubmitting,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .testTag("continue_email_btn")
                    ) {
                        if (isSubmitting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text("Registering Camper...", fontSize = 15.sp)
                        } else {
                            Text(
                                text = "Continue to Campsites",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Fast Demo / Guest Option
                    TextButton(
                        onClick = {
                            emailInput = "camper@camphaven.org"
                            vehicleModel = "Ford Transit Custom"
                            vehicleHeight = "8 ft 4 in"
                            vehicleWeight = "6,200 lbs"
                            licensePlate = "CAMP-777"
                            submitEmail("camper@camphaven.org")
                        },
                        modifier = Modifier.testTag("guest_explorer_btn")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Explore,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Fill Sample Camper Rig & Explore",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Protected by Google AI Studio Security\nBy continuing, you agree to Wilderness Dark-Sky & Camp Etiquette.",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.65f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
