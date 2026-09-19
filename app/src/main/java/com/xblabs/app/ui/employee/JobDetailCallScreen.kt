package com.xblabs.app.ui.employee

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xblabs.app.data.models.*
import com.xblabs.app.ui.components.ClientStatusChip
import com.xblabs.app.ui.components.FollowUpBadge

@Composable
fun JobDetailCallScreen(
    client: Client,
    employee: User,
    callRecords: List<CallRecord>,
    onBack: () -> Unit,
    onSubmitOutcome: (CallStatusOutcome, String, Long?) -> Unit
) {
    val context = LocalContext.current
    var selectedOutcome by remember { mutableStateOf<CallStatusOutcome?>(null) }
    var notesText by remember { mutableStateOf("") }
    var selectedScheduleDays by remember { mutableStateOf(1) } // 1, 3, or 7 days

    val clientCalls = callRecords.filter { it.clientId == client.id }

    val primaryColor = if (employee.isPinkPrincess) Color(0xFFEC4899) else Color(0xFF38BDF8)
    val cardBg = if (employee.isPinkPrincess) Color(0xFF2A152A) else Color(0xFF1E293B)
    val cardBorder = if (employee.isPinkPrincess) Color(0xFF4A204B) else Color(0xFF334155)

    fun makeCall() {
        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${client.phone}"))
        context.startActivity(intent)
    }

    fun submit() {
        val outcome = selectedOutcome ?: return
        val now = System.currentTimeMillis()
        val customDate = now + (selectedScheduleDays * 86400000L)
        onSubmitOutcome(outcome, notesText.trim(), customDate)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Back Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(4.dp))
            Text("Client & Call Workflow", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.weight(1f)
        ) {
            // Business Overview Card
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(client.businessName, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            ClientStatusChip(status = client.currentStatus)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Category, contentDescription = null, tint = primaryColor, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Category: ${client.category}", fontSize = 12.sp, color = Color(0xFFCBD5E1))

                            if (client.rating > 0.0) {
                                Spacer(modifier = Modifier.width(12.dp))
                                Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(2.dp))
                                Text("${client.rating} (${client.reviewCount})", fontSize = 12.sp, color = Color(0xFFF59E0B))
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text("Address: ${client.address}", fontSize = 12.sp, color = Color(0xFF94A3B8))
                        Text("Website: ${client.displayWebsite}", fontSize = 12.sp, color = primaryColor)

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FollowUpBadge(attemptNumber = client.followUpCount)

                            if (client.mapsUrl.isNotBlank()) {
                                TextButton(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(client.mapsUrl))
                                        context.startActivity(intent)
                                    },
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Place, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Open Maps", fontSize = 12.sp, color = primaryColor)
                                }
                            }
                        }
                    }
                }
            }

            // Big Prominent Call Button
            item {
                Button(
                    onClick = { makeCall() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp),
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("CALL CLIENT (${client.phone})", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }

            // Call Result Selection Section
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder)
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text("Select Call Outcome Result", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.height(12.dp))

                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutcomeChipOption(
                                title = "Didn't Pick Up 📵",
                                description = "Move to follow-up schedule (attempt ${client.followUpCount + 1}/3)",
                                isSelected = selectedOutcome == CallStatusOutcome.DIDNT_PICK_UP,
                                color = Color(0xFFF59E0B),
                                onClick = { selectedOutcome = CallStatusOutcome.DIDNT_PICK_UP }
                            )

                            OutcomeChipOption(
                                title = "Maybe Interested 🤔",
                                description = "Move to follow-up schedule (attempt ${client.followUpCount + 1}/3)",
                                isSelected = selectedOutcome == CallStatusOutcome.MAYBE_INTERESTED,
                                color = Color(0xFF38BDF8),
                                onClick = { selectedOutcome = CallStatusOutcome.MAYBE_INTERESTED }
                            )

                            OutcomeChipOption(
                                title = "Interested 🔥",
                                description = "Move to High Priority Interested Pipeline",
                                isSelected = selectedOutcome == CallStatusOutcome.INTERESTED,
                                color = Color(0xFF10B981),
                                onClick = { selectedOutcome = CallStatusOutcome.INTERESTED }
                            )

                            OutcomeChipOption(
                                title = "Not Interested ❌",
                                description = "Close employee workflow for this lead",
                                isSelected = selectedOutcome == CallStatusOutcome.NOT_INTERESTED,
                                color = Color(0xFF64748B),
                                onClick = { selectedOutcome = CallStatusOutcome.NOT_INTERESTED }
                            )
                        }

                        if (selectedOutcome == CallStatusOutcome.DIDNT_PICK_UP || selectedOutcome == CallStatusOutcome.MAYBE_INTERESTED) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Text("Follow-up Schedule Date:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                            Spacer(modifier = Modifier.height(6.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                FilterChip(
                                    selected = selectedScheduleDays == 1,
                                    onClick = { selectedScheduleDays = 1 },
                                    label = { Text("Tomorrow") }
                                )
                                FilterChip(
                                    selected = selectedScheduleDays == 3,
                                    onClick = { selectedScheduleDays = 3 },
                                    label = { Text("In 3 Days") }
                                )
                                FilterChip(
                                    selected = selectedScheduleDays == 7,
                                    onClick = { selectedScheduleDays = 7 },
                                    label = { Text("In 7 Days") }
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        OutlinedTextField(
                            value = notesText,
                            onValueChange = { notesText = it },
                            placeholder = { Text("Add call notes (e.g., 'Asked to call back after 5 PM')", color = Color(0xFF94A3B8)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(90.dp),
                            shape = RoundedCornerShape(12.dp)
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { submit() },
                            enabled = selectedOutcome != null,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                        ) {
                            Text("Save Outcome & Continue", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OutcomeChipOption(
    title: String,
    description: String,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) color.copy(alpha = 0.2f) else Color(0xFF0F172A),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) color else Color(0xFF334155))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(selected = isSelected, onClick = onClick, colors = RadioButtonDefaults.colors(selectedColor = color))
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text(description, fontSize = 11.sp, color = Color(0xFF94A3B8))
            }
        }
    }
}
