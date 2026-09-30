# Upgrade plan

## Current state

Score: 4/10 (was 1/10) — tested pure-Kotlin domain core and honest CI; the
Compose UI is still a placeholder and the Android build is unverified locally.

## Backlog

- P0: Confirm the `android` CI job is green (AGP 8.5 + Kotlin 2.0.21 Compose
  plugin + material dependency were fixed statically, without an Android SDK).
- P0: Replace the placeholder Home/Explore screens with UI backed by `core`
  (ViewModel + state holder calling the `core` API).
- P1: Fetch aggregated views from the popular-pages backend (Retrofit/Ktor) with a fake for tests.
- P1: Time-window picker (24h / 7d / 30d) and a pull-to-refresh list.
- P2: Compose UI tests (`androidx.compose.ui:ui-test-junit4`) for the main flow.
- P2: Pin `distributionSha256Sum` in `gradle-wrapper.properties`.

## Done in this pass

- Added `core/` (included Gradle build) with the app's domain logic and 4 unit tests.
- Committed the Gradle wrapper (8.10.2).
- Fixed Kotlin 2.0 Compose setup: `org.jetbrains.kotlin.plugin.compose`
  replaces the removed `composeOptions.kotlinCompilerExtensionVersion`; Java/Kotlin
  target 17; added `com.google.android.material` for the manifest theme.
- CI: removed `|| true`; separate `core` and `android` jobs.
- README now states what is verified and what is not.
