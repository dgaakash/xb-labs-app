package com.xblabs.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val AdminDarkBackground = Color(0xFF0F172A)
val AdminCardSurface = Color(0xFF1E293B)
val AdminCardBorder = Color(0xFF334155)
val AdminSkyPrimary = Color(0xFF38BDF8)
val AdminIndigoSecondary = Color(0xFF6366F1)
val AdminEmeraldSuccess = Color(0xFF10B981)
val AdminAmberWarning = Color(0xFFF59E0B)
val AdminRoseDanger = Color(0xFFEF4444)
val AdminTextPrimary = Color(0xFFF8FAFC)
val AdminTextSecondary = Color(0xFF94A3B8)

private val AdminColorScheme = darkColorScheme(
    primary = AdminSkyPrimary,
    secondary = AdminIndigoSecondary,
    background = AdminDarkBackground,
    surface = AdminCardSurface,
    onPrimary = Color(0xFF0F172A),
    onSecondary = Color.White,
    onBackground = AdminTextPrimary,
    onSurface = AdminTextPrimary
)

@Composable
fun AdminCrmTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AdminColorScheme,
        content = content
    )
}
