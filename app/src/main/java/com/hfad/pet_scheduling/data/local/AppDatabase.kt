package com.hfad.pet_scheduling.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.hfad.pet_scheduling.data.local.dao.*
import com.hfad.pet_scheduling.data.local.entities.*

@Database(
    entities = [
        Pet::class,
        ScheduleTask::class,
        CompletedTask::class,
        SharedAccess::class
    ],
    version = 1,
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

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "pet_scheduling_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}

