package com.hfad.pet_scheduling.data.repository

import com.hfad.pet_scheduling.data.entities.Pet
import com.hfad.pet_scheduling.data.entities.SharedAccess
import app.cash.sqldelight.coroutines.asFlow
import com.hfad.pet_scheduling.database.PetSchedulingDatabase
import com.hfad.petscheduling.database.PetQueries
import com.hfad.petscheduling.database.SharedAccessQueries
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.combine
import kotlinx.datetime.Clock

class PetRepository(
    private val database: PetSchedulingDatabase
) {
    private val petQueries: PetQueries = database.petQueries
    private val sharedAccessQueries: SharedAccessQueries = database.sharedAccessQueries

    // Get all pets owned by user
    fun getAllPetsByUser(userId: String): Flow<List<Pet>> {
        return petQueries.getAllPetsByUser(userId) { petId, userId, name, type, breed, birthDate, photoUrl, notes,
            vetName, vetPhone, vetEmail, vetAddress,
            emergencyContactName, emergencyContactPhone, emergencyContactEmail, emergencyContactRelationship,
            createdAt, updatedAt ->
            Pet(
                petId = petId,
                userId = userId,
                name = name,
                type = type,
                breed = breed,
                birthDate = birthDate,
                photoUrl = photoUrl,
                notes = notes,
                vetName = vetName,
                vetPhone = vetPhone,
                vetEmail = vetEmail,
                vetAddress = vetAddress,
                emergencyContactName = emergencyContactName,
                emergencyContactPhone = emergencyContactPhone,
                emergencyContactEmail = emergencyContactEmail,
                emergencyContactRelationship = emergencyContactRelationship,
                createdAt = createdAt,
                updatedAt = updatedAt
            )
        }.asFlow().map { it.executeAsList() }
    }

    // Get all pets accessible to user (owned + shared)
    fun getAllAccessiblePets(userId: String): Flow<List<Pet>> {
        val ownedPets = getAllPetsByUser(userId)
        val sharedAccess = getSharedPetsForUser(userId)
        
        return combine(ownedPets, sharedAccess) { owned, shared ->
            val sharedPetIds = shared.map { it.petId }.toSet()
            // For now, return owned pets. Later we can fetch shared pets by their IDs
            owned
        }
    }

    fun getPetById(petId: String): Flow<Pet?> {
        return petQueries.getPetById(petId) { petId, userId, name, type, breed, birthDate, photoUrl, notes,
            vetName, vetPhone, vetEmail, vetAddress,
            emergencyContactName, emergencyContactPhone, emergencyContactEmail, emergencyContactRelationship,
            createdAt, updatedAt ->
            Pet(
                petId = petId,
                userId = userId,
                name = name,
                type = type,
                breed = breed,
                birthDate = birthDate,
                photoUrl = photoUrl,
                notes = notes,
                vetName = vetName,
                vetPhone = vetPhone,
                vetEmail = vetEmail,
                vetAddress = vetAddress,
                emergencyContactName = emergencyContactName,
                emergencyContactPhone = emergencyContactPhone,
                emergencyContactEmail = emergencyContactEmail,
                emergencyContactRelationship = emergencyContactRelationship,
                createdAt = createdAt,
                updatedAt = updatedAt
            )
        }.asFlow().map { it.executeAsOneOrNull() }
    }

    suspend fun getPetByIdSuspend(petId: String): Pet? {
        return petQueries.getPetById(petId) { petId, userId, name, type, breed, birthDate, photoUrl, notes,
            vetName, vetPhone, vetEmail, vetAddress,
            emergencyContactName, emergencyContactPhone, emergencyContactEmail, emergencyContactRelationship,
            createdAt, updatedAt ->
            Pet(
                petId = petId,
                userId = userId,
                name = name,
                type = type,
                breed = breed,
                birthDate = birthDate,
                photoUrl = photoUrl,
                notes = notes,
                vetName = vetName,
                vetPhone = vetPhone,
                vetEmail = vetEmail,
                vetAddress = vetAddress,
                emergencyContactName = emergencyContactName,
                emergencyContactPhone = emergencyContactPhone,
                emergencyContactEmail = emergencyContactEmail,
                emergencyContactRelationship = emergencyContactRelationship,
                createdAt = createdAt,
                updatedAt = updatedAt
            )
        }.executeAsOneOrNull()
    }

    suspend fun insertPet(pet: Pet): Long {
        val petId = if (pet.petId.isEmpty()) Pet.generateId() else pet.petId
        val now = Clock.System.now().toEpochMilliseconds()
        petQueries.insertPet(
            petId = petId,
            userId = pet.userId,
            name = pet.name,
            type = pet.type,
            breed = pet.breed,
            birthDate = pet.birthDate,
            photoUrl = pet.photoUrl,
            notes = pet.notes,
            vetName = pet.vetName,
            vetPhone = pet.vetPhone,
            vetEmail = pet.vetEmail,
            vetAddress = pet.vetAddress,
            emergencyContactName = pet.emergencyContactName,
            emergencyContactPhone = pet.emergencyContactPhone,
            emergencyContactEmail = pet.emergencyContactEmail,
            emergencyContactRelationship = pet.emergencyContactRelationship,
            createdAt = if (pet.createdAt == 0L) now else pet.createdAt,
            updatedAt = now
        )
        return 1L // SQLDelight doesn't return row ID for INSERT OR REPLACE
    }

    suspend fun updatePet(pet: Pet) {
        val now = Clock.System.now().toEpochMilliseconds()
        petQueries.updatePet(
            userId = pet.userId,
            name = pet.name,
            type = pet.type,
            breed = pet.breed,
            birthDate = pet.birthDate,
            photoUrl = pet.photoUrl,
            notes = pet.notes,
            vetName = pet.vetName,
            vetPhone = pet.vetPhone,
            vetEmail = pet.vetEmail,
            vetAddress = pet.vetAddress,
            emergencyContactName = pet.emergencyContactName,
            emergencyContactPhone = pet.emergencyContactPhone,
            emergencyContactEmail = pet.emergencyContactEmail,
            emergencyContactRelationship = pet.emergencyContactRelationship,
            updatedAt = now,
            petId = pet.petId
        )
    }

    suspend fun deletePet(pet: Pet) {
        deletePetById(pet.petId)
    }

    suspend fun deletePetById(petId: String) {
        petQueries.deletePet(petId)
    }

    fun searchPets(userId: String, query: String): Flow<List<Pet>> {
        return petQueries.searchPets(userId, query) { petId, userId, name, type, breed, birthDate, photoUrl, notes,
            vetName, vetPhone, vetEmail, vetAddress,
            emergencyContactName, emergencyContactPhone, emergencyContactEmail, emergencyContactRelationship,
            createdAt, updatedAt ->
            Pet(
                petId = petId,
                userId = userId,
                name = name,
                type = type,
                breed = breed,
                birthDate = birthDate,
                photoUrl = photoUrl,
                notes = notes,
                vetName = vetName,
                vetPhone = vetPhone,
                vetEmail = vetEmail,
                vetAddress = vetAddress,
                emergencyContactName = emergencyContactName,
                emergencyContactPhone = emergencyContactPhone,
                emergencyContactEmail = emergencyContactEmail,
                emergencyContactRelationship = emergencyContactRelationship,
                createdAt = createdAt,
                updatedAt = updatedAt
            )
        }.asFlow().map { it.executeAsList() }
    }

    // Shared Access Functions
    suspend fun sharePet(sharedAccess: SharedAccess): Long {
        val shareId = if (sharedAccess.shareId.isEmpty()) SharedAccess.generateId() else sharedAccess.shareId
        val now = Clock.System.now().toEpochMilliseconds()
        sharedAccessQueries.insertSharedAccess(
            shareId = shareId,
            petId = sharedAccess.petId,
            ownerUserId = sharedAccess.ownerUserId,
            sharedWithUserId = sharedAccess.sharedWithUserId,
            permissionLevel = sharedAccess.permissionLevel,
            createdAt = if (sharedAccess.createdAt == 0L) now else sharedAccess.createdAt,
            isActive = if (sharedAccess.isActive) 1 else 0
        )
        return 1L
    }

    fun getSharedPetsForUser(userId: String): Flow<List<SharedAccess>> {
        return sharedAccessQueries.getSharedPetsForUser(userId) { shareId, petId, ownerUserId, sharedWithUserId, permissionLevel, createdAt, isActive ->
            SharedAccess(
                shareId = shareId,
                petId = petId,
                ownerUserId = ownerUserId,
                sharedWithUserId = sharedWithUserId,
                permissionLevel = permissionLevel,
                createdAt = createdAt,
                isActive = isActive == 1L
            )
        }.asFlow().map { it.executeAsList() }
    }

    fun getPetsSharedByUser(userId: String): Flow<List<SharedAccess>> {
        return sharedAccessQueries.getPetsSharedByUser(userId) { shareId, petId, ownerUserId, sharedWithUserId, permissionLevel, createdAt, isActive ->
            SharedAccess(
                shareId = shareId,
                petId = petId,
                ownerUserId = ownerUserId,
                sharedWithUserId = sharedWithUserId,
                permissionLevel = permissionLevel,
                createdAt = createdAt,
                isActive = isActive == 1L
            )
        }.asFlow().map { it.executeAsList() }
    }

    suspend fun getPermissionLevel(petId: String, userId: String): String? {
        return sharedAccessQueries.getPermissionLevel(petId, userId).executeAsOneOrNull()
    }

    suspend fun updatePermissionLevel(shareId: String, permissionLevel: String) {
        sharedAccessQueries.updatePermissionLevel(permissionLevel, shareId)
    }

    suspend fun revokeSharedAccess(shareId: String) {
        sharedAccessQueries.setSharedAccessActive(0, shareId)
    }
}

