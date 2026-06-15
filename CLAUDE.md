# Shadow Ascent — Claude Code Context

## What This Repo Is

Clean-start canonical build for Shadow Ascent: a full campaign narrative Metroidvania with a staged expansion track toward systemic open-world RPG architecture. Two donor repos feed it via controlled wave imports:
- `indie-ninja-adventures` — simulation, worldgen, ECS, validation patterns
- `shadow_ascent_integrated_package` — narrative data contracts, plateau/beat/flag/quest semantics

## Module Structure

```
java/core/     — simulation, story, physics, ECS, world systems (no UI, no input)
java/client/   — player-facing runtime entry point (PlaytestClient, HubMissionDemo)
java/server/   — authoritative/server-side scaffold
data/          — canonical narrative and world data contracts (JSON)
docs/          — product, roadmap, migration, and architecture docs
```

Java package root: `shadowascent.{core,client,server}.*`

## Layer Contracts

- `core` must not import from `client` or `server`
- Physics, ECS, and world simulation live in `core` only
- Input handling and rendering belong in `client` only
- `core` systems must be testable without a running client (regression harness enforces this)

## Data Contracts — Canonical Sources

These files are the source of truth. **Never hardcode IDs, names, or values they define.**

| File | Canonical for |
|---|---|
| `data/plateaus.json` | plateau IDs, act assignments, tag ranges |
| `data/narrative_beats.json` | beat IDs, milestone flags, unlock conditions |
| `data/story_flags.json` | all canonical story flag keys |
| `data/quests.json` | quest IDs, steps, reward_effects, availability gates |
| `data/dialogue.json` | NPC dialogue lines keyed by NPC + context |
| `data/npc_registry.json` | NPC IDs, display names, roles, eligible plateaus |
| `data/chunk_grammar.json` | traversal chunk tag rules and plateau grammar |
| `data/area_catalog.json` | area IDs and region assignments |
| `data/world_state.json` / `faction_state.json` / `settlement_state.json` | M5 simulation scaffold |

When adding new narrative content, cross-reference all five core contracts (plateaus, beats, flags, quests, chunk_grammar) to avoid tag leakage and flag conflicts.

## Migration Rules

- Imports from donor repos must be **sliced**, not bulk-pasted — bring in only what the current wave requires
- Wave imports must compile and pass full regression before the next slice begins
- Wave 4 is complete: physics collision primitives + all sim actor slices (SimEnemy, SimPlayer, SimBoss, entity completions, crafting, PlayerInputController) + full GameSimulator feature set (entity wiring, enemy damage loop, boss dispatch, shuriken flight, platforms/portals, echo integration, co-op scaffolding)
- Wave 5 is complete: PlaytestClient UX slice (InventoryPanel, ShopPanel, CraftingPanel, MinimapRenderer)
- Future waves: scope against `docs/MIGRATION_MAP.md` and `docs/IMPLEMENTATION_BACKLOG.md`

## Regression Gate

Every change must pass:

```bash
./gradlew runRegressionTests
./gradlew runDataContractDiagnostics
```

For world/worldgen changes also run:

```bash
./gradlew runWorldgenDiagnostics
./gradlew runWorldSimulationDiagnostics
```

Full gate (what CI runs):
```bash
./gradlew clean :core:compileJava :client:compileJava :server:compileJava runDataContractDiagnostics runWorldgenDiagnostics runWorldSimulationDiagnostics runRegressionTests
```

## Contract Validation Mode

- **Local default:** `WARN` — contracts load with warnings, no hard failure
- **CI default:** `FAIL_FAST` — set via `SHADOWASCENT_CONTRACTS_VALIDATION_MODE=fail_fast`
- Never disable FAIL_FAST on CI without a documented reason

## Save Schema Policy

- Current save version: `SAVE_V3` (envelope: `SAVE_V3|story_state_b64=...|region_overlays_b64=...|checksum_sha256=...|encoding=utf8_base64`)
- `SAVE_V1` and `SAVE_V2` are migratable; legacy (v0) loads with warning
- `SAVE_V4+` is reserved/unsupported until new schema fields are introduced
- Migration matrix: `SaveMigrationMatrix.java`
- When adding `SAVE_V4` fields: implement a concrete migrator in the matrix before shipping

## Current Milestone State (as of 2026-06-15)

