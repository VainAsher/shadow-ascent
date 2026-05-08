# Shadow Ascent Clean Start

Clean implementation home for building **Shadow Ascent** as a complete start-to-finish campaign.

## What This Repo Is

This repository is the canonical build destination for the game vision, using prior repos as reference sources.

- `indie-ninja-adventures` is the primary engineering donor (simulation, worldgen, validation patterns).
- `shadow_ascent_integrated_package` is the primary narrative/data-contract donor (plateaus, beats, flags, quest-chain semantics).

## Current Truth (2026-05-06)

- **M0 Foundation:** completed (scaffold, module boundaries, initial docs).
- **M1 Act I QA Gate:** in progress (objective-driven mission runtime is implemented; route hardening remains).
- **M2 Campaign Spine:** in progress (data contracts validate and resolve beats; canonical + side-quest mission templates are contract-backed).
- **M3 Stability:** active prep (save v2 migration path, CI fail-fast contract gate).
- **Playable Client:** MVP interactive client now exists (`runPlayableClient`); donor-grade player/physics import is active with first collision-complexity geometry and minimap overlays landed.

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
./gradlew runPlayableClient
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
- `docs/ROADMAP.md`
- `docs/MIGRATION_MAP.md`
