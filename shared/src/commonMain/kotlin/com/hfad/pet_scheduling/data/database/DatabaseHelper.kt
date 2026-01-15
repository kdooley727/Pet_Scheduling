package com.hfad.pet_scheduling.data.database

import com.hfad.pet_scheduling.database.PetSchedulingDatabase

class DatabaseHelper(private val driverFactory: DatabaseDriverFactory) {
    val database: PetSchedulingDatabase by lazy {
        PetSchedulingDatabase(driverFactory.createDriver())
    }
}

