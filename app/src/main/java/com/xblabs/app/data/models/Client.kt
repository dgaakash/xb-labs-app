package com.xblabs.app.data.models

enum class ClientStatus {
    NEW,
    IN_PROGRESS,
    FOLLOW_UP,
    INTERESTED,
    NOT_INTERESTED,
    COMPLETED,
    ARCHIVED,
    CLOSED
}

enum class WorkflowStatus {
    ACTIVE,
    CLOSED,
    EXHAUSTED
}

enum class PipelineStage {
    NONE,
    INTERESTED,
    CONTACTED,
    PROPOSAL,
    CONVERTED,
    LOST
}

enum class Priority {
    HIGH,
    NORMAL,
    LOW
}

data class Client(
    val id: String,
    val businessName: String,
    val category: String = "General",
    val rating: Double = 0.0,
    val reviewCount: Int = 0,
    val phone: String,
    val normalizedPhone: String,
    val website: String = "",
    val address: String = "",
    val mapsUrl: String = "",
    val assignedEmployeeId: String? = null,
    val assignedEmployeeName: String? = null,
    val currentStatus: ClientStatus = ClientStatus.NEW,
    val employeeWorkflowStatus: WorkflowStatus = WorkflowStatus.ACTIVE,
    val pipelineStage: PipelineStage = PipelineStage.NONE,
    val priority: Priority = Priority.NORMAL,
    val followUpCount: Int = 0,
    val nextFollowUpDate: Long? = null,
    val lastContactedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val importedAt: Long = System.currentTimeMillis(),
    val importBatchId: String = "",
    val archivedAt: Long? = null
) {
    val isAssigned: Boolean
        get() = !assignedEmployeeId.isNullOrEmpty()

    val displayWebsite: String
        get() = if (website.isBlank() || website.equals("N/A", ignoreCase = true)) "N/A" else website

    val hasOverdueFollowUp: Boolean
        get() = currentStatus == ClientStatus.FOLLOW_UP &&
                nextFollowUpDate != null &&
                nextFollowUpDate < System.currentTimeMillis()
}
