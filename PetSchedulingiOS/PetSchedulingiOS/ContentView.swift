//
//  ContentView.swift
//  PetSchedulingiOS
//

import SwiftUI
import FirebaseAuth

struct ContentView: View {
    @EnvironmentObject var authState: AuthState
    
    var body: some View {
        Group {
            if authState.isAuthenticated {
                PetListView()
                    .task { await saveUserProfileIfNeeded() }
            } else {
                LoginView()
            }
        }
        .environmentObject(authState)
    }
    
    private func saveUserProfileIfNeeded() async {
        guard let user = Auth.auth().currentUser else { return }
        let email = user.email ?? ""
        guard !email.isEmpty else { return }
        try? await FirebaseService.shared.saveUserProfile(email: email, displayName: user.displayName)
    }
}

#Preview {
    ContentView()
        .environmentObject(AuthState())
}
