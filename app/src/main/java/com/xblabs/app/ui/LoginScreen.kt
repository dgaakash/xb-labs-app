package com.xblabs.app.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class UserAccount(
    val email: String,
    val name: String,
    val role: String,
    val color: Color
)

object PredefinedAccounts {
    val ADMIN = UserAccount(
        email = "xavier@xblabs.com",
        name = "Xavier",
        role = "Admin",
        color = Color(0xFF6366F1) // Indigo
    )

    val EMPLOYEE = UserAccount(
        email = "blessi@xblabs.com",
        name = "Blessi",
        role = "PR & Sales",
        color = Color(0xFFEC4899) // Pink
    )

    val ALL = listOf(ADMIN, EMPLOYEE)

    fun findByEmail(email: String): UserAccount? {
        val trimmed = email.trim().lowercase()
        return ALL.find { it.email.lowercase() == trimmed }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: (UserAccount) -> Unit
) {
    var emailInput by remember { mutableStateOf("") }
    var passwordInput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val backgroundBrush = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF0F172A),
            Color(0xFF1E293B),
            Color(0xFF0F172A)
        )
    )

    fun attemptLogin(email: String) {
        val account = PredefinedAccounts.findByEmail(email)
        if (account != null) {
            errorMessage = null
            onLoginSuccess(account)
        } else {
            errorMessage = "Invalid email. Please use xavier@xblabs.com or blessi@xblabs.com"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundBrush)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFF1E293B).copy(alpha = 0.95f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header Logo Badge
                Box(
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF38BDF8), Color(0xFF6366F1))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "XB Labs Logo",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "XB Labs Portal",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Sign in to access your workspace",
                    fontSize = 14.sp,
                    color = Color(0xFF94A3B8),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // One-tap Account Quick Selectors
                Text(
                    text = "Select Account:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFFCBD5E1),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Admin Quick Tile
                    AccountQuickTile(
                        account = PredefinedAccounts.ADMIN,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            emailInput = PredefinedAccounts.ADMIN.email
                            attemptLogin(PredefinedAccounts.ADMIN.email)
                        }
                    )

                    // Employee Quick Tile
                    AccountQuickTile(
                        account = PredefinedAccounts.EMPLOYEE,
                        modifier = Modifier.weight(1f),
                        onClick = {
                            emailInput = PredefinedAccounts.EMPLOYEE.email
                            attemptLogin(PredefinedAccounts.EMPLOYEE.email)
                        }
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                HorizontalDivider(color = Color(0xFF334155), thickness = 1.dp)

                Spacer(modifier = Modifier.height(20.dp))

                // Email Input
                OutlinedTextField(
                    value = emailInput,
                    onValueChange = {
                        emailInput = it
                        errorMessage = null
                    },
                    label = { Text("Email Address", color = Color(0xFF94A3B8)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8)
                        )
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF0F172A).copy(alpha = 0.6f),
                        unfocusedContainerColor = Color(0xFF0F172A).copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Password Input
                OutlinedTextField(
                    value = passwordInput,
                    onValueChange = { passwordInput = it },
                    label = { Text("Password", color = Color(0xFF94A3B8)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = Color(0xFF38BDF8)
                        )
                    },
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { attemptLogin(emailInput) }
                    ),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF0F172A).copy(alpha = 0.6f),
                        unfocusedContainerColor = Color(0xFF0F172A).copy(alpha = 0.6f)
                    ),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                AnimatedVisibility(visible = errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = Color(0xFFEF4444),
                        fontSize = 12.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        textAlign = TextAlign.Start
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Login Button
                Button(
                    onClick = { attemptLogin(emailInput) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF38BDF8)
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = Color(0xFF0F172A)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Sign In",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AccountQuickTile(
    account: UserAccount,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = 1.dp,
                color = account.color.copy(alpha = 0.4f),
                shape = RoundedCornerShape(16.dp)
            )
            .clickable { onClick() },
        color = account.color.copy(alpha = 0.12f)
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(account.color),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = account.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = account.role,
                fontSize = 11.sp,
                color = account.color,
                fontWeight = FontWeight.Medium
            )
        }
    }
}
