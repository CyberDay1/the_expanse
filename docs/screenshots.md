# Screenshot Capture Guide

Binary image assets are excluded from the repository to keep pull requests text-only. To
review the latest visuals referenced in the README:

1. Launch the mod in a NeoForge development environment.
2. Press `F3` and capture the debug overlay showing the Y range of `-256 .. 2015`.
3. Fly to an ocean biome, locate a widened canyon, and capture an overview shot.
4. Upload both PNG files to the shared drive under `The Expanse/Testing/Screenshots`.
5. Update the sharing link in the internal test report so reviewers can verify the
   captures without embedding binaries in version control.

Ensure screenshots are refreshed for every milestone release so the README's test
observations stay in sync with the most recent build.

---

## Integration Test Pack Setup

Follow these steps to assemble the manual integration testing instance:

1. Create a fresh Minecraft 1.21.1 profile using NeoForge `21.1.209` as the loader.
2. Install the following mods in the instance's `mods/` directory:
   - The Expanse (current development build or release under test).
   - [Tectonic](https://modrinth.com/mod/tectonic) for terrain generation coverage.
   - [Mekanism](https://modrinth.com/mod/mekanism) to validate rescaled ore spawns.
   - [AllTheOres](https://modrinth.com/mod/alltheores) for additional ore variants.
3. (Optional) Add other biome or structure mods you want to stress test against The Expanse.
4. Launch the dev client with `./gradlew runClient`, create a brand-new world, and enable
   cheats to simplify `/placefeature` testing.

Keep a copy of the exact mod list and versions used in the shared test notes so reviewers
can recreate the same environment locally.

## Manual Command Reference

Run the following commands in creative mode to validate scaled ore placement. Use a
spectator pass afterward to check that only the rescaled variants remain.

```
/placefeature the-expanse:ore_coal_scaled
/placefeature the-expanse:ore_iron_scaled
/placefeature the-expanse:ore_copper_scaled
/placefeature the-expanse:ore_gold_scaled
/placefeature the-expanse:ore_redstone_scaled
/placefeature the-expanse:ore_lapis_scaled
/placefeature the-expanse:ore_diamond_scaled

/placefeature the-expanse:ore_osmium_scaled
/placefeature the-expanse:ore_fluorite_scaled
/placefeature the-expanse:ore_lead_scaled
/placefeature the-expanse:ore_tin_scaled
/placefeature the-expanse:ore_uranium_scaled

/placefeature the-expanse:ore_aluminum_scaled
/placefeature the-expanse:ore_silver_scaled
/placefeature the-expanse:ore_nickel_scaled
/placefeature the-expanse:ore_platinum_scaled
/placefeature the-expanse:ore_zinc_scaled
```

Document any anomalies you encounter (missing blocks, incorrect heights, duplicate
spawns) in the integration checklist alongside links to supporting screenshots stored in
The Expanse's shared drive. Do **not** add binaries to the repository—reference them via
shared storage links instead.

## Observations to Capture

When updating screenshots or test notes, include the following confirmations:

- The debug overlay (`F3`) reports the extended Y range of `-256 .. 2015`.
- Tectonic's noise-based terrain appears intact, including widened ocean ravines.
- Mekanism and AllTheOres nodes spawn at the expected heights without overlapping the
  vanilla replacements removed by The Expanse biome modifiers.
- Chunk generation remains stable while flying through newly generated regions (no lag
  spikes or placement errors in logs).

These observations, paired with the external screenshots, form the documented proof of
compatibility for the manual integration pass.

## CI Scope Reminder

Automated CI remains limited to unit and resource validation tasks. Integration testing
that relies on external mods must stay manual. Use the checklist in
`docs/integration_checklist.md` to track each run and upload the completed copy (or a link
to it) with the associated screenshots in the shared testing folder.
