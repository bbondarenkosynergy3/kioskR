# Build Instructions for 360 Synergy Kiosk

## Quick Start Guide

This Android project is designed to be built using standard Android development tools. Since Replit doesn't natively support Android SDK and Gradle builds, follow one of these methods:

## Method 1: Download & Build Locally (Recommended)

### Prerequisites
- Android Studio (latest version)
- Android SDK 34
- JDK 8 or higher

### Steps
1. **Download the project**
   ```bash
   # Clone or download this repository
   git clone <your-repo-url>
   cd 360-synergy-kiosk
   ```

2. **Open in Android Studio**
   - Launch Android Studio
   - File → Open → Select project folder
   - Wait for Gradle sync to complete

3. **Build the APK**
   - For Debug: `Build → Build Bundle(s) / APK(s) → Build APK(s)`
   - For Release: `Build → Generate Signed Bundle / APK`
   
4. **Find your APK**
   - Debug: `app/build/outputs/apk/debug/app-debug.apk`
   - Release: `app/build/outputs/apk/release/app-release.apk`

## Method 2: Command Line Build (Advanced)

### Prerequisites
```bash
# Install Android SDK
export ANDROID_HOME=$HOME/Android/Sdk
export PATH=$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools
```

### Build Commands
```bash
# Make Gradle wrapper executable
chmod +x gradlew

# Build debug APK (for testing)
./gradlew assembleDebug

# Build release APK (optimized, smaller size)
./gradlew assembleRelease

# Clean build (if needed)
./gradlew clean
```

## Method 3: Codemagic CI/CD (Cloud Build)

### Setup
1. Sign up at https://codemagic.io
2. Connect your Git repository
3. Add this `codemagic.yaml` to your project root:

```yaml
workflows:
  android-kiosk-build:
    name: 360 Synergy Kiosk Build
    instance_type: mac_mini_m1
    max_build_duration: 60
    environment:
      android_signing:
        - kiosk_keystore
      groups:
        - google_credentials
      vars:
        PACKAGE_NAME: "net.synergy360.kiosk"
        GOOGLE_PLAY_TRACK: internal
      java: 11
    triggering:
      events:
        - push
      branch_patterns:
        - pattern: 'main'
          include: true
          source: true
    scripts:
      - name: Set up local.properties
        script: | 
          echo "sdk.dir=$ANDROID_SDK_ROOT" > "$CM_BUILD_DIR/local.properties"
      
      - name: Build Android APK
        script: | 
          ./gradlew assembleRelease
    
    artifacts:
      - app/build/outputs/**/*.apk
      - app/build/outputs/**/*.aab
    
    publishing:
      email:
        recipients:
          - your-email@example.com
        notify:
          success: true
          failure: true
```

### Build Process
1. Push code to repository
2. Codemagic automatically builds APK
3. Download from Codemagic dashboard or email

## Method 4: GitHub Actions (Alternative CI/CD)

Create `.github/workflows/build-apk.yml`:

```yaml
name: Build Android APK

on:
  push:
    branches: [ main ]
  pull_request:
    branches: [ main ]

jobs:
  build:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 11
      uses: actions/setup-java@v3
      with:
        java-version: '11'
        distribution: 'temurin'
    
    - name: Grant execute permission for gradlew
      run: chmod +x gradlew
    
    - name: Build with Gradle
      run: ./gradlew assembleRelease
    
    - name: Upload APK
      uses: actions/upload-artifact@v3
      with:
        name: app-release
        path: app/build/outputs/apk/release/app-release.apk
```

## Build Outputs

### Debug APK
- **Purpose**: Testing and development
- **Size**: Larger (~10-15 MB)
- **Location**: `app/build/outputs/apk/debug/app-debug.apk`
- **Optimization**: None
- **Signing**: Debug keystore

### Release APK
- **Purpose**: Production deployment
- **Size**: Smaller (~5-8 MB with ProGuard)
- **Location**: `app/build/outputs/apk/release/app-release.apk`
- **Optimization**: ProGuard enabled, code obfuscation
- **Signing**: Requires release keystore

## Signing the APK (for Release)

### Generate Keystore (First Time)
```bash
keytool -genkey -v -keystore kiosk-release.keystore \
  -alias kiosk -keyalg RSA -keysize 2048 -validity 10000
```

### Configure Signing in `app/build.gradle`
```gradle
android {
    signingConfigs {
        release {
            storeFile file("../kiosk-release.keystore")
            storePassword "your_keystore_password"
            keyAlias "kiosk"
            keyPassword "your_key_password"
        }
    }
    
    buildTypes {
        release {
            signingConfig signingConfigs.release
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}
```

### Build Signed APK
```bash
./gradlew assembleRelease
```

## Troubleshooting

### Gradle Sync Failed
```bash
# Clear Gradle cache
./gradlew clean
rm -rf .gradle
rm -rf app/build

# Re-sync
./gradlew build --refresh-dependencies
```

### SDK Not Found
```bash
# Set ANDROID_HOME
export ANDROID_HOME=$HOME/Android/Sdk
export PATH=$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools
```

### Build Failed: License Not Accepted
```bash
# Accept all SDK licenses
cd $ANDROID_HOME/tools/bin
./sdkmanager --licenses
```

### Out of Memory Error
```bash
# Increase Gradle memory in gradle.properties
org.gradle.jvmargs=-Xmx4096m -Dfile.encoding=UTF-8
```

## Testing the APK

### Install on Device
```bash
# Via ADB
adb install app/build/outputs/apk/release/app-release.apk

# Or drag & drop APK to device and install manually
```

### Verify Installation
```bash
# Check if app is installed
adb shell pm list packages | grep synergy360

# Launch app
adb shell am start -n net.synergy360.kiosk/.MainActivity
```

## Next Steps

After building the APK:
1. Install on target Android device (API 29+)
2. Set up as Device Owner for kiosk mode
3. Configure auto-start permissions
4. Test sleep/wake cycles
5. Verify admin exit gesture

See README.md for detailed installation and configuration instructions.
