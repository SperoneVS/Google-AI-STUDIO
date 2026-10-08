package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.CampsiteViewModel
import com.example.ui.viewmodel.ScreenDestination

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                val viewModel: CampsiteViewModel = viewModel()
                val currentScreen by viewModel.currentScreen.collectAsState()

                // Request location to compute real proximity if camper grants permission
                val locationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) { permissions ->
                    val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
                    val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
                    if (fineGranted || coarseGranted) {
                        tryGetLocation { lat, lon ->
                            viewModel.setUserLocation(lat, lon)
                        }
                    }
                }

                LaunchedEffect(Unit) {
                    viewModel.trySilentSignIn(this@MainActivity)
                    val fineCheck = ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.ACCESS_FINE_LOCATION)
                    if (fineCheck != PackageManager.PERMISSION_GRANTED) {
                        locationPermissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    } else {
                        tryGetLocation { lat, lon ->
                            viewModel.setUserLocation(lat, lon)
                        }
                    }
                }

                val currentUser by viewModel.currentUser.collectAsState()

                Surface(modifier = Modifier.fillMaxSize()) {
                    if (currentUser == null) {
                        AuthScreen(viewModel = viewModel)
                    } else {
                        AnimatedContent(
                            targetState = currentScreen,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "ScreenTransition"
                        ) { screen ->
                            when (screen) {
                                is ScreenDestination.Explore -> {
                                    ExploreScreen(viewModel = viewModel)
                                }
                                is ScreenDestination.RadarMap -> {
                                    RadarMapScreen(viewModel = viewModel)
                                }
                                is ScreenDestination.Detail -> {
                                    DetailScreen(campsiteId = screen.campsiteId, viewModel = viewModel)
                                }
                                is ScreenDestination.AddSpot -> {
                                    AddCampsiteScreen(viewModel = viewModel)
                                }
                                is ScreenDestination.GearChecklist -> {
                                    GearChecklistScreen(viewModel = viewModel)
                                }
                                is ScreenDestination.Profile -> {
                                    ProfileScreen(viewModel = viewModel)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private fun tryGetLocation(onLocationFound: (Double, Double) -> Unit) {
        try {
            val locationManager = getSystemService(LOCATION_SERVICE) as? android.location.LocationManager
            val location: Location? = locationManager?.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER)
                ?: locationManager?.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER)
            if (location != null) {
                onLocationFound(location.latitude, location.longitude)
            }
        } catch (_: SecurityException) {
            // Permission denied or revoked; default coordinates are safely used
        }
    }
}
