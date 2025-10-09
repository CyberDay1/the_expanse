# Expanse Heights

## Multi-Version Builds with Stonecutter

The active variant defaults to the value declared in [`stonecutter.gradle.kts`](stonecutter.gradle.kts). Build it with:

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

## Feature Toggles

Build behaviour can be customised via the root-level [`configurable.properties`](configurable.properties) file. The Gradle
scripts automatically load these flags and expose them as project properties so they can be reused by downstream tasks. The
default file includes the following toggles:

| Property | Default | Description |
| --- | --- | --- |
| `enableDatagen` | `true` | Enables data generation tasks. When set to `false`, any task whose name includes `datagen` is disabled. |
| `useMixins` | `false` | Enables mixin-related tasks. When set to `false`, tasks with `mixin` in their name are disabled. |

Override the defaults by editing `configurable.properties` or by supplying the same properties as Gradle project properties
(`-PenableDatagen=false`, etc.).

## Gradle wrapper JAR

This repository omits `gradle/wrapper/gradle-wrapper.jar` from version control to comply with contribution rules that forbid binary uploads.
If you need to use the wrapper scripts, regenerate the JAR locally by running `gradle wrapper` with a compatible Gradle installation.

