---
doc_type: roadmap
status: living
owner: core-team
last_updated: 2026-05-14
version_anchor: 0.0.1
---
# Shadow Ascent Clean Start Roadmap

## Vision

Deliver a complete narrative Metroidvania campaign with authored critical progression, elastic optional content, evolving hubs, and deterministic validation-friendly runtime systems.

North-star extension: evolve this foundation into a systemic open-world narrative RPG framework through staged post-M4 milestones (M5/M6), without bypassing current campaign quality gates.

## Status Legend

- `completed` - implemented and validated against current scope.
- `active` - currently being built or hardened.
- `queued` - planned but blocked by upstream milestones.

## Milestones

### M0 — Clean Start Foundation (`completed`)

Delivered:

- Repository scaffold and Java module boundaries (`core`, `client`, `server`).
- Initial campaign/QA docs.
- Baseline build/version metadata.

### M1 — Act I QA Gate (`completed`)

Target:

- Act I first-session route as a reliable quality gate.
- Objective-driven mission flow (not simulated auto-completion).
- Hub/NPC transitions validated by state and data contracts.

Delivered in this lane:

- Contract-backed mission and side-quest runtime with per-objective tracking.
- Progression-bound hub state transitions (flag/beat aware).
- Contract validation policy toggle (`WARN` vs `FAIL_FAST`).
- Playable-client bounded traversal slice with moving platforms and ability-gated blockers.
- Playable-client mission/HUD feedback uplift for route readability.
- Playable-client authored four-room traversal route with camera-follow and ability-execution route activators.
- Playable-client first combat encounter/runtime slice with attack input, encounter clears, and combat-seal gating.
- Playable-client telegraphed combat state windows (telegraph/vulnerable/recover) for timing-based attack validation.
- Playable-client evidence logging now emits movement-feel sign-off telemetry (`MOVEMENT_SIGNOFF`) for manual donor-parity notes.
- Movement sign-off completed (2026-05-07): dash direction-lock bug fixed (SHIFT modifier key binding gap), dash cooldown raised to 1.0s. Evidence in `docs/ACT_I_QA_ROUTE.md`.

Exit criteria:

- First-session route completes without scripted shortcuts.
- Save/load preserves route-critical state.
- Route can be replayed as a regression gate.

### M2 — Campaign Spine Integration (`completed`)

Target:

- Multi-act progression driven by data contracts (`narrative_beats`, `story_flags`, `plateaus`).
- Authored critical path + elastic optional path separation in runtime rules.
- Story-state continuity through act transitions.

Exit criteria:

- Runtime can resolve next critical beat from current story state.
- Core progression rules are sourced from data contracts, not only hardcoded branching.

Delivered in this lane:

- Canonical mission templates loaded from `quests.json` + `narrative_beats.json`.
- Side-quest chain availability/completion rules sourced from contract fields.
- Typed reward effects sourced from explicit `reward_effects` schema fields.

### M3 — Stability and Release Readiness (`completed`)

Closed: 2026-05-08. Gate evidence: `docs/M3_RELEASE_GATE.md`.

Target:

- Save format versioning and migration compatibility guardrails.
- Expanded automated checks (data contracts + progression + persistence).
- Release candidate workflow with evidence capture.

Exit criteria:

- Regression suite and contract diagnostics pass in repeatable runs.
- Release checklist is green for internal QA build.

Delivered in this lane:

- Versioned save envelope + backward-compatible legacy load path (`v0` warn, `v1`/`v2` migrate, `v3` current).
- First concrete forward migration paths implemented (`v1`→`v2`→`v3`) with regression checks.
- CI defaults now enforce fail-fast contract validation mode.
- SAVE_V3 envelope with `region_overlays_b64` + `checksum_sha256` (SHA-256 payload integrity guard) — 2026-05-08.
- `testSaveChecksumGuard` (4 sub-tests) and `testSaveV3OverlayPersistence` in regression harness.
- Formal release gate checklist defined and all 12 criteria confirmed green (`docs/M3_RELEASE_GATE.md`).

### M4 — Campaign Completion and Content Scale (`active`)

Target:

- Extend authored coverage through later acts.
- Integrate optional plateau content while preserving campaign truth.
- Deepen emotional mechanics and hub evolution outcomes.

Already delivered in this lane:

- `SUMMIT_SHRINE` authored critical route and quest-policy/chunk-grammar support.
- `HOLLOW_DEPTHS` authored critical route plus four side-quest chains and supporting NPC/dialogue data.
- LibGDX `runGame` production-client slice now supports the HUD/modal/title/dialogue/pause/save/load path needed to carry broader authored content.

Exit criteria:

