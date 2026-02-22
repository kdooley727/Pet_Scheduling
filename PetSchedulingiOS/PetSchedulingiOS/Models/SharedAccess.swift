//
//  SharedAccess.swift
//  PetSchedulingiOS
//

import Foundation

struct SharedAccess: Identifiable {
    var id: String { shareId }
    let shareId: String
    let petId: String
    let ownerUserId: String
    let ownerEmail: String?
    let sharedWithUserId: String
    let sharedWithEmail: String?
    let permissionLevel: String
    let createdAt: Int64
    var isActive: Bool
}
