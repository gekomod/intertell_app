# Intertell — Android apps

Two native Android apps (Kotlin + Jetpack Compose) implemented from a Claude
Design handoff: a subscriber app (`client/`) and a field technician app
(`technician/`). Both use mock, in-memory data behind a repository interface
— see each project's own README for details on screens and how to wire in a
real backend later.

## Getting an APK

Every push to `main` triggers [`.github/workflows/build-apk.yml`](.github/workflows/build-apk.yml),
which builds both apps' debug APKs on GitHub-hosted runners (no local Android
SDK needed) and publishes them as assets on a new
[Release](../../releases) — `intertell-client-debug.apk` and
`intertell-technician-debug.apk`. Download and install directly on an Android
device (enable "install from unknown sources" for debug builds), or run
manually via the Actions tab → "Build APKs" → "Run workflow".

## Local build

```
cd client         # or technician
gradle wrapper --gradle-version 8.7   # once, generates ./gradlew
./gradlew assembleDebug
```
