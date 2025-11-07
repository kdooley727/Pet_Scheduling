# Pet Scheduling App 🐾

A comprehensive Android application for managing pet care schedules with AI-powered suggestions, built with modern Android development practices.

## 📱 Overview

Pet Scheduling is a full-featured mobile application designed to help pet owners manage their pets' daily care routines. The app allows users to create detailed schedules for feeding, medication, grooming, exercise, and other pet care tasks, with intelligent AI suggestions and reminder notifications.

## ✨ Key Features

### 🐕 Pet Management
- **Multi-Pet Support**: Manage multiple pets with individual profiles
- **Pet Profiles**: Store pet information including name, type, breed, birth date, photos, and notes
- **Pet Types Supported**: Dogs, Cats, Birds, Fish, Rabbits, Hamsters, and more

### 📅 Schedule Management
- **Task Scheduling**: Create recurring tasks with customizable schedules
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

### 🤖 AI-Powered Features
- **Smart Schedule Suggestions**: Google Gemini AI integration generates personalized care schedules based on pet information
- **Intelligent Recommendations**: AI suggests optimal feeding times, exercise routines, and care tasks

### 🔔 Notifications & Reminders
- **Customizable Reminders**: Set reminders 5, 15, 30, 60, or 120 minutes before scheduled tasks
- **Background Notifications**: WorkManager integration for reliable reminder delivery
- **Notification Channels**: Organized notification system for better user experience

### 👥 Collaboration Features
- **Shared Access**: Share pet schedules with family members or pet sitters
- **Permission Levels**: View-only, Edit, or Full Access permissions
- **Multi-User Support**: Multiple users can manage the same pet's schedule

### 🔐 Authentication
- **Firebase Authentication**: Secure email/password authentication
- **Google Sign-In**: One-tap Google authentication integration
- **Session Management**: Persistent login sessions

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

## 📁 Project Structure

```
app/src/main/java/com/hfad/pet_scheduling/
├── data/
│   ├── local/
│   │   ├── AppDatabase.kt          # Room database configuration
│   │   ├── dao/                     # Data Access Objects
│   │   └── entities/                # Database entities
│   └── repository/                  # Repository pattern implementation
├── ui/
│   ├── auth/                        # Authentication screens
│   ├── pets/                        # Pet management screens
│   └── theme/                       # App theming
├── utils/                           # Utility classes
│   ├── GeminiHelper.kt             # AI integration
│   ├── NotificationHelper.kt       # Notification management
│   └── GoogleSignInHelper.kt       # Google Sign-In
├── viewmodels/                      # ViewModels for UI
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
   git clone https://github.com/your-username/Pet_Scheduling.git
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

## 🔮 Future Enhancements

- [ ] Cloud sync across devices
- [ ] Photo upload and storage
- [ ] Calendar integration
- [ ] Export schedules to PDF
- [ ] Widget support for quick task viewing
- [ ] Dark mode theme
- [ ] Multi-language support
- [ ] Pet health tracking
- [ ] Vet appointment reminders
- [ ] Medication dosage tracking

## 📝 License

This project is open source and available for educational purposes.

## 👨‍💻 Author

**Your Name**
- GitHub: [@your-username](https://github.com/your-username)
- LinkedIn: [Your LinkedIn Profile]

---

⭐ If you find this project helpful, please consider giving it a star!

