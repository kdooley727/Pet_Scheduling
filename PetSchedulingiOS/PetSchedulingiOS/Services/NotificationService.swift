//
//  NotificationService.swift
//  PetSchedulingiOS
//

import Foundation
import UserNotifications

final class NotificationService {
    static let shared = NotificationService()
    
    private init() {}
    
    func requestPermission() {
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .badge, .sound]) { granted, error in
            if let error = error {
                print("NotificationService: Permission error: \(error)")
            }
        }
    }
    
    /// Check if notifications are enabled in app settings and system
    private var shouldSchedule: Bool {
        guard UserDefaults.standard.object(forKey: "notificationsEnabled") as? Bool ?? true else { return false }
        return true
    }
    
    func scheduleNotification(for task: ScheduleTask, petName: String) {
        guard shouldSchedule else { return }
        
        let reminderTimeMs = task.startTime - Int64(task.reminderMinutesBefore) * 60 * 1000
        let reminderDate = Date(timeIntervalSince1970: Double(reminderTimeMs) / 1000)
        let now = Date()
        
        guard reminderDate > now else {
            return
        }
        
        let content = UNMutableNotificationContent()
        content.title = petName
        content.body = task.title
        content.sound = .default
        content.userInfo = ["taskId": task.taskId, "petId": task.petId]
        
        let interval = reminderDate.timeIntervalSince(now)
        let trigger = UNTimeIntervalNotificationTrigger(timeInterval: max(interval, 1), repeats: false)
        let request = UNNotificationRequest(identifier: "task-\(task.taskId)", content: content, trigger: trigger)
        
        UNUserNotificationCenter.current().add(request) { error in
            if let error = error {
                print("NotificationService: Failed to schedule: \(error)")
            }
        }
    }
    
    func cancelNotification(taskId: String) {
        UNUserNotificationCenter.current().removePendingNotificationRequests(withIdentifiers: ["task-\(taskId)"])
    }
    
    func rescheduleNotification(for task: ScheduleTask, petName: String) {
        cancelNotification(taskId: task.taskId)
        scheduleNotification(for: task, petName: petName)
    }
}
