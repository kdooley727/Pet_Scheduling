package com.hfad.pet_scheduling.data.repository

import com.hfad.pet_scheduling.data.local.dao.CompletedTaskDao
import com.hfad.pet_scheduling.data.local.dao.TaskDao
import com.hfad.pet_scheduling.data.local.entities.CompletedTask
import com.hfad.pet_scheduling.data.local.entities.ScheduleTask
import kotlinx.coroutines.flow.Flow

class ScheduleRepository(
    private val taskDao: TaskDao,
    private val completedTaskDao: CompletedTaskDao
) {
    // Task Operations
    fun getActiveTasksByPet(petId: String): Flow<List<ScheduleTask>> {
        return taskDao.getActiveTasksByPet(petId)
    }

    fun getActiveTasksByPets(petIds: List<String>): Flow<List<ScheduleTask>> {
        return taskDao.getActiveTasksByPets(petIds)
    }

    fun getTaskById(taskId: String): Flow<ScheduleTask?> {
        return taskDao.getTaskByIdFlow(taskId)
    }

    suspend fun getTaskByIdSuspend(taskId: String): ScheduleTask? {
        return taskDao.getTaskById(taskId)
    }

    fun getTasksInDateRange(
        petIds: List<String>,
        startTime: Long,
        endTime: Long
    ): Flow<List<ScheduleTask>> {
        return taskDao.getTasksInDateRange(petIds, startTime, endTime)
    }

    fun getUpcomingTasks(
        petIds: List<String>,
        currentTime: Long = System.currentTimeMillis(),
        limit: Int = 10
    ): Flow<List<ScheduleTask>> {
        return taskDao.getUpcomingTasks(petIds, currentTime, limit)
    }

    suspend fun insertTask(task: ScheduleTask): Long {
        return taskDao.insertTask(task)
    }

    suspend fun insertTasks(tasks: List<ScheduleTask>) {
        taskDao.insertTasks(tasks)
    }

    suspend fun updateTask(task: ScheduleTask) {
        taskDao.updateTask(task)
    }

    suspend fun deleteTask(task: ScheduleTask) {
        taskDao.deleteTask(task)
    }

    suspend fun deleteTaskById(taskId: String) {
        taskDao.deleteTaskById(taskId)
    }

    suspend fun setTaskActiveStatus(taskId: String, isActive: Boolean) {
        taskDao.setTaskActiveStatus(taskId, isActive)
    }

    // Completed Task Operations
    fun getCompletedTasksByTaskId(taskId: String): Flow<List<CompletedTask>> {
        return completedTaskDao.getCompletedTasksByTaskId(taskId)
    }

    fun getCompletedTasksInDateRange(
        taskIds: List<String>,
        startTime: Long,
        endTime: Long
    ): Flow<List<CompletedTask>> {
        return completedTaskDao.getCompletedTasksInDateRange(taskIds, startTime, endTime)
    }

    fun getCompletionCount(taskId: String): Flow<Int> {
        return completedTaskDao.getCompletionCount(taskId)
    }

    suspend fun insertCompletedTask(completedTask: CompletedTask): Long {
        return completedTaskDao.insertCompletedTask(completedTask)
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
            scheduledTime = scheduledTime ?: System.currentTimeMillis()
        )
        return completedTaskDao.insertCompletedTask(completedTask)
    }

    suspend fun deleteCompletedTask(completedTaskId: String) {
        completedTaskDao.deleteCompletedTaskById(completedTaskId)
    }
}

