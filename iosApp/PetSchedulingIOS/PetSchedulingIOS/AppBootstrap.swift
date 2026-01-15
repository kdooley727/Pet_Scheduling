import FirebaseCore
import shared

enum AppBootstrap {
    static func configure() {
        FirebaseApp.configure()
        let driverFactory = DatabaseDriverFactoryImpl()
        _ = DatabaseHelper(driverFactory: driverFactory)
    }
}