- Act coverage expands beyond current scaffold with validated transitions.
- Optional content does not violate critical story constraints.
- Remaining authored campaign spine is explicitly represented and runtime-legible through:
  - `EMBER_MONASTERY`
  - `WINDING_SKYROAD`
  - `MIRROR_SUMMIT`
  - `BEACON_CLIFF`
- `runGame` can present authored area identity, authored NPC placement, and authored mission prompts instead of relying only on fixture/bounded bootstrap behavior.
- `runGame` is the primary forward QA surface, with `HOLLOW_DEPTHS` used as the current authored-runtime deepening slice before `EMBER_MONASTERY` expansion.

### M5 — Systemic World Simulation Foundation (`completed`)

Target:

- Introduce contract-backed world-state runtime (`world_state`, `faction_state`, `settlement_state` envelopes).
- Add deterministic simulation ticks for world pressure signals (prosperity, corruption, route risk, faction tension).
- Bind mission/quest candidate generation to world-state signals (not only static chains).

Exit criteria:

- World-state contracts load/validate under diagnostics.
- Simulation ticks are deterministic under seeded regression runs.
- Quest availability can be traced to explicit world-state causes in logs.

Delivered in this lane:

- Contract files added for `world_state`, `faction_state`, and `settlement_state`.
- `GameDataContracts` now indexes/validates world/faction/settlement state definitions with plateau/flag cross-reference checks and numeric range guards.
- Added world simulation diagnostics command: `runWorldSimulationDiagnostics`.
- Added deterministic `WorldSimulationTick` loop with seeded replay behavior and emitted pressure-change events.
- Added regression coverage for M5 contract presence/reference integrity and tick determinism.
- Wired simulation event stream into mission/side-quest availability scoring with per-mission traceability output.
- `QuestEcologyEngine` delivers typed `QuestOpportunity` records (CORRUPTION_SURGE, PROSPERITY_CRISIS, ROUTE_HAZARD, FACTION_CONFLICT, STABILITY_WINDOW) from simulation pressure samples; wired into `MissionManager`; two regression tests pass. Also closes first M6 task (2026-05-07).

### M6 — Open-World Runtime Expansion (`active`)

Target:

- Expand from bounded authored route into constraint-driven regional streaming topology.
- Add runtime world mutation overlays (seasonal/environmental/faction consequences) with state persistence.
- Add cooperative traversal validation design lane (non-shipping prototype scope) without breaking single-player progression truth.

Exit criteria:

- Regional streaming constraints pass validation (connectivity, accessibility, no softlocks).
- Runtime mutation state persists through save/load and regression replays.
- Co-op traversal validation harness demonstrates recovery guarantees in constrained scenarios.

Delivered in this lane (2026-05-08):

- Regional streaming core: `RegionManifest`, `RegionInstance`, `RegionLoader`, `MutationOverlay`, `RegionalStreamingConstraintValidator`, `RegionFragmentData`, `SocketBinding`, `ZoneOverride`, `ResolvedAnchor`, `RegionLoadException` in `core.world.streaming`; 3 authored fragment JSONs under `data/worldgen/regions/`; `runRegionalStreamingDiagnostics` Gradle task.
- PlaytestClient → RegionLoader wiring: `buildPlaytestProgressionGraph`, `initializeCollisionLayout`, `buildAllDynamicTiles`, `refreshCollisionHashFromRegions`.
- SAVE_V3 overlay persistence: `OverlayPayloadCodec`, V3 envelope, v2→v3 migrator, PlaytestClient save/load wired.
- M6 authored region templates: 3 new section templates (`lantern_region_hub`, `lantern_hub`, `hollow_dungeon`); template count 10→13; all stub-geometry WARN lines eliminated.
- Co-op session scaffolding: `requestRevive` + `tickCoopProximity` + `COOP_REVIVE`/`PLAYER_PROXIMITY` events in `GameSimulator`.
- GameSimulator full feature set: enemy damage loop, boss pattern dispatch, shuriken flight, moving platforms + portals, echo recorder integration.
- Regression harness: 49/49 PASS (was 28/28 at M6 start).

## Architecture Tracks

- `core runtime` - simulation, mission, progression, save-state.
- `data contracts` - narrative/world contract loading and validation.
- `world generation` - section templates, socket contracts, progression validation.
- `client experience` - UI/HUD/camera/playability.
- `qa and diagnostics` - reproducible checks and evidence capture.

## Immediate Priorities

