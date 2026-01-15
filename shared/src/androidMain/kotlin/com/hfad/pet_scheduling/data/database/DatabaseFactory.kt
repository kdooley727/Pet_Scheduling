package com.hfad.pet_scheduling.data.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.hfad.pet_scheduling.database.PetSchedulingDatabase

actual class DatabaseDriverFactory(private val context: Context) {
    actual fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema = PetSchedulingDatabase.Schema,
            context = context,
            name = "pet_scheduling_database.db"
        )
    }
}
