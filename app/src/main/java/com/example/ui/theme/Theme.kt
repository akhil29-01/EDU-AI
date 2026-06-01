package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryNavy,
    secondary = SecondaryPurple,
    tertiary = SecondaryContainerPurple,
    background = PrimaryNavy,
    surface = PrimaryNavy,
    onPrimary = CardSurfaceWhite,
    onSecondary = CardSurfaceWhite,
    onBackground = CardSurfaceWhite,
    onSurface = CardSurfaceWhite
)

private val LightColorScheme = lightColorScheme(
    primary = PrimaryNavy,
    secondary = SecondaryPurple,
    secondaryContainer = SecondaryContainerPurple,
    onSecondaryContainer = OnSecondaryContainerPurple,
    tertiary = SecondaryContainerPurple,
    background = BackgroundSurface,
    surface = CardSurfaceWhite,
    onPrimary = CardSurfaceWhite,
    onSecondary = CardSurfaceWhite,
    onBackground = TextDark,
    onSurface = TextDark,
    onSurfaceVariant = TextDarkVariant,
    outlineVariant = OutlineVariantBorder,
    outline = OutlineGrey,
    error = ErrorCrimson,
    errorContainer = ErrorContainerCrimson
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Set to false to preserve brand system identity
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
