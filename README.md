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
