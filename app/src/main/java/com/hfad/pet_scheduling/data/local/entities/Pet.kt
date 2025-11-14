package com.hfad.pet_scheduling.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "pets")
data class Pet(
    @PrimaryKey
    val petId: String = java.util.UUID.randomUUID().toString(),

    val userId: String,
    val name: String,
    val type: String,
    val breed: String?,
    val birthDate: Long?,
    val photoUrl: String?,
    val notes: String?,
    
    // Emergency contacts - stored as JSON strings
    val vetName: String? = null,
    val vetPhone: String? = null,
    val vetEmail: String? = null,
    val vetAddress: String? = null,
    
    val emergencyContactName: String? = null,
    val emergencyContactPhone: String? = null,
    val emergencyContactEmail: String? = null,
    val emergencyContactRelationship: String? = null,
    
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)