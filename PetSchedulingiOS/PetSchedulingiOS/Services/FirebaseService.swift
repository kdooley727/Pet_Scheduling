//
//  FirebaseService.swift
//  PetSchedulingiOS
//

import Foundation
import UIKit
import FirebaseAuth
import FirebaseFirestore
import FirebaseStorage

final class FirebaseService {
    static let shared = FirebaseService()
    private let db = Firestore.firestore()
    
    private let petsCollection = "pets"
    private let tasksCollection = "tasks"
    private let completedTasksCollection = "completed_tasks"
    private let usersCollection = "users"
    private let sharedAccessCollection = "shared_access"
    
    private init() {}
    
    var currentUserId: String? { Auth.auth().currentUser?.uid }
    
    // MARK: - Users (for email lookup when sharing)
    
    func saveUserProfile(email: String, displayName: String?) async throws {
        guard let userId = currentUserId else { throw FirebaseError.notAuthenticated }
        let data: [String: Any] = [
            "email": email.lowercased(),
            "displayName": displayName ?? NSNull(),
            "updatedAt": Int64(Date().timeIntervalSince1970 * 1000)
        ]
        try await db.collection(usersCollection).document(userId).setData(data, merge: true)
    }
    
    func lookupUserIdByEmail(_ email: String) async throws -> String? {
        let snapshot = try await db.collection(usersCollection)
            .whereField("email", isEqualTo: email.lowercased().trimmingCharacters(in: .whitespaces))
            .limit(to: 1)
            .getDocuments()
        return snapshot.documents.first?.documentID
    }
    
    // MARK: - Shared Access
    
    func createSharedAccess(petId: String, sharedWithUserId: String, sharedWithEmail: String, permissionLevel: String) async throws {
        guard let ownerUserId = currentUserId else { throw FirebaseError.notAuthenticated }
        let ownerEmail = Auth.auth().currentUser?.email ?? ""
        
        let shareId = UUID().uuidString
        let now = Int64(Date().timeIntervalSince1970 * 1000)
        let data: [String: Any] = [
            "shareId": shareId,
            "petId": petId,
            "ownerUserId": ownerUserId,
            "ownerEmail": ownerEmail,
            "sharedWithUserId": sharedWithUserId,
            "sharedWithEmail": sharedWithEmail,
            "permissionLevel": permissionLevel,
            "createdAt": now,
            "isActive": true
        ]
        try await db.collection(sharedAccessCollection).document(shareId).setData(data)
    }
    
    func getSharedAccessForUser() async throws -> [SharedAccess] {
        guard let userId = currentUserId else { throw FirebaseError.notAuthenticated }
        
        let sharedWithMe = try await db.collection(sharedAccessCollection)
            .whereField("sharedWithUserId", isEqualTo: userId)
            .whereField("isActive", isEqualTo: true)
            .getDocuments()
        
        let sharedByMe = try await db.collection(sharedAccessCollection)
            .whereField("ownerUserId", isEqualTo: userId)
            .whereField("isActive", isEqualTo: true)
            .getDocuments()
        
        var seen = Set<String>()
        let all = (sharedWithMe.documents + sharedByMe.documents).filter { seen.insert($0.documentID).inserted }
        return all.compactMap { try? parseSharedAccess(from: $0) }
    }
    
    func getSharedPetsWithMe() async throws -> [(SharedAccess, Pet)] {
        guard let userId = currentUserId else { throw FirebaseError.notAuthenticated }
        
        let snapshot = try await db.collection(sharedAccessCollection)
            .whereField("sharedWithUserId", isEqualTo: userId)
            .whereField("isActive", isEqualTo: true)
            .getDocuments()
        
        var result: [(SharedAccess, Pet)] = []
        for doc in snapshot.documents {
            guard let shared = try? parseSharedAccess(from: doc) else { continue }
            let petDoc = try await db.collection(petsCollection).document(shared.petId).getDocument()
            guard petDoc.exists, let pet = try? parsePet(from: petDoc) else { continue }
            var petWithShared = pet
            petWithShared.isSharedWithMe = true
            petWithShared.sharedByEmail = shared.ownerEmail
            petWithShared.sharedPermissionLevel = shared.permissionLevel
            result.append((shared, petWithShared))
        }
        return result
    }
    
