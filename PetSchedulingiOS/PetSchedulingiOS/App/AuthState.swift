//
//  AuthState.swift
//  PetSchedulingiOS
//

import Foundation
import FirebaseAuth

@MainActor
final class AuthState: ObservableObject {
    @Published var isAuthenticated = false
    
    private var authListener: AuthStateDidChangeListenerHandle?
    
    init() {
        isAuthenticated = Auth.auth().currentUser != nil
        authListener = Auth.auth().addStateDidChangeListener { [weak self] _, user in
            Task { @MainActor in
                self?.isAuthenticated = user != nil
            }
        }
    }
    
    deinit {
        if let listener = authListener {
            Auth.auth().removeStateDidChangeListener(listener)
        }
    }
    
    func signOut() {
        try? Auth.auth().signOut()
        isAuthenticated = false
    }
}
