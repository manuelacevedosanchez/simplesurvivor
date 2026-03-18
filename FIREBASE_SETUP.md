# Firebase Setup Instructions

## Step 1: Create Firebase Project

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Click "Add Project" and follow the wizard
3. Enable Google Analytics when prompted (recommended)

## Step 2: Register Your Android App

1. In Firebase Console, click "Add app" → Android icon
2. Enter your package name: `es.masmultimedia`
3. Enter app nickname: `SimpleSurvivor`
4. (Optional) Enter SHA-1 signing certificate fingerprint
   - Get it with: `./gradlew signingReport`
5. Click "Register app"

## Step 3: Download Configuration File

1. Download `google-services.json`
2. Place it in: `android/google-services.json`
3. **Important:** Add to `.gitignore` if it contains sensitive data

## Step 4: Enable Crashlytics

1. In Firebase Console, go to Crashlytics
2. Click "Enable Crashlytics"
3. Follow the setup wizard

## Step 5: Uncomment Gradle Configuration

In `android/build.gradle`, uncomment these lines:

```groovy
// At the top of the file:
apply plugin: 'com.google.gms.google-services'
apply plugin: 'com.google.firebase.crashlytics'

// In dependencies block:
implementation platform('com.google.firebase:firebase-bom:32.7.0')
implementation 'com.google.firebase:firebase-crashlytics'
implementation 'com.google.firebase:firebase-analytics'
```

## Step 6: Test Crashlytics

Build and run the app. In Firebase Console → Crashlytics, you should see your app after a few minutes.

To force a test crash (ONLY in debug builds):
```kotlin
throw RuntimeException("Test Crash")
```

## ProGuard/R8 Configuration

The `proguard-rules.pro` already contains necessary keep rules. If you encounter issues, add:

```proguard
-keepattributes SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception
```

## Troubleshooting

- **Build fails:** Make sure `google-services.json` is in the correct location
- **Crashes not appearing:** Wait 5-10 minutes, then check dashboard
- **Duplicate class errors:** Check Firebase BOM version compatibility

