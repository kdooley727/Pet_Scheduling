//
//  DateTimeUtils.swift
//  PetSchedulingiOS
//

import Foundation

enum DateTimeUtils {
    static func formatDate(_ timestamp: Int64) -> String {
        let date = Date(timeIntervalSince1970: Double(timestamp) / 1000)
        let formatter = DateFormatter()
        formatter.dateStyle = .medium
        return formatter.string(from: date)
    }
    
    static func formatTime(_ timestamp: Int64) -> String {
        let date = Date(timeIntervalSince1970: Double(timestamp) / 1000)
        let formatter = DateFormatter()
        formatter.timeStyle = .short
        return formatter.string(from: date)
    }
    
    static func formatDateTime(_ timestamp: Int64) -> String {
        let date = Date(timeIntervalSince1970: Double(timestamp) / 1000)
        let formatter = DateFormatter()
        formatter.dateStyle = .medium
        formatter.timeStyle = .short
        return formatter.string(from: date)
    }
    
    static func formatReminder(_ minutes: Int) -> String {
        if minutes < 60 {
            return "\(minutes) min before"
        } else if minutes == 60 {
            return "1 hour before"
        } else {
            return "\(minutes / 60) hours before"
        }
    }
}
