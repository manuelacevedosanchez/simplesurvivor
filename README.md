# SimpleSurvivor

A [libGDX](https://libgdx.com/) project generated with [gdx-liftoff](https://github.com/libgdx/gdx-liftoff).

This project was generated with a Kotlin project template that includes Kotlin application launchers and [KTX](https://libktx.github.io/) utilities.

## Platforms

- `core`: Main module with the application logic shared by all platforms.
- `android`: Android mobile platform. Needs Android SDK.

## Gradle

This project uses [Gradle](https://gradle.org/) to manage dependencies.
The Gradle wrapper was included, so you can run Gradle tasks using `gradlew.bat` or `./gradlew` commands.
Useful Gradle tasks and flags:

- `--continue`: when using this flag, errors will not stop the tasks from running.
- `--daemon`: thanks to this flag, Gradle daemon will be used to run chosen tasks.
- `--offline`: when using this flag, cached dependency archives will be used.
- `--refresh-dependencies`: this flag forces validation of all dependencies. Useful for snapshot versions.
- `android:lint`: performs Android project validation.
- `build`: builds sources and archives of every project.
- `cleanEclipse`: removes Eclipse project data.
- `cleanIdea`: removes IntelliJ project data.
- `clean`: removes `build` folders, which store compiled classes and built archives.
- `eclipse`: generates Eclipse project data.
- `idea`: generates IntelliJ project data.
- `test`: runs unit tests (if any).

Note that most tasks that are not specific to a single project can be run with `name:` prefix, where the `name` should be replaced with the ID of a specific project.
For example, `core:clean` removes `build` folder only from the `core` project.

## Android Variants (`debug` and `pro`)

The `android` module now defines two main build variants you can work with:

- `debug`: internal testing build (`applicationId` becomes `es.masmultimedia.debug`, no minify, debuggable, app name `SimpleSurvivor Debug`, orange adaptive launcher icon).
- `pro`: production-oriented build (`applicationId` stays `es.masmultimedia`, minify + resource shrink enabled, app name `SimpleSurvivor Pro`, blue/gold adaptive launcher icon).

### In Android Studio

1. Open **Build Variants** tool window.
2. For module `android`, choose `debug` while developing.
3. Switch to `pro` before generating release-ready artifacts.

### Gradle Commands

```bash
./gradlew :android:assembleDebug
./gradlew :android:installDebug
./gradlew :android:runDebug

./gradlew :android:assemblePro
./gradlew :android:installPro
./gradlew :android:runPro
```

### Notes

- `debug` and `pro` can coexist on the same device because `debug` uses a different package suffix.
- `debug` and `pro` also have separate launcher names and icons, so they are easy to distinguish on-device.
- `release` still exists for compatibility, but the recommended production path is `pro`.
- You can check the active mode from `BuildConfig.IS_PRO_BUILD` if you need runtime switches.

## Play Store Preparation

This project includes several features to prepare for Play Store release:

### 1. Release Signing Configuration

1. Copy `keystore.properties.template` to `keystore.properties`
2. Generate a release keystore:
   ```bash
   keytool -genkey -v -keystore release-keystore.jks -keyalias your_alias -keyalg RSA -keysize 2048 -validity 10000
   ```
3. Fill in `keystore.properties` with your keystore details
4. Build the signed APK/AAB:
   ```bash
   ./gradlew :android:bundlePro  # For AAB (recommended for Play Store)
   ./gradlew :android:assemblePro  # For APK
   ```

### 2. Loading Screen (Async Asset Loading)

The game now uses `LoadingScreen` to load assets asynchronously, preventing ANR (Application Not Responding) issues on Android.

### 3. Audio System

- `AudioManager` - Centralized audio control with volume settings
- Add sound files to `assets/sounds/` (WAV format)
- Add music files to `assets/music/` (OGG format)
- See `assets/sounds/README.md` and `assets/music/README.md` for details

### 4. Internationalization (i18n)

Multi-language support using LibGDX I18NBundle:
- English (default): `assets/i18n/strings.properties`
- Spanish: `assets/i18n/strings_es.properties`
- Portuguese: `assets/i18n/strings_pt.properties`

Use `I18n.get("key")` or `I18n.format("key", args)` to get localized strings.

### 5. Firebase Crashlytics (Optional)

For crash reporting in production:
1. Follow instructions in `FIREBASE_SETUP.md`
2. Add `google-services.json` to `android/`
3. Uncomment Firebase plugins in `android/build.gradle`

### Play Store Checklist

Before submitting to Play Store, ensure you have:

- [ ] Release keystore created and configured
- [ ] Privacy Policy URL
- [ ] App icon (512x512 PNG)
- [ ] Feature graphic (1024x500)
- [ ] Screenshots (min 2, recommended 8)
- [ ] Short and full descriptions
- [ ] Content rating questionnaire completed
- [ ] Target audience and content declaration
- [ ] Sound effects and music added
- [ ] Tested on multiple devices/screen sizes

