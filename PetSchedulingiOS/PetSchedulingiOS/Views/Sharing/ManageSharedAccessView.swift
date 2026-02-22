//
//  ManageSharedAccessView.swift
//  PetSchedulingiOS
//

import SwiftUI

struct ManageSharedAccessView: View {
    @State private var sharedWithMe: [(SharedAccess, Pet)] = []
    @State private var sharedByMe: [(SharedAccess, Pet)] = []
    @State private var isLoading = true
    @State private var selectedTab = 0
    
    var body: some View {
        VStack(spacing: 0) {
            Picker("", selection: $selectedTab) {
                Text("Shared With Me").tag(0)
                Text("I Shared").tag(1)
            }
            .pickerStyle(.segmented)
            .padding()
            
            if isLoading {
                Spacer()
                ProgressView()
            } else {
                List {
                    let items = selectedTab == 0 ? sharedWithMe : sharedByMe
                    if items.isEmpty {
                        Section {
                            VStack(spacing: 12) {
                                Image(systemName: "person.2")
                                    .font(.largeTitle)
                                    .foregroundStyle(.secondary)
                                Text(selectedTab == 0 ? "No pets shared with you" : "You haven't shared any pets")
                                    .font(.headline)
                                Text(selectedTab == 0
                                    ? "When someone shares a pet with you, it will appear here."
                                    : "Share a pet from the pet list to give others access.")
                                    .font(.caption)
                                    .foregroundStyle(.secondary)
                                    .multilineTextAlignment(.center)
                            }
                            .frame(maxWidth: .infinity)
                            .padding()
                        }
                    } else {
                        ForEach(items, id: \.0.shareId) { access, pet in
                            HStack {
                                VStack(alignment: .leading, spacing: 4) {
                                    Text(pet.name)
                                        .font(.headline)
                                    Text(permissionDisplay(access.permissionLevel))
                                        .font(.caption)
                                        .foregroundStyle(.secondary)
                                    Text(selectedTab == 0
                                        ? "Shared by \(access.ownerEmail ?? "—")"
                                        : "Shared with \(access.sharedWithEmail ?? "—")")
                                        .font(.caption2)
                                        .foregroundStyle(.secondary)
                                }
                                Spacer()
                                if selectedTab == 1 {
                                    Button("Revoke") {
                                        revokeAccess(access, petName: pet.name)
                                    }
                                    .font(.caption)
                                    .foregroundStyle(.red)
                                }
                            }
                            .padding(.vertical, 4)
                        }
                    }
                }
                .listStyle(.plain)
            }
        }
        .navigationTitle("Shared Access")
        .task { await loadSharedAccess() }
        .refreshable { await loadSharedAccess() }
    }
    
    private func permissionDisplay(_ level: String) -> String {
        switch level {
        case "view": return "View only"
        case "edit": return "Can edit"
        case "manage": return "Full access"
        default: return level
        }
    }
    
    private func loadSharedAccess() async {
        await MainActor.run { isLoading = true }
        do {
            let sharedWithMeList = try await FirebaseService.shared.getSharedPetsWithMe()
            let sharedByMeList = try await FirebaseService.shared.getSharedByMe()
            await MainActor.run {
                sharedWithMe = sharedWithMeList
                sharedByMe = sharedByMeList
            }
        } catch {
            // Handle
        }
        await MainActor.run { isLoading = false }
    }
    
    private func revokeAccess(_ access: SharedAccess, petName: String) {
        Task {
            do {
                try await FirebaseService.shared.revokeSharedAccess(shareId: access.shareId)
                await loadSharedAccess()
            } catch {
                // Handle
            }
        }
    }
}
