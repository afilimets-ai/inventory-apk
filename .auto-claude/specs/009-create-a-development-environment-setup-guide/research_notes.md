# Android Development Environment Research Notes

**Research Date:** March 24, 2026
**Purpose:** Document current Android SDK requirements and recommended versions for development environment setup guide

---

## 1. Android SDK API Levels

### Current Requirements (2026)

**Google Play Target API Requirements:**
- **New apps and app updates:** Must target Android 15 (API level 35) or higher
- **Special platforms:** Wear OS, Android Automotive OS, and Android TV apps must target Android 14 (API level 34) or higher
- **Existing apps:** Must target Android 14 (API level 34) or higher to remain available to new users

**Recommended API Levels for 2026:**
- **Target SDK:** API level 35 (Android 15)
- **Minimum SDK:** API level 24 (Android 7.0) for broad device compatibility
- **Compile SDK:** API level 35 or latest available

### Future SDK Release Plans
- Google plans for one annual requirement each year tied to the major API level only
- More frequent SDK releases with two releases planned in 2025
- Q2: Major release with new developer APIs
- Q4: Minor release with new developer APIs

### Key API Level Information
- Android will continue quarterly SDK releases but target API requirements remain annual
- API level 36.1 is already supported by Android Gradle Plugin 9.1 and 9.2
- Developers should stay current with the latest stable API level for new projects

