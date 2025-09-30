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

## Warnings

Worldrise fundamentally alters overworld generation and ore placement. These changes are
only safe for **new worlds**—existing saves may exhibit chunk borders, duplicated ores, or
other corruption. Always back up your worlds before experimenting.

## Development Notes

Automated validation lives in `src/test/java` and ensures the mod metadata and resource
JSON files remain well-formed. Run `./gradlew build` to execute these checks locally.
