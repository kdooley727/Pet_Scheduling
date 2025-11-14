package com.hfad.pet_scheduling.data

/**
 * Data classes for statistics
 */
data class TaskStatistics(
    val totalTasks: Int,
    val completedTasks: Int,
    val completionRate: Float, // Percentage 0-100
    val currentStreak: Int, // Days
    val longestStreak: Int, // Days
    val mostCommonCategory: String?,
    val tasksByCategory: Map<String, Int>,
    val completionByCategory: Map<String, Int>,
    val recentCompletions: List<CompletionEntry>
)

data class CompletionEntry(
    val date: Long,
    val count: Int
)

data class PetStatistics(
    val petId: String,
    val petName: String,
    val totalTasks: Int,
    val completedTasks: Int,
    val completionRate: Float,
    val activeTasks: Int
)

