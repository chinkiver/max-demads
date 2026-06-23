#!/bin/bash
set -e

# 服务器端重启脚本
# 在当前工作目录下执行
# 用法：
#   ./restart.sh [jar文件名] [profiles]
# 示例：
#   ./restart.sh max-demands.jar prod

JAR_NAME=${1:-max-demands.jar}
PROFILE=${2:-prod}

echo "========================================"
echo "Max 需求管理系统 - 重启脚本"
echo "JAR: $JAR_NAME"
echo "Profile: $PROFILE"
echo "========================================"

SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)

"$SCRIPT_DIR/stop.sh" "$JAR_NAME"
sleep 2
"$SCRIPT_DIR/start.sh" "$JAR_NAME" "$PROFILE"

echo "========================================"
