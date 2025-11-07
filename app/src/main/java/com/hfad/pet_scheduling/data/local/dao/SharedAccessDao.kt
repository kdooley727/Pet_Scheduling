package com.hfad.pet_scheduling.data.local.dao

import androidx.room.*
import com.hfad.pet_scheduling.data.local.entities.SharedAccess
import kotlinx.coroutines.flow.Flow

@Dao
interface SharedAccessDao {
    @Query("SELECT * FROM shared_access WHERE petId = :petId AND isActive = 1")
    fun getActiveSharedAccessByPet(petId: String): Flow<List<SharedAccess>>

    @Query("SELECT * FROM shared_access WHERE sharedWithUserId = :userId AND isActive = 1")
    fun getSharedPetsForUser(userId: String): Flow<List<SharedAccess>>

    @Query("SELECT * FROM shared_access WHERE ownerUserId = :userId AND isActive = 1")
    fun getPetsSharedByUser(userId: String): Flow<List<SharedAccess>>

    @Query("SELECT * FROM shared_access WHERE petId = :petId AND sharedWithUserId = :userId AND isActive = 1")
    suspend fun getSharedAccess(petId: String, userId: String): SharedAccess?

    @Query("SELECT * FROM shared_access WHERE shareId = :shareId")
    suspend fun getSharedAccessById(shareId: String): SharedAccess?

    @Query("SELECT permissionLevel FROM shared_access WHERE petId = :petId AND sharedWithUserId = :userId AND isActive = 1")
    suspend fun getPermissionLevel(petId: String, userId: String): String?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSharedAccess(sharedAccess: SharedAccess): Long

    @Update
    suspend fun updateSharedAccess(sharedAccess: SharedAccess)

    @Query("UPDATE shared_access SET isActive = :isActive WHERE shareId = :shareId")
    suspend fun setSharedAccessActive(shareId: String, isActive: Boolean)

    @Query("UPDATE shared_access SET permissionLevel = :permissionLevel WHERE shareId = :shareId")
    suspend fun updatePermissionLevel(shareId: String, permissionLevel: String)

    @Delete
    suspend fun deleteSharedAccess(sharedAccess: SharedAccess)

    @Query("DELETE FROM shared_access WHERE shareId = :shareId")
    suspend fun deleteSharedAccessById(shareId: String)

    @Query("DELETE FROM shared_access WHERE petId = :petId")
    suspend fun deleteSharedAccessByPet(petId: String)
}