| Milestone | Status |
|---|---|
| M0 Foundation | Complete |
| M1 Act I QA Gate | Complete (2026-05-07) |
| M2 Campaign Spine | Complete (2026-05-07) |
| M3 Stability/Release | Complete (2026-05-08) |
| M4 Campaign Content Scale | Complete (2026-05-15) |
| M5 World Simulation Foundation | Complete (2026-05-07) |
| M6 Open-World Runtime Expansion | Active (2026-05-08) |

Gate criteria: `docs/NORTH_STAR_EXECUTION_MATRIX.md`

## Production Client Stack (decided 2026-05-08)

The shipping client is **LibGDX (`lwjgl3` backend)**, not Swing. This is the target for all player-facing development from Phase 1 onward.

- **`DesktopLauncher` + `ShadowAscentGame`** will be the LibGDX entry points in `java/client/`.
- **`PlaytestClient` (Swing)** remains as the regression/QA harness until LibGDX client reaches feature parity. It is not deleted during migration. Both coexist as separate Gradle tasks (`runPlayableClient` and `runGame`).
- **`GameSimulator.drainEvents()`** is the canonical event bus between simulation and presentation. No pub/sub framework needed.
- **Custom AABB physics** only — Box2D is explicitly ruled out for this project.
- **Tiled `.tmx` maps** are the authored level geometry format (pairing with existing `SectionTemplate` JSON for semantic metadata).
- **HTML5/GWT backend** and **multiplayer networking** are out of scope for v1.0.

Full production stack and phase plan: `docs/planning/PRODUCTION_STACK_AND_LONG_TERM_PLAN.md`

## Key File Warnings

**`PlaytestClient.java` (~80KB)** — Swing QA harness. Wave 4/5 subsystem extractions complete (CombatSubsystem, TraversalSubsystem, UISubsystem, MinimapRenderer, HudRenderer, StoryManager, MissionUiCoordinator). Do not add new feature code; the LibGDX client is the path forward. See `docs/planning/PRODUCTION_STACK_AND_LONG_TERM_PLAN.md`.

**`RegressionTest.java` (~100KB, 54 tests)** — regression harness. When adding new test cases, review logic for correctness, not just structure. New test sections follow existing labelled-section pattern.

**`GameDataContracts.java` (~37KB)** — contract loader/validator. The runtime source of truth for data lookups. Additions here must maintain backward compatibility with existing contract consumers.

## Naming and Pattern Conventions

- ECS components: extend `Component`, follow `TransformComponent`/`HealthComponent`/`AIComponent` pattern
- Gradle tasks: verb + noun (e.g., `runRegressionTests`, `runWorldgenDiagnostics`)
- Regression test sections: labelled blocks with pass/fail counters matching existing harness structure
- Mission templates: loaded from `quests.json` + `narrative_beats.json` via `ContractMissionTemplateCatalog`, not hardcoded

## Operating Pattern — Claude→Codex Worker Loop

Claude Code is the architect. Codex CLI is the bounded terminal worker. The user is the approval gate.

- Classify every task before acting: `DESIGN` | `INSPECT` | `VALIDATE` | `PATCH` | `REVIEW`
- Prefer targeted file reads, grep summaries, and Codex compressed reports over full file or log dumps
- Never advance from VALIDATE to PATCH without an explicit narrow task boundary
- Ask the user before broad refactors, API changes, narrative canon changes, or any write-mode Codex task larger than a narrow patch
- Codex tasks live in `.codex/tasks/` — invoke with `codex exec "$(Get-Content .codex\tasks\<name>.md -Raw)"`

Full pattern, invocation templates, and approval gates: `docs/guides/DEVELOPER_WORKFLOW.md`

## Architecture Docs

- `docs/CURRENT_STATE.md` — runtime snapshot, verification evidence, active risks
- `docs/ROADMAP.md` — milestone definitions and completion criteria
- `docs/MIGRATION_MAP.md` — donor wave import map and status
- `docs/IMPLEMENTATION_BACKLOG.md` — ordered task backlog per milestone
- `docs/NORTH_STAR_EXECUTION_MATRIX.md` — acceptance test targets per milestone
- `docs/CANONICAL_ARCHITECTURE_PLAN.md` — layer boundary and module decisions
- `docs/planning/PRODUCTION_STACK_AND_LONG_TERM_PLAN.md` — shipping stack decisions + phase plan P0–P11
- `docs/DOC_MAINTENANCE_PLAN.md` — doc ownership table, update trigger rules, staleness prevention
