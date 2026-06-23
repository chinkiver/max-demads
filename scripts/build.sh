#!/bin/bash
set -e

# 获取脚本所在目录
SCRIPT_DIR=$(cd "$(dirname "$0")" && pwd)
PROJECT_DIR=$(cd "$SCRIPT_DIR/.." && pwd)

echo "========================================"
echo "Max 需求管理系统 - 构建脚本"
echo "========================================"

cd "$PROJECT_DIR"

# 1. 构建前端
echo "[1/3] 构建前端..."
cd max-demands-ui
npm install
npm run build
cd ..

# 2. 构建后端 JAR
echo "[2/3] 构建后端 JAR..."
mvn clean package -DskipTests

# 3. 检查构建产物
echo "[3/3] 检查构建产物..."
JAR_FILE=$(ls target/max-demands-*.jar 2>/dev/null | head -1)
if [ -z "$JAR_FILE" ]; then
    echo "错误：未找到构建产物 JAR 文件"
    exit 1
fi

echo "构建成功：$JAR_FILE"
echo "========================================"