    func getSharedByMe() async throws -> [(SharedAccess, Pet)] {
        guard let userId = currentUserId else { throw FirebaseError.notAuthenticated }
        let snapshot = try await db.collection(sharedAccessCollection)
            .whereField("ownerUserId", isEqualTo: userId)
            .whereField("isActive", isEqualTo: true)
            .getDocuments()
        
        var result: [(SharedAccess, Pet)] = []
        for doc in snapshot.documents {
            guard let shared = try? parseSharedAccess(from: doc) else { continue }
            let petDoc = try await db.collection(petsCollection).document(shared.petId).getDocument()
            guard petDoc.exists, let pet = try? parsePet(from: petDoc) else { continue }
            result.append((shared, pet))
        }
        return result
    }
    
    func revokeSharedAccess(shareId: String) async throws {
        guard let userId = currentUserId else { throw FirebaseError.notAuthenticated }
        let doc = try await db.collection(sharedAccessCollection).document(shareId).getDocument()
        guard doc.exists, (doc.data()?["ownerUserId"] as? String) == userId else {
            throw NSError(domain: "FirebaseService", code: -1, userInfo: [NSLocalizedDescriptionKey: "Not authorized to revoke"])
        }
        try await db.collection(sharedAccessCollection).document(shareId).updateData(["isActive": false])
    }
    
    // MARK: - Pets
    
    func fetchPets() async throws -> [Pet] {
        guard let userId = currentUserId else { throw FirebaseError.notAuthenticated }
        
        let snapshot = try await db.collection(petsCollection)
            .whereField("userId", isEqualTo: userId)
            .getDocuments()
        
        return snapshot.documents.compactMap { doc in
            try? parsePet(from: doc)
        }
    }
    
    func fetchAllPetsIncludingShared() async throws -> [Pet] {
        var owned = try await fetchPets()
        let shared = try await getSharedPetsWithMe()
        
        let ownedIds = Set(owned.map { $0.petId })
        for (_, pet) in shared {
            guard !ownedIds.contains(pet.petId) else { continue }
            owned.append(pet)
        }
        return owned
    }
    
    func savePet(_ pet: Pet) async throws {
        let data: [String: Any?] = [
            "petId": pet.petId,
            "userId": pet.userId,
            "name": pet.name,
            "type": pet.type,
            "breed": pet.breed,
            "birthDate": pet.birthDate,
            "photoUrl": pet.photoUrl,
            "notes": pet.notes,
            "vetName": pet.vetName,
            "vetPhone": pet.vetPhone,
            "vetEmail": pet.vetEmail,
            "vetAddress": pet.vetAddress,
            "emergencyContactName": pet.emergencyContactName,
            "emergencyContactPhone": pet.emergencyContactPhone,
            "emergencyContactEmail": pet.emergencyContactEmail,
            "emergencyContactRelationship": pet.emergencyContactRelationship,
            "createdAt": pet.createdAt,
            "updatedAt": pet.updatedAt
        ]
        
        try await db.collection(petsCollection).document(pet.petId).setData(data.compactMapValues { $0 } as [String: Any], merge: true)
    }
    
    func deletePet(_ petId: String) async throws {
        try await db.collection(petsCollection).document(petId).delete()
    }
    
    func uploadPetPhoto(image: UIImage, petId: String) async throws -> String {
        guard let userId = currentUserId else { throw FirebaseError.notAuthenticated }
        guard let imageData = image.jpegData(compressionQuality: 0.7) else {
            throw NSError(domain: "FirebaseService", code: -1, userInfo: [NSLocalizedDescriptionKey: "Could not convert image to JPEG"])
        }
        
        let filename = "pet_\(petId)_\(UUID().uuidString).jpg"
        let storagePath = "pet_photos/\(userId)/\(filename)"
        let storageRef = Storage.storage().reference().child(storagePath)
        
        _ = try await storageRef.putDataAsync(imageData)
        let downloadURL = try await storageRef.downloadURL()
        return downloadURL.absoluteString
    }
    
