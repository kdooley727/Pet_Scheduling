package com.hfad.pet_scheduling.data.entities

import kotlinx.serialization.Serializable

@Serializable
data class SharedAccess(
    val shareId: String = "",
    val petId: String,
    val ownerUserId: String, // The pet owner
    val sharedWithUserId: String, // The user with whom the pet is shared
    val permissionLevel: String, // "view", "edit", "manage"
    val createdAt: Long = 0L,
    val isActive: Boolean = true
) {
    companion object {
        fun generateId(): String = java.util.UUID.randomUUID().toString()
    }
}

