package com.hfad.pet_scheduling

import android.app.Application
import android.util.Log
import com.hfad.pet_scheduling.data.local.AppDatabase
import com.hfad.pet_scheduling.data.repository.PetRepository
import com.hfad.pet_scheduling.data.repository.ScheduleRepository

class PetSchedulingApplication : Application() {
    companion object {
        private const val TAG = "PetSchedulingApp"
    }

    override fun onCreate() {
        super.onCreate()
        try {
            // Initialize Firebase (it auto-initializes with google-services.json, but we can verify)
            Log.d(TAG, "Application onCreate started")
        } catch (e: Exception) {
            Log.e(TAG, "Error in Application onCreate", e)
        }
    }

    // Database instance
    val database by lazy {
        try {
            AppDatabase.getDatabase(this)
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing database", e)
            throw e
        }
    }

    // Repositories
    val petRepository by lazy {
        try {
            PetRepository(
                database.petDao(),
                database.sharedAccessDao()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing PetRepository", e)
            throw e
        }
    }

    val scheduleRepository by lazy {
        try {
            ScheduleRepository(
                database.taskDao(),
                database.completedTaskDao()
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing ScheduleRepository", e)
            throw e
        }
    }
}

