---
doc_type: current_state
status: living
owner: core-team
last_updated: 2026-05-08
version_anchor: 0.0.1
---
# Current State

Canonical runtime and execution snapshot for the clean-start repository.

## Product State

- Product direction: full campaign narrative Metroidvania with staged expansion track toward systemic open-world narrative RPG architecture.
- Clean-start repo status: foundational scaffold with active migration/integration lane.
- Milestone truth:
  - M0 foundation: complete.
  - M1 Act I QA gate: complete (2026-05-07).
  - M2 campaign spine integration: complete (2026-05-07) — contract-backed mission/hub runtime exit criteria met; multi-act content authoring is M4 scope.
  - M3 stability/release: complete (2026-05-08). Gate evidence: `docs/M3_RELEASE_GATE.md`.
  - M4 campaign completion/content scale: queued.
  - M5 systemic world simulation foundation: complete (2026-05-07) — all three exit criteria met; QuestEcologyEngine closes first M6 task.
  - M6 open-world runtime expansion: active.

## What Is Implemented

- Java module structure and runnable entry points (`core`, `client`, `server`).
- Donor player/physics tuning baseline imported into `core.physics`:
  - `PhysicsConstants` (movement/jump/dash/gravity profile),
  - `PhysicsState` (runtime mutable per-entity physics state model),
  - `PlayableControllerModel` (deterministic controller simulation used by regression checks).
- Java client now includes a human-playtest MVP runtime (`PlaytestClient`) with:
  - windowed input loop with robust key-binding capture (A/D run + ALT precision walk + SPACE jump + SHIFT/C dash + F attack + interaction),
  - NPC proximity interaction and dialogue surfacing,
  - mission start/progression shortcuts for QA route traversal,
  - in-session save/load hooks for route continuity checks,
  - donor-profile movement tuning subset integrated with compact-world scaling (run/jump/wall-jump/dash tuned for current smaller playtest arena, plus optional precision walk),
  - platformer controller slice integrated (gravity + coyote-time + wall-coyote + wall-jump + wall-slide clamp + double-jump + wall-jump input-lock timing + wall stamina/exhaustion behavior),
  - dash input/reliability hardening integrated (`SHIFT` and `C` triggers, explicit cooldown/lock feedback logging, and shorter dash control-lock window to prevent prolonged direction pinning),
  - chunked collision lookup backing authored traversal geometry (platform routes, wall-jump surfaces, blockers) for non-flat exploration feel,
  - bounded dynamic traversal systems: moving platforms (carry motion) plus ability-gated blockers (`dash`, `combat_basic`) with lock feedback,
  - first authored multi-room traversal topology with camera-follow across four bounded rooms (hub -> forge terraces -> hollow shaft -> summit route),
  - ability-execution world interactions (dash-pass sigils + interact altars) that unlock route platforms and can advance compatible mission objectives,
  - first combat encounter runtime hooks with telegraphed state windows (`DORMANT -> TELEGRAPH -> VULNERABLE -> RECOVER`), attack-window validation, encounter-clear barriers, objective-progression integration, and pattern-based timing variety (STANDARD/FAST),
  - player consequence loop: health system (3 HP max), damage on mistimed attacks, death/reset mechanics with 2-second delay,
  - minimap overlay (`M` toggle) showing player/NPC positions, collision geometry, moving platforms, and gate lock/open state for route readability,
  - richer mission/HUD feedback: objective-progress completion bar, next-objective surfacing, ability unlock surfacing, traversal gate summaries, and mission-feed toast lines.
  - persistent session evidence logs written per run to `logs/playtest/playtest_session_*.log` with timestamped events and periodic runtime snapshots,
  - movement sign-off telemetry envelope emitted at session end (`MOVEMENT_SIGNOFF`) with movement/airtime/jump/dash/wall/damage/death metrics for manual donor-feel review.
- Story state enums and baseline progression scaffolding.
- Hub and mission manager scaffolding.
- Regression harness execution command.
- Deterministic playable-controller regression coverage in `runRegressionTests`:
  - donor-calibrated run-distance and jump-arc baselines,
  - coyote-window success/failure behavior,
  - dash trigger baseline,
  - wall stamina exhaustion and wall-jump gating behavior.
