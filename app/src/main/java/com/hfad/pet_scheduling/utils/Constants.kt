package com.hfad.pet_scheduling.utils

object Constants {
    // Task Categories
    object TaskCategory {
        const val FEEDING = "feeding"
        const val MEDICATION = "medication"
        const val VET_VISIT = "vet_visit"
        const val GROOMING = "grooming"
        const val EXERCISE = "exercise"
        const val TRAINING = "training"
        const val REMINDER = "reminder"
        const val OTHER = "other"

        val ALL_CATEGORIES = listOf(
            FEEDING,
            MEDICATION,
            VET_VISIT,
            GROOMING,
            EXERCISE,
            TRAINING,
            REMINDER,
            OTHER
        )

        fun getDisplayName(category: String): String {
            return when (category) {
                FEEDING -> "Feeding"
                MEDICATION -> "Medication"
                VET_VISIT -> "Vet Visit"
                GROOMING -> "Grooming"
                EXERCISE -> "Exercise"
                TRAINING -> "Training"
                REMINDER -> "Reminder"
                OTHER -> "Other"
                else -> category
            }
        }
    }

    // Recurrence Patterns
    object RecurrencePattern {
        const val NONE = "none"
        const val DAILY = "daily"
        const val WEEKLY = "weekly"
        const val MONTHLY = "monthly"
        const val YEARLY = "yearly"
        const val CUSTOM = "custom"

        val ALL_PATTERNS = listOf(
            NONE,
            DAILY,
            WEEKLY,
            MONTHLY,
            YEARLY,
            CUSTOM
        )

        fun getDisplayName(pattern: String): String {
            return when (pattern) {
                NONE -> "No Repeat"
                DAILY -> "Daily"
                WEEKLY -> "Weekly"
                MONTHLY -> "Monthly"
                YEARLY -> "Yearly"
                CUSTOM -> "Custom"
                else -> pattern
            }
        }
    }

    // Permission Levels
    object PermissionLevel {
        const val VIEW = "view"
        const val EDIT = "edit"
        const val MANAGE = "manage"

        val ALL_LEVELS = listOf(VIEW, EDIT, MANAGE)

        fun getDisplayName(level: String): String {
            return when (level) {
                VIEW -> "View Only"
                EDIT -> "Can Edit"
                MANAGE -> "Full Access"
                else -> level
            }
        }
    }

    // Pet Types
    object PetType {
        const val DOG = "dog"
        const val CAT = "cat"
        const val BIRD = "bird"
        const val FISH = "fish"
        const val RABBIT = "rabbit"
        const val HAMSTER = "hamster"
        const val OTHER = "other"

        val ALL_TYPES = listOf(
            DOG,
            CAT,
            BIRD,
            FISH,
            RABBIT,
            HAMSTER,
            OTHER
        )

        fun getDisplayName(type: String): String {
            return when (type) {
                DOG -> "Dog"
                CAT -> "Cat"
                BIRD -> "Bird"
                FISH -> "Fish"
                RABBIT -> "Rabbit"
                HAMSTER -> "Hamster"
                OTHER -> "Other"
                else -> type
            }
        }
    }

    // Default Reminder Times (in minutes before task)
    object ReminderTimes {
        const val MINUTES_5 = 5
        const val MINUTES_15 = 15
        const val MINUTES_30 = 30
        const val MINUTES_60 = 60
        const val MINUTES_120 = 120

        val ALL_TIMES = listOf(
            MINUTES_5,
            MINUTES_15,
            MINUTES_30,
            MINUTES_60,
            MINUTES_120
        )
    }

    // Database
    const val DATABASE_NAME = "pet_scheduling_database"
    const val DATABASE_VERSION = 1

    // Notification Channels
    const val NOTIFICATION_CHANNEL_ID = "pet_scheduling_notifications"
    const val NOTIFICATION_CHANNEL_NAME = "Pet Scheduling Reminders"

    // WorkManager Tags
    const val REMINDER_WORK_TAG = "reminder_work"
}

