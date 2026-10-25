package com.vedshubham.dailysix.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Light = lightColorScheme(
    primary = Color(0xFFD9701E),
    onPrimary = Color.White,
    primaryContainer = Color(0xFFFFE0C7),
    onPrimaryContainer = Color(0xFF5A2D0C),
    secondary = Color(0xFF3A6EA5),
    secondaryContainer = Color(0xFFDCE8F7),
    onSecondaryContainer = Color(0xFF14304F),
    background = Color(0xFFFFFBF5),
    surface = Color(0xFFFFFBF5),
    surfaceVariant = Color(0xFFF3EBE2),
    onSurfaceVariant = Color(0xFF5E554C),
    outline = Color(0xFF8C8178),
    error = Color(0xFFC62828),
    errorContainer = Color(0xFFFFDAD6),
)

private val Dark = darkColorScheme(
    primary = Color(0xFFFFB57A),
    onPrimary = Color(0xFF4A2300),
    primaryContainer = Color(0xFF6E3A10),
    onPrimaryContainer = Color(0xFFFFDCC2),
    secondary = Color(0xFF9CC4F0),
    secondaryContainer = Color(0xFF26476B),
    onSecondaryContainer = Color(0xFFD4E5FA),
    background = Color(0xFF16130F),
    surface = Color(0xFF16130F),
    surfaceVariant = Color(0xFF2B2520),
    onSurfaceVariant = Color(0xFFD3C6BA),
    outline = Color(0xFF9C9086),
    error = Color(0xFFFF8A80),
    errorContainer = Color(0xFF5C1A16),
)

@Composable
fun DailySixTheme(content: @Composable () -> Unit) {
    MaterialTheme(colorScheme = if (isSystemInDarkTheme()) Dark else Light, content = content)
}
