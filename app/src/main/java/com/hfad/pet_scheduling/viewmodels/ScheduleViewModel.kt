package com.hfad.pet_scheduling.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hfad.pet_scheduling.data.local.entities.CompletedTask
import com.hfad.pet_scheduling.data.local.entities.ScheduleTask
import com.hfad.pet_scheduling.data.repository.ScheduleRepository
import com.hfad.pet_scheduling.utils.DateTimeUtils
import kotlinx.coroutines.launch

class ScheduleViewModel(private val scheduleRepository: ScheduleRepository) : ViewModel() {
    private val _tasks = MutableLiveData<List<ScheduleTask>>()
    val tasks: LiveData<List<ScheduleTask>> = _tasks

    private val _selectedTask = MutableLiveData<ScheduleTask?>()
    val selectedTask: LiveData<ScheduleTask?> = _selectedTask

    private val _upcomingTasks = MutableLiveData<List<ScheduleTask>>()
    val upcomingTasks: LiveData<List<ScheduleTask>> = _upcomingTasks

    private val _completedTasks = MutableLiveData<List<CompletedTask>>()
    val completedTasks: LiveData<List<CompletedTask>> = _completedTasks

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    /**
     * Load active tasks for a specific pet
     */
    fun loadTasksForPet(petId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                scheduleRepository.getActiveTasksByPet(petId).collect { taskList ->
                    _tasks.value = taskList
                    _isLoading.value = false
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
                    _isLoading.value = false
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
                        _isLoading.value = false
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
                _isLoading.value = true
                scheduleRepository.insertTask(task)
                _isLoading.value = false
            } catch (e: Exception) {
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
                scheduleRepository.deleteTaskById(taskId)
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
     * Clear selected task
     */
    fun clearSelectedTask() {
        _selectedTask.value = null
    }
}

