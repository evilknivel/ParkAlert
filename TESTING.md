# ParkAlert – GDPR / AdMob Testing Checklist

Use this checklist before every release to verify GDPR compliance,
AdMob integration, and multilingual behaviour.

---

## 1. Consent Flow

- [ ] **First launch**: Consent dialog (UMP / IAB TCF 2.2) appears **before** any ad
      is loaded or displayed.
- [ ] **Dialog content**: Dialog lists Google as an ad partner and explains data usage.
- [ ] **Reject all**: Tapping "Reject all" results in non-personalised ads only –
      no personalised ad requests are sent.
- [ ] **Accept all**: Tapping "Accept all" results in personalised ads.
- [ ] **Second launch**: No consent dialog on subsequent launches if consent is
      already stored and still valid.
- [ ] **Consent expiry**: After clearing app data, the dialog reappears on next launch.

## 2. Ad Loading

- [ ] **No ads before consent**: With a fresh install, no `AdRequest` is fired before
      the user interacts with the consent dialog.
- [ ] **Banner on main screen**: After consent, a banner ad appears at the bottom of
      the duration-selection screen.
- [ ] **Banner on timer screen**: After consent, a banner ad appears at the bottom of
      the countdown screen.
- [ ] **No ad on Android Auto screen**: Confirm no ad UI in `SelectDurationScreen.kt`
      or `TimerScreen.kt`.
- [ ] **Test IDs in use**: Logcat shows `"Test ad"` or the test banner is visually
      identifiable. No real ads should show with the test App ID.

## 3. Settings Screen

- [ ] **Gear icon visible**: Settings button visible top-right on main screen.
- [ ] **Consent re-open**: Settings → "Privacy & Consent" (EN) / "Datenschutz &
      Einwilligung" (DE) reopens the UMP privacy options form for EEA users.
- [ ] **Privacy policy accessible**: Settings → "Privacy Policy" / "Datenschutzerklärung"
      opens the in-app privacy policy.
- [ ] **Privacy policy offline**: Policy loads without a network connection (local assets).
- [ ] **External links**: Tapping Google's privacy policy link opens the system browser.
- [ ] **App version**: Correct version string shown.
- [ ] **Licenses**: Tapping "Licenses" / "Lizenzen" shows the license dialog.

## 4. App Functionality Without Consent

- [ ] **Timer works**: Declining all consent does not break timer, notifications,
      or vibration.
- [ ] **No forced acceptance**: User can dismiss consent dialog and still use app fully.
- [ ] **No crash on denied consent**: App handles `FormError` gracefully (e.g. airplane mode).

## 5. Multilingual / i18n

- [ ] **German device**: Set device language to German (de). Verify:
  - App title "ParkAlert" shown
  - "Parkzeit auswählen" on main screen
  - "⚠️ Achtung: Noch 10 Minuten!" at warning state
  - "🚨 Zeit abgelaufen!" at alert state
  - "Verbleibende Zeit" label below countdown
  - "Zurück" on reset button
  - "Einstellungen" as settings title
  - "Datenschutzerklärung" privacy policy title
  - Privacy policy loads German HTML (`privacy_policy_de.html`)
  - Notifications in German: "ParkAlert läuft – MM:SS verbleibend"
  - Android Auto strings in German

- [ ] **English device**: Set device language to English (en). Verify:
  - "Select parking duration" on main screen
  - "⚠️ Warning: 10 minutes left!" at warning state
  - "🚨 Time is up!" at alert state
  - "Remaining Time" label below countdown
  - "Reset" on reset button
  - "Settings" as settings title
  - "Privacy Policy" title
  - Privacy policy loads English HTML (`privacy_policy_en.html`)
  - Notifications in English: "ParkAlert running – MM:SS remaining"
  - Android Auto strings in English

- [ ] **Language switch**: Change device language while app is in background and
      reopen – UI should reflect the new language immediately.

### How to test language on Android Emulator

```bash
# Switch to German
adb shell am start -a android.settings.LOCALE_SETTINGS
# Or via Settings → General management → Language → Add language → Deutsch

# Switch to English
# Settings → General management → Language → English
```

Or in Android Studio AVD, go to Settings → System → Language & input → Languages.

## 6. Android Auto

- [ ] **Strings localised**: On a German device, Android Auto shows German strings
      ("Verbleibende Parkzeit", "🚨 Parkzeit abgelaufen!" etc.).
- [ ] **Timestamps localised**: "Gestartet: HH:MM Uhr" (DE) vs "Started: HH:MM" (EN).
- [ ] **Stop button**: "Stopp" (DE) / "Stop" (EN).

## 7. Pre-Release Checklist (Before Publishing to Play Store)

- [ ] Replace test App ID: `admob_app_id` in `res/values/strings.xml`.
- [ ] Replace test banner IDs: `admob_banner_main` and `admob_banner_timer`.
- [ ] Privacy policy URLs updated if you host them online.
- [ ] AdMob account linked with package `de.parkalert.app`.
- [ ] Play Console → App content → Privacy policy URL set.
- [ ] Play Console Data safety form completed for AdMob.
- [ ] AdMob Console → Privacy & messaging: GDPR message configured for your app.
- [ ] Play Store metadata uploaded from `playstore-metadata/` (de-DE and en-US).
- [ ] App tested on real device in German and English.

---

## Testing Tips

### Force the consent dialog to reappear

Clear app data: Settings → Apps → ParkAlert → Storage → Clear Data.

Or in code (debug builds only):
```kotlin
UserMessagingPlatform.getConsentInformation(context).reset()
```

### Simulate EEA device (for non-EEA developers)

Temporarily add debug settings to `ConsentManager.gatherConsent()`:
```kotlin
val debugSettings = ConsentDebugSettings.Builder(activity)
    .setDebugGeography(ConsentDebugSettings.DebugGeography.DEBUG_GEOGRAPHY_EEA)
    .addTestDeviceHashedId("YOUR_DEVICE_HASH") // from logcat
    .build()

val params = ConsentRequestParameters.Builder()
    .setConsentDebugSettings(debugSettings)
    .setTagForUnderAgeOfConsent(false)
    .build()
```

### Verify non-personalised ads

With Charles Proxy or similar, intercept the AdMob request and confirm:
- Consent denied: request contains appropriate TCF consent string indicating no personalisation.
- Consent accepted: request contains a valid TCF consent string.