- Data contract layer scaffold:
  - contract files imported to `data/`,
  - runtime loader/validator (`GameDataContracts`),
  - next-critical-beat resolution,
  - diagnostics command (`runDataContractDiagnostics`),
  - strict quest `reward_effects` validation by effect type and payload requirements,
  - world simulation contract scaffolding (`world_state`, `faction_state`, `settlement_state`) with cross-reference validation.
- NPC/beat contract reconciliation for currently referenced narrative IDs.
- Objective-driven mission runtime:
  - per-objective progress and required-count tracking,
  - mission completion gated on objective satisfaction,
  - no auto-complete placeholder branch in mission progression flow.
- Contract-backed mission template loading:
  - canonical mission definitions hydrated from `quests.json` and `narrative_beats.json`,
  - `MissionManager` initialization now consumes contract templates instead of hardcoded mission text/objective definitions.
- Side-quest chain runtime from contracts:
  - quest steps loaded into mission templates from `quests.json`,
  - availability gated by `act`, `plateau`, `required_flags`, and chain completion flags,
  - completion applies quest `sets_flags` at runtime,
  - per-step objective decomposition derived from quest `objective`, `area_pool`, and `enemy` fields,
  - `reward_effects` contract arrays (`type`, `magnitude`, optional `payload`) parsed as authoritative typed runtime reward effects,
  - legacy `reward` tokens retained for compatibility only, with unknown fallback if explicit effects are absent.
- Hub progression runtime:
  - `HubManager` now resolves hub state from contract-driven progression flags (with next-critical-beat context) instead of manual linear stepping.
- Hub NPC schedule runtime:
  - NPC registry is now seeded from `npc_registry.json` contract definitions (IDs/display names/roles/eligible plateaus),
  - active NPC availability is derived from unlocked/unresolved beat context via `GameDataContracts.scheduledNpcIds(...)`,
  - handcrafted hub-state NPC tables are now fallback-only for empty contract-schedule edge cases.
- Hub NPC dialogue runtime:
  - high-frequency hub dialogue selection now routes through contract-authored `dialogue.json` lines via `GameDataContracts.selectNpcDialogueLine(...)`,
  - legacy hardcoded dialogue branches remain fallback-only when no matching contract line is available.
- Save-state runtime hardening:
  - save files now use versioned `SAVE_V3|story_state_b64=...|region_overlays_b64=...|checksum_sha256=...|encoding=utf8_base64` envelope format,
  - backward-compatible load support for legacy unversioned (`v0`), `SAVE_V1|story_state=...`, and `SAVE_V2|story_state_b64=...` saves,
  - full migration chain implemented: `v0` → v3, `v1` → v3, `v2` → v3 via `SaveMigrationMatrix`,
  - migration matrix policy: `v0` legacy raw state, `v1` migratable, `v2` migratable, `v3` current envelope, `v4+` reserved/unsupported,
  - checksum guard (`checksum_sha256`) added to V3 envelope; tampered or truncated saves fail with explicit error,
  - regression coverage updated for v3 + v2 + v1 + legacy compatibility, unsupported forward-version handling, and checksum guard.
- Contract validation startup policy:
  - runtime mode toggle implemented via `ContractValidationMode` (`WARN` or `FAIL_FAST`),
  - configuration supported through `shadowascent.contracts.validation.mode` (system property) or `SHADOWASCENT_CONTRACTS_VALIDATION_MODE` (env var).
- CI default validation policy:
  - GitHub Actions workflow now sets `SHADOWASCENT_CONTRACTS_VALIDATION_MODE=fail_fast` for compile/diagnostics/regression gates,
  - local runs remain warn-mode by default unless explicitly configured.
- Wave 2 worldgen validation import:
  - section template model/loading/validation classes imported into `core.world.sections`,
  - progression validation and generation-report/planner classes imported into `core.world.progression` and `core.world.validation`,
  - donor-authored `data/worldgen/sections` and initial `data/worldgen/progressions/act1.json` datasets imported,
  - `runWorldgenDiagnostics` now validates a non-zero load path,
  - regression coverage includes donor-parity variety/modulo-selection, validator parity, and strict-mode failure behavior.
