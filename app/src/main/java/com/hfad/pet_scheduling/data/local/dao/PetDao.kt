package com.hfad.pet_scheduling.data.local.dao

import androidx.room.*
import com.hfad.pet_scheduling.data.local.entities.Pet
import kotlinx.coroutines.flow.Flow

@Dao
interface PetDao {
    @Query("SELECT * FROM pets WHERE userId = :userId ORDER BY name ASC")
    fun getAllPetsByUser(userId: String): Flow<List<Pet>>

    @Query("SELECT * FROM pets WHERE petId = :petId")
    suspend fun getPetById(petId: String): Pet?

    @Query("SELECT * FROM pets WHERE petId = :petId")
    fun getPetByIdFlow(petId: String): Flow<Pet?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPet(pet: Pet): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPets(pets: List<Pet>)

    @Update
    suspend fun updatePet(pet: Pet)

    @Delete
    suspend fun deletePet(pet: Pet)

    @Query("DELETE FROM pets WHERE petId = :petId")
    suspend fun deletePetById(petId: String)

    @Query("SELECT * FROM pets WHERE userId = :userId AND name LIKE :query || '%' ORDER BY name ASC")
    fun searchPets(userId: String, query: String): Flow<List<Pet>>
}

