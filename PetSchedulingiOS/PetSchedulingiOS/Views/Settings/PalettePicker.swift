//
//  PalettePicker.swift
//  PetSchedulingiOS
//

import SwiftUI

struct PalettePicker: View {
    @Binding var selection: ColorPalette
    var isDark: Bool
    
    private let columns = [
        GridItem(.adaptive(minimum: 44), spacing: 12)
    ]
    
    var body: some View {
        LazyVGrid(columns: columns, spacing: 12) {
            ForEach(ColorPalette.allCases) { palette in
                Button {
                    selection = palette
                } label: {
                    VStack(spacing: 6) {
                        ZStack {
                            Circle()
                                .fill(
                                    LinearGradient(
                                        colors: isDark ? palette.gradientDark : palette.gradientLight,
                                        startPoint: .topLeading,
                                        endPoint: .bottomTrailing
                                    )
                                )
                                .frame(width: 40, height: 40)
                                .overlay(
                                    Circle()
                                        .strokeBorder(selection == palette ? Color.primary : Color.clear, lineWidth: 3)
                                )
                                .shadow(color: .black.opacity(0.15), radius: 2, x: 0, y: 1)
                            Image(systemName: palette.icon)
                                .font(.system(size: 18, weight: .medium))
                                .foregroundStyle(.white)
                                .shadow(color: .black.opacity(0.3), radius: 1)
                        }
                        Text(palette.displayName)
                            .font(.caption2)
                            .lineLimit(1)
                            .foregroundStyle(.primary)
                    }
                    .frame(maxWidth: .infinity)
                }
                .buttonStyle(.plain)
            }
        }
    }
}
