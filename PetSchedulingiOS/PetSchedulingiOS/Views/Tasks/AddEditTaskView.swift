//
//  AddEditTaskView.swift
//  PetSchedulingiOS
//

import SwiftUI

struct AddEditTaskView: View {
    let petId: String
    var petName: String = "Your pet"
    let taskId: String?
    @Environment(\.dismiss) private var dismiss
    
    @State private var title = ""
    @State private var description = ""
    @State private var category = "feeding"
    @State private var startDate = Date()
    @State private var recurrencePattern = "daily"
    @State private var reminderMinutes = 15
    @State private var isLoading = false
    @State private var errorMessage: String?
    
    private var isEditMode: Bool { taskId != nil }
    
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
                
                Section("Task Info") {
                    TextField("Title", text: $title)
                    TextField("Description", text: $description, axis: .vertical)
                        .lineLimit(3...6)
                    
                    Picker("Category", selection: $category) {
                        ForEach(Constants.TaskCategory.allCategories, id: \.self) { c in
                            Text(Constants.TaskCategory.displayName(for: c)).tag(c)
                        }
                    }
                    
                    DatePicker("Date & Time", selection: $startDate)
                    
                    Picker("Recurrence", selection: $recurrencePattern) {
                        ForEach(Constants.RecurrencePattern.allPatterns, id: \.self) { p in
                            Text(Constants.RecurrencePattern.displayName(for: p)).tag(p)
                        }
                    }
                    
                    Picker("Reminder", selection: $reminderMinutes) {
                        ForEach(Constants.reminderTimes, id: \.self) { m in
                            Text(DateTimeUtils.formatReminder(m)).tag(m)
                        }
                    }
                    .accessibilityLabel("Reminder")
                    .accessibilityHint("When to remind before the task")
                }
            }
            .navigationTitle(isEditMode ? "Edit Task" : "Add Task")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Save") { saveTask() }
                        .disabled(isLoading || title.trimmingCharacters(in: .whitespaces).isEmpty)
                }
            }
            .task {
                if let id = taskId {
                    await loadTask(id: id)
                } else {
                    reminderMinutes = UserDefaults.standard.object(forKey: "defaultReminderMinutes") as? Int ?? 15
                }
            }
        }
    }
    
    @State private var resolvedPetName = ""
    
    private var displayPetName: String {
        resolvedPetName.isEmpty ? petName : resolvedPetName
    }
    
    private func loadTask(id: String) async {
        guard let task = try? await FirebaseService.shared.fetchTask(byId: id) else { return }
        title = task.title
        description = task.description ?? ""
        category = task.category
        startDate = Date(timeIntervalSince1970: Double(task.startTime) / 1000)
        recurrencePattern = task.recurrencePattern
        reminderMinutes = task.reminderMinutesBefore
        if let pets = try? await FirebaseService.shared.fetchPets(),
           let pet = pets.first(where: { $0.petId == task.petId }) {
            await MainActor.run { resolvedPetName = pet.name }
        }
    }
    
    private func saveTask() {
        guard let userId = FirebaseService.shared.currentUserId else {
            errorMessage = "Not signed in. Please sign in and try again."
            return
        }
        guard !title.trimmingCharacters(in: .whitespaces).isEmpty else { return }
        errorMessage = nil
        
        isLoading = true
        let startTime = Int64(startDate.timeIntervalSince1970 * 1000)
        let now = Int64(Date().timeIntervalSince1970 * 1000)
        
        let task = ScheduleTask(
            taskId: taskId ?? UUID().uuidString,
            petId: petId,
            title: title.trimmingCharacters(in: .whitespaces),
            description: description.isEmpty ? nil : description,
            category: category,
            startTime: startTime,
            recurrencePattern: recurrencePattern,
            recurrenceInterval: 1,
            reminderMinutesBefore: reminderMinutes,
            isActive: true,
            createdAt: now,
            createdByUserId: userId
        )
        
        Task {
            do {
                try await FirebaseService.shared.saveTask(task)
                if taskId != nil {
                    NotificationService.shared.rescheduleNotification(for: task, petName: displayPetName)
                } else {
                    NotificationService.shared.scheduleNotification(for: task, petName: displayPetName)
                }
                await MainActor.run { dismiss() }
            } catch {
                await MainActor.run {
                    errorMessage = error.localizedDescription
                }
            }
            await MainActor.run { isLoading = false }
        }
    }
}
