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
    primary = GovPrimaryDark,
    onPrimary = GovOnPrimaryDark,
    primaryContainer = GovPrimaryContainerDark,
    onPrimaryContainer = GovOnPrimaryContainerDark,
    secondary = GovSecondaryDark,
    onSecondary = GovOnSecondaryDark,
    secondaryContainer = GovSecondaryContainerDark,
    onSecondaryContainer = GovOnSecondaryContainerDark,
    tertiary = GovTertiaryDark,
    onTertiary = GovOnTertiaryDark,
    tertiaryContainer = GovTertiaryContainerDark,
    onTertiaryContainer = GovOnTertiaryContainerDark,
    background = GovBackgroundDark,
    onBackground = GovOnBackgroundDark,
    surface = GovSurfaceDark,
    onSurface = GovOnSurfaceDark,
    surfaceVariant = GovSurfaceVariantDark,
    onSurfaceVariant = GovOnSurfaceVariantDark,
    outline = GovOutlineDark
)

private val LightColorScheme = lightColorScheme(
    primary = GovPrimaryLight,
    onPrimary = GovOnPrimaryLight,
    primaryContainer = GovPrimaryContainerLight,
    onPrimaryContainer = GovOnPrimaryContainerLight,
    secondary = GovSecondaryLight,
    onSecondary = GovOnSecondaryLight,
    secondaryContainer = GovSecondaryContainerLight,
    onSecondaryContainer = GovOnSecondaryContainerLight,
    tertiary = GovTertiaryLight,
    onTertiary = GovOnTertiaryLight,
    tertiaryContainer = GovTertiaryContainerLight,
    onTertiaryContainer = GovOnTertiaryContainerLight,
    background = GovBackgroundLight,
    onBackground = GovOnBackgroundLight,
    surface = GovSurfaceLight,
    onSurface = GovOnSurfaceLight,
    surfaceVariant = GovSurfaceVariantLight,
    onSurfaceVariant = GovOnSurfaceVariantLight,
    outline = GovOutlineLight
)

@Composable
fun GovWorkTheme(
    themeMode: String = "system", // "system", "light", "dark"
    dynamicColor: Boolean = false, // Keep executive government theme cohesive
    content: @Composable () -> Unit
) {
    val darkTheme = when (themeMode) {
        "light" -> false
        "dark" -> true
        else -> isSystemInDarkTheme()
    }

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
