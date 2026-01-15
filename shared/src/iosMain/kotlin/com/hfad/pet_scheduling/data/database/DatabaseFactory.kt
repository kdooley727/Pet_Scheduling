package com.hfad.pet_scheduling.data.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.native.NativeSqliteDriver
import com.hfad.pet_scheduling.database.PetSchedulingDatabase

actual class DatabaseDriverFactory {
    actual fun createDriver(): SqlDriver {
        return NativeSqliteDriver(
            PetSchedulingDatabase.Schema,
            "pet_scheduling_database.db"
        )
    }
}

