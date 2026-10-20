<p align="center">
  <img src="docs/ic_launcher_playstore.png" width="120" alt="ParkAlert logo"/>
</p>

<h1 align="center">ParkAlert</h1>

<p align="center">
  Free parking timer for supermarket car parks — with Android Auto support and German / English UI.
</p>

<p align="center">
  <img src="https://img.shields.io/badge/platform-Android-brightgreen" alt="Platform: Android"/>
  <img src="https://img.shields.io/badge/min%20SDK-26%20(Android%208.0)-blue" alt="Min SDK 26"/>
  <img src="https://img.shields.io/badge/version-1.4-orange" alt="Version 1.4"/>
  <img src="https://img.shields.io/badge/status-Play%20Console%20testing-yellow" alt="Status: Play Console testing"/>
</p>

---

> **DE:** Kostenloser Parkzeit-Timer für deutsche Einzelhandelsparkplätze (Lidl, Aldi, Rewe, …) mit Android Auto-Unterstützung und mehrsprachiger Oberfläche.
>
> **EN:** Free parking timer for supermarket car parks with Android Auto support and full multilingual (German / English) UI.

**Package:** `de.parkalert.app` &nbsp;|&nbsp; **Min SDK:** 26 (Android 8.0) &nbsp;|&nbsp; **Target SDK:** 36 &nbsp;|&nbsp; **Version:** 1.4 (versionCode 14)

---

## Status

| Area | State |
|---|---|
| Core timer (phone) | Complete |
| Android Auto | Complete |
| German / English UI | Complete |
| Foreground service + notifications | Complete |
| Overtime tracking | Complete |
| GDPR / UMP consent (IAB TCF 2.2) | Complete |
| AdMob banner ads | Complete — production IDs; AdMob app review pending until store listing is linked |
| Play Store release | Uploaded to Play Console for testing — not yet public |
| Screenshots | Pending |

---

## Features

- Five quick-select buttons: **15 / 30 / 60 / 90 / 120 minutes**
- Full-screen countdown with colour feedback: Green → Yellow (10 min left) → Red (expired)
- Overtime counter after expiry with live notifications
- Timestamps for start time and expiry time shown on the timer screen
- Persistent foreground service — timer survives screen lock and app switch
- Alarm notification + vibration on expiry
- **Android Auto** integration (GridTemplate for selection + MessageTemplate for countdown) — no ads on Auto screens
- **Multilingual:** German (`de`) and English (`en`) — follows device language by default, can be overridden in Settings
- **GDPR / DSGVO-compliant** AdMob banner ads with UMP consent dialog (IAB TCF 2.2)
- In-app privacy policy (offline, localised HTML)
- Settings screen: language selection, manage consent, view privacy policy, open-source licences
- Splash screen (AndroidX SplashScreen API)

---

## Requirements

| | |
|---|---|
| Android | 8.0 (API 26) or newer |
| Android Auto | 8.x or newer (optional) |
| Permissions | `POST_NOTIFICATIONS`, `VIBRATE`, `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_SPECIAL_USE`, `RECEIVE_BOOT_COMPLETED`, `INTERNET`, `ACCESS_NETWORK_STATE`, `AD_ID` |

---

## Build from source

### Prerequisites

- **JDK 17+** (Android Studio ships with JBR 21)
- **Android SDK** — API 36, Build-Tools 36, and the **Android Automotive OS** SDK extension

### Windows

```powershell
git clone https://github.com/evilknivel/ParkAlert.git
cd ParkAlert
.\gradlew.bat assembleDebug
# APK → app\build\outputs\apk\debug\app-debug.apk

# Signed release bundle for Play Store (requires keystore.properties, see below)
.\gradlew.bat bundleRelease
# AAB → app\build\outputs\bundle\release\app-release.aab
```

### Linux / macOS

```bash
git clone https://github.com/evilknivel/ParkAlert.git
cd ParkAlert
chmod +x gradlew
./gradlew assembleDebug
```

### Install to connected device

```bash
.\gradlew.bat installDebug   # Windows
./gradlew installDebug       # Linux/macOS
```

### Release signing

Release builds read `keystore.properties` from the repository root (gitignored — never commit it or the `.jks` file):

```properties
storeFile=parkalert-release.jks
storePassword=...
keyAlias=...
keyPassword=...
```

Release builds are minified and resource-shrunk with R8 (`app/proguard-rules.pro`).

---

## Multilingual Support

The app uses Android's standard i18n system. By default the UI follows the device language; users can override it in **Settings → Language** (Automatic / Deutsch / English). The choice is stored as `selected_language` in the `parkalert_prefs` SharedPreferences and applied in `ParkAlertApplication` and in each Activity's `attachBaseContext`.

| Locale | Folder | Status |
|---|---|---|
| English (default) | `res/values/` | Complete |
| German | `res/values-de/` | Complete |
| English (explicit) | `res/values-en/` | Complete |

### Adding a new language

1. Create `app/src/main/res/values-XX/strings.xml` (replace `XX` with the BCP 47 tag, e.g. `fr`).
2. Copy `res/values-de/strings.xml` as a starting point and translate each string.
3. Create a localised privacy policy: `app/src/main/assets/privacy_policy_XX.html`.
4. Add the filename to your locale strings:
   ```xml
   <string name="privacy_policy_asset">privacy_policy_XX.html</string>
   ```
5. Add the language to the selection dialog in `SettingsActivity` (`showLanguageDialog()`).
6. Build and test on a device or emulator set to that language.

