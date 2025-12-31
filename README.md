# Android Location Tracker

A minimal Android application that tracks device location every 30 seconds and sends data to a backend API.

## Tech Stack

### Mobile App
- **Language**: Kotlin
- **Framework**: Native Android (Jetpack)
- **Location API**: Google Play Services - FusedLocationProvider
- **Networking**: Retrofit + OkHttp
- **Background Execution**: Foreground Service
- **Local Storage**: Room Database
- **Minimum SDK**: Android 10 (API 29)
- **Target SDK**: Android 14 (API 34)

### Dependencies
- androidx.core:core-ktx:1.12.0
- com.google.android.gms:play-services-location:21.0.1
- androidx.room:room-runtime:2.6.1
- com.squareup.retrofit2:retrofit:2.9.0
- org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3

## Building APK on Linux

### Prerequisites
1. Install Android SDK and build tools
2. Set ANDROID_HOME environment variable
3. Install Java 8 or higher

### Build Steps

1. **Clone and navigate to project**
   ```bash
   cd /home/in-lt-89/dev/android-location-app
   ```

2. **Make gradlew executable**
   ```bash
   chmod +x gradlew
   ```

3. **Build debug APK**
   ```bash
   ./gradlew assembleDebug
   ```

4. **Build release APK (signed)**
   ```bash
   ./gradlew assembleRelease
   ```

5. **Locate APK files**
   - Debug: `app/build/outputs/apk/debug/app-debug.apk`
   - Release: `app/build/outputs/apk/release/app-release.apk`

### Transfer to Mobile

**Option 1: USB Transfer**
```bash
# Connect phone via USB, enable file transfer
cp app/build/outputs/apk/debug/app-debug.apk /path/to/phone/storage/
```

**Option 2: ADB Install**
```bash
# Enable USB debugging on phone
adb install app/build/outputs/apk/debug/app-debug.apk
```

**Option 3: Web Transfer**
```bash
# Start simple HTTP server
python3 -m http.server 8000
# Access from phone browser: http://your-linux-ip:8000
```

## Configuration

Update the API endpoint in `LocationRepository.kt`:
```kotlin
.baseUrl("https://180d1f2d9e34.ngrok-free.app/api/v1/")
```

## Features

- Tracks location every 30 seconds
- Runs in background via foreground service
- Offline storage with automatic sync
- Battery-efficient location tracking
- Automatic retry on network failures
