package com.hfad.pet_scheduling.data.repository

import com.hfad.pet_scheduling.data.local.dao.PetDao
import com.hfad.pet_scheduling.data.local.dao.SharedAccessDao
import com.hfad.pet_scheduling.data.local.entities.Pet
import com.hfad.pet_scheduling.data.local.entities.SharedAccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

class PetRepository(
    private val petDao: PetDao,
    private val sharedAccessDao: SharedAccessDao
) {
    // Get all pets owned by user
    fun getAllPetsByUser(userId: String): Flow<List<Pet>> {
        return petDao.getAllPetsByUser(userId)
    }

    // Get all pets accessible to user (owned + shared)
    fun getAllAccessiblePets(userId: String): Flow<List<Pet>> {
        val ownedPets = petDao.getAllPetsByUser(userId)
        val sharedAccess = sharedAccessDao.getSharedPetsForUser(userId)
        
        return combine(ownedPets, sharedAccess) { owned, shared ->
            val sharedPetIds = shared.map { it.petId }.toSet()
            // Combine owned pets with shared pets (need to fetch shared pets separately)
            owned
        }
    }

    fun getPetById(petId: String): Flow<Pet?> {
        return petDao.getPetByIdFlow(petId)
    }

    suspend fun getPetByIdSuspend(petId: String): Pet? {
        return petDao.getPetById(petId)
    }

    suspend fun insertPet(pet: Pet): Long {
        return petDao.insertPet(pet)
    }

    suspend fun updatePet(pet: Pet) {
        petDao.updatePet(pet.copy(updatedAt = System.currentTimeMillis()))
    }

    suspend fun deletePet(pet: Pet) {
        petDao.deletePet(pet)
    }

    suspend fun deletePetById(petId: String) {
        petDao.deletePetById(petId)
    }

    fun searchPets(userId: String, query: String): Flow<List<Pet>> {
        return petDao.searchPets(userId, query)
    }

    // Shared Access Functions
    suspend fun sharePet(sharedAccess: SharedAccess): Long {
        return sharedAccessDao.insertSharedAccess(sharedAccess)
    }

    fun getSharedPetsForUser(userId: String): Flow<List<SharedAccess>> {
        return sharedAccessDao.getSharedPetsForUser(userId)
    }

    fun getPetsSharedByUser(userId: String): Flow<List<SharedAccess>> {
        return sharedAccessDao.getPetsSharedByUser(userId)
    }

    suspend fun getPermissionLevel(petId: String, userId: String): String? {
        return sharedAccessDao.getPermissionLevel(petId, userId)
    }

    suspend fun updatePermissionLevel(shareId: String, permissionLevel: String) {
        sharedAccessDao.updatePermissionLevel(shareId, permissionLevel)
    }

    suspend fun revokeSharedAccess(shareId: String) {
        sharedAccessDao.setSharedAccessActive(shareId, false)
    }
}

