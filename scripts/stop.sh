#!/bin/bash

# 停止脚本
# 用法：
#   ./scripts/stop.sh [jar文件名]
# 示例：
#   ./scripts/stop.sh max-demands-1.0.0.jar

JAR_NAME=${1:-max-demands-1.0.0.jar}

echo "========================================"
echo "Max 需求管理系统 - 停止脚本"
echo "JAR: $JAR_NAME"
echo "========================================"

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
echo "========================================"
