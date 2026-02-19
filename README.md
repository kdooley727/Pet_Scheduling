# Pet Scheduling App 🐾

A comprehensive Android application for managing pet care schedules with AI-powered suggestions, built with modern Android development practices.

## 📱 Overview

Pet Scheduling is a full-featured **cross-platform** mobile application (Android + iOS) designed to help pet owners manage their pets' daily care routines. The app allows users to create detailed schedules for feeding, medication, grooming, exercise, and other pet care tasks, with intelligent AI suggestions and reminder notifications. Share pets with family, pet sitters, or boarding facilities—all synced via Firebase.

## ✨ Key Features

### 🐕 Pet Management
- **Multi-Pet Support**: Manage multiple pets with individual profiles
- **Pet Profiles**: Store pet information including name, type, breed, birth date, photos, and notes
- **Pet Types Supported**: Dogs, Cats, Birds, Fish, Rabbits, Hamsters, and more
- **Pet Photos**: Upload and store pet photos with image picker integration
- **Emergency Contacts**: Store veterinarian and emergency contact information for each pet

### 📅 Schedule Management
- **Task Scheduling**: Create recurring tasks with customizable schedules
- **Task Templates**: Save and reuse common task configurations
- **Task Categories**: 
  - Feeding
  - Medication
  - Vet Visits
  - Grooming
  - Exercise
  - Training
  - Reminders
  - Custom tasks
- **Recurrence Patterns**: Daily, Weekly, Monthly, Yearly, or Custom schedules
- **Task Completion Tracking**: Mark tasks as completed with notes and timestamps
- **Task History**: View completed tasks and completion statistics

### 🤖 AI-Powered Features
- **Smart Schedule Suggestions**: Google Gemini AI integration generates personalized care schedules based on pet information
- **Intelligent Recommendations**: AI suggests optimal feeding times, exercise routines, and care tasks

### 🔔 Notifications & Reminders
- **Customizable Reminders**: Set reminders 5, 15, 30, 60, or 120 minutes before scheduled tasks
- **Background Notifications**: WorkManager integration for reliable reminder delivery
- **Notification Channels**: Organized notification system for better user experience
- **Notification Rescheduling**: Automatic rescheduling of missed notifications
- **Action Notifications**: Quick actions directly from notification tray

### 👥 Collaboration Features
- **Pet Sharing**: Share pets with family, couples, pet sitters, or boarding facilities via email
- **Cross-Platform**: Share between Android and iOS—both users see the same pets and tasks
- **Permission Levels**: View-only (read), Edit (add/complete tasks), or Full Access (manage pet & share)
- **Manage Shared Access**: View "Shared With Me" and "I Shared" in Settings; revoke access anytime
- **Cloud Sync**: Automatic synchronization of pets, tasks, and shared access across devices

### 🔐 Authentication
- **Firebase Authentication**: Secure email/password authentication
- **Google Sign-In**: One-tap Google authentication integration
- **Session Management**: Persistent login sessions

### 📊 Statistics & Analytics
- **Completion Rates**: Track task completion statistics for each pet
- **Activity History**: View recent activity and task completion history
- **Category Analytics**: See completion rates by task category
- **Pet Statistics**: Individual statistics for each pet

### 🎨 User Experience
- **Dark Theme**: Toggle between light and dark themes with night mode support
- **Settings Screen**: Customize app preferences, notifications, and sync settings
- **Search & Filter**: Quickly find pets and tasks with search functionality
- **Smooth Animations**: Enhanced UI with fade, slide, and scale animations
- **Material Design 3**: Modern, accessible UI components

### 📤 Export & Sharing
- **Export Functionality**: Export schedules and pet details to PDF or CSV format
- **Vet Records**: Generate formatted reports for veterinarian visits
- **Data Export**: Backup your pet data and schedules

### 📱 Widget Support
- **Home Screen Widget**: Quick access to upcoming tasks directly from home screen
- **Task List Widget**: View your pet's scheduled tasks at a glance

## 🛠️ Technology Stack

### Core Technologies
- **Language**: Kotlin
- **Minimum SDK**: 26 (Android 8.0)
- **Target SDK**: 34 (Android 14)
- **Architecture**: MVVM (Model-View-ViewModel)

### Android Architecture Components
- **Room Database**: Local data persistence with SQLite
- **ViewModel & LiveData**: Lifecycle-aware data management
- **Navigation Component**: Fragment-based navigation
- **WorkManager**: Background task scheduling for notifications

### Libraries & Frameworks
- **Firebase Services**:
  - Authentication
  - Cloud Firestore
  - Cloud Storage
  - Cloud Messaging
  - Analytics
- **Google AI (Gemini)**: AI-powered schedule generation
- **Retrofit**: RESTful API communication
- **Glide**: Image loading and caching
- **Coroutines**: Asynchronous programming
- **Material Design Components**: Modern UI/UX

### Database Schema
- **Pets Table**: Pet profiles and information
- **Schedule Tasks Table**: Recurring task definitions
- **Completed Tasks Table**: Task completion history
- **Shared Access Table**: User permissions and sharing
- **Emergency Contacts Table**: Veterinarian and emergency contact information

## 📁 Project Structure

