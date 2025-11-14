# How to Share Your Pet Scheduling App

## Option 1: Build and Share Debug APK (Quickest Method)

### Steps:
1. **Build the debug APK:**
   ```bash
   ./gradlew assembleDebug
   ```

2. **Find the APK:**
   - Location: `app/build/outputs/apk/debug/app-debug.apk`

3. **Share the APK:**
   - Upload to Google Drive, Dropbox, or email it
   - Or use a file sharing service like WeTransfer

4. **On the recipient's device:**
   - Enable "Install from Unknown Sources" in Settings
   - Download and install the APK

**Note:** Debug APKs are unsigned and Android may warn about installing from unknown sources.

---

## Option 2: Build Signed Release APK (Recommended)

### First-time Setup - Create a Keystore:

1. **Create a keystore file** (one-time setup):
   ```bash
   keytool -genkey -v -keystore pet-scheduling-key.jks -keyalg RSA -keysize 2048 -validity 10000 -alias pet-scheduling
   ```
   - Store this file securely! You'll need it for all future updates.
   - Remember the password you set.

2. **Create `keystore.properties` file** in the project root:
   ```
   storePassword=YOUR_STORE_PASSWORD
   keyPassword=YOUR_KEY_PASSWORD
   keyAlias=pet-scheduling
   storeFile=pet-scheduling-key.jks
   ```

3. **Add to `.gitignore`** (if not already):
   ```
   keystore.properties
   *.jks
   *.keystore
   ```

4. **Update `app/build.gradle.kts`** to add signing config (see below)

### Build Release APK:

1. **Build the release APK:**
   ```bash
   ./gradlew assembleRelease
   ```

2. **Find the APK:**
   - Location: `app/build/outputs/apk/release/app-release.apk`

3. **Share the APK** (same as Option 1)

---

## Option 3: Firebase App Distribution (Best for Testing)

Firebase App Distribution lets you share test builds easily:

1. **Set up Firebase App Distribution:**
   - Go to Firebase Console → App Distribution
   - Enable it for your project
   - Add testers by email

2. **Upload APK to Firebase:**
   ```bash
   ./gradlew assembleDebug
   # Then upload via Firebase Console
   ```

3. **Invite testers via email:**
   - They get a link to download and install
   - No need to enable "Unknown Sources"
   - Firebase automatically handles SHA-1 fingerprints

**Setup:** https://firebase.google.com/docs/app-distribution

**Note:** If you're not using App Distribution, you'll need to manually add each tester's SHA-1 fingerprint to Firebase Console. See `ADD_TESTER_DEVICE.md` for instructions.

---

## Option 4: Google Play Store Internal Testing (Best for Distribution)

1. **Create a Google Play Developer account** ($25 one-time fee)

2. **Upload APK to Play Console:**
   - Create an internal testing track
   - Upload your release APK
   - Add testers by email

3. **Testers install via Play Store:**
   - They get a Play Store link
   - Install like any other app

**Benefits:** Automatic updates, easy distribution, no "Unknown Sources" needed

---

## Security Notes:

- **Debug APKs:** Not recommended for production, easier to reverse-engineer
- **Release APKs:** Signed and more secure, recommended for sharing
- **Play Store:** Most secure and user-friendly option

---

## Quick Command Reference:

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK (requires signing setup)
./gradlew assembleRelease

# Find APK location
# Debug: app/build/outputs/apk/debug/app-debug.apk
# Release: app/build/outputs/apk/release/app-release.apk
```

