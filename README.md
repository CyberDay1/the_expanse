# Worldrise

Worldrise is a NeoForge world generation mod that expands the build height and rebalances
key overworld features to better fit the taller terrain profile. The current prototype
focuses on stretching the vertical space, rescaling ore distributions, and introducing a
new canyon carver for ocean biomes.

## Installation

1. Install Minecraft 1.21.1 with the matching NeoForge loader.
2. Download or build the Worldrise mod JAR.
3. Place the JAR into your `mods` folder.
4. Launch the game with NeoForge to generate a new world using the expanded height rules.

## Supported Versions

* **Minecraft:** 1.21.1
* **NeoForge:** 21.1.x

## World Height

Worldrise expands the overworld build limits to **Y -256 through Y 2015**. The vertical
space gives large cave systems, floating structures, and ocean trenches more room to
generate without crowding the surface.

## Warnings

Worldrise fundamentally alters overworld generation and ore placement. These changes are
only safe for **new worlds**—existing saves may exhibit chunk borders, duplicated ores, or
other corruption. Always back up your worlds before experimenting.

## Configuration

The `config/worldrise.toml` file exposes tuning multipliers for players who need to balance
performance with resource availability when stretching the world height:

* `scaling.defaultOreMultiplier` – Global multiplier applied to ore placement `count`/`tries`
  when no biome-specific override matches. Values are clamped to produce at least one
  placement attempt.
* `scaling.defaultCarverMultiplier` – Global multiplier applied to carver `probability`
  fields when no override matches. Probabilities are clamped to the `[0.0, 1.0]` range.
* `scaling.biomeOreMultipliers` / `scaling.biomeCarverMultipliers` – Lists of strings in the
  format `<biome-id or #tag>=<multiplier>`. These entries override the default multipliers for
  matching biomes or biome tags, enabling biome-specific ore density or carver frequency.

Lower multipliers lighten chunk generation cost by creating fewer features, whereas higher
values increase terrain detail at the expense of performance. Extreme settings may break
the expected gameplay balance, so adjust cautiously.

## Compatibility Notes

* **Tectonic:** Verified alongside Worldrise—terrain retains Tectonic's noise while
  Worldrise ravines and rescaled ores inject cleanly without duplication.
* **Mekanism:** Mekanism ores honor the scaling pass and continue to generate in their
  expected strata.
* **AllTheOres:** Custom ore features remain intact, adopting Worldrise height scaling for
  better distribution through the expanded Y-range.

## Integration Highlights

* F3 debug overlay reports the expanded Y range of **-256 to 2015** in-game.
* Ocean biomes exhibit visibly wider canyons carved by the custom ocean canyon carver.
* `/placefeature worldrise:ore_coal_scaled` successfully spawns scaled coal seams for
  verification during testing sessions.

## Test Evidence

Screenshots documenting the F3 height overlay and widened ocean ravines are hosted in the
team's shared drive (see `docs/screenshots.md` for capture instructions). The textual
observations from those captures are summarized in the integration highlights above so
the README remains review-friendly without bundling binary assets.

## Development Notes

Automated validation lives in `src/test/java` and ensures the mod metadata and resource
JSON files remain well-formed. Run `./gradlew build` to execute these checks locally.
