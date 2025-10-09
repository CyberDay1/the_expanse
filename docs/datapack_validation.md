# Datapack validation

## Schema validation

The existing unit tests exercise our JSON data and schema contracts using fast in-JVM checks. They ensure that file layouts, pack metadata, and basic worldgen shapes conform to expectations without starting Minecraft. These tests run as part of the regular `check` task and provide rapid feedback while iterating on datapack content.

## Runtime validation

The `datapackRuntimeTest` task boots a headless NeoForge dedicated server with the bundled datapack enabled. Starting the full runtime catches issues that only surface once the game parses registries and worldgen, such as missing references, malformed biome modifiers, or invalid structure configurations. The task tails the server log and fails if the JVM exits with an error or if the log reports datapack loading issues.

### Running locally

```bash
./gradlew datapackRuntimeTest -Pstonecutter.active=1.21.1-neoforge
```

Logs are written to `versions/<variant>/build/datapackRuntime/server/logs/datapack-runtime.log`. You can inspect the file to confirm load order or to debug failures.

### CI integration

Continuous integration runs `datapackRuntimeTest` for every configured Stonecutter variant immediately after the standard `check` task. On failure the workflow uploads the collected server logs as build artifacts for easier debugging.
