#!/usr/bin/env bash
# Run once per environment to create distribution groups and add testers.
# Usage: DEVELOPER_EMAIL=you@example.com bash scripts/setup_testers.sh
set -euo pipefail

: "${DEVELOPER_EMAIL:?Set DEVELOPER_EMAIL before running this script}"

echo "Creating Firebase App Distribution groups..."
firebase appdistribution:group:create --display-name "Dev" dev             2>/dev/null || echo "Group 'dev' already exists"
firebase appdistribution:group:create --display-name "External Testers" external-testers  2>/dev/null || echo "Group 'external-testers' already exists"

echo "Adding developer to dev group..."
firebase appdistribution:testers:add --group dev "$DEVELOPER_EMAIL"

# Add external testers below this line:
# firebase appdistribution:testers:add --group external-testers "tester@example.com"

echo "Setup complete."
