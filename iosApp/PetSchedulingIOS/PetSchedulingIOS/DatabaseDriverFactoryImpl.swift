import shared
import SQLDelight

final class DatabaseDriverFactoryImpl: DatabaseDriverFactory {
    func createDriver() -> SqlDriver {
        NativeSqliteDriver(
            schema: PetSchedulingDatabase.Companion.shared.Schema,
            name: "pet_scheduling_database.db"
        )
    }
}