    // MARK: - Tasks
    
    func fetchTasks(forPetId petId: String) async throws -> [ScheduleTask] {
        guard currentUserId != nil else { throw FirebaseError.notAuthenticated }
        
        // Query by petId so tasks show regardless of auth provider (email vs Google can have different UIDs)
        let snapshot = try await db.collection(tasksCollection)
            .whereField("petId", isEqualTo: petId)
            .getDocuments()
        
        return snapshot.documents.compactMap { doc in
            try? parseTask(from: doc)
        }.filter { $0.isActive }
    }
    
    func fetchTask(byId taskId: String) async throws -> ScheduleTask? {
        let doc = try await db.collection(tasksCollection).document(taskId).getDocument()
        guard doc.exists else { return nil }
        return try? parseTask(from: doc)
    }
    
    func saveTask(_ task: ScheduleTask) async throws {
        let data: [String: Any] = [
            "taskId": task.taskId,
            "petId": task.petId,
            "title": task.title,
            "description": task.description ?? NSNull(),
            "category": task.category,
            "startTime": task.startTime,
            "recurrencePattern": task.recurrencePattern,
            "recurrenceInterval": task.recurrenceInterval,
            "reminderMinutesBefore": task.reminderMinutesBefore,
            "isActive": task.isActive,
            "createdAt": task.createdAt,
            "createdByUserId": task.createdByUserId
        ]
        
        try await db.collection(tasksCollection).document(task.taskId).setData(data, merge: true)
    }
    
    func deleteTask(_ taskId: String) async throws {
        try await db.collection(tasksCollection).document(taskId).delete()
    }
    
    // MARK: - Completed Tasks
    
    func fetchCompletedTasks(forPetId petId: String) async throws -> [CompletedTask] {
        guard let userId = currentUserId else { throw FirebaseError.notAuthenticated }
        
        let tasksSnapshot = try await db.collection(tasksCollection)
            .whereField("petId", isEqualTo: petId)
            .getDocuments()
        
        let taskIds = tasksSnapshot.documents.map { $0.documentID }
        guard !taskIds.isEmpty else { return [] }
        
        let completedSnapshot = try await db.collection(completedTasksCollection)
            .whereField("completedByUserId", isEqualTo: userId)
            .getDocuments()
        
        return completedSnapshot.documents.compactMap { doc in
            let taskId = doc.data()["taskId"] as? String
            guard let tid = taskId, taskIds.contains(tid) else { return nil }
            return try? parseCompletedTask(from: doc)
        }
    }
    
    func fetchCompletedTasks(forTaskId taskId: String) async throws -> [CompletedTask] {
        let snapshot = try await db.collection(completedTasksCollection)
            .whereField("taskId", isEqualTo: taskId)
            .order(by: "completedAt", descending: true)
            .getDocuments()
        
        return snapshot.documents.compactMap { try? parseCompletedTask(from: $0) }
    }
    
    func markTaskCompleted(taskId: String, completedByUserId: String, notes: String? = nil, scheduledTime: Int64? = nil) async throws {
        let completedTaskId = UUID().uuidString
        let now = Int64(Date().timeIntervalSince1970 * 1000)
        
        let data: [String: Any] = [
            "completedTaskId": completedTaskId,
            "taskId": taskId,
            "completedAt": now,
            "completedByUserId": completedByUserId,
            "notes": notes ?? NSNull(),
            "scheduledTime": scheduledTime ?? now
        ]
        
        try await db.collection(completedTasksCollection).document(completedTaskId).setData(data)
    }
    
    // MARK: - Parsing
    
