# ─────────────────────────────────────────────────────────────
# restore_build_env.ps1 — The Expanse build environment repair
# ─────────────────────────────────────────────────────────────

Write-Host "🧱 Restoring The Expanse build environment..." -ForegroundColor Cyan
$projectRoot = Split-Path -Parent $MyInvocation.MyCommand.Definition
Set-Location $projectRoot

# 1️⃣ Clean Gradle and build cache
Write-Host "🧹 Cleaning old Gradle and build folders..." -ForegroundColor Yellow
$pathsToRemove = @(
    ".gradle",
    "build",
    "versions\*\build",
    "versions\*\out"
)
foreach ($path in $pathsToRemove) {
    Get-ChildItem -Path $path -ErrorAction SilentlyContinue | ForEach-Object {
        Remove-Item -Path $_.FullName -Recurse -Force -ErrorAction SilentlyContinue
        Write-Host "   Removed $($_.FullName)" -ForegroundColor DarkGray
    }
}

# 2️⃣ Recreate build.gradle.kts loader
$buildGradleKts = @"
// Root build file — delegates to NeoForge build logic
apply(from = \"build.neoforge.gradle.kts\")
"@
Set-Content -Path "$projectRoot\build.gradle.kts" -Value $buildGradleKts -Encoding UTF8
Write-Host "✅ Rewrote build.gradle.kts to load build.neoforge.gradle.kts" -ForegroundColor Green

# 3️⃣ Recreate stonecutter.json
$stonecutterJson = @"
{
    "default": "1.21.1",
    "variants": {
        "1.21.1": {
            "replace": {
                "NEOFORGE_VERSION": "21.1.209",
                "PACK_FORMAT": "48",
                "MC_VERSION": "1.21.1"
            },
            "buildscript": "build.neoforge.gradle.kts"
        },
        "1.21.2": {
            "replace": {
                "NEOFORGE_VERSION": "21.2.84",
                "PACK_FORMAT": "57",
                "MC_VERSION": "1.21.2"
            },
            "buildscript": "build.neoforge.gradle.kts"
        },
        "1.21.3": {
            "replace": {
                "NEOFORGE_VERSION": "21.3.64",
                "PACK_FORMAT": "57",
                "MC_VERSION": "1.21.3"
            },
            "buildscript": "build.neoforge.gradle.kts"
        },
        "1.21.4": {
            "replace": {
                "NEOFORGE_VERSION": "21.4.154",
                "PACK_FORMAT": "61",
                "MC_VERSION": "1.21.4"
            },
            "buildscript": "build.neoforge.gradle.kts"
        },
        "1.21.5": {
            "replace": {
                "NEOFORGE_VERSION": "21.5.72",
                "PACK_FORMAT": "71",
                "MC_VERSION": "1.21.5"
            },
            "buildscript": "build.neoforge.gradle.kts"
        }
    }
}
"@
Set-Content -Path "$projectRoot\stonecutter.json" -Value $stonecutterJson -Encoding UTF8
Write-Host "✅ Regenerated stonecutter.json with 1.21.x variants" -ForegroundColor Green

# 4️⃣ Refresh Gradle dependencies
Write-Host "🔄 Refreshing Gradle dependencies..." -ForegroundColor Yellow
& ./gradlew --refresh-dependencies clean > $null 2>&1
if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ Gradle cache refreshed successfully" -ForegroundColor Green
} else {
    Write-Host "⚠️ Warning: Gradle refresh encountered issues" -ForegroundColor Red
}

# 5️⃣ Run Stonecutter and build all mods
Write-Host "🧱 Running Stonecutter and assembling all versions..." -ForegroundColor Yellow
& ./gradlew assembleAllMods --no-daemon

if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ All NeoForge mod builds completed successfully!" -ForegroundColor Green
} else {
    Write-Host "❌ Build failed — check Gradle logs above for details." -ForegroundColor Red
}

Write-Host "🏁 Build environment restoration complete." -ForegroundColor Cyan
