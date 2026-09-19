package com.xblabs.app.ui.employee

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.xblabs.app.data.models.*
import com.xblabs.app.ui.components.CrmMetricCard

@Composable
fun EmployeeDashboardScreen(
    employee: User,
    clients: List<Client>,
    callRecords: List<CallRecord>,
    followUps: List<FollowUp>,
    onStartNextJob: (Client) -> Unit,
    onNavigateTab: (String) -> Unit
) {
    val myClients = clients.filter { it.assignedEmployeeId == employee.id }
    val myActivePendingJobs = myClients.filter {
        it.employeeWorkflowStatus == WorkflowStatus.ACTIVE &&
                it.currentStatus in listOf(ClientStatus.NEW, ClientStatus.IN_PROGRESS, ClientStatus.FOLLOW_UP)
    }

    val myCalls = callRecords.filter { it.employeeId == employee.id }
    val now = System.currentTimeMillis()
    val startOfToday = now - (now % (24 * 60 * 60 * 1000L))
    val callsToday = myCalls.count { it.timestamp >= startOfToday }

    val myFollowUps = followUps.filter { it.employeeId == employee.id && it.state == FollowUpState.PENDING }
    val followUpsDueToday = myFollowUps.count { it.dueDate >= startOfToday && it.dueDate < startOfToday + 86400000L }
    val overdueFollowUps = myFollowUps.count { it.isOverdue }

    val interestedCount = myClients.count { it.currentStatus == ClientStatus.INTERESTED }

    // Smart priority selector for "Start Next Job"
    val nextJob = rememberNextJob(myActivePendingJobs, myFollowUps)

    val primaryColor = if (employee.isPinkPrincess) Color(0xFFEC4899) else Color(0xFF38BDF8)
    val cardBg = if (employee.isPinkPrincess) Color(0xFF2A152A) else Color(0xFF1E293B)
    val cardBorder = if (employee.isPinkPrincess) Color(0xFF4A204B) else Color(0xFF334155)

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Action: Start Next Job
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = androidx.compose.foundation.BorderStroke(1.dp, primaryColor.copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (employee.isPinkPrincess) "Ready to Shine! ✨" else "Sales Action Center",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = primaryColor
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (myActivePendingJobs.isEmpty()) "You're all caught up!" else "${myActivePendingJobs.size} Jobs Pending",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    if (nextJob != null) {
                        Button(
                            onClick = { onStartNextJob(nextJob) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = primaryColor)
                        ) {
                            Icon(imageVector = Icons.Default.PhoneInTalk, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Start Next Job (${nextJob.businessName})",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(50),
                            color = primaryColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = if (employee.isPinkPrincess) "No active jobs in your queue ✨" else "No active jobs assigned.",
                                fontSize = 13.sp,
                                color = primaryColor,
                                fontWeight = FontWeight.Medium,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }

        // Action Metrics Grid
        item {
            Text(
                text = "My Activity & Workload",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }

        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CrmMetricCard("Jobs Remaining", "${myActivePendingJobs.size}", Icons.Default.Work, primaryColor, modifier = Modifier.weight(1f)) {
                        onNavigateTab("jobs")
                    }
                    CrmMetricCard("Calls Today", "$callsToday", Icons.Default.Phone, Color(0xFF10B981), modifier = Modifier.weight(1f))
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CrmMetricCard("Follow-ups Due", "$followUpsDueToday", Icons.Default.Schedule, Color(0xFFF59E0B), modifier = Modifier.weight(1f)) {
                        onNavigateTab("followups")
                    }
                    CrmMetricCard("Overdue Alerts", "$overdueFollowUps", Icons.Default.Warning, Color(0xFFEF4444), modifier = Modifier.weight(1f)) {
                        onNavigateTab("followups")
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    CrmMetricCard("Interested Prospects", "$interestedCount", Icons.Default.Star, Color(0xFF10B981), modifier = Modifier.weight(1f)) {
                        onNavigateTab("interested")
                    }
                    CrmMetricCard("Total Calls Made", "${myCalls.size}", Icons.Default.CheckCircle, primaryColor, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

private fun rememberNextJob(myActiveJobs: List<Client>, pendingFollowUps: List<FollowUp>): Client? {
    if (myActiveJobs.isEmpty()) return null

    // Priority 1: Overdue follow-ups
    val overdueClientId = pendingFollowUps.firstOrNull { it.isOverdue }?.clientId
    val overdueClient = myActiveJobs.find { it.id == overdueClientId }
    if (overdueClient != null) return overdueClient

    // Priority 2: Due today follow-up
    val now = System.currentTimeMillis()
    val dueTodayClientId = pendingFollowUps.firstOrNull { it.dueDate <= now + 86400000L }?.clientId
    val dueTodayClient = myActiveJobs.find { it.id == dueTodayClientId }
    if (dueTodayClient != null) return dueTodayClient

    // Priority 3: High priority / Interested
    val highPri = myActiveJobs.find { it.priority == Priority.HIGH }
    if (highPri != null) return highPri

    // Priority 4: First pending job
    return myActiveJobs.firstOrNull()
}
