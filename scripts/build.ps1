# Max 需求管理系统 - Windows 构建脚本
# 用法：右键选择"使用 PowerShell 运行"，或在 PowerShell 中执行：
#   .\scripts\build.ps1

param(
    [string]$Profile = "prod"
)

$ErrorActionPreference = "Stop"
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

$scriptDir = Split-Path -Parent $MyInvocation.MyCommand.Definition
$projectDir = Split-Path -Parent $scriptDir

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Max 需求管理系统 - Windows 构建脚本" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan

Set-Location $projectDir

# 1. 构建前端
Write-Host "[1/3] 构建前端..." -ForegroundColor Yellow
Set-Location "$projectDir\max-demands-ui"
npm install
if ($LASTEXITCODE -ne 0) { throw "npm install 失败" }

npm run build
if ($LASTEXITCODE -ne 0) { throw "前端构建失败" }

Set-Location $projectDir

# 2. 构建后端 JAR
Write-Host "[2/3] 构建后端 JAR..." -ForegroundColor Yellow
mvn clean package -DskipTests
if ($LASTEXITCODE -ne 0) { throw "后端构建失败" }

# 3. 检查构建产物
Write-Host "[3/3] 检查构建产物..." -ForegroundColor Yellow
$jarFile = Get-ChildItem -Path "$projectDir\target" -Filter "max-demands-*.jar" | Select-Object -First 1
if (-not $jarFile) {
    throw "错误：未找到构建产物 JAR 文件"
}

Write-Host "构建成功：$($jarFile.FullName)" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Cyan

# 暂停，方便查看结果
Write-Host "按任意键退出..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
