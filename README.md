# Location Tracker

A professional Android application for tracking location data with real-time distance estimation and API synchronization.

## Features

- Real-time location tracking with background service
- Hybrid distance estimation with API synchronization
- Automatic location data syncing to backend
- Comprehensive logging and error tracking
- Persistent data storage with Room database

## Prerequisites

- Android Studio (latest version recommended)
- JDK 8 or higher
- Android SDK (API level 29 or higher)
- Gradle build system

## Building the Application

### Build Debug APK

Open your terminal/command prompt in the project root directory and run:

**Windows (PowerShell/CMD):**
```powershell
.\gradlew :app:assembleDebug
```

**Mac / Linux:**
```bash
./gradlew :app:assembleDebug
```

### Locate the Output

Once the build is successful (`BUILD SUCCESSFUL`), you can find your APK file at:
```
app/build/outputs/apk/debug/app-debug.apk
```

## Useful Commands

| Task | Purpose | Command |
|------|---------|---------|
| Clean Build | Remove build artifacts and rebuild | `.\gradlew clean :app:assembleDebug` |
| Run Unit Tests | Execute all unit tests | `.\gradlew test` |
| Install on Device | Build and install APK on connected device | `.\gradlew installDebug` |

## Troubleshooting

### Permission Issues (Mac/Linux)

If you encounter a "Permission Denied" error when running Gradle commands on Mac/Linux, grant execute permissions to the Gradle wrapper:

```bash
chmod +x gradlew
```

### Build Variants

If you add product flavors (e.g., Free and Pro versions), modify the build command accordingly:

```bash
.\gradlew :app:assembleFreeDebug
```

## Configuration

### API Endpoint

The application allows you to configure the backend API endpoint URL directly from the UI. The URL is stored in SharedPreferences and persists across app restarts.

### Permissions

The app requires the following permissions:
- `ACCESS_FINE_LOCATION` - For precise location tracking
- `ACCESS_COARSE_LOCATION` - For approximate location tracking
- `ACCESS_BACKGROUND_LOCATION` - For location tracking in background

## Version Information

The current app version is displayed at the bottom of the main screen. Version information is automatically retrieved from the app's package metadata.

## Technical Details

- **Minimum SDK**: 29 (Android 10)
- **Target SDK**: 33 (Android 13)
- **Architecture**: MVVM with ViewModel and StateFlow
- **Database**: Room Database for local storage
- **Networking**: Retrofit with OkHttp
- **Coroutines**: Kotlin Coroutines for asynchronous operations

## License

[Add your license information here]

## Support

For issues, questions, or contributions, please refer to the project's issue tracker or contact the development team.
