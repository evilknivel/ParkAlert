# ParkAlert Release Checklist

## Before submitting to Play Store:

### App
- [ ] `versionCode` / `versionName` in `app/build.gradle` bumped (current: 12 / 1.3) — must be higher than the last upload to Play Console
- [ ] `changelog.txt` updated in `playstore-metadata/de-DE/` and `playstore-metadata/en-US/`
- [ ] Signed AAB built: `.\gradlew.bat bundleRelease` (requires `keystore.properties`)
- [ ] `TESTING.md` checklist completed
- [x] Real AdMob App ID in strings.xml
- [x] Real AdMob Banner IDs in strings.xml (`admob_banner_main`, `admob_banner_timer`)
- [ ] AdMob test-device IDs in `MainActivity` / `TimerActivity` still wrapped in `BuildConfig.DEBUG` (or removed)
- [ ] `play-services-ads` and `user-messaging-platform` updated to latest stable (TODOs in `app/build.gradle`)
- [ ] Privacy policy URL: https://evilknivel.github.io/parkalert/
- [ ] Tested on real device (Samsung Galaxy S20 FE)
- [ ] Android Auto tested via DHU

### Play Store
- [ ] App icon 512x512 (ic_launcher_playstore.png)
- [ ] Screenshots minimum 2
- [ ] Short description DE (max 80 chars)
- [ ] Short description EN (max 80 chars)
- [ ] Full description DE (max 4000 chars)
- [ ] Full description EN (max 4000 chars)
- [ ] Privacy policy URL entered
- [ ] Content rating completed
- [ ] Target audience: 18+
- [ ] AAB uploaded

### Legal
- [ ] Gewerbe angemeldet Hamburg
- [ ] AdMob account verified
- [ ] AdMob payment details (Bankverbindung)
