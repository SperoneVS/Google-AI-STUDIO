package com.example.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = ForestPrimaryDark,
    onPrimary = ForestOnPrimaryDark,
    primaryContainer = ForestPrimaryContainerDark,
    onPrimaryContainer = ForestOnPrimaryContainerDark,
    secondary = WaterSecondaryDark,
    onSecondary = WaterOnSecondaryDark,
    secondaryContainer = WaterSecondaryContainerDark,
    onSecondaryContainer = WaterOnSecondaryContainerDark,
    tertiary = EnergyTertiaryDark,
    onTertiary = EnergyOnTertiaryDark,
    tertiaryContainer = EnergyTertiaryContainerDark,
    onTertiaryContainer = EnergyOnTertiaryContainerDark,
    background = CampBackgroundDark,
    onBackground = CampOnBackgroundDark,
    surface = CampSurfaceDark,
    onSurface = CampOnSurfaceDark,
    surfaceVariant = CampSurfaceVariantDark,
    onSurfaceVariant = CampOnSurfaceVariantDark,
)

private val LightColorScheme = lightColorScheme(
    primary = ForestPrimary,
    onPrimary = ForestOnPrimary,
    primaryContainer = ForestPrimaryContainer,
    onPrimaryContainer = ForestOnPrimaryContainer,
    secondary = WaterSecondary,
    onSecondary = WaterOnSecondary,
    secondaryContainer = WaterSecondaryContainer,
    onSecondaryContainer = WaterOnSecondaryContainer,
    tertiary = EnergyTertiary,
    onTertiary = EnergyOnTertiary,
    tertiaryContainer = EnergyTertiaryContainer,
    onTertiaryContainer = EnergyOnTertiaryContainer,
    background = CampBackgroundLight,
    onBackground = CampOnBackgroundLight,
    surface = CampSurfaceLight,
    onSurface = CampOnSurfaceLight,
    surfaceVariant = CampSurfaceVariantLight,
    onSurfaceVariant = CampOnSurfaceVariantLight,
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Use our handcrafted forest/water/energy palette by default
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
