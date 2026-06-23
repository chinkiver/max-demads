#!/bin/bash
set -e

# 启动脚本
# 用法：
#   ./scripts/start.sh [jar文件名] [profiles]
# 示例：
#   ./scripts/start.sh max-demands-1.0.0.jar prod

# 默认配置
JAR_NAME=${1:-max-demands-1.0.0.jar}
PROFILE=${2:-prod}
APP_NAME=max-demands
DEPLOY_DIR=$(cd "$(dirname "$0")/.." && pwd)
LOG_DIR="$DEPLOY_DIR/logs"
LOG_FILE="$LOG_DIR/$APP_NAME.log"

echo "========================================"
echo "Max 需求管理系统 - 启动脚本"
echo "JAR: $DEPLOY_DIR/$JAR_NAME"
echo "Profile: $PROFILE"
echo "========================================"

cd "$DEPLOY_DIR"

# 创建日志目录
mkdir -p "$LOG_DIR"

# 检查 JAR 文件是否存在
if [ ! -f "$DEPLOY_DIR/$JAR_NAME" ]; then
    echo "错误：未找到 JAR 文件：$DEPLOY_DIR/$JAR_NAME"
    exit 1
fi

# 检查是否已在运行
PID=$(ps -ef | grep "$JAR_NAME" | grep -v grep | awk '{print $2}' | head -1)
if [ -n "$PID" ]; then
    echo "应用已在运行，PID: $PID"
    exit 0
fi

# 启动应用
nohup java -jar "$DEPLOY_DIR/$JAR_NAME" --spring.profiles.active="$PROFILE" > "$LOG_FILE" 2>&1 &

sleep 3

# 检查启动状态
NEW_PID=$(ps -ef | grep "$JAR_NAME" | grep -v grep | awk '{print $2}' | head -1)
if [ -n "$NEW_PID" ]; then
    echo "应用启动成功，PID: $NEW_PID"
    echo "日志文件：$LOG_FILE"
else
    echo "应用启动失败，请检查日志：$LOG_FILE"
    exit 1
fi

echo "========================================"
