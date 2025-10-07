# Expanse Heights

## Multi-Version Builds with Stonecutter

The active variant defaults to the value declared in [`stonecutter.json`](stonecutter.json). Build it with:

```bash
./gradlew chiseledBuild
```

To target a different Minecraft/NeoForge variant, override the active variant when invoking Gradle:

```bash
./gradlew chiseledBuild -Pstonecutter.active=1.21.4-neoforge
```

Run the audit client for a given variant in the same way:

```bash
./gradlew runAuditClient -Pstonecutter.active=1.21.4-neoforge
```

Audit reports are archived to `reports/parity/` as configured.

## Gradle wrapper JAR

This repository omits `gradle/wrapper/gradle-wrapper.jar` from version control to comply with contribution rules that forbid binary uploads.
If you need to use the wrapper scripts, regenerate the JAR locally by running `gradle wrapper` with a compatible Gradle installation.

