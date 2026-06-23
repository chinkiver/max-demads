#!/bin/bash
set -e

DEPLOY_DIR=${1:-/opt/max-demands}
JAR_NAME=${2:-max-demands.jar}
APP_NAME=${3:-max-demands}

cd "$DEPLOY_DIR"

# 检查是否已在运行
PID=$(ps -ef | grep "$JAR_NAME" | grep -v grep | awk '{print $2}' | head -1)
if [ -n "$PID" ]; then
    echo "应用已在运行，PID: $PID"
    exit 0
fi

# 启动应用
nohup java -jar "$DEPLOY_DIR/$JAR_NAME" --spring.profiles.active=prod > "$DEPLOY_DIR/logs/$APP_NAME.log" 2>&1 &

sleep 3

# 检查启动状态
NEW_PID=$(ps -ef | grep "$JAR_NAME" | grep -v grep | awk '{print $2}' | head -1)
if [ -n "$NEW_PID" ]; then
    echo "应用启动成功，PID: $NEW_PID"
else
    echo "应用启动失败，请检查日志"
    exit 1
fi
