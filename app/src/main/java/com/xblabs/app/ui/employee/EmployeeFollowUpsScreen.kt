package com.xblabs.app.ui.employee

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xblabs.app.data.models.*
import com.xblabs.app.ui.components.FollowUpBadge
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun EmployeeFollowUpsScreen(
    employee: User,
    clients: List<Client>,
    followUps: List<FollowUp>,
    onSelectClient: (Client) -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Due Today", "Overdue", "Upcoming")

    val myFollowUps = followUps.filter { it.employeeId == employee.id && it.state == FollowUpState.PENDING }

    val now = System.currentTimeMillis()
    val startOfToday = now - (now % (86400000L))
    val endOfToday = startOfToday + 86400000L

    val filteredList = remember(myFollowUps, selectedTab) {
        when (selectedTab) {
            0 -> myFollowUps.filter { it.dueDate >= startOfToday && it.dueDate < endOfToday }
            1 -> myFollowUps.filter { it.isOverdue }
            else -> myFollowUps.filter { it.dueDate >= endOfToday }
        }
    }

    val primaryColor = if (employee.isPinkPrincess) Color(0xFFEC4899) else Color(0xFF38BDF8)
    val cardBg = if (employee.isPinkPrincess) Color(0xFF2A152A) else Color(0xFF1E293B)
    val cardBorder = if (employee.isPinkPrincess) Color(0xFF4A204B) else Color(0xFF334155)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Schedule, contentDescription = null, tint = primaryColor)
            Spacer(modifier = Modifier.width(8.dp))
            Text("My Scheduled Follow-ups", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(modifier = Modifier.height(14.dp))

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = cardBg,
            contentColor = primaryColor
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (filteredList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (employee.isPinkPrincess) "No follow-ups due here! ✨" else "No follow-ups due in this section.",
                    color = Color(0xFF94A3B8),
                    fontSize = 14.sp
                )
            }
        } else {
            val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredList) { fol ->
                    val client = clients.find { it.id == fol.clientId }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { if (client != null) onSelectClient(client) },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (fol.isOverdue) Color(0xFFEF4444) else cardBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(fol.clientName, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                FollowUpBadge(attemptNumber = fol.attemptNumber)
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text("Phone: ${fol.clientPhone}", fontSize = 12.sp, color = Color(0xFFCBD5E1))
                            Text("Scheduled: ${dateFormat.format(Date(fol.dueDate))}", fontSize = 12.sp, color = if (fol.isOverdue) Color(0xFFEF4444) else primaryColor)

                            if (fol.notes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Previous Note: ${fol.notes}", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            }
                        }
                    }
                }
            }
        }
    }
}
