package com.hfad.pet_scheduling.data.local.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "shared_access",
    foreignKeys = [
        ForeignKey(
            entity = Pet::class,
            parentColumns = arrayOf("petId"),
            childColumns = arrayOf("petId"),
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["petId"]),
        Index(value = ["sharedWithUserId"]),
        Index(value = ["petId", "sharedWithUserId"], unique = true)
    ]
)
data class SharedAccess(
    @PrimaryKey
    val shareId: String = java.util.UUID.randomUUID().toString(),

    val petId: String,
    val ownerUserId: String, // The pet owner
    val sharedWithUserId: String, // The user with whom the pet is shared
    val permissionLevel: String, // "view", "edit", "manage"
    val createdAt: Long = System.currentTimeMillis(),
    val isActive: Boolean = true
)

