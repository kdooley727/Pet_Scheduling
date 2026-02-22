//
//  TaskRowView.swift
//  PetSchedulingiOS
//

import SwiftUI

struct TaskRowView: View {
    let task: ScheduleTask
    let isCompleted: Bool
    var onMarkComplete: (() -> Void)? = nil
    
    var body: some View {
        HStack(spacing: 12) {
            Button {
                onMarkComplete?()
            } label: {
                Image(systemName: isCompleted ? "checkmark.circle.fill" : "circle")
                    .foregroundStyle(isCompleted ? .green : .secondary)
                    .font(.title2)
            }
            .buttonStyle(.plain)
            .disabled(isCompleted)
            
            VStack(alignment: .leading, spacing: 4) {
                Text(task.title)
                    .font(.headline)
                    .strikethrough(isCompleted)
                Text(Constants.TaskCategory.displayName(for: task.category))
                    .font(.caption)
                    .foregroundStyle(.secondary)
                Text(DateTimeUtils.formatDateTime(task.startTime))
                    .font(.caption2)
                    .foregroundStyle(.secondary)
            }
            
            Spacer()
        }
        .padding(.vertical, 4)
    }
}
