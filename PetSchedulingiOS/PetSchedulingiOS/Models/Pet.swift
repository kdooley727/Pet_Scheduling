//
//  Pet.swift
//  PetSchedulingiOS
//

import Foundation

struct Pet: Identifiable, Codable, Hashable {
    var id: String { petId }
    let petId: String
    let userId: String
    var isSharedWithMe: Bool = false
    var sharedByEmail: String? = nil
    var sharedPermissionLevel: String? = nil  // "view", "edit", "manage" when shared
    var name: String
    var type: String
    var breed: String?
    var birthDate: Int64?
    var photoUrl: String?
    var notes: String?
    var vetName: String?
    var vetPhone: String?
    var vetEmail: String?
    var vetAddress: String?
    var emergencyContactName: String?
    var emergencyContactPhone: String?
    var emergencyContactEmail: String?
    var emergencyContactRelationship: String?
    let createdAt: Int64
    var updatedAt: Int64
    
    init(petId: String = UUID().uuidString, userId: String, name: String, type: String,
         breed: String? = nil, birthDate: Int64? = nil, photoUrl: String? = nil, notes: String? = nil,
         vetName: String? = nil, vetPhone: String? = nil, vetEmail: String? = nil, vetAddress: String? = nil,
         emergencyContactName: String? = nil, emergencyContactPhone: String? = nil,
         emergencyContactEmail: String? = nil, emergencyContactRelationship: String? = nil,
         createdAt: Int64 = Int64(Date().timeIntervalSince1970 * 1000),
         updatedAt: Int64 = Int64(Date().timeIntervalSince1970 * 1000)) {
        self.petId = petId
        self.userId = userId
        self.name = name
        self.type = type
        self.breed = breed
        self.birthDate = birthDate
        self.photoUrl = photoUrl
        self.notes = notes
        self.vetName = vetName
        self.vetPhone = vetPhone
        self.vetEmail = vetEmail
        self.vetAddress = vetAddress
        self.emergencyContactName = emergencyContactName
        self.emergencyContactPhone = emergencyContactPhone
        self.emergencyContactEmail = emergencyContactEmail
        self.emergencyContactRelationship = emergencyContactRelationship
        self.createdAt = createdAt
        self.updatedAt = updatedAt
    }
}
