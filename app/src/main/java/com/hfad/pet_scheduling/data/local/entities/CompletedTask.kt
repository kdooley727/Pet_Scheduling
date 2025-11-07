package com.hfad.pet_scheduling.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "completed_tasks",
    foreignKeys = [
        ForeignKey(
            entity = ScheduleTask::class,
            parentColumns = arrayOf("taskId"),
            childColumns = arrayOf("taskId"),
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["taskId"]), Index(value = ["completedAt"])]
)
data class CompletedTask(
    @PrimaryKey
    val completedTaskId: String = java.util.UUID.randomUUID().toString(),

    val taskId: String,
    val completedAt: Long = System.currentTimeMillis(),
    val completedByUserId: String,
    val notes: String?,
    val scheduledTime: Long? // The original scheduled time this completion corresponds to
)

