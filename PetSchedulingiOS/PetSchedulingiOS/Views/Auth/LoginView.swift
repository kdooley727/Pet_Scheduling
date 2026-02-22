//
//  LoginView.swift
//  PetSchedulingiOS
//

import SwiftUI
import FirebaseAuth

struct LoginView: View {
    @EnvironmentObject var authState: AuthState
    @Environment(\.appTheme) private var theme
    @State private var email = ""
    @State private var password = ""
    @State private var isLoading = false
    @State private var errorMessage: String?
    @State private var showSignUp = false
    
    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 24) {
                    VStack(spacing: 12) {
                        ZStack {
                            RoundedRectangle(cornerRadius: 24)
                                .fill(theme.gradient.opacity(0.2))
                                .frame(width: 80, height: 80)
                            Image(systemName: "pawprint.circle.fill")
                                .font(.system(size: 48))
                                .foregroundStyle(theme.gradient)
                        }
                        Text("Pet Scheduling")
                            .font(.largeTitle.bold())
                            .foregroundStyle(.primary)
                        
                        Text("Sign in to manage your pets")
                            .font(.body)
                            .foregroundStyle(.secondary)
                    }
                    .padding(.top, 48)
                    .padding(.bottom, 32)
                    
                    VStack(spacing: 16) {
                        TextField("Email", text: $email)
                            .textFieldStyle(.roundedBorder)
                            .textContentType(.emailAddress)
                            .autocapitalization(.none)
                            .keyboardType(.emailAddress)
                            .accessibilityLabel("Email")
                        
                        SecureField("Password", text: $password)
                            .textFieldStyle(.roundedBorder)
                            .textContentType(.password)
                            .accessibilityLabel("Password")
                        
                        Button("Forgot Password?") {
                            sendPasswordReset()
                        }
                        .font(.subheadline)
                        .frame(maxWidth: .infinity, alignment: .trailing)
                        
                        if let error = errorMessage {
                            Text(error)
                                .font(.caption)
                                .foregroundStyle(.red)
                        }
                        
                        Button(action: signIn) {
                            if isLoading {
                                ProgressView()
                                    .progressViewStyle(CircularProgressViewStyle(tint: .white))
                                    .frame(maxWidth: .infinity)
                                    .padding()
                            } else {
                                Text("Sign In")
                                    .frame(maxWidth: .infinity)
                                    .padding()
                            }
                        }
                        .buttonStyle(.borderedProminent)
                        .controlSize(.large)
                        .disabled(isLoading || email.isEmpty || password.isEmpty)
                        .accessibilityLabel("Sign In")
                        .accessibilityHint("Sign in with email and password")
                        
                        Button("Create Account") {
                            showSignUp = true
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
                            .background(theme.accentColor)
                            .foregroundStyle(.white)
                            .cornerRadius(12)
                            .shadow(color: theme.accentColor.opacity(0.3), radius: 4, x: 0, y: 2)
                        }
                        .disabled(isLoading)
                        .accessibilityLabel("Continue with Google")
                        .accessibilityHint("Sign in with Google account")
                    }
                    .padding(.horizontal, 24)
                }
            }
            .navigationDestination(isPresented: $showSignUp) {
                SignUpView()
            }
        }
    }
    
    private func signIn() {
        guard !email.isEmpty, !password.isEmpty else {
            errorMessage = "Please enter email and password"
            return
        }
        guard email.contains("@") else {
            errorMessage = "Invalid email format"
            return
        }
        guard password.count >= 6 else {
            errorMessage = "Password must be at least 6 characters"
            return
        }
        
        isLoading = true
        errorMessage = nil
        
        Auth.auth().signIn(withEmail: email, password: password) { result, error in
            isLoading = false
            if let error = error {
                errorMessage = "Sign in failed: \(error.localizedDescription)"
                return
            }
            authState.isAuthenticated = result?.user != nil
        }
    }
    
    private func sendPasswordReset() {
        guard !email.isEmpty else {
            errorMessage = "Please enter your email first"
            return
        }
        Auth.auth().sendPasswordReset(withEmail: email) { error in
            if error == nil {
                errorMessage = nil
                // Could show success toast
            } else {
                errorMessage = error?.localizedDescription
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
