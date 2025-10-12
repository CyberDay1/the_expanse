<#
.SYNOPSIS
  Ensures every NeoForge version folder has the correct buildMod task.
.DESCRIPTION
  Copies or patches the build.gradle.kts in each version subfolder
  to ensure the safe buildMod definition exists.
#>

Write-Host "Patching all NeoForge version folders..." -ForegroundColor Cyan

$root = Split-Path -Parent $MyInvocation.MyCommand.Path
$versions = Join-Path $root "versions"

if (!(Test-Path $versions)) {
    Write-Host "ERROR: Versions folder not found: $versions" -ForegroundColor Red
    exit 1
}

# Safe buildMod definition
$buildModDef = @'
val reobfJar = tasks.findByName("reobfJar") ?: tasks.register("reobfJar") {
    dependsOn(tasks.jar)
}

tasks.register("buildMod") {
    group = "build"
    description = "Builds the distributable mod jar"
    dependsOn(tasks.jar, reobfJar)
}
'@

Get-ChildItem -Path $versions -Recurse -Directory | ForEach-Object {
    $buildFile = Join-Path $_.FullName "build.gradle.kts"

    if (Test-Path $buildFile) {
        Write-Host "Checking $($_.Name)..."
        $content = Get-Content $buildFile -Raw

        # Remove old "tasks.named('build')" or wrong buildMod
        $cleaned = $content -replace '(?s)tasks\.named\("build"\).*?\}', ''
        $cleaned = $cleaned -replace '(?s)tasks\.register\("buildMod"\).*?\}', ''

        # Append correct definition
        if ($cleaned -notmatch 'tasks\.register\("buildMod"\)') {
            $cleaned += "`r`n$buildModDef"
            Write-Host "  Added buildMod task to $($_.Name)" -ForegroundColor Green
        }

        Set-Content -Path $buildFile -Value $cleaned -Encoding UTF8
    }
}

Write-Host "Patch complete. Run './gradlew --refresh-dependencies' next." -ForegroundColor Cyan
