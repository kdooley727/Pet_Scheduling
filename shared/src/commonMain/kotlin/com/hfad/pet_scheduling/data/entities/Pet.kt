package com.hfad.pet_scheduling.data.entities

import kotlinx.serialization.Serializable

@Serializable
data class Pet(
    val petId: String = "",
    val userId: String,
    val name: String,
    val type: String,
    val breed: String? = null,
    val birthDate: Long? = null,
    val photoUrl: String? = null,
    val notes: String? = null,
    
    // Emergency contacts
    val vetName: String? = null,
    val vetPhone: String? = null,
    val vetEmail: String? = null,
    val vetAddress: String? = null,
    
    val emergencyContactName: String? = null,
    val emergencyContactPhone: String? = null,
    val emergencyContactEmail: String? = null,
    val emergencyContactRelationship: String? = null,
    
    val createdAt: Long = 0L,
    val updatedAt: Long = 0L
) {
    companion object {
        fun generateId(): String = java.util.UUID.randomUUID().toString()
    }
}

