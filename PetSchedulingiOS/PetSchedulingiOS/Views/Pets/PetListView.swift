//
//  PetListView.swift
//  PetSchedulingiOS
//

import SwiftUI

struct PetListView: View {
    @EnvironmentObject var authState: AuthState
    @Environment(\.appTheme) private var theme
    @State private var pets: [Pet] = []
    @State private var isLoading = true
    @State private var errorMessage: String?
    @State private var showAddPet = false
    @State private var showSettings = false
    @State private var showStatistics = false
    @State private var selectedPetForTasks: Pet?
    @State private var selectedPetForShare: Pet?
    @State private var selectedPetForEdit: Pet?
    @State private var showSignOutConfirm = false
    
    var body: some View {
        NavigationStack {
            ZStack {
                if isLoading {
                    ProgressView()
                } else if let error = errorMessage {
                    VStack(spacing: 16) {
                        Image(systemName: "exclamationmark.triangle")
                            .font(.largeTitle)
                            .foregroundStyle(.orange)
                        Text("Error loading pets")
                            .font(.headline)
                        Text(error)
                            .font(.caption)
                            .foregroundStyle(.secondary)
                            .multilineTextAlignment(.center)
                            .padding()
                    }
                } else if pets.isEmpty {
                    emptyState
                } else {
                    petList
                }
            }
            .padding(.bottom, 56)
            .navigationTitle("My Pets")
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Button {
                        showAddPet = true
                    } label: {
                        Label("Add Pet", systemImage: "plus")
                    }
                    .accessibilityLabel("Add Pet")
                    .accessibilityHint("Add a new pet")
                }
                ToolbarItem(placement: .topBarTrailing) {
                    Menu {
                        Button {
                            showStatistics = true
                        } label: {
                            Label("Statistics", systemImage: "chart.bar")
                        }
                        Button {
                            showSettings = true
                        } label: {
                            Label("Settings", systemImage: "gearshape")
                        }
                    } label: {
                        Image(systemName: "ellipsis.circle")
                    }
                    .accessibilityLabel("More options")
                    .accessibilityHint("Statistics and Settings")
                }
            }
            .sheet(isPresented: $showAddPet) {
                AddEditPetView(petId: nil)
                    .onDisappear { loadPets() }
            }
            .sheet(item: $selectedPetForEdit) { pet in
                AddEditPetView(petId: pet.petId)
                    .onDisappear { loadPets() }
            }
            .navigationDestination(isPresented: $showSettings) {
                SettingsView()
            }
            .navigationDestination(isPresented: $showStatistics) {
                StatisticsView()
            }
            .navigationDestination(item: $selectedPetForTasks) { pet in
                TaskListView(pet: pet)
            }
            .navigationDestination(item: $selectedPetForShare) { pet in
                SharePetView(pet: pet)
            }
            .overlay(alignment: .bottom) {
                Button("Sign Out") {
                    showSignOutConfirm = true
                }
                .font(.caption)
                .padding(.vertical, 20)
                .padding(.horizontal, 32)
                .accessibilityLabel("Sign Out")
                .accessibilityHint("Sign out of your account")
            }
            .alert("Sign Out", isPresented: $showSignOutConfirm) {
                Button("Cancel", role: .cancel) {}
                Button("Sign Out", role: .destructive) {
                    authState.signOut()
                }
            } message: {
                Text("Are you sure you want to sign out?")
            }
            .task { await performLoadPets() }
            .refreshable { await performLoadPets(recordSync: true) }
            .onReceive(NotificationCenter.default.publisher(for: .syncNowRequested)) { _ in
                Task { await performLoadPets(recordSync: true) }
            }
        }
    }
    
    private var emptyState: some View {
        VStack(spacing: 28) {
            ZStack {
                Circle()
                    .fill(theme.gradient.opacity(0.3))
                    .frame(width: 120, height: 120)
                Image(systemName: "pawprint.circle.fill")
                    .font(.system(size: 64, design: .rounded))
                    .foregroundStyle(theme.gradient)
            }
            .accessibilityHidden(true)
            Text("No Pets Yet")
                .font(.title2.bold())
            Text("Add your first pet to get started!")
                .font(.body)
                .foregroundStyle(.secondary)
                .multilineTextAlignment(.center)
        }
        .padding(32)
        .accessibilityElement(children: .combine)
        .accessibilityLabel("No pets yet. Add your first pet to get started.")
    }
    
    private var petList: some View {
        List {
            ForEach(pets) { pet in
                PetRowView(pet: pet)
                    .contentShape(Rectangle())
                    .onTapGesture {
                        selectedPetForTasks = pet
                    }
                    .contextMenu {
                        if !pet.isSharedWithMe || (pet.sharedPermissionLevel == "edit" || pet.sharedPermissionLevel == "manage") {
                            Button {
                                selectedPetForEdit = pet
                            } label: {
                                Label("Edit", systemImage: "pencil")
                            }
                        }
                        if !pet.isSharedWithMe {
                            Button {
                                selectedPetForShare = pet
                            } label: {
                                Label("Share", systemImage: "square.and.arrow.up")
                            }
                        }
                        if !pet.isSharedWithMe {
                            Button(role: .destructive) {
                                deletePet(pet)
                            } label: {
                                Label("Delete", systemImage: "trash")
                            }
                        }
                    }
            }
        }
        .listStyle(.plain)
    }
    
    private func loadPets() {
        Task { await performLoadPets() }
    }
    
    private func performLoadPets(recordSync: Bool = false) async {
        guard FirebaseService.shared.currentUserId != nil else { return }
        await MainActor.run {
            isLoading = true
            errorMessage = nil
        }
        do {
            let fetched = try await FirebaseService.shared.fetchAllPetsIncludingShared()
            await MainActor.run {
                pets = fetched
                if recordSync {
                    SyncStatusService.shared.recordSync()
                    NotificationCenter.default.post(name: .syncCompleted, object: nil)
                }
            }
        } catch {
            await MainActor.run {
                errorMessage = error.localizedDescription
                if recordSync { NotificationCenter.default.post(name: .syncCompleted, object: nil) }
            }
        }
        await MainActor.run { isLoading = false }
    }
    
    private func deletePet(_ pet: Pet) {
        Task {
            do {
                try await FirebaseService.shared.deletePet(pet.petId)
                loadPets()
            } catch {
                errorMessage = error.localizedDescription
            }
        }
    }
}
