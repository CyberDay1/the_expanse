# ===================================================================
# reset-gradle.ps1
# Full Gradle environment reset & rebuild script for Windows
# Works safely for NeoForge + Stonecutter projects
# ===================================================================

Write-Host "🔧 Starting Gradle full reset..."

# --- 1. Stop any running Gradle daemons
Write-Host "🧱 Stopping Gradle daemons..."
& .\gradlew --stop 2>$null

# --- 2. Close potential leftover caches
Write-Host "🧹 Cleaning user Gradle caches..."
$gradleUser = "$env:USERPROFILE\.gradle"
$tempGradle = "$env:LOCALAPPDATA\Temp\gradle*"

if (Test-Path $gradleUser) { Remove-Item -Recurse -Force $gradleUser }
if (Test-Path $tempGradle) { Remove-Item -Recurse -Force $tempGradle -ErrorAction SilentlyContinue }

# --- 3. Clean project-level caches
Write-Host "🧼 Cleaning project .gradle and build directories..."
if (Test-Path ".\.gradle") { Remove-Item -Recurse -Force ".\.gradle" }
if (Test-Path ".\build") { Remove-Item -Recurse -Force ".\build" }

# --- 4. Recreate empty Gradle home
Write-Host "📁 Recreating clean Gradle home..."
New-Item -ItemType Directory -Force -Path $gradleUser | Out-Null

# --- 5. Create/update gradle.properties with safe defaults
Write-Host "⚙️  Writing gradle.properties with recommended settings..."
$gradleProps = @"
# Gradle performance and safety defaults
org.gradle.dependency.verification=off
org.gradle.daemon=false
org.gradle.parallel=true
org.gradle.configureondemand=true
org.gradle.caching=true
"@
Set-Content -Path ".\gradle.properties" -Value $gradleProps -Encoding UTF8

# --- 6. Trigger a full dependency rebuild
Write-Host "🌐 Running dependency refresh..."
& .\gradlew --no-daemon --refresh-dependencies clean

Write-Host "✅ Gradle reset complete!"
Write-Host "Next: Run your project task again, e.g.:"
Write-Host "    .\gradlew :1.21.1-neoforge:neoFormPatch"