- Wave 4 migration starter slice:
  - `core.physics` now includes donor-profile modular collision primitives (`TileType`, `TileRect`, upgraded `SpatialHash` with dynamic overlays and raycast),
  - regression harness now includes a dedicated Wave 4 spatial-hash behavior check, including locked-gate raycast blocking and unlocked-path clearing behavior,
  - playable-client runtime now includes a bounded combat loop slice (attack input, telegraphed encounter state machine, encounter clear states, and combat-seal route gating).
- Wave 5 playable-client UX slice:
  - `PlaytestClient` HUD now surfaces mission objective progress, objective routing hints, unlocked abilities, and traversal gate state,
  - mission feedback lines now flash on mission start, objective progress/completion, mission completion, and ability unlock events,
  - interaction hints now surface nearby ability-trigger affordances (dash-pass vs interact activation).
- Wave 4 GameSimulator entity-wiring slice (2026-05-08):
  - `SimEvent` record `(String type, String entityId, Map<String,Object> data)` — typed simulation event envelope drained per frame,
  - `GameSimulator` bounded coordinator: entity registry (`addPlayer/removePlayer/addEnemy/addBoss/addNpc/addPickup`), tick loop, event drain API (`drainEvents()`), snapshot diagnostics (`snapshot()`),
  - enemy type default table (goblin/bat/slime/skeleton/wolf) drives `addEnemy` factory,
  - `tickEnemy`: stun/flee override → UNAWARE→ALERTED with `ENEMY_AGGRO` emit → CHASE movement or PATROL oscillation,
  - `tickBoss`: INTRO timer expiry → `BOSS_INTRO_DONE`; HP ratio thresholds (75%/50%/25%) → `BOSS_PHASE_TRANSITION`; defeated → `BOSS_DEFEATED`,
  - `tickPickups`: AABB overlap vs each player → `PICKUP_COLLECTED`,
  - first bounded slice of the XL GameSimulator decomposition; full port of donor monolith remains deferred.
- Wave 4 moving platforms + portals (2026-05-08):
  - `tickMovingPlatforms`: `step()` oscillates platform; players standing on top carried by x-delta each tick,
  - `tickPortals`: `step(dt)` advances pulse; `PORTAL_ACTIVATED` emitted on proximity + ability-gate pass; `isActive=false` after first activation (one-shot).
- Wave 4 projectile flight (2026-05-08):
  - `fireShuriken(playerId, vx, vy)` spawns `SimShuriken` at player centre; `SHURIKEN_FIRED` emitted,
  - `tickShurikens(dt)` moves projectile each tick, AABB-tests against live enemies, emits `SHURIKEN_HIT` / `ENEMY_DEFEATED`, removes on hit or TTL expiry,
  - `getShurikens()` unmodifiable query; `overlaps()` shared AABB helper added.
- Wave 4 BossPatternLibrary dispatch (2026-05-08):
  - `tickBoss` calls `BossPatternLibrary.tick()` after INTRO expires; `PatternContext` built with players-by-slot + enemy list; null spawn/projectile stubs (both null-checked inside patterns),
  - `BOSS_SCRIPTED_LOSS` emitted when SIREN pattern returns `ServerEvent.SCRIPTED_LOSS`,
  - all 5 boss types (SIREN, ECHO_WARDEN, TIME_LEECH_LORD, MEMORY_EATER, VEIL_MAIDEN) tick without NPE.
- M6 authored region templates — stub geometry elimination (2026-05-08):
  - 3 new `data/worldgen/sections/` templates: `lantern_region_hub` (4×2), `lantern_hub` (3×2), `hollow_dungeon` (3×3),
  - `SectionTemplateLibrary` template count 10→13; zero validation issues,
  - 3 regression test constructors switched from `empty()`/`null` to `loadDefault()`; all `[WARN] RegionLoader: no template for` lines eliminated from test output.
- Wave 4 co-op session scaffolding in GameSimulator (2026-05-08):
  - `tickCoopProximity()` emits `PLAYER_PROXIMITY` each tick for each alive player pair within 80px,
  - `requestRevive(reviverPlayerId, targetPlayerId)`: revives dead target at `max(1, maxHealth/2)` HP + invincibility frames if reviver is alive and within `REVIVE_RANGE`; emits `COOP_REVIVE`; no-op if out of range or preconditions unmet.
