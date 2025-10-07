# Expanse Heights

## Multi-Version Builds with Stonecutter

Switch to a specific Minecraft/NeoForge variant and build:

```powershell
./gradlew.bat stonecutter use 1.21.1-neoforge
./gradlew.bat chiseledBuild
```

Run the audit client for a given variant:

```powershell
./gradlew.bat stonecutter use 1.21.4-neoforge
./gradlew.bat runAuditClient
```

Audit reports are archived to `reports/parity/` as configured.

## Gradle wrapper JAR

This repository omits `gradle/wrapper/gradle-wrapper.jar` from version control to comply with contribution rules that forbid binary uploads.
If you need to use the wrapper scripts, regenerate the JAR locally by running `gradle wrapper` with a compatible Gradle installation.

