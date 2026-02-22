//
//  TaskListView.swift
//  PetSchedulingiOS
//

import SwiftUI

struct TaskListView: View {
    let pet: Pet
    @Environment(\.appTheme) private var theme
    @State private var tasks: [ScheduleTask] = []
    @State private var completedTaskIds: Set<String> = []
    @State private var isLoading = true
    @State private var showAddTask = false
    @State private var selectedTask: ScheduleTask?
    @State private var filter: TaskFilter = .all
    @State private var searchText = ""
    
    enum TaskFilter: String, CaseIterable {
        case all = "All"
        case today = "Today"
        case week = "This Week"
    }
    
    private var canEditTasks: Bool {
        !pet.isSharedWithMe || (pet.sharedPermissionLevel == "edit" || pet.sharedPermissionLevel == "manage")
    }
    
    private var filteredTasks: [ScheduleTask] {
        var result = tasks
        if !searchText.isEmpty {
            let q = searchText.lowercased()
            result = result.filter {
                $0.title.lowercased().contains(q) ||
                ($0.description?.lowercased().contains(q) ?? false) ||
                $0.category.lowercased().contains(q)
            }
        }
        let now = Date()
        switch filter {
        case .today:
            let start = Calendar.current.startOfDay(for: now).timeIntervalSince1970 * 1000
            let end = start + 86400000
            result = result.filter { $0.startTime >= Int64(start) && $0.startTime < Int64(end) }
        case .week:
            let calendar = Calendar.current
            guard let startOfWeek = calendar.date(from: calendar.dateComponents([.yearForWeekOfYear, .weekOfYear], from: now)) else { break }
            let endOfWeek = calendar.date(byAdding: .day, value: 7, to: startOfWeek)!
            let start = Int64(startOfWeek.timeIntervalSince1970 * 1000)
            let end = Int64(endOfWeek.timeIntervalSince1970 * 1000)
            result = result.filter { $0.startTime >= start && $0.startTime < end }
        case .all:
            break
        }
        return result.sorted { $0.startTime < $1.startTime }
    }
    
    var body: some View {
        VStack(spacing: 0) {
            Picker("Filter", selection: $filter) {
                ForEach(TaskFilter.allCases, id: \.self) { f in
                    Text(f.rawValue).tag(f)
                }
            }
            .pickerStyle(.segmented)
            .padding()
            .accessibilityLabel("Task filter")
            .accessibilityHint("Filter tasks by all, today, or this week")
            
            if isLoading {
                Spacer()
                ProgressView()
            } else if filteredTasks.isEmpty {
                Spacer()
                VStack(spacing: 20) {
                    ZStack {
                        RoundedRectangle(cornerRadius: 20)
                            .fill(theme.gradient.opacity(0.2))
                            .frame(width: 80, height: 80)
                        Image(systemName: "checklist")
                            .font(.system(size: 36, design: .rounded))
                            .foregroundStyle(theme.gradient)
                    }
                    Text("No Tasks")
                        .font(.title2.bold())
                    Text("Add a task to get started")
                        .foregroundStyle(.secondary)
                }
            } else {
                List {
                    ForEach(filteredTasks) { task in
                        TaskRowView(
                            task: task,
                            isCompleted: completedTaskIds.contains(task.taskId),
                            onMarkComplete: canEditTasks ? { markComplete(task) } : nil
                        )
                            .contentShape(Rectangle())
                            .onTapGesture {
                                selectedTask = task
                            }
                            .swipeActions(edge: .leading) {
                                if canEditTasks {
                                    Button {
                                        markComplete(task)
                                    } label: {
                                        Label("Complete", systemImage: "checkmark.circle")
                                    }
                                    .disabled(completedTaskIds.contains(task.taskId))
                                }
                            }
                            .swipeActions(edge: .trailing, allowsFullSwipe: canEditTasks) {
                                if canEditTasks {
                                    Button(role: .destructive) {
                                        deleteTask(task)
                                    } label: {
                                        Label("Delete", systemImage: "trash")
                                    }
                                }
                            }
                    }
                }
                .listStyle(.plain)
            }
        }
        .navigationTitle(pet.name)
        .navigationBarTitleDisplayMode(.inline)
        .searchable(text: $searchText, prompt: "Search tasks")
        .toolbar {
            if canEditTasks {
                ToolbarItem(placement: .primaryAction) {
                    Button {
                        showAddTask = true
                    } label: {
                        Image(systemName: "plus.circle.fill")
                    }
                    .accessibilityLabel("Add task")
                    .accessibilityHint("Add a new task for \(pet.name)")
                }
            }
        }
        .sheet(isPresented: $showAddTask) {
            AddEditTaskView(petId: pet.petId, petName: pet.name, taskId: nil)
                .onDisappear { loadTasks() }
        }
        .navigationDestination(item: $selectedTask) { task in
            TaskDetailView(task: task, canEdit: canEditTasks)
                .onDisappear { loadTasks() }
        }
        .task(id: pet.petId) { await performLoadTasks() }
        .refreshable { await performLoadTasks() }
        .onAppear { loadTasks() }
        .onReceive(NotificationCenter.default.publisher(for: .syncNowRequested)) { _ in
            Task { await performLoadTasks() }
        }
    }
    
    private func loadTasks() {
        Task { await performLoadTasks() }
    }
    
    private func performLoadTasks() async {
        await MainActor.run { isLoading = true }
        do {
            let fetchedTasks = try await FirebaseService.shared.fetchTasks(forPetId: pet.petId)
            let completed = try await FirebaseService.shared.fetchCompletedTasks(forPetId: pet.petId)
            let now = Date()
            let startOfDay = Int64(Calendar.current.startOfDay(for: now).timeIntervalSince1970 * 1000)
            let endOfDay = startOfDay + 86400000
            let completedIds = Set(completed
                .filter { ($0.scheduledTime ?? $0.completedAt) >= startOfDay && ($0.scheduledTime ?? $0.completedAt) < endOfDay }
                .map { $0.taskId })
            await MainActor.run {
                tasks = fetchedTasks
                completedTaskIds = completedIds
            }
            for task in fetchedTasks where task.isActive {
                NotificationService.shared.scheduleNotification(for: task, petName: pet.name)
            }
        } catch {
            print("TaskListView: Failed to load tasks for pet \(pet.petId): \(error)")
            await MainActor.run { tasks = [] }
        }
        await MainActor.run { isLoading = false }
    }
    
    private func markComplete(_ task: ScheduleTask) {
        guard let userId = FirebaseService.shared.currentUserId else { return }
        Task {
            try? await FirebaseService.shared.markTaskCompleted(
                taskId: task.taskId,
                completedByUserId: userId,
                scheduledTime: Int64(Date().timeIntervalSince1970 * 1000)
            )
            loadTasks()
        }
    }
    
    private func deleteTask(_ task: ScheduleTask) {
        Task {
            do {
                NotificationService.shared.cancelNotification(taskId: task.taskId)
                try await FirebaseService.shared.deleteTask(task.taskId)
                await MainActor.run {
                    tasks.removeAll { $0.taskId == task.taskId }
                }
            } catch {
                print("TaskListView: Failed to delete task: \(error)")
                loadTasks()
            }
        }
    }
}
