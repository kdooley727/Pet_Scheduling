package com.hfad.pet_scheduling.utils

import android.content.Context
import androidx.work.*
import com.hfad.pet_scheduling.data.local.entities.ScheduleTask
import com.hfad.pet_scheduling.workers.ReminderWorker
import java.util.concurrent.TimeUnit

/**
 * Helper class to schedule and cancel WorkManager notifications for pet care tasks
 */
class NotificationScheduler(private val context: Context) {
    private val workManager = WorkManager.getInstance(context)

    /**
     * Schedule notification for a task
     * Handles both one-time and recurring tasks
     */
    fun scheduleNotification(task: ScheduleTask, petName: String) {
        android.util.Log.d("NotificationScheduler", "🔔 scheduleNotification called for task: ${task.title}")
        
        if (!task.isActive) {
            // Cancel existing notifications if task is inactive
            android.util.Log.d("NotificationScheduler", "⚠️ Task is not active, cancelling notifications")
            cancelNotification(task.taskId)
            return
        }

        val currentTime = System.currentTimeMillis()
        val taskTime = task.startTime
        val reminderTime = taskTime - (task.reminderMinutesBefore * 60 * 1000L)
        
        android.util.Log.d("NotificationScheduler", "⏰ Current time: ${DateTimeUtils.formatDateTime(currentTime)}")
        android.util.Log.d("NotificationScheduler", "⏰ Task time: ${DateTimeUtils.formatDateTime(taskTime)}")
        android.util.Log.d("NotificationScheduler", "⏰ Reminder time: ${DateTimeUtils.formatDateTime(reminderTime)}")
        android.util.Log.d("NotificationScheduler", "⏰ Reminder minutes before: ${task.reminderMinutesBefore}")

        // Only schedule if reminder time is in the future
        if (reminderTime <= currentTime) {
            android.util.Log.w(
                "NotificationScheduler", 
                "⚠️ Reminder time is in the past for task ${task.taskId}. " +
                "Current: ${DateTimeUtils.formatDateTime(currentTime)}, " +
                "Reminder: ${DateTimeUtils.formatDateTime(reminderTime)}, " +
                "Task: ${DateTimeUtils.formatDateTime(taskTime)}"
            )
            
            // For recurring tasks, schedule the next occurrence
            if (task.recurrencePattern != Constants.RecurrencePattern.NONE) {
                android.util.Log.d("NotificationScheduler", "📅 Task is recurring, scheduling next occurrence")
                scheduleNextRecurrence(task, petName)
            } else {
                android.util.Log.w("NotificationScheduler", "❌ One-time task reminder time is in the past - notification NOT scheduled")
            }
            // For one-time tasks, don't schedule if reminder time has passed
            return
        }

        // Create work request data
        val inputData = Data.Builder()
            .putString(ReminderWorker.KEY_TASK_ID, task.taskId)
            .putString(ReminderWorker.KEY_PET_NAME, petName)
            .putString(ReminderWorker.KEY_TASK_TITLE, task.title)
            .putString(ReminderWorker.KEY_TASK_DESCRIPTION, task.description)
            .build()

        // Calculate delay until reminder time
        val delay = reminderTime - currentTime

        // Create one-time work request
        val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInputData(inputData)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .addTag(Constants.REMINDER_WORK_TAG)
            .addTag("task_${task.taskId}")
            .build()

        // Enqueue the work
        workManager.enqueue(workRequest)

        android.util.Log.d(
            "NotificationScheduler",
            "✅ Scheduled notification for task '${task.title}' (ID: ${task.taskId}) " +
                    "at ${DateTimeUtils.formatDateTime(reminderTime)} " +
                    "(${delay / 1000 / 60} minutes from now)"
        )

        // For recurring tasks, also schedule future occurrences
        if (task.recurrencePattern != Constants.RecurrencePattern.NONE) {
            scheduleRecurringNotifications(task, petName, taskTime)
        }
    }

