package com.xblabs.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Pink Princess Palette (for Blessi / Pink Princess Theme)
val PinkPrincessBg = Color(0xFF180E19)
val PinkPrincessSurface = Color(0xFF2A152A)
val PinkPrincessBorder = Color(0xFF4A204B)
val PinkPrincessPrimary = Color(0xFFF472B6)
val PinkPrincessSecondary = Color(0xFFEC4899)
val PinkPrincessAccent = Color(0xFFF43F5E)
val PinkPrincessTextPrimary = Color(0xFFFDF2F8)
val PinkPrincessTextSecondary = Color(0xFFFBCFE8)

// Standard Employee Palette
val EmployeeStandardBg = Color(0xFF0F172A)
val EmployeeStandardSurface = Color(0xFF1E293B)
val EmployeeStandardPrimary = Color(0xFF38BDF8)

private val PinkPrincessColorScheme = darkColorScheme(
    primary = PinkPrincessPrimary,
    secondary = PinkPrincessSecondary,
    background = PinkPrincessBg,
    surface = PinkPrincessSurface,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = PinkPrincessTextPrimary,
    onSurface = PinkPrincessTextPrimary
)

private val StandardEmployeeColorScheme = darkColorScheme(
    primary = EmployeeStandardPrimary,
    secondary = Color(0xFF6366F1),
    background = EmployeeStandardBg,
    surface = EmployeeStandardSurface,
    onPrimary = Color(0xFF0F172A),
    onSecondary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

@Composable
fun EmployeeCrmTheme(
    isPinkPrincess: Boolean = false,
    content: @Composable () -> Unit
) {
    val colors = if (isPinkPrincess) PinkPrincessColorScheme else StandardEmployeeColorScheme
    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}
