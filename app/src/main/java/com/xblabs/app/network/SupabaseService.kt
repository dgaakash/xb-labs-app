package com.xblabs.app.network

import com.xblabs.app.data.models.*
import com.xblabs.app.settings.AppConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * Native Kotlin HTTP client for Supabase Postgrest API operations.
 */
class SupabaseService {

    private fun getHeaders(connection: HttpURLConnection, method: String, preferReturn: Boolean = false) {
        connection.requestMethod = method
        connection.connectTimeout = AppConfig.CONNECT_TIMEOUT_MS
        connection.readTimeout = AppConfig.READ_TIMEOUT_MS
        connection.setRequestProperty("apikey", AppConfig.SUPABASE_ANON_KEY)
        connection.setRequestProperty("Authorization", "Bearer ${AppConfig.SUPABASE_ANON_KEY}")
        connection.setRequestProperty("Accept", "application/json")
        connection.setRequestProperty("Content-Type", "application/json")
        if (preferReturn) {
            connection.setRequestProperty("Prefer", "return=representation")
        }
    }

    private fun executeGet(endpoint: String): String = try {
        val url = URL("${AppConfig.SUPABASE_URL}/rest/v1/$endpoint")
        val connection = (url.openConnection() as HttpURLConnection).apply {
            getHeaders(this, "GET")
        }
        val responseCode = connection.responseCode
        if (responseCode !in 200..299) {
            throw IllegalStateException("Supabase GET error $responseCode on $endpoint")
        }
        val reader = BufferedReader(InputStreamReader(connection.inputStream))
        reader.use { it.readText() }
    } catch (e: Exception) {
        "[]"
    }

    private fun executePost(endpoint: String, jsonBody: String): Boolean = try {
        val url = URL("${AppConfig.SUPABASE_URL}/rest/v1/$endpoint")
        val connection = (url.openConnection() as HttpURLConnection).apply {
            getHeaders(this, "POST", preferReturn = true)
            doOutput = true
        }
        OutputStreamWriter(connection.outputStream, "UTF-8").use { writer ->
            writer.write(jsonBody)
            writer.flush()
        }
        val responseCode = connection.responseCode
        responseCode in 200..299
    } catch (e: Exception) {
        false
    }

    private fun executePatch(endpoint: String, jsonBody: String): Boolean = try {
        val url = URL("${AppConfig.SUPABASE_URL}/rest/v1/$endpoint")
        val connection = (url.openConnection() as HttpURLConnection).apply {
            getHeaders(this, "PATCH")
            doOutput = true
        }
        OutputStreamWriter(connection.outputStream, "UTF-8").use { writer ->
            writer.write(jsonBody)
            writer.flush()
        }
        val responseCode = connection.responseCode
        responseCode in 200..299
    } catch (e: Exception) {
        false
    }

    // --- USERS TABLE ---

