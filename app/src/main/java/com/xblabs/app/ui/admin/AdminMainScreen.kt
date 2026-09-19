package com.xblabs.app.ui.admin

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
import com.xblabs.app.data.models.User
import com.xblabs.app.parser.ParsedLeadRecord
import com.xblabs.app.ui.components.NotificationCenterDialog
import com.xblabs.app.ui.theme.AdminCrmTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMainScreen(
    adminUser: User,
    repository: CrmRepository,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableStateOf("dashboard") }
    var showNotificationsModal by remember { mutableStateOf(false) }

    val clients by repository.clients.collectAsState()
    val employees by repository.users.collectAsState()
    val callRecords by repository.callRecords.collectAsState()
    val followUps by repository.followUps.collectAsState()
    val importBatches by repository.importBatches.collectAsState()
    val notifications by repository.notifications.collectAsState()
    val activityLogs by repository.activityLogs.collectAsState()

    val myNotifications = notifications.filter { it.userId == adminUser.id || it.userId == "ALL" }
    val unreadCount = myNotifications.count { !it.isRead }

    AdminCrmTheme {
        Scaffold(
            topBar = {
                Surface(
                    color = Color(0xFF1E293B),
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
                                    .background(Color(0xFF6366F1)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text("XB Labs CRM", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Admin • ${adminUser.name}", fontSize = 11.sp, color = Color(0xFF94A3B8))
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
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
                                    Icon(imageVector = Icons.Default.Notifications, contentDescription = "Notifications", tint = Color(0xFF38BDF8))
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
                    containerColor = Color(0xFF1E293B),
                    contentColor = Color(0xFF38BDF8)
                ) {
                    NavigationBarItem(
                        selected = selectedTab == "dashboard",
                        onClick = { selectedTab = "dashboard" },
                        icon = { Icon(Icons.Default.Dashboard, contentDescription = null) },
                        label = { Text("Dashboard", fontSize = 10.sp) }
                    )
                    NavigationBarItem(
                        selected = selectedTab == "import",
                        onClick = { selectedTab = "import" },
                        icon = { Icon(Icons.Default.UploadFile, contentDescription = null) },
                        label = { Text("Import", fontSize = 10.sp) }
                    )
                    NavigationBarItem(
                        selected = selectedTab == "clients",
                        onClick = { selectedTab = "clients" },
                        icon = { Icon(Icons.Default.BusinessCenter, contentDescription = null) },
                        label = { Text("Clients", fontSize = 10.sp) }
                    )
                    NavigationBarItem(
                        selected = selectedTab == "employees",
                        onClick = { selectedTab = "employees" },
                        icon = { Icon(Icons.Default.People, contentDescription = null) },
                        label = { Text("Team", fontSize = 10.sp) }
                    )
                    NavigationBarItem(
                        selected = selectedTab == "more",
                        onClick = { selectedTab = "more" },
                        icon = { Icon(Icons.Default.MoreHoriz, contentDescription = null) },
                        label = { Text("More", fontSize = 10.sp) }
                    )
                }
            },
            containerColor = Color(0xFF0F172A)
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (selectedTab) {
                    "dashboard" -> AdminDashboardScreen(
                        clients = clients,
                        employees = employees,
                        callRecords = callRecords,
                        followUps = followUps,
                        onNavigateTab = { target -> selectedTab = target }
                    )
                    "import" -> ImportJobsScreen(
                        adminUser = adminUser,
                        existingClients = clients,
                        onImportConfirmed = { records, source ->
                            repository.importLeads(adminUser, records, source)
                        }
                    )
                    "clients" -> AdminClientsScreen(
                        clients = clients,
                        employees = employees,
                        callRecords = callRecords,
                        followUps = followUps,
                        onReassignClients = { ids, emp -> repository.reassignClients(ids, emp, adminUser) },
                        onArchiveClient = { id -> repository.archiveClient(id, adminUser) }
                    )
                    "employees" -> AdminEmployeesScreen(
                        employees = employees,
                        clients = clients,
                        callRecords = callRecords,
                        followUps = followUps,
                        activityLogs = activityLogs,
                        onCreateEmployee = { name, email, uname, pass, theme ->
                            repository.createEmployee(name, email, uname, pass, theme)
                        },
                        onToggleActive = { empId, active, redist ->
                            repository.toggleEmployeeActiveStatus(empId, active, redist, adminUser)
                        }
                    )
                    "more" -> AdminMoreMenuScreen(
                        importBatches = importBatches,
                        activityLogs = activityLogs,
                        clients = clients,
                        callRecords = callRecords,
                        followUps = followUps
                    )
                }
            }
        }

        if (showNotificationsModal) {
            NotificationCenterDialog(
                notifications = myNotifications,
                onDismiss = { showNotificationsModal = false },
                onMarkAllRead = { repository.markAllNotificationsRead(adminUser.id) },
                onNotificationClick = { notif -> repository.markNotificationRead(notif.id) }
            )
        }
    }
}

@Composable
private fun AdminMoreMenuScreen(
    importBatches: List<com.xblabs.app.data.models.ImportBatch>,
    activityLogs: List<com.xblabs.app.data.models.ActivityLog>,
    clients: List<com.xblabs.app.data.models.Client>,
    callRecords: List<com.xblabs.app.data.models.CallRecord>,
    followUps: List<com.xblabs.app.data.models.FollowUp>
) {
    var subTab by remember { mutableStateOf("followups") }

    Column(modifier = Modifier.fillMaxSize()) {
        ScrollableTabRow(
            selectedTabIndex = when(subTab) {
                "followups" -> 0
                "interested" -> 1
                "history" -> 2
                "analytics" -> 3
                else -> 4
            },
            containerColor = Color(0xFF1E293B),
            contentColor = Color(0xFF38BDF8)
        ) {
            Tab(selected = subTab == "followups", onClick = { subTab = "followups" }, text = { Text("Follow-ups", fontSize = 11.sp) })
            Tab(selected = subTab == "interested", onClick = { subTab = "interested" }, text = { Text("Pipeline", fontSize = 11.sp) })
            Tab(selected = subTab == "history", onClick = { subTab = "history" }, text = { Text("Import History", fontSize = 11.sp) })
            Tab(selected = subTab == "analytics", onClick = { subTab = "analytics" }, text = { Text("Analytics", fontSize = 11.sp) })
            Tab(selected = subTab == "audit", onClick = { subTab = "audit" }, text = { Text("Audit Logs", fontSize = 11.sp) })
        }

        Box(modifier = Modifier.fillMaxSize()) {
            when (subTab) {
                "followups" -> AdminFollowUpsScreen(followUps = followUps)
                "interested" -> AdminInterestedPipelineScreen(clients = clients)
                "history" -> ImportHistoryScreen(importBatches = importBatches)
                "analytics" -> AdminAnalyticsScreen(clients = clients, callRecords = callRecords)
                "audit" -> AdminActivityLogScreen(activityLogs = activityLogs)
            }
        }
    }
}
