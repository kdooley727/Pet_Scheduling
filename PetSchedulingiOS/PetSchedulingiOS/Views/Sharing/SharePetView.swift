//
//  SharePetView.swift
//  PetSchedulingiOS
//

import SwiftUI
import UIKit

struct SharePetView: View {
    let pet: Pet
    @State private var shareEmail = ""
    @State private var permissionLevel = "view"
    @State private var isSharing = false
    @State private var message = ""
    
    var body: some View {
        NavigationStack {
            Form {
                Section("Pet") {
                    HStack {
                        if let urlString = pet.photoUrl, let url = URL(string: urlString) {
                            AsyncImage(url: url) { image in
                                image.resizable().aspectRatio(contentMode: .fill)
                            } placeholder: {
                                Image(systemName: "pawprint.circle.fill")
                                    .font(.title)
                            }
                            .frame(width: 50, height: 50)
                            .clipShape(Circle())
                        } else {
                            Image(systemName: "pawprint.circle.fill")
                                .font(.system(size: 50))
                                .foregroundStyle(.secondary)
                        }
                        Text(pet.name)
                            .font(.headline)
                    }
                }
                
                Section("Share With") {
                    TextField("Email address", text: $shareEmail)
                        .keyboardType(.emailAddress)
                        .textContentType(.emailAddress)
                        .autocapitalization(.none)
                    
                    Picker("Permission", selection: $permissionLevel) {
                        Text("View Only").tag("view")
                        Text("Can Edit").tag("edit")
                        Text("Full Access").tag("manage")
                    }
                    .pickerStyle(.menu)
                }
                
                if !message.isEmpty {
                    Section {
                        Text(message)
                            .foregroundStyle(message.contains("Error") ? .red : .green)
                    }
                }
            }
            .navigationTitle("Share Pet")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .primaryAction) {
                    Button {
                        shareToOtherApps()
                    } label: {
                        Label("Share", systemImage: "square.and.arrow.up")
                    }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Share Access") {
                        sharePet()
                    }
                    .disabled(isSharing || shareEmail.isEmpty)
                }
            }
        }
    }
    
    private func sharePet() {
        let email = shareEmail.trimmingCharacters(in: .whitespaces).lowercased()
        guard !email.isEmpty else {
            message = "Please enter an email address"
            return
        }
        guard pet.userId == FirebaseService.shared.currentUserId else {
            message = "You can only share pets you own"
            return
        }
        isSharing = true
        message = ""
        Task {
            do {
                guard let sharedWithUserId = try await FirebaseService.shared.lookupUserIdByEmail(email) else {
                    await MainActor.run {
                        message = "User not found. They need to sign up for Pet Scheduling first using this email."
                        isSharing = false
                    }
                    return
                }
                guard sharedWithUserId != FirebaseService.shared.currentUserId else {
                    await MainActor.run {
                        message = "You cannot share a pet with yourself"
                        isSharing = false
                    }
                    return
                }
                try await FirebaseService.shared.createSharedAccess(
                    petId: pet.petId,
                    sharedWithUserId: sharedWithUserId,
                    sharedWithEmail: email,
                    permissionLevel: permissionLevel
                )
                await MainActor.run {
                    message = "Successfully shared \(pet.name) with \(email)"
                    shareEmail = ""
                    isSharing = false
                }
            } catch {
                await MainActor.run {
                    message = "Error: \(error.localizedDescription)"
                    isSharing = false
                }
            }
        }
    }
    
    private func shareToOtherApps() {
        var text = "\(pet.name) - \(Constants.PetType.displayName(for: pet.type))"
        if let breed = pet.breed, !breed.isEmpty { text += "\nBreed: \(breed)" }
        if let notes = pet.notes, !notes.isEmpty { text += "\nNotes: \(notes)" }
        if let vetName = pet.vetName, !vetName.isEmpty { text += "\nVet: \(vetName)" }
        
        let activityVC = UIActivityViewController(activityItems: [text], applicationActivities: nil)
        if let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
           let rootVC = windowScene.windows.first?.rootViewController {
            var topVC = rootVC
            while let presented = topVC.presentedViewController { topVC = presented }
            if let popover = activityVC.popoverPresentationController {
                popover.sourceView = topVC.view
                popover.sourceRect = CGRect(x: topVC.view.bounds.midX, y: topVC.view.bounds.midY, width: 0, height: 0)
                popover.permittedArrowDirections = []
            }
            topVC.present(activityVC, animated: true)
        }
    }
}
