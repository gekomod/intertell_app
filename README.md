# Intertell — Android apps

Two native Android apps (Kotlin + Jetpack Compose) implemented from a Claude
Design handoff: a subscriber app (`client/`) and a field technician app
(`technician/`).

- **`client/`** — still mock, in-memory data behind a repository interface.
- **`technician/`** — talks to the real intratell backend's `/api/tech/*`
  JSON API (bearer-token auth; only technicians that exist in that
  database, and only while `active`, can sign in). No offline/mock mode —
  see `technician/app/src/main/java/pl/intertell/technik/data/`:
  - `TechnicianRepository` — the interface every screen depends on.
  - `api/ApiTechnicianRepository` — the (only) implementation, backed by
    plain OkHttp + `org.json` (no Retrofit/kotlinx-serialization, kept
    deliberately dependency-light since this app can't be build-verified
    locally — see below).
  - `api/ServerConfig` — DataStore-backed base URL + auth token. Default
    base URL is `https://inter.nasdom.tech` (flagged temporary by
    whoever's running it) but editable at runtime from the login screen's
    "Zmień" link, no rebuild needed.

  The backend side of this (new `/api/tech/*` routes, bearer-token
  sessions, technician CRUD, customer search) lives in the separate
  `intratell` Go server repo, not here — whoever deploys that server needs
  the updated version for the app to have anything to talk to.

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
