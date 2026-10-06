# Quest Progress HUD

A lightweight Minecraft HUD mod that displays FTB Quests progress without opening the quest screen.

## Current status

**v0.0.3 – FTB Quests progress test**

The current build renders:

```text
Quests: 342 / 500
```

in the top-left corner while playing in a world.

The values are read from the client's synced FTB Quests data and refreshed once per second.

## Target

- Minecraft 1.21.1
- NeoForge 21.1.251+
- FTB Quests 2101.1.x
- Primary test environment: All the Mods 10 v8.2

## Download a test build

1. Open the **Actions** tab on GitHub.
2. Open the latest successful **Build** workflow.
3. Download the `quest-progress-hud` artifact.
4. Extract the ZIP.
5. Put the `.jar` file into the ATM10 instance's `mods` folder.
6. Remove any older Quest Progress HUD jar.
7. Start ATM10 and enter a world.

## Development

The project is based on the official NeoForge 1.21.1 ModDevGradle MDK.

This repository is currently in early development.
