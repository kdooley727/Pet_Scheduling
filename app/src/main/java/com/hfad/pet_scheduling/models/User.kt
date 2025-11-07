package com.hfad.pet_scheduling.models

data class User(
    val userId: String,
    val email: String,
    val displayName: String?,
    val photoUrl: String?,
    val createdAt: Long = System.currentTimeMillis()
)

