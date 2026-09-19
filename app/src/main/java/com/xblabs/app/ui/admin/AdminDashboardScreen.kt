package com.xblabs.app.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xblabs.app.data.models.*
import com.xblabs.app.ui.components.CrmMetricCard

@Composable
fun AdminDashboardScreen(
    clients: List<Client>,
    employees: List<User>,
    callRecords: List<CallRecord>,
    followUps: List<FollowUp>,
    onNavigateTab: (String) -> Unit
) {
    val totalClients = clients.size
    val unassignedClients = clients.count { !it.isAssigned }
    val activeJobs = clients.count { it.employeeWorkflowStatus == WorkflowStatus.ACTIVE && it.currentStatus in listOf(ClientStatus.NEW, ClientStatus.IN_PROGRESS, ClientStatus.FOLLOW_UP) }

    val now = System.currentTimeMillis()
    val startOfToday = now - (now % (24 * 60 * 60 * 1000L))

    val jobsCompletedToday = callRecords.count { it.timestamp >= startOfToday && it.statusOutcome in listOf(CallStatusOutcome.INTERESTED, CallStatusOutcome.NOT_INTERESTED) }
    val followUpsDueToday = followUps.count { it.state == FollowUpState.PENDING && it.dueDate >= startOfToday && it.dueDate < startOfToday + 86400000L }
    val followUpsOverdue = followUps.count { it.isOverdue }

    val interestedClients = clients.count { it.currentStatus == ClientStatus.INTERESTED }
    val maybeInterested = callRecords.count { it.statusOutcome == CallStatusOutcome.MAYBE_INTERESTED }
    val notInterested = clients.count { it.currentStatus == ClientStatus.NOT_INTERESTED }

    val totalCalls = callRecords.size
    val activeEmployees = employees.filter { it.role == UserRole.EMPLOYEE && it.active }
    val activeEmpCount = activeEmployees.size

    val employeesWithNoJobs = activeEmployees.count { emp ->
        clients.none { c -> c.assignedEmployeeId == emp.id && c.employeeWorkflowStatus == WorkflowStatus.ACTIVE }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Quick Action Bar
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Quick Operations",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Button(
                            onClick = { onNavigateTab("import") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).padding(end = 4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.UploadFile, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Import", fontSize = 12.sp)
                        }

                        Button(
                            onClick = { onNavigateTab("clients") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).padding(horizontal = 4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Group, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Clients", fontSize = 12.sp)
                        }

                        Button(
                            onClick = { onNavigateTab("employees") },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f).padding(start = 4.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Badge, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Team", fontSize = 12.sp)
                        }
                    }
                }
            }
        }

        // Metrics Grid Title
        item {
            Text(
                text = "Operational Overview",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        // 12 Cards Grid Layout
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CrmMetricCard("Total Clients", "$totalClients", Icons.Default.BusinessCenter, Color(0xFF38BDF8), modifier = Modifier.weight(1f))
                    CrmMetricCard("Unassigned", "$unassignedClients", Icons.Default.PersonOff, Color(0xFFF59E0B), modifier = Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CrmMetricCard("Active Jobs", "$activeJobs", Icons.Default.PlayArrow, Color(0xFF10B981), modifier = Modifier.weight(1f))
                    CrmMetricCard("Completed Today", "$jobsCompletedToday", Icons.Default.CheckCircle, Color(0xFF8B5CF6), modifier = Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CrmMetricCard("Follow-ups Due Today", "$followUpsDueToday", Icons.Default.Schedule, Color(0xFF38BDF8), modifier = Modifier.weight(1f))
                    CrmMetricCard("Follow-ups Overdue", "$followUpsOverdue", Icons.Default.Warning, Color(0xFFEF4444), modifier = Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CrmMetricCard("Interested Clients", "$interestedClients", Icons.Default.Star, Color(0xFF10B981), modifier = Modifier.weight(1f))
                    CrmMetricCard("Maybe Interested", "$maybeInterested", Icons.Default.HelpOutline, Color(0xFFF59E0B), modifier = Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CrmMetricCard("Not Interested", "$notInterested", Icons.Default.Cancel, Color(0xFF64748B), modifier = Modifier.weight(1f))
                    CrmMetricCard("Total Calls Made", "$totalCalls", Icons.Default.PhoneInTalk, Color(0xFF38BDF8), modifier = Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    CrmMetricCard("Active Employees", "$activeEmpCount", Icons.Default.People, Color(0xFF6366F1), modifier = Modifier.weight(1f))
                    CrmMetricCard("Employees No Jobs", "$employeesWithNoJobs", Icons.Default.HourglassEmpty, if (employeesWithNoJobs > 0) Color(0xFFEF4444) else Color(0xFF10B981), modifier = Modifier.weight(1f))
                }
            }
        }

        // Charts & Distribution Visualizers
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF334155))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Pipeline Lead Status Distribution",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (totalClients == 0) {
                        Text("No clients imported yet.", color = Color(0xFF94A3B8), fontSize = 13.sp)
                    } else {
                        val newCount = clients.count { it.currentStatus == ClientStatus.NEW }
                        val followUpCount = clients.count { it.currentStatus == ClientStatus.FOLLOW_UP }
                        val interestedCount = clients.count { it.currentStatus == ClientStatus.INTERESTED }
                        val closedCount = clients.count { it.currentStatus in listOf(ClientStatus.NOT_INTERESTED, ClientStatus.CLOSED) }

                        val newFrac = newCount.toFloat() / totalClients
                        val followFrac = followUpCount.toFloat() / totalClients
                        val intFrac = interestedCount.toFloat() / totalClients
                        val closedFrac = closedCount.toFloat() / totalClients

                        // Stacked horizontal distribution bar
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(20.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFF0F172A))
                        ) {
                            if (newFrac > 0) Box(modifier = Modifier.weight(newFrac).fillMaxHeight().background(Color(0xFF38BDF8)))
                            if (followFrac > 0) Box(modifier = Modifier.weight(followFrac).fillMaxHeight().background(Color(0xFFF59E0B)))
                            if (intFrac > 0) Box(modifier = Modifier.weight(intFrac).fillMaxHeight().background(Color(0xFF10B981)))
                            if (closedFrac > 0) Box(modifier = Modifier.weight(closedFrac).fillMaxHeight().background(Color(0xFF64748B)))
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Legend
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            LegendItem(color = Color(0xFF38BDF8), label = "New ($newCount)")
                            LegendItem(color = Color(0xFFF59E0B), label = "Follow-Up ($followUpCount)")
                            LegendItem(color = Color(0xFF10B981), label = "Interested ($interestedCount)")
                            LegendItem(color = Color(0xFF64748B), label = "Closed ($closedCount)")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, fontSize = 11.sp, color = Color(0xFF94A3B8))
    }
}
