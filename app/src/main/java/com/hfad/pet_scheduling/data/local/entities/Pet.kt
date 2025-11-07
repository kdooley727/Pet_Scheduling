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
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)