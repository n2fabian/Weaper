# Firebase App Distribution — Context

Project: Weaper
Firebase project: create at console.firebase.google.com, package `com.weaper`

## Distribution Groups

| Alias            | Display Name     | When Used          |
|------------------|------------------|--------------------|
| dev              | Dev              | Every build        |
| external-testers | External Testers | --release flag     |

## Auth Options

- **Option A (CI/CD):** `FIREBASE_SERVICE_ACCOUNT_FILE=/path/to/sa.json`
- **Option B (local):** `FIREBASE_TOKEN` from `firebase login:ci --no-localhost`

## Commands

```bash
# Daily build
bash scripts/build_and_ship.sh

# Release build
bash scripts/build_and_ship.sh --release

# Setup once (new environment)
DEVELOPER_EMAIL=you@example.com bash scripts/setup_testers.sh
```

## Gradle Task

```bash
./gradlew assembleRelease appDistributionUploadRelease
```

## Environment Variables

| Variable                       | Required | Description                                   |
|--------------------------------|----------|-----------------------------------------------|
| `DEPLOY_GROUP`                 | No       | Distribution group (default: `dev`)           |
| `BUILD_NUMBER`                 | No       | Release notes build tag (default: `local`)    |
| `FIREBASE_APP_ID`              | Yes*     | App ID from google-services.json              |
| `FIREBASE_TOKEN`               | One of   | CI token from `firebase login:ci`             |
| `FIREBASE_SERVICE_ACCOUNT_FILE`| One of   | Path to service account JSON                  |

*Can be hardcoded in `build.gradle.kts` after reading from `google-services.json`.
