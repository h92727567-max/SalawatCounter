package com.salawat.counter.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = Gold,
    onPrimary = DarkGreen,
    secondary = MediumGreen,
    background = DarkGreen,
    surface = MediumGreen,
    onBackground = White,
    onSurface = White
)

@Composable
fun SalawatCounterTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
