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

`local.properties` at the repo root is **git-ignored** (`.gitignore` ignores
`/local.properties`) and per-machine. It must at least point at your Android SDK
(`sdk.dir=...`).

The **Google Maps API key is injected at build time** (GN-13), so no key ever
lives in a checked-in resource file. `app/build.gradle`'s `resolveMapsApiKey()`
supplies the `MAPS_API_KEY` manifest placeholder (referenced from
`AndroidManifest.xml` as `${MAPS_API_KEY}`) from the first available source:

1. a `MAPS_API_KEY` entry in the repo-root `local.properties` (per-machine,
   git-ignored) — the usual path for developers;
2. the `MAPS_API_KEY` environment variable — for CI/release builds;
3. otherwise a non-secret `YOUR_MAPS_API_KEY` placeholder, so a checkout with no
   key still builds (maps just won't render).

To make maps render, add your real key to `local.properties`:

```
MAPS_API_KEY=AIza...
```

The old per-checkout `app/src/{debug,release}/res/values/google_maps_api.xml`
files are no longer used or needed.

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
