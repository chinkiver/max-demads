#!/bin/bash
set -e

# 获取脚本所在目录
SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
PROJECT_DIR=$(cd "$SCRIPT_DIR/.." && pwd)

# 加载配置
source "$SCRIPT_DIR/deploy.conf"

echo "========================================"
echo "Max 需求管理系统 - 部署脚本"
echo "目标服务器：$SERVER_USER@$SERVER_HOST:$SERVER_PORT"
echo "部署目录：$DEPLOY_DIR"
echo "========================================"

# 1. 本地构建
echo "[1/4] 开始本地构建..."
"$SCRIPT_DIR/build.sh"

# 2. 上传 JAR 和远程脚本
echo "[2/4] 上传文件到服务器..."
JAR_FILE=$(ls "$PROJECT_DIR"/target/max-demands-*.jar | head -1)

ssh -p "$SERVER_PORT" "$SERVER_USER@$SERVER_HOST" "mkdir -p $DEPLOY_DIR/scripts"
scp -P "$SERVER_PORT" "$JAR_FILE" "$SERVER_USER@$SERVER_HOST:$DEPLOY_DIR/$JAR_NAME"
scp -P "$SERVER_PORT" "$SCRIPT_DIR"/remote/*.sh "$SERVER_USER@$SERVER_HOST:$DEPLOY_DIR/scripts/"
ssh -p "$SERVER_PORT" "$SERVER_USER@$SERVER_HOST" "chmod +x $DEPLOY_DIR/scripts/*.sh"

# 3. 停止旧服务
echo "[3/4] 停止旧服务..."
ssh -p "$SERVER_PORT" "$SERVER_USER@$SERVER_HOST" "cd $DEPLOY_DIR && ./scripts/stop.sh $JAR_NAME" || true

# 4. 启动新服务
echo "[4/4] 启动新服务..."
ssh -p "$SERVER_PORT" "$SERVER_USER@$SERVER_HOST" "cd $DEPLOY_DIR && ./scripts/start.sh $JAR_NAME $PROFILE"

echo "========================================"
echo "部署完成"
echo "应用地址：http://$SERVER_HOST:$SERVER_PORT"
echo "========================================"
