# Quest Progress HUD

A lightweight Minecraft HUD mod that displays FTB Quests progress without opening the quest screen.

## Current status

**v0.0.4 – FTB Quests progress with percentage**

The current build renders:

```text
Quests: 479 / 4790 (10.0%)
```

in the top-left corner while playing in a world.

The values are read from the client's synced FTB Quests data and refreshed once per second.
The percentage is completed / total × 100, rounded to one decimal place (0.0% when total is zero).
While quest data is syncing, the HUD shows `Quests: Loading...`.

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
5. Put `quest-progress-hud-0.0.4.jar` into the ATM10 instance's `mods` folder.
6. Remove any older Quest Progress HUD jar.
7. Start ATM10 and enter a world.

## Development

The project is based on the official NeoForge 1.21.1 ModDevGradle MDK.

This repository is currently in early development.
