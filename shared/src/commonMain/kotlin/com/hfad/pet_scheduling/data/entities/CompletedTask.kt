package com.hfad.pet_scheduling.data.entities

import kotlinx.serialization.Serializable

@Serializable
data class CompletedTask(
    val completedTaskId: String = "",
    val taskId: String,
    val completedAt: Long = 0L,
    val completedByUserId: String,
    val notes: String? = null,
    val scheduledTime: Long? = null // The original scheduled time this completion corresponds to
) {
    companion object {
        fun generateId(): String = java.util.UUID.randomUUID().toString()
    }
}

