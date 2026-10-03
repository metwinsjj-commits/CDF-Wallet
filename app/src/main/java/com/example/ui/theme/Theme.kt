package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val TrustDarkColorScheme = darkColorScheme(
    primary = TrustBlue,
    onPrimary = TrustDarkTextPrimary,
    primaryContainer = TrustBlueContainer,
    onPrimaryContainer = TrustBlueLight,
    secondary = TrustGreen,
    onSecondary = TrustDarkBg,
    secondaryContainer = TrustGreenContainer,
    onSecondaryContainer = TrustGreen,
    tertiary = CdfGoldAccent,
    background = TrustDarkBg,
    surface = TrustDarkSurface,
    surfaceVariant = TrustDarkSurfaceElevated,
    outline = TrustDarkBorder,
    outlineVariant = TrustDarkBorder,
    onBackground = TrustDarkTextPrimary,
    onSurface = TrustDarkTextPrimary,
    onSurfaceVariant = TrustDarkTextSecondary,
    error = TrustRed
)

private val TrustLightColorScheme = lightColorScheme(
    primary = TrustBlue,
    onPrimary = TrustLightSurface,
    primaryContainer = TrustBlueContainer,
    onPrimaryContainer = TrustBlue,
    secondary = TrustGreen,
    onSecondary = TrustLightSurface,
    secondaryContainer = TrustGreenContainer,
    onSecondaryContainer = TrustGreenDark,
    tertiary = CdfGoldAccent,
    background = TrustLightBg,
    surface = TrustLightSurface,
    surfaceVariant = TrustLightSurfaceElevated,
    outline = TrustLightBorder,
    outlineVariant = TrustLightBorder,
    onBackground = TrustLightTextPrimary,
    onSurface = TrustLightTextPrimary,
    onSurfaceVariant = TrustLightTextSecondary,
    error = TrustRed
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Default to Trust Wallet's signature midnight dark look
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) TrustDarkColorScheme else TrustLightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
