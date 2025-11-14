package com.hfad.pet_scheduling.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.hfad.pet_scheduling.data.local.AppDatabase
import com.hfad.pet_scheduling.utils.NotificationHelper
import com.hfad.pet_scheduling.utils.NotificationScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver to handle notification action buttons (Mark Complete, Snooze)
 */
class NotificationActionReceiver : BroadcastReceiver() {
    
    companion object {
        const val ACTION_MARK_COMPLETE = "com.hfad.pet_scheduling.MARK_COMPLETE"
        const val ACTION_SNOOZE = "com.hfad.pet_scheduling.SNOOZE"
        const val EXTRA_TASK_ID = "task_id"
        const val EXTRA_PET_NAME = "pet_name"
        const val EXTRA_TASK_TITLE = "task_title"
    }
    
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getStringExtra(EXTRA_TASK_ID)
        val petName = intent.getStringExtra(EXTRA_PET_NAME) ?: "Your pet"
        val taskTitle = intent.getStringExtra(EXTRA_TASK_TITLE) ?: "Task"
        
        if (taskId == null) {
            Log.e("NotificationActionReceiver", "Task ID is null")
            return
        }
        
        when (intent.action) {
            ACTION_MARK_COMPLETE -> {
                handleMarkComplete(context, taskId, petName, taskTitle)
            }
            ACTION_SNOOZE -> {
                handleSnooze(context, taskId, petName, taskTitle)
            }
        }
    }
    
    private fun handleMarkComplete(context: Context, taskId: String, petName: String, taskTitle: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("NotificationActionReceiver", "✅ Mark Complete action for task: $taskId")
                
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser == null) {
                    Log.e("NotificationActionReceiver", "User not authenticated")
                    return@launch
                }
                
                val database = AppDatabase.getDatabase(context)
                val task = database.taskDao().getTaskById(taskId)
                
                if (task == null) {
                    Log.e("NotificationActionReceiver", "Task not found: $taskId")
                    return@launch
                }
                
                // Mark task as completed
                val completedTaskDao = database.completedTaskDao()
                val completedTask = com.hfad.pet_scheduling.data.local.entities.CompletedTask(
                    taskId = taskId,
                    completedByUserId = currentUser.uid,
                    notes = "Completed from notification",
                    scheduledTime = System.currentTimeMillis()
                )
                completedTaskDao.insertCompletedTask(completedTask)
                
                // Cancel any remaining notifications for this task
                val notificationScheduler = NotificationScheduler(context)
                notificationScheduler.cancelNotification(taskId)
                
                // Cancel the notification
                val notificationHelper = NotificationHelper(context)
                notificationHelper.cancelNotification(taskId.hashCode())
                
                Log.d("NotificationActionReceiver", "✅ Task marked as complete from notification")
            } catch (e: Exception) {
                Log.e("NotificationActionReceiver", "Error marking task complete", e)
            }
        }
    }
    
    private fun handleSnooze(context: Context, taskId: String, petName: String, taskTitle: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                Log.d("NotificationActionReceiver", "⏰ Snooze action for task: $taskId")
                
                val database = AppDatabase.getDatabase(context)
                val task = database.taskDao().getTaskById(taskId)
                
                if (task == null || !task.isActive) {
                    Log.e("NotificationActionReceiver", "Task not found or inactive: $taskId")
                    return@launch
                }
                
                // Cancel current notification
                val notificationHelper = NotificationHelper(context)
                notificationHelper.cancelNotification(taskId.hashCode())
                
                // Schedule new notification for 10 minutes from now
                val snoozeTime = System.currentTimeMillis() + (10 * 60 * 1000L) // 10 minutes
                
                // Create a temporary task with updated start time for snooze
                val snoozedTask = task.copy(startTime = snoozeTime)
                
                val notificationScheduler = NotificationScheduler(context)
                notificationScheduler.scheduleNotification(snoozedTask, petName)
                
                Log.d("NotificationActionReceiver", "✅ Task snoozed for 10 minutes")
            } catch (e: Exception) {
                Log.e("NotificationActionReceiver", "Error snoozing task", e)
            }
        }
    }
}