    suspend fun fetchUsers(): List<User> = withContext(Dispatchers.IO) {
        val jsonStr = executeGet("users?select=*&order=name.asc")
        val list = mutableListOf<User>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val obj = array.optJSONObject(i) ?: continue
                list.add(
                    User(
                        id = obj.optString("id"),
                        name = obj.optString("name"),
                        email = obj.optString("email"),
                        username = obj.optString("username"),
                        role = UserRole.valueOf(obj.optString("role", "EMPLOYEE")),
                        passwordHash = obj.optString("password_hash"),
                        theme = obj.optString("theme", "default"),
                        active = obj.optBoolean("active", true),
                        createdAt = obj.optLong("created_at", System.currentTimeMillis()),
                        lastActive = obj.optLong("last_active", System.currentTimeMillis())
                    )
                )
            }
        } catch (e: Exception) {}
        list
    }

    suspend fun insertUser(user: User): Boolean = withContext(Dispatchers.IO) {
        val obj = JSONObject().apply {
            put("id", user.id)
            put("name", user.name)
            put("email", user.email)
            put("username", user.username)
            put("role", user.role.name)
            put("password_hash", user.passwordHash)
            put("theme", user.theme)
            put("active", user.active)
            put("created_at", user.createdAt)
            put("last_active", user.lastActive)
        }
        executePost("users", obj.toString())
    }

    suspend fun updateUser(user: User): Boolean = withContext(Dispatchers.IO) {
        val obj = JSONObject().apply {
            put("name", user.name)
            put("email", user.email)
            put("username", user.username)
            put("role", user.role.name)
            put("password_hash", user.passwordHash)
            put("theme", user.theme)
            put("active", user.active)
            put("last_active", user.lastActive)
        }
        executePatch("users?id=eq.${user.id}", obj.toString())
    }

    // --- CLIENTS TABLE ---

    suspend fun fetchClients(): List<Client> = withContext(Dispatchers.IO) {
        val jsonStr = executeGet("clients?select=*&order=created_at.desc")
        val list = mutableListOf<Client>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val c = array.optJSONObject(i) ?: continue
                list.add(
                    Client(
                        id = c.optString("id"),
                        businessName = c.optString("business_name"),
                        category = c.optString("category", "General"),
                        rating = c.optDouble("rating", 0.0),
                        reviewCount = c.optInt("review_count", 0),
                        phone = c.optString("phone"),
                        normalizedPhone = c.optString("normalized_phone"),
                        website = c.optString("website"),
                        address = c.optString("address"),
                        mapsUrl = c.optString("maps_url"),
                        assignedEmployeeId = if (c.isNull("assigned_employee_id")) null else c.optString("assigned_employee_id"),
                        assignedEmployeeName = if (c.isNull("assigned_employee_name")) null else c.optString("assigned_employee_name"),
                        currentStatus = ClientStatus.valueOf(c.optString("current_status", "NEW")),
                        employeeWorkflowStatus = WorkflowStatus.valueOf(c.optString("employee_workflow_status", "ACTIVE")),
                        pipelineStage = PipelineStage.valueOf(c.optString("pipeline_stage", "NEW_LEAD")),
                        priority = Priority.valueOf(c.optString("priority", "NORMAL")),
                        followUpCount = c.optInt("follow_up_count", 0),
                        nextFollowUpDate = if (c.isNull("next_follow_up_date")) null else c.optLong("next_follow_up_date"),
                        lastContactedAt = if (c.isNull("last_contacted_at")) null else c.optLong("last_contacted_at"),
                        createdAt = c.optLong("created_at", System.currentTimeMillis()),
                        importedAt = c.optLong("imported_at", System.currentTimeMillis()),
                        importBatchId = c.optString("import_batch_id"),
                        archivedAt = if (c.isNull("archived_at")) null else c.optLong("archived_at")
                    )
                )
            }
        } catch (e: Exception) {}
        list
    }

    suspend fun insertClients(clients: List<Client>): Boolean = withContext(Dispatchers.IO) {
        val array = JSONArray()
        for (client in clients) {
            val c = JSONObject().apply {
                put("id", client.id)
                put("business_name", client.businessName)
                put("category", client.category)
                put("rating", client.rating)
                put("review_count", client.reviewCount)
                put("phone", client.phone)
                put("normalized_phone", client.normalizedPhone)
                put("website", client.website)
                put("address", client.address)
                put("maps_url", client.mapsUrl)
                put("assigned_employee_id", client.assignedEmployeeId)
                put("assigned_employee_name", client.assignedEmployeeName)
                put("current_status", client.currentStatus.name)
                put("employee_workflow_status", client.employeeWorkflowStatus.name)
                put("pipeline_stage", client.pipelineStage.name)
                put("priority", client.priority.name)
                put("follow_up_count", client.followUpCount)
                put("next_follow_up_date", client.nextFollowUpDate)
                put("last_contacted_at", client.lastContactedAt)
                put("created_at", client.createdAt)
                put("imported_at", client.importedAt)
                put("import_batch_id", client.importBatchId)
                put("archived_at", client.archivedAt)
            }
            array.put(c)
        }
        executePost("clients", array.toString())
    }

    suspend fun updateClient(client: Client): Boolean = withContext(Dispatchers.IO) {
        val c = JSONObject().apply {
            put("business_name", client.businessName)
            put("assigned_employee_id", client.assignedEmployeeId)
            put("assigned_employee_name", client.assignedEmployeeName)
            put("current_status", client.currentStatus.name)
            put("employee_workflow_status", client.employeeWorkflowStatus.name)
            put("pipeline_stage", client.pipelineStage.name)
            put("priority", client.priority.name)
            put("follow_up_count", client.followUpCount)
            put("next_follow_up_date", client.nextFollowUpDate)
            put("last_contacted_at", client.lastContactedAt)
            put("archived_at", client.archivedAt)
        }
        executePatch("clients?id=eq.${client.id}", c.toString())
    }

    // --- CALL RECORDS TABLE ---

    suspend fun fetchCallRecords(): List<CallRecord> = withContext(Dispatchers.IO) {
        val jsonStr = executeGet("call_records?select=*&order=timestamp.desc")
        val list = mutableListOf<CallRecord>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val cr = array.optJSONObject(i) ?: continue
                list.add(
                    CallRecord(
                        id = cr.optString("id"),
                        clientId = cr.optString("client_id"),
                        clientName = cr.optString("client_name"),
                        employeeId = cr.optString("employee_id"),
                        employeeName = cr.optString("employee_name"),
                        statusOutcome = CallStatusOutcome.valueOf(cr.optString("status_outcome")),
                        notes = cr.optString("notes"),
                        timestamp = cr.optLong("timestamp"),
                        followUpNumber = cr.optInt("follow_up_number", 0)
                    )
                )
            }
        } catch (e: Exception) {}
        list
    }

    suspend fun insertCallRecord(record: CallRecord): Boolean = withContext(Dispatchers.IO) {
        val cr = JSONObject().apply {
            put("id", record.id)
            put("client_id", record.clientId)
            put("client_name", record.clientName)
            put("employee_id", record.employeeId)
            put("employee_name", record.employeeName)
            put("status_outcome", record.statusOutcome.name)
            put("notes", record.notes)
            put("timestamp", record.timestamp)
            put("follow_up_number", record.followUpNumber)
        }
        executePost("call_records", cr.toString())
    }

    // --- FOLLOW UPS TABLE ---

    suspend fun fetchFollowUps(): List<FollowUp> = withContext(Dispatchers.IO) {
        val jsonStr = executeGet("follow_ups?select=*&order=due_date.asc")
        val list = mutableListOf<FollowUp>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val f = array.optJSONObject(i) ?: continue
                list.add(
                    FollowUp(
                        id = f.optString("id"),
                        clientId = f.optString("client_id"),
                        clientName = f.optString("client_name"),
                        clientPhone = f.optString("client_phone"),
                        employeeId = f.optString("employee_id"),
                        employeeName = f.optString("employee_name"),
                        attemptNumber = f.optInt("attempt_number", 1),
                        dueDate = f.optLong("due_date"),
                        state = FollowUpState.valueOf(f.optString("state", "PENDING")),
                        notes = f.optString("notes"),
                        previousResult = f.optString("previous_result"),
                        createdAt = f.optLong("created_at"),
                        completedAt = if (f.isNull("completed_at")) null else f.optLong("completed_at")
                    )
                )
            }
        } catch (e: Exception) {}
        list
    }

    suspend fun insertFollowUp(followUp: FollowUp): Boolean = withContext(Dispatchers.IO) {
        val f = JSONObject().apply {
            put("id", followUp.id)
            put("client_id", followUp.clientId)
            put("client_name", followUp.clientName)
            put("client_phone", followUp.clientPhone)
            put("employee_id", followUp.employeeId)
            put("employee_name", followUp.employeeName)
            put("attempt_number", followUp.attemptNumber)
            put("due_date", followUp.dueDate)
            put("state", followUp.state.name)
            put("notes", followUp.notes)
            put("previous_result", followUp.previousResult)
            put("created_at", followUp.createdAt)
            put("completed_at", followUp.completedAt)
        }
        executePost("follow_ups", f.toString())
    }

    // --- IMPORT BATCHES TABLE ---

    suspend fun fetchImportBatches(): List<ImportBatch> = withContext(Dispatchers.IO) {
        val jsonStr = executeGet("import_batches?select=*&order=created_at.desc")
        val list = mutableListOf<ImportBatch>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val b = array.optJSONObject(i) ?: continue
                list.add(
                    ImportBatch(
                        id = b.optString("id"),
                        adminId = b.optString("admin_id"),
                        adminName = b.optString("admin_name"),
                        createdAt = b.optLong("created_at"),
                        sourceName = b.optString("source_name"),
                        totalRecords = b.optInt("total_records"),
                        importedRecords = b.optInt("imported_records"),
                        duplicateRecords = b.optInt("duplicate_records"),
                        invalidRecords = b.optInt("invalid_records"),
                        distributionSummary = b.optString("distribution_summary")
                    )
                )
            }
        } catch (e: Exception) {}
        list
    }

    suspend fun insertImportBatch(batch: ImportBatch): Boolean = withContext(Dispatchers.IO) {
        val b = JSONObject().apply {
            put("id", batch.id)
            put("admin_id", batch.adminId)
            put("admin_name", batch.adminName)
            put("created_at", batch.createdAt)
            put("source_name", batch.sourceName)
            put("total_records", batch.totalRecords)
            put("imported_records", batch.importedRecords)
            put("duplicate_records", batch.duplicateRecords)
            put("invalid_records", batch.invalidRecords)
            put("distribution_summary", batch.distributionSummary)
        }
        executePost("import_batches", b.toString())
    }

    // --- NOTIFICATIONS TABLE ---

    suspend fun fetchNotifications(): List<Notification> = withContext(Dispatchers.IO) {
        val jsonStr = executeGet("notifications?select=*&order=created_at.desc")
        val list = mutableListOf<Notification>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val n = array.optJSONObject(i) ?: continue
                list.add(
                    Notification(
                        id = n.optString("id"),
                        userId = n.optString("user_id"),
                        type = NotificationType.valueOf(n.optString("type")),
                        title = n.optString("title"),
                        message = n.optString("message"),
                        isRead = n.optBoolean("is_read", false),
                        createdAt = n.optLong("created_at")
                    )
                )
            }
        } catch (e: Exception) {}
        list
    }

    suspend fun insertNotification(notification: Notification): Boolean = withContext(Dispatchers.IO) {
        val n = JSONObject().apply {
            put("id", notification.id)
            put("user_id", notification.userId)
            put("type", notification.type.name)
            put("title", notification.title)
            put("message", notification.message)
            put("is_read", notification.isRead)
            put("created_at", notification.createdAt)
        }
        executePost("notifications", n.toString())
    }

    suspend fun markNotificationRead(id: String): Boolean = withContext(Dispatchers.IO) {
        val body = JSONObject().apply { put("is_read", true) }
        executePatch("notifications?id=eq.$id", body.toString())
    }

    suspend fun markAllNotificationsRead(userId: String): Boolean = withContext(Dispatchers.IO) {
        val body = JSONObject().apply { put("is_read", true) }
        executePatch("notifications?user_id=eq.$userId", body.toString())
    }

    // --- ACTIVITY LOGS TABLE ---

    suspend fun fetchActivityLogs(): List<ActivityLog> = withContext(Dispatchers.IO) {
        val jsonStr = executeGet("activity_logs?select=*&order=timestamp.desc&limit=500")
        val list = mutableListOf<ActivityLog>()
        try {
            val array = JSONArray(jsonStr)
            for (i in 0 until array.length()) {
                val l = array.optJSONObject(i) ?: continue
                list.add(
                    ActivityLog(
                        id = l.optString("id"),
                        userId = l.optString("user_id"),
                        userName = l.optString("user_name"),
                        action = l.optString("action"),
                        entityType = l.optString("entity_type"),
                        entityId = l.optString("entity_id"),
                        metadata = l.optString("metadata"),
                        timestamp = l.optLong("timestamp")
                    )
                )
            }
        } catch (e: Exception) {}
        list
    }

    suspend fun insertActivityLog(log: ActivityLog): Boolean = withContext(Dispatchers.IO) {
        val l = JSONObject().apply {
            put("id", log.id)
            put("user_id", log.userId)
            put("user_name", log.userName)
            put("action", log.action)
            put("entity_type", log.entityType)
            put("entity_id", log.entityId)
            put("metadata", log.metadata)
            put("timestamp", log.timestamp)
        }
        executePost("activity_logs", l.toString())
    }
}
