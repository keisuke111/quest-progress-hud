# Quest Progress HUD

A compact client-side HUD that shows your team's **FTB Quests progress** without opening the quest book.

## Features

- Completed / total quest counts, completion percentage, and a progress bar.
- Track **all quests** or a **selected chapter**.
- Progress refreshes once per second while the HUD is visible.
- Drag the HUD over the game view and preview size/background opacity in the layout editor.
- Configurable visibility, screen corner, exact position offsets, size, and background opacity.
- Optional visibility toggle key, **unassigned by default**.
- Settings persist between launches. Configuration labels and help support English and Japanese.

## Requirements

- **Minecraft 1.21.1**
- **NeoForge 21.1.251 or newer** within Minecraft 1.21.1
- **FTB Quests (NeoForge)** and its required dependencies
- **Java 21**

Install this HUD on the **client**. FTB Quests must already be installed and provide quest/team data to that client.

Confirmed gameplay environment: **All the Mods 10 v8.2 / NeoForge 21.1.251 / FTB Quests 2101.1.36**. Other modpacks and FTB Quests versions are not yet verified. **ATM10 v8.5 is not included in the confirmed environments for 1.0.0.** Any required compatibility fix will be handled separately.

## Installation and settings

1. Close Minecraft and place the JAR in your instance's `mods` folder.
2. Remove any older Quest Progress HUD JAR, keeping only one version.
3. Start the game and enter a world. The HUD waits until quest definitions and team progress have synced.

Open **Mods → Quest Progress HUD → Config**:

- **Edit HUD layout:** drag to reposition and preview size/background opacity while in a world. Save applies changes; Cancel/Escape discards them. Reset previews the default layout.
- **Numeric HUD settings:** configure visibility, corner, exact offsets, size, and background opacity.
- **Select chapter:** choose All quests or one chapter, then Save.

Assign the visibility key through **Options → Controls → Key Binds → Quest Progress HUD → Toggle quest HUD**. F1 hides the HUD with Minecraft's interface without changing its saved visibility setting.

Upgrading from v0.0.10 to 1.0.0 retains existing config and key bindings; no reset is required.

## How progress is counted

Progress uses the currently synced **team's completion data**, rather than a separate personal counter.

**All quests** is the default and includes every registered quest, including hidden, optional, repeatable, and internal quests. When a chapter is selected, both completed and total counts are limited to that chapter. No additional filtering is applied.

The percentage is rounded to one decimal place. Repeatable quests count according to their current completion state, not the number of times repeated. An empty quest file or chapter displays **0 / 0 and 0.0%**.

Chapter selection is saved per Minecraft instance. If the saved chapter is absent in another world's quest data, the HUD displays **Chapter unavailable** until you select another chapter or All quests.

## Release status

**1.0.0 is the first stable release**, carrying forward the functionality of v0.0.10 after continued stable gameplay reported by the author in ATM10 v8.2. Broader modpack compatibility and multiplayer/team transitions still need further in-game verification.

## Source and support

- [Source code and documentation](https://github.com/keisuke111/quest-progress-hud)
- [Report a bug or request a feature](https://github.com/keisuke111/quest-progress-hud/issues/new/choose)
- [Changelog](https://github.com/keisuke111/quest-progress-hud/blob/main/CHANGELOG.md)

License: **All Rights Reserved**.
