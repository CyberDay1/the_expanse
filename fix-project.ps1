<#
fix-project.ps1
Unified repo cleanup for The Expanse
- Removes Stonecutter remnants, normalizes structure
- Moves template/src -> src
- Enforces neoforge.mods.toml only
- Installs pre-commit guard
- Iteratively prunes unwanted branches (remote & local)
#>

$ErrorActionPreference = "Stop"

Write-Host "=== The Expanse: Unified Repo Fix ===" -ForegroundColor Cyan

# Detect default branch
$defaultBranch = (git remote show origin | Select-String "HEAD branch").ToString().Split(":")[-1].Trim()
Write-Host "Detected default branch: $defaultBranch" -ForegroundColor Green

# Step 1: Clean up Stonecutter remnants
if (Test-Path "template/src") {
    Write-Host "Moving template/src -> src..." -ForegroundColor Yellow
    Move-Item -Force "template/src" "src"
}
if (Test-Path "build.neoforge.gradle.kts") {
    Write-Host "Replacing build.gradle.kts with build.neoforge.gradle.kts..." -ForegroundColor Yellow
    Remove-Item -Force "build.gradle.kts" -ErrorAction SilentlyContinue
    Rename-Item "build.neoforge.gradle.kts" "build.gradle.kts" -Force
}
if (Test-Path "template") {
    Write-Host "Removing template ..." -ForegroundColor Yellow
    Remove-Item -Recurse -Force "template"
}

# Step 2: Ensure mods.toml is gone
if (Test-Path "src/main/resources/META-INF/mods.toml") {
    Write-Host "Removing mods.toml (Forge-only)..." -ForegroundColor Yellow
    Remove-Item -Force "src/main/resources/META-INF/mods.toml"
}

# Step 3: Pre-commit guard
$hookDir = ".git/hooks"
$hookFile = "$hookDir/pre-commit"
if (-not (Test-Path $hookDir)) {
    New-Item -ItemType Directory -Force -Path $hookDir | Out-Null
}
@"
#!/bin/sh
# Prevent committing Forge-only mods.toml
if git diff --cached --name-only | grep -q "META-INF/mods.toml"; then
  echo "❌ ERROR: mods.toml detected in commit! Use neoforge.mods.toml only."
  exit 1
fi
"@ | Out-File $hookFile -Encoding ascii
try { git update-index --chmod=+x $hookFile } catch {}
Write-Host "✓ Installed pre-commit guard" -ForegroundColor Green

# Step 4: Commit cleanup
git add -A
try {
    git commit -m "Repo cleanup: remove Stonecutter remnants, enforce neoforge.mods.toml, install pre-commit guard" --allow-empty
} catch {
    Write-Host "No new cleanup changes to commit." -ForegroundColor Yellow
}

# Step 5: Branch prune loop
$keep = @($defaultBranch,
          "1.21.1-neoforge","1.21.2-neoforge","1.21.3-neoforge",
          "1.21.4-neoforge","1.21.5-neoforge","1.21.6-neoforge",
          "1.21.7-neoforge","1.21.8-neoforge","1.21.9-neoforge")

Write-Host "=== Branch Prune ===" -ForegroundColor Cyan
do {
    $branches = git branch -r | ForEach-Object { $_.Trim() -replace "^origin/", "" } | Where-Object { $_ -ne "HEAD" }
    $pruned = $false
    foreach ($branch in $branches) {
        if ($keep -notcontains $branch) {
            Write-Host "Deleting remote branch: $branch" -ForegroundColor Yellow
            git push origin --delete $branch
            $pruned = $true
        }
    }
} while ($pruned)

Write-Host "✓ Branch pruning complete. Only default + 1.21.x branches remain." -ForegroundColor Green