    /**
     * Schedule notifications for recurring tasks
     * Schedules up to 10 future occurrences
     */
    private fun scheduleRecurringNotifications(
        task: ScheduleTask,
        petName: String,
        startTime: Long
    ) {
        val maxOccurrences = 10 // Limit to prevent too many scheduled notifications
        var nextTime = startTime

        for (i in 1 until maxOccurrences) {
            nextTime = DateTimeUtils.getNextOccurrence(
                nextTime,
                task.recurrencePattern,
                task.recurrenceInterval
            )

            val reminderTime = nextTime - (task.reminderMinutesBefore * 60 * 1000L)
            val currentTime = System.currentTimeMillis()

            // Only schedule if reminder time is in the future
            if (reminderTime <= currentTime) {
                continue
            }

            val delay = reminderTime - currentTime

            val inputData = Data.Builder()
                .putString(ReminderWorker.KEY_TASK_ID, task.taskId)
                .putString(ReminderWorker.KEY_PET_NAME, petName)
                .putString(ReminderWorker.KEY_TASK_TITLE, task.title)
                .putString(ReminderWorker.KEY_TASK_DESCRIPTION, task.description)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
                .setInputData(inputData)
                .setInitialDelay(delay, TimeUnit.MILLISECONDS)
                .addTag(Constants.REMINDER_WORK_TAG)
                .addTag("task_${task.taskId}")
                .addTag("recurring_${i}")
                .build()

            workManager.enqueue(workRequest)
        }
    }

    /**
     * Schedule the next occurrence for a recurring task
     */
    private fun scheduleNextRecurrence(task: ScheduleTask, petName: String) {
        val currentTime = System.currentTimeMillis()
        var nextOccurrence = task.startTime
        
        // Find the next occurrence that's in the future
        while (nextOccurrence <= currentTime) {
            nextOccurrence = DateTimeUtils.getNextOccurrence(
                nextOccurrence,
                task.recurrencePattern,
                task.recurrenceInterval
            )
        }

        val reminderTime = nextOccurrence - (task.reminderMinutesBefore * 60 * 1000L)

        if (reminderTime <= currentTime) {
            return
        }

        val delay = reminderTime - currentTime

        val inputData = Data.Builder()
            .putString(ReminderWorker.KEY_TASK_ID, task.taskId)
            .putString(ReminderWorker.KEY_PET_NAME, petName)
            .putString(ReminderWorker.KEY_TASK_TITLE, task.title)
            .putString(ReminderWorker.KEY_TASK_DESCRIPTION, task.description)
            .build()

        val workRequest = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInputData(inputData)
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .addTag(Constants.REMINDER_WORK_TAG)
            .addTag("task_${task.taskId}")
            .build()

        workManager.enqueue(workRequest)
        
        android.util.Log.d(
            "NotificationScheduler",
            "✅ Scheduled next occurrence for recurring task '${task.title}' " +
                    "at ${DateTimeUtils.formatDateTime(reminderTime)} " +
                    "(${delay / 1000 / 60} minutes from now)"
        )
    }

    /**
     * Cancel all notifications for a specific task
     */
    fun cancelNotification(taskId: String) {
        workManager.cancelAllWorkByTag("task_$taskId")
        android.util.Log.d("NotificationScheduler", "❌ Cancelled all notifications for task ID: $taskId")
    }

    /**
     * Cancel all notifications
     */
    fun cancelAllNotifications() {
        workManager.cancelAllWorkByTag(Constants.REMINDER_WORK_TAG)
        android.util.Log.d("NotificationScheduler", "Cancelled all notifications")
    }

    /**
     * Reschedule notifications for a task (useful when task is updated)
     */
    fun rescheduleNotification(task: ScheduleTask, petName: String) {
        cancelNotification(task.taskId)
        scheduleNotification(task, petName)
    }
}

