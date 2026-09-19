package com.xblabs.app.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.xblabs.app.data.models.*

class CrmDatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "xblabs_crm.db"
        private const val DATABASE_VERSION = 1

        // Table names
        private const val TABLE_USERS = "users"
        private const val TABLE_CLIENTS = "clients"
        private const val TABLE_CALL_RECORDS = "call_records"
        private const val TABLE_FOLLOW_UPS = "follow_ups"
        private const val TABLE_IMPORT_BATCHES = "import_batches"
        private const val TABLE_NOTIFICATIONS = "notifications"
        private const val TABLE_ACTIVITY_LOGS = "activity_logs"
    }

    override fun onCreate(db: SQLiteDatabase) {
        // Users Table
        db.execSQL(
            """
            CREATE TABLE $TABLE_USERS (
                id TEXT PRIMARY KEY,
                name TEXT NOT NULL,
                email TEXT NOT NULL UNIQUE,
                username TEXT NOT NULL UNIQUE,
                role TEXT NOT NULL,
                password_hash TEXT NOT NULL,
                theme TEXT NOT NULL,
                active INTEGER NOT NULL,
                created_at INTEGER NOT NULL,
                last_active INTEGER NOT NULL
            )
            """.trimIndent()
        )

        // Clients Table (Master Record)
        db.execSQL(
            """
            CREATE TABLE $TABLE_CLIENTS (
                id TEXT PRIMARY KEY,
                business_name TEXT NOT NULL,
                category TEXT NOT NULL,
                rating REAL NOT NULL,
                review_count INTEGER NOT NULL,
                phone TEXT NOT NULL,
                normalized_phone TEXT NOT NULL,
                website TEXT,
                address TEXT,
                maps_url TEXT,
                assigned_employee_id TEXT,
                assigned_employee_name TEXT,
                current_status TEXT NOT NULL,
                employee_workflow_status TEXT NOT NULL,
                pipeline_stage TEXT NOT NULL,
                priority TEXT NOT NULL,
                follow_up_count INTEGER NOT NULL,
                next_follow_up_date INTEGER,
                last_contacted_at INTEGER,
                created_at INTEGER NOT NULL,
                imported_at INTEGER NOT NULL,
                import_batch_id TEXT NOT NULL,
                archived_at INTEGER
            )
            """.trimIndent()
        )

        // Call Records Table
        db.execSQL(
            """
            CREATE TABLE $TABLE_CALL_RECORDS (
                id TEXT PRIMARY KEY,
                client_id TEXT NOT NULL,
                client_name TEXT NOT NULL,
                employee_id TEXT NOT NULL,
                employee_name TEXT NOT NULL,
                status_outcome TEXT NOT NULL,
                notes TEXT,
                timestamp INTEGER NOT NULL,
                follow_up_number INTEGER NOT NULL
            )
            """.trimIndent()
        )

        // Follow Ups Table
        db.execSQL(
            """
            CREATE TABLE $TABLE_FOLLOW_UPS (
                id TEXT PRIMARY KEY,
                client_id TEXT NOT NULL,
                client_name TEXT NOT NULL,
                client_phone TEXT NOT NULL,
                employee_id TEXT NOT NULL,
                employee_name TEXT NOT NULL,
                attempt_number INTEGER NOT NULL,
                due_date INTEGER NOT NULL,
                state TEXT NOT NULL,
                notes TEXT,
                previous_result TEXT,
                created_at INTEGER NOT NULL,
                completed_at INTEGER
            )
            """.trimIndent()
        )

        // Import Batches Table
        db.execSQL(
            """
            CREATE TABLE $TABLE_IMPORT_BATCHES (
                id TEXT PRIMARY KEY,
                admin_id TEXT NOT NULL,
                admin_name TEXT NOT NULL,
                created_at INTEGER NOT NULL,
                source_name TEXT NOT NULL,
                total_records INTEGER NOT NULL,
                imported_records INTEGER NOT NULL,
                duplicate_records INTEGER NOT NULL,
                invalid_records INTEGER NOT NULL,
                distribution_summary TEXT
            )
            """.trimIndent()
        )

        // Notifications Table
        db.execSQL(
            """
            CREATE TABLE $TABLE_NOTIFICATIONS (
                id TEXT PRIMARY KEY,
                user_id TEXT NOT NULL,
                type TEXT NOT NULL,
                title TEXT NOT NULL,
                message TEXT NOT NULL,
                is_read INTEGER NOT NULL,
                created_at INTEGER NOT NULL
            )
            """.trimIndent()
        )

        // Activity Logs Table
        db.execSQL(
            """
            CREATE TABLE $TABLE_ACTIVITY_LOGS (
                id TEXT PRIMARY KEY,
                user_id TEXT NOT NULL,
                user_name TEXT NOT NULL,
                action TEXT NOT NULL,
                entity_type TEXT NOT NULL,
                entity_id TEXT NOT NULL,
                metadata TEXT,
                timestamp INTEGER NOT NULL
            )
            """.trimIndent()
        )

        // Indexes for performance
        db.execSQL("CREATE INDEX idx_clients_phone ON $TABLE_CLIENTS(normalized_phone)")
        db.execSQL("CREATE INDEX idx_clients_employee ON $TABLE_CLIENTS(assigned_employee_id)")
        db.execSQL("CREATE INDEX idx_clients_status ON $TABLE_CLIENTS(current_status)")
        db.execSQL("CREATE INDEX idx_followups_employee ON $TABLE_FOLLOW_UPS(employee_id)")
        db.execSQL("CREATE INDEX idx_notifications_user ON $TABLE_NOTIFICATIONS(user_id)")
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USERS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CLIENTS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_CALL_RECORDS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_FOLLOW_UPS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_IMPORT_BATCHES")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NOTIFICATIONS")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_ACTIVITY_LOGS")
        onCreate(db)
    }

    // --- USERS CRUD ---

    fun insertUser(user: User): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("id", user.id)
            put("name", user.name)
            put("email", user.email)
            put("username", user.username)
            put("role", user.role.name)
            put("password_hash", user.passwordHash)
            put("theme", user.theme)
            put("active", if (user.active) 1 else 0)
            put("created_at", user.createdAt)
            put("last_active", user.lastActive)
        }
        return db.insertWithOnConflict(TABLE_USERS, null, values, SQLiteDatabase.CONFLICT_REPLACE) != -1L
    }

    fun getAllUsers(): List<User> {
        val users = mutableListOf<User>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_USERS ORDER BY name ASC", null)
        cursor.use { c ->
            while (c.moveToNext()) {
                users.add(
                    User(
                        id = c.getString(c.getColumnIndexOrThrow("id")),
                        name = c.getString(c.getColumnIndexOrThrow("name")),
                        email = c.getString(c.getColumnIndexOrThrow("email")),
                        username = c.getString(c.getColumnIndexOrThrow("username")),
                        role = UserRole.valueOf(c.getString(c.getColumnIndexOrThrow("role"))),
                        passwordHash = c.getString(c.getColumnIndexOrThrow("password_hash")),
                        theme = c.getString(c.getColumnIndexOrThrow("theme")),
                        active = c.getInt(c.getColumnIndexOrThrow("active")) == 1,
                        createdAt = c.getLong(c.getColumnIndexOrThrow("created_at")),
                        lastActive = c.getLong(c.getColumnIndexOrThrow("last_active"))
                    )
                )
            }
        }
        return users
    }

    fun updateUser(user: User): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("name", user.name)
            put("email", user.email)
            put("username", user.username)
            put("role", user.role.name)
            put("theme", user.theme)
            put("active", if (user.active) 1 else 0)
            put("last_active", user.lastActive)
        }
        return db.update(TABLE_USERS, values, "id = ?", arrayOf(user.id)) > 0
    }

    // --- CLIENTS CRUD ---

    fun insertClient(client: Client): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
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
        return db.insertWithOnConflict(TABLE_CLIENTS, null, values, SQLiteDatabase.CONFLICT_REPLACE) != -1L
    }

    fun getAllClients(): List<Client> {
        val clients = mutableListOf<Client>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_CLIENTS ORDER BY created_at DESC", null)
        cursor.use { c ->
            while (c.moveToNext()) {
                clients.add(cursorToClient(c))
            }
        }
        return clients
    }

    fun updateClient(client: Client): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
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
            put("archived_at", client.archivedAt)
        }
        return db.update(TABLE_CLIENTS, values, "id = ?", arrayOf(client.id)) > 0
    }

    private fun cursorToClient(c: android.database.Cursor): Client {
        return Client(
            id = c.getString(c.getColumnIndexOrThrow("id")),
            businessName = c.getString(c.getColumnIndexOrThrow("business_name")),
            category = c.getString(c.getColumnIndexOrThrow("category")),
            rating = c.getDouble(c.getColumnIndexOrThrow("rating")),
            reviewCount = c.getInt(c.getColumnIndexOrThrow("review_count")),
            phone = c.getString(c.getColumnIndexOrThrow("phone")),
            normalizedPhone = c.getString(c.getColumnIndexOrThrow("normalized_phone")),
            website = c.getString(c.getColumnIndexOrThrow("website")) ?: "",
            address = c.getString(c.getColumnIndexOrThrow("address")) ?: "",
            mapsUrl = c.getString(c.getColumnIndexOrThrow("maps_url")) ?: "",
            assignedEmployeeId = c.getString(c.getColumnIndexOrThrow("assigned_employee_id")),
            assignedEmployeeName = c.getString(c.getColumnIndexOrThrow("assigned_employee_name")),
            currentStatus = ClientStatus.valueOf(c.getString(c.getColumnIndexOrThrow("current_status"))),
            employeeWorkflowStatus = WorkflowStatus.valueOf(c.getString(c.getColumnIndexOrThrow("employee_workflow_status"))),
            pipelineStage = PipelineStage.valueOf(c.getString(c.getColumnIndexOrThrow("pipeline_stage"))),
            priority = Priority.valueOf(c.getString(c.getColumnIndexOrThrow("priority"))),
            followUpCount = c.getInt(c.getColumnIndexOrThrow("follow_up_count")),
            nextFollowUpDate = if (c.isNull(c.getColumnIndexOrThrow("next_follow_up_date"))) null else c.getLong(c.getColumnIndexOrThrow("next_follow_up_date")),
            lastContactedAt = if (c.isNull(c.getColumnIndexOrThrow("last_contacted_at"))) null else c.getLong(c.getColumnIndexOrThrow("last_contacted_at")),
            createdAt = c.getLong(c.getColumnIndexOrThrow("created_at")),
            importedAt = c.getLong(c.getColumnIndexOrThrow("imported_at")),
            importBatchId = c.getString(c.getColumnIndexOrThrow("import_batch_id")),
            archivedAt = if (c.isNull(c.getColumnIndexOrThrow("archived_at"))) null else c.getLong(c.getColumnIndexOrThrow("archived_at"))
        )
    }

    // --- CALL RECORDS CRUD ---

    fun insertCallRecord(record: CallRecord): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
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
        return db.insert(TABLE_CALL_RECORDS, null, values) != -1L
    }

    fun getAllCallRecords(): List<CallRecord> {
        val list = mutableListOf<CallRecord>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_CALL_RECORDS ORDER BY timestamp DESC", null)
        cursor.use { c ->
            while (c.moveToNext()) {
                list.add(
                    CallRecord(
                        id = c.getString(c.getColumnIndexOrThrow("id")),
                        clientId = c.getString(c.getColumnIndexOrThrow("client_id")),
                        clientName = c.getString(c.getColumnIndexOrThrow("client_name")),
                        employeeId = c.getString(c.getColumnIndexOrThrow("employee_id")),
                        employeeName = c.getString(c.getColumnIndexOrThrow("employee_name")),
                        statusOutcome = CallStatusOutcome.valueOf(c.getString(c.getColumnIndexOrThrow("status_outcome"))),
                        notes = c.getString(c.getColumnIndexOrThrow("notes")) ?: "",
                        timestamp = c.getLong(c.getColumnIndexOrThrow("timestamp")),
                        followUpNumber = c.getInt(c.getColumnIndexOrThrow("follow_up_number"))
                    )
                )
            }
        }
        return list
    }

    // --- FOLLOW UPS CRUD ---

    fun insertFollowUp(followUp: FollowUp): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
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
        return db.insertWithOnConflict(TABLE_FOLLOW_UPS, null, values, SQLiteDatabase.CONFLICT_REPLACE) != -1L
    }

    fun getAllFollowUps(): List<FollowUp> {
        val list = mutableListOf<FollowUp>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_FOLLOW_UPS ORDER BY due_date ASC", null)
        cursor.use { c ->
            while (c.moveToNext()) {
                list.add(
                    FollowUp(
                        id = c.getString(c.getColumnIndexOrThrow("id")),
                        clientId = c.getString(c.getColumnIndexOrThrow("client_id")),
                        clientName = c.getString(c.getColumnIndexOrThrow("client_name")),
                        clientPhone = c.getString(c.getColumnIndexOrThrow("client_phone")),
                        employeeId = c.getString(c.getColumnIndexOrThrow("employee_id")),
                        employeeName = c.getString(c.getColumnIndexOrThrow("employee_name")),
                        attemptNumber = c.getInt(c.getColumnIndexOrThrow("attempt_number")),
                        dueDate = c.getLong(c.getColumnIndexOrThrow("due_date")),
                        state = FollowUpState.valueOf(c.getString(c.getColumnIndexOrThrow("state"))),
                        notes = c.getString(c.getColumnIndexOrThrow("notes")) ?: "",
                        previousResult = c.getString(c.getColumnIndexOrThrow("previous_result")) ?: "",
                        createdAt = c.getLong(c.getColumnIndexOrThrow("created_at")),
                        completedAt = if (c.isNull(c.getColumnIndexOrThrow("completed_at"))) null else c.getLong(c.getColumnIndexOrThrow("completed_at"))
                    )
                )
            }
        }
        return list
    }

    fun updateFollowUp(followUp: FollowUp): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("state", followUp.state.name)
            put("notes", followUp.notes)
            put("completed_at", followUp.completedAt)
        }
        return db.update(TABLE_FOLLOW_UPS, values, "id = ?", arrayOf(followUp.id)) > 0
    }

    // --- IMPORT BATCHES CRUD ---

    fun insertImportBatch(batch: ImportBatch): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
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
        return db.insert(TABLE_IMPORT_BATCHES, null, values) != -1L
    }

    fun getAllImportBatches(): List<ImportBatch> {
        val list = mutableListOf<ImportBatch>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_IMPORT_BATCHES ORDER BY created_at DESC", null)
        cursor.use { c ->
            while (c.moveToNext()) {
                list.add(
                    ImportBatch(
                        id = c.getString(c.getColumnIndexOrThrow("id")),
                        adminId = c.getString(c.getColumnIndexOrThrow("admin_id")),
                        adminName = c.getString(c.getColumnIndexOrThrow("admin_name")),
                        createdAt = c.getLong(c.getColumnIndexOrThrow("created_at")),
                        sourceName = c.getString(c.getColumnIndexOrThrow("source_name")),
                        totalRecords = c.getInt(c.getColumnIndexOrThrow("total_records")),
                        importedRecords = c.getInt(c.getColumnIndexOrThrow("imported_records")),
                        duplicateRecords = c.getInt(c.getColumnIndexOrThrow("duplicate_records")),
                        invalidRecords = c.getInt(c.getColumnIndexOrThrow("invalid_records")),
                        distributionSummary = c.getString(c.getColumnIndexOrThrow("distribution_summary")) ?: ""
                    )
                )
            }
        }
        return list
    }

    // --- NOTIFICATIONS CRUD ---

    fun insertNotification(notification: Notification): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("id", notification.id)
            put("user_id", notification.userId)
            put("type", notification.type.name)
            put("title", notification.title)
            put("message", notification.message)
            put("is_read", if (notification.isRead) 1 else 0)
            put("created_at", notification.createdAt)
        }
        return db.insert(TABLE_NOTIFICATIONS, null, values) != -1L
    }

    fun getAllNotifications(): List<Notification> {
        val list = mutableListOf<Notification>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_NOTIFICATIONS ORDER BY created_at DESC", null)
        cursor.use { c ->
            while (c.moveToNext()) {
                list.add(
                    Notification(
                        id = c.getString(c.getColumnIndexOrThrow("id")),
                        userId = c.getString(c.getColumnIndexOrThrow("user_id")),
                        type = NotificationType.valueOf(c.getString(c.getColumnIndexOrThrow("type"))),
                        title = c.getString(c.getColumnIndexOrThrow("title")),
                        message = c.getString(c.getColumnIndexOrThrow("message")),
                        isRead = c.getInt(c.getColumnIndexOrThrow("is_read")) == 1,
                        createdAt = c.getLong(c.getColumnIndexOrThrow("created_at"))
                    )
                )
            }
        }
        return list
    }

    fun markNotificationAsRead(id: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("is_read", 1)
        }
        return db.update(TABLE_NOTIFICATIONS, values, "id = ?", arrayOf(id)) > 0
    }

    fun markAllNotificationsAsRead(userId: String): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("is_read", 1)
        }
        return db.update(TABLE_NOTIFICATIONS, values, "user_id = ? OR user_id = 'ALL'", arrayOf(userId)) > 0
    }

    // --- ACTIVITY LOGS CRUD ---

    fun insertActivityLog(log: ActivityLog): Boolean {
        val db = writableDatabase
        val values = ContentValues().apply {
            put("id", log.id)
            put("user_id", log.userId)
            put("user_name", log.userName)
            put("action", log.action)
            put("entity_type", log.entityType)
            put("entity_id", log.entityId)
            put("metadata", log.metadata)
            put("timestamp", log.timestamp)
        }
        return db.insert(TABLE_ACTIVITY_LOGS, null, values) != -1L
    }

    fun getAllActivityLogs(): List<ActivityLog> {
        val list = mutableListOf<ActivityLog>()
        val db = readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_ACTIVITY_LOGS ORDER BY timestamp DESC LIMIT 500", null)
        cursor.use { c ->
            while (c.moveToNext()) {
                list.add(
                    ActivityLog(
                        id = c.getString(c.getColumnIndexOrThrow("id")),
                        userId = c.getString(c.getColumnIndexOrThrow("user_id")),
                        userName = c.getString(c.getColumnIndexOrThrow("user_name")),
                        action = c.getString(c.getColumnIndexOrThrow("action")),
                        entityType = c.getString(c.getColumnIndexOrThrow("entity_type")),
                        entityId = c.getString(c.getColumnIndexOrThrow("entity_id")),
                        metadata = c.getString(c.getColumnIndexOrThrow("metadata")) ?: "",
                        timestamp = c.getLong(c.getColumnIndexOrThrow("timestamp"))
                    )
                )
            }
        }
        return list
    }
}
