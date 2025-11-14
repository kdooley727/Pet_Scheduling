package com.hfad.pet_scheduling.utils

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.hfad.pet_scheduling.PetSchedulingApplication
import com.hfad.pet_scheduling.data.remote.FirestoreSyncService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Manager class to handle cloud sync operations
 * Handles syncing local Room database with Firebase Firestore
 */
class CloudSyncManager(
    private val application: PetSchedulingApplication,
    private val syncService: FirestoreSyncService = FirestoreSyncService()
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val auth = FirebaseAuth.getInstance()

    companion object {
        private const val TAG = "CloudSyncManager"
    }

    /**
     * Sync status enum
     */
    enum class SyncStatus {
        IDLE,           // Not syncing
        SYNCING,        // Currently syncing
        SUCCESS,        // Last sync successful
        ERROR           // Last sync failed
    }

    private val _syncStatus = MutableStateFlow(SyncStatus.IDLE)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()

    private val _syncMessage = MutableStateFlow<String?>(null)
    val syncMessage: StateFlow<String?> = _syncMessage.asStateFlow()

    /**
     * Sync all local data to cloud
     */
    fun syncToCloud() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.w(TAG, "⚠️ User not authenticated, skipping cloud sync")
            return
        }

        scope.launch {
            try {
                _syncStatus.value = SyncStatus.SYNCING
                _syncMessage.value = "Syncing to cloud..."
                Log.d(TAG, "🔄 Starting cloud sync...")
                
                // Sync pets
                val pets = application.petRepository.getAllPetsByUser(currentUser.uid).first()
                syncService.syncPetsToCloud(pets).fold(
                    onSuccess = { Log.d(TAG, "✅ Pets synced successfully") },
                    onFailure = { e -> Log.e(TAG, "❌ Failed to sync pets", e) }
                )

                // Sync tasks
                val allTasks = application.scheduleRepository.getAllActiveTasks()
                syncService.syncTasksToCloud(allTasks).fold(
                    onSuccess = { Log.d(TAG, "✅ Tasks synced successfully") },
                    onFailure = { e -> Log.e(TAG, "❌ Failed to sync tasks", e) }
                )

                // Sync completed tasks (get from all tasks)
                val completedTasks = mutableListOf<com.hfad.pet_scheduling.data.local.entities.CompletedTask>()
                allTasks.forEach { task ->
                    val completed = application.scheduleRepository
                        .getCompletedTasksByTaskId(task.taskId).first()
                    completedTasks.addAll(completed)
                }
                syncService.syncCompletedTasksToCloud(completedTasks).fold(
                    onSuccess = { Log.d(TAG, "✅ Completed tasks synced successfully") },
                    onFailure = { e -> Log.e(TAG, "❌ Failed to sync completed tasks", e) }
                )

                Log.d(TAG, "✅ Cloud sync completed")
                _syncStatus.value = SyncStatus.SUCCESS
                _syncMessage.value = "Synced successfully"
                
                // Reset to IDLE after 2 seconds
                kotlinx.coroutines.delay(2000)
                _syncStatus.value = SyncStatus.IDLE
                _syncMessage.value = null
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error during cloud sync", e)
                _syncStatus.value = SyncStatus.ERROR
                _syncMessage.value = "Sync failed: ${e.message}"
                
                // Reset to IDLE after 3 seconds
                kotlinx.coroutines.delay(3000)
                _syncStatus.value = SyncStatus.IDLE
                _syncMessage.value = null
            }
        }
    }

    /**
     * Fetch all data from cloud and merge with local database
     * Uses last-write-wins conflict resolution
     */
    fun syncFromCloud() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            Log.w(TAG, "⚠️ User not authenticated, skipping cloud fetch")
            return
        }

        scope.launch {
            try {
                _syncStatus.value = SyncStatus.SYNCING
                _syncMessage.value = "Syncing from cloud..."
                Log.d(TAG, "🔄 Starting cloud fetch...")

                // Fetch pets from cloud
                syncService.fetchPetsFromCloud().fold(
                    onSuccess = { cloudPets ->
                        val localPets = application.petRepository.getAllPetsByUser(currentUser.uid).first()
                        
                        // Merge: keep latest updatedAt
                        cloudPets.forEach { cloudPet ->
                            val localPet = localPets.find { it.petId == cloudPet.petId }
                            when {
                                localPet == null -> {
                                    // New pet from cloud, add to local
                                    application.petRepository.insertPet(cloudPet)
                                    Log.d(TAG, "➕ Added pet from cloud: ${cloudPet.name}")
                                }
                                cloudPet.updatedAt > localPet.updatedAt -> {
                                    // Cloud version is newer, update local
                                    application.petRepository.updatePet(cloudPet)
                                    Log.d(TAG, "🔄 Updated pet from cloud: ${cloudPet.name}")
                                }
                                else -> {
                                    // Local version is newer or same, sync to cloud
                                    syncService.syncPetsToCloud(listOf(localPet))
                                }
                            }
                        }
                        
                        // Sync local-only pets to cloud
                        localPets.forEach { localPet ->
                            if (cloudPets.none { it.petId == localPet.petId }) {
                                syncService.syncPetsToCloud(listOf(localPet))
                            }
                        }
                    },
                    onFailure = { e -> Log.e(TAG, "❌ Failed to fetch pets from cloud", e) }
                )

                // Fetch tasks from cloud
                syncService.fetchTasksFromCloud().fold(
                    onSuccess = { cloudTasks ->
                        val localTasks = application.scheduleRepository.getAllActiveTasks()
                        
                        // Merge: keep latest createdAt
                        cloudTasks.forEach { cloudTask ->
                            val localTask = localTasks.find { it.taskId == cloudTask.taskId }
                            when {
                                localTask == null -> {
                                    // New task from cloud, add to local
                                    application.scheduleRepository.insertTask(cloudTask)
                                    Log.d(TAG, "➕ Added task from cloud: ${cloudTask.title}")
                                }
                                cloudTask.createdAt > localTask.createdAt -> {
                                    // Cloud version is newer, update local
                                    application.scheduleRepository.updateTask(cloudTask)
                                    Log.d(TAG, "🔄 Updated task from cloud: ${cloudTask.title}")
                                }
                                else -> {
                                    // Local version is newer or same, sync to cloud
                                    syncService.syncTasksToCloud(listOf(localTask))
                                }
                            }
                        }
                        
                        // Sync local-only tasks to cloud
                        localTasks.forEach { localTask ->
                            if (cloudTasks.none { it.taskId == localTask.taskId }) {
                                syncService.syncTasksToCloud(listOf(localTask))
                            }
                        }
                    },
                    onFailure = { e -> Log.e(TAG, "❌ Failed to fetch tasks from cloud", e) }
                )

                // Fetch completed tasks from cloud
                syncService.fetchCompletedTasksFromCloud().fold(
                    onSuccess = { cloudCompleted ->
                        // For completed tasks, we'll merge by checking if they exist locally
                        cloudCompleted.forEach { cloudCompletedTask ->
                            val localCompleted = application.scheduleRepository
                                .getCompletedTasksByTaskId(cloudCompletedTask.taskId).first()
                            
                            if (localCompleted.none { it.completedTaskId == cloudCompletedTask.completedTaskId }) {
                                // New completed task from cloud, add to local
                                application.scheduleRepository.insertCompletedTask(cloudCompletedTask)
                                Log.d(TAG, "➕ Added completed task from cloud")
                            }
                        }
                    },
                    onFailure = { e -> Log.e(TAG, "❌ Failed to fetch completed tasks from cloud", e) }
                )

                Log.d(TAG, "✅ Cloud fetch completed")
                _syncStatus.value = SyncStatus.SUCCESS
                _syncMessage.value = "Synced successfully"
                
                // Reset to IDLE after 2 seconds
                kotlinx.coroutines.delay(2000)
                _syncStatus.value = SyncStatus.IDLE
                _syncMessage.value = null
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error during cloud fetch", e)
                _syncStatus.value = SyncStatus.ERROR
                _syncMessage.value = "Sync failed: ${e.message}"
                
                // Reset to IDLE after 3 seconds
                kotlinx.coroutines.delay(3000)
                _syncStatus.value = SyncStatus.IDLE
                _syncMessage.value = null
            }
        }
    }

    /**
     * Full sync: fetch from cloud, then sync local changes back
     */
    fun fullSync() {
        scope.launch {
            syncFromCloud()
            // Small delay to ensure fetch completes
            kotlinx.coroutines.delay(1000)
            syncToCloud()
        }
    }
}

