package com.hfad.pet_scheduling.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "schedule_tasks",
    foreignKeys = [
        ForeignKey(
            entity = Pet::class,
            parentColumns = arrayOf("petId"),
            childColumns = arrayOf("petId"),
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["petId"])]
)
data class ScheduleTask(
    @PrimaryKey
    val taskId: String = java.util.UUID.randomUUID().toString(),

    val petId: String,
    val title: String,
    val description: String?,
    val category: String,
    val startTime: Long,
    val recurrencePattern: String,
    val recurrenceInterval: Int = 1,
    val reminderMinutesBefore: Int = 15,
    val isActive: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val createdByUserId: String
)