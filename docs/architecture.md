# Weaper Architecture

## System Overview

```
┌─────────────────────────────────────────────────────────────┐
│                      LOCAL WIFI NETWORK                      │
│                                                              │
│  ┌──────────────────┐          ┌──────────────────────────┐  │
│  │   Android App    │          │       MacBook            │  │
│  │   (Weaper)       │          │                          │  │
│  │                  │          │  ┌────────────────────┐  │  │
│  │  ┌────────────┐  │  OSC/UDP │  │  REAPER DAW        │  │  │
│  │  │ Setlist    │──┼──────────┼──│  (port 8000)       │  │  │
│  │  │ Soundboard │  │          │  └────────────────────┘  │  │
│  │  └────────────┘  │          │                          │  │
│  │                  │  HTTP    │  ┌────────────────────┐  │  │
│  │  ┌────────────┐  │──────────┼──│  local-server      │  │  │
│  │  │ Sync UI    │  │          │  │  (port 3000)       │  │  │
│  │  └────────────┘  │          │  └────────────────────┘  │  │
│  │                  │          │                          │  │
│  └──────┬───────────┘          └──────────────────────────┘  │
│         │                                                     │
└─────────┼───────────────────────────────────────────────────┘
          │ Firebase (internet, optional)
          ▼
   ┌────────────────┐
   │  Firestore DB  │
   │  (metadata     │
   │   only)        │
   └────────────────┘
```

## Android App Layers

### Presentation Layer
- **Jetpack Compose** screens: Setlist, Soundboard, Sync, Settings
- **ViewModels** with StateFlow for reactive UI state
- **Dark theme** optimized for stage lighting conditions

### Domain Layer
- **Models**: `SetlistItem`, `SoundboardItem`, `SyncFile`, `OscCommand`
- **Repository interfaces**: `SetlistRepository`, `SoundboardRepository`, `SyncRepository`
- **Use cases**: `PlaySetlistItemUseCase`, `TriggerSoundUseCase`, `SyncFilesUseCase`

### Data Layer
- **OscClient**: Sends UDP datagrams encoding OSC 1.0 messages
- **FirebaseSetlistRepository**: Firestore real-time listener with `callbackFlow`
- **FirebaseSoundboardRepository**: Same pattern, tracks file availability
- **LocalSyncRepository**: MD5 delta sync via Retrofit to local Node.js server
- **AppPreferences**: DataStore-backed preferences (IP, port, server URL)

## Why OSC over UDP?

OSC via UDP provides:
- No connection setup (unlike TCP)
- Sub-millisecond delivery on local networks
- Fire-and-forget semantics — ideal for live performance triggers
- Native REAPER support

## Delta Sync Design

The file sync avoids unnecessary transfers:

1. Android app fetches remote file list: `GET /files` → `[{name, hash, size}]`
2. App computes local file MD5 hashes
3. Compare: files with different hash OR missing from server are flagged
4. Only flagged files are uploaded: `POST /upload`

This minimizes WiFi bandwidth use during soundcheck.

## Firebase Metadata Strategy

Audio files are **never** stored in Firebase (too large, not needed).
Only metadata is synced:
- Setlist ordering and REAPER marker mappings
- Soundboard button labels, colors, OSC paths
- File references (name + hash) for availability checks

Multiple band members can edit the setlist from their own phones,
and changes propagate in real-time via Firestore listeners.
