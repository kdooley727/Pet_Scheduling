package com.hfad.pet_scheduling.widgets

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.google.firebase.auth.FirebaseAuth
import com.hfad.pet_scheduling.PetSchedulingApplication
import com.hfad.pet_scheduling.R
import com.hfad.pet_scheduling.data.local.entities.ScheduleTask
import com.hfad.pet_scheduling.utils.DateTimeUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/**
 * Widget provider for displaying upcoming pet care tasks on the home screen
 */
class TaskWidgetProvider : AppWidgetProvider() {

    private val widgetScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // Update all widgets
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        // Handle manual refresh
        if (intent.action == ACTION_REFRESH) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                android.content.ComponentName(context, TaskWidgetProvider::class.java)
            )
            onUpdate(context, appWidgetManager, appWidgetIds)
        }
    }

    private fun updateWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        widgetScope.launch {
            try {
                val currentUser = FirebaseAuth.getInstance().currentUser
                if (currentUser == null) {
                    // Show login prompt
                    val views = RemoteViews(context.packageName, R.layout.widget_task_list)
                    views.setTextViewText(R.id.widget_title, "Please log in")
                    views.setTextViewText(R.id.widget_empty_text, "Sign in to see your tasks")
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                    return@launch
                }

                val application = context.applicationContext as PetSchedulingApplication
                val petRepository = application.petRepository
                val scheduleRepository = application.scheduleRepository

                // Get all pets for the current user (using runBlocking to get first emission)
                val pets = runBlocking {
                    petRepository.getAllPetsByUser(currentUser.uid).first()
                }
                val petIds = pets.map { it.petId }

                if (petIds.isEmpty()) {
                    // No pets
                    val views = RemoteViews(context.packageName, R.layout.widget_task_list)
                    views.setTextViewText(R.id.widget_title, "No Pets")
                    views.setTextViewText(R.id.widget_empty_text, "Add a pet to see tasks")
                    appWidgetManager.updateAppWidget(appWidgetId, views)
                    return@launch
                }

                // Get upcoming tasks (next 5 tasks) - using runBlocking to get first emission
                val tasks = runBlocking {
                    scheduleRepository.getUpcomingTasks(
                        petIds = petIds,
                        currentTime = System.currentTimeMillis(),
                        limit = 5
                    ).first()
                }

                // Update widget UI
                val views = RemoteViews(context.packageName, R.layout.widget_task_list)
                
                if (tasks.isEmpty()) {
                    views.setTextViewText(R.id.widget_title, "Upcoming Tasks")
                    views.setTextViewText(R.id.widget_empty_text, "No upcoming tasks")
                    views.setViewVisibility(R.id.widget_task_list, android.view.View.GONE)
                    views.setViewVisibility(R.id.widget_empty_text, android.view.View.VISIBLE)
                } else {
                    views.setViewVisibility(R.id.widget_empty_text, android.view.View.GONE)
                    views.setViewVisibility(R.id.widget_task_list, android.view.View.VISIBLE)
                    
                    // Update title
                    views.setTextViewText(R.id.widget_title, "Upcoming Tasks (${tasks.size})")
                    
                    // Update task items (show up to 3 tasks)
                    val tasksToShow = tasks.take(3)
                    updateTaskViews(context, views, tasksToShow, pets)
                }

                // Set click intent to open app
                val intent = Intent(context, com.hfad.pet_scheduling.MainActivity::class.java)
                val pendingIntent = android.app.PendingIntent.getActivity(
                    context,
                    0,
                    intent,
                    android.app.PendingIntent.FLAG_UPDATE_CURRENT or android.app.PendingIntent.FLAG_IMMUTABLE
                )
                views.setOnClickPendingIntent(R.id.widget_container, pendingIntent)

                appWidgetManager.updateAppWidget(appWidgetId, views)
            } catch (e: Exception) {
                android.util.Log.e("TaskWidgetProvider", "Error updating widget", e)
                val views = RemoteViews(context.packageName, R.layout.widget_task_list)
                views.setTextViewText(R.id.widget_title, "Error")
                views.setTextViewText(R.id.widget_empty_text, "Unable to load tasks")
                appWidgetManager.updateAppWidget(appWidgetId, views)
            }
        }
    }

    private fun updateTaskViews(
        context: Context,
        views: RemoteViews,
        tasks: List<ScheduleTask>,
        pets: List<com.hfad.pet_scheduling.data.local.entities.Pet>
    ) {
        val petMap = pets.associateBy { it.petId }
        
        // Update task 1
        if (tasks.isNotEmpty()) {
            val task = tasks[0]
            val pet = petMap[task.petId]
            views.setTextViewText(R.id.widget_task1_title, task.title)
            views.setTextViewText(
                R.id.widget_task1_time,
                DateTimeUtils.formatTime(task.startTime)
            )
            views.setTextViewText(
                R.id.widget_task1_pet,
                pet?.name ?: "Unknown Pet"
            )
            views.setViewVisibility(R.id.widget_task1_container, android.view.View.VISIBLE)
        } else {
            views.setViewVisibility(R.id.widget_task1_container, android.view.View.GONE)
        }

        // Update task 2
        if (tasks.size > 1) {
            val task = tasks[1]
            val pet = petMap[task.petId]
            views.setTextViewText(R.id.widget_task2_title, task.title)
            views.setTextViewText(
                R.id.widget_task2_time,
                DateTimeUtils.formatTime(task.startTime)
            )
            views.setTextViewText(
                R.id.widget_task2_pet,
                pet?.name ?: "Unknown Pet"
            )
            views.setViewVisibility(R.id.widget_task2_container, android.view.View.VISIBLE)
        } else {
            views.setViewVisibility(R.id.widget_task2_container, android.view.View.GONE)
        }

        // Update task 3
        if (tasks.size > 2) {
            val task = tasks[2]
            val pet = petMap[task.petId]
            views.setTextViewText(R.id.widget_task3_title, task.title)
            views.setTextViewText(
                R.id.widget_task3_time,
                DateTimeUtils.formatTime(task.startTime)
            )
            views.setTextViewText(
                R.id.widget_task3_pet,
                pet?.name ?: "Unknown Pet"
            )
            views.setViewVisibility(R.id.widget_task3_container, android.view.View.VISIBLE)
        } else {
            views.setViewVisibility(R.id.widget_task3_container, android.view.View.GONE)
        }
    }

    companion object {
        const val ACTION_REFRESH = "com.hfad.pet_scheduling.widget.REFRESH"
    }
}

