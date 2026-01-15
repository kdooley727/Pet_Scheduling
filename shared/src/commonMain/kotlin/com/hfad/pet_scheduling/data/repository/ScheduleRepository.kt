package com.hfad.pet_scheduling.data.repository

import app.cash.sqldelight.coroutines.asFlow
import com.hfad.pet_scheduling.data.entities.CompletedTask
import com.hfad.pet_scheduling.data.entities.ScheduleTask
import com.hfad.pet_scheduling.database.PetSchedulingDatabase
import com.hfad.petscheduling.database.CompletedTaskQueries
import com.hfad.petscheduling.database.TaskQueries
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Clock

class ScheduleRepository(
    private val database: PetSchedulingDatabase
) {
    private val taskQueries: TaskQueries = database.taskQueries
    private val completedTaskQueries: CompletedTaskQueries = database.completedTaskQueries

    // Task Operations
    fun getActiveTasksByPet(petId: String): Flow<List<ScheduleTask>> {
        return taskQueries.getActiveTasksByPet(petId) { taskId, petId, title, description, category, startTime,
            recurrencePattern, recurrenceInterval, reminderMinutesBefore, isActive, createdAt, createdByUserId ->
            ScheduleTask(
                taskId = taskId,
                petId = petId,
                title = title,
                description = description,
                category = category,
                startTime = startTime,
                recurrencePattern = recurrencePattern,
                recurrenceInterval = recurrenceInterval.toInt(),
                reminderMinutesBefore = reminderMinutesBefore.toInt(),
                isActive = isActive == 1L,
                createdAt = createdAt,
                createdByUserId = createdByUserId
            )
        }.asFlow().map { it.executeAsList() }
    }

    fun getActiveTasksByPets(petIds: List<String>): Flow<List<ScheduleTask>> {
        return taskQueries.getActiveTasksByPets(petIds) { taskId, petId, title, description, category, startTime,
            recurrencePattern, recurrenceInterval, reminderMinutesBefore, isActive, createdAt, createdByUserId ->
            ScheduleTask(
                taskId = taskId,
                petId = petId,
                title = title,
                description = description,
                category = category,
                startTime = startTime,
                recurrencePattern = recurrencePattern,
                recurrenceInterval = recurrenceInterval.toInt(),
                reminderMinutesBefore = reminderMinutesBefore.toInt(),
                isActive = isActive == 1L,
                createdAt = createdAt,
                createdByUserId = createdByUserId
            )
        }.asFlow().map { it.executeAsList() }
    }

    fun getTaskById(taskId: String): Flow<ScheduleTask?> {
        return taskQueries.getTaskById(taskId) { taskId, petId, title, description, category, startTime,
            recurrencePattern, recurrenceInterval, reminderMinutesBefore, isActive, createdAt, createdByUserId ->
            ScheduleTask(
                taskId = taskId,
                petId = petId,
                title = title,
                description = description,
                category = category,
                startTime = startTime,
                recurrencePattern = recurrencePattern,
                recurrenceInterval = recurrenceInterval.toInt(),
                reminderMinutesBefore = reminderMinutesBefore.toInt(),
                isActive = isActive == 1L,
                createdAt = createdAt,
                createdByUserId = createdByUserId
            )
        }.asFlow().map { it.executeAsOneOrNull() }
    }

    suspend fun getTaskByIdSuspend(taskId: String): ScheduleTask? {
        return taskQueries.getTaskById(taskId) { taskId, petId, title, description, category, startTime,
            recurrencePattern, recurrenceInterval, reminderMinutesBefore, isActive, createdAt, createdByUserId ->
            ScheduleTask(
                taskId = taskId,
                petId = petId,
                title = title,
                description = description,
                category = category,
                startTime = startTime,
                recurrencePattern = recurrencePattern,
                recurrenceInterval = recurrenceInterval.toInt(),
                reminderMinutesBefore = reminderMinutesBefore.toInt(),
                isActive = isActive == 1L,
                createdAt = createdAt,
                createdByUserId = createdByUserId
            )
        }.executeAsOneOrNull()
    }

    fun getTasksInDateRange(
        petIds: List<String>,
        startTime: Long,
        endTime: Long
    ): Flow<List<ScheduleTask>> {
        return taskQueries.getTasksInDateRange(petIds, startTime, endTime) { taskId, petId, title, description, category, startTime,
            recurrencePattern, recurrenceInterval, reminderMinutesBefore, isActive, createdAt, createdByUserId ->
            ScheduleTask(
                taskId = taskId,
                petId = petId,
                title = title,
                description = description,
                category = category,
                startTime = startTime,
                recurrencePattern = recurrencePattern,
                recurrenceInterval = recurrenceInterval.toInt(),
                reminderMinutesBefore = reminderMinutesBefore.toInt(),
                isActive = isActive == 1L,
                createdAt = createdAt,
                createdByUserId = createdByUserId
            )
        }.asFlow().map { it.executeAsList() }
    }

    fun getUpcomingTasks(
        petIds: List<String>,
        currentTime: Long = Clock.System.now().toEpochMilliseconds(),
        limit: Int = 10
    ): Flow<List<ScheduleTask>> {
        return taskQueries.getUpcomingTasks(petIds, currentTime, limit.toLong()) { taskId, petId, title, description, category, startTime,
            recurrencePattern, recurrenceInterval, reminderMinutesBefore, isActive, createdAt, createdByUserId ->
            ScheduleTask(
                taskId = taskId,
                petId = petId,
                title = title,
                description = description,
                category = category,
                startTime = startTime,
                recurrencePattern = recurrencePattern,
                recurrenceInterval = recurrenceInterval.toInt(),
                reminderMinutesBefore = reminderMinutesBefore.toInt(),
                isActive = isActive == 1L,
                createdAt = createdAt,
                createdByUserId = createdByUserId
            )
        }.asFlow().map { it.executeAsList() }
    }

    suspend fun getAllActiveTasks(): List<ScheduleTask> {
        return taskQueries.getAllActiveTasks { taskId, petId, title, description, category, startTime,
            recurrencePattern, recurrenceInterval, reminderMinutesBefore, isActive, createdAt, createdByUserId ->
            ScheduleTask(
                taskId = taskId,
                petId = petId,
                title = title,
                description = description,
                category = category,
                startTime = startTime,
                recurrencePattern = recurrencePattern,
                recurrenceInterval = recurrenceInterval.toInt(),
                reminderMinutesBefore = reminderMinutesBefore.toInt(),
                isActive = isActive == 1L,
                createdAt = createdAt,
                createdByUserId = createdByUserId
            )
        }.executeAsList()
    }

    suspend fun insertTask(task: ScheduleTask): Long {
        val taskId = if (task.taskId.isEmpty()) ScheduleTask.generateId() else task.taskId
        val now = Clock.System.now().toEpochMilliseconds()
        taskQueries.insertTask(
            taskId = taskId,
            petId = task.petId,
            title = task.title,
            description = task.description,
            category = task.category,
            startTime = task.startTime,
            recurrencePattern = task.recurrencePattern,
            recurrenceInterval = task.recurrenceInterval.toLong(),
            reminderMinutesBefore = task.reminderMinutesBefore.toLong(),
            isActive = if (task.isActive) 1L else 0L,
            createdAt = if (task.createdAt == 0L) now else task.createdAt,
            createdByUserId = task.createdByUserId
        )
        return 1L
    }

    suspend fun insertTasks(tasks: List<ScheduleTask>) {
        tasks.forEach { insertTask(it) }
    }

    suspend fun updateTask(task: ScheduleTask) {
        taskQueries.updateTask(
            petId = task.petId,
            title = task.title,
            description = task.description,
            category = task.category,
            startTime = task.startTime,
            recurrencePattern = task.recurrencePattern,
            recurrenceInterval = task.recurrenceInterval.toLong(),
            reminderMinutesBefore = task.reminderMinutesBefore.toLong(),
            isActive = if (task.isActive) 1L else 0L,
            createdByUserId = task.createdByUserId,
            taskId = task.taskId
        )
    }

    suspend fun deleteTask(task: ScheduleTask) {
        deleteTaskById(task.taskId)
    }

    suspend fun deleteTaskById(taskId: String) {
        taskQueries.deleteTask(taskId)
    }

    suspend fun setTaskActiveStatus(taskId: String, isActive: Boolean) {
        taskQueries.setTaskActiveStatus(if (isActive) 1L else 0L, taskId)
    }

    // Completed Task Operations
    fun getCompletedTasksByTaskId(taskId: String): Flow<List<CompletedTask>> {
        return completedTaskQueries.getCompletedTasksByTaskId(taskId) { completedTaskId, taskId, completedAt, completedByUserId, notes, scheduledTime ->
            CompletedTask(
                completedTaskId = completedTaskId,
                taskId = taskId,
                completedAt = completedAt,
                completedByUserId = completedByUserId,
                notes = notes,
                scheduledTime = scheduledTime
            )
        }.asFlow().map { it.executeAsList() }
    }

    suspend fun getCompletedTasksByPet(petId: String): List<CompletedTask> {
        return completedTaskQueries.getCompletedTasksByPet(petId) { completedTaskId, taskId, completedAt, completedByUserId, notes, scheduledTime ->
            CompletedTask(
                completedTaskId = completedTaskId,
                taskId = taskId,
                completedAt = completedAt,
                completedByUserId = completedByUserId,
                notes = notes,
                scheduledTime = scheduledTime
            )
        }.executeAsList()
    }

    fun getCompletedTasksInDateRange(
        taskIds: List<String>,
        startTime: Long,
        endTime: Long
    ): Flow<List<CompletedTask>> {
        return completedTaskQueries.getCompletedTasksByDateRange(taskIds, startTime, endTime) { completedTaskId, taskId, completedAt, completedByUserId, notes, scheduledTime ->
            CompletedTask(
                completedTaskId = completedTaskId,
                taskId = taskId,
                completedAt = completedAt,
                completedByUserId = completedByUserId,
                notes = notes,
                scheduledTime = scheduledTime
            )
        }.asFlow().map { it.executeAsList() }
    }

    fun getCompletionCount(taskId: String): Flow<Int> {
        return completedTaskQueries.getCompletionCount(taskId)
            .asFlow()
            .map { it.executeAsOne().toInt() }
    }

    suspend fun insertCompletedTask(completedTask: CompletedTask): Long {
        val completedTaskId = if (completedTask.completedTaskId.isEmpty()) CompletedTask.generateId() else completedTask.completedTaskId
        val now = Clock.System.now().toEpochMilliseconds()
        completedTaskQueries.insertCompletedTask(
            completedTaskId = completedTaskId,
            taskId = completedTask.taskId,
            completedAt = if (completedTask.completedAt == 0L) now else completedTask.completedAt,
            completedByUserId = completedTask.completedByUserId,
            notes = completedTask.notes,
            scheduledTime = completedTask.scheduledTime
        )
        return 1L
    }

    suspend fun markTaskCompleted(
        taskId: String,
        completedByUserId: String,
        notes: String? = null,
        scheduledTime: Long? = null
    ): Long {
        val completedTask = CompletedTask(
            taskId = taskId,
            completedByUserId = completedByUserId,
            notes = notes,
            scheduledTime = scheduledTime ?: Clock.System.now().toEpochMilliseconds()
        )
        return insertCompletedTask(completedTask)
    }

    suspend fun deleteCompletedTask(completedTaskId: String) {
        completedTaskQueries.deleteCompletedTask(completedTaskId)
    }
}

