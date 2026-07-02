#!/bin/bash
set -e

# 服务器端启动脚本
# 在当前工作目录下执行
# 用法：
#   ./start.sh [jar文件名] [profiles]
# 示例：
#   ./start.sh max-demands.jar prod

JAR_NAME=${1:-max-demands.jar}
PROFILE=${2:-prod}
APP_NAME=max-demands
DEPLOY_DIR=$(pwd)
LOG_DIR="$DEPLOY_DIR/logs"
LOG_FILE="$LOG_DIR/$APP_NAME.log"

echo "========================================"
echo "Max 需求管理系统 - 启动脚本"
echo "工作目录：$DEPLOY_DIR"
echo "JAR: $DEPLOY_DIR/$JAR_NAME"
echo "Profile: $PROFILE"
echo "========================================"

# 创建日志目录
mkdir -p "$LOG_DIR"

# 检查 JAR 文件是否存在
if [ ! -f "$DEPLOY_DIR/$JAR_NAME" ]; then
    echo "错误：未找到 JAR 文件：$DEPLOY_DIR/$JAR_NAME"
    exit 1
fi

# 检查是否已在运行
PID=$(pgrep -f "java.*$JAR_NAME" || true)
if [ -n "$PID" ]; then
    echo "应用已在运行，PID: $PID"
    exit 0
fi

# 启动应用（日志由 Logback 接管，nohup 输出重定向到 /dev/null 避免生成 nohup.out）
nohup java -jar "$DEPLOY_DIR/$JAR_NAME" --spring.profiles.active="$PROFILE" > /dev/null 2>&1 &

sleep 5

# 检查启动状态
NEW_PID=$(pgrep -f "java.*$JAR_NAME" || true)
if [ -n "$NEW_PID" ]; then
    echo "应用启动成功，PID: $NEW_PID"
    echo "日志文件：$LOG_FILE"
else
    echo "应用启动失败，请检查日志：$LOG_FILE"
    exit 1
fi

echo "========================================"
