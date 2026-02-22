//
//  SignUpView.swift
//  PetSchedulingiOS
//

import SwiftUI
import FirebaseAuth

struct SignUpView: View {
    @EnvironmentObject var authState: AuthState
    @Environment(\.dismiss) private var dismiss
    @State private var displayName = ""
    @State private var email = ""
    @State private var password = ""
    @State private var confirmPassword = ""
    @State private var isLoading = false
    @State private var errorMessage: String?
    
    var body: some View {
        ScrollView {
            VStack(spacing: 24) {
                VStack(spacing: 8) {
                        Text("Create Account")
                            .font(.largeTitle.bold())
                            .foregroundStyle(.primary)
                    
                    Text("Join us to manage your pets")
                        .font(.body)
                        .foregroundStyle(.secondary)
                }
                .padding(.top, 48)
                .padding(.bottom, 32)
                
                VStack(spacing: 16) {
                    TextField("Display Name", text: $displayName)
                        .textFieldStyle(.roundedBorder)
                        .textContentType(.name)
                    
                    TextField("Email", text: $email)
                        .textFieldStyle(.roundedBorder)
                        .textContentType(.emailAddress)
                        .autocapitalization(.none)
                        .keyboardType(.emailAddress)
                    
                    SecureField("Password", text: $password)
                        .textFieldStyle(.roundedBorder)
                        .textContentType(.newPassword)
                    
                    SecureField("Confirm Password", text: $confirmPassword)
                        .textFieldStyle(.roundedBorder)
                        .textContentType(.newPassword)
                    
                    if let error = errorMessage {
                        Text(error)
                            .font(.caption)
                            .foregroundStyle(.red)
                    }
                    
                    Button(action: signUp) {
                        if isLoading {
                            ProgressView()
                                .progressViewStyle(CircularProgressViewStyle(tint: .white))
                                .frame(maxWidth: .infinity)
                                .padding()
                        } else {
                            Text("Create Account")
                                .frame(maxWidth: .infinity)
                                .padding()
                        }
                    }
                    .buttonStyle(.borderedProminent)
                    .disabled(isLoading || !isValid)
                    
                    Button("Back to Sign In") {
                        dismiss()
                    }
                    .buttonStyle(.bordered)
                    
                    Text("OR")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                        .padding(.top, 8)
                    
                    Button(action: signInWithGoogle) {
                        HStack {
                            Image(systemName: "globe")
                            Text("Continue with Google")
                        }
                        .frame(maxWidth: .infinity)
                        .padding()
                        .background(Color.accentColor)
                        .foregroundStyle(.white)
                        .cornerRadius(8)
                    }
                    .disabled(isLoading)
                }
                .padding(.horizontal, 24)
            }
        }
        .navigationBarBackButtonHidden(false)
    }
    
    private var isValid: Bool {
        !email.isEmpty && email.contains("@") &&
        password.count >= 6 && password == confirmPassword
    }
    
    private func signUp() {
        guard !email.isEmpty, email.contains("@") else {
            errorMessage = "Invalid email format"
            return
        }
        guard password.count >= 6 else {
            errorMessage = "Password must be at least 6 characters"
            return
        }
        guard password == confirmPassword else {
            errorMessage = "Passwords do not match"
            return
        }
        
        isLoading = true
        errorMessage = nil
        
        Auth.auth().createUser(withEmail: email, password: password) { result, error in
            if let error = error {
                isLoading = false
                errorMessage = "Sign up failed: \(error.localizedDescription)"
                return
            }
            if let user = result?.user, !displayName.isEmpty {
                let changeRequest = user.createProfileChangeRequest()
                changeRequest.displayName = displayName
                changeRequest.commitChanges { _ in
                    isLoading = false
                    authState.isAuthenticated = true
                }
            } else {
                isLoading = false
                authState.isAuthenticated = result?.user != nil
            }
        }
    }
    
    private func signInWithGoogle() {
        isLoading = true
        errorMessage = nil
        
        Task {
            do {
                try await GoogleSignInHelper.signIn()
                await MainActor.run {
                    authState.isAuthenticated = Auth.auth().currentUser != nil
                }
            } catch {
                await MainActor.run {
                    errorMessage = error.localizedDescription
                }
            }
            await MainActor.run {
                isLoading = false
            }
        }
    }
}
