package com.hfad.pet_scheduling.viewmodels

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hfad.pet_scheduling.data.entities.CompletedTask
import com.hfad.pet_scheduling.data.entities.ScheduleTask
import com.hfad.pet_scheduling.data.repository.PetRepository
import com.hfad.pet_scheduling.data.repository.ScheduleRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import com.hfad.pet_scheduling.utils.DateTimeUtils
import com.hfad.pet_scheduling.utils.NotificationScheduler
import com.hfad.pet_scheduling.widgets.TaskWidgetProvider
import com.hfad.pet_scheduling.PetSchedulingApplication
import com.hfad.pet_scheduling.data.remote.FirestoreSyncService
import kotlinx.coroutines.launch

class ScheduleViewModel(
    private val scheduleRepository: ScheduleRepository,
    private val petRepository: PetRepository,
    private val application: Application
) : ViewModel() {
    
    private val notificationScheduler = NotificationScheduler(application)
    private val cloudSyncManager = (application as? PetSchedulingApplication)?.cloudSyncManager
    private val _tasks = MutableStateFlow<List<ScheduleTask>>(emptyList())
    val tasks: StateFlow<List<ScheduleTask>> = _tasks.asStateFlow()

    private val _selectedTask = MutableStateFlow<ScheduleTask?>(null)
    val selectedTask: StateFlow<ScheduleTask?> = _selectedTask.asStateFlow()

    private val _upcomingTasks = MutableStateFlow<List<ScheduleTask>>(emptyList())
    val upcomingTasks: StateFlow<List<ScheduleTask>> = _upcomingTasks.asStateFlow()

    private val _completedTasks = MutableStateFlow<List<CompletedTask>>(emptyList())
    val completedTasks: StateFlow<List<CompletedTask>> = _completedTasks.asStateFlow()

    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    /**
     * Load active tasks for a specific pet
     */
    fun loadTasksForPet(petId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                scheduleRepository.getActiveTasksByPet(petId).collect { taskList ->
                    _tasks.value = taskList
                    if (_isLoading.value) {
                        _isLoading.value = false
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error loading tasks: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    /**
     * Load active tasks for multiple pets
     */
    fun loadTasksForPets(petIds: List<String>) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                scheduleRepository.getActiveTasksByPets(petIds).collect { taskList ->
                    _tasks.value = taskList
                    if (_isLoading.value) {
                        _isLoading.value = false
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error loading tasks: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    /**
     * Load upcoming tasks
     */
    fun loadUpcomingTasks(petIds: List<String>, limit: Int = 10) {
        viewModelScope.launch {
            try {
                scheduleRepository.getUpcomingTasks(
                    petIds,
                    DateTimeUtils.getCurrentTimestamp(),
                    limit
                ).collect { taskList ->
                    _upcomingTasks.value = taskList
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error loading upcoming tasks: ${e.message}"
            }
        }
    }

    /**
     * Load tasks in a date range
     */
    fun loadTasksInDateRange(
        petIds: List<String>,
        startTime: Long,
        endTime: Long
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                scheduleRepository.getTasksInDateRange(petIds, startTime, endTime)
                    .collect { taskList ->
                        _tasks.value = taskList
                        if (_isLoading.value) {
                            _isLoading.value = false
                        }
                    }
            } catch (e: Exception) {
                _errorMessage.value = "Error loading tasks: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    /**
     * Get task by ID
     */
    fun getTaskById(taskId: String) {
        viewModelScope.launch {
            try {
                scheduleRepository.getTaskById(taskId).collect { task ->
                    _selectedTask.value = task
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error loading task: ${e.message}"
            }
        }
    }

    /**
     * Create or update a task
     */
    fun saveTask(task: ScheduleTask) {
        viewModelScope.launch {
            try {
                android.util.Log.d("ScheduleViewModel", "💾 Starting to save task: ${task.title}")
                _isLoading.value = true
                
                val result = scheduleRepository.insertTask(task)
                android.util.Log.d("ScheduleViewModel", "✅ Task saved to database, ID: ${task.taskId}")
                
                // Update widget
                updateWidget(application)
                
                // Schedule notification for the task
                android.util.Log.d("ScheduleViewModel", "📅 Scheduling notification for task: ${task.title}")
                val pet = petRepository.getPetByIdSuspend(task.petId)
                val petName = pet?.name ?: "Your pet"
                android.util.Log.d("ScheduleViewModel", "🐾 Pet name: $petName, Task start time: ${DateTimeUtils.formatDateTime(task.startTime)}, Reminder: ${task.reminderMinutesBefore} minutes before")
                
                notificationScheduler.scheduleNotification(task, petName)
                android.util.Log.d("ScheduleViewModel", "✅ Notification scheduling completed")
                
                // Sync to cloud
                cloudSyncManager?.syncToCloud()
                
                _isLoading.value = false
            } catch (e: Exception) {
                android.util.Log.e("ScheduleViewModel", "❌ Error saving task", e)
                _errorMessage.value = "Error saving task: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    /**
     * Update an existing task
     */
    fun updateTask(task: ScheduleTask) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                scheduleRepository.updateTask(task)
                
                // Update widget
                updateWidget(application)
                
                // Reschedule notification for the updated task
                val pet = petRepository.getPetByIdSuspend(task.petId)
                val petName = pet?.name ?: "Your pet"
                notificationScheduler.rescheduleNotification(task, petName)
                
                // Sync to cloud
                cloudSyncManager?.syncToCloud()
                
                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = "Error updating task: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    /**
     * Delete a task
     */
    fun deleteTask(task: ScheduleTask) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                // Cancel notifications before deleting
                notificationScheduler.cancelNotification(task.taskId)
                
                // Delete from cloud first (before local deletion)
                val syncService = FirestoreSyncService()
                syncService.deleteTaskFromCloud(task.taskId).fold(
                    onSuccess = {
                        android.util.Log.d("ScheduleViewModel", "✅ Task deleted from Firestore: ${task.title}")
                    },
                    onFailure = { e ->
                        android.util.Log.e("ScheduleViewModel", "❌ Failed to delete task from Firestore", e)
                        // Continue with local deletion even if cloud deletion fails
                    }
                )
                
                // Update widget
                updateWidget(application)
                
                // Delete from local database
                scheduleRepository.deleteTask(task)
                
                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = "Error deleting task: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    /**
     * Delete a task by ID
     */
    fun deleteTaskById(taskId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                
                // Get task first to have it for logging
                val task = scheduleRepository.getTaskByIdSuspend(taskId)
                
                // Cancel notifications before deleting
                notificationScheduler.cancelNotification(taskId)
                
                // Delete from cloud first (before local deletion)
                val syncService = FirestoreSyncService()
                syncService.deleteTaskFromCloud(taskId).fold(
                    onSuccess = {
                        android.util.Log.d("ScheduleViewModel", "✅ Task deleted from Firestore: $taskId")
                    },
                    onFailure = { e ->
                        android.util.Log.e("ScheduleViewModel", "❌ Failed to delete task from Firestore", e)
                        // Continue with local deletion even if cloud deletion fails
                    }
                )
                
                // Delete from local database
                scheduleRepository.deleteTaskById(taskId)
                
                // Update widget
                updateWidget(application)
                
                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = "Error deleting task: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    /**
     * Toggle task active status
     */
    fun toggleTaskActive(taskId: String, isActive: Boolean) {
        viewModelScope.launch {
            try {
                scheduleRepository.setTaskActiveStatus(taskId, isActive)
                
                // Get task and pet to reschedule/cancel notifications
                val task = scheduleRepository.getTaskByIdSuspend(taskId)
                if (task != null) {
                    if (isActive) {
                        // Reschedule notifications if task is being activated
                        val pet = petRepository.getPetByIdSuspend(task.petId)
                        val petName = pet?.name ?: "Your pet"
                        notificationScheduler.scheduleNotification(task, petName)
                    } else {
                        // Cancel notifications if task is being deactivated
                        notificationScheduler.cancelNotification(taskId)
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error updating task status: ${e.message}"
            }
        }
    }

    /**
     * Mark a task as completed
     */
    fun markTaskCompleted(
        taskId: String,
        completedByUserId: String,
        notes: String? = null,
        scheduledTime: Long? = null
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                scheduleRepository.markTaskCompleted(
                    taskId,
                    completedByUserId,
                    notes,
                    scheduledTime
                )
                
                // Sync to cloud
                cloudSyncManager?.syncToCloud()
                
                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = "Error completing task: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    /**
     * Load completed tasks for a specific task
     */
    fun loadCompletedTasksForTask(taskId: String) {
        viewModelScope.launch {
            try {
                scheduleRepository.getCompletedTasksByTaskId(taskId).collect { completedList ->
                    _completedTasks.value = completedList
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error loading completed tasks: ${e.message}"
            }
        }
    }

    /**
     * Load completed tasks in date range
     */
    fun loadCompletedTasksInDateRange(
        taskIds: List<String>,
        startTime: Long,
        endTime: Long
    ) {
        viewModelScope.launch {
            try {
                scheduleRepository.getCompletedTasksInDateRange(
                    taskIds,
                    startTime,
                    endTime
                ).collect { completedList ->
                    _completedTasks.value = completedList
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error loading completed tasks: ${e.message}"
            }
        }
    }

    /**
     * Get completion count for a task
     */
    fun getCompletionCount(taskId: String) {
        viewModelScope.launch {
            try {
                scheduleRepository.getCompletionCount(taskId).collect { count ->
                    // Handle count if needed
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error getting completion count: ${e.message}"
            }
        }
    }

    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }

    /**
     * Update widget when tasks change
     */
    private fun updateWidget(context: Context) {
        try {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val componentName = ComponentName(context, TaskWidgetProvider::class.java)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(componentName)
            
            if (appWidgetIds.isNotEmpty()) {
                val intent = android.content.Intent(context, TaskWidgetProvider::class.java).apply {
                    action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                    putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
                }
                context.sendBroadcast(intent)
            }
        } catch (e: Exception) {
            android.util.Log.e("ScheduleViewModel", "Error updating widget", e)
        }
    }

    /**
     * Clear selected task
     */
    fun clearSelectedTask() {
        _selectedTask.value = null
    }
}

