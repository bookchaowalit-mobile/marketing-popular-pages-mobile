# Upgrade plan

## Current state

Score: 5/10 (was 4/10) — tested pure-Kotlin domain core and honest CI; the
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

## Done in this pass (pass 1)

- Added `core/` (included Gradle build) with the app's domain logic and 4 unit tests.
- Committed the Gradle wrapper (8.10.2).
- Fixed Kotlin 2.0 Compose setup: `org.jetbrains.kotlin.plugin.compose`
  replaces the removed `composeOptions.kotlinCompilerExtensionVersion`; Java/Kotlin
  target 17; added `com.google.android.material` for the manifest theme.
- CI: removed `|| true`; separate `core` and `android` jobs.
- README now states what is verified and what is not.

## Done in this pass (pass 2)

- Fixed `normalizePath`: for URLs with no path, a `/` inside the query or fragment (`https://x.com?next=/admin`) became the page path; protocol-relative URLs (`//cdn.x.com/blog`) kept the host as a path segment.
- 6 more edge-case tests (10 total): URL/host corners, empty and slash-only paths, half-open windows, trending with new/vanished pages, compact-number boundaries. Verified with `./gradlew -p core test --offline`.
- Still not verified locally: the Compose `app/` (no Android SDK; dl.google.com is blocked here).

## Done in this pass (pass 3)

- Fixed `normalizePath` for relative paths carrying a URL: `/login?next=https://example.com/admin` was grouped as `/admin` (any `://` was taken as the scheme separator); only a leading `scheme://` now counts.
- `compact` gained a `B` suffix: counts from 999,950,000 showed as "1000M" / "2147.5M" (the pass-2 pin was updated to "2.1B").
- 3 tests (13 total; 2 fail on the old code), verified with `./gradlew -p core test --offline`. Half-up rounding of `compact` was brute-force checked against integer maths for 1K..3M (no float drift).