1. ~~Close M1 with manual Act I QA evidence capture and sign-off logs.~~ Completed 2026-05-07 — evidence in `docs/ACT_I_QA_ROUTE.md`, movement sign-off closed, M1 promoted.
2. ~~Upgrade playable client to donor-grade movement/physics feel and capture sign-off evidence.~~ Completed 2026-05-07 — dash direction-lock and cooldown fixed, sign-off closed.
3. ~~Continue Wave 4 PlaytestClient decomposition.~~ Complete 2026-05-08 — CombatSubsystem, TraversalSubsystem, UISubsystem extracted; Wave 4 sim actor slices all complete; 35/35 pass.
4. ~~Implement SAVE_V3 scaffolding and migrator.~~ Complete 2026-05-08 — SAVE_V3 envelope with `region_overlays_b64` + `checksum_sha256`; v2→v3 migrator; overlay persistence; 37/37 pass.
4a. ~~Wave 5 UI/playability overlay slice.~~ Complete 2026-05-08 — `InventoryPanel`, `ShopPanel`, `CraftingPanel`; 40/40 pass.
4b. ~~Wave 4 GameSimulator entity-wiring and full feature set.~~ Complete 2026-05-08 — entity registry, tick loop, enemy damage loop, boss dispatch, shuriken flight, platforms/portals, echo integration, co-op scaffolding; 49/49 pass.
5. ~~Continue M5 with quest-ecology opportunity generation.~~ Completed 2026-05-07 — `QuestEcologyEngine` delivered; M5 promoted to complete.
6. ~~Advance M6 — regional streaming, authored templates, mutation persistence.~~ Complete 2026-05-08 — all major M6 tasks delivered; see M6 "Delivered" section above.
7. ~~Keep docs synchronized with code truth after each milestone change.~~ Complete 2026-05-08 — `docs/DOC_MAINTENANCE_PLAN.md` created with ownership table, update triggers, staleness rules, and discoverability conventions; full doc audit pass completed.

### Next open items

- ~~M3 exit criteria.~~ Closed 2026-05-08 — gate doc at `docs/M3_RELEASE_GATE.md`; M3 promoted to complete.
- M4 campaign content remains active: authored `SUMMIT_SHRINE` and `HOLLOW_DEPTHS` are landed, while `EMBER_MONASTERY` through `BEACON_CLIFF` still need broader runtime-facing authored coverage and validation gates.
- ~~M6 remaining: authored chunk-streaming geometry.~~ Resolved 2026-05-08. Region fragment JSONs carry world-space tile geometry; `RegionLoader` sources static tiles from fragment data.
- ~~M6 remaining: echo puzzle evaluation logic.~~ Resolved 2026-05-09. `EchoPuzzleSolution` + `EchoPuzzleEvaluator` in `core.simulation`; first authored puzzle room in PlaytestClient Room 4.
- ~~M6 remaining: mutation visibility evidence.~~ Resolved 2026-05-08. `overlayStatusLine` in HUD; `MUTATION_OVERLAY_SAVE`/`LOAD` evidence lines; `refreshOverlayHud()` wired.
- ~~M6 remaining: co-op collision model.~~ Resolved 2026-05-08. `tickCoopCollisions()` pairwise AABB separation; `PLAYER_COLLISION` event; 51/51 pass.
- ~~**P1 LibGDX bootstrap**~~ — complete 2026-05-09. `DesktopLauncher` + `ShadowAscentGame` + `HubScreen` + `runGame` task; `GameInputProcessor` (full key bindings); `StubWorldRenderer` (tile + entity rects); `GameSimulator` wired with stub geometry + `CollisionWorld` injection.
- ~~**P2 collision + camera**~~ — complete 2026-05-09. `CollisionWorld.resolveX` + `resolveY` wired in `GameSimulator.tickPlayers()`; Y-axis camera orientation fixed (`setToOrtho(true)`); camera world-bounds clamping from tile extents; 54/54 regression pass.
- **P3 asset pipeline** — complete 2026-05-10. Deterministic `packSprites` task, generated placeholder atlas, `TextureAtlas` loading in `ShadowAscentGame`, and atlas-backed `SpriteWorldRenderer` are all landed and client-test-backed.
- **P4 HUD + overlay port** — complete 2026-05-13. `runGame` now has a persistent HUD, docked toggleable minimap, shared modal lifecycle, first-pass inventory/shop/crafting overlays, dialogue interaction, pause/save/load flow, title/new-game/continue routing, audio event-key resolution, and explicit HUD interaction hints driven directly from the production-client runtime.
- **Next LibGDX production-client follow-up**: keep the lane bounded to authored geometry, animation/state fidelity, and deeper production-client interaction polish that can be validated in `runGame` without turning into full art migration.
- **Current M4 follow-up**: finish the `HOLLOW_DEPTHS` runtime-facing slice first by deepening authored bootstrap, NPC placement, combat readability, and mission surfacing in `runGame`, then use that path as the pattern for `EMBER_MONASTERY`.