**Sources:**
- [SDK Platform release notes](https://developer.android.com/tools/releases/platforms)
- [API Levels](https://apilevels.com/)
- [Target API level requirements for Google Play apps](https://support.google.com/googleplay/android-developer/answer/11926878?hl=en)
- [Meet Google Play's target API level requirement](https://developer.android.com/google/play/requirements/target-sdk)

---

## 2. Java and Kotlin Version Requirements

### Java Versions

**Current Recommendations (2026):**
- **Source Compatibility:** Java 17
- **Target Compatibility:** Java 17
- **JVM Target:** '17'

**Java Version Support:**
- Android Studio 3.0+ supports Java 8 language features
- Android Studio supports using Java 11+ APIs without requiring a minimum API level
- Java 17 is the current standard for Android development in 2026
- Gradle 9.4.1 now supports Java 26 (latest)

**Configuration:**
```groovy
android {
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }
}
```

### Kotlin Versions

**Current Recommendations (2026):**
- **Kotlin Version:** 2.3.20 or higher (latest stable as of 2026)
- **Kotlin Gradle Plugin (KGP):** 2.2.10 or higher
- **Built-in Kotlin:** Enabled by default in Android Gradle Plugin 9.0+

**Key Kotlin Features:**
- Android Gradle Plugin 9.0 includes built-in Kotlin support
- No longer need to apply `org.jetbrains.kotlin.android` plugin separately for Android apps
- Kotlin 2.x includes multiple release types: language releases, tooling releases, and bug fix releases
- Android Studio provides full code completion for Kotlin

**JVM Target Configuration:**
```kotlin
kotlinOptions {
    jvmTarget = '17'
}
```

**Sources:**
- [Java versions in Android builds](https://developer.android.com/build/jdks)
- [Android Studio Panda 2](https://developer.android.com/studio/releases)
- [AGP, D8, and R8 versions required for Kotlin versions](https://developer.android.com/build/kotlin-support)
- [Kotlin release process](https://kotlinlang.org/docs/releases.html)
- [Update your Kotlin projects for Android Gradle Plugin 9.0](https://blog.jetbrains.com/kotlin/2026/01/update-your-projects-for-agp9/)

---

## 3. Gradle Version Requirements

### Android Gradle Plugin (AGP)

**Latest Versions (2026):**
- **AGP 9.1.0:** Released March 2026
- **AGP 9.2.0:** Preview/Alpha available
- **AGP 9.0.1:** Stable, released January 2026

**Version Support:**
- AGP 9.0: Supports maximum API level 36.1
- AGP 9.1: Supports maximum API level 36.1
- AGP 9.2: Supports maximum API level 36.1

**Key Features in AGP 9.0:**
- Built-in Kotlin support enabled by default
- Requires Kotlin Gradle Plugin (KGP) 2.2.10 or higher
- No longer requires separate application of `org.jetbrains.kotlin.android` plugin
- Opt-out features will be removed in AGP 10.0 (mid-2026)

### Gradle Versions

**Latest Stable (2026):**
- **Gradle 9.4.1:** Released March 19, 2026 (first patch release for 9.4.0)
- **Gradle 9.x:** Tested with Android Gradle Plugin versions 8.13 through 9.1.0-alpha04

**Compatibility:**
- Gradle 9.4.1 supports Java 26
- For best performance, use the latest possible version of both Gradle and AGP
- Gradle is continuously tested with the latest AGP versions

**Recommended Configuration:**
```gradle
// Project-level build.gradle
plugins {
    id 'com.android.application' version '9.1.0' apply false
}

// gradle-wrapper.properties
distributionUrl=https\://services.gradle.org/distributions/gradle-9.4.1-all.zip
```

**Important Migration Notes:**
- Migration to AGP 9.0 features is becoming increasingly important
- AGP 10.0 (mid-2026) will remove opt-out capabilities for AGP 9.0 features
- Always update to the latest stable versions for security and performance

**Sources:**
- [Compatibility Matrix](https://docs.gradle.org/current/userguide/compatibility.html)
- [About Android Gradle plugin](https://developer.android.com/build/releases/about-agp)
- [Android Gradle plugin 9.0.1 (January 2026)](https://developer.android.com/build/releases/agp-9-0-0-release-notes)
- [Android Gradle plugin 9.1.0 (March 2026)](https://developer.android.com/build/releases/agp-9-1-0-release-notes)
- [Gradle Releases](https://gradle.org/releases/)
- [Gradle 9.4.1 Release Notes](https://docs.gradle.org/current/release-notes.html)

---

## 4. Android Studio Requirements

### Latest Version (2026)

**Current Stable Release:**
- **Android Studio Panda 2** (2025.3.2)
- Released March 2026
- Codename: Panda
- Version: 2025.3.2

### System Requirements

#### Minimum Hardware Requirements
- **RAM:** 8GB minimum (official requirement)
- **Disk Space:** 8GB minimum for IDE + Android SDK + Android Emulator system image
- **Screen Resolution:** 1280 x 800 minimum

#### Recommended Hardware Requirements
- **RAM:** 16GB minimum for practical development, 32GB recommended
  - 8GB can cause performance slowdowns with emulators
  - Android Emulator alone consumes ~4GB RAM
  - With 8GB, only 4GB remains for IDE, OS, and browser
- **Disk Space:** 20GB+ recommended
  - IDE installation: ~3GB
  - Android SDK: ~5GB
  - Emulator system images: ~2GB each
  - Gradle cache and build artifacts: 5-10GB+
- **CPU:** Multi-core processor (4+ cores recommended)
- **SSD:** Solid State Drive strongly recommended for build performance

#### Operating System Requirements
- **Windows:** Windows 10/11 (64-bit)
- **macOS:** macOS 11.0 (Big Sur) or higher
- **Linux:** GNOME or KDE desktop
  - Tested on Ubuntu 20.04 LTS or higher
  - 64-bit distribution capable of running 32-bit applications

### 2026 AI Features Requirements
- **Gemini Agent Mode:** Requires adequate RAM (16GB min, 32GB recommended)
- **Journeys Testing:** Storage-efficient but benefits from higher RAM
- AI features remain storage-efficient but require adequate memory for optimal performance

### IDE Features
- **Built-in Languages:** Kotlin, Java, C/C++ with full code completion
- **Emulator:** Built-in Android Emulator for testing (hardware acceleration recommended)
- **SDK Manager:** Integrated SDK management
- **AVD Manager:** Integrated Android Virtual Device management
- **Version Control:** Git, GitHub, SVN integration
- **Build System:** Gradle with built-in Kotlin support (AGP 9.0+)

### Download Size
- **IDE Download:** ~1.5-2GB
- **Full SDK Download:** ~5-8GB
- **Total First-Time Setup:** ~10-15GB download

**Sources:**
- [How Much RAM Does Android Studio Really Need? [2026 Expert Guide]](https://techozea.com/minimum-ram-android-studio/)
- [Android Studio: Definition, Features, Installation, System Requirements](https://www.intelivita.com/blog/android-studio/)
- [Download Android Studio & App Tools](https://developer.android.com/studio)
- [Android Studio Release Updates: Android Studio Panda 2](https://androidstudio.googleblog.com/2026/03/android-studio-panda-2-202532-now.html)
- [What are the system requirements for Android Studio?](https://clouddevs.com/android/system-requirements/)

---

## 5. IDE Setup Requirements and Configuration

### Android Studio Installation Steps

#### Windows Installation
1. **Download Android Studio:**
   - Visit [developer.android.com/studio](https://developer.android.com/studio)
   - Download Android Studio Panda 2 (2025.3.2) or latest stable
   - File size: ~1.5-2GB

2. **Run the Installer:**
   - Double-click the `.exe` file
   - Follow the Setup Wizard
   - Choose installation location (default: `C:\Program Files\Android\Android Studio`)
   - Select components to install:
     - Android Studio IDE
     - Android SDK
     - Android Virtual Device (AVD)
     - Performance (Intel® HAXM) - for emulator acceleration

3. **Initial Configuration:**
   - Launch Android Studio
   - Choose "Standard" installation type (recommended for most users)
   - Select UI theme (Light or Dark)
   - Verify settings and download required components
   - Download size: ~5-8GB for complete SDK

4. **Post-Installation:**
   - Configure hardware acceleration (Intel HAXM or AMD Hypervisor)
   - Set up environment variables (optional but recommended):
     - `ANDROID_HOME`: Path to Android SDK (e.g., `C:\Users\<username>\AppData\Local\Android\Sdk`)
     - Add to PATH: `%ANDROID_HOME%\platform-tools` and `%ANDROID_HOME%\tools`

#### macOS Installation
1. **Download Android Studio:**
   - Visit [developer.android.com/studio](https://developer.android.com/studio)
   - Download Android Studio for Mac (Intel or Apple Silicon)
   - File format: `.dmg` (disk image)

2. **Install:**
   - Open the `.dmg` file
   - Drag Android Studio to Applications folder
   - Launch from Applications

3. **Initial Setup:**
   - Follow Setup Wizard (same as Windows)
   - macOS may require granting permissions for the IDE
   - Hardware acceleration is built-in (no HAXM needed)

4. **Environment Variables (optional):**
   - Add to `~/.bash_profile`, `~/.zshrc`, or `~/.zprofile`:
     ```bash
     export ANDROID_HOME=$HOME/Library/Android/sdk
     export PATH=$PATH:$ANDROID_HOME/platform-tools
     export PATH=$PATH:$ANDROID_HOME/tools
     ```

#### Linux Installation (Ubuntu/Debian)
1. **Prerequisites:**
   ```bash
   sudo apt update
   sudo apt install libc6:i386 libncurses5:i386 libstdc++6:i386 lib32z1 libbz2-1.0:i386
   ```

2. **Download and Extract:**
   - Download Android Studio `.tar.gz` from [developer.android.com/studio](https://developer.android.com/studio)
   - Extract to `/opt/` or home directory:
     ```bash
     sudo tar -xvzf android-studio-*.tar.gz -C /opt/
     ```

3. **Launch:**
   ```bash
   cd /opt/android-studio/bin
   ./studio.sh
   ```

4. **Create Desktop Entry (optional):**
   - Android Studio > Tools > Create Desktop Entry

5. **Environment Variables:**
   - Add to `~/.bashrc` or `~/.profile`:
     ```bash
     export ANDROID_HOME=$HOME/Android/Sdk
     export PATH=$PATH:$ANDROID_HOME/platform-tools
     export PATH=$PATH:$ANDROID_HOME/tools
     ```

### SDK Manager Setup and Usage

#### Accessing SDK Manager
- **From Welcome Screen:** Configure > SDK Manager
- **From Project:** Tools > SDK Manager
- **Shortcut:** Settings/Preferences > Appearance & Behavior > System Settings > Android SDK

#### SDK Platforms Tab

**Essential SDK Platforms to Install:**
1. **Android 15.0 (API 35) - Latest**
   - Android SDK Platform 35
   - Required for targeting latest API level
   - Google Play requirements compliance

2. **Android 7.0 (API 24) - Minimum SDK**
   - For testing minimum supported version
   - Provides ~95% device coverage

3. **Recommended Additional Platforms:**
   - Android 14.0 (API 34) - for testing previous major version
   - Android 13.0 (API 33) - for broader compatibility testing

**Platform Components:**
- **SDK Platform:** Core libraries for each Android version
- **Sources for Android:** Source code for Android framework (helpful for debugging)
- **Google APIs:** Google-specific APIs (Maps, Play Services)

#### SDK Tools Tab

**Essential SDK Tools (2026):**
1. **Android SDK Build-Tools:**
   - Latest version (35.x.x or higher)
   - Used for building Android apps
   - Update regularly for new features and bug fixes

2. **Android Emulator:**
   - Latest version (33.x.x or higher)
   - Required for running virtual devices
   - Includes performance improvements

3. **Android SDK Platform-Tools:**
   - Latest version (35.x.x or higher)
   - Includes ADB (Android Debug Bridge), fastboot
   - Essential for device communication

4. **Android SDK Tools:**
   - Basic tools for SDK management
   - Included by default

5. **Intel x86 Emulator Accelerator (HAXM) - Windows/Linux Intel only:**
   - Version 7.8.0 or higher
   - Required for fast emulator performance on Intel CPUs
   - macOS uses built-in Hypervisor Framework
   - AMD users should enable AMD Hypervisor

6. **Google Play Services:**
   - Latest version
   - Required for Google Play-dependent features
   - Includes Play Store in emulator

7. **NDK (Native Development Kit) - Optional:**
   - Latest version (r27 or higher)
   - Only if developing with C/C++
   - Large download (~1-2GB)

8. **CMake - Optional:**
   - Latest version (3.28.x or higher)
   - Required for native code builds
   - Only needed with NDK

#### SDK Manager Configuration

**Update Sites:**
- SDK Manager checks for updates from Google's repositories
- Default update sites are pre-configured
- Check for updates regularly: Help > Check for Updates

**Proxy Settings (if behind corporate firewall):**
- Settings > Appearance & Behavior > System Settings > HTTP Proxy
- Configure proxy server and port
- Test connection before downloading

**SDK Location:**
- Default locations:
  - **Windows:** `C:\Users\<username>\AppData\Local\Android\Sdk`
  - **macOS:** `~/Library/Android/sdk`
  - **Linux:** `~/Android/Sdk`
- Can be changed in SDK Manager settings
- Should be on SSD for best performance

### Emulator Configuration and Setup

#### Creating an Android Virtual Device (AVD)

**Accessing AVD Manager:**
- **From Welcome Screen:** More Actions > Virtual Device Manager
- **From Project:** Tools > Device Manager
- **Toolbar:** Device Manager icon

**Step-by-Step AVD Creation:**

1. **Click "Create Device"**

2. **Select Hardware:**
   - **Recommended devices:**
     - Pixel 7 Pro (6.7", 1440x3120, 512 DPI) - flagship testing
     - Pixel 6 (6.4", 1080x2400, 420 DPI) - popular device
     - Medium Phone (5.4", 1080x2340) - common size
     - Pixel Tablet (10.95", 2560x1600) - tablet testing
   - **Category:** Phone, Tablet, Wear OS, TV, Automotive
   - **Play Store:** Choose devices with Play Store icon for Google Play testing
   - Click "Next"

3. **Select System Image:**
   - **Recommended (2026):**
     - **API Level 35 (Android 15.0)** - Latest
     - **Target:** x86_64 or ARM64 (depending on host CPU)
     - **Variant:**
       - Google APIs - includes Google Play Services
       - Google Play - includes Play Store (recommended for most apps)
   - **ABI Selection:**
     - **x86_64:** Best for Intel/AMD CPUs with HAXM (fastest)
     - **ARM64:** Required for Apple Silicon Macs, slower on Intel
   - Download system image if not already installed (~2GB each)
   - Click "Next"

4. **Configure AVD:**
   - **AVD Name:** Descriptive name (e.g., "Pixel_7_Pro_API_35")
   - **Startup Orientation:** Portrait or Landscape
   - **Advanced Settings:**
     - **RAM:** 2GB-4GB (depends on host RAM)
       - 2GB: Minimum for basic testing
       - 4GB: Recommended for smooth performance
       - Don't exceed 50% of host RAM
     - **VM Heap:** 256MB-512MB (app heap size)
     - **Internal Storage:** 2048MB-8192MB (default 6144MB is good)
     - **SD Card:** Optional, 512MB-2048MB for testing storage
     - **Camera:** Webcam or emulated
     - **Network:** Fast or Full speed
     - **Boot Option:**
       - Cold boot - fresh start (slower)
       - Quick boot - saves state (recommended, faster)
     - **Graphics:**
       - Automatic (recommended)
       - Hardware - GLES 2.0 (faster, requires GPU support)
       - Software - GLES 2.0 (slower, compatibility fallback)
     - **Device Frame:** Enable to show device bezel

5. **Click "Finish"**

#### Hardware Acceleration Setup

**Windows (Intel CPU):**
1. **Install Intel HAXM:**
   - Via SDK Manager: SDK Tools > Intel x86 Emulator Accelerator (HAXM)
   - Or download from [GitHub](https://github.com/intel/haxm)
   - Requires Intel VT-x enabled in BIOS

2. **Verify HAXM:**
   ```cmd
   sc query intelhaxm
   ```
   - Should show "RUNNING"

3. **Troubleshooting:**
   - Disable Hyper-V if installed: `bcdedit /set hypervisorlaunchtype off`
   - Enable Intel VT-x in BIOS/UEFI
   - Check Windows Features: Disable "Windows Hypervisor Platform" if conflicts occur

**Windows (AMD CPU):**
1. **Enable Windows Hypervisor Platform:**
   - Control Panel > Programs > Turn Windows features on or off
   - Enable "Windows Hypervisor Platform"
   - Restart computer

2. **Enable SVM in BIOS:**
   - AMD Virtualization (AMD-V/SVM) must be enabled in BIOS

**macOS:**
- Hardware acceleration is built-in via Hypervisor Framework
- No additional setup required
- Works automatically on both Intel and Apple Silicon

**Linux:**
1. **Install KVM:**
   ```bash
   sudo apt install qemu-kvm libvirt-daemon-system libvirt-clients bridge-utils
   ```

2. **Add user to KVM group:**
   ```bash
   sudo adduser $USER kvm
   sudo adduser $USER libvirt
   ```

3. **Verify KVM:**
   ```bash
   kvm-ok
   ```
   - Should show "KVM acceleration can be used"

4. **Logout and login** for group changes to take effect

#### Emulator Performance Optimization

**Best Practices:**
1. **Use x86_64 images** when possible (faster than ARM on Intel/AMD)
2. **Enable hardware graphics:** AVD settings > Graphics: Hardware or Automatic
3. **Limit RAM allocation:** Don't exceed 50% of host RAM
4. **Close unnecessary apps** on host machine during emulator use
5. **Use Quick Boot:** Saves emulator state for faster subsequent launches
6. **Disable animations in emulator:**
   - Settings > Developer Options > Window/Transition/Animator animation scale > Off
7. **Install on SSD:** Ensure Android SDK and AVD are on SSD, not HDD
8. **Multi-core CPU:** Assign 2-4 CPU cores in AVD advanced settings

**Emulator Command-Line Options:**
```bash
# Launch emulator with performance settings
emulator -avd Pixel_7_Pro_API_35 -gpu host -memory 4096 -cores 4

# Launch with cold boot (ignore saved state)
emulator -avd Pixel_7_Pro_API_35 -no-snapshot-load

# Launch with specific network speed
emulator -avd Pixel_7_Pro_API_35 -netspeed full
```

#### Emulator Snapshots

**Managing Snapshots:**
- **Quick Boot Snapshots:** Automatically saved on emulator close
- **Custom Snapshots:** Create via emulator "..." menu > Snapshots
- **Benefits:**
  - Instant emulator start (5-10 seconds vs 30-60 seconds cold boot)
  - Restore specific testing states
  - Save time during development

**Snapshot Location:**
- Windows: `C:\Users\<username>\.android\avd\<avd_name>.avd\snapshots`
- macOS/Linux: `~/.android/avd/<avd_name>.avd/snapshots`

#### Emulator Features and Tools

**Extended Controls (... menu):**
- **Location:** Simulate GPS coordinates, routes
- **Cellular:** Change network type (4G, 5G, etc.)
- **Battery:** Simulate battery level, charging state
- **Phone:** Test phone calls, SMS
- **Camera:** Use host webcam or virtual scene
- **Fingerprint:** Simulate fingerprint authentication
- **Virtual Sensors:** Accelerometer, gyroscope, etc.
- **Settings:** Emulator preferences and configuration

**Keyboard Shortcuts:**
- **Ctrl + M (Cmd + M on Mac):** Menu
- **Ctrl + B (Cmd + B):** Back button
- **Ctrl + H (Cmd + H):** Home button
- **Ctrl + O (Cmd + O):** Overview/Recent apps
- **Ctrl + P (Cmd + P):** Power button
- **Ctrl + S (Cmd + S):** Screenshot

#### Troubleshooting Common Emulator Issues

**Emulator won't start:**
- Check hardware acceleration is enabled and working
- Verify sufficient RAM allocation (not too high or too low)
- Try cold boot: `emulator -avd <name> -no-snapshot-load`
- Check antivirus isn't blocking emulator
- Update emulator via SDK Manager

**Emulator is slow:**
- Ensure hardware acceleration (HAXM/KVM/Hypervisor) is active
- Use x86_64 system image instead of ARM
- Enable hardware graphics: AVD settings > Graphics: Hardware
- Reduce RAM allocation if too high
- Close unnecessary applications on host
- Disable host antivirus scanning of SDK folder

**"HAXM is not installed" error:**
- Install HAXM via SDK Manager
- Enable Intel VT-x in BIOS
- Disable Hyper-V on Windows: `bcdedit /set hypervisorlaunchtype off`

**Gradle build fails:**
- Update Android Gradle Plugin to latest stable (9.1.0)
- Check Java version (should be JDK 17)
- Verify Gradle version compatibility (9.4.1 recommended)
- Clear Gradle cache: `./gradlew clean` or delete `~/.gradle/caches`

### IDE Configuration Recommendations

#### Project-Level Settings

**build.gradle (Project level):**
```gradle
plugins {
    id 'com.android.application' version '9.1.0' apply false
    id 'com.android.library' version '9.1.0' apply false
    // No need for kotlin-android plugin with AGP 9.0+
}
```

**build.gradle (App level):**
```gradle
plugins {
    id 'com.android.application'
    // Kotlin support is built-in with AGP 9.0+
}

android {
    namespace 'com.example.app'
    compileSdk 35

    defaultConfig {
        applicationId "com.example.app"
        minSdk 24
        targetSdk 35
        versionCode 1
        versionName "1.0"
    }

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = '17'
    }

    buildTypes {
        release {
            minifyEnabled true
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }
}
```

**gradle-wrapper.properties:**
```properties
distributionUrl=https\://services.gradle.org/distributions/gradle-9.4.1-all.zip
```

#### IDE Preferences

**Code Style:**
- Settings > Editor > Code Style > Kotlin/Java
- Use default Android code style or import team's style guide
- Enable auto-formatting: Settings > Tools > Actions on Save > Reformat code

**Build Settings:**
- Settings > Build, Execution, Deployment > Compiler
- **Build process heap size:** 2048MB minimum (4096MB recommended)
- **Parallel compilation:** Enable for faster builds
- Settings > Build, Execution, Deployment > Gradle
- **Gradle JDK:** Use Embedded JDK or JDK 17+
- **Offline mode:** Enable for faster builds (when dependencies are cached)

**Editor Settings:**
- Settings > Editor > General > Auto Import
  - Enable "Add unambiguous imports on the fly"
  - Enable "Optimize imports on the fly"
- Settings > Editor > General > Code Completion
  - Enable "Auto-popup code completion" (400ms delay)

**Version Control:**
- Settings > Version Control > Git
- Configure Git executable path
- Enable "Use credential helper"
- Settings > Version Control > Commit
  - Enable "Analyze code" before commit
  - Enable "Check TODO" before commit

**Performance Settings:**
- Help > Edit Custom VM Options (increase if you have 16GB+ RAM)
  ```
  -Xms1024m
  -Xmx4096m
  -XX:ReservedCodeCacheSize=512m
  ```
- Settings > Appearance & Behavior > System Settings
  - Increase "Synchronize files on frame activation" delay
  - Reduce "Reopen last project on startup" if not needed

**Sources:**
- [Install Android Studio](https://developer.android.com/studio/install)
- [Configure hardware acceleration for the Android Emulator](https://developer.android.com/studio/run/emulator-acceleration)
- [Create and manage virtual devices](https://developer.android.com/studio/run/managing-avds)
- [Start the emulator from the command line](https://developer.android.com/studio/run/emulator-commandline)
- [Update the IDE and SDK Tools](https://developer.android.com/studio/intro/update)
- [SDK Manager](https://developer.android.com/tools/sdkmanager)
- [Run apps on the Android Emulator](https://developer.android.com/studio/run/emulator)

---

## Summary: Recommended Development Environment (2026)

### Software Versions
- **Android Studio:** Panda 2 (2025.3.2) or latest stable
- **Android SDK:** API level 35 (Android 15) - compile and target
- **Minimum SDK:** API level 24 (Android 7.0) or higher for compatibility
- **Java:** JDK 17 (minimum, Java 26 supported by Gradle 9.4.1)
- **Kotlin:** 2.3.20 or higher
- **Gradle:** 9.4.1 or latest stable
- **Android Gradle Plugin:** 9.1.0 or latest stable
- **Kotlin Gradle Plugin:** 2.2.10 or higher (if not using built-in Kotlin)

### Hardware Recommendations
- **Minimum:** 8GB RAM, 20GB disk space, dual-core processor
- **Recommended:** 16GB RAM, 50GB SSD space, quad-core processor
- **Optimal:** 32GB RAM, 100GB+ SSD space, 6+ core processor

### Operating System
- Windows 10/11 (64-bit)
- macOS 11.0 or higher
- Ubuntu 20.04 LTS or higher (Linux)

### Key Configuration Notes
1. Built-in Kotlin support is enabled by default in AGP 9.0+
2. No need to apply `org.jetbrains.kotlin.android` plugin separately
3. Use Java 17 for source/target compatibility and JVM target
4. Target API level 35 for Google Play compliance
5. Update to latest stable versions regularly for security and performance
6. SSD strongly recommended for build performance
7. Hardware acceleration (Intel HAXM/AMD) required for optimal emulator performance

---

## 6. Common Setup Issues and Troubleshooting

This section documents the most common issues encountered during Android development environment setup and their solutions, based on widespread community reports and official documentation.

### Issue 1: Environment Variables Not Set (ANDROID_HOME/JAVA_HOME)

**Symptoms:**
- Error: "ANDROID_HOME is not set" or "SDK location not found"
- Error: "JAVA_HOME is not set" during Gradle builds
- Command-line tools (adb, sdkmanager) not recognized
- Build fails with "Android SDK not found"

**Root Causes:**
- Environment variables not configured after Android Studio installation
- Incorrect path specified in environment variables
- Variables set in wrong shell configuration file (macOS/Linux)
- System environment variables not refreshed after setting

**Solutions:**

**Windows:**
```cmd
# Set ANDROID_HOME
setx ANDROID_HOME "C:\Users\%USERNAME%\AppData\Local\Android\Sdk"

# Set JAVA_HOME (if using standalone JDK)
setx JAVA_HOME "C:\Program Files\Android\Android Studio\jbr"

# Add to PATH
setx PATH "%PATH%;%ANDROID_HOME%\platform-tools;%ANDROID_HOME%\tools"
```

**macOS/Linux:**
```bash
# Add to ~/.zshrc (macOS) or ~/.bashrc (Linux)
export ANDROID_HOME=$HOME/Library/Android/sdk  # macOS
# or
export ANDROID_HOME=$HOME/Android/Sdk  # Linux

export JAVA_HOME=/Applications/Android\ Studio.app/Contents/jbr/Contents/Home  # macOS
# or
export JAVA_HOME=/opt/android-studio/jbr  # Linux

export PATH=$PATH:$ANDROID_HOME/platform-tools
export PATH=$PATH:$ANDROID_HOME/tools
export PATH=$PATH:$ANDROID_HOME/cmdline-tools/latest/bin

# Apply changes
source ~/.zshrc  # or source ~/.bashrc
```

**Verification:**
```bash
# Check ANDROID_HOME
echo $ANDROID_HOME  # macOS/Linux
echo %ANDROID_HOME%  # Windows

# Check JAVA_HOME
echo $JAVA_HOME  # macOS/Linux
echo %JAVA_HOME%  # Windows

# Test ADB
adb version

# Test sdkmanager
sdkmanager --version
```

**Prevention:**
- Document environment variable setup in team onboarding guides
- Use Android Studio's embedded JDK (no JAVA_HOME needed for IDE)
- Restart terminal/IDE after setting environment variables
- Verify paths exist before setting variables

---

### Issue 2: SDK License Not Accepted

**Symptoms:**
- Error during build: "You have not accepted the license agreements"
- Error: "Android SDK licenses not accepted"
- CI/CD builds fail with license errors
- Gradle sync fails with "licenses not accepted" message

**Root Causes:**
- Fresh Android Studio installation without license acceptance
- SDK components installed via command line without accepting licenses
- CI/CD environment lacks accepted licenses
- SDK updated with new license terms

**Solutions:**

**Interactive Method (Android Studio):**
1. Open Android Studio
2. Go to Settings/Preferences > Appearance & Behavior > System Settings > Android SDK
3. Click "SDK Platforms" or "SDK Tools"
4. Select any component and click "Apply"
5. Review and accept license agreements in popup dialog

**Command-Line Method:**
```bash
# Accept all SDK licenses at once
sdkmanager --licenses

# Press 'y' for each license prompt
# Or accept all automatically (CI/CD)
yes | sdkmanager --licenses
```

**CI/CD Automation:**
```yaml
# GitHub Actions example
- name: Accept Android licenses
  run: yes | sdkmanager --licenses

# Or copy pre-accepted licenses
- name: Copy accepted licenses
  run: |
    mkdir -p $ANDROID_HOME/licenses
    echo "24333f8a63b6825ea9c5514f83c2829b004d1fee" > $ANDROID_HOME/licenses/android-sdk-license
    echo "84831b9409646a918e30573bab4c9c91346d8abd" > $ANDROID_HOME/licenses/android-sdk-preview-license
```

**Manual License File Creation:**
```bash
# Create licenses directory
mkdir -p $ANDROID_HOME/licenses

# Create license files with accepted hashes
echo "24333f8a63b6825ea9c5514f83c2829b004d1fee" > $ANDROID_HOME/licenses/android-sdk-license
echo "84831b9409646a918e30573bab4c9c91346d8abd" > $ANDROID_HOME/licenses/android-sdk-preview-license
echo "d56f5187479451eabf01fb78af6dfcb131a6481e" > $ANDROID_HOME/licenses/android-sdk-license
echo "e9acab5b5fbb560a72cfaecce8946896ff6aab9d" > $ANDROID_HOME/licenses/intel-android-extra-license
```

**Verification:**
```bash
# Check if licenses are accepted
ls $ANDROID_HOME/licenses/

# Should show:
# android-sdk-license
# android-sdk-preview-license
# intel-android-extra-license (if using HAXM)
```

**Prevention:**
- Run `sdkmanager --licenses` immediately after Android Studio installation
- Include license acceptance in setup automation scripts
- Document license acceptance in project README
- Keep license files in version control for CI/CD (check your organization's policy)

---

### Issue 3: Gradle Build Failures - Version Incompatibility

**Symptoms:**
- Error: "Unsupported class file major version 65" (or similar)
- Error: "This version of the Android Support plugin requires Gradle X.X"
- Error: "The Android Gradle plugin supports only Kotlin Gradle plugin version Y"
- Build fails after updating Android Studio or dependencies
- "Could not determine the dependencies of task"

**Root Causes:**
- Mismatched Android Gradle Plugin (AGP) and Gradle versions
- Incompatible Java version for current Gradle version
- Outdated Kotlin plugin version with new AGP
- Project created with older Android Studio version

**Solutions:**

**Step 1: Check Current Versions**
```bash
# Check Gradle version
./gradlew --version

# Check Gradle wrapper version
cat gradle/wrapper/gradle-wrapper.properties
```

**Step 2: Update Gradle Wrapper (Recommended 2026)**
```bash
# Update to Gradle 9.4.1
./gradlew wrapper --gradle-version=9.4.1 --distribution-type=all
```

Or manually edit `gradle/wrapper/gradle-wrapper.properties`:
```properties
distributionUrl=https\://services.gradle.org/distributions/gradle-9.4.1-all.zip
```

**Step 3: Update Android Gradle Plugin**

Edit project-level `build.gradle` or `build.gradle.kts`:
```gradle
plugins {
    id 'com.android.application' version '9.1.0' apply false
    id 'com.android.library' version '9.1.0' apply false
    // Note: kotlin-android plugin not needed with AGP 9.0+
}
```

**Step 4: Update Kotlin Version (if using explicit plugin)**
```gradle
plugins {
    id 'org.jetbrains.kotlin.android' version '2.3.20' apply false
}
```

**Step 5: Verify Java Version**
```bash
# Check Java version
java -version
# Should be Java 17 or higher for AGP 9.x

# In build.gradle (app level)
android {
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_17
        targetCompatibility JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = '17'
    }
}
```

**Step 6: Clean and Rebuild**
```bash
# Clean build cache
./gradlew clean

# Clear Gradle cache (if issues persist)
rm -rf ~/.gradle/caches
./gradlew clean build --refresh-dependencies
```

**AGP and Gradle Compatibility Matrix (2026):**
| AGP Version | Minimum Gradle | Recommended Gradle | Minimum JDK |
|-------------|----------------|---------------------|-------------|
| 9.1.0       | 8.9            | 9.4.1               | 17          |
| 9.0.1       | 8.9            | 9.4.1               | 17          |
| 8.7.0       | 8.4            | 8.10                | 17          |

**Common Error Messages and Fixes:**

| Error | Cause | Fix |
|-------|-------|-----|
| "Unsupported class file major version 65" | Java version mismatch | Update to JDK 17+ |
| "Minimum supported Gradle version is 8.9" | Gradle too old for AGP | Update Gradle wrapper |
| "Could not resolve com.android.tools.build:gradle:9.1.0" | Repository issue | Check repositories block |
| "Kotlin plugin should be enabled" | Missing Kotlin plugin | Apply kotlin-android plugin or use AGP 9.0+ |

**Verification:**
```bash
# Sync and build
./gradlew build

# Should complete without version errors
```

**Prevention:**
- Keep AGP, Gradle, and Kotlin versions in sync using compatibility matrix
- Update dependencies regularly (monthly review)
- Use Android Studio's update notifications
- Test updates in feature branch before merging
- Document required versions in project README

---

### Issue 4: Emulator Not Starting - Hardware Acceleration Issues

**Symptoms:**
- Emulator fails to start with "HAXM is not installed" error
- Emulator is extremely slow (>5 minutes to boot)
- Error: "Intel HAXM is required to run this AVD"
- Error: "/dev/kvm permission denied" (Linux)
- Black screen or frozen emulator window
- Hyper-V conflict errors (Windows)

**Root Causes:**
- Hardware acceleration not installed or not enabled
- BIOS virtualization settings disabled
- Hyper-V/WSL2 conflicts on Windows
- Incorrect emulator system image (ARM on Intel CPU)
- Insufficient permissions for KVM (Linux)

**Solutions:**

**Windows (Intel CPU) - HAXM Setup:**

1. **Check if VT-x is enabled:**
```cmd
# Open Task Manager > Performance > CPU
# Look for "Virtualization: Enabled"
```

2. **Enable VT-x in BIOS:**
- Restart computer and enter BIOS (Del, F2, F10, or Esc during boot)
- Find "Intel Virtualization Technology" or "VT-x"
- Enable it and save changes

3. **Disable Hyper-V (if installed):**
```cmd
# Run as Administrator
bcdedit /set hypervisorlaunchtype off

# Also disable in Windows Features
# Control Panel > Programs > Turn Windows features on or off
# Uncheck: Hyper-V, Windows Hypervisor Platform, Virtual Machine Platform
# Restart computer
```

4. **Install HAXM:**
```cmd
# Via SDK Manager
# Settings > SDK Tools > Intel x86 Emulator Accelerator (HAXM)

# Or manual installation
# Download from: https://github.com/intel/haxm/releases
# Run intelhaxm-android.exe as Administrator
```

5. **Verify HAXM:**
```cmd
sc query intelhaxm
# Should show "STATE: 4 RUNNING"
```

**Windows (AMD CPU) - Hyper-V Setup:**

1. **Enable SVM in BIOS:**
- Enter BIOS during boot
- Find "SVM Mode" or "AMD-V"
- Enable and save

2. **Enable Windows Hypervisor Platform:**
```cmd
# Run as Administrator
# Control Panel > Programs > Turn Windows features on or off
# Check: Windows Hypervisor Platform
# Restart computer
```

3. **Use Android Emulator with Hyper-V:**
- No additional installation needed
- Use x86_64 system images (not ARM)

**macOS - Built-in Acceleration:**
- Hardware acceleration works automatically
- No additional setup required
- Works on both Intel and Apple Silicon Macs

**For Apple Silicon Macs:**
- Use ARM64 system images instead of x86_64
- Download "ARM 64 v8a" images in AVD Manager
- Performance is excellent without additional setup

**Linux - KVM Setup:**

1. **Check if KVM is supported:**
```bash
egrep -c '(vmx|svm)' /proc/cpuinfo
# Should return > 0
```

2. **Install KVM:**
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install qemu-kvm libvirt-daemon-system libvirt-clients bridge-utils cpu-checker

# Fedora/RHEL
sudo dnf install @virtualization
```

3. **Add user to kvm group:**
```bash
sudo adduser $USER kvm
sudo adduser $USER libvirt

# Or
sudo usermod -aG kvm $USER
sudo usermod -aG libvirt $USER
```

4. **Set permissions on /dev/kvm:**
```bash
sudo chmod 666 /dev/kvm

# Or permanently
sudo chown $USER /dev/kvm
```

5. **Verify KVM:**
```bash
kvm-ok
# Should show: "KVM acceleration can be used"

# Check KVM module
lsmod | grep kvm
# Should show kvm_intel or kvm_amd
```

6. **Logout and login** for group changes to take effect

**Common Fixes for Emulator Issues:**

| Problem | Solution |
|---------|----------|
| Emulator won't start | Try cold boot: `emulator -avd <name> -no-snapshot-load` |
| Black screen | Enable hardware graphics in AVD settings |
| Very slow | Check HAXM/KVM running, use x86_64 images |
| HAXM conflict with Hyper-V | Disable Hyper-V or switch to AMD processor workflow |
| Permission denied (Linux) | Add user to kvm group, set /dev/kvm permissions |

**Verification:**
```bash
# List available AVDs
emulator -list-avds

# Start emulator from command line with verbose output
emulator -avd Pixel_7_Pro_API_35 -verbose

# Check for hardware acceleration messages in output
# Should see: "HAX is working and emulator runs in fast virt mode"
```

**Prevention:**
- Choose correct system image (x86_64 for Intel/AMD, ARM64 for Apple Silicon)
- Document virtualization requirements in setup guide
- Enable hardware acceleration before creating AVDs
- Keep emulator updated via SDK Manager
- Avoid running VirtualBox/VMware/Hyper-V simultaneously with HAXM

---

### Issue 5: Memory and Performance Issues

**Symptoms:**
- Android Studio freezes or becomes unresponsive
- Error: "OutOfMemoryError: Java heap space"
- Error: "OutOfMemoryError: GC overhead limit exceeded"
- Gradle build is very slow (>5 minutes for incremental builds)
- Indexing takes forever (>30 minutes)
- Emulator crashes or system becomes sluggish

**Root Causes:**
- Insufficient RAM allocation to IDE and Gradle
- Default JVM heap sizes too small for large projects
- Too many background processes running
- Project on HDD instead of SSD
- Gradle daemon using default settings
- Antivirus scanning project files during build

**Solutions:**

**Step 1: Increase IDE Heap Size**

Edit custom VM options: `Help > Edit Custom VM Options`

```properties
# For systems with 16GB RAM
-Xms1024m
-Xmx4096m
-XX:ReservedCodeCacheSize=512m
-XX:+UseG1GC
-XX:SoftRefLRUPolicyMSPerMB=50
-XX:CICompilerCount=2
-XX:+HeapDumpOnOutOfMemoryError
-XX:HeapDumpPath=$USER_HOME/java_error_in_studio.hprof

# For systems with 32GB+ RAM
-Xms2048m
-Xmx8192m
-XX:ReservedCodeCacheSize=1024m
-XX:+UseG1GC
-XX:SoftRefLRUPolicyMSPerMB=50
-XX:CICompilerCount=2
```

**Restart Android Studio after changes**

**Step 2: Increase Gradle Build Memory**

Edit `gradle.properties` in project root:

```properties
# Gradle daemon heap size
org.gradle.jvmargs=-Xmx4096m -XX:MaxMetaspaceSize=1024m -XX:+HeapDumpOnOutOfMemoryError

# Enable Gradle caching
org.gradle.caching=true

# Enable parallel builds
org.gradle.parallel=true

# Enable configuration cache (AGP 9.0+)
org.gradle.configuration-cache=true

# Configure Gradle daemon
org.gradle.daemon=true
org.gradle.configureondemand=true
```

**Step 3: Optimize Android Studio Settings**

**File > Settings/Preferences:**

1. **Appearance & Behavior > System Settings:**
   - Uncheck "Reopen projects on startup" (if not needed)
   - Uncheck "Confirm application exit"
   - Synchronize files on frame activation: Set to "Never"

2. **Build, Execution, Deployment > Compiler:**
   - Build process heap size: 2048 MB minimum
   - Command-line Options: `--parallel --max-workers=4`
   - Check "Compile independent modules in parallel"

3. **Build, Execution, Deployment > Gradle:**
   - Gradle JDK: Use Embedded JDK
   - Check "Download external annotations for dependencies"

4. **Editor > General:**
   - Uncheck unused plugins in Settings > Plugins
   - Reduce "Recent files limit" to 20-30

**Step 4: Clean and Optimize Project**

```bash
# Clean build artifacts
./gradlew clean

# Clear Gradle cache (if corrupted)
rm -rf ~/.gradle/caches
rm -rf .gradle

# Invalidate IDE caches
# File > Invalidate Caches > Invalidate and Restart
```

**Step 5: Optimize AVD Memory**

In AVD Manager > Edit AVD > Advanced Settings:
- **RAM:** 2048-4096 MB (don't exceed 50% of host RAM)
- **VM Heap:** 256-512 MB
- Close emulator when not actively testing

**Step 6: System-Level Optimizations**

**Exclude project directories from antivirus scanning:**
- `C:\Users\<username>\.gradle` (Windows)
- `C:\Users\<username>\.android` (Windows)
- `<project_directory>\build`
- `~/.gradle` (macOS/Linux)
- `~/.android` (macOS/Linux)

**Move project to SSD:**
- Builds are 3-5x faster on SSD vs HDD
- Ensure Android SDK is also on SSD

**Close unnecessary applications:**
- Chrome/browsers (can use 2-4GB RAM)
- Other IDEs
- Communication apps (Slack, Teams)

**Step 7: Enable Gradle Build Cache**

In project-level `build.gradle`:
```gradle
buildscript {
    // Enable build cache
    configurations.all {
        resolutionStrategy.cacheDynamicVersionsFor 0, 'seconds'
        resolutionStrategy.cacheChangingModulesFor 0, 'seconds'
    }
}
```

**Memory Recommendations by System RAM:**

| System RAM | IDE Heap (-Xmx) | Gradle Heap (jvmargs) | AVD RAM | Notes |
|------------|----------------|----------------------|---------|-------|
| 8GB        | 2048m          | 2048m                | 2048m   | Minimal, close other apps |
| 16GB       | 4096m          | 4096m                | 2048m   | Good for most projects |
| 32GB       | 8192m          | 6144m                | 4096m   | Excellent performance |
| 64GB+      | 16384m         | 8192m                | 8192m   | No memory constraints |

**Verification:**
```bash
# Check Gradle daemon status
./gradlew --status

# Monitor build performance
./gradlew build --profile
# Check build/reports/profile/ for performance report

# Monitor IDE memory usage
# Help > Diagnostic Tools > Memory Indicator (shows in status bar)
```

**Prevention:**
- Start with recommended memory settings during initial setup
- Monitor memory usage regularly (enable memory indicator)
- Keep project dependencies minimal
- Clean build artifacts weekly
- Restart IDE daily for optimal performance
- Use SSD for all development work

---

### Issue 6: ADB Connection Issues

**Symptoms:**
- Error: "adb: device offline"
- Error: "adb: no devices/emulators found"
- Device shows as "unauthorized" in `adb devices`
- USB debugging not working
- Multiple ADB versions conflict
- "daemon not running; starting now at tcp:5037" repeatedly

**Root Causes:**
- ADB server crashed or in bad state
- USB debugging not enabled on device
- USB cable or port issue (data vs charging only)
- Multiple ADB versions installed (Android Studio vs system)
- Firewall blocking ADB port (5037)
- Device authorization not granted

**Solutions:**

**Step 1: Restart ADB Server**
```bash
# Kill and restart ADB server
adb kill-server
adb start-server

# Check connected devices
adb devices

# Should show:
# List of devices attached
# <device_id>    device
```

**Step 2: Enable USB Debugging on Android Device**

1. Enable Developer Options:
   - Settings > About Phone
   - Tap "Build Number" 7 times
   - Enter PIN/password if prompted

2. Enable USB Debugging:
   - Settings > System > Developer Options
   - Enable "USB debugging"
   - Enable "USB debugging (Security settings)" (if available)

3. Connect device via USB

4. On device, accept "Allow USB debugging?" prompt
   - Check "Always allow from this computer"
   - Tap "OK"

**Step 3: Check USB Connection**
```bash
# macOS/Linux - check USB connection
lsusb

# Windows - check in Device Manager
# Should see "Android Device" > "Android Composite ADB Interface"
```

**Step 4: Fix Unauthorized Device**
```bash
# Revoke previous authorizations on device
# Settings > Developer Options > Revoke USB debugging authorizations

# Disconnect and reconnect USB cable
# Accept new authorization prompt on device

# Verify
adb devices
# Should show "device" not "unauthorized"
```

**Step 5: Fix Multiple ADB Versions**
```bash
# Check ADB version
adb version
# Should match Android Studio's ADB version

# Find ADB locations
# macOS/Linux
which adb
find / -name adb 2>/dev/null

# Windows
where adb

# Ensure only using Android Studio's ADB
# Add to PATH (first position):
# Windows: %ANDROID_HOME%\platform-tools
# macOS/Linux: $ANDROID_HOME/platform-tools
```

**Step 6: Check Firewall Settings**

**Windows:**
```cmd
# Allow ADB through firewall
netsh advfirewall firewall add rule name="ADB" dir=in action=allow protocol=TCP localport=5037
```

**macOS:**
```bash
# System Preferences > Security & Privacy > Firewall > Firewall Options
# Add Android Studio and allow incoming connections
```

**Linux:**
```bash
# Allow ADB port
sudo ufw allow 5037/tcp
```

**Step 7: Fix Device Offline**
```bash
# Device shows offline
adb devices
# <device_id>    offline

# Reconnect
adb reconnect device

# Or force reconnect
adb disconnect
adb connect <device_ip>:5555  # For wireless debugging

# For USB
adb kill-server
# Unplug and replug USB cable
adb start-server
```

**Step 8: Wireless ADB Debugging (Alternative)**
```bash
# On device connected via USB
adb tcpip 5555

# Disconnect USB cable
# Find device IP: Settings > About Phone > Status > IP address

# Connect wirelessly
adb connect <device_ip>:5555

# Verify
adb devices
# Should show: <device_ip>:5555    device

# To switch back to USB
adb usb
```

**Common ADB Commands:**
```bash
# List all devices
adb devices -l

# Get device serial number
adb get-serialno

# Install APK
adb install app.apk

# Uninstall app
adb uninstall com.example.app

# View logcat
adb logcat

# Access device shell
adb shell

# Push file to device
adb push local_file /sdcard/

# Pull file from device
adb pull /sdcard/remote_file ./

# Restart device
adb reboot

# Check ADB version
adb version
```

**Verification:**
```bash
# Should show device as "device" (not offline/unauthorized)
adb devices

# Test ADB shell access
adb shell ls

# Test app installation
adb install -r app.apk
```

**Prevention:**
- Use high-quality USB data cables (not charging-only cables)
- Keep ADB and platform-tools updated via SDK Manager
- Always grant USB debugging authorization on device
- Use wireless debugging for convenience (fewer cable issues)
- Document ADB setup in team wiki
- Add ADB to PATH during initial setup

---

### Issue 7: Network and Proxy Issues During Setup

**Symptoms:**
- Error: "Connection timed out" when downloading SDK components
- Error: "Failed to download any source lists"
- SDK Manager shows "Nothing to show" or empty package list
- Gradle build fails with "Could not resolve dependencies"
- Error: "peer not authenticated" or SSL certificate errors
- Very slow downloads (hanging at 0% or timing out)

**Root Causes:**
- Corporate firewall blocking downloads
- Proxy settings not configured in Android Studio
- SSL certificate verification issues
- DNS resolution problems
- Antivirus blocking connections
- Repository URLs outdated or blocked

**Solutions:**

**Step 1: Configure Proxy in Android Studio**

1. **Settings > Appearance & Behavior > System Settings > HTTP Proxy**

2. **Manual Proxy Configuration:**
   - Select "Manual proxy configuration"
   - HTTP:
     - Host: `your.proxy.server`
     - Port: `8080` (or your proxy port)
   - HTTPS: (same as HTTP for most setups)
   - Proxy authentication (if required):
     - Login: `your_username`
     - Password: `your_password`
   - No proxy for: `localhost,127.0.0.1`

3. **Check connection:**
   - Click "Check connection"
   - Test with: `https://google.com`
   - Should show "Connection successful"

**Step 2: Configure Gradle Proxy**

Edit `~/.gradle/gradle.properties` (global) or project `gradle.properties`:

```properties
# HTTP Proxy
systemProp.http.proxyHost=your.proxy.server
systemProp.http.proxyPort=8080
systemProp.http.proxyUser=your_username
systemProp.http.proxyPassword=your_password
systemProp.http.nonProxyHosts=localhost|127.0.0.1

# HTTPS Proxy
systemProp.https.proxyHost=your.proxy.server
systemProp.https.proxyPort=8080
systemProp.https.proxyUser=your_username
systemProp.https.proxyPassword=your_password
systemProp.https.nonProxyHosts=localhost|127.0.0.1

# SOCKS Proxy (alternative)
# systemProp.socksProxyHost=your.proxy.server
# systemProp.socksProxyPort=1080
```

**Step 3: Configure SDK Manager Proxy**

**Command-line sdkmanager:**
```bash
# Set proxy for sdkmanager
sdkmanager --proxy=http --proxy_host=your.proxy.server --proxy_port=8080 --licenses
```

**Via environment variables:**
```bash
# Linux/macOS
export http_proxy=http://your.proxy.server:8080
export https_proxy=http://your.proxy.server:8080
export HTTP_PROXY=$http_proxy
export HTTPS_PROXY=$https_proxy

# Windows
set HTTP_PROXY=http://your.proxy.server:8080
set HTTPS_PROXY=http://your.proxy.server:8080
```

**Step 4: Fix SSL Certificate Issues**

**Option 1: Disable SSL verification (NOT recommended for production)**
```properties
# In gradle.properties (temporary workaround only)
systemProp.javax.net.ssl.trustAll=true
```

**Option 2: Import corporate certificate**
```bash
# Find Java keystore location
# Android Studio's JDK: <android-studio>/jbr/lib/security/cacerts

# Import certificate (run as administrator/sudo)
keytool -import -trustcacerts -alias corporateCert \
  -file /path/to/certificate.crt \
  -keystore <android-studio>/jbr/lib/security/cacerts \
  -storepass changeit
```

**Option 3: Use HTTP instead of HTTPS (temporary)**

Edit project `build.gradle`:
```gradle
repositories {
    // Temporary: Use HTTP mirrors
    maven { url 'http://maven.aliyun.com/nexus/content/groups/public/' }
    maven { url 'http://maven.aliyun.com/nexus/content/repositories/jcenter' }
    google()
    mavenCentral()
}
```

**Step 5: Use Alternative Repository Mirrors**

For users in regions with slow access to Google servers:

```gradle
// Project-level build.gradle
allprojects {
    repositories {
        // China mirrors (example)
        maven { url 'https://maven.aliyun.com/repository/google' }
        maven { url 'https://maven.aliyun.com/repository/jcenter' }
        maven { url 'https://maven.aliyun.com/repository/public' }

        // Fallback to official repositories
        google()
        mavenCentral()
    }
}
```

**Step 6: Clear DNS Cache**

**Windows:**
```cmd
ipconfig /flushdns
```

**macOS:**
```bash
sudo dscacheutil -flushcache
sudo killall -HUP mDNSResponder
```

**Linux:**
```bash
sudo systemd-resolve --flush-caches
# Or
sudo /etc/init.d/nscd restart
```

**Step 7: Temporarily Disable Antivirus/Firewall**

- Temporarily disable antivirus to test if it's blocking downloads
- Add exceptions for:
  - Android Studio executable
  - Gradle daemon
  - Android SDK directory
  - Project directory

**Step 8: Increase Timeout Values**

In `gradle.properties`:
```properties
# Increase timeout for slow networks
systemProp.org.gradle.internal.http.connectionTimeout=120000
systemProp.org.gradle.internal.http.socketTimeout=120000
```

**Step 9: Download SDK Components Offline**

If online download is impossible:

1. Download SDK components on a different machine with internet
2. Copy SDK folder to target machine:
   - Windows: `C:\Users\<username>\AppData\Local\Android\Sdk`
   - macOS: `~/Library/Android/sdk`
   - Linux: `~/Android/Sdk`
3. Point Android Studio to existing SDK location

**Verification:**
```bash
# Test Gradle dependency download
./gradlew dependencies --refresh-dependencies

# Test SDK Manager connection
sdkmanager --list

# Test internet connectivity
ping google.com
curl -I https://dl.google.com/android/repository/repository2-1.xml
```

**Prevention:**
- Document proxy settings in team setup guide
- Maintain offline SDK backup for emergencies
- Use company artifact repository/mirror (Artifactory, Nexus)
- Configure proxy settings before first SDK download
- Keep DNS and network stable during setup
- Work with IT to whitelist required domains:
  - `dl.google.com`
  - `dl-ssl.google.com`
  - `maven.google.com`
  - `services.gradle.org`
  - `repo.maven.apache.org`

---

### Issue 8: Missing Build Tools or SDK Components

**Symptoms:**
- Error: "Failed to find Build Tools revision X.X.X"
- Error: "Failed to find Platform SDK with path: platforms;android-XX"
- Error: "NDK is not installed"
- Error: "CMake version X.X.X was not found"
- Gradle sync fails with missing component errors
- Project won't build after cloning from repository

**Root Causes:**
- Required SDK components not installed
- Build configuration specifies non-installed versions
- SDK path not properly configured
- Partial SDK installation or corrupted downloads
- Team members using different SDK versions

**Solutions:**

**Step 1: Identify Missing Components**

Check build.gradle for required components:
```gradle
android {
    compileSdk 35  // Requires: platforms;android-35
    buildToolsVersion "35.0.0"  // Requires: build-tools;35.0.0
    ndkVersion "27.0.11902837"  // Requires: NDK r27
}
```

**Step 2: Install via SDK Manager (GUI)**

1. **Open SDK Manager:**
   - Tools > SDK Manager
   - Or Settings > Android SDK

2. **Install SDK Platforms:**
   - SDK Platforms tab
   - Check the required API level (e.g., Android 15.0 API 35)
   - Click "Apply"

3. **Install SDK Tools:**
   - SDK Tools tab
   - Check:
     - Android SDK Build-Tools (install required version)
     - Android SDK Platform-Tools
     - Android SDK Tools
     - Android Emulator
     - NDK (if needed)
     - CMake (if needed)
   - Click "Apply"

**Step 3: Install via Command Line**

```bash
# List available packages
sdkmanager --list

# Install specific SDK platform
sdkmanager "platforms;android-35"

# Install specific build tools
sdkmanager "build-tools;35.0.0"

# Install platform tools
sdkmanager "platform-tools"

# Install NDK
sdkmanager "ndk;27.0.11902837"

# Install CMake
sdkmanager "cmake;3.28.1"

# Install system image for emulator
sdkmanager "system-images;android-35;google_apis;x86_64"

# Install multiple at once
sdkmanager "platforms;android-35" "build-tools;35.0.0" "platform-tools"

# Update all installed packages
sdkmanager --update
```

**Step 4: Fix Build Tools Version Mismatch**

**Option 1: Update build.gradle to use installed version**
```gradle
android {
    compileSdk 35
    // Remove or comment out specific buildToolsVersion
    // buildToolsVersion "35.0.0"
    // AGP will use latest installed version automatically
}
```

**Option 2: Install the exact version specified**
```bash
# Install specific build tools version
sdkmanager "build-tools;35.0.0"
```

**Step 5: Fix SDK Path Issues**

**Check SDK location:**
- Settings > Android SDK > Android SDK Location

**Set ANDROID_HOME if not set:**
```bash
# macOS/Linux
export ANDROID_HOME=$HOME/Library/Android/sdk  # macOS
export ANDROID_HOME=$HOME/Android/Sdk  # Linux

# Windows
setx ANDROID_HOME "C:\Users\%USERNAME%\AppData\Local\Android\Sdk"
```

**Update local.properties:**
```properties
# In project root: local.properties
sdk.dir=/path/to/Android/Sdk

# Windows example:
# sdk.dir=C\:\\Users\\username\\AppData\\Local\\Android\\Sdk

# macOS example:
# sdk.dir=/Users/username/Library/Android/sdk

# Linux example:
# sdk.dir=/home/username/Android/Sdk
```

**Step 6: Sync and Rebuild Project**

```bash
# Sync Gradle with Android Studio
# File > Sync Project with Gradle Files

# Or via command line
./gradlew sync

# Clean and rebuild
./gradlew clean build
```

**Step 7: Fix Corrupted SDK Components**

If components are installed but not working:

```bash
# Uninstall and reinstall component
sdkmanager --uninstall "build-tools;35.0.0"
sdkmanager "build-tools;35.0.0"

# Or delete manually and reinstall
rm -rf $ANDROID_HOME/build-tools/35.0.0
sdkmanager "build-tools;35.0.0"
```

**Step 8: Standardize Team SDK Components**

Create a script for consistent team setup:

```bash
#!/bin/bash
# setup-android-sdk.sh

# Accept licenses
yes | sdkmanager --licenses

# Install required platforms
sdkmanager "platforms;android-35"
sdkmanager "platforms;android-34"

# Install build tools
sdkmanager "build-tools;35.0.0"

# Install essential tools
sdkmanager "platform-tools"
sdkmanager "emulator"
sdkmanager "tools"

# Install system images
sdkmanager "system-images;android-35;google_apis;x86_64"

# Optional: NDK for native development
# sdkmanager "ndk;27.0.11902837"
# sdkmanager "cmake;3.28.1"

echo "Android SDK setup complete!"
sdkmanager --list_installed
```

**Create version requirements file:**

`android-requirements.txt`:
```
Platform: android-35
Build Tools: 35.0.0
Platform Tools: 35.0.1
NDK: 27.0.11902837 (optional)
CMake: 3.28.1 (optional)
System Image: system-images;android-35;google_apis;x86_64
```

**Common Component Errors and Solutions:**

| Error | Missing Component | Install Command |
|-------|------------------|-----------------|
| "Failed to find Build Tools 35.0.0" | Build Tools | `sdkmanager "build-tools;35.0.0"` |
| "Failed to find Platform SDK: android-35" | SDK Platform | `sdkmanager "platforms;android-35"` |
| "NDK is not installed" | NDK | `sdkmanager "ndk;27.0.11902837"` |
| "CMake X.X.X was not found" | CMake | `sdkmanager "cmake;3.28.1"` |
| "adb: command not found" | Platform Tools | `sdkmanager "platform-tools"` |
| "No system images installed" | Emulator Image | `sdkmanager "system-images;android-35;google_apis;x86_64"` |

**Verification:**
```bash
# List installed SDK components
sdkmanager --list_installed

# Should show all required components
# Example output:
# build-tools;35.0.0
# platforms;android-35
# platform-tools
# emulator
# system-images;android-35;google_apis;x86_64

# Verify build succeeds
./gradlew build
```

**Prevention:**
- Document required SDK components in project README
- Use `sdkmanager --list_installed > sdk-components.txt` and commit to repo
- Create automated setup script for new team members
- Use Android Studio's "Sync Project with Gradle Files" to auto-detect missing components
- Pin specific versions in build.gradle for consistency
- Regularly update SDK components as a team (monthly or quarterly)
- Use CI/CD to validate SDK setup scripts

---

## Additional Troubleshooting Resources

### Official Documentation
- [Android Studio User Guide](https://developer.android.com/studio/intro)
- [Troubleshoot known issues with Android Emulator](https://developer.android.com/studio/run/emulator-troubleshooting)
- [Troubleshoot build errors](https://developer.android.com/studio/troubleshoot)
- [Configure hardware acceleration for the Android Emulator](https://developer.android.com/studio/run/emulator-acceleration)
- [ADB (Android Debug Bridge) Guide](https://developer.android.com/tools/adb)
- [Gradle build configuration tips](https://developer.android.com/build/optimize-your-build)

### Community Resources
- [Stack Overflow - Android Tag](https://stackoverflow.com/questions/tagged/android)
- [Reddit - r/androiddev](https://www.reddit.com/r/androiddev/)
- [Android Developers - Medium](https://medium.com/androiddevelopers)
- [Google's Android Developers Channel](https://www.youtube.com/@AndroidDevelopers)

### Diagnostic Commands Reference

```bash
# System Information
java -version                    # Check Java version
./gradlew --version             # Check Gradle version
adb version                     # Check ADB version
emulator -version               # Check Emulator version

# SDK Management
sdkmanager --list               # List available packages
sdkmanager --list_installed     # List installed packages
sdkmanager --update             # Update all packages
sdkmanager --licenses           # Accept licenses

# Device Management
adb devices -l                  # List connected devices (detailed)
adb shell getprop ro.build.version.release  # Android version on device
adb shell getprop ro.product.model          # Device model

# Build Diagnostics
./gradlew tasks                 # List all available tasks
./gradlew dependencies          # Show dependency tree
./gradlew --profile build       # Build with performance profiling
./gradlew clean build --info   # Verbose build output
./gradlew clean build --debug  # Very verbose debug output

# Cache Management
./gradlew clean                 # Clean build outputs
rm -rf ~/.gradle/caches         # Clear Gradle cache
rm -rf ~/.android/build-cache   # Clear Android build cache
File > Invalidate Caches        # Clear IDE caches (in Android Studio)

# Emulator Management
emulator -list-avds             # List available AVDs
emulator -avd <name> -verbose   # Launch with verbose logging
emulator -avd <name> -wipe-data # Launch with wiped data
```

---

**Sources:**
- [Troubleshoot known issues with Android Emulator](https://developer.android.com/studio/run/emulator-troubleshooting)
- [Troubleshoot build errors](https://developer.android.com/studio/troubleshoot)
- [Configure hardware acceleration for the Android Emulator](https://developer.android.com/studio/run/emulator-acceleration)
- [Gradle Build Configuration](https://developer.android.com/build/gradle-tips)
- [ADB Usage Guide](https://developer.android.com/tools/adb)
- [Android Studio Performance Tips](https://developer.android.com/studio/intro/studio-config)
- [Stack Overflow - Android Development](https://stackoverflow.com/questions/tagged/android)
- [Intel HAXM Documentation](https://github.com/intel/haxm/wiki)
- [KVM Installation Guide - Ubuntu](https://help.ubuntu.com/community/KVM/Installation)

---

**Last Updated:** March 24, 2026
**Next Review:** June 2026 (after Q2 major Android SDK release)
