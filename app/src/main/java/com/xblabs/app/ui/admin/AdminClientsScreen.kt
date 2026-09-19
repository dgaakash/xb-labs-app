package com.xblabs.app.ui.admin

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xblabs.app.data.models.*
import com.xblabs.app.ui.components.ClientStatusChip
import com.xblabs.app.ui.components.FollowUpBadge
import com.xblabs.app.ui.components.PriorityBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminClientsScreen(
    clients: List<Client>,
    employees: List<User>,
    callRecords: List<CallRecord>,
    followUps: List<FollowUp>,
    onReassignClients: (List<String>, User) -> Unit,
    onArchiveClient: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedEmployeeFilter by remember { mutableStateOf<String?>(null) }
    var selectedStatusFilter by remember { mutableStateOf<ClientStatus?>(null) }
    var selectedClientForDetail by remember { mutableStateOf<Client?>(null) }

    // Multi-select state
    var selectedClientIds by remember { mutableStateOf(setOf<String>()) }
    var showBulkReassignModal by remember { mutableStateOf(false) }

    val filteredClients = remember(clients, searchQuery, selectedEmployeeFilter, selectedStatusFilter) {
        clients.filter { client ->
            val matchQuery = searchQuery.isBlank() ||
                    client.businessName.contains(searchQuery, ignoreCase = true) ||
                    client.phone.contains(searchQuery) ||
                    client.address.contains(searchQuery, ignoreCase = true) ||
                    client.website.contains(searchQuery, ignoreCase = true)

            val matchEmp = selectedEmployeeFilter == null || client.assignedEmployeeId == selectedEmployeeFilter
            val matchStatus = selectedStatusFilter == null || client.currentStatus == selectedStatusFilter

            matchQuery && matchEmp && matchStatus
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Search & Filter Header
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by name, phone, address...", color = Color(0xFF64748B)) },
                    leadingIcon = { Icon(imageVector = Icons.Default.Search, contentDescription = null, tint = Color(0xFF38BDF8)) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = null, tint = Color(0xFF94A3B8))
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF38BDF8),
                        unfocusedBorderColor = Color(0xFF334155),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedContainerColor = Color(0xFF0F172A),
                        unfocusedContainerColor = Color(0xFF0F172A)
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Filters Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        selected = selectedStatusFilter == null,
                        onClick = { selectedStatusFilter = null },
                        label = { Text("All Statuses", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = selectedStatusFilter == ClientStatus.NEW,
                        onClick = { selectedStatusFilter = if (selectedStatusFilter == ClientStatus.NEW) null else ClientStatus.NEW },
                        label = { Text("New", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = selectedStatusFilter == ClientStatus.FOLLOW_UP,
                        onClick = { selectedStatusFilter = if (selectedStatusFilter == ClientStatus.FOLLOW_UP) null else ClientStatus.FOLLOW_UP },
                        label = { Text("Follow-Up", fontSize = 11.sp) }
                    )
                    FilterChip(
                        selected = selectedStatusFilter == ClientStatus.INTERESTED,
                        onClick = { selectedStatusFilter = if (selectedStatusFilter == ClientStatus.INTERESTED) null else ClientStatus.INTERESTED },
                        label = { Text("Interested", fontSize = 11.sp) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Bulk action header if selected
        if (selectedClientIds.isNotEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = Color(0xFF38BDF8).copy(alpha = 0.15f),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${selectedClientIds.size} client(s) selected",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8)
                    )

                    Row {
                        Button(
                            onClick = { showBulkReassignModal = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text("Reassign Selected", fontSize = 11.sp, color = Color(0xFF0F172A))
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        TextButton(onClick = { selectedClientIds = emptySet() }) {
                            Text("Deselect", fontSize = 11.sp, color = Color(0xFF94A3B8))
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
        }

        Text(
            text = "Master Client Records (${filteredClients.size})",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (filteredClients.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No clients found matching filters.", color = Color(0xFF94A3B8), fontSize = 14.sp)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredClients) { client ->
                    val isSelected = selectedClientIds.contains(client.id)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedClientForDetail = client },
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = if (isSelected) Color(0xFF1E293B).copy(alpha = 0.9f) else Color(0xFF1E293B)),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) Color(0xFF38BDF8) else Color(0xFF334155)
                        )
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = { checked ->
                                            selectedClientIds = if (checked) selectedClientIds + client.id else selectedClientIds - client.id
                                        }
                                    )
                                    Text(
                                        text = client.businessName,
                                        fontSize = 15.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }

                                ClientStatusChip(status = client.currentStatus)
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(imageVector = Icons.Default.Phone, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(text = client.phone, fontSize = 12.sp, color = Color(0xFFCBD5E1))

                                    if (client.rating > 0.0) {
                                        Spacer(modifier = Modifier.width(10.dp))
                                        Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(2.dp))
                                        Text(text = "${client.rating} (${client.reviewCount})", fontSize = 12.sp, color = Color(0xFFF59E0B))
                                    }
                                }

                                if (client.followUpCount > 0) {
                                    FollowUpBadge(attemptNumber = client.followUpCount)
                                }
                            }

                            Spacer(modifier = Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Assigned: ${client.assignedEmployeeName ?: "Unassigned"}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF94A3B8)
                                )

                                Text(
                                    text = "Addr: ${client.address.take(24)}...",
                                    fontSize = 11.sp,
                                    color = Color(0xFF64748B)
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Detail Modal for Master Client Record
    if (selectedClientForDetail != null) {
        val client = selectedClientForDetail!!
        val context = LocalContext.current
        val clientCalls = callRecords.filter { it.clientId == client.id }
        val clientFollowUps = followUps.filter { it.clientId == client.id }

        AlertDialog(
            onDismissRequest = { selectedClientForDetail = null },
            title = {
                Column {
                    Text(client.businessName, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text("Category: ${client.category} • Added ${client.phone}", fontSize = 12.sp, color = Color(0xFF94A3B8))
                }
            },
            text = {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Address: ${client.address.ifBlank { "N/A" }}", fontSize = 12.sp, color = Color.White)
                                Text("Website: ${client.displayWebsite}", fontSize = 12.sp, color = Color(0xFF38BDF8))
                                Text("Assigned Employee: ${client.assignedEmployeeName ?: "Unassigned"}", fontSize = 12.sp, color = Color(0xFFCBD5E1))
                                Text("Follow-up Attempts: ${client.followUpCount} / 3", fontSize = 12.sp, color = Color(0xFFF59E0B))

                                if (client.mapsUrl.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(6.dp))
                                    TextButton(
                                        onClick = {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(client.mapsUrl))
                                            context.startActivity(intent)
                                        },
                                        contentPadding = PaddingValues(0.dp)
                                    ) {
                                        Icon(imageVector = Icons.Default.Place, contentDescription = null, modifier = Modifier.size(14.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text("Open Google Maps", fontSize = 12.sp, color = Color(0xFF38BDF8))
                                    }
                                }
                            }
                        }
                    }

                    item {
                        Text("Complete Interaction Timeline (${clientCalls.size} calls)", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    if (clientCalls.isEmpty()) {
                        item {
                            Text("No calls recorded yet.", fontSize = 12.sp, color = Color(0xFF64748B))
                        }
                    } else {
                        items(clientCalls) { call ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF0F172A),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text("${call.employeeName} • ${call.statusOutcome.name}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                                        Text("Attempt ${call.followUpNumber}/3", fontSize = 10.sp, color = Color(0xFF94A3B8))
                                    }
                                    if (call.notes.isNotBlank()) {
                                        Text("Note: ${call.notes}", fontSize = 11.sp, color = Color(0xFFCBD5E1))
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Row {
                    OutlinedButton(
                        onClick = {
                            onArchiveClient(client.id)
                            selectedClientForDetail = null
                        },
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text("Archive", color = Color(0xFFEF4444), fontSize = 12.sp)
                    }

                    Button(
                        onClick = {
                            showBulkReassignModal = true
                            selectedClientIds = setOf(client.id)
                            selectedClientForDetail = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8))
                    ) {
                        Text("Reassign", fontSize = 12.sp, color = Color(0xFF0F172A))
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { selectedClientForDetail = null }) {
                    Text("Close", color = Color(0xFF94A3B8))
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }

    // Bulk Reassign Modal
    if (showBulkReassignModal) {
        val activeEmployees = employees.filter { it.role == UserRole.EMPLOYEE && it.active }

        AlertDialog(
            onDismissRequest = { showBulkReassignModal = false },
            title = { Text("Reassign ${selectedClientIds.size} Client(s)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White) },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text("Select target employee:", fontSize = 12.sp, color = Color(0xFF94A3B8))
                    Spacer(modifier = Modifier.height(10.dp))

                    LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(activeEmployees) { emp ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF0F172A),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        onReassignClients(selectedClientIds.toList(), emp)
                                        showBulkReassignModal = false
                                        selectedClientIds = emptySet()
                                    }
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(emp.name, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                    Text(emp.email, fontSize = 11.sp, color = Color(0xFF94A3B8))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showBulkReassignModal = false }) {
                    Text("Cancel", color = Color(0xFF94A3B8))
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }
}
