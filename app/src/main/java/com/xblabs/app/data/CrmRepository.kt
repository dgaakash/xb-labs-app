package com.xblabs.app.data

import android.content.Context
import com.xblabs.app.data.models.*
import com.xblabs.app.parser.ParsedLeadRecord
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class CrmRepository private constructor(context: Context) {

    private val dbHelper = CrmDatabaseHelper(context.applicationContext)
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()

    private val _clients = MutableStateFlow<List<Client>>(emptyList())
    val clients: StateFlow<List<Client>> = _clients.asStateFlow()

    private val _callRecords = MutableStateFlow<List<CallRecord>>(emptyList())
    val callRecords: StateFlow<List<CallRecord>> = _callRecords.asStateFlow()

    private val _followUps = MutableStateFlow<List<FollowUp>>(emptyList())
    val followUps: StateFlow<List<FollowUp>> = _followUps.asStateFlow()

    private val _importBatches = MutableStateFlow<List<ImportBatch>>(emptyList())
    val importBatches: StateFlow<List<ImportBatch>> = _importBatches.asStateFlow()

    private val _notifications = MutableStateFlow<List<Notification>>(emptyList())
    val notifications: StateFlow<List<Notification>> = _notifications.asStateFlow()

    private val _activityLogs = MutableStateFlow<List<ActivityLog>>(emptyList())
    val activityLogs: StateFlow<List<ActivityLog>> = _activityLogs.asStateFlow()

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    companion object {
        @Volatile
        private var INSTANCE: CrmRepository? = null

        fun getInstance(context: Context): CrmRepository {
            return INSTANCE ?: synchronized(this) {
                val instance = CrmRepository(context)
                INSTANCE = instance
                instance.init()
                instance
            }
        }
    }

    private fun init() {
        scope.launch {
            seedDefaultUsersIfEmpty()
            refreshAll()
        }
    }

    private fun seedDefaultUsersIfEmpty() {
        val existing = dbHelper.getAllUsers()
        if (existing.isEmpty()) {
            val admin = User(
                id = "usr_admin_xavier",
                name = "Xavier",
                email = "xavier@xblabs.com",
                username = "xavier",
                role = UserRole.ADMIN,
                passwordHash = "xblabs123@@@@",
                theme = "default"
            )
            val blessi = User(
                id = "usr_emp_blessi",
                name = "Blessi",
                email = "blessi@xblabs.com",
                username = "blessi",
                role = UserRole.EMPLOYEE,
                passwordHash = "xblabs123@",
                theme = "pink-princess"
            )
            dbHelper.insertUser(admin)
            dbHelper.insertUser(blessi)
        }
    }

    fun refreshAll() {
        _users.value = dbHelper.getAllUsers()
        _clients.value = dbHelper.getAllClients()
        _callRecords.value = dbHelper.getAllCallRecords()
        _followUps.value = dbHelper.getAllFollowUps()
        _importBatches.value = dbHelper.getAllImportBatches()
        _notifications.value = dbHelper.getAllNotifications()
        _activityLogs.value = dbHelper.getAllActivityLogs()
    }

    fun setCurrentUser(user: User?) {
        _currentUser.value = user
        if (user != null) {
            val updated = user.copy(lastActive = System.currentTimeMillis())
            dbHelper.updateUser(updated)
            refreshAll()
        }
    }

    fun authenticate(input: String, pass: String): User? {
        val trimmed = input.trim().lowercase()
        val found = _users.value.find { u ->
            (u.username.lowercase() == trimmed || u.email.lowercase() == trimmed) &&
                    u.passwordHash == pass && u.active
        }
        if (found != null) {
            setCurrentUser(found)
        }
        return found
    }

    // --- LEAD IMPORT & FAIR WORKLOAD BALANCING ---

    fun importLeads(admin: User, parsedRecords: List<ParsedLeadRecord>, sourceName: String = "Text Import"): ImportBatch {
        val batchId = "batch_" + UUID.randomUUID().toString().take(8)
        val validRecordsToImport = parsedRecords.filter { it.isValid && !it.isDuplicate }
        val activeEmployees = _users.value.filter { it.role == UserRole.EMPLOYEE && it.active }

        // Workload mapping: count current active jobs per employee
        val activeWorkloadMap = activeEmployees.associate { emp ->
            emp.id to _clients.value.count { c ->
                c.assignedEmployeeId == emp.id &&
                        c.employeeWorkflowStatus == WorkflowStatus.ACTIVE &&
                        c.currentStatus in listOf(ClientStatus.NEW, ClientStatus.IN_PROGRESS, ClientStatus.FOLLOW_UP)
            }
        }.toMutableMap()

        val assignedCounts = activeEmployees.associate { it.id to 0 }.toMutableMap()
        val importedClients = mutableListOf<Client>()

        val now = System.currentTimeMillis()

        for (rec in validRecordsToImport) {
            var assignedEmp: User? = null
            if (activeEmployees.isNotEmpty()) {
                // Priority: pick employee with lowest active workload
                val targetEmpId = activeWorkloadMap.minByOrNull { it.value }?.key
                assignedEmp = activeEmployees.find { it.id == targetEmpId }
                if (targetEmpId != null) {
                    activeWorkloadMap[targetEmpId] = (activeWorkloadMap[targetEmpId] ?: 0) + 1
                    assignedCounts[targetEmpId] = (assignedCounts[targetEmpId] ?: 0) + 1
                }
            }

            val client = Client(
                id = "cli_" + UUID.randomUUID().toString().take(8),
                businessName = rec.businessName,
                category = rec.category.ifBlank { "General" },
                rating = rec.rating,
                reviewCount = rec.reviewCount,
                phone = rec.phone,
                normalizedPhone = rec.normalizedPhone,
                website = rec.website,
                address = rec.address,
                mapsUrl = rec.mapsUrl,
                assignedEmployeeId = assignedEmp?.id,
                assignedEmployeeName = assignedEmp?.name,
                currentStatus = ClientStatus.NEW,
                employeeWorkflowStatus = WorkflowStatus.ACTIVE,
                priority = Priority.NORMAL,
                importedAt = now,
                createdAt = now,
                importBatchId = batchId
            )
            dbHelper.insertClient(client)
            importedClients.add(client)
        }

        // Distribution Summary text
        val summaryBuilder = StringBuilder()
        assignedCounts.forEach { (empId, count) ->
            val name = activeEmployees.find { it.id == empId }?.name ?: "Unknown"
            summaryBuilder.append("$name: $count clients; ")
        }

        val batch = ImportBatch(
            id = batchId,
            adminId = admin.id,
            adminName = admin.name,
            createdAt = now,
            sourceName = sourceName,
            totalRecords = parsedRecords.size,
            importedRecords = validRecordsToImport.size,
            duplicateRecords = parsedRecords.count { it.isDuplicate },
            invalidRecords = parsedRecords.count { !it.isValid },
            distributionSummary = summaryBuilder.toString().trimEnd(';', ' ')
        )
        dbHelper.insertImportBatch(batch)

        // Create Notifications for employees who received jobs
        assignedCounts.forEach { (empId, count) ->
            if (count > 0) {
                val notif = Notification(
                    id = "notif_" + UUID.randomUUID().toString().take(8),
                    userId = empId,
                    type = NotificationType.NEW_JOBS,
                    title = "New Clients Assigned",
                    message = "You have received $count new clients from batch '$sourceName'. Check your jobs queue!",
                    createdAt = now
                )
                dbHelper.insertNotification(notif)
            }
        }

        // Activity Log
        logActivity(
            user = admin,
            action = "IMPORT_LEADS",
            entityType = "ImportBatch",
            entityId = batchId,
            metadata = "Imported ${validRecordsToImport.size} records. Dist: ${summaryBuilder.toString()}"
        )

        refreshAll()
        return batch
    }

    // --- CALL RECORD & 3-FOLLOW-UP LIMIT LOGIC ---

    fun recordCallOutcome(
        client: Client,
        employee: User,
        outcome: CallStatusOutcome,
        notes: String,
        customFollowUpDate: Long?
    ): Client {
        val now = System.currentTimeMillis()
        val currentFollowUpNum = client.followUpCount

        val callRecord = CallRecord(
            id = "call_" + UUID.randomUUID().toString().take(8),
            clientId = client.id,
            clientName = client.businessName,
            employeeId = employee.id,
            employeeName = employee.name,
            statusOutcome = outcome,
            notes = notes,
            timestamp = now,
            followUpNumber = currentFollowUpNum
        )
        dbHelper.insertCallRecord(callRecord)

        var updatedClient: Client

        when (outcome) {
            CallStatusOutcome.DIDNT_PICK_UP, CallStatusOutcome.MAYBE_INTERESTED -> {
                val nextAttemptNum = currentFollowUpNum + 1

                if (nextAttemptNum <= 3) {
                    // Schedule Follow Up (attempt 1, 2, or 3)
                    val dueDate = customFollowUpDate ?: (now + 24 * 60 * 60 * 1000L) // Default tomorrow
                    val followUp = FollowUp(
                        id = "fol_" + UUID.randomUUID().toString().take(8),
                        clientId = client.id,
                        clientName = client.businessName,
                        clientPhone = client.phone,
                        employeeId = employee.id,
                        employeeName = employee.name,
                        attemptNumber = nextAttemptNum,
                        dueDate = dueDate,
                        state = FollowUpState.PENDING,
                        notes = notes,
                        previousResult = outcome.name,
                        createdAt = now
                    )
                    dbHelper.insertFollowUp(followUp)

                    updatedClient = client.copy(
                        currentStatus = ClientStatus.FOLLOW_UP,
                        employeeWorkflowStatus = WorkflowStatus.ACTIVE,
                        followUpCount = nextAttemptNum,
                        nextFollowUpDate = dueDate,
                        lastContactedAt = now
                    )
                } else {
                    // Exceeded 3 follow-ups limit!
                    // Exits employee active queue, but remains permanently visible in Admin master view!
                    updatedClient = client.copy(
                        currentStatus = ClientStatus.CLOSED,
                        employeeWorkflowStatus = WorkflowStatus.EXHAUSTED,
                        followUpCount = nextAttemptNum,
                        nextFollowUpDate = null,
                        lastContactedAt = now
                    )
                }
            }

            CallStatusOutcome.INTERESTED -> {
                updatedClient = client.copy(
                    currentStatus = ClientStatus.INTERESTED,
                    employeeWorkflowStatus = WorkflowStatus.ACTIVE,
                    pipelineStage = PipelineStage.INTERESTED,
                    priority = Priority.HIGH,
                    nextFollowUpDate = null,
                    lastContactedAt = now
                )
            }

            CallStatusOutcome.NOT_INTERESTED -> {
                updatedClient = client.copy(
                    currentStatus = ClientStatus.NOT_INTERESTED,
                    employeeWorkflowStatus = WorkflowStatus.CLOSED,
                    nextFollowUpDate = null,
                    lastContactedAt = now
                )
            }
        }

        dbHelper.updateClient(updatedClient)

        logActivity(
            user = employee,
            action = "CALL_OUTCOME_${outcome.name}",
            entityType = "Client",
            entityId = client.id,
            metadata = "Notes: $notes | Attempt: ${updatedClient.followUpCount}/3"
        )

        // Check if employee queue is empty after this action
        checkEmployeeQueueEmpty(employee)

        refreshAll()
        return updatedClient
    }

    private fun checkEmployeeQueueEmpty(employee: User) {
        val remainingActiveCount = dbHelper.getAllClients().count { c ->
            c.assignedEmployeeId == employee.id &&
                    c.employeeWorkflowStatus == WorkflowStatus.ACTIVE &&
                    c.currentStatus in listOf(ClientStatus.NEW, ClientStatus.IN_PROGRESS, ClientStatus.FOLLOW_UP)
        }

        if (remainingActiveCount == 0) {
            // Notify Admin about empty queue
            val adminUsers = _users.value.filter { it.role == UserRole.ADMIN }
            for (admin in adminUsers) {
                val notif = Notification(
                    id = "notif_" + UUID.randomUUID().toString().take(8),
                    userId = admin.id,
                    type = NotificationType.QUEUE_EMPTY,
                    title = "Employee Queue Empty",
                    message = "${employee.name} has completed all assigned jobs and currently has no remaining active clients. Consider importing or assigning additional clients.",
                    createdAt = System.currentTimeMillis()
                )
                dbHelper.insertNotification(notif)
            }
        }
    }

    // --- REASSIGNMENT & JOB MANAGEMENT ---

    fun reassignClients(clientIds: List<String>, targetEmployee: User, admin: User) {
        val now = System.currentTimeMillis()
        for (id in clientIds) {
            val client = _clients.value.find { it.id == id } ?: continue
            val oldEmpName = client.assignedEmployeeName ?: "Unassigned"
            val updated = client.copy(
                assignedEmployeeId = targetEmployee.id,
                assignedEmployeeName = targetEmployee.name,
                employeeWorkflowStatus = WorkflowStatus.ACTIVE
            )
            dbHelper.updateClient(updated)

            logActivity(
                user = admin,
                action = "REASSIGN_CLIENT",
                entityType = "Client",
                entityId = id,
                metadata = "Reassigned from $oldEmpName to ${targetEmployee.name}"
            )
        }

        // Notify target employee
        val notif = Notification(
            id = "notif_" + UUID.randomUUID().toString().take(8),
            userId = targetEmployee.id,
            type = NotificationType.REASSIGNMENT,
            title = "Jobs Reassigned",
            message = "Admin ${admin.name} reassigned ${clientIds.size} client(s) to your queue.",
            createdAt = now
        )
        dbHelper.insertNotification(notif)

        refreshAll()
    }

    fun createEmployee(name: String, email: String, username: String, pass: String, theme: String = "default"): User {
        val newUser = User(
            id = "usr_emp_" + UUID.randomUUID().toString().take(8),
            name = name,
            email = email,
            username = username,
            role = UserRole.EMPLOYEE,
            passwordHash = pass,
            theme = theme
        )
        dbHelper.insertUser(newUser)
        logActivity(_currentUser.value ?: newUser, "CREATE_EMPLOYEE", "User", newUser.id, "Created employee ${newUser.name}")
        refreshAll()
        return newUser
    }

    fun toggleEmployeeActiveStatus(employeeId: String, active: Boolean, redistributeJobs: Boolean, admin: User) {
        val emp = _users.value.find { it.id == employeeId } ?: return
        val updatedEmp = emp.copy(active = active)
        dbHelper.updateUser(updatedEmp)

        if (!active && redistributeJobs) {
            // Find active remaining employees
            val remainingEmps = _users.value.filter { it.id != employeeId && it.role == UserRole.EMPLOYEE && it.active }
            val empClientsToMove = _clients.value.filter { it.assignedEmployeeId == employeeId && it.employeeWorkflowStatus == WorkflowStatus.ACTIVE }

            if (remainingEmps.isNotEmpty() && empClientsToMove.isNotEmpty()) {
                val clientIdsToMove = empClientsToMove.map { it.id }
                // Distribute round-robin or lowest workload
                var empIdx = 0
                for (cid in clientIdsToMove) {
                    val target = remainingEmps[empIdx % remainingEmps.size]
                    reassignClients(listOf(cid), target, admin)
                    empIdx++
                }
            }
        }

        logActivity(admin, if (active) "ENABLE_EMPLOYEE" else "DISABLE_EMPLOYEE", "User", employeeId, "Active: $active")
        refreshAll()
    }

    fun archiveClient(clientId: String, admin: User) {
        val client = _clients.value.find { it.id == clientId } ?: return
        val updated = client.copy(
            currentStatus = ClientStatus.ARCHIVED,
            employeeWorkflowStatus = WorkflowStatus.CLOSED,
            archivedAt = System.currentTimeMillis()
        )
        dbHelper.updateClient(updated)
        logActivity(admin, "ARCHIVE_CLIENT", "Client", clientId, "Archived client ${client.businessName}")
        refreshAll()
    }

    fun markNotificationRead(id: String) {
        dbHelper.markNotificationAsRead(id)
        refreshAll()
    }

    fun markAllNotificationsRead(userId: String) {
        dbHelper.markAllNotificationsAsRead(userId)
        refreshAll()
    }

    private fun logActivity(user: User, action: String, entityType: String, entityId: String, metadata: String = "") {
        val log = ActivityLog(
            id = "log_" + UUID.randomUUID().toString().take(8),
            userId = user.id,
            userName = user.name,
            action = action,
            entityType = entityType,
            entityId = entityId,
            metadata = metadata,
            timestamp = System.currentTimeMillis()
        )
        dbHelper.insertActivityLog(log)
    }
}
