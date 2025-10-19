# 360 Synergy Kiosk - Android Project

## Project Overview

This is an Android kiosk application built in Kotlin that creates a locked-down, fullscreen WebView displaying https://app.360synergy.net. The app is designed to run on Android tablets/devices in kiosk mode with automatic sleep/wake cycles.

## Purpose

Create a secure, unattended kiosk application for 360 Synergy that:
- Displays the web application in fullscreen without system UI
- Prevents users from exiting or accessing other apps
- Automatically manages power through sleep/wake cycles
- Provides admin-only exit mechanism via secret gesture
- Auto-starts on device boot

## Technical Architecture

### Core Components

1. **MainActivity.kt**
   - Main entry point and WebView container
   - Handles fullscreen setup and kiosk lock mode
   - Manages network connectivity and offline detection
   - Coordinates sleep/wake manager and gesture detector

2. **SleepWakeManager.kt**
   - Manages automatic sleep/wake cycles (2-minute intervals)
   - Controls WakeLock acquisition and release
   - Handles WebView pause/resume during sleep cycles
   - Resets timer on user interaction

3. **CornerTapDetector.kt**
   - Implements secret 4-corner tap gesture detection
   - Validates tap sequence: top-left → top-right → bottom-left → bottom-right
   - 10-second timeout for sequence completion
   - Triggers exit dialog on successful sequence

4. **BootReceiver.kt**
   - BroadcastReceiver for BOOT_COMPLETED events
   - Automatically launches MainActivity on device startup
   - Ensures kiosk mode persists after reboot

### Key Features Implementation

#### Fullscreen Mode
- Uses `SYSTEM_UI_FLAG_FULLSCREEN` and `SYSTEM_UI_FLAG_IMMERSIVE_STICKY`
- Hides status bar, navigation bar, and system UI
- Window flags: `FLAG_KEEP_SCREEN_ON`, `FLAG_SHOW_WHEN_LOCKED`, `FLAG_TURN_SCREEN_ON`

#### Kiosk Lock
- `startLockTask()` blocks Home, Recent Apps, and Back buttons
- Requires Device Owner setup: `adb shell dpm set-device-owner`
- Exit only via secret gesture sequence

#### Sleep/Wake Cycle
- 120-second (2-minute) interval
- Sleep: releases WakeLock, pauses WebView
- Wake: acquires WakeLock, resumes WebView, reloads page
- User touch resets the timer

#### Offline Handling
- Monitors network state with ConnectivityManager
- Shows "Reconnecting..." overlay when offline
- Auto-retries connection every 5 seconds
- Reloads WebView when connection restored

## Build Configuration

- **Min SDK**: 29 (Android 10)
- **Target SDK**: 34 (Android 14)
- **Language**: Kotlin 1.9.0
- **Build Tool**: Gradle 8.0
- **Optimize**: ProGuard enabled for release builds

## Project Structure

```
app/src/main/
├── java/net/synergy360/kiosk/
│   ├── MainActivity.kt          # Main WebView activity
│   ├── SleepWakeManager.kt      # Sleep/wake cycle logic
│   ├── CornerTapDetector.kt     # 4-corner gesture detector
│   └── BootReceiver.kt          # Boot auto-start receiver
├── res/
│   ├── layout/
│   │   └── activity_main.xml    # WebView + offline layout
│   └── values/
│       └── strings.xml          # App strings
└── AndroidManifest.xml          # Permissions & components
```

## Required Permissions

- `INTERNET` - Load web content
- `WAKE_LOCK` - Control screen power
- `RECEIVE_BOOT_COMPLETED` - Auto-start on boot
- `ACCESS_NETWORK_STATE` - Detect connectivity
- `DISABLE_KEYGUARD` - Wake without keyguard

## Building the APK

⚠️ **Important**: This project cannot be built directly on Replit because Android SDK and Gradle builds require specific Android development tools not available in this environment.

### Build Options:

1. **Local Build** - Download and open in Android Studio
2. **Command Line** - Use `./gradlew assembleRelease` with Android SDK
3. **CI/CD** - Use Codemagic (config included in `codemagic.yaml`)

See `BUILD_INSTRUCTIONS.md` for detailed steps.

## Installation & Setup

1. Build APK using one of the methods above
2. Set device as Device Owner: `adb shell dpm set-device-owner net.synergy360.kiosk/.DeviceAdminReceiver`
3. Install APK: `adb install app-release.apk`
4. Grant permissions via Settings
5. Enable auto-start for device manufacturer
6. Reboot device - app starts automatically

## Admin Exit Sequence

To exit kiosk mode:
1. Tap top-left corner
2. Tap top-right corner
3. Tap bottom-left corner
4. Tap bottom-right corner
5. Confirm "Exit Kiosk Mode?" dialog

## Customization Options

### Change Web URL
```kotlin
// MainActivity.kt, line ~28
private val webUrl = "https://your-url.com"
```

### Change Sleep Interval
```kotlin
// SleepWakeManager.kt, line ~15
private val sleepInterval = 180000L // 3 minutes
```

### Change Exit Gesture
```kotlin
// CornerTapDetector.kt, line ~11-16
private val requiredSequence = listOf(
    Corner.TOP_LEFT,
    Corner.BOTTOM_RIGHT,
    // ... customize sequence
)
```

## Recent Changes

**2025-10-19** - Initial project creation
- Set up Android project structure with Kotlin
- Implemented WebView with fullscreen kiosk mode
- Added 2-minute sleep/wake cycle automation
- Implemented 4-corner tap gesture for admin exit
- Added boot receiver for auto-start
- Created offline detection and reconnection
- Added Codemagic CI/CD configuration
- Created comprehensive documentation

## User Preferences

- Target: Android tablets for kiosk deployment
- Priority: Stability and security over features
- Build Method: Codemagic CI/CD preferred
- Language: Russian and English documentation

## Known Limitations

- Requires Device Owner mode (factory reset needed)
- Manufacturer-specific battery optimization settings required
- Cannot be built on Replit (requires Android SDK)
- Needs physical device or emulator for testing

## Next Steps (Future Enhancements)

- Add remote configuration for URL and sleep interval
- Implement device management dashboard
- Add usage analytics and health monitoring
- Create OTA update mechanism
- Add multi-language support in UI
- Implement crash reporting and diagnostics

## Support

For build issues, see BUILD_INSTRUCTIONS.md
For usage issues, see README.md
For customization, see source code comments
