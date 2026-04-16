# 🅿 ParkAlert

> **DE:** Kostenloser Parkzeit-Timer für deutsche Einzelhandelsparkplätze (Lidl, Aldi, Rewe, …) mit Android Auto-Unterstützung und mehrsprachiger Oberfläche.
>
> **EN:** Free parking timer for supermarket car parks with Android Auto support and full multilingual (German / English) UI.

**Package:** `de.parkalert.app` &nbsp;|&nbsp; **Min SDK:** 26 (Android 8.0)

---

## Screenshots / Vorschau

| Start | Timer | Warning (10 min) | Alert (0 min) |
|:-:|:-:|:-:|:-:|
| *(Startscreen)* | *(Running)* | *(Warning)* | *(Alert)* |

---

## Features / Funktionen

- Five quick-select buttons: **15 / 30 / 60 / 90 / 120 minutes**
- Full-screen countdown with colour feedback: Green → Yellow (10 min) → Red (expired)
- Persistent foreground service – timer survives screen lock
- Alarm notification + vibration on expiry
- **Android Auto** integration (GridTemplate + MessageTemplate) – no ads on Auto screens
- **Multilingual:** German (`de`) and English (`en`) – follows device language automatically
- **GDPR / DSGVO-compliant** AdMob banner ads with UMP consent dialog (IAB TCF 2.2)
- In-app privacy policy (offline, localised HTML)
- Settings screen: manage consent, view privacy policy, open source licenses

---

## Requirements / Voraussetzungen

| | |
|---|---|
| Android | 8.0 (API 26) or newer |
| Android Auto | 8.x or newer (optional) |
| Permissions | `POST_NOTIFICATIONS`, `VIBRATE`, `FOREGROUND_SERVICE`, `INTERNET`, `AD_ID` |

---

## Installation

### Option A – Pre-built APK

1. Download the latest APK from [Releases](../../releases).
2. Enable *Unknown Sources* on your device (Settings → Apps → Special access).
3. Install the APK file.

### Option B – Via ADB

```bash
adb install app/build/outputs/apk/debug/app-debug.apk
```

---

## Build from source

### Prerequisites

