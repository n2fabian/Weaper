# Weaper 🎵

An Android application that acts as a live performance controller for **REAPER DAW** running on a MacBook. Connect over local WiFi (XR18 mixer access point or router) and control REAPER in real time via OSC (Open Sound Control).

Designed for live band/stage use: dark UI, large buttons, low latency.

---

## Features

| Feature | Description |
|---|---|
| **Setlist Control** | Create setlists mapped to REAPER markers/regions; tap to jump & play |
| **Soundboard** | Grid of sample trigger buttons; each sends OSC to REAPER |
| **Delta File Sync** | Upload audio files to Mac over WiFi — only changed/missing files are transferred |
| **Firebase Metadata Sync** | Setlists and soundboard configs sync across devices via Firestore |
| **OSC Communication** | Low-latency UDP messages to REAPER; configurable IP and port |
| **Settings** | Configure REAPER IP, OSC port, and sync server URL from the app |

---

## Repository Structure

```
Weaper/
├── android-app/        Android Kotlin/Compose app
├── local-server/       Node.js file sync server (runs on MacBook)
├── docs/               Architecture diagrams, OSC mappings, setup guides
├── scripts/            Helper scripts (start server, etc.)
└── README.md
```

---

## Quick Start

### 1. Configure REAPER for OSC

See [docs/reaper-osc-setup.md](docs/reaper-osc-setup.md) for full instructions.

**Summary:**
- In REAPER: `Options → Preferences → Control/OSC/Web`
- Add a new OSC device
- Set **Mode**: `Configure device IP+local port`
- **Local listen port**: `8000`
- **Device IP**: your Android phone's IP on the same network
- Enable: `Allow binding messages to REAPER actions and FX parameters`

### 2. Start the Local Sync Server (MacBook)

```bash
cd local-server
cp .env.example .env
# Edit .env — set UPLOAD_DIR to your REAPER media folder
npm install
npm start
```

The server will start on port 3000 by default.
See [docs/local-server-setup.md](docs/local-server-setup.md).

### 3. Build & Run the Android App

```bash
cd android-app

# Copy Firebase config (get from Firebase Console)
cp google-services.json.example app/google-services.json
# Edit app/google-services.json with your real Firebase project credentials

# Build
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug
```

In the app → **Settings**:
- Set **REAPER IP** to your MacBook's local IP (e.g., `192.168.1.100`)
- Set **OSC Port** to `8000`
- Set **Sync Server URL** to `http://192.168.1.100:3000`

---

## Architecture

```
Android App (Kotlin/Compose)
├── Presentation Layer   Compose screens + ViewModels + StateFlow
├── Domain Layer         Use cases, repository interfaces, models
└── Data Layer
    ├── OscClient        UDP datagrams to REAPER (fire-and-forget, low latency)
    ├── FirebaseRepos    Firestore real-time listeners for setlist/soundboard
    ├── SyncApiClient    Retrofit HTTP to local Node.js server
    └── AppPreferences   DataStore for settings

MacBook
├── REAPER               DAW handling audio playback
├── local-server/        Node.js Express — file uploads + hash listing
└── Firebase SDK         (not needed on Mac — Firestore is cloud)

Firebase (Cloud)
└── Firestore            Metadata only (setlists, soundboard, file refs)
```

See [docs/architecture.md](docs/architecture.md) for full diagram.

---

## OSC Command Reference

| Action | OSC Address | Args |
|---|---|---|
| Play | `/action/40044` | — |
| Stop | `/action/40047` | — |
| Go to Marker N | `/marker/N` | — |
| Go to Region N | `/region/N` | — |
| Trigger Track N | `/track/N/play` | — |

See [docs/osc-mappings.md](docs/osc-mappings.md) for full list.

---

## Firebase Setup

1. Create a project at [Firebase Console](https://console.firebase.google.com)
2. Add an Android app with package name `com.weaper`
3. Download `google-services.json` → place in `android-app/app/`
4. Enable **Firestore Database** in the console
5. (Optional) Enable **Authentication**

### Firestore Collections

| Collection | Documents |
|---|---|
| `setlists` | `{ id, title, artist, markerId, regionId, autoPlay, bpm, orderIndex }` |
| `soundboard` | `{ id, label, fileName, fileHash, trackId, oscPath, color, orderIndex }` |

---

## Networking Assumptions

- Android device, MacBook, and XR18 mixer are on the **same local network**
- No internet required for OSC or file sync
- Internet required only for Firebase metadata sync

---

## Development

### Android App

```bash
cd android-app
./gradlew build          # Build all variants
./gradlew test           # Unit tests
./gradlew lint           # Lint
```

### Local Server

```bash
cd local-server
npm install
npm test                 # Jest tests
npm run dev              # Dev mode with auto-reload
```

---

## License

MIT
