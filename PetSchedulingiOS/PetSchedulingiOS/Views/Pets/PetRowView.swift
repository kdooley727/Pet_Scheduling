//
//  PetRowView.swift
//  PetSchedulingiOS
//

import SwiftUI

struct PetRowView: View {
    let pet: Pet
    @Environment(\.appTheme) private var theme
    
    var body: some View {
        HStack(spacing: 16) {
            if let urlString = pet.photoUrl, let url = URL(string: urlString) {
                AsyncImage(url: url) { image in
                    image
                        .resizable()
                        .aspectRatio(contentMode: .fill)
                } placeholder: {
                    ZStack {
                        Circle()
                            .fill(theme.gradient.opacity(0.2))
                        Image(systemName: "pawprint.circle.fill")
                            .font(.title)
                            .foregroundStyle(theme.accentColor)
                    }
                }
                .frame(width: 72, height: 72)
                .clipShape(Circle())
                .overlay(
                    Circle()
                        .stroke(theme.accentColor.opacity(0.3), lineWidth: 2)
                )
            } else {
                ZStack {
                    Circle()
                        .fill(theme.gradient.opacity(0.2))
                    Image(systemName: "pawprint.circle.fill")
                        .font(.system(size: 32))
                        .foregroundStyle(theme.accentColor)
                }
                .frame(width: 72, height: 72)
                .overlay(
                    Circle()
                        .stroke(theme.accentColor.opacity(0.3), lineWidth: 2)
                )
            }
            
            VStack(alignment: .leading, spacing: 4) {
                Text(pet.name)
                    .font(.headline)
                Text(Constants.PetType.displayName(for: pet.type))
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
                if pet.isSharedWithMe, let email = pet.sharedByEmail {
                    Text("Shared by \(email)")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                } else if let breed = pet.breed, !breed.isEmpty {
                    Text(breed)
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }
            }
        }
        .padding(.vertical, 8)
        .accessibilityElement(children: .combine)
        .accessibilityLabel("\(pet.name), \(Constants.PetType.displayName(for: pet.type))")
        .accessibilityHint("Double tap to view schedule")
    }
}
