# Changelog

## 1.0.0 — 2026-11-06

First stable release, based on v0.0.10 after continued stable use reported by the author in ATM10 v8.2.

- Preserve the compact HUD, live layout editor, optional visibility key and chapter selection.
- Preserve existing config/key bindings, one-second refresh, team progress and all-registered-quests default.
- Update release/download documentation, mod-page links and maintenance wording.
- Include the repository license in the jar and verify packaged metadata/version, English/Japanese resources and standalone jar contents.
- Keep Minecraft 1.21.1 / NeoForge 21.1.251 / FTB Quests 2101.1.36 as the tested target. ATM10 v8.5 remains unverified; any required compatibility fix is deferred to 1.0.1.

## 0.0.10 — 2026-10-13

Beta release.

### Added

- Live HUD layout editor: drag the HUD and preview scale/background opacity over the game view. Save applies the layout; Cancel/Escape discards it; Reset previews defaults. (#3)
- Optional **Toggle quest HUD** key, unassigned by default, with saved visibility and protection against repeated or menu input. (#6)
- Chapter selection with paged titles, identifying tooltips, and saved chapter IDs. A missing chapter displays **Chapter unavailable**. (#7)

### Compatibility and defaults

- Minecraft 1.21.1, NeoForge 21.1.251 and FTB Quests 2101.1.36 remain the primary tested versions, in ATM10 v8.2.
- One-second progress refresh and the compact HUD design are unchanged.
- **All quests** remains the default. Hidden, optional, repeatable and internal quests are still included; chapter selection limits completed and total counts to the same chapter.
- Existing HUD settings remain compatible. Configuration labels/help support English and Japanese.

## 0.0.9

- Initial public beta: compact team quest counts, percentage and progress bar; saved visibility, corner, offsets, size and background opacity settings.

See [v0.0.9 release notes](releases/v0.0.9.md) for its environment and file verification details.
