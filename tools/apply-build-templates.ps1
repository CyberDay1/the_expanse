Param(
  [string]$VersionsDir = "versions",
  [string]$NeoTemplate = "_templates/build.gradle.neoforge.kts",
  [string]$ForgeTemplate = "_templates/build.gradle.forge.kts"
)

if (!(Test-Path $NeoTemplate)) { throw "Missing template: $NeoTemplate" }
if (!(Test-Path $ForgeTemplate)) { throw "Missing template: $ForgeTemplate" }
if (!(Test-Path $VersionsDir))  { throw "Missing versions dir: $VersionsDir" }

Get-ChildItem -Path $VersionsDir -Directory | ForEach-Object {
  $name = $_.Name
  $dest = Join-Path $_.FullName "build.gradle.kts"

  if ($name -like "*-neoforge") {
    Copy-Item $NeoTemplate $dest -Force
    Write-Host "Applied NeoForge template -> $name"
  } elseif ($name -like "*-forge") {
    Copy-Item $ForgeTemplate $dest -Force
    Write-Host "Applied Forge template -> $name"
  } else {
    Write-Host "Skipped $name (unknown loader suffix)"
  }
}
