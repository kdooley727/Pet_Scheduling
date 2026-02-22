//
//  ScheduleTask.swift
//  PetSchedulingiOS
//

import Foundation

struct ScheduleTask: Identifiable, Codable, Hashable {
    var id: String { taskId }
    let taskId: String
    let petId: String
    var title: String
    var description: String?
    var category: String
    var startTime: Int64
    var recurrencePattern: String
    var recurrenceInterval: Int
    var reminderMinutesBefore: Int
    var isActive: Bool
    let createdAt: Int64
    let createdByUserId: String
    
    init(taskId: String = UUID().uuidString, petId: String, title: String, description: String? = nil,
         category: String, startTime: Int64, recurrencePattern: String, recurrenceInterval: Int = 1,
         reminderMinutesBefore: Int = 15, isActive: Bool = true,
         createdAt: Int64 = Int64(Date().timeIntervalSince1970 * 1000),
         createdByUserId: String) {
        self.taskId = taskId
        self.petId = petId
        self.title = title
        self.description = description
        self.category = category
        self.startTime = startTime
        self.recurrencePattern = recurrencePattern
        self.recurrenceInterval = recurrenceInterval
        self.reminderMinutesBefore = reminderMinutesBefore
        self.isActive = isActive
        self.createdAt = createdAt
        self.createdByUserId = createdByUserId
    }
}
