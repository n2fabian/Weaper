# Firebase App Distribution

Weaper uses the Firebase App Distribution Gradle plugin to ship builds to testers.

## Quick Start

```bash
# Build and distribute to Dev group (default)
bash scripts/build_and_ship.sh

# Build and distribute to External Testers (release)
bash scripts/build_and_ship.sh --release
```

## Distribution Groups

| Group              | When Used           |
|--------------------|---------------------|
| `dev`              | Every build         |
| `external-testers` | `--release` flag    |

## One-Time Environment Setup

Run once per machine after cloning:

```bash
DEVELOPER_EMAIL=you@example.com bash scripts/setup_testers.sh
```

This creates the two distribution groups in Firebase and adds the developer to `dev`.

## Authentication

Choose one method:

- **Local:** `firebase login` (interactive browser auth)
- **CI/CD token:** `export FIREBASE_TOKEN=$(firebase login:ci --no-localhost)`
- **Service account:** `export FIREBASE_SERVICE_ACCOUNT_FILE=/path/to/sa.json`

## Prerequisites

```bash
npm install -g firebase-tools
firebase login
```

The `FIREBASE_APP_ID` is read from the `FIREBASE_APP_ID` environment variable, or
falls back to `YOUR_APP_ID`. Set it from `android-app/app/google-services.json`
(field: `.client[0].client_info.mobilesdk_app_id`).

## Gradle Integration

The `appDistributionUploadRelease` Gradle task (from the
`com.google.firebase.appdistribution` plugin v5.0.0) handles the upload.
Configuration lives in `android-app/app/build.gradle.kts` inside the `android {}`
block under `firebaseAppDistribution {}`.

See [.agent-relay/context/firebase-distribution.md](../.agent-relay/context/firebase-distribution.md)
for full environment variable reference and cross-device context.