    private func parsePet(from doc: DocumentSnapshot) throws -> Pet {
        let data = doc.data() ?? [:]
        return Pet(
            petId: data["petId"] as? String ?? doc.documentID,
            userId: data["userId"] as? String ?? "",
            name: data["name"] as? String ?? "",
            type: data["type"] as? String ?? "",
            breed: data["breed"] as? String,
            birthDate: (data["birthDate"] as? NSNumber)?.int64Value,
            photoUrl: data["photoUrl"] as? String,
            notes: data["notes"] as? String,
            vetName: data["vetName"] as? String,
            vetPhone: data["vetPhone"] as? String,
            vetEmail: data["vetEmail"] as? String,
            vetAddress: data["vetAddress"] as? String,
            emergencyContactName: data["emergencyContactName"] as? String,
            emergencyContactPhone: data["emergencyContactPhone"] as? String,
            emergencyContactEmail: data["emergencyContactEmail"] as? String,
            emergencyContactRelationship: data["emergencyContactRelationship"] as? String,
            createdAt: (data["createdAt"] as? NSNumber)?.int64Value ?? Int64(Date().timeIntervalSince1970 * 1000),
            updatedAt: (data["updatedAt"] as? NSNumber)?.int64Value ?? Int64(Date().timeIntervalSince1970 * 1000)
        )
    }
    
    private func parseTask(from doc: DocumentSnapshot) throws -> ScheduleTask {
        let data = doc.data() ?? [:]
        let startTime = int64FromFirestore(data["startTime"]) ?? Int64(Date().timeIntervalSince1970 * 1000)
        let createdAt = int64FromFirestore(data["createdAt"]) ?? Int64(Date().timeIntervalSince1970 * 1000)
        return ScheduleTask(
            taskId: data["taskId"] as? String ?? doc.documentID,
            petId: data["petId"] as? String ?? "",
            title: data["title"] as? String ?? "",
            description: data["description"] as? String,
            category: data["category"] as? String ?? "",
            startTime: startTime,
            recurrencePattern: data["recurrencePattern"] as? String ?? "daily",
            recurrenceInterval: intFromFirestore(data["recurrenceInterval"]) ?? 1,
            reminderMinutesBefore: intFromFirestore(data["reminderMinutesBefore"]) ?? 15,
            isActive: data["isActive"] as? Bool ?? true,
            createdAt: createdAt,
            createdByUserId: data["createdByUserId"] as? String ?? ""
        )
    }
    
    private func int64FromFirestore(_ value: Any?) -> Int64? {
        if let n = value as? NSNumber { return n.int64Value }
        if let i = value as? Int { return Int64(i) }
        return nil
    }
    
    private func intFromFirestore(_ value: Any?) -> Int? {
        if let n = value as? NSNumber { return n.intValue }
        if let i = value as? Int { return i }
        return nil
    }
    
    private func parseSharedAccess(from doc: DocumentSnapshot) throws -> SharedAccess {
        let data = doc.data() ?? [:]
        return SharedAccess(
            shareId: data["shareId"] as? String ?? doc.documentID,
            petId: data["petId"] as? String ?? "",
            ownerUserId: data["ownerUserId"] as? String ?? "",
            ownerEmail: data["ownerEmail"] as? String,
            sharedWithUserId: data["sharedWithUserId"] as? String ?? "",
            sharedWithEmail: data["sharedWithEmail"] as? String,
            permissionLevel: data["permissionLevel"] as? String ?? "view",
            createdAt: (data["createdAt"] as? NSNumber)?.int64Value ?? 0,
            isActive: data["isActive"] as? Bool ?? true
        )
    }
    
    private func parseCompletedTask(from doc: DocumentSnapshot) throws -> CompletedTask {
        let data = doc.data() ?? [:]
        return CompletedTask(
            completedTaskId: data["completedTaskId"] as? String ?? doc.documentID,
            taskId: data["taskId"] as? String ?? "",
            completedAt: (data["completedAt"] as? NSNumber)?.int64Value ?? 0,
            completedByUserId: data["completedByUserId"] as? String ?? "",
            notes: data["notes"] as? String,
            scheduledTime: (data["scheduledTime"] as? NSNumber)?.int64Value
        )
    }
}

enum FirebaseError: LocalizedError {
    case notAuthenticated
    
    var errorDescription: String? {
        switch self {
        case .notAuthenticated: return "User not authenticated"
        }
    }
}
