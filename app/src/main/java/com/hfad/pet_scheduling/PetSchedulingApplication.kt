package com.hfad.pet_scheduling

import android.app.Application
import android.util.Log
import com.hfad.pet_scheduling.data.database.DatabaseDriverFactory
import com.hfad.pet_scheduling.data.database.DatabaseHelper
import com.hfad.pet_scheduling.data.repository.PetRepository
import com.hfad.pet_scheduling.data.repository.ScheduleRepository
import com.hfad.pet_scheduling.utils.CloudSyncManager

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

    // Database helper using SQLDelight
    private val databaseHelper by lazy {
        try {
            val driverFactory = DatabaseDriverFactory(this)
            DatabaseHelper(driverFactory)
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing DatabaseHelper", e)
            throw e
        }
    }

    val database get() = databaseHelper.database

    // Repositories
    val petRepository by lazy {
        try {
            PetRepository(database)
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing PetRepository", e)
            throw e
        }
    }

    val scheduleRepository by lazy {
        try {
            ScheduleRepository(database)
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing ScheduleRepository", e)
            throw e
        }
    }

    val cloudSyncManager by lazy {
        try {
            CloudSyncManager(this)
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing CloudSyncManager", e)
            throw e
        }
    }
}

