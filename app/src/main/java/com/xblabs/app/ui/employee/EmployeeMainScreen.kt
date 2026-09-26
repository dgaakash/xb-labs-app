package com.xblabs.app.ui.employee

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
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
import com.xblabs.app.data.CrmRepository
import com.xblabs.app.data.models.CallStatusOutcome
import com.xblabs.app.data.models.Client
import com.xblabs.app.data.models.User
import com.xblabs.app.ui.components.NotificationCenterDialog
import com.xblabs.app.ui.theme.EmployeeCrmTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmployeeMainScreen(
    employee: User,
    repository: CrmRepository,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableStateOf("dashboard") }
    var activeJobForCall by remember { mutableStateOf<Client?>(null) }
    var showNotificationsModal by remember { mutableStateOf(false) }

    val clients by repository.clients.collectAsState()
    val callRecords by repository.callRecords.collectAsState()
    val followUps by repository.followUps.collectAsState()
    val notifications by repository.notifications.collectAsState()

    val isSyncing by repository.isSyncing.collectAsState()
    val myNotifications = notifications.filter { it.userId == employee.id || it.userId == "ALL" }
    val unreadCount = myNotifications.count { !it.isRead }

    val primaryColor = if (employee.isPinkPrincess) Color(0xFFEC4899) else Color(0xFF38BDF8)
    val navBg = if (employee.isPinkPrincess) Color(0xFF2A152A) else Color(0xFF1E293B)
    val appBg = if (employee.isPinkPrincess) Color(0xFF180E19) else Color(0xFF0F172A)

    EmployeeCrmTheme(isPinkPrincess = employee.isPinkPrincess) {
        if (activeJobForCall != null) {
            JobDetailCallScreen(
                client = activeJobForCall!!,
                employee = employee,
                callRecords = callRecords,
                onBack = { activeJobForCall = null },
                onSubmitOutcome = { outcome, notes, customDate ->
                    repository.recordCallOutcome(activeJobForCall!!, employee, outcome, notes, customDate)
                    activeJobForCall = null
                }
            )
        } else {
            Scaffold(
                topBar = {
                    Surface(
                        color = navBg,
                        shadowElevation = 4.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(primaryColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (employee.isPinkPrincess) Icons.Default.Favorite else Icons.Default.Person,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(employee.name, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                        if (employee.isPinkPrincess) {
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("✨", fontSize = 12.sp)
                                        }
                                    }
                                    Text("XB Labs Sales Workspace", fontSize = 11.sp, color = Color(0xFF94A3B8))
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(onClick = { repository.triggerRemoteSync() }) {
                                    if (isSyncing) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(20.dp),
                                            color = primaryColor,
                                            strokeWidth = 2.dp
                                        )
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.Sync,
                                            contentDescription = "Sync Data",
                                            tint = primaryColor
                                        )
                                    }
                                }

                                IconButton(onClick = { showNotificationsModal = true }) {
                                    BadgedBox(
                                        badge = {
                                            if (unreadCount > 0) {
                                                Badge(containerColor = Color(0xFFEF4444)) {
                                                    Text("$unreadCount", color = Color.White)
                                                }
                                            }
                                        }
                                    ) {
                                        Icon(imageVector = Icons.Default.Notifications, contentDescription = "Notifications", tint = primaryColor)
                                    }
                                }

                                IconButton(onClick = onLogout) {
                                    Icon(imageVector = Icons.Default.Logout, contentDescription = "Sign Out", tint = Color(0xFFF87171))
                                }
                            }
                        }
                    }
                },
                bottomBar = {
                    NavigationBar(
                        containerColor = navBg,
                        contentColor = primaryColor
                    ) {
                        NavigationBarItem(
                            selected = selectedTab == "dashboard",
                            onClick = { selectedTab = "dashboard" },
                            icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                            label = { Text("Home", fontSize = 10.sp) }
                        )
                        NavigationBarItem(
                            selected = selectedTab == "jobs",
                            onClick = { selectedTab = "jobs" },
                            icon = { Icon(Icons.Default.Work, contentDescription = null) },
                            label = { Text("My Jobs", fontSize = 10.sp) }
                        )
                        NavigationBarItem(
                            selected = selectedTab == "followups",
                            onClick = { selectedTab = "followups" },
                            icon = { Icon(Icons.Default.Schedule, contentDescription = null) },
                            label = { Text("Follow-ups", fontSize = 10.sp) }
                        )
                        NavigationBarItem(
                            selected = selectedTab == "interested",
                            onClick = { selectedTab = "interested" },
                            icon = { Icon(Icons.Default.Star, contentDescription = null) },
                            label = { Text("Interested", fontSize = 10.sp) }
                        )
                        NavigationBarItem(
                            selected = selectedTab == "stats",
                            onClick = { selectedTab = "stats" },
                            icon = { Icon(Icons.Default.Assessment, contentDescription = null) },
                            label = { Text("Stats", fontSize = 10.sp) }
                        )
                    }
                },
                containerColor = appBg
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    when (selectedTab) {
                        "dashboard" -> EmployeeDashboardScreen(
                            employee = employee,
                            clients = clients,
                            callRecords = callRecords,
                            followUps = followUps,
                            onStartNextJob = { client -> activeJobForCall = client },
                            onNavigateTab = { target -> selectedTab = target }
                        )
                        "jobs" -> EmployeeJobsScreen(
                            employee = employee,
                            clients = clients,
                            onSelectJob = { client -> activeJobForCall = client }
                        )
                        "followups" -> EmployeeFollowUpsScreen(
                            employee = employee,
                            clients = clients,
                            followUps = followUps,
                            onSelectClient = { client -> activeJobForCall = client }
                        )
                        "interested" -> EmployeeInterestedPipelineScreen(
                            employee = employee,
                            clients = clients,
                            onSelectClient = { client -> activeJobForCall = client }
                        )
                        "stats" -> EmployeeStatsScreen(
                            employee = employee,
                            clients = clients,
                            callRecords = callRecords
                        )
                    }
                }
            }
        }

        if (showNotificationsModal) {
            NotificationCenterDialog(
                notifications = myNotifications,
                onDismiss = { showNotificationsModal = false },
                onMarkAllRead = { repository.markAllNotificationsRead(employee.id) },
                onNotificationClick = { notif -> repository.markNotificationRead(notif.id) }
            )
        }
    }
}
