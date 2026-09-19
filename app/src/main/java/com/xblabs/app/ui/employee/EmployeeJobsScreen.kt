package com.xblabs.app.ui.employee

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xblabs.app.data.models.*
import com.xblabs.app.ui.components.ClientStatusChip
import com.xblabs.app.ui.components.FollowUpBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeJobsScreen(
    employee: User,
    clients: List<Client>,
    onSelectJob: (Client) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("All Active", "New", "Follow-Up ⏳", "High Priority")

    val myAssignedClients = remember(clients, employee) {
        clients.filter {
            it.assignedEmployeeId == employee.id && it.employeeWorkflowStatus == WorkflowStatus.ACTIVE
        }
    }

    val filteredList = remember(myAssignedClients, searchQuery, selectedTab) {
        myAssignedClients.filter { client ->
            val matchesQuery = searchQuery.isBlank() ||
                    client.businessName.contains(searchQuery, ignoreCase = true) ||
                    client.phone.contains(searchQuery) ||
                    client.category.contains(searchQuery, ignoreCase = true)

            val matchesTab = when (selectedTab) {
                1 -> client.currentStatus == ClientStatus.NEW
                2 -> client.currentStatus == ClientStatus.FOLLOW_UP
                3 -> client.priority == Priority.HIGH || client.currentStatus == ClientStatus.INTERESTED
                else -> true
            }

            matchesQuery && matchesTab
        }
    }

    val cardBg = if (employee.isPinkPrincess) Color(0xFF2A152A) else Color(0xFF1E293B)
    val cardBorder = if (employee.isPinkPrincess) Color(0xFF4A204B) else Color(0xFF334155)
    val accentColor = if (employee.isPinkPrincess) Color(0xFFEC4899) else Color(0xFF38BDF8)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search my assigned jobs...", color = Color(0xFF94A3B8)) },
            leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = accentColor) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(14.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = accentColor,
                unfocusedBorderColor = cardBorder,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White,
                focusedContainerColor = if (employee.isPinkPrincess) Color(0xFF180E19) else Color(0xFF0F172A),
                unfocusedContainerColor = if (employee.isPinkPrincess) Color(0xFF180E19) else Color(0xFF0F172A)
            )
        )

        Spacer(modifier = Modifier.height(10.dp))

        ScrollableTabRow(
            selectedTabIndex = selectedTab,
            containerColor = cardBg,
            contentColor = accentColor
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
                    .height(220.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (employee.isPinkPrincess) "No active jobs in this queue ✨" else "No active jobs found.",
                    color = Color(0xFF94A3B8),
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredList) { client ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectJob(client) },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = client.businessName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.weight(1f)
                                )

                                ClientStatusChip(status = client.currentStatus)
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = accentColor, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(client.phone, fontSize = 13.sp, color = Color(0xFFCBD5E1), fontWeight = FontWeight.Medium)
                                }

                                if (client.followUpCount > 0) {
                                    FollowUpBadge(attemptNumber = client.followUpCount)
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Text(
                                text = "Category: ${client.category} • ${client.address.take(30)}...",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                }
            }
        }
    }
}
