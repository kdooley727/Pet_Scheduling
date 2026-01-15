# iOS App Setup Guide

This directory contains the iOS app for Pet Scheduling. The app uses the shared Kotlin Multiplatform module for business logic and data management.

## Prerequisites

- Xcode 14.0 or later
- CocoaPods (for Firebase iOS SDK)
- macOS with Apple Silicon or Intel processor

## Setup Instructions

### 1. Create Xcode Project

1. Open Xcode
2. Create a new project:
   - Choose "iOS" → "App"
   - Product Name: `PetSchedulingIOS`
   - Interface: SwiftUI
   - Language: Swift
   - Organization Identifier: `com.hfad`
   - Bundle Identifier: `com.hfad.pet_scheduling.ios`

### 2. Add Shared Framework

1. In Xcode, go to File → Add Files to "PetSchedulingIOS"
2. Navigate to the project root and select the `shared` folder
3. Make sure "Create groups" is selected
4. Add the shared module to your target

### 3. Configure Build Settings

1. Select your project in Xcode
2. Go to Build Settings
3. Search for "Framework Search Paths"
4. Add: `$(SRCROOT)/../shared/build/xcode-frameworks/$(CONFIGURATION)/$(SDK_NAME)`

### 4. Add Firebase (Same Project as Android)

1. In Firebase Console (existing project), add a new **iOS app**
2. Use the same bundle ID as above
3. Download `GoogleService-Info.plist`
4. In Xcode, add `GoogleService-Info.plist` to your app target

1. Install CocoaPods if not already installed:
   ```bash
   sudo gem install cocoapods
   ```

2. Create `Podfile` in `iosApp/PetSchedulingIOS/` (already provided in repo):
   ```ruby
   platform :ios, '13.0'
   use_frameworks!

   target 'PetSchedulingIOS' do
     pod 'Firebase/Auth'
     pod 'Firebase/Firestore'
     pod 'Firebase/Storage'
     pod 'Firebase/Messaging'
     pod 'Firebase/Analytics'
     pod 'GoogleSignIn'
   end
   ```

3. Install pods:
   ```bash
   cd iosApp/PetSchedulingIOS
   pod install
   ```

4. Open `PetSchedulingIOS.xcworkspace` (not `.xcodeproj`)

### 5. Build Shared Framework

From the project root:
```bash
./gradlew :shared:embedAndSignAppleFrameworkForXcode
```

Or configure Xcode to build it automatically:
1. In Xcode, add a new Run Script Phase
2. Add script:
   ```bash
   cd "$SRCROOT/../.."
   ./gradlew :shared:embedAndSignAppleFrameworkForXcode
   ```

### 6. Create iOS Database Factory

The shared module expects a `DatabaseDriverFactory` implementation. A template is already added:

- `iosApp/PetSchedulingIOS/PetSchedulingIOS/DatabaseDriverFactoryImpl.swift`

```swift
import shared

class DatabaseDriverFactoryImpl: DatabaseDriverFactory {
    func createDriver() -> SqlDriver {
        return NativeSqliteDriver(
            schema: PetSchedulingDatabase.Companion.shared.Schema,
            name: "pet_scheduling_database.db"
        )
    }
}
```

### 7. Initialize Firebase + Database

In your `App.swift`:

```swift
import SwiftUI
import shared

// Call this once during app startup
AppBootstrap.configure()

@main
struct PetSchedulingIOSApp: App {
    init() {
        AppBootstrap.configure()
    }
    
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
```

## Project Structure

```
iosApp/
└── PetSchedulingIOS/
    ├── PetSchedulingIOS/
    │   ├── App.swift              # App entry point
    │   ├── AppBootstrap.swift     # Firebase + DB setup
    │   ├── DatabaseDriverFactoryImpl.swift
    │   ├── Views/                 # SwiftUI views
    │   ├── ViewModels/            # iOS-specific view models (if needed)
    │   └── Services/              # Platform-specific services
    ├── Podfile                    # CocoaPods dependencies
    └── Info.plist                 # App configuration
```

## Next Steps

1. **Create UI Views**: Implement SwiftUI views that use the shared repositories
2. **Firebase Integration**: Implement Firebase for iOS using the iOS SDK
3. **Navigation**: Set up navigation using SwiftUI NavigationStack
4. **State Management**: Use `@StateObject` and `@ObservedObject` with shared StateFlows

## Building

1. Build shared framework:
   ```bash
   ./gradlew :shared:embedAndSignAppleFrameworkForXcode
   ```

2. Open Xcode and build:
   - Product → Build (⌘B)
   - Product → Run (⌘R)

## Troubleshooting

### Framework Not Found
- Make sure you've built the shared framework
- Check Framework Search Paths in Build Settings
- Clean build folder (⌘⇧K) and rebuild

### Pod Installation Issues
- Run `pod repo update`
- Delete `Pods/` and `Podfile.lock`, then `pod install` again

### Database Issues
- Ensure SQLDelight schemas are properly generated
- Check that database file path is correct

## Resources

- [SwiftUI Documentation](https://developer.apple.com/documentation/swiftui)
- [Firebase iOS SDK](https://firebase.google.com/docs/ios/setup)
- [Kotlin/Native Interop](https://kotlinlang.org/docs/native-objc-interop.html)