- M6 co-op player AABB collision separation (2026-05-08):
  - `tickCoopCollisions()` runs pairwise overlap checks for all alive players each tick,
  - minimum-separation-axis push applied symmetrically (half-overlap each player) resolves overlap without tunnelling,
  - `PLAYER_COLLISION` event emitted per separated pair; dead players excluded; 51/51 regression tests pass.
- Wave 4 echo subsystem wired in GameSimulator (2026-05-08):
  - `tickPlayers` records `p.latestInput` into `p.echoRecorder` each tick (600-frame ring buffer),
  - `spawnEcho(playerId, echoId, looping)`: snapshots recorder, builds `ReplayPlayer.fromInputSequence`, creates `SimEcho` at player position; emits `ECHO_STARTED`,
  - `tickEchoes()`: steps each active echo per tick; emits `ECHO_COMPLETED` / `ECHO_FAILED` on terminal transitions; removes finished echoes; wired into `tick(dt)`,
  - `getEchoes()` unmodifiable query.
- Wave 4 enemy combat damage loop (2026-05-08):
  - `tickEnemy` ATTACK branch: `attackTimer` decremented per tick; on expiry `nearest.takeDamage(baseDamage)` called; `PLAYER_DAMAGED` emitted on landed hit; `PLAYER_DIED` + `respawnTimer=3.0f` on death,
  - invincibility frames (`SimPlayer.invincibilityTicks`) correctly suppress follow-up hits,
  - `attackEnemy(playerId, enemyId, damage)` public API: player-originated hit on named enemy; `ENEMY_DEFEATED` emitted on kill.
- Wave 5 UI overlay panels (2026-05-08):
  - `InventoryPanel` — 4×5 slot grid; use consumables, equip/unequip weapons + armor; toggle with `I` key,
  - `ShopPanel` — two-column buy/sell layout; `TradeRequest` record; opens via `E` near hub shop fixture (`merchant_npc` tier-2, seed 12345),
  - `CraftingPanel` — full `RecipeBook.all()` recipe list with ingredient-availability counts; craft on `Enter`; toggle with `T` key,
  - all three panels rendered natively in Swing `Graphics2D` (LibGDX patterns from donor adapted; not ported),
  - `PlaytestClient` seeded with `SimInventory playerInventory` (100 coins, dagger, 3 potions, 4 iron) and `SimShop hubShop` at startup,
  - arrow-key navigation + Esc close + contextual Enter (panel action or mission start) wired,
  - `processPanelInputs()` and `anyPanelOpen()` manage panel input routing in `tick()`.
- Wave 5 MinimapRenderer extraction (2026-05-08):
  - `MinimapRenderer` standalone class in `com.shadowascent.client`; extracted from `UISubsystem.drawMinimap`,
  - full minimap rendering logic owned by `MinimapRenderer.draw(Graphics2D g, UISubsystem.RenderState state)`,
  - `UISubsystem` delegates via `minimapRenderer.draw(g, state)`; `drawMinimap` method body deleted,
  - `activeNpcsSorted()` duplicated to `MinimapRenderer` (original retained in `UISubsystem` for NPC draw at lines 174/412).
- M5 simulation contract scaffold:
  - added `runWorldSimulationDiagnostics` command to validate/report world/faction/settlement contract snapshots,
  - deterministic `WorldSimulationTick` runtime loop implemented with seeded replay behavior and emitted pressure-change events,
  - mission/side-quest availability now consumes simulation signal scores with per-mission trace lines (`score`, `threshold`, `cause`),
  - regression harness includes `M5 World Simulation Contract Scaffold`, `M5 World Simulation Tick Determinism`, and quest signal traceability coverage.
- Expansion planning baseline:
  - north-star to executable milestone matrix documented with acceptance-test targets in `docs/NORTH_STAR_EXECUTION_MATRIX.md`,
  - roadmap/backlog scope now includes active M5 and queued M6 expansion tracks with explicit gating.

## Open Issues / Remaining Work

