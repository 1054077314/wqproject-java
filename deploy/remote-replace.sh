#!/usr/bin/env bash
set -euo pipefail

APP_NAME="${APP_NAME:-campus-share}"
REMOTE_ROOT="${REMOTE_ROOT:-/opt/campus-share}"
SERVICE_NAME="${SERVICE_NAME:-campus-share}"
JAR_NAME="${JAR_NAME:-campus-share-1.0.0.jar}"

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
echo "remote replace done"
