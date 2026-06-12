package com.example.ui.theme

import androidx.compose.ui.graphics.Color

val Background = Color(0xFF000000)
val Surface = Color(0xFF1A1A1A)
val SurfaceAlt = Color(0xFF242424)
val Border = Color(0xFF2A2A2A)
val Accent = Color(0xFFF5A623)
val AccentDark = Color(0xFFC47D0E)
val AccentText = Color(0xFF000000)
val TextPrimary = Color(0xFFFFFFFF)
val TextMuted = Color(0xFF888888)
val TextDim = Color(0xFF444444)

val CategoryWork = Color(0xFFF5A623)
val CategoryHealth = Color(0xFF4CAF50)
val CategoryLearning = Color(0xFF9C27B0)
val CategoryPersonal = Color(0xFF2196F3)

fun getCategoryColor(category: String): Color {
    return when (category) {
        "Work" -> CategoryWork
        "Health" -> CategoryHealth
        "Learning" -> CategoryLearning
        "Personal" -> CategoryPersonal
        else -> TextMuted
    }
}
