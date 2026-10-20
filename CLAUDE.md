# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

ParkAlert (repo folder `ParkTimer`) — Android parking timer for supermarket car parks, with Android Auto support, German/English UI, and AdMob banner ads behind GDPR/UMP consent. Single module `:app`, package/namespace `de.parkalert.app`, Kotlin only (no Java sources), ViewBinding for all layouts (no `findViewById`).

Version, SDK levels and dependencies live in `app/build.gradle` (currently compile/target SDK 36, min SDK 26; AGP 8.11, Gradle 8.13) — the single source of truth. When bumping the version, also update the version mentions in `README.md` and `RELEASE_CHECKLIST.md`.

The privacy policy exists three times and must stay in sync: `docs/index.md` (GitHub Pages, auto-deployed by `pages.yml` on push to `master`/`main`), `app/src/main/assets/privacy_policy_de.html` and `privacy_policy_en.html` (offline in-app).

## Commands

Windows (primary dev environment):

```powershell
.\gradlew.bat assembleDebug     # APK → app\build\outputs\apk\debug\app-debug.apk
.\gradlew.bat installDebug      # install on connected device/emulator
.\gradlew.bat bundleRelease     # signed AAB for Play Store (needs keystore.properties)
.\gradlew.bat lint
```

- The system `java` on this machine is JDK 25, which Gradle 8.13 cannot run on ("Unsupported class file major version 69"). Use Android Studio's JBR 21: in Bash, `export JAVA_HOME="/c/Program Files/Android/Android Studio/jbr"` before calling `./gradlew.bat`.
- No tests exist yet (`junit` is declared, but there is no `app/src/test`). Once added: `.\gradlew.bat test --tests "de.parkalert.app.SomeTest"`.
- Release signing reads `keystore.properties` (`storeFile`, `storePassword`, `keyAlias`, `keyPassword`) at the repo root. It and `*.jks` are gitignored — never commit them.
- Release builds use R8 (`minifyEnabled` + `shrinkResources`); check `app/proguard-rules.pro` when adding reflection-based or Car App code.
- CI (`.github/workflows/build.yml`) only runs `assembleDebug` on push/PR to `main`/`master`. `pages.yml` publishes `docs/` (the privacy policy site) to GitHub Pages.
- Android Auto testing: run the Desktop Head Unit (`$ANDROID_SDK_ROOT/extras/google/auto/desktop-head-unit`) with "Unknown sources" on in Android Auto developer settings.

## Architecture

**Timer core: one service, two UIs.** `TimerService` is a foreground service (`foregroundServiceType="specialUse"`) that owns the countdown (`CountDownTimer`, 1 s tick), switches to an overtime counter (`Handler` loop) after expiry, and posts the notifications and the alert vibration. It is started with `EXTRA_DURATION_MINUTES` by both the phone UI (`MainActivity`) and the Auto UI (`automotive/TimerScreen`), and stopped via `stopService`.

**State and communication** (see `Constants.kt`):
- `TimerState` is a process-wide `@Volatile` singleton (`isRunning`, `durationMinutes`, `remainingMinutes`). UIs read it to decide what to show, e.g. `MainActivity.onResume` jumps to `TimerActivity` if a timer is running.
- Live updates go out as the app-internal broadcast `ACTION_TIMER_UPDATE` with extras `EXTRA_REMAINING_MILLIS`, `EXTRA_TIMER_STATE`, `EXTRA_START_TIME_MILLIS`, `EXTRA_END_TIME_MILLIS`. Receivers (`TimerActivity`, `SelectDurationScreen`, `TimerScreen`) register with `ContextCompat.registerReceiver(..., RECEIVER_NOT_EXPORTED)`; Auto screens unregister in a lifecycle `onDestroy` observer.
- Timer states: `RUNNING` → `WARNING` (≤ 10 min, `WARNING_THRESHOLD_MILLIS`) → `ALERT` (expired, overtime) / `STOPPED`. Each maps to green/yellow/red in the UI.

**Android Auto** (`automotive/`): `ParkAlertCarAppService` → `ParkAlertSession` → `SelectDurationScreen` (GridTemplate) → `TimerScreen` (MessageTemplate). Declared under the `androidx.car.app.category.IOT` category. **Never show ads on Auto screens** (Google policy).

**Consent and ads:** `ConsentManager` (singleton wrapping UMP / IAB TCF 2.2) must resolve before `MobileAds.initialize()` or any ad request. `MainActivity` gathers consent and then initialises ads once (`AtomicBoolean` guard). `TimerActivity` loads its banner only when `canRequestAds` is true (skipped in `BuildConfig.DEBUG`). Debug builds register test devices via `BuildConfig.DEBUG` guards, which is why `buildConfig true` is enabled. Production AdMob IDs live in `res/values/strings.xml` (`admob_app_id`, `admob_banner_main`, `admob_banner_timer`); debug builds still get test ads through the registered test devices. Don't replace them with Google's test IDs for release.

**Localisation:** default strings are English (`res/values/`), with `values-de/` and `values-en/` overrides. The user can force a language in Settings, stored as `selected_language` (`auto`/`de`/`en`) in SharedPreferences `parkalert_prefs`. It is applied in `ParkAlertApplication` (main process only, since the Car App service can run in another process context) **and** in each Activity's `attachBaseContext` override. A new Activity must copy that override. Every localised privacy policy is an offline asset (`assets/privacy_policy_XX.html`) whose file name comes from the `privacy_policy_asset` string. All UI text belongs in `strings.xml`; never hardcode it.

## Conventions

- Version bumps edit `versionCode`/`versionName` in `app/build.gradle` and are committed as `chore: bump version to X.Y (versionCode N)`. Play Store listing text lives in `playstore-metadata/{de-DE,en-US}/` (update `changelog.txt` per release).
- Work happens on `dev`; `master` is the main/PR branch.
- Before a release, go through `TESTING.md` (consent flow, ad loading, no ads on Auto, DE/EN UI) and `RELEASE_CHECKLIST.md`.
- From CONTRIBUTING.md: keep Activities/Services thin, and leave no `Log.d` in production code paths.
