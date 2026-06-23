@echo off
chcp 65001 >nul

echo ========================================
echo Max 需求管理系统 - Windows 构建脚本
echo ========================================

set "SCRIPT_DIR=%~dp0"
set "PROJECT_DIR=%SCRIPT_DIR%.."

cd /d "%PROJECT_DIR%"

echo [1/3] 构建前端...
cd /d "%PROJECT_DIR%\max-demands-ui"
call npm install
if %errorlevel% neq 0 (
    echo 错误：npm install 失败
    pause
    exit /b 1
)

call npm run build
if %errorlevel% neq 0 (
    echo 错误：前端构建失败
    pause
    exit /b 1
)

cd /d "%PROJECT_DIR%"

echo [2/3] 构建后端 JAR...
call mvn clean package -DskipTests
if %errorlevel% neq 0 (
    echo 错误：后端构建失败
    pause
    exit /b 1
)

echo [3/3] 检查构建产物...
if not exist "%PROJECT_DIR%\target\max-demands-*.jar" (
    echo 错误：未找到构建产物 JAR 文件
    pause
    exit /b 1
)

for %%f in ("%PROJECT_DIR%\target\max-demands-*.jar") do (
    echo 构建成功：%%f
)

echo ========================================
pause
