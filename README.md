# Expanse Heights

## Multi-Version Builds with Stonecutter

Switch to a specific Minecraft/NeoForge variant and build:

```powershell
.\gradlew.bat stonecutter use 1.20.1-neoforge
.\gradlew.bat build
```

Run the audit client for a given variant:

```powershell
.\gradlew.bat stonecutter use 1.21.1-neoforge
.\gradlew.bat runAuditClient
```

Audit reports are archived to `reports/parity/` as configured.
