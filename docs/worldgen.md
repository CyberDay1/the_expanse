# Worldgen Data Overview

The `data/the_expanse` datapack namespace carries the definitions that expand the
vertical build and generation range used by the mod. The layout mirrors the
vanilla structure so that the content can be merged cleanly across all supported
NeoForge variants.

## Dimension types

```
data/the_expanse/dimension_type/*.json
```

The `overworld.json` and `nether.json` files pin every dimension to the
-256→2288 vertical bounds that NeoForge 1.21.x expects. Both `height` and
`logical_height` are kept in sync so height-based checks (spawning, portals,
etc.) inherit the extended build limit.

## Noise settings

```
data/the_expanse/worldgen/noise_settings/*.json
```

Each file mirrors the vanilla router configuration but updates the `noise`
section so the generator emits terrain across the wider Y range. The same files
are shared across every NeoForge version through the Stonecutter templates, so
any future tweaks should be applied here first.

## Ore scaling features

```
data/the_expanse/worldgen/configured_feature/ore_scaling_*.json

data/the_expanse/worldgen/placed_feature/ore_scaling_*.json
```

The configured features define the actual ore targets while the matching placed
features provide placement modifiers using vanilla ranges. The runtime
`OreScaler` rewrites those height modifiers at load time so that the ores spawn
throughout the expanded vertical space without duplicating JSON for every
version.

Finally, the placed features are added to the shared `forge:ores` tag so the
scaler automatically detects them during reload.
