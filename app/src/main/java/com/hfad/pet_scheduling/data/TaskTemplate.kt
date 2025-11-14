package com.hfad.pet_scheduling.data

import com.hfad.pet_scheduling.utils.Constants

/**
 * Template for creating common pet care tasks
 */
data class TaskTemplate(
    val id: String,
    val name: String,
    val description: String?,
    val category: String,
    val defaultTime: String, // Format: "HH:mm" (e.g., "08:00")
    val recurrencePattern: String = Constants.RecurrencePattern.DAILY,
    val reminderMinutesBefore: Int = Constants.ReminderTimes.MINUTES_15,
    val iconResId: Int? = null
) {
    companion object {
        /**
         * Get default templates for common pet care routines
         */
        fun getDefaultTemplates(): List<TaskTemplate> {
            return listOf(
                // Feeding Templates
                TaskTemplate(
                    id = "morning_feeding",
                    name = "Morning Feeding",
                    description = "Feed your pet breakfast",
                    category = Constants.TaskCategory.FEEDING,
                    defaultTime = "08:00",
                    recurrencePattern = Constants.RecurrencePattern.DAILY
                ),
                TaskTemplate(
                    id = "evening_feeding",
                    name = "Evening Feeding",
                    description = "Feed your pet dinner",
                    category = Constants.TaskCategory.FEEDING,
                    defaultTime = "18:00",
                    recurrencePattern = Constants.RecurrencePattern.DAILY
                ),
                
                // Medication Templates
                TaskTemplate(
                    id = "morning_medication",
                    name = "Morning Medication",
                    description = "Give morning medication",
                    category = Constants.TaskCategory.MEDICATION,
                    defaultTime = "09:00",
                    recurrencePattern = Constants.RecurrencePattern.DAILY,
                    reminderMinutesBefore = Constants.ReminderTimes.MINUTES_30
                ),
                TaskTemplate(
                    id = "evening_medication",
                    name = "Evening Medication",
                    description = "Give evening medication",
                    category = Constants.TaskCategory.MEDICATION,
                    defaultTime = "20:00",
                    recurrencePattern = Constants.RecurrencePattern.DAILY,
                    reminderMinutesBefore = Constants.ReminderTimes.MINUTES_30
                ),
                
                // Exercise Templates
                TaskTemplate(
                    id = "morning_walk",
                    name = "Morning Walk",
                    description = "Take your pet for a morning walk",
                    category = Constants.TaskCategory.EXERCISE,
                    defaultTime = "07:00",
                    recurrencePattern = Constants.RecurrencePattern.DAILY
                ),
                TaskTemplate(
                    id = "evening_walk",
                    name = "Evening Walk",
                    description = "Take your pet for an evening walk",
                    category = Constants.TaskCategory.EXERCISE,
                    defaultTime = "17:00",
                    recurrencePattern = Constants.RecurrencePattern.DAILY
                ),
                TaskTemplate(
                    id = "playtime",
                    name = "Playtime",
                    description = "Interactive play session",
                    category = Constants.TaskCategory.EXERCISE,
                    defaultTime = "15:00",
                    recurrencePattern = Constants.RecurrencePattern.DAILY
                ),
                
                // Grooming Templates
                TaskTemplate(
                    id = "bath",
                    name = "Bath",
                    description = "Give your pet a bath",
                    category = Constants.TaskCategory.GROOMING,
                    defaultTime = "14:00",
                    recurrencePattern = Constants.RecurrencePattern.WEEKLY
                ),
                TaskTemplate(
                    id = "brushing",
                    name = "Brushing",
                    description = "Brush your pet's fur",
                    category = Constants.TaskCategory.GROOMING,
                    defaultTime = "10:00",
                    recurrencePattern = Constants.RecurrencePattern.DAILY
                ),
                TaskTemplate(
                    id = "nail_trim",
                    name = "Nail Trimming",
                    description = "Trim your pet's nails",
                    category = Constants.TaskCategory.GROOMING,
                    defaultTime = "11:00",
                    recurrencePattern = Constants.RecurrencePattern.MONTHLY
                ),
                
                // Vet Visit Templates
                TaskTemplate(
                    id = "vet_checkup",
                    name = "Vet Checkup",
                    description = "Regular veterinary checkup",
                    category = Constants.TaskCategory.VET_VISIT,
                    defaultTime = "10:00",
                    recurrencePattern = Constants.RecurrencePattern.MONTHLY
                ),
                TaskTemplate(
                    id = "vaccination",
                    name = "Vaccination",
                    description = "Annual vaccination appointment",
                    category = Constants.TaskCategory.VET_VISIT,
                    defaultTime = "10:00",
                    recurrencePattern = Constants.RecurrencePattern.YEARLY
                ),
                
                // Training Templates
                TaskTemplate(
                    id = "training_session",
                    name = "Training Session",
                    description = "Training and obedience practice",
                    category = Constants.TaskCategory.TRAINING,
                    defaultTime = "16:00",
                    recurrencePattern = Constants.RecurrencePattern.DAILY
                ),
                
                // Other Templates
                TaskTemplate(
                    id = "water_refill",
                    name = "Water Refill",
                    description = "Refill water bowl",
                    category = Constants.TaskCategory.OTHER,
                    defaultTime = "08:00",
                    recurrencePattern = Constants.RecurrencePattern.DAILY
                ),
                TaskTemplate(
                    id = "litter_box",
                    name = "Litter Box Cleaning",
                    description = "Clean the litter box",
                    category = Constants.TaskCategory.OTHER,
                    defaultTime = "09:00",
                    recurrencePattern = Constants.RecurrencePattern.DAILY
                )
            )
        }
        
        /**
         * Get templates filtered by category
         */
        fun getTemplatesByCategory(category: String): List<TaskTemplate> {
            return getDefaultTemplates().filter { it.category == category }
        }
    }
}

