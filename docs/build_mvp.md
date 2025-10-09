# MVP build and validation process

This project ships a minimal yet reproducible build that focuses on the runtime
safety of the bundled datapack. Follow the steps below before publishing any
changes.

## Prerequisites

* Java 21 available on your `PATH` (CI uses Temurin 21).
* A clean working tree so Gradle can safely switch Stonecutter variants.

## Validate source and datapack logic

Run the full verification suite for **every** Stonecutter variant. The helper
below expands the default list declared in `stonecutter.gradle.kts`:

```bash
./gradlew checkAllVariants
```

The `checkAllVariants` aggregate executes `check` for each configured version in
sequence and fails fast on any warnings or errors.

## Verify dependency locks

The lock files must stay in sync with the resolved dependencies. Rebuild the
locks without enabling the daemon or build cache so the output matches CI:

```bash
./gradlew --no-daemon --warning-mode=fail verifyDependencyLocks
```

## Datapack runtime smoke test

Finally, launch the headless datapack server against the default NeoForge
variant. CI covers the same scenario to catch regressions:

```bash
./gradlew --no-daemon --warning-mode=fail datapackRuntimeTest \
  -Pstonecutter.active=1.21.1-neoforge
```

Gradle stores the server log at
`versions/1.21.1-neoforge/build/datapackRuntime/server/logs/datapack-runtime.log`.
Inspect the file when the task fails to identify broken registries or datapack
load errors.

Completing the three steps above replicates the CI coverage for the MVP build.
