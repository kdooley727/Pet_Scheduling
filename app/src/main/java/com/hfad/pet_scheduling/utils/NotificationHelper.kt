package com.hfad.pet_scheduling.utils

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.hfad.pet_scheduling.MainActivity
import com.hfad.pet_scheduling.R
import com.hfad.pet_scheduling.receivers.NotificationActionReceiver

class NotificationHelper(private val context: Context) {
    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                Constants.NOTIFICATION_CHANNEL_ID,
                Constants.NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for pet scheduling reminders"
                enableVibration(true)
                enableLights(true)
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Show a notification for a schedule task reminder
     */
    fun showTaskReminder(
        taskId: String,
        petName: String,
        taskTitle: String,
        taskDescription: String? = null
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("taskId", taskId)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            taskId.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Create Mark Complete action
        val markCompleteIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_MARK_COMPLETE
            putExtra(NotificationActionReceiver.EXTRA_TASK_ID, taskId)
            putExtra(NotificationActionReceiver.EXTRA_PET_NAME, petName)
            putExtra(NotificationActionReceiver.EXTRA_TASK_TITLE, taskTitle)
        }
        val markCompletePendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.hashCode() + 1, // Different request code
            markCompleteIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Create Snooze action
        val snoozeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = NotificationActionReceiver.ACTION_SNOOZE
            putExtra(NotificationActionReceiver.EXTRA_TASK_ID, taskId)
            putExtra(NotificationActionReceiver.EXTRA_PET_NAME, petName)
            putExtra(NotificationActionReceiver.EXTRA_TASK_TITLE, taskTitle)
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            taskId.hashCode() + 2, // Different request code
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Try to use custom icon, fallback to a working icon if vector doesn't render
        // Note: Some Android versions don't render vector drawables well for notification icons
        val iconResId = try {
            // Try custom icon first
            val customIcon = R.drawable.ic_notification_pet
            val drawable = context.getDrawable(customIcon)
            if (drawable != null) {
                android.util.Log.d("NotificationHelper", "✅ Using custom notification icon")
                customIcon
            } else {
                android.util.Log.w("NotificationHelper", "⚠️ Custom icon not found, using default")
                android.R.drawable.ic_dialog_info
            }
        } catch (e: Exception) {
            android.util.Log.e("NotificationHelper", "❌ Error with custom icon, using default", e)
            android.R.drawable.ic_dialog_info
        }
        
        val notification = NotificationCompat.Builder(context, Constants.NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(iconResId)
            .setContentTitle("Reminder: $taskTitle")
            .setContentText("Time to take care of $petName!")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(taskDescription ?: "Time to take care of $petName!")
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            // Add action buttons
            .addAction(
                android.R.drawable.ic_menu_compass, // Icon for complete
                "Mark Complete",
                markCompletePendingIntent
            )
            .addAction(
                android.R.drawable.ic_menu_recent_history, // Icon for snooze
                "Snooze 10 min",
                snoozePendingIntent
            )
            .build()

        notificationManager.notify(taskId.hashCode(), notification)
    }

    /**
     * Cancel a notification by ID
     */
    fun cancelNotification(notificationId: Int) {
        notificationManager.cancel(notificationId)
    }

    /**
     * Cancel all notifications
     */
    fun cancelAllNotifications() {
        notificationManager.cancelAll()
    }
}