- ~~**M3 exit criteria not fully defined**~~ — resolved 2026-05-08. Formal gate checklist defined and all 12 criteria confirmed green; gate doc at `docs/M3_RELEASE_GATE.md`; M3 promoted to complete.
- **M4 campaign content**: authored act coverage beyond current scaffold not yet expanded; optional plateau content with worldgen validation gates pending.
- ~~**Chunk-streaming geometry**~~ — resolved 2026-05-08. All 3 region fragment JSONs now carry authored world-space tile geometry; `RegionLoader` sources static tiles from fragment data; stub fallback retained only for missing fragments; dead `addSolidTile`/`addPlatformTile` helpers removed.
- ~~**Echo puzzle evaluation**~~ — resolved 2026-05-08. `EchoPuzzleSolution` + `EchoPuzzleEvaluator` in `core.simulation`; rising-edge press counting; completion/tick/action-minimum checks with per-rule trace; 51/51 pass.
- **Echo / enemy / player collision**: `SimEcho` does not participate in collision with enemies or players (echo combat deferred).
- ~~**Co-op collision model**~~ — resolved 2026-05-08. `tickCoopCollisions()` added to `GameSimulator`; pairwise AABB separation with MSA push; `PLAYER_COLLISION` event; dead players excluded; 51/51 pass.
- **PlaytestClient decomposition**: `PlaytestClient.java` remains large despite subsystem extractions. Further decomposition scoped to future waves.

## Verification Evidence

Latest gate (2026-05-08) — Wave 5 extractions (StoryManager, MissionUiCoordinator, HudRenderer) + LibGDX scaffold + echo puzzle room + faction tension mutation + M3 save envelope / checksum guard:

```text
.\gradlew.bat clean :core:compileJava :client:compileJava :server:compileJava \
  runDataContractDiagnostics runWorldgenDiagnostics runWorldSimulationDiagnostics \
  runRegressionTests
BUILD SUCCESSFUL in 47s
runDataContractDiagnostics: contracts_loaded=true valid=true beats=45 critical_flags=61
  plateaus=7 world_regions=3 factions=3 settlements=3
runWorldgenDiagnostics: Section templates loaded: 13, validation issues: 0
runWorldSimulationDiagnostics: Validation issues: none; tick_events=8
runRegressionTests: All sections PASS (53 sections)
```

Historical gate evidence is in `docs/handover/CODEX_*.md` files (one per delivered option).

## Active Risks

1. Status drift between docs and implementation if milestone labels are not updated with evidence.
2. Scope inflation from copying donor code wholesale instead of bounded wave imports.
3. Future save-schema evolution (`V4+`) requires concrete migrator implementations before shipping — current V3 envelope is stable but any new save fields must follow the `SaveMigrationMatrix` policy before promotion.
4. North-star expansion scope (M6+) can overrun delivery unless gate criteria remain strict and test-backed.

## Next Actions

1. ~~**M3 exit criteria**~~ — closed 2026-05-08. Gate doc: `docs/M3_RELEASE_GATE.md`.
2. **M4 campaign content** — extend authored act coverage beyond current scaffold; integrate optional plateau content with worldgen validation gates. Unblocked as of M3 close.
3. ~~**Chunk-streaming geometry (M6)**~~ — resolved 2026-05-08. `RegionFragmentData.tiles` + `RegionLoader` parse path; all 3 region fragment JSONs authored; `addSolidTile`/`addPlatformTile` removed.
3a. ~~**Mutation visibility evidence (M6)**~~ — resolved 2026-05-08. HUD overlay line (amber when active), `MUTATION_OVERLAY_SAVE`/`LOAD` evidence lines, `refreshOverlayHud()` wired at init/transition/load.
4. ~~**Echo puzzle evaluation (M6)**~~ — resolved 2026-05-08. `EchoPuzzleSolution` + `EchoPuzzleEvaluator` wired; first authored echo puzzle room (summit_echo_room_1, minKills=1) in PlaytestClient Room 4; PUZZLE_PASSED/PUZZLE_FAILED emitted.
5. ~~**Co-op collision model (M6)**~~ — resolved 2026-05-08. `tickCoopCollisions()` in `GameSimulator`; pairwise AABB separation with MSA push; `PLAYER_COLLISION` event; dead players excluded.
