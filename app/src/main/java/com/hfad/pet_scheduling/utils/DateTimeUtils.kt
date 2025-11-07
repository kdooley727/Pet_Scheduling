package com.hfad.pet_scheduling.utils

import java.text.SimpleDateFormat
import java.util.*

object DateTimeUtils {
    private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val timeFormat = SimpleDateFormat("hh:mm a", Locale.getDefault())
    private val dateTimeFormat = SimpleDateFormat("MMM dd, yyyy 'at' hh:mm a", Locale.getDefault())
    private val shortDateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())

    /**
     * Format timestamp to date string (e.g., "Jan 15, 2024")
     */
    fun formatDate(timestamp: Long): String {
        return dateFormat.format(Date(timestamp))
    }

    /**
     * Format timestamp to time string (e.g., "03:45 PM")
     */
    fun formatTime(timestamp: Long): String {
        return timeFormat.format(Date(timestamp))
    }

    /**
     * Format timestamp to date and time string (e.g., "Jan 15, 2024 at 03:45 PM")
     */
    fun formatDateTime(timestamp: Long): String {
        return dateTimeFormat.format(Date(timestamp))
    }

    /**
     * Format timestamp to short date string (e.g., "01/15/2024")
     */
    fun formatShortDate(timestamp: Long): String {
        return shortDateFormat.format(Date(timestamp))
    }

    /**
     * Get current timestamp in milliseconds
     */
    fun getCurrentTimestamp(): Long {
        return System.currentTimeMillis()
    }

    /**
     * Get start of day timestamp (00:00:00)
     */
    fun getStartOfDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    /**
     * Get end of day timestamp (23:59:59)
     */
    fun getEndOfDay(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }

    /**
     * Get start of week (Monday) timestamp
     */
    fun getStartOfWeek(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    /**
     * Get end of week (Sunday) timestamp
     */
    fun getEndOfWeek(timestamp: Long): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)
        return calendar.timeInMillis
    }

    /**
     * Add days to a timestamp
     */
    fun addDays(timestamp: Long, days: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.add(Calendar.DAY_OF_YEAR, days)
        return calendar.timeInMillis
    }

    /**
     * Add hours to a timestamp
     */
    fun addHours(timestamp: Long, hours: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.add(Calendar.HOUR_OF_DAY, hours)
        return calendar.timeInMillis
    }

    /**
     * Add minutes to a timestamp
     */
    fun addMinutes(timestamp: Long, minutes: Int): Long {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = timestamp
        calendar.add(Calendar.MINUTE, minutes)
        return calendar.timeInMillis
    }

    /**
     * Calculate next occurrence based on recurrence pattern
     */
    fun getNextOccurrence(
        startTime: Long,
        recurrencePattern: String,
        recurrenceInterval: Int = 1
    ): Long {
        return when (recurrencePattern) {
            Constants.RecurrencePattern.DAILY -> addDays(startTime, recurrenceInterval)
            Constants.RecurrencePattern.WEEKLY -> addDays(startTime, 7 * recurrenceInterval)
            Constants.RecurrencePattern.MONTHLY -> {
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = startTime
                calendar.add(Calendar.MONTH, recurrenceInterval)
                calendar.timeInMillis
            }
            Constants.RecurrencePattern.YEARLY -> {
                val calendar = Calendar.getInstance()
                calendar.timeInMillis = startTime
                calendar.add(Calendar.YEAR, recurrenceInterval)
                calendar.timeInMillis
            }
            else -> startTime
        }
    }

    /**
     * Check if a timestamp is today
     */
    fun isToday(timestamp: Long): Boolean {
        val today = Calendar.getInstance()
        val date = Calendar.getInstance()
        date.timeInMillis = timestamp
        
        return today.get(Calendar.YEAR) == date.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == date.get(Calendar.DAY_OF_YEAR)
    }

    /**
     * Check if a timestamp is in the past
     */
    fun isPast(timestamp: Long): Boolean {
        return timestamp < System.currentTimeMillis()
    }

    /**
     * Check if a timestamp is in the future
     */
    fun isFuture(timestamp: Long): Boolean {
        return timestamp > System.currentTimeMillis()
    }
}

