package com.hfad.pet_scheduling.utils

import com.hfad.pet_scheduling.data.CompletionEntry
import com.hfad.pet_scheduling.data.PetStatistics
import com.hfad.pet_scheduling.data.TaskStatistics
import com.hfad.pet_scheduling.data.local.entities.CompletedTask
import com.hfad.pet_scheduling.data.local.entities.ScheduleTask
import java.util.*

/**
 * Utility class for calculating statistics from task data
 */
object StatisticsCalculator {

    /**
     * Calculate overall task statistics
     */
    fun calculateTaskStatistics(
        allTasks: List<ScheduleTask>,
        completedTasks: List<CompletedTask>
    ): TaskStatistics {
        val totalTasks = allTasks.size
        val completedCount = completedTasks.size
        
        val completionRate = if (totalTasks > 0) {
            (completedCount.toFloat() / totalTasks.toFloat()) * 100f
        } else {
            0f
        }

        // Calculate tasks by category
        val tasksByCategory = allTasks.groupingBy { it.category }.eachCount()
        val completionByCategory = completedTasks
            .mapNotNull { completed ->
                allTasks.find { it.taskId == completed.taskId }?.category
            }
            .groupingBy { it }
            .eachCount()

        val mostCommonCategory = tasksByCategory.maxByOrNull { it.value }?.key

        // Calculate streaks
        val (currentStreak, longestStreak) = calculateStreaks(completedTasks)

        // Recent completions (last 7 days)
        val recentCompletions = getRecentCompletions(completedTasks, 7)

        return TaskStatistics(
            totalTasks = totalTasks,
            completedTasks = completedCount,
            completionRate = completionRate,
            currentStreak = currentStreak,
            longestStreak = longestStreak,
            mostCommonCategory = mostCommonCategory,
            tasksByCategory = tasksByCategory,
            completionByCategory = completionByCategory,
            recentCompletions = recentCompletions
        )
    }

    /**
     * Calculate statistics for a specific pet
     */
    fun calculatePetStatistics(
        petId: String,
        petName: String,
        petTasks: List<ScheduleTask>,
        allCompletedTasks: List<CompletedTask>
    ): PetStatistics {
        val taskIds = petTasks.map { it.taskId }
        val petCompletedTasks = allCompletedTasks.filter { it.taskId in taskIds }
        
        val totalTasks = petTasks.size
        val completedCount = petCompletedTasks.size
        val activeTasks = petTasks.count { it.isActive }
        
        val completionRate = if (totalTasks > 0) {
            (completedCount.toFloat() / totalTasks.toFloat()) * 100f
        } else {
            0f
        }

        return PetStatistics(
            petId = petId,
            petName = petName,
            totalTasks = totalTasks,
            completedTasks = completedCount,
            completionRate = completionRate,
            activeTasks = activeTasks
        )
    }

    /**
     * Calculate current and longest streaks
     */
    private fun calculateStreaks(completedTasks: List<CompletedTask>): Pair<Int, Int> {
        if (completedTasks.isEmpty()) return Pair(0, 0)

        val calendar = Calendar.getInstance()
        val today = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        // Get unique completion dates (by day)
        val completionDates = completedTasks.map { completed ->
            calendar.timeInMillis = completed.completedAt
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            calendar.timeInMillis
        }.distinct().sortedDescending()

        if (completionDates.isEmpty()) return Pair(0, 0)

        // Calculate current streak
        var currentStreak = 0
        var checkDate = today
        for (date in completionDates) {
            val daysDiff = ((checkDate - date) / (1000 * 60 * 60 * 24)).toInt()
            if (daysDiff == currentStreak) {
                currentStreak++
                checkDate = date - (1000 * 60 * 60 * 24) // Move to previous day
            } else {
                break
            }
        }

        // Calculate longest streak
        var longestStreak = 1
        var currentLongestStreak = 1
        for (i in 1 until completionDates.size) {
            val daysDiff = ((completionDates[i - 1] - completionDates[i]) / (1000 * 60 * 60 * 24)).toInt()
            if (daysDiff == 1) {
                currentLongestStreak++
                longestStreak = maxOf(longestStreak, currentLongestStreak)
            } else {
                currentLongestStreak = 1
            }
        }

        return Pair(currentStreak, longestStreak)
    }

    /**
     * Get recent completion counts by day
     */
    private fun getRecentCompletions(
        completedTasks: List<CompletedTask>,
        days: Int
    ): List<CompletionEntry> {
        val calendar = Calendar.getInstance()
        val today = calendar.apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val completionByDate = mutableMapOf<Long, Int>()

        // Initialize all dates with 0
        for (i in 0 until days) {
            val date = today - (i * 24 * 60 * 60 * 1000L)
            completionByDate[date] = 0
        }

        // Count completions per day
        completedTasks.forEach { completed ->
            calendar.timeInMillis = completed.completedAt
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val date = calendar.timeInMillis
            
            if (completionByDate.containsKey(date)) {
                completionByDate[date] = completionByDate[date]!! + 1
            }
        }

        return completionByDate.entries
            .sortedBy { it.key }
            .map { CompletionEntry(it.key, it.value) }
    }
}

