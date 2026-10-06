# Quest Progress HUD

A lightweight Minecraft HUD mod that displays FTB Quests progress without opening the quest screen.

## Current status

**v0.0.5 – Compact HUD with progress bar**

The compact panel renders:

```text
Quests       10.1%
482 / 4790
[progress bar]
```

in the top-left corner while playing in a world, using Minecraft's font.
A translucent dark background keeps the text readable. The percentage and thin
progress bar use a soft green accent; the total count uses gray.
The panel expands horizontally for longer counts, and hides the percentage and
bar until quest data is loaded.

The values are read from the client's synced FTB Quests data and refreshed once per second.
The percentage is completed / total × 100, rounded to one decimal place (0.0% when total is zero).
While quest data is syncing, the panel shows `Quests` and `Loading...`.

Quest counting is unchanged from v0.0.3: the denominator counts every registered Quest,
including hidden, repeatable, and internal quests. No filtering is applied yet.

## Target

- Minecraft 1.21.1
- NeoForge 21.1.251+
- FTB Quests 2101.1.36 (compile target)
- Primary test environment: All the Mods 10 v8.2

## Download a test build

1. Open the **Actions** tab on GitHub.
2. Open the latest successful **Build** workflow.
3. Download the `quest-progress-hud` artifact.
4. Extract the ZIP.
5. Put `quest-progress-hud-0.0.5.jar` into the ATM10 instance's `mods` folder.
6. Remove any older Quest Progress HUD jar.
7. Start ATM10 and enter a world.

## Development

The project is based on the official NeoForge 1.21.1 ModDevGradle MDK.

This repository is currently in early development.
