package com.hfad.pet_scheduling.data.entities

import kotlinx.serialization.Serializable

@Serializable
data class ScheduleTask(
    val taskId: String = "",
    val petId: String,
    val title: String,
    val description: String? = null,
    val category: String,
    val startTime: Long,
    val recurrencePattern: String,
    val recurrenceInterval: Int = 1,
    val reminderMinutesBefore: Int = 15,
    val isActive: Boolean = true,
    val createdAt: Long = 0L,
    val createdByUserId: String
) {
    companion object {
        fun generateId(): String = java.util.UUID.randomUUID().toString()
    }
}

