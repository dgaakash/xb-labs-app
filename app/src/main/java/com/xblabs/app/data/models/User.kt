package com.xblabs.app.data.models

import androidx.compose.ui.graphics.Color

enum class UserRole {
    ADMIN,
    EMPLOYEE
}

data class User(
    val id: String,
    val name: String,
    val email: String,
    val username: String,
    val role: UserRole,
    val passwordHash: String,
    val theme: String = "default", // "default" or "pink-princess"
    val active: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val lastActive: Long = System.currentTimeMillis()
) {
    val isPinkPrincess: Boolean
        get() = theme == "pink-princess" || username.equals("blessi", ignoreCase = true)

    val roleName: String
        get() = if (role == UserRole.ADMIN) "Admin" else "Employee"

    val displayColor: Color
        get() = if (isPinkPrincess) Color(0xFFEC4899) else if (role == UserRole.ADMIN) Color(0xFF6366F1) else Color(0xFF38BDF8)
}
