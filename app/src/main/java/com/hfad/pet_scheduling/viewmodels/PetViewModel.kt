package com.hfad.pet_scheduling.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hfad.pet_scheduling.data.local.entities.Pet
import com.hfad.pet_scheduling.data.local.entities.SharedAccess
import com.hfad.pet_scheduling.data.repository.PetRepository
import kotlinx.coroutines.launch

class PetViewModel(private val petRepository: PetRepository) : ViewModel() {
    private val _pets = MutableLiveData<List<Pet>>()
    val pets: LiveData<List<Pet>> = _pets

    private val _selectedPet = MutableLiveData<Pet?>()
    val selectedPet: LiveData<Pet?> = _selectedPet

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val _sharedAccess = MutableLiveData<List<SharedAccess>>()
    val sharedAccess: LiveData<List<SharedAccess>> = _sharedAccess

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
                petRepository.getAllPetsByUser(userId).collect { petList ->
                    _pets.value = petList
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                _errorMessage.value = "Error loading pets: ${e.message}"
                _isLoading.value = false
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
                _isLoading.value = false
            } catch (e: Exception) {
                android.util.Log.e("PetViewModel", "Error saving pet", e)
                _errorMessage.value = "Error saving pet: ${e.message ?: e.javaClass.simpleName}"
                _isLoading.value = false
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
                petRepository.deletePet(pet)
                _isLoading.value = false
            } catch (e: Exception) {
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

