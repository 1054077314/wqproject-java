#!/usr/bin/env bash
set -euo pipefail

# Build and deploy campus-share to a Linux ECS host.
# Required: ssh/scp access to DEPLOY_HOST, local JDK 17+, Maven and Node.js.

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

DEPLOY_HOST="${DEPLOY_HOST:-root@120.26.174.97}"
APP_NAME="${APP_NAME:-campus-share}"
REMOTE_ROOT="${REMOTE_ROOT:-/opt/campus-share}"
SERVICE_NAME="${SERVICE_NAME:-campus-share}"
JAR_NAME="${JAR_NAME:-campus-share-1.0.0.jar}"

BACKEND_JAR="$PROJECT_ROOT/target/$JAR_NAME"
FRONTEND_ARCHIVE="$PROJECT_ROOT/target/frontend-dist.tgz"

echo "==> Build backend"
cd "$PROJECT_ROOT"
if command -v mvn >/dev/null 2>&1; then
  MVN_CMD=(mvn)
elif [ -x "$PROJECT_ROOT/mvnw" ]; then
  MVN_CMD=("$PROJECT_ROOT/mvnw")
elif [ -x "$PROJECT_ROOT/.tools/apache-maven-3.9.6/bin/mvn" ]; then
  MVN_CMD=("$PROJECT_ROOT/.tools/apache-maven-3.9.6/bin/mvn")
elif [ -f "$PROJECT_ROOT/.tools/apache-maven-3.9.6/bin/mvn.cmd" ]; then
  MVN_CMD=("$PROJECT_ROOT/.tools/apache-maven-3.9.6/bin/mvn.cmd")
else
  echo "Maven not found. Install Maven or add Maven wrapper before deploy." >&2
  exit 1
fi
"${MVN_CMD[@]}" -DskipTests package

echo "==> Build frontend"
cd "$PROJECT_ROOT/frontend"
npm run build

echo "==> Package frontend"
cd "$PROJECT_ROOT"
mkdir -p target
tar -czf "$FRONTEND_ARCHIVE" -C frontend/dist .

echo "==> Upload artifacts to $DEPLOY_HOST"
scp "$BACKEND_JAR" "$DEPLOY_HOST:/tmp/$JAR_NAME"
scp "$FRONTEND_ARCHIVE" "$DEPLOY_HOST:/tmp/${APP_NAME}-frontend-dist.tgz"

echo "==> Replace artifacts and restart service"
ssh "$DEPLOY_HOST" \
  "APP_NAME='$APP_NAME' REMOTE_ROOT='$REMOTE_ROOT' SERVICE_NAME='$SERVICE_NAME' JAR_NAME='$JAR_NAME' bash -s" <<'REMOTE'
set -euo pipefail

ts="$(date +%Y%m%d%H%M%S)"
mkdir -p "$REMOTE_ROOT/app" "$REMOTE_ROOT/frontend" "$REMOTE_ROOT/logs"

if [ -f "$REMOTE_ROOT/app/$JAR_NAME" ]; then
  cp "$REMOTE_ROOT/app/$JAR_NAME" "$REMOTE_ROOT/app/$JAR_NAME.bak.$ts"
fi

if [ -d "$REMOTE_ROOT/frontend/dist" ]; then
  cp -a "$REMOTE_ROOT/frontend/dist" "$REMOTE_ROOT/frontend/dist.bak.$ts"
fi

install -m 0644 "/tmp/$JAR_NAME" "$REMOTE_ROOT/app/$JAR_NAME"
rm -rf "$REMOTE_ROOT/frontend/dist.new"
mkdir -p "$REMOTE_ROOT/frontend/dist.new"
tar -xzf "/tmp/${APP_NAME}-frontend-dist.tgz" -C "$REMOTE_ROOT/frontend/dist.new"
rm -rf "$REMOTE_ROOT/frontend/dist"
mv "$REMOTE_ROOT/frontend/dist.new" "$REMOTE_ROOT/frontend/dist"

systemctl restart "$SERVICE_NAME"
systemctl reload nginx
systemctl is-active "$SERVICE_NAME"

rm -f "/tmp/$JAR_NAME" "/tmp/${APP_NAME}-frontend-dist.tgz"
REMOTE

echo "==> Done"
