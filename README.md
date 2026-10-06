# Quest Progress HUD

A lightweight Minecraft HUD mod that displays FTB Quests progress without opening the quest screen.

## Current status

**v0.0.7 – Framed compact HUD with an experience-style gauge**

The compact panel renders:

```text
Quests       10.1%
482 / 4790
[progress bar]
```

in the top-left corner while playing in a world, using Minecraft's font.
A translucent dark background with a thin, subtle beveled frame keeps the text
readable. Tighter spacing gives the panel a compact shape. The percentage uses
a soft green accent; the total count uses gray. The progress gauge has a dark
inset track, green highlights and small notches inspired by Minecraft's experience bar.
It still represents quest completion, not player experience.
The panel expands horizontally for longer counts, and hides the percentage and
bar until quest data is loaded.

The values are read from the client's synced FTB Quests data and refreshed once per second.
The percentage is completed / total × 100, rounded to one decimal place (0.0% when total is zero).
While quest data is syncing, the panel shows `Quests` and `Loading...`.

Quest counting is unchanged from v0.0.3: the denominator counts every registered Quest,
including hidden, repeatable, and internal quests. No filtering is applied yet.

## HUD settings

Open **Mods → Quest Progress HUD → Config** and edit the client settings.
Close the settings with **Done** to save; the HUD uses the updated values when
you return to the world, without restarting. Settings persist across launches
in `config/quest_progress_hud-client.toml` within the instance.

| Setting | Default | Options |
| --- | --- | --- |
| Show HUD | On | On / Off |
| Screen corner | Top left | All four corners |
| Horizontal / vertical offset | 0 | -10000 to 10000 GUI pixels |
| HUD size | 1.0 | 0.5 to 3.0, relative to Minecraft GUI scale |
| Background opacity | 72% | 0% (transparent) to 100% (opaque) |

Positive offsets move inward from the selected edges, starting at the default
8 GUI-pixel margin. Positions are clamped to the screen. If the selected size
cannot fit on a small screen, the HUD automatically shrinks to fit.
The v0.0.6 display settings and saved configuration remain compatible.
The frame fades with background opacity and disappears at 0%. Settings labels and help are available
in English and Japanese; the HUD keeps its original English labels.

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
5. Put `quest-progress-hud-0.0.7.jar` into the ATM10 instance's `mods` folder.
6. Remove any older Quest Progress HUD jar.
7. Start ATM10 and enter a world.

## Development

The project is based on the official NeoForge 1.21.1 ModDevGradle MDK.

This repository is currently in early development.