- **JDK 17** ([Adoptium Temurin](https://adoptium.net/))
- **Android SDK** (via Android Studio or command-line tools)

### Windows (PowerShell)

```powershell
git clone https://github.com/<YOUR_USERNAME>/ParkAlert.git
cd ParkAlert

# Download Gradle wrapper JAR (one-time)
Invoke-WebRequest `
  -Uri "https://raw.githubusercontent.com/gradle/gradle/v8.4.0/gradle/wrapper/gradle-wrapper.jar" `
  -OutFile "gradle\wrapper\gradle-wrapper.jar"

.\gradlew.bat assembleDebug
# APK: app\build\outputs\apk\debug\app-debug.apk
```

### Linux / macOS

```bash
git clone https://github.com/<YOUR_USERNAME>/ParkAlert.git
cd ParkAlert
curl -L -o gradle/wrapper/gradle-wrapper.jar \
  "https://raw.githubusercontent.com/gradle/gradle/v8.4.0/gradle/wrapper/gradle-wrapper.jar"
chmod +x gradlew
./gradlew assembleDebug
```

---

## Multilingual Support

The app uses Android's standard i18n system – no manual language switcher needed.
The UI language follows the device language automatically.

| Locale | Folder | Status |
|---|---|---|
| English (default) | `res/values/` | ✅ Complete |
| German | `res/values-de/` | ✅ Complete |
| English (explicit) | `res/values-en/` | ✅ Complete (same as default) |

### Adding a new language

1. Create `app/src/main/res/values-XX/strings.xml` (replace `XX` with the BCP 47 language tag, e.g. `fr` for French).
2. Copy `res/values-de/strings.xml` as a starting point and translate each string.
3. Create a localised privacy policy: `app/src/main/assets/privacy_policy_XX.html`.
4. Add the filename to `values-XX/strings.xml`:
   ```xml
   <string name="privacy_policy_asset">privacy_policy_XX.html</string>
   ```
5. Build and test on a device/emulator set to that language.

---

## AdMob Setup

### Development (Test IDs – already configured)

| String resource | Current (test) value |
|---|---|
| `admob_app_id` | `ca-app-pub-3940256099942544~3347511913` |
| `admob_banner_main` | `ca-app-pub-3940256099942544/6300978111` |
| `admob_banner_timer` | `ca-app-pub-3940256099942544/6300978111` |

### Switching to Real IDs (before publishing)

1. Create an [AdMob account](https://admob.google.com) and register the app with package `de.parkalert.app`.
2. Create a **Banner** ad unit (or two – one per screen).
3. In `app/src/main/res/values/strings.xml`, replace:

```xml
<string name="admob_app_id" translatable="false">ca-app-pub-YOUR_APP_ID</string>
<string name="admob_banner_main" translatable="false">ca-app-pub-YOUR_BANNER_ID</string>
<string name="admob_banner_timer" translatable="false">ca-app-pub-YOUR_BANNER_ID</string>
```

---

## GDPR / DSGVO Compliance

- First launch in the EEA: UMP consent dialog (IAB TCF 2.2) shown **before** any ad loads.
- Consent denied → non-personalised ads only. App fully functional.
- Consent can be changed anytime: gear icon → **Privacy & Consent**.
- Offline German privacy policy: `assets/privacy_policy_de.html`.
- Offline English privacy policy: `assets/privacy_policy_en.html`.
- No ads on Android Auto (Google policy).

See [`TESTING.md`](TESTING.md) for the full GDPR compliance checklist.

---

## Android Auto Setup

1. Install **Android Auto** on your phone (Play Store).
2. Use the Desktop Head Unit (DHU) for testing:
   ```bash
   $ANDROID_SDK_ROOT/extras/google/auto/desktop-head-unit
   ```
3. Enable *Unknown sources* in Android Auto developer settings.
4. ParkAlert appears as a **Parking** app in the Auto launcher.

---

## Play Store Publishing Checklist

- [ ] Replace test AdMob IDs in `res/values/strings.xml`
- [ ] Configure GDPR message in AdMob Console (Privacy & messaging → GDPR)
- [ ] Add privacy policy URL in Play Console (App content → Privacy policy)
- [ ] Complete Play Console **Data safety** form (AdMob: device ID, usage data, approx. location)
- [ ] Test consent dialog with EEA geography simulation (see `TESTING.md`)
- [ ] Verify banner ads load on main screen and timer screen
- [ ] Verify no ads appear in Android Auto
- [ ] Test German UI on `de` device / emulator
- [ ] Test English UI on `en` device / emulator
- [ ] Upload Play Store metadata from `playstore-metadata/` folder

---

## Project Structure

```
ParkAlert/
├── app/src/main/
│   ├── assets/
│   │   ├── privacy_policy_de.html        # German GDPR privacy policy (offline)
│   │   └── privacy_policy_en.html        # English privacy policy (offline)
│   ├── java/de/parkalert/app/
│   │   ├── ConsentManager.kt             # UMP / GDPR consent wrapper
│   │   ├── Constants.kt                  # Shared constants (package: de.parkalert.app)
│   │   ├── MainActivity.kt               # Duration selection + consent + banner ad
│   │   ├── TimerActivity.kt              # Countdown screen + banner ad
│   │   ├── TimerService.kt               # Foreground service (background timer)
│   │   ├── SettingsActivity.kt           # Settings: consent, privacy policy, licenses
│   │   ├── PrivacyPolicyActivity.kt      # In-app localised privacy policy WebView
│   │   └── automotive/
│   │       ├── ParkAlertCarAppService.kt # Car App entry point (renamed)
│   │       ├── ParkAlertSession.kt       # Car session (renamed)
│   │       ├── SelectDurationScreen.kt   # GridTemplate (Auto) – no ads
│   │       └── TimerScreen.kt            # MessageTemplate (Auto) – no ads, localised
│   └── res/
│       ├── drawable/ic_settings.xml      # Gear icon
│       ├── layout/                       # XML layouts (unchanged)
│       ├── values/                       # Default strings (English) + AdMob IDs
│       ├── values-de/                    # German string overrides
│       ├── values-en/                    # Explicit English strings
│       └── xml/automotive_app_desc.xml
├── playstore-metadata/
│   ├── de-DE/                            # German Play Store listing
│   └── en-US/                            # English Play Store listing
├── TESTING.md                            # GDPR + AdMob test checklist
├── .github/workflows/build.yml           # CI: builds ParkAlert-debug APK
├── build.gradle                          # Project-level Gradle
├── app/build.gradle                      # App-level (namespace: de.parkalert.app)
└── settings.gradle                       # rootProject.name = "ParkAlert"
```

---

## License

MIT – see [LICENSE](LICENSE).

---

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).
