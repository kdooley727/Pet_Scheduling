//
//  SyncStatusService.swift
//  PetSchedulingiOS
//

import Foundation
import Combine

/// Tracks last sync time for cloud data. Firebase syncs on fetch; we record when user explicitly refreshes.
final class SyncStatusService: ObservableObject {
    static let shared = SyncStatusService()
    
    private let lastSyncKey = "lastSyncTime"
    
    @Published private(set) var lastSyncTime: Date?
    
    private init() {
        if let ts = UserDefaults.standard.object(forKey: lastSyncKey) as? TimeInterval {
            lastSyncTime = Date(timeIntervalSince1970: ts)
        } else {
            lastSyncTime = nil
        }
    }
    
    func recordSync() {
        let now = Date()
        UserDefaults.standard.set(now.timeIntervalSince1970, forKey: lastSyncKey)
        lastSyncTime = now
    }
    
    var lastSyncFormatted: String {
        guard let date = lastSyncTime else { return "Never" }
        let formatter = DateFormatter()
        formatter.dateStyle = .medium
        formatter.timeStyle = .short
        return formatter.string(from: date)
    }
}
