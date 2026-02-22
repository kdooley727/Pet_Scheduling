//
//  PetSchedulingiOSApp.swift
//  PetSchedulingiOS
//

import SwiftUI
import UserNotifications
import FirebaseCore
import GoogleSignIn

@main
struct PetSchedulingiOSApp: App {
    @StateObject private var authState = AuthState()
    @AppStorage("appearanceMode") private var appearanceMode = "system"
    
    init() {
        FirebaseApp.configure()
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .badge, .sound]) { _, _ in }
    }
    
    var body: some Scene {
        WindowGroup {
            ThemeProviderView()
                .environmentObject(authState)
                .preferredColorScheme(appearanceMode == "system" ? nil : (appearanceMode == "dark" ? .dark : .light))
                .onOpenURL { url in
                    GIDSignIn.sharedInstance.handle(url)
                }
        }
    }
}
