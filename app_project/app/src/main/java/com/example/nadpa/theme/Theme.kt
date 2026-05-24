package com.example.nadpa.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// NAD PA strictly uses a light (white) base with black accents
private val NADColorScheme = lightColorScheme(
    primary = Black,
    onPrimary = White,
    primaryContainer = DarkGray,
    onPrimaryContainer = White,
    secondary = DarkGray,
    onSecondary = White,
    background = White,
    onBackground = Black,
    surface = White,
    onSurface = Black,
    surfaceVariant = OffWhite,
    onSurfaceVariant = DarkGray,
    outline = LightGray,
    outlineVariant = MediumGray,
    error = Black,
    onError = White,
)

@Composable
fun NADPATheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = NADColorScheme,
        typography = Typography,
        content = content
    )
}