---

## AdMob Setup

Production AdMob IDs are set in `app/src/main/res/values/strings.xml`:

| String resource | Used for |
|---|---|
| `admob_app_id` | AdMob app (manifest `APPLICATION_ID`) |
| `admob_banner_main` | Banner on the duration-selection screen |
| `admob_banner_timer` | Banner on the countdown screen |

Debug builds still get **test ads**: the test devices are registered via `RequestConfiguration` in `MainActivity` / `TimerActivity` (`BuildConfig.DEBUG` only). Never tap real ads on your own device in a release build.

After the app is live in the Play Store:
1. Link the AdMob app to the store listing (AdMob → App settings) so AdMob can review it.
2. Make sure the GDPR message is published (AdMob → Privacy & messaging → GDPR); without it UMP shows no consent form and no ads load in the EEA.

Google's test IDs for development: app `ca-app-pub-3940256099942544~3347511913`, banner `ca-app-pub-3940256099942544/6300978111`.

---

## GDPR / DSGVO Compliance

- First launch in the EEA: UMP consent dialog (IAB TCF 2.2) shown **before** any ad loads.
- Consent denied → non-personalised ads only. App fully functional without consent.
- Consent can be changed at any time: gear icon → **Privacy & Consent**.
- Offline privacy policies: `assets/privacy_policy_de.html` and `assets/privacy_policy_en.html`.
- No ads on Android Auto (Google policy).

See [`TESTING.md`](TESTING.md) for the full GDPR compliance checklist.

---

## Android Auto

1. Install **Android Auto** on your phone (Play Store).
2. Use the Desktop Head Unit (DHU) for testing:
   ```bash
   $ANDROID_SDK_ROOT/extras/google/auto/desktop-head-unit
   ```
3. Enable *Unknown sources* in Android Auto developer settings.
4. ParkAlert appears in the Auto launcher (Car App category `androidx.car.app.category.IOT`).

---

## Play Store Publishing Checklist

- [x] Replace test AdMob IDs in `res/values/strings.xml`
- [ ] Link AdMob app to the Play Store listing after approval
- [ ] Configure GDPR message in AdMob Console (Privacy & messaging → GDPR)
- [ ] Add privacy policy URL in Play Console (App content → Privacy policy)
- [ ] Complete Play Console **Data safety** form (AdMob: device ID, usage data, approx. location)
- [ ] Test consent dialog with EEA geography simulation (see `TESTING.md`)
- [ ] Verify banner ads load on main screen and timer screen
- [ ] Verify no ads appear in Android Auto
- [ ] Test German UI on `de` device / emulator
- [ ] Test English UI on `en` device / emulator
- [ ] Add screenshots to Play Store listing
- [ ] Upload Play Store metadata from `playstore-metadata/` folder

See also [`RELEASE_CHECKLIST.md`](RELEASE_CHECKLIST.md).

---

## Project Structure

```
ParkAlert/
├── app/src/main/
│   ├── assets/
│   │   ├── privacy_policy_de.html         # German GDPR privacy policy (offline)
│   │   └── privacy_policy_en.html         # English privacy policy (offline)
│   ├── java/de/parkalert/app/
│   │   ├── ConsentManager.kt              # UMP / GDPR consent wrapper
│   │   ├── Constants.kt                   # Shared constants
│   │   ├── MainActivity.kt                # Duration selection + consent + banner ad
│   │   ├── TimerActivity.kt               # Countdown screen + banner ad
│   │   ├── TimerService.kt                # Foreground service (background timer)
│   │   ├── SettingsActivity.kt            # Settings: consent, privacy policy, licences
│   │   ├── PrivacyPolicyActivity.kt       # In-app localised privacy policy WebView
│   │   └── automotive/
│   │       ├── ParkAlertCarAppService.kt  # Car App entry point
│   │       ├── ParkAlertSession.kt        # Car session
│   │       ├── SelectDurationScreen.kt    # GridTemplate (Auto) — no ads
│   │       └── TimerScreen.kt             # MessageTemplate (Auto) — no ads, localised
│   └── res/
│       ├── drawable/                      # ic_parking.xml, ic_settings.xml, launcher vectors
│       ├── layout/                        # XML layouts for all 4 activities
│       ├── mipmap-{mdpi,hdpi,xhdpi,xxhdpi,xxxhdpi}/  # Static PNG launcher icons
│       ├── values/                        # Default strings (English) + AdMob IDs + colours
│       ├── values-de/                     # German string overrides
│       ├── values-en/                     # Explicit English strings
│       └── xml/automotive_app_desc.xml
├── docs/                                  # GitHub Pages site (online privacy policy)
│   ├── index.md                           # Privacy policy DE / EN
│   └── ic_launcher_playstore.png          # 512×512 Play Store icon
├── playstore-metadata/
│   ├── de-DE/                             # German Play Store listing
│   └── en-US/                             # English Play Store listing
├── TESTING.md                             # GDPR + AdMob test checklist
├── RELEASE_CHECKLIST.md                   # Play Store submission checklist
├── .github/workflows/build.yml            # CI: builds ParkAlert-debug APK
├── .github/workflows/pages.yml            # CI: deploys docs/ to GitHub Pages
├── build.gradle                           # Project-level Gradle
├── app/build.gradle                       # App-level (namespace: de.parkalert.app)
└── settings.gradle                        # rootProject.name = "ParkAlert"
```

---

## License

MIT — see [LICENSE](LICENSE).

---

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).
