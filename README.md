# 🅿 ParkTimer

> **DE:** Kostenloser Parkzeit-Timer für deutsche Einzelhandelsparkplätze (Lidl, Aldi, Rewe, …) mit Android Auto-Unterstützung.
>
> **EN:** Free parking timer for German retail car parks (Lidl, Aldi, Rewe, …) with Android Auto support.

---

## Screenshots / Vorschau

| Startbildschirm | Timer läuft | Warnung (10 Min) | Alarm (0 Min) |
|:-:|:-:|:-:|:-:|
| *(Startscreen)* | *(Running)* | *(Warning)* | *(Alert)* |

*Android Auto preview available in the DHU (see below).*

---

## Features / Funktionen

- **DE:** Fünf Schaltflächen für 15, 30, 60, 90 und 120 Minuten Parkdauer.
- **EN:** Five large buttons: 15 / 30 / 60 / 90 / 120 minutes.

- **DE:** Vollbild-Countdown mit Hintergrundfarbe: Grün → Gelb (10 Min) → Rot (0 Min).
- **EN:** Full-screen countdown with colour feedback: Green → Yellow (10 min) → Red (0 min).

- **DE:** Dauerhafte Benachrichtigung via Foreground Service – Timer läuft auch bei gesperrtem Bildschirm.
- **EN:** Persistent notification via Foreground Service – timer survives screen lock.

- **DE:** Alarm-Benachrichtigung + Vibration wenn die Zeit abgelaufen ist.
- **EN:** Alarm notification + vibration when time expires.

- **DE:** Android Auto-Integration: GridTemplate zur Auswahl, MessageTemplate für den Countdown.
- **EN:** Android Auto integration: GridTemplate for selection, MessageTemplate for the countdown.

---

## Requirements / Voraussetzungen

| | |
|---|---|
| Android | 8.0 (API 26) or newer |
| Android Auto | 8.x or newer (optional) |
| Permissions | `POST_NOTIFICATIONS`, `VIBRATE`, `FOREGROUND_SERVICE` |

---

## Installation

### Option A – Fertige APK / Pre-built APK

1. Download the latest APK from [Releases](../../releases).
2. Enable *Unknown Sources* on your device (Settings → Apps → Special access).
3. Install the APK file.

### Option B – Via ADB

```powershell
# Replace COM3 with your actual ADB device ID shown by: adb devices
adb install app\build\outputs\apk\debug\app-debug.apk
```

---

## Build from source / Aus dem Quellcode bauen

### Prerequisites

- **JDK 17** ([Adoptium Temurin](https://adoptium.net/))
- **Android SDK** – either via Android Studio or command-line tools
- Set `ANDROID_HOME` or `JAVA_HOME` if not already set

### Windows (PowerShell)

```powershell
# 1. Clone
git clone https://github.com/<YOUR_USERNAME>/ParkTimer.git
cd ParkTimer

# 2. Download the Gradle wrapper JAR (one-time setup)
Invoke-WebRequest `
  -Uri "https://raw.githubusercontent.com/gradle/gradle/v8.4.0/gradle/wrapper/gradle-wrapper.jar" `
  -OutFile "gradle\wrapper\gradle-wrapper.jar"

# 3. Build debug APK
.\gradlew.bat assembleDebug

# APK location:
# app\build\outputs\apk\debug\app-debug.apk
```

### Linux / macOS

```bash
git clone https://github.com/<YOUR_USERNAME>/ParkTimer.git
cd ParkTimer
curl -L -o gradle/wrapper/gradle-wrapper.jar \
  "https://raw.githubusercontent.com/gradle/gradle/v8.4.0/gradle/wrapper/gradle-wrapper.jar"
chmod +x gradlew
./gradlew assembleDebug
```

> **Tip:** Opening the project in Android Studio automatically downloads the wrapper JAR and sets up everything.

---

## Android Auto Setup

1. Install **Android Auto** on your phone (Play Store).
2. Connect your phone to a compatible head unit **or** use the Desktop Head Unit (DHU) for testing:

```bash
# In Android Studio: SDK Manager → SDK Tools → Android Auto Desktop Head Unit Emulator
# Then launch:
$ANDROID_SDK_ROOT/extras/google/auto/desktop-head-unit
```

3. In Android Auto settings, enable **Unknown sources** (Developer mode).
4. ParkTimer appears as a **Parking** app in the Android Auto launcher.

---

## ADB Install

```powershell
# List connected devices
adb devices

# Install on the first connected device
adb install -r app\build\outputs\apk\debug\app-debug.apk

# Install on a specific device (replace DEVICE_ID)
adb -s DEVICE_ID install -r app\build\outputs\apk\debug\app-debug.apk
```

---

## Project structure

```
ParkTimer/
├── app/src/main/
│   ├── java/de/parktimer/app/
│   │   ├── Constants.kt              # Shared constants
│   │   ├── MainActivity.kt           # Duration selection screen
│   │   ├── TimerActivity.kt          # Countdown screen
│   │   ├── TimerService.kt           # Foreground service (background timer)
│   │   └── automotive/
│   │       ├── ParkTimerCarAppService.kt   # Car App entry point
│   │       ├── ParkTimerSession.kt         # Car session
│   │       ├── SelectDurationScreen.kt     # GridTemplate (Auto)
│   │       └── TimerScreen.kt             # MessageTemplate (Auto)
│   └── res/
│       ├── layout/                   # XML layouts
│       ├── values/                   # Colors, strings, themes
│       └── xml/automotive_app_desc.xml
├── .github/workflows/build.yml       # CI: build APK on every push
├── build.gradle                      # Project-level Gradle
├── app/build.gradle                  # App-level Gradle
└── settings.gradle
```

---

## License

MIT – see [LICENSE](LICENSE).

---

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).
