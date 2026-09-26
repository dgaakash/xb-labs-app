package com.xblabs.app.network

import com.xblabs.app.data.models.*
import org.json.JSONArray
import org.json.JSONObject

/**
 * Data class representing the server update payload response.
 */
data class UpdateResponse(
    val latestVersionCode: Long,
    val latestVersionName: String,
    val minimumSupportedVersionCode: Long,
    val forceUpdate: Boolean,
    val apkUrl: String,
    val sha256: String,
    val fileSize: Long,
    val releaseNotes: List<String>,
    val message: String
) {
    companion object {
        fun fromJson(jsonStr: String): UpdateResponse {
            val obj = JSONObject(jsonStr)
            val notes = mutableListOf<String>()
            val notesArray = obj.optJSONArray("releaseNotes")
            if (notesArray != null) {
                for (i in 0 until notesArray.length()) {
                    notes.add(notesArray.getString(i))
                }
            }
            return UpdateResponse(
                latestVersionCode = obj.optLong("latestVersionCode", 0L),
                latestVersionName = obj.optString("latestVersionName", ""),
                minimumSupportedVersionCode = obj.optLong("minimumSupportedVersionCode", 0L),
                forceUpdate = obj.optBoolean("forceUpdate", false),
                apkUrl = obj.optString("apkUrl", ""),
                sha256 = obj.optString("sha256", ""),
                fileSize = obj.optLong("fileSize", 0L),
                releaseNotes = notes,
                message = obj.optString("message", "")
            )
        }
    }
}

/**
 * Payload representing synchronized CRM data across devices.
 */
