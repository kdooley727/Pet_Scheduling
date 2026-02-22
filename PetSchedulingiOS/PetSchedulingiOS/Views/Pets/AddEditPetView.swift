//
//  AddEditPetView.swift
//  PetSchedulingiOS
//

import SwiftUI
import PhotosUI

struct AddEditPetView: View {
    let petId: String?
    @Environment(\.dismiss) private var dismiss
    
    @State private var name = ""
    @State private var petType = "dog"
    @State private var breed = ""
    @State private var birthDate: Date?
    @State private var notes = ""
    @State private var vetName = ""
    @State private var vetPhone = ""
    @State private var vetEmail = ""
    @State private var vetAddress = ""
    @State private var emergencyContactName = ""
    @State private var emergencyContactPhone = ""
    @State private var emergencyContactEmail = ""
    @State private var emergencyContactRelationship = ""
    @State private var isLoading = false
    @State private var errorMessage: String?
    @State private var showDeleteConfirm = false
    @State private var selectedPhotoItem: PhotosPickerItem?
    @State private var selectedPhotoImage: UIImage?
    @State private var existingPhotoUrl: String?
    
    private var isEditMode: Bool { petId != nil }
    
    var body: some View {
        NavigationStack {
            Form {
                if let error = errorMessage {
                    Section {
                        Text(error)
                            .foregroundStyle(.red)
                            .font(.caption)
                    }
                }
                
                Section("Photo") {
                    HStack(spacing: 16) {
                        if let image = selectedPhotoImage {
                            Image(uiImage: image)
                                .resizable()
                                .scaledToFill()
                                .frame(width: 100, height: 100)
                                .clipShape(Circle())
                        } else if let urlString = existingPhotoUrl, let url = URL(string: urlString) {
                            AsyncImage(url: url) { image in
                                image.resizable().scaledToFill()
                            } placeholder: {
                                Image(systemName: "pawprint.circle.fill")
                                    .font(.title)
                                    .foregroundStyle(.secondary)
                            }
                            .frame(width: 100, height: 100)
                            .clipShape(Circle())
                        } else {
                            Image(systemName: "pawprint.circle.fill")
                                .font(.title)
                                .foregroundStyle(.secondary)
                                .frame(width: 100, height: 100)
                        }
                        
                        PhotosPicker(selection: $selectedPhotoItem, matching: .images) {
                            Label("Select Photo", systemImage: "photo.on.rectangle.angled")
                        }
                        .onChange(of: selectedPhotoItem) { _, newItem in
                            Task {
                                if let data = try? await newItem?.loadTransferable(type: Data.self),
                                   let uiImage = UIImage(data: data) {
                                    selectedPhotoImage = uiImage
                                }
                            }
                        }
                        
                        if selectedPhotoImage != nil || existingPhotoUrl != nil {
                            Button(role: .destructive) {
                                selectedPhotoItem = nil
                                selectedPhotoImage = nil
                                existingPhotoUrl = nil
                            } label: {
                                Label("Remove", systemImage: "trash")
                            }
                        }
                    }
                    .padding(.vertical, 8)
                }
                
                Section("Basic Info") {
                    TextField("Pet Name", text: $name)
                        .textContentType(.name)
                    
                    Picker("Type", selection: $petType) {
                        ForEach(Constants.PetType.allTypes, id: \.self) { type in
                            Text(Constants.PetType.displayName(for: type)).tag(type)
                        }
                    }
                    
                    TextField("Breed", text: $breed)
                    
                    Toggle("Has birth date", isOn: Binding(
                        get: { birthDate != nil },
                        set: { if $0 { birthDate = Date() } else { birthDate = nil } }
                    ))
                    if birthDate != nil {
                        DatePicker("Date", selection: Binding(
                            get: { birthDate ?? Date() },
                            set: { birthDate = $0 }
                        ), in: ...Date(), displayedComponents: .date)
                    }
                    
                    TextField("Notes", text: $notes, axis: .vertical)
                        .lineLimit(3...6)
                }
                
                Section("Vet Info") {
                    TextField("Vet Name", text: $vetName)
                    TextField("Vet Phone", text: $vetPhone)
                        .keyboardType(.phonePad)
                    TextField("Vet Email", text: $vetEmail)
                        .keyboardType(.emailAddress)
                    TextField("Vet Address", text: $vetAddress)
                }
                
                Section("Emergency Contact") {
                    TextField("Name", text: $emergencyContactName)
                    TextField("Phone", text: $emergencyContactPhone)
                        .keyboardType(.phonePad)
                    TextField("Email", text: $emergencyContactEmail)
                        .keyboardType(.emailAddress)
                    TextField("Relationship", text: $emergencyContactRelationship)
                }
                
                if isEditMode {
                    Section {
                        Button(role: .destructive) {
                            showDeleteConfirm = true
                        } label: {
                            Text("Delete Pet")
                        }
                    }
                }
            }
            .navigationTitle(isEditMode ? "Edit Pet" : "Add Pet")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                addEditToolbarContent
            }
            .alert("Delete Pet", isPresented: $showDeleteConfirm) {
                Button("Cancel", role: .cancel) {}
                Button("Delete", role: .destructive) {
                    deletePet()
                }
            } message: {
                Text("Are you sure you want to delete \(name)? This will also delete all associated tasks.")
            }
            .task {
                if let id = petId {
                    await loadPet(id: id)
                }
            }
        }
    }
    
    @ToolbarContentBuilder
    private var addEditToolbarContent: some ToolbarContent {
        ToolbarItem(placement: .cancellationAction) {
            Button("Cancel") { dismiss() }
        }
        ToolbarItem(placement: .confirmationAction) {
            Button("Save") { savePet() }
                .disabled(isLoading || name.trimmingCharacters(in: .whitespaces).isEmpty)
        }
    }
    
    private func loadPet(id: String) async {
        let allPets = (try? await FirebaseService.shared.fetchPets()) ?? []
        let pet = allPets.first { $0.petId == id }
        guard let p = pet else { return }
        
        name = p.name
        petType = p.type
        breed = p.breed ?? ""
        birthDate = p.birthDate.map { Date(timeIntervalSince1970: Double($0) / 1000) }
        existingPhotoUrl = p.photoUrl
        notes = p.notes ?? ""
        vetName = p.vetName ?? ""
        vetPhone = p.vetPhone ?? ""
        vetEmail = p.vetEmail ?? ""
        vetAddress = p.vetAddress ?? ""
        emergencyContactName = p.emergencyContactName ?? ""
        emergencyContactPhone = p.emergencyContactPhone ?? ""
        emergencyContactEmail = p.emergencyContactEmail ?? ""
        emergencyContactRelationship = p.emergencyContactRelationship ?? ""
    }
    
    private func savePet() {
        guard let userId = FirebaseService.shared.currentUserId else {
            errorMessage = "Not signed in. Please sign in and try again."
            return
        }
        guard !name.trimmingCharacters(in: .whitespaces).isEmpty else { return }
        errorMessage = nil
        
        isLoading = true
        let now = Int64(Date().timeIntervalSince1970 * 1000)
        let finalPetId = petId ?? UUID().uuidString
        
        Task {
            var photoUrl: String? = existingPhotoUrl
            
            if let image = selectedPhotoImage {
                do {
                    photoUrl = try await FirebaseService.shared.uploadPetPhoto(image: image, petId: finalPetId)
                } catch {
                    await MainActor.run {
                        errorMessage = "Photo upload failed: \(error.localizedDescription)"
                    }
                    isLoading = false
                    return
                }
            }
            
            let pet = Pet(
                petId: finalPetId,
                userId: userId,
                name: name.trimmingCharacters(in: .whitespaces),
                type: petType,
                breed: breed.isEmpty ? nil : breed,
                birthDate: birthDate.map { Int64($0.timeIntervalSince1970 * 1000) },
                photoUrl: photoUrl,
                notes: notes.isEmpty ? nil : notes,
                vetName: vetName.isEmpty ? nil : vetName,
                vetPhone: vetPhone.isEmpty ? nil : vetPhone,
                vetEmail: vetEmail.isEmpty ? nil : vetEmail,
                vetAddress: vetAddress.isEmpty ? nil : vetAddress,
                emergencyContactName: emergencyContactName.isEmpty ? nil : emergencyContactName,
                emergencyContactPhone: emergencyContactPhone.isEmpty ? nil : emergencyContactPhone,
                emergencyContactEmail: emergencyContactEmail.isEmpty ? nil : emergencyContactEmail,
                emergencyContactRelationship: emergencyContactRelationship.isEmpty ? nil : emergencyContactRelationship,
                createdAt: now,
                updatedAt: now
            )
            
            do {
                try await FirebaseService.shared.savePet(pet)
                await MainActor.run { dismiss() }
            } catch {
                await MainActor.run {
                    errorMessage = error.localizedDescription
                }
            }
            await MainActor.run { isLoading = false }
        }
    }
    
    private func deletePet() {
        guard let id = petId else { return }
        Task {
            do {
                try await FirebaseService.shared.deletePet(id)
                dismiss()
            } catch {
                errorMessage = error.localizedDescription
            }
        }
    }
}
