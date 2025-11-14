# Release Notes - Version 1.1

## What's New

### Bug Fixes:
- ✅ **Fixed emergency contacts sync** - Veterinarian and emergency contact information now syncs properly with cloud storage
- ✅ **Improved pet deletion** - Pets are now properly deleted from both local database and cloud storage
- ✅ **Enhanced delete button visibility** - Delete button now displays correctly in both light and dark themes

### Improvements:
- 🔄 **Better cloud synchronization** - Emergency contact fields (vet info, emergency contacts) are now included in cloud sync
- 🎨 **UI improvements** - More uniform button styling on edit pet screen

---

## Version 1.0 Features (Still Available)

Pet Scheduling helps you manage your pets' care schedules and track important information.

### Key Features:
- ✅ **Pet Management**: Add pets with photos, edit details, and delete pets
- ✅ **Task Scheduling**: Create recurring tasks (feeding, medication, grooming, etc.)
- ✅ **Notifications**: Get reminder notifications for scheduled tasks
- ✅ **Cloud Sync**: Your data syncs across devices via Firebase
- ✅ **Google Sign-In**: Easy authentication with your Google account
- ✅ **Dark Theme**: Toggle between light and dark themes
- ✅ **Emergency Contacts**: Store veterinarian and emergency contact information
- ✅ **Statistics**: View completion rates and task analytics
- ✅ **Export**: Export schedules and pet details for vet records (PDF/CSV)
- ✅ **Home Widget**: Quick access to upcoming tasks from home screen
- ✅ **Search & Filter**: Find pets and tasks quickly

---

## What to Test:

1. **Emergency Contacts Sync** ⭐ NEW
   - Add veterinarian and emergency contact info to a pet
   - Switch devices or reinstall app
   - Verify emergency contacts sync correctly

2. **Pet Deletion**
   - Delete a pet from the edit screen
   - Verify it's removed from both local storage and cloud
   - Change themes - deleted pets should stay deleted

3. **Core Functionality**
   - Google Sign-In authentication
   - Adding and editing pets
   - Creating and scheduling tasks
   - Receiving notification reminders
   - Cloud sync across devices
   - Dark theme toggle

---

## Known Issues:

- **Family Sharing**: UI is available but requires user lookup by email (coming in future update)

---

## Requirements:

- Android 8.0 (API 26) or higher
- Internet connection for cloud sync and Google Sign-In

---

## How to Report Issues:

Please report any bugs or issues you encounter. Your feedback helps improve the app!

---

## Shorter Version (For Firebase App Distribution):

**Version 1.1**

**Bug Fixes:**
- Fixed emergency contacts cloud sync
- Improved pet deletion functionality
- Enhanced UI button visibility

**What to Test:**
- Emergency contacts sync properly
- Pet deletion works correctly
- All core features function as expected

Report any issues you find!

