package com.xblabs.app.ui.admin

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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xblabs.app.data.models.*

@Composable
fun AdminEmployeesScreen(
    employees: List<User>,
    clients: List<Client>,
    callRecords: List<CallRecord>,
    followUps: List<FollowUp>,
    activityLogs: List<ActivityLog>,
    onCreateEmployee: (String, String, String, String, String) -> Unit,
    onToggleActive: (String, Boolean, Boolean) -> Unit
) {
    var showAddEmployeeModal by remember { mutableStateOf(false) }
    var selectedEmpForDetail by remember { mutableStateOf<User?>(null) }
    var showDisableConfirmModal by remember { mutableStateOf<User?>(null) }

    val activeEmployees = employees.filter { it.role == UserRole.EMPLOYEE }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("Employee Monitoring", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("${activeEmployees.size} team members", fontSize = 12.sp, color = Color(0xFF94A3B8))
            }

            Button(
                onClick = { showAddEmployeeModal = true },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(imageVector = Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp), tint = Color(0xFF0F172A))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Employee", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (activeEmployees.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("No employees created yet.", color = Color(0xFF94A3B8), fontSize = 14.sp)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(activeEmployees) { emp ->
                    val empClients = clients.filter { it.assignedEmployeeId == emp.id }
                    val activePendingCount = empClients.count { it.employeeWorkflowStatus == WorkflowStatus.ACTIVE && it.currentStatus in listOf(ClientStatus.NEW, ClientStatus.IN_PROGRESS, ClientStatus.FOLLOW_UP) }
                    val empCalls = callRecords.filter { it.employeeId == emp.id }

                    val now = System.currentTimeMillis()
                    val startOfToday = now - (now % (24 * 60 * 60 * 1000L))
                    val callsToday = empCalls.count { it.timestamp >= startOfToday }

                    val interestedCount = empCalls.count { it.statusOutcome == CallStatusOutcome.INTERESTED }
                    val notInterestedCount = empCalls.count { it.statusOutcome == CallStatusOutcome.NOT_INTERESTED }

                    val isQueueEmpty = activePendingCount == 0 && empClients.isNotEmpty()

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { selectedEmpForDetail = emp },
                        shape = RoundedCornerShape(18.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                        border = androidx.compose.foundation.BorderStroke(1.dp, if (isQueueEmpty) Color(0xFFEF4444) else Color(0xFF334155))
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(44.dp)
                                            .clip(CircleShape)
                                            .background(emp.displayColor.copy(alpha = 0.2f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (emp.isPinkPrincess) Icons.Default.Favorite else Icons.Default.Person,
                                            contentDescription = null,
                                            tint = emp.displayColor,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(emp.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                            if (emp.isPinkPrincess) {
                                                Spacer(modifier = Modifier.width(6.dp))
                                                Text("✨", fontSize = 12.sp)
                                            }
                                        }
                                        Text(emp.email, fontSize = 12.sp, color = Color(0xFF94A3B8))
                                    }
                                }

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    if (isQueueEmpty) {
                                        Surface(
                                            shape = RoundedCornerShape(50),
                                            color = Color(0xFFEF4444).copy(alpha = 0.2f)
                                        ) {
                                            Text("Queue Empty", fontSize = 10.sp, color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp))
                                        }
                                        Spacer(modifier = Modifier.width(8.dp))
                                    }

                                    Switch(
                                        checked = emp.active,
                                        onCheckedChange = { active ->
                                            if (!active) {
                                                showDisableConfirmModal = emp
                                            } else {
                                                onToggleActive(emp.id, true, false)
                                            }
                                        }
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            // Stats Row
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(Color(0xFF0F172A), RoundedCornerShape(12.dp))
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceAround
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Active Jobs", fontSize = 10.sp, color = Color(0xFF94A3B8))
                                    Text("$activePendingCount", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Calls Today", fontSize = 10.sp, color = Color(0xFF94A3B8))
                                    Text("$callsToday", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFF10B981))
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Interested", fontSize = 10.sp, color = Color(0xFF94A3B8))
                                    Text("$interestedCount", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                                }
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Total Calls", fontSize = 10.sp, color = Color(0xFF94A3B8))
                                    Text("${empCalls.size}", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Add Employee Dialog
    if (showAddEmployeeModal) {
        var nameInput by remember { mutableStateOf("") }
        var emailInput by remember { mutableStateOf("") }
        var usernameInput by remember { mutableStateOf("") }
        var passwordInput by remember { mutableStateOf("") }
        var themeInput by remember { mutableStateOf("default") }

        AlertDialog(
            onDismissRequest = { showAddEmployeeModal = false },
            title = { Text("Add New Employee", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White) },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedTextField(
                        value = nameInput,
                        onValueChange = { nameInput = it },
                        label = { Text("Full Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = emailInput,
                        onValueChange = { emailInput = it },
                        label = { Text("Email Address") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = usernameInput,
                        onValueChange = { usernameInput = it },
                        label = { Text("Username") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = passwordInput,
                        onValueChange = { passwordInput = it },
                        label = { Text("Password") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = themeInput == "pink-princess",
                            onCheckedChange = { themeInput = if (it) "pink-princess" else "default" }
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Pink Princess Theme ✨", fontSize = 13.sp, color = Color.White)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nameInput.isNotBlank() && usernameInput.isNotBlank() && passwordInput.isNotBlank()) {
                            onCreateEmployee(nameInput.trim(), emailInput.trim(), usernameInput.trim(), passwordInput.trim(), themeInput)
                            showAddEmployeeModal = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8))
                ) {
                    Text("Create Employee", color = Color(0xFF0F172A), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddEmployeeModal = false }) {
                    Text("Cancel")
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }

    // Disable Employee Confirm Dialog
    if (showDisableConfirmModal != null) {
        val emp = showDisableConfirmModal!!
        var redistribute by remember { mutableStateOf(true) }

        AlertDialog(
            onDismissRequest = { showDisableConfirmModal = null },
            title = { Text("Disable ${emp.name}?", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White) },
            text = {
                Column {
                    Text("Disabled employees cannot log in or receive new clients.", fontSize = 13.sp, color = Color(0xFF94A3B8))
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = redistribute, onCheckedChange = { redistribute = it })
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Automatically redistribute active jobs to remaining active team members", fontSize = 12.sp, color = Color.White)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onToggleActive(emp.id, false, redistribute)
                        showDisableConfirmModal = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Disable Account", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDisableConfirmModal = null }) {
                    Text("Cancel")
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }

    // Detailed Employee Profile Modal
    if (selectedEmpForDetail != null) {
        val emp = selectedEmpForDetail!!
        val empCalls = callRecords.filter { it.employeeId == emp.id }
        val empClients = clients.filter { it.assignedEmployeeId == emp.id }
        val empFollowUps = followUps.filter { it.employeeId == emp.id }

        AlertDialog(
            onDismissRequest = { selectedEmpForDetail = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Badge, contentDescription = null, tint = emp.displayColor)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("${emp.name}'s Operational Profile", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
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
                        Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text("Email: ${emp.email}", fontSize = 12.sp, color = Color.White)
                                Text("Username: ${emp.username}", fontSize = 12.sp, color = Color(0xFFCBD5E1))
                                Text("Assigned Jobs Total: ${empClients.size}", fontSize = 12.sp, color = Color(0xFF38BDF8))
                                Text("Call Actions Recorded: ${empCalls.size}", fontSize = 12.sp, color = Color(0xFF10B981))
                            }
                        }
                    }

                    item {
                        Text("Recent Call Actions History", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    }

                    if (empCalls.isEmpty()) {
                        item { Text("No call actions performed yet.", fontSize = 12.sp, color = Color(0xFF94A3B8)) }
                    } else {
                        items(empCalls.take(10)) { call ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = Color(0xFF0F172A),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(modifier = Modifier.padding(8.dp)) {
                                    Text("${call.clientName} • ${call.statusOutcome.name}", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF38BDF8))
                                    if (call.notes.isNotBlank()) Text(call.notes, fontSize = 11.sp, color = Color(0xFFCBD5E1))
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { selectedEmpForDetail = null }) {
                    Text("Close", color = Color(0xFF38BDF8))
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }
}
