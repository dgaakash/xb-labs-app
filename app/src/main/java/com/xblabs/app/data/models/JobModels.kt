package com.xblabs.app.data.models

enum class CallStatusOutcome {
    DIDNT_PICK_UP,
    NOT_INTERESTED,
    MAYBE_INTERESTED,
    INTERESTED
}

data class CallRecord(
    val id: String,
    val clientId: String,
    val clientName: String,
    val employeeId: String,
    val employeeName: String,
    val statusOutcome: CallStatusOutcome,
    val notes: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val followUpNumber: Int = 0
)

enum class FollowUpState {
    PENDING,
    COMPLETED,
    OVERDUE,
    CANCELLED
}

data class FollowUp(
    val id: String,
    val clientId: String,
    val clientName: String,
    val clientPhone: String,
    val employeeId: String,
    val employeeName: String,
    val attemptNumber: Int, // 1, 2, or 3
    val dueDate: Long,
    val state: FollowUpState = FollowUpState.PENDING,
    val notes: String = "",
    val previousResult: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null
) {
    val isOverdue: Boolean
        get() = state == FollowUpState.PENDING && dueDate < System.currentTimeMillis()
}

data class ImportBatch(
    val id: String,
    val adminId: String,
    val adminName: String,
    val createdAt: Long = System.currentTimeMillis(),
    val sourceName: String = "Text Import",
    val totalRecords: Int = 0,
    val importedRecords: Int = 0,
    val duplicateRecords: Int = 0,
    val invalidRecords: Int = 0,
    val distributionSummary: String = ""
)

enum class NotificationType {
    NEW_JOBS,
    QUEUE_EMPTY,
    FOLLOW_UP_DUE,
    REASSIGNMENT,
    SYSTEM_ALERT
}

data class Notification(
    val id: String,
    val userId: String,
    val type: NotificationType,
    val title: String,
    val message: String,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

data class ActivityLog(
    val id: String,
    val userId: String,
    val userName: String,
    val action: String,
    val entityType: String,
    val entityId: String,
    val metadata: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
