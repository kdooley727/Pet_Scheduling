# How to Get SHA-1 Fingerprint for Google Sign-In

## Why You Need This
When you use a new computer, your debug keystore has a different SHA-1 fingerprint. Firebase needs this fingerprint to allow Google Sign-In to work.

## Method 1: Using Android Studio (Easiest)

1. **Open Android Studio**
2. **Open your Pet_Scheduling project**
3. **Open the Gradle panel** (usually on the right side, or View → Tool Windows → Gradle)
4. **Navigate to**: `Pet_Scheduling` → `app` → `Tasks` → `android` → `signingReport`
5. **Double-click `signingReport`**
6. **Look at the Run panel** at the bottom - you'll see output like:
   ```
   Variant: debug
   Config: debug
   Store: C:\Users\YourName\.android\debug.keystore
   Alias: AndroidDebugKey
   MD5: XX:XX:XX:...
   SHA1: XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX
   SHA-256: XX:XX:XX:...
   ```
7. **Copy the SHA1 value** (the long string with colons)

## Method 2: Using Command Line (if Java is installed)

### Windows (PowerShell):
```powershell
cd $env:USERPROFILE\.android
keytool -list -v -keystore debug.keystore -alias androiddebugkey -storepass android -keypass android
```

### Windows (Command Prompt):
```cmd
cd %USERPROFILE%\.android
keytool -list -v -keystore debug.keystore -alias androiddebugkey -storepass android -keypass android
```

Look for the **SHA1** line in the output.

## Method 3: Using Gradle Command

If you have Java/JDK installed and in your PATH:

```bash
cd D:\DevTools\AndroidStudioProjects\Pet_Scheduling
.\gradlew signingReport
```

Then look for the SHA1 value in the output.

---

## Adding SHA-1 to Firebase Console

1. **Go to [Firebase Console](https://console.firebase.google.com/)**
2. **Select your project** (pet-scheduling)
3. **Click the gear icon** ⚙️ next to "Project Overview"
4. **Select "Project settings"**
5. **Scroll down to "Your apps"** section
6. **Find your Android app** (or add it if it's not there)
7. **Click "Add fingerprint"** button
8. **Paste your SHA-1 fingerprint** (the one you copied above)
9. **Click "Save"**
10. **Download the updated `google-services.json`** file
11. **Replace the file** in your project at `app/google-services.json`

## Important Notes

- ⚠️ **You need to do this for BOTH debug and release keystores** if you plan to release the app
- ⚠️ **After adding the SHA-1, wait a few minutes** for Firebase to update
- ⚠️ **You may need to rebuild the app** after updating `google-services.json`
- ⚠️ **If you're using a release build**, you'll need the release keystore SHA-1 too

## Troubleshooting

- **Still not working?** Make sure:
  1. Google Sign-In is enabled in Firebase Console → Authentication → Sign-in method
  2. The Web Client ID in `strings.xml` matches Firebase Console
  3. You've waited a few minutes after adding the SHA-1
  4. You've rebuilt the app after updating `google-services.json`