```
app/src/main/java/com/hfad/pet_scheduling/
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt          # Room database configuration
│   │   ├── dao/                     # Data Access Objects
│   │   └── entities/                # Database entities (Pet, Task, EmergencyContact)
│   ├── remote/                      # Remote data sources
│   │   └── FirestoreSyncService.kt  # Firebase Firestore sync
│   ├── StatisticsData.kt           # Statistics data models
│   ├── TaskTemplate.kt             # Task template models
│   └── repository/                  # Repository pattern implementation
├── ui/
│   ├── auth/                        # Authentication screens
│   ├── pets/                        # Pet management screens
│   ├── schedules/                   # Task scheduling screens
│   ├── sharing/                     # Pet sharing and collaboration
│   ├── settings/                    # Settings screen
│   ├── statistics/                  # Statistics and analytics
│   └── theme/                       # App theming
├── utils/                           # Utility classes
│   ├── GeminiHelper.kt             # AI integration
│   ├── NotificationHelper.kt       # Notification management
│   ├── NotificationScheduler.kt    # Notification scheduling
│   ├── NotificationRescheduler.kt  # Notification rescheduling
│   ├── CloudSyncManager.kt         # Cloud sync management
│   ├── ExportHelper.kt             # Data export functionality
│   ├── FirebaseStorageHelper.kt   # Firebase storage operations
│   ├── ImagePicker.kt              # Image selection and upload
│   ├── StatisticsCalculator.kt    # Statistics calculations
│   └── GoogleSignInHelper.kt       # Google Sign-In
├── viewmodels/                      # ViewModels for UI
├── receivers/                       # Broadcast receivers
│   └── NotificationActionReceiver.kt # Notification action handling
├── workers/                         # Background workers
│   └── ReminderWorker.kt           # WorkManager reminder worker
├── widgets/                         # App widgets
│   └── TaskWidgetProvider.kt       # Home screen widget
└── MainActivity.kt                  # Main activity
```

## 🚀 Getting Started

### Prerequisites
- Android Studio Hedgehog (2023.1.1) or later
- Android SDK 26 or higher
- Kotlin 1.9.22 or later
- JDK 8 or higher

### Setup Instructions

1. **Clone the repository**
   ```bash
   git clone https://github.com/kdooley727/Pet_Scheduling.git
   cd Pet_Scheduling
   ```

2. **Firebase Configuration**
   - Create a Firebase project at [Firebase Console](https://console.firebase.google.com/)
   - Enable Authentication (Email/Password and Google Sign-In)
   - Enable Firestore Database
   - Enable Cloud Storage
   - Download `google-services.json` and place it in `app/` directory

3. **Google Gemini API Key**
   - Get a Gemini API key from [Google AI Studio](https://makersuite.google.com/app/apikey)
   - Add the API key to your project configuration (see `GeminiHelper.kt`)

4. **Build the project**
   ```bash
   ./gradlew build
   ```

5. **Run the app**
   - Connect an Android device or start an emulator
   - Click "Run" in Android Studio or use `./gradlew installDebug`

## 📸 Screenshots

_Add screenshots of your app here to showcase the UI/UX_

## 🎯 Key Technical Highlights

- **MVVM Architecture**: Clean separation of concerns with ViewModels and LiveData
- **Repository Pattern**: Centralized data access layer
- **Room Database**: Type-safe database queries with compile-time verification
- **Coroutines**: Efficient asynchronous operations
- **Material Design 3**: Modern, accessible UI components
- **Firebase Integration**: Scalable backend services
- **AI Integration**: Google Gemini for intelligent features
- **WorkManager**: Reliable background task execution

## 🆕 Recent Updates (Latest Release)

### Version 1.3 Features (February 2025)
- ✅ **Pet Sharing Between Users**: Share pets with family, pet sitters, or boarding facilities via email
- ✅ **Cross-Platform Sharing**: Share between Android and iOS users seamlessly
- ✅ **Permission Levels**: View-only, Edit, or Full Access when sharing
- ✅ **Manage Shared Access**: View "Shared With Me" and "I Shared" in Settings
- ✅ **iOS App**: Full SwiftUI app with Firebase sync, Google Sign-In, and feature parity with Android
- ✅ **iOS Task Management**: Swipe-to-delete, pull-to-refresh, mark complete, local notifications
- ✅ **iOS Polish**: Dynamic Type, Dark mode, Share pet info to other apps
- ✅ **Firestore Rules**: Updated for users, shared_access, pets, tasks, and completed_tasks collections

### Version 1.1 Features
- ✅ **Statistics Dashboard**: View completion rates, activity history, and analytics
- ✅ **Enhanced Sharing**: Improved pet sharing with better permission management
- ✅ **Settings Screen**: Comprehensive settings for notifications, theme, and sync preferences
- ✅ **Home Screen Widget**: Quick access to upcoming tasks
- ✅ **Dark Theme**: Full dark mode support with night theme
- ✅ **Export Functionality**: Export pet data and schedules to PDF/CSV
- ✅ **Emergency Contacts**: Store veterinarian and emergency contact information
- ✅ **Task Templates**: Save and reuse common task configurations
- ✅ **Cloud Sync Improvements**: Enhanced synchronization with better error handling
- ✅ **Notification Enhancements**: Improved notification scheduling and rescheduling
- ✅ **UI Improvements**: Smooth animations and better visual feedback
- ✅ **Image Picker**: Enhanced pet photo upload and management

### Bug Fixes
- ✅ Fixed emergency contacts cloud sync
- ✅ Improved pet deletion functionality
- ✅ Enhanced UI button visibility in both themes
- ✅ Better cloud synchronization reliability

## 🔮 Future Enhancements

- [ ] Calendar integration
- [ ] Multi-language support
- [ ] Pet health tracking
- [ ] Medication dosage tracking
- [ ] Advanced AI recommendations
- [ ] Social features and pet community
- [ ] Integration with pet wearables

## 📝 License

This project is open source and available for educational purposes.

## 👨‍💻 Author

**kdooley727**
- GitHub: [@kdooley727](https://github.com/kdooley727)

---

⭐ If you find this project helpful, please consider giving it a star!

