# patch-buildmod.ps1
# Adds buildMod task to Forge and NeoForge templates, then applies to all versions

$ErrorActionPreference = "Stop"

$templates = @(
    "_templates/build.gradle.neoforge.kts",
    "_templates/build.gradle.forge.kts"
)

foreach ($template in $templates) {
    if (Test-Path $template) {
        Write-Host "Patching $template..."

        $content = Get-Content $template -Raw

        if ($content -notmatch 'tasks.register\("buildMod"\)') {
            $buildModBlock = @'
/**
 * Ensure reobfJar is wired to jar.
 * If NeoForge/Forge didn’t create it, we register it ourselves.
 */
val reobfJar = tasks.findByName("reobfJar") ?: tasks.register("reobfJar") {
    dependsOn(tasks.jar)
}

tasks.register("buildMod") {
    group = "build"
    description = "Builds the distributable mod jar"
    dependsOn(tasks.jar, reobfJar)
}
'@

            # Append before publishing { if it exists, otherwise at the end
            if ($content -match "publishing\s*{") {
                $content = $content -replace "(?=publishing\s*{)", "$buildModBlock`r`n"
            } else {
                $content += "`r`n$buildModBlock"
            }

            Set-Content $template $content -NoNewline
            Write-Host "✓ Added buildMod to $template"
        } else {
            Write-Host "✓ $template already contains buildMod"
        }
    } else {
        Write-Host "⚠ Template not found: $template"
    }
}

# Sync templates into versioned subprojects
Get-ChildItem versions -Directory | ForEach-Object {
    $version = $_.Name
    if ($version -match "neoforge") {
        Copy-Item "_templates/build.gradle.neoforge.kts" "$($_.FullName)/build.gradle.kts" -Force
        Write-Host "Updated build.gradle.kts for $version (NeoForge)"
    } elseif ($version -match "forge") {
        Copy-Item "_templates/build.gradle.forge.kts" "$($_.FullName)/build.gradle.kts" -Force
        Write-Host "Updated build.gradle.kts for $version (Forge)"
    }
}

Write-Host "`nAll templates patched and version builds synced."
Write-Host "Now run: ./gradlew :1.21.1-neoforge:buildMod"
