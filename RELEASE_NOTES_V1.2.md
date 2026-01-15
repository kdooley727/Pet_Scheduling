# Release Notes - Version 1.2

## What's New

### New Features:
- 🐄 **Farm Animals Support** - Added farm animals to pet type dropdown:
  - Chicken, Cow, Goat, Sheep, Pig, Horse, Duck, Turkey, Donkey
  - Now you can schedule care for all your farm animals!

### Bug Fixes:
- ✅ **Fixed duplicate deletion issue** - Pets and tasks are now properly deleted from Firebase
  - Deleted items will no longer reappear after reinstalling the app
  - Deletions are now properly synced to cloud storage
- ✅ **Fixed test compilation errors** - Updated test files to match new ViewModel signatures

### Improvements:
- 🎨 **UI Improvements**:
  - Sign Out button is now smaller and better positioned (no longer overlaps with Add Pet button)
  - Added confirmation dialog for Sign Out to prevent accidental sign-outs
  - Improved spacing and visual hierarchy
- 🔄 **Better sync handling** - Improved error messages when cloud deletion fails

---

## Version History

### Version 1.1 Features:
- Emergency contacts sync
- Improved pet deletion
- Enhanced UI button visibility

### Version 1.0 Features:
- Pet Management with photos
- Task Scheduling and notifications
- Cloud Sync across devices
- Google Sign-In
- Dark Theme support
- Statistics and Export features
- Home Widget

---

## What to Test:

1. **Farm Animals** ⭐ NEW
   - Try adding pets with farm animal types (Chicken, Cow, Pig, etc.)
   - Verify they appear correctly in the pet list
   - Create tasks for farm animals

2. **Deletion Sync** ⭐ FIXED
   - Delete a pet or task
   - Verify it's removed from both local storage and Firebase
   - Reinstall the app - deleted items should NOT reappear
   - If deletion fails (offline), you'll see a warning message

3. **UI Improvements**
   - Check that Sign Out button doesn't overlap with Add Pet button
   - Try signing out - you should see a confirmation dialog
   - Verify all buttons are properly visible and accessible

4. **Core Functionality**
   - Google Sign-In authentication
   - Adding and editing pets (including farm animals)
   - Creating and scheduling tasks
   - Receiving notification reminders
   - Cloud sync across devices
   - Dark theme toggle

---

## Known Issues:

- None at this time

---

## Requirements:

- Android 8.0 (API 26) or higher
- Internet connection for cloud sync and Google Sign-In

---

## How to Report Issues:

Please report any bugs or issues you encounter. Your feedback helps improve the app!

---

## Shorter Version (For Firebase App Distribution):

**Version 1.2**

**New Features:**
- Added farm animals to pet types (Chicken, Cow, Goat, Sheep, Pig, Horse, Duck, Turkey, Donkey)

**Bug Fixes:**
- Fixed duplicate deletion issue - deleted pets/tasks no longer reappear after reinstall
- Fixed test compilation errors

**Improvements:**
- UI improvements (Sign Out button positioning, confirmation dialogs)
- Better sync error handling

**What to Test:**
- Try adding farm animals as pets
- Delete pets/tasks and verify they don't reappear after reinstall
- Check UI improvements (button positioning, dialogs)

Report any issues you find!

