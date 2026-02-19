package com.hfad.pet_scheduling.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hfad.pet_scheduling.data.entities.Pet
import com.hfad.pet_scheduling.data.entities.SharedAccess
import com.hfad.pet_scheduling.data.repository.PetRepository
import com.hfad.pet_scheduling.data.remote.FirestoreSyncService
import com.hfad.pet_scheduling.utils.CloudSyncManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PetViewModel(
    private val petRepository: PetRepository,
    private val cloudSyncManager: CloudSyncManager? = null,
    private val scheduleRepository: com.hfad.pet_scheduling.data.repository.ScheduleRepository? = null
) : ViewModel() {
    private val _pets = MutableStateFlow<List<Pet>>(emptyList())
    val pets: StateFlow<List<Pet>> = _pets.asStateFlow()

    private val _selectedPet = MutableStateFlow<Pet?>(null)
    val selectedPet: StateFlow<Pet?> = _selectedPet.asStateFlow()

    private val _isLoading = MutableStateFlow<Boolean>(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _saveResult = MutableStateFlow<Boolean?>(null)
    val saveResult: StateFlow<Boolean?> = _saveResult.asStateFlow()

    private val _sharedAccess = MutableStateFlow<List<SharedAccess>>(emptyList())
    val sharedAccess: StateFlow<List<SharedAccess>> = _sharedAccess.asStateFlow()

    private var currentUserId: String? = null

    /**
     * Initialize the ViewModel with current user ID
     */
    fun initialize(userId: String) {
        currentUserId = userId
        loadPets(userId)
    }

    /**
     * Load all pets for the current user
     */
    fun loadPets(userId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                android.util.Log.d("PetViewModel", "Loading pets for userId: $userId")
                
                petRepository.getAllAccessiblePets(userId).collect { petList ->
                    android.util.Log.d("PetViewModel", "Received ${petList.size} pets")
                    _pets.value = petList
                    if (_isLoading.value) {
                        _isLoading.value = false
                    }
                }
            } catch (e: Exception) {
                android.util.Log.e("PetViewModel", "Error loading pets", e)
                _errorMessage.value = "Error loading pets: ${e.message ?: e.javaClass.simpleName}"
                _isLoading.value = false
                _pets.value = emptyList()
            }
        }
    }

    /**
     * Get pet by ID
     */
    fun getPetById(petId: String) {
        viewModelScope.launch {
            try {
                petRepository.getPetById(petId).collect { pet ->
                    _selectedPet.value = pet
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error loading pet: ${e.message}"
            }
        }
    }

    /**
     * Create or update a pet
     */
    fun savePet(pet: Pet, isNewPet: Boolean = false) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                _saveResult.value = null
                
                android.util.Log.d("PetViewModel", "Saving pet: name=${pet.name}, type=${pet.type}, isNewPet=$isNewPet")
                
                if (isNewPet) {
                    // New pet - insert
                    val result = petRepository.insertPet(pet)
                    android.util.Log.d("PetViewModel", "Pet inserted with result: $result")
                } else {
                    // Existing pet - update
                    petRepository.updatePet(pet)
                    android.util.Log.d("PetViewModel", "Pet updated successfully")
                }
                
                // Sync to cloud
                cloudSyncManager?.syncToCloud()
                
                _isLoading.value = false
                _saveResult.value = true
            } catch (e: Exception) {
                android.util.Log.e("PetViewModel", "Error saving pet", e)
                _errorMessage.value = "Error saving pet: ${e.message ?: e.javaClass.simpleName}"
                _isLoading.value = false
                _saveResult.value = false
            }
        }
    }

    /**
     * Delete a pet
     */
    fun deletePet(pet: Pet) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                android.util.Log.d("PetViewModel", "Deleting pet: ${pet.name} (${pet.petId})")
                
                // Delete from cloud first (before local deletion)
                val syncService = FirestoreSyncService()
                val cloudDeleteResult = syncService.deletePetFromCloud(pet.petId)
                val cloudDeleteSucceeded = cloudDeleteResult.isSuccess
                
                cloudDeleteResult.fold(
                    onSuccess = {
                        android.util.Log.d("PetViewModel", "✅ Pet deleted from Firestore")
                    },
                    onFailure = { e ->
                        android.util.Log.e("PetViewModel", "❌ Failed to delete pet from Firestore", e)
                        // Show warning - pet will be deleted locally but may reappear on sync
                        _errorMessage.value = "Warning: Pet deleted locally but may reappear if cloud deletion failed. Please check your connection and try again if needed."
                    }
                )
                
                // Delete associated tasks from cloud and local database
                scheduleRepository?.let { repo ->
                    try {
                        val tasks = repo.getAllActiveTasks()
                        tasks.filter { it.petId == pet.petId }.forEach { task ->
                            // Delete task from cloud
                            syncService.deleteTaskFromCloud(task.taskId).fold(
                                onSuccess = {
                                    android.util.Log.d("PetViewModel", "✅ Task deleted from Firestore: ${task.title}")
                                },
                                onFailure = { e ->
                                    android.util.Log.e("PetViewModel", "❌ Failed to delete task from Firestore", e)
                                }
                            )
                            // Delete task from local database
                            repo.deleteTask(task)
                            android.util.Log.d("PetViewModel", "✅ Deleted associated task: ${task.title}")
                        }
                    } catch (e: Exception) {
                        android.util.Log.e("PetViewModel", "Error deleting associated tasks", e)
                    }
                }
                
                // Delete from local database
                // Note: If cloud deletion failed, the pet may reappear on next sync
                // User should delete again when online to ensure it's removed from Firebase
                petRepository.deletePet(pet)
                android.util.Log.d("PetViewModel", "✅ Pet deleted from local database")
                
                if (!cloudDeleteSucceeded) {
                    android.util.Log.w("PetViewModel", "⚠️ Cloud deletion failed - pet may reappear on sync")
                }
                
                _isLoading.value = false
            } catch (e: Exception) {
                android.util.Log.e("PetViewModel", "Error deleting pet", e)
                _errorMessage.value = "Error deleting pet: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    /**
     * Delete a pet by ID
     */
    fun deletePetById(petId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                petRepository.deletePetById(petId)
                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = "Error deleting pet: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    /**
     * Search pets by name
     */
    fun searchPets(query: String) {
        val userId = currentUserId ?: return
        viewModelScope.launch {
            try {
                petRepository.searchPets(userId, query).collect { results ->
                    _pets.value = results
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error searching pets: ${e.message}"
            }
        }
    }

    /**
     * Clear search and reload all pets
     */
    fun clearSearch() {
        currentUserId?.let { loadPets(it) }
    }

    /**
     * Share a pet with another user
     */
    fun sharePet(sharedAccess: SharedAccess) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                petRepository.sharePet(sharedAccess)
                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = "Error sharing pet: ${e.message}"
                _isLoading.value = false
            }
        }
    }

    /**
     * Load shared pets for current user
     */
    fun loadSharedPets(userId: String) {
        viewModelScope.launch {
            try {
                petRepository.getSharedPetsForUser(userId).collect { sharedList ->
                    _sharedAccess.value = sharedList
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error loading shared pets: ${e.message}"
            }
        }
    }

    /**
     * Revoke shared access
     */
    fun revokeSharedAccess(shareId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                petRepository.revokeSharedAccess(shareId)
                _isLoading.value = false
            } catch (e: Exception) {
                _errorMessage.value = "Error revoking access: ${e.message}"
                _isLoading.value = false
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
     * Clear selected pet
     */
    fun clearSelectedPet() {
        _selectedPet.value = null
    }
}

