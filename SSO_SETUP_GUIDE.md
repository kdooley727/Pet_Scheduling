# SSO (Single Sign-On) Setup Guide

This guide will help you configure Google Sign-In and Apple Sign-In for your Pet Scheduling app.

## ✅ What's Already Implemented

The app now supports:
- ✅ **Google Sign-In** - Fully integrated with UI buttons
- ✅ **Apple Sign-In** - Backend support ready (requires additional setup)
- ✅ Email/Password authentication (already working)

## 🔧 Google Sign-In Setup

### Step 1: Enable Google Sign-In in Firebase Console

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project: **pet-scheduling**
3. Navigate to **Authentication** > **Sign-in method**
4. Click on **Google** provider
5. Enable the Google provider
6. Click **Save**

### Step 2: Get Your Web Client ID

1. In Firebase Console, go to **Authentication** > **Sign-in method** > **Google**
2. Under **Web SDK configuration**, you'll see a **Web client ID**
3. Copy this Web Client ID (it looks like: `123456789-abcdefghijklmnop.apps.googleusercontent.com`)

### Step 3: Add Web Client ID to Your App

1. Open `app/src/main/res/values/strings.xml`
2. Replace `YOUR_WEB_CLIENT_ID_HERE` with your actual Web Client ID:

```xml
<string name="default_web_client_id">123456789-abcdefghijklmnop.apps.googleusercontent.com</string>
```

### Step 4: Test Google Sign-In

1. Build and run the app
2. On the Login or Sign-Up screen, tap **"Continue with Google"**
3. Select your Google account
4. You should be automatically signed in and redirected to the Pet List screen

## 🍎 Apple Sign-In Setup (Optional)

Apple Sign-In is more complex and requires:
1. Apple Developer Account ($99/year)
2. App ID configuration in Apple Developer Portal
3. Service ID setup
4. Additional Firebase configuration

**Note:** Apple Sign-In is primarily for iOS apps, but can work on Android with proper setup.

### Basic Setup Steps:

1. **Firebase Console:**
   - Go to **Authentication** > **Sign-in method**
   - Enable **Apple** provider
   - Configure OAuth redirect URLs

2. **Apple Developer:**
   - Create a Service ID
   - Configure domains and redirect URLs
   - Generate a key for authentication

3. **Code Implementation:**
   - The `AuthViewModel.signInWithApple()` method is ready
   - You'll need to implement the Apple Sign-In UI flow
   - Use Apple's Sign in with Apple SDK

For detailed Apple Sign-In setup, see: [Firebase Apple Auth Documentation](https://firebase.google.com/docs/auth/ios/apple)

## 🎨 UI Features

The app now includes:
- **"Continue with Google"** button on Login screen
- **"Continue with Google"** button on Sign-Up screen
- Clean separation with "OR" divider
- Automatic navigation after successful SSO sign-in
- Error handling for failed sign-in attempts

## 🔍 Troubleshooting

### Google Sign-In Not Working

1. **Check Web Client ID:**
   - Verify the Web Client ID in `strings.xml` matches Firebase Console
   - Make sure there are no extra spaces or characters

2. **Verify Firebase Setup:**
   - Ensure Google provider is enabled in Firebase Console
   - Check that `google-services.json` is up to date

3. **Check SHA-1 Certificate:**
   - In Firebase Console, go to **Project Settings** > **Your apps**
   - Add your app's SHA-1 fingerprint
   - For debug: `keytool -list -v -keystore ~/.android/debug.keystore -alias androiddebugkey -storepass android -keypass android`

4. **Build Errors:**
   - Make sure `play-services-auth` dependency is synced
   - Clean and rebuild the project: `./gradlew clean build`

### Common Issues

- **"Google Sign-In not configured"**: Web Client ID is missing or incorrect
- **"Sign-in failed"**: Check Firebase Console for enabled providers
- **App crashes**: Verify all dependencies are included in `build.gradle.kts`

## 📝 Additional Providers

You can add more SSO providers by:

1. **Facebook:**
   - Add Facebook SDK dependency
   - Configure in Firebase Console
   - Implement similar helper class

2. **Twitter:**
   - Add Twitter SDK
   - Configure in Firebase Console
   - Implement authentication flow

3. **Microsoft:**
   - Add Microsoft Authentication Library
   - Configure Azure AD
   - Implement OAuth flow

## 📚 Resources

- [Firebase Authentication Documentation](https://firebase.google.com/docs/auth)
- [Google Sign-In for Android](https://developers.google.com/identity/sign-in/android)
- [Firebase Google Auth](https://firebase.google.com/docs/auth/android/google-signin)

## ✅ Next Steps

After setting up Google Sign-In:
1. Test the authentication flow
2. Verify user data is properly stored
3. Consider adding user profile display
4. Implement sign-out functionality (already in Pet List screen)

