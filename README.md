# Shadow Ascent Clean Start

Clean implementation home for building **Shadow Ascent** as a complete start-to-finish campaign.

## What This Repo Is

This repository is the canonical build destination for the game vision, using prior repos as reference sources.

- `indie-ninja-adventures` is the primary engineering donor (simulation, worldgen, validation patterns).
- `shadow_ascent_integrated_package` is the primary narrative/data-contract donor (plateaus, beats, flags, quest-chain semantics).

## Current Truth (2026-05-14)

- **M0 Foundation:** completed.
- **M1 Act I QA Gate:** completed.
- **M2 Campaign Spine:** completed.
- **M3 Stability:** completed.
- **M4 Campaign Completion and Content Scale:** active.
- **M5 Systemic World Simulation Foundation:** completed.
- **M6 Open-World Runtime Expansion:** active.
- **Production Client:** LibGDX `runGame` is now the primary forward runtime and QA surface. It includes a title/new-game/continue flow, persistent HUD, toggleable minimap, dialogue modal, pause/save/load flow, modal inventory/shop/crafting overlays, audible placeholder SFX/music via the runtime audio registry, contextual interaction hints, and a deeper authored Hollow Depths bootstrap path with area-specific NPC presence on top of the atlas-backed placeholder rendering route. `runPlayableClient` is retained as a legacy Swing reference/intermediary layer, not the main forward play surface.

## Repository Structure

- `docs/` - canonical product, roadmap, migration, and architecture docs.
- `data/` - narrative/world data contracts imported from integrated source.
- `java/core/` - shared simulation and story systems.
- `java/client/` - player-facing runtime entry point.
- `java/server/` - authoritative/server-side entry point scaffold.

## Verification Commands

From repository root:

```bash
./gradlew runRegressionTests
./gradlew runDataContractDiagnostics
./gradlew runWorldgenDiagnostics
./gradlew packSprites
./gradlew :client:test
./gradlew runPlayableClient
./gradlew runGame
```

`runRegressionTests` includes deterministic playable-controller checks (run/jump/double-jump/coyote/dash/wall-exhaustion gating), save v2/v1/legacy compatibility checks, and donor-parity worldgen checks.

Playable client controls: `A/D` run, `ALT` precision walk, `SPACE` jump, `SHIFT`/`C` dash, `F` attack, `S` fast-fall, `E` interact, `ENTER`/`TAB` start mission, `R` objective shortcut, `M` minimap toggle, `F5` save, `F9` load.

Playable session evidence logs are written to `logs/playtest/playtest_session_*.log`.

## Runtime Entry

```bash
java -jar build/libs/shadow-ascent-release-candidate.jar
```

## Canonical Docs

Start with:

- `docs/INDEX.md`
- `docs/CURRENT_STATE.md`
- `docs/PLAYABLE_TRUTH.md`
- `docs/ROADMAP.md`
- `docs/MIGRATION_MAP.md`
