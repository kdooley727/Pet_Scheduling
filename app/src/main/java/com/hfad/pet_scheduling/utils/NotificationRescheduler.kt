package com.hfad.pet_scheduling.utils

import android.content.Context
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.hfad.pet_scheduling.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Utility class to reschedule notifications for all active tasks
 * Should be called on app startup to ensure notifications persist after device restarts
 */
class NotificationRescheduler(private val context: Context) {
    
    private val notificationScheduler = NotificationScheduler(context)
    
    /**
     * Reschedule notifications for all active tasks for the current user
     */
    fun rescheduleAllNotifications() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser == null) {
                    Log.d("NotificationRescheduler", "⚠️ No user logged in, skipping notification rescheduling")
                    return@launch
                }
                
                Log.d("NotificationRescheduler", "🔄 Starting to reschedule notifications for user: ${currentUser.uid}")
                
                val database = AppDatabase.getDatabase(context)
                
                // Get all active tasks
                val allTasks = database.taskDao().getAllActiveTasks()
                
                if (allTasks.isEmpty()) {
                    Log.d("NotificationRescheduler", "ℹ️ No active tasks found, nothing to reschedule")
                    return@launch
                }
                
                Log.d("NotificationRescheduler", "📋 Found ${allTasks.size} active task(s) to reschedule")
                
                var scheduledCount = 0
                var skippedCount = 0
                
                for (task in allTasks) {
                    // Only reschedule tasks for the current user
                    if (task.createdByUserId != currentUser.uid) {
                        skippedCount++
                        continue
                    }
                    
                    // Get pet name for the task
                    val pet = database.petDao().getPetById(task.petId)
                    val petName = pet?.name ?: "Your pet"
                    
                    // Cancel any existing notifications for this task first
                    notificationScheduler.cancelNotification(task.taskId)
                    
                    // Reschedule the notification
                    notificationScheduler.scheduleNotification(task, petName)
                    
                    scheduledCount++
                }
                
                Log.d(
                    "NotificationRescheduler",
                    "✅ Rescheduling complete: $scheduledCount scheduled, $skippedCount skipped"
                )
            } catch (e: Exception) {
                Log.e("NotificationRescheduler", "❌ Error rescheduling notifications", e)
            }
        }
    }
    
    /**
     * Reschedule notifications for tasks belonging to specific pets
     * Note: This method uses Flow.collect which may not work well in a loop.
     * Consider using getAllActiveTasks() and filtering by petId instead.
     */
    fun rescheduleNotificationsForPets(petIds: List<String>) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser == null) {
                    Log.d("NotificationRescheduler", "⚠️ No user logged in, skipping notification rescheduling")
                    return@launch
                }
                
                Log.d("NotificationRescheduler", "🔄 Rescheduling notifications for ${petIds.size} pet(s)")
                
                val database = AppDatabase.getDatabase(context)
                
                // Get all active tasks and filter by pet IDs
                val allTasks = database.taskDao().getAllActiveTasks()
                val filteredTasks = allTasks.filter { it.petId in petIds && it.createdByUserId == currentUser.uid }
                
                var scheduledCount = 0
                
                for (task in filteredTasks) {
                    // Get pet name
                    val pet = database.petDao().getPetById(task.petId)
                    val petName = pet?.name ?: "Your pet"
                    
                    // Cancel existing notifications and reschedule
                    notificationScheduler.cancelNotification(task.taskId)
                    notificationScheduler.scheduleNotification(task, petName)
                    scheduledCount++
                }
                
                Log.d("NotificationRescheduler", "✅ Rescheduled $scheduledCount notification(s)")
            } catch (e: Exception) {
                Log.e("NotificationRescheduler", "❌ Error rescheduling notifications for pets", e)
            }
        }
    }
}

