#!/bin/bash

DEPLOY_DIR=${1:-/opt/max-demands}
JAR_NAME=${2:-max-demands.jar}

PID=$(ps -ef | grep "$JAR_NAME" | grep -v grep | awk '{print $2}' | head -1)
if [ -z "$PID" ]; then
    echo "应用未运行"
    exit 0
fi

echo "正在停止应用，PID: $PID..."
kill "$PID"

# 等待进程结束
for i in {1..30}; do
    if ! ps -p "$PID" > /dev/null 2>&1; then
        echo "应用已停止"
        exit 0
    fi
    sleep 1
done

# 强制结束
echo "进程未正常退出，执行强制结束..."
kill -9 "$PID" || true
echo "应用已停止"
