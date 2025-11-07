package com.hfad.pet_scheduling.data.local.dao

import androidx.room.*
import com.hfad.pet_scheduling.data.local.entities.ScheduleTask
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM schedule_tasks WHERE petId = :petId AND isActive = 1 ORDER BY startTime ASC")
    fun getActiveTasksByPet(petId: String): Flow<List<ScheduleTask>>

    @Query("SELECT * FROM schedule_tasks WHERE petId IN (:petIds) AND isActive = 1 ORDER BY startTime ASC")
    fun getActiveTasksByPets(petIds: List<String>): Flow<List<ScheduleTask>>

    @Query("SELECT * FROM schedule_tasks WHERE taskId = :taskId")
    suspend fun getTaskById(taskId: String): ScheduleTask?

    @Query("SELECT * FROM schedule_tasks WHERE taskId = :taskId")
    fun getTaskByIdFlow(taskId: String): Flow<ScheduleTask?>

    @Query("""
        SELECT * FROM schedule_tasks 
        WHERE petId IN (:petIds) 
        AND isActive = 1 
        AND startTime BETWEEN :startTime AND :endTime
        ORDER BY startTime ASC
    """)
    fun getTasksInDateRange(
        petIds: List<String>,
        startTime: Long,
        endTime: Long
    ): Flow<List<ScheduleTask>>

    @Query("""
        SELECT * FROM schedule_tasks 
        WHERE petId IN (:petIds) 
        AND isActive = 1 
        AND startTime >= :currentTime
        ORDER BY startTime ASC
        LIMIT :limit
    """)
    fun getUpcomingTasks(
        petIds: List<String>,
        currentTime: Long,
        limit: Int = 10
    ): Flow<List<ScheduleTask>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: ScheduleTask): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<ScheduleTask>)

    @Update
    suspend fun updateTask(task: ScheduleTask)

    @Delete
    suspend fun deleteTask(task: ScheduleTask)

    @Query("DELETE FROM schedule_tasks WHERE taskId = :taskId")
    suspend fun deleteTaskById(taskId: String)

    @Query("UPDATE schedule_tasks SET isActive = :isActive WHERE taskId = :taskId")
    suspend fun setTaskActiveStatus(taskId: String, isActive: Boolean)
}

