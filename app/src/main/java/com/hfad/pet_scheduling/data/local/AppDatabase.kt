package com.hfad.pet_scheduling.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.hfad.pet_scheduling.data.local.dao.*
import com.hfad.pet_scheduling.data.local.entities.*

@Database(
    entities = [
        Pet::class,
        ScheduleTask::class,
        CompletedTask::class,
        SharedAccess::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun petDao(): PetDao
    abstract fun taskDao(): TaskDao
    abstract fun completedTaskDao(): CompletedTaskDao
    abstract fun sharedAccessDao(): SharedAccessDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        // Migration from version 1 to 2: Add emergency contact fields to Pet table
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add new columns for emergency contacts
                database.execSQL("ALTER TABLE pets ADD COLUMN vetName TEXT")
                database.execSQL("ALTER TABLE pets ADD COLUMN vetPhone TEXT")
                database.execSQL("ALTER TABLE pets ADD COLUMN vetEmail TEXT")
                database.execSQL("ALTER TABLE pets ADD COLUMN vetAddress TEXT")
                database.execSQL("ALTER TABLE pets ADD COLUMN emergencyContactName TEXT")
                database.execSQL("ALTER TABLE pets ADD COLUMN emergencyContactPhone TEXT")
                database.execSQL("ALTER TABLE pets ADD COLUMN emergencyContactEmail TEXT")
                database.execSQL("ALTER TABLE pets ADD COLUMN emergencyContactRelationship TEXT")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                try {
                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "pet_scheduling_database"
                    )
                        .addMigrations(MIGRATION_1_2)
                        .fallbackToDestructiveMigration() // Only as fallback if migration fails
                        .build()
                    INSTANCE = instance
                    android.util.Log.d("AppDatabase", "Database instance created successfully")
                    instance
                } catch (e: Exception) {
                    android.util.Log.e("AppDatabase", "Error creating database", e)
                    // If database creation fails, try with destructive migration
                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "pet_scheduling_database"
                    )
                        .fallbackToDestructiveMigration()
                        .build()
                    INSTANCE = instance
                    android.util.Log.d("AppDatabase", "Database recreated with destructive migration")
                    instance
                }
            }
        }

        /**
         * Clear the database instance (useful for testing or when database is corrupted)
         */
        fun clearInstance() {
            synchronized(this) {
                INSTANCE?.close()
                INSTANCE = null
                android.util.Log.d("AppDatabase", "Database instance cleared")
            }
        }
    }
}

