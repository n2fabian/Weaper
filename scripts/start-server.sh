#!/bin/bash
# Weaper Local Sync Server — Start Script
# Starts the Node.js sync server in the background with logging

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
SERVER_DIR="$SCRIPT_DIR/../local-server"
LOG_FILE="$SCRIPT_DIR/../local-server/server.log"
PID_FILE="$SCRIPT_DIR/../local-server/server.pid"

echo "🎵 Starting Weaper Sync Server..."

# Check Node.js is installed
if ! command -v node &>/dev/null; then
  echo "❌ Node.js not found. Install from https://nodejs.org"
  exit 1
fi

# Check dependencies installed
if [ ! -d "$SERVER_DIR/node_modules" ]; then
  echo "📦 Installing dependencies..."
  cd "$SERVER_DIR" && npm install
fi

# Stop any existing server
if [ -f "$PID_FILE" ]; then
  OLD_PID=$(cat "$PID_FILE")
  if kill -0 "$OLD_PID" 2>/dev/null; then
    echo "Stopping existing server (PID $OLD_PID)..."
    kill "$OLD_PID"
    sleep 1
  fi
  rm -f "$PID_FILE"
fi

# Start server
cd "$SERVER_DIR"
nohup node src/index.js > "$LOG_FILE" 2>&1 &
SERVER_PID=$!
echo $SERVER_PID > "$PID_FILE"

sleep 2

if kill -0 "$SERVER_PID" 2>/dev/null; then
  echo "✅ Server started (PID $SERVER_PID)"
  echo "📋 Log: $LOG_FILE"
  echo "🌐 URL: http://localhost:${PORT:-3000}"
else
  echo "❌ Server failed to start. Check log: $LOG_FILE"
  exit 1
fi
