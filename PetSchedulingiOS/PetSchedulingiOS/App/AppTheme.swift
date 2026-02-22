//
//  AppTheme.swift
//  PetSchedulingiOS
//

import SwiftUI

// MARK: - Color Palette Presets

enum ColorPalette: String, CaseIterable, Identifiable {
    case ocean = "ocean"
    case forest = "forest"
    case sunset = "sunset"
    case lavender = "lavender"
    case coral = "coral"
    case mint = "mint"
    case slate = "slate"
    case rose = "rose"
    
    var id: String { rawValue }
    
    var displayName: String {
        switch self {
        case .ocean: return "Ocean"
        case .forest: return "Forest"
        case .sunset: return "Sunset"
        case .lavender: return "Lavender"
        case .coral: return "Coral"
        case .mint: return "Mint"
        case .slate: return "Slate"
        case .rose: return "Rose"
        }
    }
    
    var icon: String {
        switch self {
        case .ocean: return "drop.fill"
        case .forest: return "leaf.fill"
        case .sunset: return "sun.max.fill"
        case .lavender: return "sparkles"
        case .coral: return "flame.fill"
        case .mint: return "leaf.circle.fill"
        case .slate: return "circle.fill"
        case .rose: return "heart.fill"
        }
    }
    
    var accentLight: Color {
        switch self {
        case .ocean: return Color(red: 0.2, green: 0.5, blue: 0.8)
        case .forest: return Color(red: 0.2, green: 0.6, blue: 0.4)
        case .sunset: return Color(red: 0.9, green: 0.5, blue: 0.2)
        case .lavender: return Color(red: 0.5, green: 0.4, blue: 0.8)
        case .coral: return Color(red: 0.95, green: 0.4, blue: 0.4)
        case .mint: return Color(red: 0.3, green: 0.8, blue: 0.6)
        case .slate: return Color(red: 0.2, green: 0.3, blue: 0.4)
        case .rose: return Color(red: 0.85, green: 0.35, blue: 0.45)
        }
    }
    
    var accentDark: Color {
        switch self {
        case .ocean: return Color(red: 0.4, green: 0.65, blue: 0.95)
        case .forest: return Color(red: 0.4, green: 0.75, blue: 0.55)
        case .sunset: return Color(red: 0.95, green: 0.6, blue: 0.4)
        case .lavender: return Color(red: 0.65, green: 0.55, blue: 0.9)
        case .coral: return Color(red: 0.95, green: 0.5, blue: 0.5)
        case .mint: return Color(red: 0.45, green: 0.85, blue: 0.7)
        case .slate: return Color(red: 0.5, green: 0.6, blue: 0.75)
        case .rose: return Color(red: 0.9, green: 0.45, blue: 0.55)
        }
    }
    
    var gradientLight: [Color] {
        switch self {
        case .ocean: return [Color(red: 0.2, green: 0.5, blue: 0.8), Color(red: 0.3, green: 0.6, blue: 0.9)]
        case .forest: return [Color(red: 0.2, green: 0.6, blue: 0.4), Color(red: 0.3, green: 0.7, blue: 0.5)]
        case .sunset: return [Color(red: 0.9, green: 0.5, blue: 0.2), Color(red: 0.95, green: 0.4, blue: 0.3)]
        case .lavender: return [Color(red: 0.5, green: 0.4, blue: 0.8), Color(red: 0.6, green: 0.5, blue: 0.9)]
        case .coral: return [Color(red: 0.95, green: 0.4, blue: 0.4), Color(red: 0.9, green: 0.5, blue: 0.5)]
        case .mint: return [Color(red: 0.3, green: 0.8, blue: 0.6), Color(red: 0.4, green: 0.85, blue: 0.7)]
        case .slate: return [Color(red: 0.2, green: 0.3, blue: 0.4), Color(red: 0.3, green: 0.4, blue: 0.5)]
        case .rose: return [Color(red: 0.85, green: 0.35, blue: 0.45), Color(red: 0.9, green: 0.4, blue: 0.5)]
        }
    }
    
    var gradientDark: [Color] {
        switch self {
        case .ocean: return [Color(red: 0.2, green: 0.45, blue: 0.7), Color(red: 0.3, green: 0.55, blue: 0.85)]
        case .forest: return [Color(red: 0.15, green: 0.5, blue: 0.35), Color(red: 0.25, green: 0.6, blue: 0.45)]
        case .sunset: return [Color(red: 0.7, green: 0.35, blue: 0.2), Color(red: 0.85, green: 0.45, blue: 0.3)]
        case .lavender: return [Color(red: 0.4, green: 0.3, blue: 0.65), Color(red: 0.5, green: 0.4, blue: 0.75)]
        case .coral: return [Color(red: 0.75, green: 0.3, blue: 0.3), Color(red: 0.85, green: 0.4, blue: 0.4)]
        case .mint: return [Color(red: 0.2, green: 0.6, blue: 0.5), Color(red: 0.3, green: 0.7, blue: 0.6)]
        case .slate: return [Color(red: 0.25, green: 0.35, blue: 0.45), Color(red: 0.35, green: 0.45, blue: 0.55)]
        case .rose: return [Color(red: 0.65, green: 0.25, blue: 0.35), Color(red: 0.75, green: 0.3, blue: 0.4)]
        }
    }
}

// MARK: - App Theme (passed via environment)

struct AppTheme {
    let appearanceMode: String
    let paletteLight: ColorPalette
    let paletteDark: ColorPalette
    let colorScheme: ColorScheme?
    
    var isDarkMode: Bool {
        switch appearanceMode {
        case "dark": return true
        case "light": return false
        default: return colorScheme == .dark
        }
    }
    
    var currentPalette: ColorPalette {
        isDarkMode ? paletteDark : paletteLight
    }
    
    var accentColor: Color {
        isDarkMode ? paletteDark.accentDark : paletteLight.accentLight
    }
    
    var gradientColors: [Color] {
        isDarkMode ? paletteDark.gradientDark : paletteLight.gradientLight
    }
    
    var gradient: LinearGradient {
        LinearGradient(
            colors: gradientColors,
            startPoint: .topLeading,
            endPoint: .bottomTrailing
        )
    }
}

struct AppThemeKey: EnvironmentKey {
    static let defaultValue: AppTheme = AppTheme(
        appearanceMode: "system",
        paletteLight: .ocean,
        paletteDark: .lavender,
        colorScheme: nil
    )
}

extension EnvironmentValues {
    var appTheme: AppTheme {
        get { self[AppThemeKey.self] }
        set { self[AppThemeKey.self] = newValue }
    }
}
