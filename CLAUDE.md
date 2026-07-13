# CLAUDE.md

Guidance for AI coding agents (and humans) working in the **GeoNoise** repository.

## Project overview

GeoNoise is an **Android application, written in Java**, for measuring ambient
noise and rendering it as a geolocalised heatmap. It began as a bachelor thesis
(BEng in Telecommunications, Universidad de Málaga) and is being incrementally
modernised. The full thesis (LaTeX source, in Spanish) lives under `doc/`.

## Layout

Single Gradle module, `:app` (`settings.gradle` → `include ':app'`).

- `app/src/main/java/es/guillermoorellana/geonoise/` — application code, split by role:
  - `activity/` — `MainActivity`, `SettingsActivity`
  - `fragment/` — screen fragments (`RecordFragment`, `MapsFragment`, `NavigationDrawerFragment`, `FilePickerDialogFragment`, `PastSessionFragment`)
  - `service/` — background services (`RecordService`, `LocationService`)
  - `utils/` — helpers and pure logic (`RecordAudioPermission`, `Utils`, `AsyncHeatmapFileLoader`, `HeatmapCapable`, `LocationNoiseUpdatesListener`)
- `app/src/main/res/` — layouts, strings (with `values-es/` Spanish translations), preferences, drawables
- `app/src/test/java/` — JVM unit tests (JUnit 4)
- `doc/` — thesis LaTeX sources and figures
- `screenshots/` — README imagery

## Build system

Build only through the **Gradle wrapper** (`./gradlew`) — never a system-installed Gradle.

- Android Gradle Plugin **8.5.0** (root `build.gradle`), Gradle **8.7** (`gradle/wrapper/gradle-wrapper.properties`).
- `compileSdk 34`, `minSdkVersion 21`, `targetSdkVersion 34` (`app/build.gradle`). `targetSdk` can't move up without reworking the permissions model.
- Java source/target is 8 (you'll see an "obsolete source/target 8" javac warning — expected, not an error).

Common commands (run from the repo root):

| Command | What it does |
| --- | --- |
| `./gradlew test` | Run all JVM unit tests (`:app:testDebugUnitTest` + release variant). |
| `./gradlew :app:testDebugUnitTest` | Run the debug-variant unit tests only. |
| `./gradlew lint` | Android Lint over the module. |
| `./gradlew assembleDebug` | Build the debug APK. |
| `./gradlew build` | Full build (assemble + lint + test). |

Test reports land in `app/build/reports/tests/testDebugUnitTest/index.html`.

### Local-only files needed to build

Two files are **git-ignored to avoid leaking secrets** (`.gitignore` ignores
`google_maps_api.xml`; commit `b0841d5` untracked them). A checkout will not
contain them, and a build that touches resources fails with
`resource string/google_maps_key ... not found` until you provide them:

- `app/src/debug/res/values/google_maps_api.xml`
- `app/src/release/res/values/google_maps_api.xml`

Each must define a `google_maps_key` string. For local builds a placeholder is
enough (maps just won't render); a real Google Maps API key is needed for maps
to work. `local.properties` (holding `sdk.dir`) is likewise git-ignored and
per-machine — point it at your Android SDK.

## Testing conventions

- **Framework:** JUnit 4 (`testImplementation 'junit:junit:4.13.2'` in `app/build.gradle`), JVM unit tests under `app/src/test/java/…` mirroring the production package.
- **Test-first for new behaviour.** Write a failing test that pins the behaviour, watch it fail for the right reason, then write the minimum code to pass, then refactor on green. Exceptions: pure docs/config/metadata changes with no behaviour to assert.
- **Prefer a pure decision core.** Android framework calls are hard to unit-test on the JVM, so factor the *decision* out of the framework glue. The reference example is
  `utils/RecordAudioPermission.java` (added under GN-2): its `onStartPressed`,
  `onRequestResult`, and `canStartAudio` methods take only primitives and return an
  `Outcome` enum / boolean — no Android calls — so `RecordAudioPermissionTest`
  exercises every branch without a device. A thin wrapper (`isGranted(Context)`)
  feeds live framework state into that pure core. Follow this seam for new logic.

## Coding conventions

- **Java**, Android/AndroidX. Do not introduce Kotlin or new build tooling as part of an unrelated change.
- Follow the existing package-by-role structure above; keep pure logic in `utils/` and framework glue thin.
- Match the style of surrounding code. Keep dependency changes deliberate and scoped to the task that needs them.

## Commit conventions

- One logical change per commit; commit only the files that change touches (never `git add -A`).
- Subject format: `[GN-<n>] <scope>: <subject>`, e.g.
  `[GN-2] utils: add RecordAudioPermission decision core`. `<n>` is the Linear
  issue number; `<scope>` is the area touched (`utils`, `service`, `record`, …).
- Don't bypass git hooks (`--no-verify`); fix the cause instead.

## Voluntold in this repo

This repo is worked with **voluntold** (a board-agnostic ticket runner) via the
`voluntold` MCP server. Its runtime state is intentionally kept **local and
per-machine** — `.git/info/exclude` (VOL-175) excludes it from git:

```
/.mcp.json
/.claude/settings.local.json
/.claude/agent-memory/
/.voluntold/
/.voluntold.json
```

Do **not** commit these or move them into the tracked `.gitignore`; they are
machine-local scaffolding, not project source. `.claude/worktrees/` holds
transient per-ticket git worktrees and is likewise local-only.
