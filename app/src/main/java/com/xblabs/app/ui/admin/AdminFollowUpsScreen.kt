package com.xblabs.app.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xblabs.app.data.models.FollowUp
import com.xblabs.app.ui.components.FollowUpBadge
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdminFollowUpsScreen(
    followUps: List<FollowUp>
) {
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Due Today", "Overdue", "Upcoming", "All")

    val now = System.currentTimeMillis()
    val startOfToday = now - (now % (86400000L))
    val endOfToday = startOfToday + 86400000L

    val filteredFollowUps = remember(followUps, selectedTab) {
        when (selectedTab) {
            0 -> followUps.filter { it.dueDate >= startOfToday && it.dueDate < endOfToday }
            1 -> followUps.filter { it.isOverdue }
            2 -> followUps.filter { it.dueDate >= endOfToday }
            else -> followUps
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = Icons.Default.Schedule, contentDescription = null, tint = Color(0xFF38BDF8))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Master Follow-ups Overview", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(modifier = Modifier.height(14.dp))

        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color(0xFF1E293B),
            contentColor = Color(0xFF38BDF8)
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

        if (filteredFollowUps.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No follow-ups found in this section.", color = Color(0xFF94A3B8), fontSize = 14.sp)
            }
        } else {
            val dateFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredFollowUps) { fol ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (fol.isOverdue) Color(0xFFEF4444) else Color(0xFF334155))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(fol.clientName, fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                FollowUpBadge(attemptNumber = fol.attemptNumber)
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text("Employee: ${fol.employeeName} • Phone: ${fol.clientPhone}", fontSize = 12.sp, color = Color(0xFFCBD5E1))
                            Text("Due Date: ${dateFormat.format(Date(fol.dueDate))}", fontSize = 12.sp, color = if (fol.isOverdue) Color(0xFFEF4444) else Color(0xFF38BDF8))

                            if (fol.notes.isNotBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Note: ${fol.notes}", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            }
                        }
                    }
                }
            }
        }
    }
}
