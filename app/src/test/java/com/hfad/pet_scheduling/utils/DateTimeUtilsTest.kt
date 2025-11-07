package com.hfad.pet_scheduling.utils

import org.junit.Assert.*
import org.junit.Test
import java.util.*

class DateTimeUtilsTest {
    
    @Test
    fun `formatDate formats date correctly`() {
        // Given
        val calendar = Calendar.getInstance().apply {
            set(2025, Calendar.JANUARY, 15, 0, 0, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val timestamp = calendar.timeInMillis
        
        // When
        val formatted = DateTimeUtils.formatDate(timestamp)
        
        // Then
        assertTrue(formatted.contains("Jan"))
        assertTrue(formatted.contains("15"))
        assertTrue(formatted.contains("2025"))
    }
    
    @Test
    fun `formatTime formats time correctly`() {
        // Given
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 14)
            set(Calendar.MINUTE, 30)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val timestamp = calendar.timeInMillis
        
        // When
        val formatted = DateTimeUtils.formatTime(timestamp)
        
        // Then
        // Should contain time in some format (could be 2:30 PM or 14:30)
        assertTrue(formatted.contains("2") || formatted.contains("14"))
        assertTrue(formatted.contains("30"))
    }
    
    @Test
    fun `formatDateTime formats date and time`() {
        // Given
        val calendar = Calendar.getInstance().apply {
            set(2025, Calendar.JANUARY, 15, 14, 30, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val timestamp = calendar.timeInMillis
        
        // When
        val formatted = DateTimeUtils.formatDateTime(timestamp)
        
        // Then
        assertTrue(formatted.contains("Jan") || formatted.contains("January"))
        assertTrue(formatted.contains("15"))
    }
}

