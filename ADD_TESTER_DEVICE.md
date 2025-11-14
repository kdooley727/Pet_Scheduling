# How to Add a Tester's Device to Firebase

When someone else installs your app, they need their device's SHA-1 fingerprint added to Firebase for Google Sign-In to work.

## Step 1: Get the Tester's SHA-1 Fingerprint

The tester needs to get their SHA-1 fingerprint from their device/computer. Here are the methods:

### Method 1: Using Android Studio (Easiest for Tester)

**Have the tester do this:**

1. **Open Android Studio** on their computer
2. **Create a new project** (or open any Android project)
3. **Open the Gradle panel** (View → Tool Windows → Gradle)
4. **Navigate to**: `ProjectName` → `app` → `Tasks` → `android` → `signingReport`
5. **Double-click `signingReport`**
6. **Look at the Run panel** - find the SHA1 value:
   ```
   Variant: debug
   Config: debug
   Store: C:\Users\TheirName\.android\debug.keystore
   Alias: AndroidDebugKey
   SHA1: XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX:XX
   ```
7. **Copy the SHA1 value** (the long string with colons)
8. **Send it to you**

### Method 2: Using Command Line (If Tester Has Java Installed)

**Have the tester run this:**

#### Windows (PowerShell):
```powershell
cd $env:USERPROFILE\.android
keytool -list -v -keystore debug.keystore -alias androiddebugkey -storepass android -keypass android
```

#### Windows (Command Prompt):
```cmd
cd %USERPROFILE%\.android
keytool -list -v -keystore debug.keystore -alias androiddebugkey -storepass android -keypass android
```

#### Mac/Linux:
```bash
cd ~/.android
keytool -list -v -keystore debug.keystore -alias androiddebugkey -storepass android -keypass android
```

Look for the **SHA1** line in the output and copy it.

### Method 3: Using Gradle Command (If Tester Has Your Project)

**Have the tester run:**
```bash
cd /path/to/your/project
./gradlew signingReport
```

Then look for the SHA1 value in the output.

---

## Step 2: Add SHA-1 to Firebase Console

**You do this part:**

1. **Go to [Firebase Console](https://console.firebase.google.com/)**
2. **Select your project** (pet-scheduling)
3. **Click the gear icon** ⚙️ next to "Project Overview"
4. **Select "Project settings"**
5. **Scroll down to "Your apps"** section
6. **Find your Android app** (package name: `com.hfad.pet_scheduling`)
7. **Click "Add fingerprint"** button
8. **Paste the tester's SHA-1 fingerprint** (the one they sent you)
9. **Click "Save"**

**Important Notes:**
- ⚠️ **You don't need to download a new `google-services.json`** - Firebase handles this server-side
- ⚠️ **Wait 5-10 minutes** after adding the SHA-1 for Firebase to update
- ⚠️ **The tester may need to reinstall the app** after you add their SHA-1
- ⚠️ **Each tester needs their own SHA-1 added** if they're using different computers/devices

---

## Step 3: Tester Installs the App

**Have the tester:**

1. **Download the APK** you shared with them
2. **Enable "Install from Unknown Sources"** on their device:
   - Settings → Security → Unknown Sources (or Install Unknown Apps)
   - Enable for their file manager/browser
3. **Install the APK**
4. **Wait 5-10 minutes** after you added their SHA-1 (if you just added it)
5. **Open the app and try Google Sign-In**

---

## Troubleshooting

### Google Sign-In Still Not Working?

1. **Check Firebase Console:**
   - Go to Authentication → Sign-in method
   - Make sure "Google" is enabled
   - Check that the Web Client ID is configured

2. **Verify SHA-1 was added correctly:**
   - Go to Firebase Console → Project Settings → Your apps
   - Check that the SHA-1 fingerprint appears in the list
   - Make sure there are no typos

3. **Wait time:**
   - Firebase can take 5-10 minutes to update after adding SHA-1
   - Have the tester wait and try again

4. **Reinstall app:**
   - Have the tester uninstall and reinstall the app
   - Sometimes the app needs to be reinstalled after SHA-1 is added

5. **Check logs:**
   - Have the tester check Logcat for Firebase/Google Sign-In errors
   - Look for messages about SHA-1 mismatch

---

## Quick Checklist

- [ ] Tester gets their SHA-1 fingerprint
- [ ] Tester sends SHA-1 to you
- [ ] You add SHA-1 to Firebase Console
- [ ] Wait 5-10 minutes
- [ ] Tester installs/reinstalls the app
- [ ] Tester tries Google Sign-In
- [ ] If it doesn't work, check troubleshooting steps above

---

## Alternative: Use Firebase App Distribution

If you have multiple testers, consider using **Firebase App Distribution**:

1. **Set up Firebase App Distribution** in Firebase Console
2. **Upload your APK** to Firebase App Distribution
3. **Invite testers by email**
4. **They get a link** to download and install
5. **No need to manually add SHA-1** - Firebase handles it automatically

This is easier for managing multiple testers!

