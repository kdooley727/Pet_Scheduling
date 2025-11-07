package com.hfad.pet_scheduling.utils

import org.junit.Assert.*
import org.junit.Test

class ConstantsTest {
    
    @Test
    fun `PetType getDisplayName returns correct display name`() {
        // Test a few pet types
        assertEquals("Dog", Constants.PetType.getDisplayName("dog"))
        assertEquals("Cat", Constants.PetType.getDisplayName("cat"))
        assertEquals("Bird", Constants.PetType.getDisplayName("bird"))
        assertEquals("Other", Constants.PetType.getDisplayName("other"))
    }
    
    @Test
    fun `TaskCategory getDisplayName returns correct display name`() {
        // Test a few categories
        assertEquals("Feeding", Constants.TaskCategory.getDisplayName("feeding"))
        assertEquals("Exercise", Constants.TaskCategory.getDisplayName("exercise"))
        assertEquals("Grooming", Constants.TaskCategory.getDisplayName("grooming"))
        assertEquals("Other", Constants.TaskCategory.getDisplayName("other"))
    }
    
    @Test
    fun `RecurrencePattern getDisplayName returns correct display name`() {
        // Test recurrence patterns
        assertEquals("Daily", Constants.RecurrencePattern.getDisplayName("daily"))
        assertEquals("Weekly", Constants.RecurrencePattern.getDisplayName("weekly"))
        assertEquals("Monthly", Constants.RecurrencePattern.getDisplayName("monthly"))
        assertEquals("No Repeat", Constants.RecurrencePattern.getDisplayName("none"))
        assertEquals("Yearly", Constants.RecurrencePattern.getDisplayName("yearly"))
    }
    
    @Test
    fun `ALL_TYPES contains expected pet types`() {
        assertTrue(Constants.PetType.ALL_TYPES.contains("dog"))
        assertTrue(Constants.PetType.ALL_TYPES.contains("cat"))
        assertTrue(Constants.PetType.ALL_TYPES.contains("bird"))
        assertTrue(Constants.PetType.ALL_TYPES.isNotEmpty())
    }
    
    @Test
    fun `ALL_CATEGORIES contains expected task categories`() {
        assertTrue(Constants.TaskCategory.ALL_CATEGORIES.contains("feeding"))
        assertTrue(Constants.TaskCategory.ALL_CATEGORIES.contains("exercise"))
        assertTrue(Constants.TaskCategory.ALL_CATEGORIES.contains("grooming"))
        assertTrue(Constants.TaskCategory.ALL_CATEGORIES.isNotEmpty())
    }
}

