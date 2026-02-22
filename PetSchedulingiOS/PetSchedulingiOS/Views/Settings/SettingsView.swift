//
//  SettingsView.swift
//  PetSchedulingiOS
//

import SwiftUI
import FirebaseAuth

struct SettingsView: View {
    @AppStorage("appearanceMode") private var appearanceMode = "system"
    @AppStorage("paletteLight") private var paletteLightRaw = ColorPalette.ocean.rawValue
    @AppStorage("paletteDark") private var paletteDarkRaw = ColorPalette.lavender.rawValue
    @AppStorage("notificationsEnabled") private var notificationsEnabled = true
    @AppStorage("defaultReminderMinutes") private var defaultReminderMinutes = 15
    @StateObject private var syncStatus = SyncStatusService.shared
    @State private var showManageSharedAccess = false
    @State private var isSyncing = false
    
    private var paletteLight: Binding<ColorPalette> {
        Binding(
            get: { ColorPalette(rawValue: paletteLightRaw) ?? .ocean },
            set: { paletteLightRaw = $0.rawValue }
        )
    }
    
    private var paletteDark: Binding<ColorPalette> {
        Binding(
            get: { ColorPalette(rawValue: paletteDarkRaw) ?? .lavender },
            set: { paletteDarkRaw = $0.rawValue }
        )
    }
    
    var body: some View {
        List {
            Section {
                Picker("Theme", selection: $appearanceMode) {
                    Text("System").tag("system")
                    Text("Light").tag("light")
                    Text("Dark").tag("dark")
                }
                .accessibilityLabel("Theme")
                .accessibilityHint("Choose light, dark, or system appearance")
                
                VStack(alignment: .leading, spacing: 12) {
                    Text("Light Theme Color")
                        .font(.subheadline.weight(.medium))
                    PalettePicker(selection: paletteLight, isDark: false)
                }
                .listRowInsets(EdgeInsets(top: 12, leading: 16, bottom: 12, trailing: 16))
                
                VStack(alignment: .leading, spacing: 12) {
                    Text("Dark Theme Color")
                        .font(.subheadline.weight(.medium))
                    PalettePicker(selection: paletteDark, isDark: true)
                }
                .listRowInsets(EdgeInsets(top: 12, leading: 16, bottom: 12, trailing: 16))
            } header: {
                Text("Appearance")
            } footer: {
                Text("Choose separate color palettes for light and dark mode.")
            }
            
            Section {
                if let user = Auth.auth().currentUser {
                    LabeledContent("Email", value: user.email ?? "—")
                        .accessibilityLabel("Email, \(user.email ?? "not set")")
                    if let name = user.displayName, !name.isEmpty {
                        LabeledContent("Name", value: name)
                            .accessibilityLabel("Name, \(name)")
                    }
                }
            } header: {
                Text("Account")
            }
            
            Section {
                Toggle("Notifications", isOn: $notificationsEnabled)
                    .accessibilityLabel("Notifications")
                    .accessibilityHint(notificationsEnabled ? "Notifications are on" : "Notifications are off")
                
                Picker("Default Reminder", selection: $defaultReminderMinutes) {
                    ForEach(Constants.reminderTimes, id: \.self) { minutes in
                        Text(DateTimeUtils.formatReminder(minutes)).tag(minutes)
                    }
                }
                .accessibilityLabel("Default reminder time")
                .accessibilityHint("When to remind before new tasks")
            } header: {
                Text("Preferences")
            } footer: {
                Text("Default reminder is used when creating new tasks.")
            }
            
            Section {
                Button {
                    triggerSync()
                } label: {
                    HStack {
                        Label("Sync Now", systemImage: "arrow.clockwise")
                        Spacer()
                        if isSyncing {
                            ProgressView()
                                .scaleEffect(0.8)
                        }
                    }
                }
                .disabled(isSyncing)
                .accessibilityLabel("Sync now")
                .accessibilityHint("Refresh data from cloud")
                
                LabeledContent("Last synced", value: syncStatus.lastSyncFormatted)
                    .accessibilityLabel("Last synced, \(syncStatus.lastSyncFormatted)")
            } header: {
                Text("Cloud Sync")
            } footer: {
                Text("Data syncs with Firebase. Tap Sync Now to refresh.")
            }
            
            Section {
                Button {
                    showManageSharedAccess = true
                } label: {
                    Label("Manage Shared Access", systemImage: "person.2")
                }
                .accessibilityLabel("Manage shared access")
                .accessibilityHint("View and manage pets shared with you")
            } header: {
                Text("Sharing")
            }
            
            Section {
                LabeledContent("Version", value: Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "1.0")
                    .accessibilityLabel("App version")
            } header: {
                Text("About")
            }
        }
        .navigationTitle("Settings")
        .dynamicTypeSize(...DynamicTypeSize.accessibility3)
        .onReceive(NotificationCenter.default.publisher(for: .syncCompleted)) { _ in
            isSyncing = false
        }
        .navigationDestination(isPresented: $showManageSharedAccess) {
            ManageSharedAccessView()
        }
    }
    
    private func triggerSync() {
        isSyncing = true
        NotificationCenter.default.post(name: .syncNowRequested, object: nil)
    }
}

extension Notification.Name {
    static let syncNowRequested = Notification.Name("syncNowRequested")
    static let syncCompleted = Notification.Name("syncCompleted")
}
