//
//  TaskDetailView.swift
//  PetSchedulingiOS
//

import SwiftUI

struct TaskDetailView: View {
    let task: ScheduleTask
    var canEdit: Bool = true
    @State private var completedTasks: [CompletedTask] = []
    @State private var showEditTask = false
    @State private var isMarkingComplete = false
    
    private var isCompletedToday: Bool {
        let now = Date()
        let startOfDay = Int64(Calendar.current.startOfDay(for: now).timeIntervalSince1970 * 1000)
        let endOfDay = startOfDay + 86400000
        return completedTasks.contains { ct in
            let t = ct.scheduledTime ?? ct.completedAt
            return t >= startOfDay && t < endOfDay
        }
    }
    
    var body: some View {
        List {
            if canEdit {
            Section {
                Button {
                    markComplete()
                } label: {
                    HStack {
                        Image(systemName: "checkmark.circle.fill")
                        Text("Mark Complete")
                        Spacer()
                        if isMarkingComplete {
                            ProgressView()
                        }
                    }
                }
                .disabled(isCompletedToday || isMarkingComplete)
            }
            }
            
            Section("Task Details") {
                LabeledContent("Title", value: task.title)
                LabeledContent("Category", value: Constants.TaskCategory.displayName(for: task.category))
                LabeledContent("Time", value: DateTimeUtils.formatDateTime(task.startTime))
                LabeledContent("Recurrence", value: Constants.RecurrencePattern.displayName(for: task.recurrencePattern))
                LabeledContent("Reminder", value: DateTimeUtils.formatReminder(task.reminderMinutesBefore))
                if let desc = task.description, !desc.isEmpty {
                    LabeledContent("Description", value: desc)
                }
            }
            
            Section("Completion History") {
                if completedTasks.isEmpty {
                    Text("No completions yet")
                        .foregroundStyle(.secondary)
                } else {
                    ForEach(completedTasks) { ct in
                        HStack {
                            Image(systemName: "checkmark.circle.fill")
                                .foregroundStyle(.green)
                            VStack(alignment: .leading) {
                                Text(DateTimeUtils.formatDateTime(ct.completedAt))
                                    .font(.subheadline)
                                if let notes = ct.notes, !notes.isEmpty {
                                    Text(notes)
                                        .font(.caption)
                                        .foregroundStyle(.secondary)
                                }
                            }
                        }
                    }
                }
            }
        }
        .navigationTitle("Task Details")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            if canEdit {
                ToolbarItem(placement: .primaryAction) {
                    Button("Edit") {
                        showEditTask = true
                    }
                }
            }
        }
        .sheet(isPresented: $showEditTask) {
            AddEditTaskView(petId: task.petId, taskId: task.taskId)
        }
        .task {
            do {
                completedTasks = try await FirebaseService.shared.fetchCompletedTasks(forTaskId: task.taskId)
            } catch {
                // Handle
            }
        }
    }
    
    private func markComplete() {
        guard let userId = FirebaseService.shared.currentUserId else { return }
        guard !isCompletedToday else { return }
        isMarkingComplete = true
        Task {
            do {
                try await FirebaseService.shared.markTaskCompleted(
                    taskId: task.taskId,
                    completedByUserId: userId,
                    scheduledTime: Int64(Date().timeIntervalSince1970 * 1000)
                )
                let updated = try await FirebaseService.shared.fetchCompletedTasks(forTaskId: task.taskId)
                await MainActor.run {
                    completedTasks = updated
                    isMarkingComplete = false
                }
            } catch {
                await MainActor.run { isMarkingComplete = false }
            }
        }
    }
}
