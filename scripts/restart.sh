#!/bin/bash
set -e

# 重启脚本
# 用法：
#   ./scripts/restart.sh [jar文件名] [profiles]
# 示例：
#   ./scripts/restart.sh max-demands-1.0.0.jar prod

JAR_NAME=${1:-max-demands-1.0.0.jar}
PROFILE=${2:-prod}

SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)

echo "========================================"
echo "Max 需求管理系统 - 重启脚本"
echo "JAR: $JAR_NAME"
echo "Profile: $PROFILE"
echo "========================================"

"$SCRIPT_DIR/stop.sh" "$JAR_NAME"
sleep 2
"$SCRIPT_DIR/start.sh" "$JAR_NAME" "$PROFILE"

echo "========================================"
