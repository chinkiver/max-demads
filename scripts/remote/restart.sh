#!/bin/bash
set -e

DEPLOY_DIR=${1:-/opt/max-demands}
JAR_NAME=${2:-max-demands.jar}
APP_NAME=${3:-max-demands}

"$DEPLOY_DIR/scripts/stop.sh" "$DEPLOY_DIR" "$JAR_NAME"
sleep 2
"$DEPLOY_DIR/scripts/start.sh" "$DEPLOY_DIR" "$JAR_NAME" "$APP_NAME"
