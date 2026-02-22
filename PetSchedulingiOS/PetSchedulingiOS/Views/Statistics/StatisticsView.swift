//
//  StatisticsView.swift
//  PetSchedulingiOS
//

import SwiftUI

struct StatisticsView: View {
    @State private var pets: [Pet] = []
    @State private var allTasks: [ScheduleTask] = []
    @State private var completedTasks: [CompletedTask] = []
    @State private var isLoading = true
    
    private var totalTasks: Int { allTasks.count }
    private var totalCompleted: Int { completedTasks.count }
    private var completionRate: Double {
        guard totalTasks > 0 else { return 0 }
        return Double(totalCompleted) / Double(totalTasks) * 100
    }
    
    var body: some View {
        Group {
            if isLoading {
                ProgressView()
            } else {
                List {
                    Section("Overview") {
                        HStack {
                            StatCard(title: "Total Tasks", value: "\(totalTasks)")
                            Spacer()
                            StatCard(title: "Completion Rate", value: String(format: "%.1f%%", completionRate))
                        }
                        HStack {
                            StatCard(title: "Pets", value: "\(pets.count)")
                            Spacer()
                            StatCard(title: "Completed", value: "\(totalCompleted)")
                        }
                    }
                    
                    Section("Tasks by Category") {
                        let byCategory = Dictionary(grouping: allTasks, by: { $0.category })
                        ForEach(Array(byCategory.keys.sorted()), id: \.self) { cat in
                            HStack {
                                Text(Constants.TaskCategory.displayName(for: cat))
                                Spacer()
                                Text("\(byCategory[cat]?.count ?? 0)")
                                    .foregroundStyle(.secondary)
                            }
                        }
                    }
                }
            }
        }
        .navigationTitle("Statistics")
        .task { loadData() }
        .refreshable { await performLoadData() }
    }
    
    private func loadData() {
        Task { await performLoadData() }
    }
    
    private func performLoadData() async {
        await MainActor.run { isLoading = true }
        do {
            let fetchedPets = try await FirebaseService.shared.fetchPets()
            var tasks: [ScheduleTask] = []
            var completed: [CompletedTask] = []
            for pet in fetchedPets {
                let t = try await FirebaseService.shared.fetchTasks(forPetId: pet.petId)
                tasks.append(contentsOf: t)
                let c = try await FirebaseService.shared.fetchCompletedTasks(forPetId: pet.petId)
                completed.append(contentsOf: c)
            }
            await MainActor.run {
                pets = fetchedPets
                allTasks = tasks
                completedTasks = completed
            }
        } catch {
            // Handle
        }
        await MainActor.run { isLoading = false }
    }
}

struct StatCard: View {
    let title: String
    let value: String
    
    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(title)
                .font(.caption)
                .foregroundStyle(.secondary)
            Text(value)
                .font(.title2.bold())
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding()
        .background(Color(.systemGray6))
        .cornerRadius(8)
    }
}
