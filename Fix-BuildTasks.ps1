# Fix-BuildTasks.ps1
# This script scans all version folders for problematic build.gradle.kts definitions
# and replaces the conflicting `tasks.named("build")` blocks with a clean `buildMod` task.

Write-Host "Scanning version folders for build.gradle.kts files..." -ForegroundColor Cyan

$versionDirs = Get-ChildItem -Path "$PSScriptRoot\versions" -Directory -ErrorAction SilentlyContinue
if (-not $versionDirs) {
    Write-Host "No version directories found under 'versions'." -ForegroundColor Red
    exit 1
}

foreach ($dir in $versionDirs) {
    $buildFile = Join-Path $dir.FullName "build.gradle.kts"
    if (Test-Path $buildFile) {
        Write-Host "Processing $($dir.Name)..."

        $content = Get-Content $buildFile -Raw

        # Remove any existing 'tasks.named("build")' block
        $cleaned = $content -replace '(?s)tasks\.named\("build"\).*?\}', ''

        # Add safe replacement if it doesn't already exist
        if ($cleaned -notmatch 'tasks\.register\("buildMod"\)') {
            $replacement = @"
tasks.register("buildMod") {
    group = "build"
    description = "Builds distributable mod jar safely."
    dependsOn(tasks.jar)
    if (tasks.findByName("reobfJar") -ne $null) {
        dependsOn(tasks.reobfJar)
    }
}
"@
            $cleaned += "`r`n$replacement"
        }

        Set-Content -Path $buildFile -Value $cleaned -Encoding UTF8
        Write-Host "  → Fixed and updated: $($dir.Name)" -ForegroundColor Green
    }
    else {
        Write-Host "  ⚠️ Skipping $($dir.Name): no build.gradle.kts found." -ForegroundColor Yellow
    }
}

Write-Host "✅ All version build files processed successfully." -ForegroundColor Cyan
