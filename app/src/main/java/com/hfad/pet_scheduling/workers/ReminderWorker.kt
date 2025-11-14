package com.hfad.pet_scheduling.workers

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.hfad.pet_scheduling.data.local.AppDatabase
import com.hfad.pet_scheduling.data.local.entities.ScheduleTask
import com.hfad.pet_scheduling.utils.NotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Worker that shows notifications for scheduled pet care tasks
 */
class ReminderWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            val taskId = inputData.getString(KEY_TASK_ID)
            val petName = inputData.getString(KEY_PET_NAME) ?: "Your pet"
            val taskTitle = inputData.getString(KEY_TASK_TITLE) ?: "Task"
            val taskDescription = inputData.getString(KEY_TASK_DESCRIPTION)

            if (taskId == null) {
                android.util.Log.e("ReminderWorker", "Task ID is null")
                return@withContext Result.failure()
            }

            // Verify task is still active before showing notification
            val database = AppDatabase.getDatabase(applicationContext)
            val task = database.taskDao().getTaskById(taskId)

            if (task == null || !task.isActive) {
                android.util.Log.d(
                    "ReminderWorker",
                    "⚠️ Task $taskId is no longer active (null: ${task == null}, active: ${task?.isActive}), skipping notification"
                )
                return@withContext Result.success()
            }

            // Show notification
            val notificationHelper = NotificationHelper(applicationContext)
            notificationHelper.showTaskReminder(
                taskId = taskId,
                petName = petName,
                taskTitle = taskTitle,
                taskDescription = taskDescription
            )

            android.util.Log.d(
                "ReminderWorker",
                "🔔 Notification shown for task: '$taskTitle' (Pet: $petName, ID: $taskId)"
            )
            Result.success()
        } catch (e: Exception) {
            android.util.Log.e("ReminderWorker", "❌ Error showing notification", e)
            Result.retry()
        }
    }

    companion object {
        const val KEY_TASK_ID = "task_id"
        const val KEY_PET_NAME = "pet_name"
        const val KEY_TASK_TITLE = "task_title"
        const val KEY_TASK_DESCRIPTION = "task_description"
    }
}