data class CrmSyncPayload(
    val users: List<User> = emptyList(),
    val clients: List<Client> = emptyList(),
    val callRecords: List<CallRecord> = emptyList(),
    val followUps: List<FollowUp> = emptyList(),
    val importBatches: List<ImportBatch> = emptyList(),
    val notifications: List<Notification> = emptyList(),
    val activityLogs: List<ActivityLog> = emptyList()
) {
    companion object {
        fun fromJson(jsonStr: String): CrmSyncPayload {
            val obj = JSONObject(jsonStr)

            val userList = mutableListOf<User>()
            val usersArray = obj.optJSONArray("users")
            if (usersArray != null) {
                for (i in 0 until usersArray.length()) {
                    val u = usersArray.optJSONObject(i) ?: continue
                    userList.add(
                        User(
                            id = u.optString("id"),
                            name = u.optString("name"),
                            email = u.optString("email"),
                            username = u.optString("username"),
                            role = UserRole.valueOf(u.optString("role", "EMPLOYEE")),
                            passwordHash = u.optString("passwordHash"),
                            theme = u.optString("theme", "default"),
                            active = u.optBoolean("active", true),
                            createdAt = u.optLong("createdAt", System.currentTimeMillis()),
                            lastActive = u.optLong("lastActive", System.currentTimeMillis())
                        )
                    )
                }
            }

            val clientList = mutableListOf<Client>()
            val clientsArray = obj.optJSONArray("clients")
            if (clientsArray != null) {
                for (i in 0 until clientsArray.length()) {
                    val c = clientsArray.optJSONObject(i) ?: continue
                    clientList.add(
                        Client(
                            id = c.optString("id"),
                            businessName = c.optString("businessName"),
                            category = c.optString("category", "General"),
                            rating = c.optDouble("rating", 0.0),
                            reviewCount = c.optInt("reviewCount", 0),
                            phone = c.optString("phone"),
                            normalizedPhone = c.optString("normalizedPhone"),
                            website = c.optString("website"),
                            address = c.optString("address"),
                            mapsUrl = c.optString("mapsUrl"),
                            assignedEmployeeId = if (c.isNull("assignedEmployeeId")) null else c.optString("assignedEmployeeId"),
                            assignedEmployeeName = if (c.isNull("assignedEmployeeName")) null else c.optString("assignedEmployeeName"),
                            currentStatus = ClientStatus.valueOf(c.optString("currentStatus", "NEW")),
                            employeeWorkflowStatus = WorkflowStatus.valueOf(c.optString("employeeWorkflowStatus", "ACTIVE")),
                            pipelineStage = PipelineStage.valueOf(c.optString("pipelineStage", "NEW_LEAD")),
                            priority = Priority.valueOf(c.optString("priority", "NORMAL")),
                            followUpCount = c.optInt("followUpCount", 0),
                            nextFollowUpDate = if (c.isNull("nextFollowUpDate")) null else c.optLong("nextFollowUpDate"),
                            lastContactedAt = if (c.isNull("lastContactedAt")) null else c.optLong("lastContactedAt"),
                            createdAt = c.optLong("createdAt", System.currentTimeMillis()),
                            importedAt = c.optLong("importedAt", System.currentTimeMillis()),
                            importBatchId = c.optString("importBatchId"),
                            archivedAt = if (c.isNull("archivedAt")) null else c.optLong("archivedAt")
                        )
                    )
                }
            }

            val callRecordList = mutableListOf<CallRecord>()
            val callsArray = obj.optJSONArray("callRecords")
            if (callsArray != null) {
                for (i in 0 until callsArray.length()) {
                    val cr = callsArray.optJSONObject(i) ?: continue
                    callRecordList.add(
                        CallRecord(
                            id = cr.optString("id"),
                            clientId = cr.optString("clientId"),
                            clientName = cr.optString("clientName"),
                            employeeId = cr.optString("employeeId"),
                            employeeName = cr.optString("employeeName"),
                            statusOutcome = CallStatusOutcome.valueOf(cr.optString("statusOutcome")),
                            notes = cr.optString("notes"),
                            timestamp = cr.optLong("timestamp"),
                            followUpNumber = cr.optInt("followUpNumber", 0)
                        )
                    )
                }
            }

            val followUpList = mutableListOf<FollowUp>()
            val followArray = obj.optJSONArray("followUps")
            if (followArray != null) {
                for (i in 0 until followArray.length()) {
                    val f = followArray.optJSONObject(i) ?: continue
                    followUpList.add(
                        FollowUp(
                            id = f.optString("id"),
                            clientId = f.optString("clientId"),
                            clientName = f.optString("clientName"),
                            clientPhone = f.optString("clientPhone"),
                            employeeId = f.optString("employeeId"),
                            employeeName = f.optString("employeeName"),
                            attemptNumber = f.optInt("attemptNumber", 1),
                            dueDate = f.optLong("dueDate"),
                            state = FollowUpState.valueOf(f.optString("state", "PENDING")),
                            notes = f.optString("notes"),
                            previousResult = f.optString("previousResult"),
                            createdAt = f.optLong("createdAt"),
                            completedAt = if (f.isNull("completedAt")) null else f.optLong("completedAt")
                        )
                    )
                }
            }

            val batchList = mutableListOf<ImportBatch>()
            val batchArray = obj.optJSONArray("importBatches")
            if (batchArray != null) {
                for (i in 0 until batchArray.length()) {
                    val b = batchArray.optJSONObject(i) ?: continue
                    batchList.add(
                        ImportBatch(
                            id = b.optString("id"),
                            adminId = b.optString("adminId"),
                            adminName = b.optString("adminName"),
                            createdAt = b.optLong("createdAt"),
                            sourceName = b.optString("sourceName"),
                            totalRecords = b.optInt("totalRecords"),
                            importedRecords = b.optInt("importedRecords"),
                            duplicateRecords = b.optInt("duplicateRecords"),
                            invalidRecords = b.optInt("invalidRecords"),
                            distributionSummary = b.optString("distributionSummary")
                        )
                    )
                }
            }

            val notifList = mutableListOf<Notification>()
            val notifArray = obj.optJSONArray("notifications")
            if (notifArray != null) {
                for (i in 0 until notifArray.length()) {
                    val n = notifArray.optJSONObject(i) ?: continue
                    notifList.add(
                        Notification(
                            id = n.optString("id"),
                            userId = n.optString("userId"),
                            type = NotificationType.valueOf(n.optString("type")),
                            title = n.optString("title"),
                            message = n.optString("message"),
                            isRead = n.optBoolean("isRead", false),
                            createdAt = n.optLong("createdAt")
                        )
                    )
                }
            }

            return CrmSyncPayload(
                users = userList,
                clients = clientList,
                callRecords = callRecordList,
                followUps = followUpList,
                importBatches = batchList,
                notifications = notifList
            )
        }
    }
}
