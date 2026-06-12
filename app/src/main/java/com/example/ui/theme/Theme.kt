package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable

private val TimeLeftColorScheme = darkColorScheme(
    primary = Accent,
    secondary = AccentDark,
    background = Background,
    surface = Surface,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onPrimary = AccentText,
    onSecondary = TextPrimary
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = true, // Force dark theme for OLED productivity aesthetic
    dynamicColor: Boolean = false, // Disable dynamic colors to keep precise custom amber look
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = TimeLeftColorScheme,
        typography = Typography,
        content = content
    )
}
