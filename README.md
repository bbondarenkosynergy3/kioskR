# 360 Synergy Kiosk - Android APK

Android kiosk application that displays https://app.360synergy.net in a locked fullscreen WebView with automatic sleep/wake cycles.

## Features

✅ **Fullscreen WebView** - Displays https://app.360synergy.net without system UI  
✅ **Kiosk Lock Mode** - Blocks Home, Back, and Recent buttons  
✅ **Sleep/Wake Cycle** - Automatically sleeps every 2 minutes and wakes up  
✅ **Secret Admin Exit** - Tap 4 corners (top-left → top-right → bottom-left → bottom-right) to exit  
✅ **Auto-start on Boot** - Launches automatically when device restarts  
✅ **Offline Detection** - Shows reconnection screen when network is unavailable  

## Technical Specifications

| Parameter | Value |
|-----------|-------|
| Target Android | 10+ (API 29+) |
| Language | Kotlin |
| Target SDK | 34 |
| Sleep Interval | 2 minutes (120,000 ms) |
| WebView URL | https://app.360synergy.net |

## Project Structure

```
360-synergy-kiosk/
├── app/
│   ├── src/main/
│   │   ├── java/net/synergy360/kiosk/
│   │   │   ├── MainActivity.kt           # Main activity with WebView & kiosk logic
│   │   │   ├── SleepWakeManager.kt       # Sleep/wake cycle management
│   │   │   ├── CornerTapDetector.kt      # 4-corner tap gesture detector
│   │   │   └── BootReceiver.kt           # Auto-start on boot receiver
│   │   ├── res/
│   │   │   ├── layout/
│   │   │   │   └── activity_main.xml     # Main layout with WebView
│   │   │   └── values/
│   │   │       └── strings.xml           # App strings
│   │   └── AndroidManifest.xml           # App manifest with permissions
│   ├── build.gradle                       # App-level build configuration
│   └── proguard-rules.pro                # ProGuard rules for release build
├── build.gradle                           # Project-level build configuration
├── settings.gradle                        # Gradle settings
└── gradle.properties                      # Gradle properties
```

## Building the APK

### Option 1: Build Locally with Android Studio

1. **Install Android Studio**
   - Download from https://developer.android.com/studio
   - Install Android SDK 34

2. **Open Project**
   ```bash
   # Open this folder in Android Studio
   File → Open → Select this project directory
   ```

3. **Build APK**
   ```bash
   # In Android Studio
   Build → Build Bundle(s) / APK(s) → Build APK(s)
   
   # Or via command line (if you have Gradle installed)
   ./gradlew assembleRelease
   ```

4. **Find APK**
   ```
   app/build/outputs/apk/release/app-release.apk
   ```

### Option 2: Build with Gradle Command Line

```bash
# Make sure you have Android SDK and Gradle installed
export ANDROID_HOME=/path/to/android/sdk

# Build debug APK (for testing)
./gradlew assembleDebug

# Build release APK (optimized)
./gradlew assembleRelease

# Install to connected device
./gradlew installDebug
```

### Option 3: Build with Codemagic CI/CD

1. **Connect to Codemagic**
   - Sign up at https://codemagic.io
   - Connect your Git repository

2. **Configure Build**
   - Create `codemagic.yaml` in project root (see example below)
   - Push to repository
   - Codemagic will automatically build APK

3. **Example codemagic.yaml**:
   ```yaml
   workflows:
     android-kiosk:
       name: Android Kiosk Build
       instance_type: mac_mini_m1
       max_build_duration: 60
       environment:
         android_signing:
           - keystore_reference
         groups:
           - android_credentials
         vars:
           PACKAGE_NAME: "net.synergy360.kiosk"
       scripts:
         - name: Build Android release
           script: |
             ./gradlew assembleRelease
       artifacts:
         - app/build/outputs/**/*.apk
   ```

## Installation & Setup

### 1. Enable Device Owner Mode (Required for Kiosk Lock)

For `startLockTask()` to work, the app must be set as Device Owner:

```bash
# Connect device via USB with ADB enabled
adb shell dpm set-device-owner net.synergy360.kiosk/.DeviceAdminReceiver
```

**Note**: Device must be factory reset and have no accounts added for this to work.

### 2. Install APK

```bash
adb install app-release.apk
```

### 3. Grant Permissions

Go to Settings → Apps → 360 Synergy Kiosk → Permissions:
- ✅ Internet
- ✅ Display over other apps
- ✅ Modify system settings

### 4. Enable Auto-start

Some manufacturers require additional settings:
- **Samsung**: Settings → Apps → 360 Synergy Kiosk → Battery → Optimize → No restrictions
- **Xiaomi**: Settings → Apps → Permissions → Autostart → Enable
- **Huawei**: Settings → Battery → App launch → 360 Synergy Kiosk → Manage manually

## Usage

### Normal Operation
- App launches automatically on boot
- Displays https://app.360synergy.net in fullscreen
- Sleeps every 2 minutes, wakes automatically
- Touch screen to reset sleep timer

### Admin Exit Sequence
1. Tap **top-left corner**
2. Tap **top-right corner**  
3. Tap **bottom-left corner**
4. Tap **bottom-right corner**
5. Confirm exit dialog

## Permissions Explained

| Permission | Purpose |
|------------|---------|
| `INTERNET` | Load web content from https://app.360synergy.net |
| `WAKE_LOCK` | Control screen on/off during sleep cycles |
| `RECEIVE_BOOT_COMPLETED` | Auto-start app when device boots |
| `ACCESS_NETWORK_STATE` | Detect network connectivity for offline handling |
| `DISABLE_KEYGUARD` | Wake device from sleep without keyguard |

## Troubleshooting

### App doesn't start on boot
- Check auto-start settings for your device manufacturer
- Ensure battery optimization is disabled
- Verify RECEIVE_BOOT_COMPLETED permission

### Can't enable kiosk lock mode
- Device must be set as Device Owner (see Installation step 1)
- Factory reset device if needed
- Run `adb shell dpm set-device-owner` command

### Screen doesn't wake after sleep
- Check WAKE_LOCK permission is granted
- Ensure battery optimization is disabled
- Check device power saving settings

### WebView shows blank page
- Verify internet connection
- Check that https://app.360synergy.net is accessible
- Clear app cache: Settings → Apps → 360 Synergy Kiosk → Storage → Clear Cache

## Customization

To modify the app behavior, edit these files:

**Change URL:**
```kotlin
// In MainActivity.kt
private val webUrl = "https://your-custom-url.com"
```

**Change Sleep Interval:**
```kotlin
// In SleepWakeManager.kt
private val sleepInterval = 180000L // 3 minutes
```

**Change Exit Gesture:**
```kotlin
// In CornerTapDetector.kt
private val requiredSequence = listOf(
    Corner.TOP_LEFT,
    Corner.BOTTOM_RIGHT,
    Corner.TOP_RIGHT,
    Corner.BOTTOM_LEFT
)
```

## License

This is a custom kiosk application for 360 Synergy.

## Support

For issues or questions, contact the development team.
