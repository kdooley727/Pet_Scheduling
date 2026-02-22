//
//  CompletedTask.swift
//  PetSchedulingiOS
//

import Foundation

struct CompletedTask: Identifiable, Codable {
    var id: String { completedTaskId }
    let completedTaskId: String
    let taskId: String
    let completedAt: Int64
    let completedByUserId: String
    let notes: String?
    let scheduledTime: Int64?
}
