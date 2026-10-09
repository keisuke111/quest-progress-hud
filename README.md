# Quest Progress HUD

A small client-side Minecraft HUD that shows your team's FTB Quests progress without opening the quest book.

## v0.0.9 — Public beta

```text
Quests    508 / 4790    10.6%
[thin progress bar]
```

The compact HUD uses Minecraft's font, a translucent dark background, a subtle rectangular frame, and a soft green progress bar. Counts and percentage share one line. The total count is gray.

- Completed quests, total quests, and a percentage rounded to one decimal place.
- Progress refreshed once per second while the HUD is visible.
- `Loading...` until both quest definitions and team progress have synced.
- Display on/off, four screen corners, horizontal/vertical offsets, size, and background opacity.
- Settings persist across launches. Positions are clamped to the screen, and the HUD shrinks to fit small screens.

## Compatibility

| Component | Target / tested version |
| --- | --- |
| Minecraft | 1.21.1 |
| Mod loader | NeoForge 21.1.251 or newer within Minecraft 1.21.1 |
| FTB Quests | Built and tested with 2101.1.36 |
| Primary test environment | All the Mods 10 v8.2 |

This HUD is installed on the client. FTB Quests and its required dependencies must already be installed in the environment, with quest data available to the client.

The jar declares FTB Quests 2101.1.0+ as a dependency; that is not a claim that every version has been tested. Other Minecraft versions, Forge, Fabric, and other modpacks are not yet verified.

## Download and installation

1. Open [Releases](https://github.com/keisuke111/quest-progress-hud/releases) and choose **v0.0.9 (Beta)**. Download `quest-progress-hud-0.0.9.jar` from Assets.
2. Close Minecraft and put the jar in your instance's `mods` folder.
3. Remove older Quest Progress HUD jars from that folder, keeping only one version.
4. Start the game and enter a world. The HUD appears after quest and team data sync.

Development builds are also available as artifacts on successful [Build workflow runs](https://github.com/keisuke111/quest-progress-hud/actions/workflows/build.yml).

## HUD settings

Open **Mods → Quest Progress HUD → Config**. Save with **Done**, then return to the world.

| Setting | Default | Range / options |
| --- | --- | --- |
| Show HUD | On | On / Off |
| Screen corner | Top left | All four corners |
| Horizontal / vertical offset | 0 | -10000 to 10000 GUI pixels |
| HUD size | 1.0 | 0.5 to 3.0, relative to Minecraft GUI scale |
| Background opacity | 72% | 0% to 100% |

Positive offsets move inward from the selected edges, starting at an 8 GUI-pixel margin. The frame fades with background opacity. F1 hides the HUD with the rest of the game interface.

Settings are stored in `config/quest_progress_hud-client.toml` inside the instance. Existing v0.0.6–v0.0.8 settings remain compatible. Settings labels and help are available in English and Japanese; HUD labels remain in English.

## What the numbers mean

- **Total:** every registered FTB Quest, including hidden, optional, repeatable, and internal quests. No filtering is applied.
- **Completed:** quests marked completed in the currently synced team's data. This is team progress, not a separate personal counter. Repeatable quests are counted as one quest according to their current completion state, not by the number of times repeated.
- **Percentage:** completed / total × 100, shown to one decimal place. An empty quest file displays 0 / 0 and 0.0%.

The static quest definitions in ATM10 v8.2 contain 4,790 quests. Your total can differ if the pack or its quest definitions change. Hidden quests are not automatically excluded, and visibility alone does not establish whether a quest is internal-only or reachable.

## Validation and feedback

The v0.0.9 build passed automated checks for layout bounds and text spacing, settings-file persistence and invalid-value correction, one-second refresh timing, sync state, disconnects, and changing world/team data. Several hours of gameplay in the primary environment were reported without observed issues.

This is an initial public beta. Other modpacks and the full range of multiplayer/team transitions still need broader in-game testing.

Report problems through [GitHub Issues](https://github.com/keisuke111/quest-progress-hud/issues). Include the mod, Minecraft, NeoForge and FTB Quests versions, the modpack version, reproduction steps, and a relevant screenshot or log excerpt.

## Contributing

Bug reports and feature requests can use the [issue templates](https://github.com/keisuke111/quest-progress-hud/issues/new/choose). English and Japanese are welcome. For pull requests and development checks, see [CONTRIBUTING.md](CONTRIBUTING.md).

## Security

See [SECURITY.md](SECURITY.md) for private vulnerability reporting and the [dependency security assessment](docs/DEPENDENCY_SECURITY.md) for known unresolved platform and build-tool alerts.

## License

All Rights Reserved. See [LICENSE](LICENSE). This matches the license declared in the mod metadata.

## Development

Java 21, NeoForge ModDevGradle, and Gradle 9.2.1. Run `gradle build` to compile the mod and run the verification checks. The project is based on the official NeoForge 1.21.1 ModDevGradle MDK.
