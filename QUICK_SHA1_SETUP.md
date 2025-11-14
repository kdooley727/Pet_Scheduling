# Quick Fix: Google Sign-In on New Computer

## The Problem
Your new computer has a different debug keystore SHA-1 fingerprint. Firebase needs this to allow Google Sign-In.

## Solution: Add Your New SHA-1 to Firebase

### Step 1: Get Your SHA-1 Fingerprint

**Option A: Using Android Studio (Easiest)**
1. Open Android Studio
2. Open Gradle panel (right side, or View → Tool Windows → Gradle)
3. Navigate: `Pet_Scheduling` → `app` → `Tasks` → `android` → `signingReport`
4. Double-click `signingReport`
5. In the Run panel, find the **SHA1** value (looks like: `XX:XX:XX:XX:...`)
6. Copy it

**Option B: Using Command Line**
Open PowerShell or Command Prompt and run:
```powershell
cd $env:USERPROFILE\.android
keytool -list -v -keystore debug.keystore -alias androiddebugkey -storepass android -keypass android
```
Look for the **SHA1** line and copy it.

### Step 2: Add SHA-1 to Firebase Console

1. Go to: https://console.firebase.google.com/
2. Select your project: **pet-scheduling**
3. Click ⚙️ **Settings** → **Project settings**
4. Scroll to **"Your apps"** section
5. Find your Android app (`com.hfad.pet_scheduling`)
6. Click **"Add fingerprint"**
7. Paste your SHA-1 fingerprint
8. Click **"Save"**

### Step 3: Download Updated google-services.json

1. Still in Firebase Console → Project settings
2. In **"Your apps"**, find your Android app
3. Click **"Download google-services.json"**
4. Replace the file at: `app/google-services.json` in your project

### Step 4: Rebuild and Test

1. In Android Studio: **Build → Clean Project**
2. Then: **Build → Rebuild Project**
3. Run the app and try Google Sign-In again

## ⚠️ Important Notes

- Wait **2-3 minutes** after adding SHA-1 for Firebase to update
- Make sure Google Sign-In is enabled: Firebase Console → Authentication → Sign-in method → Google → Enabled
- Your Web Client ID in `strings.xml` should be: `517221054105-77a8f2qt8cnv4t29osfbualbl66vqqgh.apps.googleusercontent.com`

## Still Not Working?

Check these:
- ✅ Google Sign-In enabled in Firebase Console
- ✅ SHA-1 added and saved in Firebase
- ✅ Updated `google-services.json` downloaded and replaced
- ✅ Waited a few minutes after adding SHA-1
- ✅ Rebuilt the app
- ✅ Web Client ID matches Firebase Console

