package com.hfad.pet_scheduling.data.local.entities

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Emergency contact information for a pet
 * Embedded in Pet entity
 */
data class EmergencyContact(
    val name: String,
    val phone: String,
    val email: String? = null,
    val relationship: String? = null // e.g., "Vet", "Emergency Contact", "Pet Sitter"
)

