#!/bin/bash

# 服务器端停止脚本
# 在当前工作目录下执行
# 用法：
#   ./stop.sh [jar文件名]
# 示例：
#   ./stop.sh max-demands.jar

JAR_NAME=${1:-max-demands.jar}
DEPLOY_DIR=$(pwd)

echo "========================================"
echo "Max 需求管理系统 - 停止脚本"
echo "工作目录：$DEPLOY_DIR"
echo "JAR: $DEPLOY_DIR/$JAR_NAME"
echo "========================================"

PID=$(pgrep -f "java.*$JAR_NAME")
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
