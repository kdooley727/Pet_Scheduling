//
//  ThemeProviderView.swift
//  PetSchedulingiOS
//

import SwiftUI

struct ThemeProviderView: View {
    @EnvironmentObject var authState: AuthState
    @AppStorage("appearanceMode") private var appearanceMode = "system"
    @AppStorage("paletteLight") private var paletteLightRaw = ColorPalette.ocean.rawValue
    @AppStorage("paletteDark") private var paletteDarkRaw = ColorPalette.lavender.rawValue
    @Environment(\.colorScheme) private var systemColorScheme
    
    private var paletteLight: ColorPalette {
        ColorPalette(rawValue: paletteLightRaw) ?? .ocean
    }
    
    private var paletteDark: ColorPalette {
        ColorPalette(rawValue: paletteDarkRaw) ?? .lavender
    }
    
    private var appTheme: AppTheme {
        AppTheme(
            appearanceMode: appearanceMode,
            paletteLight: paletteLight,
            paletteDark: paletteDark,
            colorScheme: appearanceMode == "system" ? systemColorScheme : (appearanceMode == "dark" ? .dark : .light)
        )
    }
    
    var body: some View {
        ContentView()
            .environmentObject(authState)
            .environment(\.appTheme, appTheme)
            .tint(appTheme.accentColor)
    }
}
