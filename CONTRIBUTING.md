# Contributing to ParkTimer

Thank you for considering a contribution! Here is everything you need to get started.

## Development setup

1. Install **Android Studio** (Hedgehog or newer).
2. Clone the repository and open the `ParkTimer` folder in Android Studio.
3. Android Studio will sync Gradle and download all dependencies automatically.
4. Connect a physical device or start an AVD (API 26+).
5. Run `▶ Run 'app'` or use `./gradlew installDebug` from the terminal.

### Android Auto testing

Use the **Desktop Head Unit (DHU)** to simulate a car head unit on your development machine:

```
# Install the DHU via Android Studio SDK Manager → SDK Tools → Android Auto Desktop Head Unit Emulator
# Then start it:
$ANDROID_SDK_ROOT/extras/google/auto/desktop-head-unit
```

## Code style

- Kotlin only – no Java source files.
- Follow the [official Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html).
- ViewBinding for all layouts (no `findViewById`).
- Keep Activities/Services thin; business logic lives in dedicated classes.

## Pull request checklist

- [ ] The app builds without errors (`./gradlew assembleDebug`).
- [ ] The timer service runs correctly in the background.
- [ ] Android Auto screens display correctly in the DHU.
- [ ] New strings are added to `strings.xml` (no hardcoded text).
- [ ] No debug `Log.d` calls left in production code paths.
- [ ] PR description explains *what* changed and *why*.

## Reporting bugs

Please open an issue and include:

- Android version / device model
- Steps to reproduce
- Expected vs. actual behaviour
- Logcat output if available

## License

By contributing you agree that your work will be licensed under the [MIT License](LICENSE).
