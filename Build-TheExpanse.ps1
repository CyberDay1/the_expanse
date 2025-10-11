# ─────────────────────────────────────────────────────────────
# 🧱 The Expanse PowerShell Build Orchestrator
# Runs clean → build → assembleAllMods with optional deep clean
# ─────────────────────────────────────────────────────────────

param(
    [switch]$DeepClean,
    [switch]$ResetLocks
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $MyInvocation.MyCommand.Definition
Set-Location $root

$logFile = Join-Path $root ("BuildLog_" + (Get-Date -Format "yyyyMMdd_HHmmss") + ".txt")

function Write-Log($msg, $color="White") {
    Write-Host $msg -ForegroundColor $color
    Add-Content $logFile $msg
}

Write-Log "🧱 Starting The Expanse build pipeline …`n" Cyan

if ($DeepClean) {
    Write-Log "🧹 Performing Gradle deep clean …" Yellow
    & .\gradlew deepClean --no-daemon | Tee-Object -FilePath $logFile -Append
}

if ($ResetLocks) {
    Write-Log "🔄 Resetting dependency locks …" Yellow
    & .\gradlew resetDependencyLocks --no-daemon | Tee-Object -FilePath $logFile -Append
}

Write-Log "🧼 Running clean build …" Yellow
& .\gradlew clean build --no-daemon | Tee-Object -FilePath $logFile -Append

Write-Log "🚀 Assembling all mods …" Yellow
& .\gradlew assembleAllMods --no-daemon | Tee-Object -FilePath $logFile -Append

Write-Log "`n✅ Build completed successfully!" Green
Write-Log "📄 Log saved → $logFile" Gray
