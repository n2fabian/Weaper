# Local Sync Server Setup

The `local-server/` is a lightweight Node.js server that runs on your MacBook. It handles audio file uploads from the Android app and provides a file list for delta sync comparison.

## Prerequisites

- Node.js 18+ installed on MacBook
- Both MacBook and Android device on the same WiFi network

## Installation

```bash
cd local-server
npm install
```

## Configuration

```bash
cp .env.example .env
```

Edit `.env`:

```env
PORT=3000

# Path where REAPER reads audio files from
# This should be inside your REAPER project's media folder
UPLOAD_DIR=/Users/yourname/Documents/REAPER Media/Weaper

MAX_FILE_SIZE=52428800   # 50MB
ALLOWED_EXTENSIONS=mp3,wav,aif,aiff,flac
ALLOWED_ORIGINS=*
```

## Running the Server

```bash
# Production
npm start

# Development (auto-restart on file changes)
npm run dev
```

You should see:
```
�� Weaper Sync Server running on port 3000
📁 Upload directory: /Users/.../REAPER Media/Weaper

Endpoints:
  GET  /health       - Health check
  GET  /files        - List files with hashes
  POST /upload       - Upload a file
  DELETE /files/:name - Delete a file
```

## API Reference

### `GET /health`
Health check.
```json
{ "status": "ok", "uploadDir": "/...", "timestamp": "2024-..." }
```

### `GET /files`
Returns list of all audio files with MD5 hashes.
```json
{
  "files": [
    { "name": "kick.wav", "hash": "abc123...", "size": 102400 },
    { "name": "intro.mp3", "hash": "def456...", "size": 5242880 }
  ]
}
```

### `POST /upload`
Upload a file (multipart/form-data, field name: `file`).
```json
{ "success": true, "filename": "kick.wav", "size": 102400 }
```

### `DELETE /files/:name`
Delete a file by name.
```json
{ "success": true, "message": "kick.wav deleted" }
```

## Running as a Background Service (macOS)

To keep the server running after closing Terminal, use a launch agent:

```bash
# Use the provided script
cd /path/to/Weaper
bash scripts/start-server.sh
```

Or manually with `launchctl` — see `scripts/com.weaper.server.plist`.

## Testing

```bash
npm test
```

Tests cover file hash computation, extension validation, and route behavior.
