#!/usr/bin/env bash
set -euo pipefail

DEPLOY_GROUP="dev"
if [[ "${1:-}" == "--release" ]]; then
  DEPLOY_GROUP="external-testers"
fi
export DEPLOY_GROUP

echo "Building Weaper — target group: $DEPLOY_GROUP"

cd "$(dirname "$0")/../android-app"
./gradlew assembleRelease appDistributionUploadRelease

echo "Done. APK distributed to group: $DEPLOY_GROUP"
