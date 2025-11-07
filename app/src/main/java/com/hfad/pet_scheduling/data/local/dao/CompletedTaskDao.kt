package com.hfad.pet_scheduling.data.local.dao

import androidx.room.*
import com.hfad.pet_scheduling.data.local.entities.CompletedTask
import kotlinx.coroutines.flow.Flow

@Dao
interface CompletedTaskDao {
    @Query("SELECT * FROM completed_tasks WHERE taskId = :taskId ORDER BY completedAt DESC")
    fun getCompletedTasksByTaskId(taskId: String): Flow<List<CompletedTask>>

    @Query("""
        SELECT * FROM completed_tasks 
        WHERE taskId IN (:taskIds) 
        AND completedAt BETWEEN :startTime AND :endTime
        ORDER BY completedAt DESC
    """)
    fun getCompletedTasksInDateRange(
        taskIds: List<String>,
        startTime: Long,
        endTime: Long
    ): Flow<List<CompletedTask>>

    @Query("SELECT * FROM completed_tasks WHERE completedTaskId = :completedTaskId")
    suspend fun getCompletedTaskById(completedTaskId: String): CompletedTask?

    @Query("SELECT COUNT(*) FROM completed_tasks WHERE taskId = :taskId")
    fun getCompletionCount(taskId: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletedTask(completedTask: CompletedTask): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCompletedTasks(completedTasks: List<CompletedTask>)

    @Delete
    suspend fun deleteCompletedTask(completedTask: CompletedTask)

    @Query("DELETE FROM completed_tasks WHERE completedTaskId = :completedTaskId")
    suspend fun deleteCompletedTaskById(completedTaskId: String)

    @Query("DELETE FROM completed_tasks WHERE taskId = :taskId")
    suspend fun deleteCompletedTasksByTaskId(taskId: String)
}

