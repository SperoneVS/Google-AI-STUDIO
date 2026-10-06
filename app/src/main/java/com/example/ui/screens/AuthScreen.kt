package com.example.ui.screens

import android.app.Activity
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.CampsiteViewModel
import kotlinx.coroutines.delay

enum class AuthTab {
    PHONE_OTP,
    GOOGLE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    viewModel: CampsiteViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? Activity

    var selectedTab by remember { mutableStateOf(AuthTab.PHONE_OTP) }

    // Phone OTP state
    var countryCode by remember { mutableStateOf("+1") }
    var phoneNumber by remember { mutableStateOf("") }
    var otpCode by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }
    var isSendingOtp by remember { mutableStateOf(false) }
    var isVerifying by remember { mutableStateOf(false) }
    var resendCountdown by remember { mutableStateOf(60) }
    var statusMessage by remember { mutableStateOf<String?>(null) }
    var isErrorMessage by remember { mutableStateOf(false) }

    // Google Sign-In state
    var isGoogleSigningIn by remember { mutableStateOf(false) }

    // Countdown timer for OTP
    LaunchedEffect(isOtpSent) {
        if (isOtpSent) {
            resendCountdown = 60
            while (resendCountdown > 0) {
                delay(1000L)
                resendCountdown--
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F241A),
                        Color(0xFF1B3B2B),
                        Color(0xFF13231B)
                    )
                )
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            // Brand Emblem
            Surface(
                shape = CircleShape,
                color = Color(0xFF2E6347),
                modifier = Modifier.size(72.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Terrain,
                        contentDescription = "CampHaven",
                        tint = Color(0xFF8CE8B5),
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "CampHaven",
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Text(
                text = "Sleep • Water • Energy Essentials",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFFA5D6A7)
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Sign in to locate campsites in your area and navigate with Google Maps.",
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.8f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Main Auth Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("auth_card"),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    // Auth Method Tabs
                    TabRow(
                        selectedTabIndex = selectedTab.ordinal,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp)),
                        indicator = {},
                        divider = {}
                    ) {
                        Tab(
                            selected = selectedTab == AuthTab.PHONE_OTP,
                            onClick = {
                                selectedTab = AuthTab.PHONE_OTP
                                statusMessage = null
                            },
                            text = {
                                Text(
                                    text = "📱 Phone OTP",
                                    fontWeight = if (selectedTab == AuthTab.PHONE_OTP) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            },
                            selectedContentColor = MaterialTheme.colorScheme.primary,
                            unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Tab(
                            selected = selectedTab == AuthTab.GOOGLE,
                            onClick = {
                                selectedTab = AuthTab.GOOGLE
                                statusMessage = null
                            },
                            text = {
                                Text(
                                    text = "🌐 Google Sign-In",
                                    fontWeight = if (selectedTab == AuthTab.GOOGLE) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 13.sp
                                )
                            },
                            selectedContentColor = MaterialTheme.colorScheme.primary,
                            unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Status / Alert banner
                    statusMessage?.let { msg ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = if (isErrorMessage) MaterialTheme.colorScheme.errorContainer else Color(0xFFE8F5E9),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 14.dp)
                        ) {
                            Text(
                                text = msg,
                                color = if (isErrorMessage) MaterialTheme.colorScheme.onErrorContainer else Color(0xFF1B5E20),
                                fontSize = 12.sp,
                                modifier = Modifier.padding(10.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                    }

                    // TAB 1: PHONE NUMBER WITH OTP
                    if (selectedTab == AuthTab.PHONE_OTP) {
                        Column {
                            if (!isOtpSent) {
                                // Step 1: Input Phone Number
                                Text(
                                    text = "Enter Mobile Number",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "We will send an SMS verification code (OTP) to your phone.",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    // Country Code
                                    OutlinedTextField(
                                        value = countryCode,
                                        onValueChange = { countryCode = it },
                                        label = { Text("Code") },
                                        modifier = Modifier.width(76.dp),
                                        singleLine = true
                                    )

                                    // Phone Number
                                    OutlinedTextField(
                                        value = phoneNumber,
                                        onValueChange = { phoneNumber = it },
                                        label = { Text("Phone Number") },
                                        placeholder = { Text("555-0199") },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                        modifier = Modifier
                                            .weight(1f)
                                            .testTag("phone_input_field"),
                                        singleLine = true,
                                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) }
                                    )
                                }

                                Spacer(modifier = Modifier.height(18.dp))

                                Button(
                                    onClick = {
                                        val fullNumber = "$countryCode $phoneNumber".trim()
                                        if (phoneNumber.trim().length < 6) {
                                            statusMessage = "Please enter a valid phone number."
                                            isErrorMessage = true
                                            return@Button
                                        }

                                         isSendingOtp = true
                                        viewModel.sendPhoneOtp(
                                            activity = activity,
                                            phoneNumber = fullNumber,
                                            onSent = { code ->
                                                isSendingOtp = false
                                                isOtpSent = true
                                                otpCode = "" // User always enters their own OTP code manually
                                                statusMessage = "Firebase verification code sent to $fullNumber"
                                                isErrorMessage = false
                                            },
                                            onAutoVerified = {
                                                isSendingOtp = false
                                                Toast.makeText(context, "Instant Phone Verification Complete!", Toast.LENGTH_SHORT).show()
                                            },
                                            onError = { err ->
                                                isSendingOtp = false
                                                statusMessage = err
                                                isErrorMessage = true
                                            }
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("send_otp_btn"),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    if (isSendingOtp) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Sending OTP...")
                                    } else {
                                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Send Verification OTP", fontWeight = FontWeight.Bold)
                                    }
                                }
                            } else {
                                // Step 2: Enter 6-digit OTP
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "Enter 6-Digit OTP",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    TextButton(onClick = { isOtpSent = false }) {
                                        Text("Change Phone", fontSize = 12.sp)
                                    }
                                }

                                Text(
                                    text = "Sent to $countryCode $phoneNumber",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                OutlinedTextField(
                                    value = otpCode,
                                    onValueChange = { if (it.length <= 6) otpCode = it },
                                    label = { Text("Enter 6-Digit Code") },
                                    placeholder = { Text("Type code from SMS") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .testTag("otp_input_field"),
                                    singleLine = true,
                                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) }
                                )

                                Spacer(modifier = Modifier.height(14.dp))

                                Button(
                                    onClick = {
                                        if (otpCode.trim().length != 6) {
                                            statusMessage = "Please enter the complete 6-digit OTP code."
                                            isErrorMessage = true
                                            return@Button
                                        }

                                        isVerifying = true
                                        viewModel.verifyPhoneOtp(
                                            enteredCode = otpCode,
                                            onSuccess = {
                                                isVerifying = false
                                                Toast.makeText(context, "Welcome to CampHaven!", Toast.LENGTH_SHORT).show()
                                            },
                                            onError = { err ->
                                                isVerifying = false
                                                statusMessage = err
                                                isErrorMessage = true
                                            }
                                        )
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("verify_otp_btn"),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    if (isVerifying) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            strokeWidth = 2.dp
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Verifying Code...")
                                    } else {
                                        Icon(Icons.Default.VerifiedUser, contentDescription = null, modifier = Modifier.size(18.dp))
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text("Verify & Sign In", fontWeight = FontWeight.Bold)
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (resendCountdown > 0) {
                                        Text(
                                            text = "Resend OTP in ${resendCountdown}s",
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    } else {
                                        TextButton(
                                            onClick = {
                                                viewModel.sendPhoneOtp(
                                                    activity = activity,
                                                    phoneNumber = "$countryCode $phoneNumber",
                                                    onSent = { code ->
                                                        otpCode = "" // User enters the newly received OTP code manually
                                                        resendCountdown = 60
                                                        statusMessage = "New Firebase code sent to $countryCode $phoneNumber"
                                                        isErrorMessage = false
                                                    },
                                                    onAutoVerified = {
                                                        Toast.makeText(context, "Instant Phone Verification Complete!", Toast.LENGTH_SHORT).show()
                                                    },
                                                    onError = { err ->
                                                        statusMessage = err
                                                        isErrorMessage = true
                                                    }
                                                )
                                            }
                                        ) {
                                            Text("Resend OTP Code", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // TAB 2: SIGN IN WITH GOOGLE
                    if (selectedTab == AuthTab.GOOGLE) {
                        var googleEmail by remember { mutableStateOf("") }
                        var googleDisplayName by remember { mutableStateOf("") }
                        var isDetectingAccount by remember { mutableStateOf(false) }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            // Google Header Emblem
                            Surface(
                                shape = CircleShape,
                                color = Color(0xFFF1F3F4),
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFF4285F4),
                                        modifier = Modifier.size(28.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text(
                                                text = "G",
                                                color = Color.White,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp
                                            )
                                        }
                                    }
                                }
                            }

                            Text(
                                text = "Sign in with Google",
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Text(
                                text = "Sign in with your Google Account to save favorite campsites, sync gear checklists, and navigate with Google Maps.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            // Google Email Input
                            OutlinedTextField(
                                value = googleEmail,
                                onValueChange = {
                                    googleEmail = it
                                    statusMessage = null
                                },
                                label = { Text("Google Account Email") },
                                placeholder = { Text("your.email@gmail.com") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("google_email_input"),
                                singleLine = true,
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.AccountCircle,
                                        contentDescription = null,
                                        tint = Color(0xFF4285F4)
                                    )
                                },
                                shape = RoundedCornerShape(12.dp)
                            )

                            // Optional Display Name
                            OutlinedTextField(
                                value = googleDisplayName,
                                onValueChange = { googleDisplayName = it },
                                label = { Text("Display Name (Optional)") },
                                placeholder = { Text("How other campers see you") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("google_name_input"),
                                singleLine = true,
                                leadingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.Badge,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                },
                                shape = RoundedCornerShape(12.dp)
                            )

                            Spacer(modifier = Modifier.height(4.dp))

                            // Primary Sign In with Google Button
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = MaterialTheme.colorScheme.surface,
                                shadowElevation = 2.dp,
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFDADCE0)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(48.dp)
                                    .clickable {
                                        val emailTrimmed = googleEmail.trim()
                                        if (emailTrimmed.isBlank() || !emailTrimmed.contains("@")) {
                                            statusMessage = "Please enter your Google Account email address."
                                            isErrorMessage = true
                                            return@clickable
                                        }

                                        val result = viewModel.directGoogleSignIn(
                                            email = emailTrimmed,
                                            displayName = googleDisplayName.trim()
                                        )

                                        if (result.isSuccess) {
                                            val signedInUser = result.getOrNull()
                                            Toast.makeText(
                                                context,
                                                "Signed in as ${signedInUser?.displayName ?: emailTrimmed}",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        } else {
                                            statusMessage = result.exceptionOrNull()?.message ?: "Sign-in failed."
                                            isErrorMessage = true
                                        }
                                    }
                                    .testTag("google_sign_in_btn")
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center
                                ) {
                                    Surface(
                                        shape = CircleShape,
                                        color = Color(0xFF4285F4),
                                        modifier = Modifier.size(22.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Text("G", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(
                                        text = "Sign in with Google Account",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = Color(0xFF3C4043)
                                    )
                                }
                            }

                            // Device Credential Manager Auto-Detect Option
                            TextButton(
                                onClick = {
                                    if (activity != null) {
                                        isDetectingAccount = true
                                        statusMessage = null
                                        viewModel.signInWithGoogle(
                                            activity = activity,
                                            onSuccess = {
                                                isDetectingAccount = false
                                                Toast.makeText(context, "Google Sign-In Complete!", Toast.LENGTH_SHORT).show()
                                            },
                                            onError = { err ->
                                                isDetectingAccount = false
                                                statusMessage = "No Google account detected on device. Please enter your email above."
                                                isErrorMessage = false
                                            }
                                        )
                                    } else {
                                        statusMessage = "Please enter your Google email above."
                                        isErrorMessage = false
                                    }
                                },
                                modifier = Modifier.testTag("detect_google_account_btn")
                            ) {
                                if (isDetectingAccount) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Detecting device account...", fontSize = 12.sp)
                                } else {
                                    Icon(Icons.Default.Sync, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Auto-detect Google Account from Device", fontSize = 12.sp)
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Protected by Google AI Studio Security & Firebase Auth\nBy continuing, you agree to Wilderness Dark-Sky & Camp Etiquette.",
                fontSize = 11.sp,
                color = Color.White.copy(alpha = 0.6f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
