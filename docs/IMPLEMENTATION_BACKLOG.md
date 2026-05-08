# Implementation Backlog and Execution Checklist

This backlog is the operational companion to `docs/ROADMAP.md` and `docs/CURRENT_STATE.md`.

## Working Rules

- Status claims must reflect implemented and verified behavior.
- Data contracts are canonical for narrative/world progression.
- Migration work is applied in bounded waves from donor repos.

## Active Workstreams

## 1) Data Contract Runtime Integration (M1/M2)

### Delivered

- [x] Imported canonical data contracts into `data/`.
- [x] Added runtime contract loader and validator in `core`.
- [x] Added diagnostics command (`runDataContractDiagnostics`).
- [x] Reconciled `narrative_beats` NPC references with `npc_registry` IDs (diagnostics now valid).

### Next

- [x] Replace hardcoded mission definitions with contract-backed mission templates.
- [x] Expand mission catalog to side-quest chain runtime availability/completion rules from `quests`.
- [x] Bind hub transition decisions to beat/flag contract states.
- [x] Add strict mode toggle for contract validation failures.
- [x] Wire CI profile defaults to fail-fast contract mode.

## 2) Mission and Progression Hardening (M1)

### Current Gaps

- ~~Manual movement-feel sign-off against donor behavior pending.~~ Resolved 2026-05-07 — dash direction-lock bug fixed (SHIFT binding gap in `bindHold`), `DASH_COOLDOWN` raised to 1.0s. Evidence in `docs/ACT_I_QA_ROUTE.md`.

### Tasks

- [x] Add per-objective progress tracking to `Mission`.
- [x] Implement deterministic objective completion checks.
- [x] Remove auto-complete placeholder branch in `MissionManager`.
- [x] Add regression test for objective-driven completion flow.
- [x] Build mission template loading from `quests` + `narrative_beats`.
- [x] Add runtime side-quest chain gating and completion flag application from `quests` (`required_flags`, chain progression, `sets_flags`).
- [x] Replace generic side-quest objective ID (`complete_step`) with step-specific objective decomposition.
- [x] Model side-quest reward semantics as typed runtime effects (instead of flags-only).
- [x] Add explicit reward-effect contract typing to replace keyword-inference parsing.
- [x] Enforce strict runtime validation for reward-effect payload keys by effect type.
- [x] Drive hub NPC availability from beat + NPC registry contracts (fallback tables only for empty schedule edge-cases).
- [x] Move high-frequency hub dialogue lines into contract-backed dialogue mapping.

## 3) Save-State and Stability (M3 prep)

### Current Gaps

- save format now uses a versioned envelope with legacy fallback and explicit matrix policy; first forward migrator (`v1 -> v2`) exists, next forward migrator is still pending schema evolution.
- ~~save envelope does not yet include schema/hash guards for payload integrity drift.~~ Resolved 2026-05-08 — SHA-256 `checksum_sha256=` field added to V3 envelope; verified on load; backward-compatible (absent = skip).

### Tasks

- [x] Introduce versioned save envelope.
- [x] Add backward-compatible load handling for legacy saves.
- [x] Add regression tests for save compatibility.
- [x] Define forward-version strategy and migration matrix for future save revisions.
- [x] Add regression test coverage for unsupported future save versions.
- [x] Implement first forward migration handler for the v1 -> v2 save schema transition.
- [x] Prepare SAVE_V3 scaffolding structure for future schema evolution.

## 4) Migration Waves From Donor Repos

Reference: `docs/MIGRATION_MAP.md`

### Wave 2 (completed)

- [x] Integrate section-template validation pipeline starter classes from old repo.
- [x] Port generation validation planner/progression/report shape.
- [x] Add worldgen diagnostics entrypoint.
- [x] Port donor worldgen unit-test intent into clean-start regression harness parity checks.
- [x] Import initial authored `data/worldgen/sections` payloads and validate non-zero load path.
- [x] Port `GenerationValidationPlannerTest` donor assertions into regression harness (2026-05-08): `testGenerationValidationPlannerPlanning` — 5 sub-tests (valid chain, blocked non-optional, optional exempt, multi-blocked, toSnapshot keys); hub-in-worldNodes usage note captured; 42/42 pass.

### Wave 3 (completed)

- [x] Realign mission/story runtime to contract-backed templates, side-quest chain gating, per-step objective decomposition, and typed reward effects.

### Wave 4

- [x] Imported first modular collision primitives (`TileType`, `TileRect`, `SpatialHash`) from donor profile into `core.physics` and covered with regression checks.
- [x] Integrated first bounded collision/traversal geometry slice into `PlaytestClient` using chunked collision candidates.
- [x] Added bounded moving-platform runtime slice with dynamic collision overlay + grounded carry behavior in `PlaytestClient`.
- [x] Added ability-gated traversal blockers (`dash`, `combat_basic`) with runtime lock/unlock behavior and gate-block feedback.
- [x] Landed first authored multi-room traversal route with camera-follow and bounded room segmentation in `PlaytestClient`.
- [x] Added ability-execution interactions (dash-pass and interact triggers) that unlock traversal platforms and advance compatible mission objectives.
- [x] Added first combat encounter simulation hooks (attack input, encounter clear states, combat-seal blockers, objective advancement integration).
- [x] Replaced raw hit-count encounter loop with telegraphed combat state windows (telegraph/vulnerable/recover) and attack-window validation.
- [x] Added encounter pattern variety (STANDARD/FAST) with dynamic timing adjustments for combat AI differentiation.
- [x] Added player consequence loop for encounters (damage/fail-state/reset flow) with runtime recovery behavior.
- [x] Extracted `CombatSubsystem` from `PlaytestClient` monolith (2026-05-07): `CombatEncounterPhase`, `EncounterPattern`, `CombatEncounter`, `CombatSubsystem` as standalone package-level types; `PlaytestClient` delegates fully; all 5 extraction steps complete including integration review and Codex CLI validation.
- [x] Extracted `TraversalSubsystem` from `PlaytestClient` (2026-05-08): `TriggerMode`, `MovingPlatform`, `AbilityGate`, `AbilityTrigger`, `CombatBarrier`, `TriggeredPlatform`, `TraversalSubsystem` as standalone package-level types; `PlaytestClient` delegates fully; inner class declarations deleted; 25/25 regression tests pass; Codex CLI validated (2026-05-08).
- [x] Extracted `UISubsystem` from `PlaytestClient` (2026-05-08): `WorldGeometry` record + `UISubsystem` with `RenderState`, `drawFrame`, `tickAndUpdate`, minimap, event log, interaction hint, NPC positions, ability snapshot, mission feedback; `PlaytestClient` delegates fully; all draw/display-state methods deleted; 25/25 regression tests pass; Codex CLI validated (2026-05-08).
- [x] Imported sim entity actor slice (2026-05-08): `EnemyAIState`, `EnemyAwarenessState`, `SimEnemy`, `ItemDatabase`, `SimInventory` into `core.simulation`; layer contract clean; 30/30 regression tests pass; Codex CLI validated.
- [x] Imported SimPlayer actor slice (2026-05-08): `InputCommand`, `YinYangComponent`, `LanternComponent`, `EchoRecorder`, `SimPlayer` into `core.simulation`; GameConfig constants inlined; ECS base classes dropped; 31/31 regression tests pass; Codex CLI validated.
- [x] Imported boss system slice (2026-05-08): `BossAIState`, `BossType`, `SimBoss`, `BossPatternLibrary` into `core.simulation`; HubStateMachine + slf4j dropped; 5 narrative boss patterns (Siren/EchoWarden/TimeLord/MemoryEater/VeilMaiden); 32/32 regression tests pass; Codex CLI validated.
- [x] Extracted PlayerInputController from GameSimulator.applyPlayerInput (2026-05-08): full player input state machine (movement/combat/stance/ninjutsu/teleport/wall-slide/anim) in `core.simulation`; external deps inlined/stubbed; 35/35 regression tests pass; Codex CLI validated.
- [x] Imported crafting system slice (2026-05-08): `CraftingRecipe`, `RecipeBook`, `SimShop` into `core.simulation`; 34/34 regression tests pass; Codex CLI validated.
- [x] Imported sim entity completions slice (2026-05-08): `ReplayPlayer`, `SimNPC`, `SimEcho`, `SimMovingPlatform`, `SimPickup`, `SimShuriken`, `SimPortal` (toState() replaced with toMap()) into `core.simulation`; PortalState network dep dropped; 33/33 regression tests pass; Codex CLI validated.
- [x] GameSimulator entity-wiring slice (2026-05-08): `SimEvent` record + `GameSimulator` bounded coordinator in `core.simulation`; entity registry, tick loop (player/enemy/boss/NPC/pickup), event drain API, snapshot diagnostics; 41/41 regression tests pass; Codex CLI validated.
- [x] SimMovingPlatform + SimPortal integration in GameSimulator (2026-05-08): `movingPlatforms`/`portals` lists; `addMovingPlatform`/`addPortal` APIs; `tickMovingPlatforms` carries players standing on platform each tick; `tickPortals` pulses portal and emits `PORTAL_ACTIVATED` on proximity+ability-gate pass (one-shot — `isActive=false` after first activation); `getMovingPlatforms`/`getPortals` queries; 47/47 regression tests pass; Codex CLI validated.
- [x] SimShuriken projectile flight in GameSimulator (2026-05-08): `shurikens` list + `shurikenCounter` field; `fireShuriken(playerId, vx, vy)` emits `SHURIKEN_FIRED`; `tickShurikens(dt)` moves per tick, AABB-tests vs alive enemies, emits `SHURIKEN_HIT` / `ENEMY_DEFEATED`, removes on hit or TTL expiry; `getShurikens()` query; `overlaps()` AABB helper; 46/46 regression tests pass; Codex CLI validated.
- [x] BossPatternLibrary tick dispatch in GameSimulator (2026-05-08): `tickBoss` calls `BossPatternLibrary.tick(b, ctx, dt)` after INTRO/phase logic; `buildBossPatternContext()` builds `PatternContext` (players by slot, enemies list, null spawn/projectile stubs); `BOSS_SCRIPTED_LOSS` event emitted on SIREN trigger; all 5 boss types dispatch without NPE; 45/45 regression tests pass; Codex CLI validated.
- [x] Enemy combat damage loop in GameSimulator (2026-05-08): `tickEnemy` now deals `baseDamage` to nearest player on cooldown expiry (`ENEMY_ATTACK_COOLDOWN=1.15f`); emits `PLAYER_DAMAGED` / `PLAYER_DIED`; `attackEnemy(playerId, enemyId, damage)` API for player-originated hits emits `ENEMY_DEFEATED`; invincibility frames guard correctly; 44/44 regression tests pass; Codex CLI validated.
- [x] M6 authored region templates — stub geometry elimination (2026-05-08): 3 new section template JSONs added (`lantern_region_hub`, `lantern_hub`, `hollow_dungeon`); `SectionTemplateLibrary` count 10→13; 3 regression test constructors switched from `empty()`/`null` to `loadDefault()`; all `[WARN] no template for ...` lines eliminated; 49/49 regression tests pass; Codex CLI validated.
- [x] Co-op session scaffolding in GameSimulator (2026-05-08): `REVIVE_RANGE=80f` constant; `tickCoopProximity()` emits `PLAYER_PROXIMITY` per tick for each alive player pair within range; `requestRevive(reviverPlayerId, targetPlayerId)` revives dead target at `max(1, maxHealth/2)` HP + `INVINCIBILITY_TICKS` frames if reviver is alive and within range; `COOP_REVIVE` emitted on success; 49/49 regression tests pass; Codex CLI validated.
- [x] EchoRecorder / ReplayPlayer / SimEcho integration in GameSimulator (2026-05-08): `tickPlayers` records `p.latestInput` into `p.echoRecorder` each tick; `echoes` list + `spawnEcho(playerId, echoId, looping)` snapshots recorder, builds `ReplayPlayer.fromInputSequence`, creates `SimEcho`; `tickEchoes()` steps each echo per tick, emits `ECHO_STARTED` / `ECHO_COMPLETED` / `ECHO_FAILED`, removes finished echoes; `getEchoes()` query; 48/48 regression tests pass; Codex CLI validated.
- [ ] Avoid direct import of large monolith classes without slicing — rule remains active for all future wave work.

### Wave 5

- [x] Added minimap overlay in `PlaytestClient` (toggle + player/NPC/geometry projection) as first selected playability-system import.
- [x] Added richer playtest HUD/mission feedback surfacing (objective progress, next-objective hint, ability unlock surfacing, traversal-gate state, mission feed toasts).
- [x] Added contextual ability-interaction hint surfacing and minimap viewport framing for route readability.
- [x] Added combat-state HUD/minimap surfacing (attack cooldown, encounter focus, encounter markers).
- [x] Integrate selected UI/playability systems from old repo client stack (2026-05-08): `InventoryPanel`, `ShopPanel`, `CraftingPanel` in `com.shadowascent.client`; LibGDX patterns adapted to Swing Graphics2D; `PlaytestClient` wired with `I`/`T`/arrow/Enter/Esc key bindings, `SimInventory playerInventory` + `SimShop hubShop` fixtures, hub shop open via E interact; 3 new regression sections; 40/40 pass.
- [x] Extract `MinimapRenderer` from `UISubsystem` (2026-05-08): standalone `MinimapRenderer` class in `com.shadowascent.client`; full minimap drawing logic (collision tiles, moving platforms, triggered platforms, combat barriers, ability gates, encounters, triggers, viewport rect, NPC dots, player dot) moved out of `UISubsystem.drawMinimap`; `activeNpcsSorted()` copied to `MinimapRenderer` (original kept in `UISubsystem` for NPC drawing at lines 174/412); `UISubsystem` field + constructor init + `minimapRenderer.draw(g, state)` delegation wired; `drawMinimap` body deleted; validated via `client:compileJava` + 43/43 regression tests pass.

- [x] Echo puzzle evaluation logic (2026-05-08): `EchoPuzzleSolution` record (`puzzleId`, `minActionPresses`, `maxTicks`, `requireCompletion`) + `EchoPuzzleEvaluator.evaluate(SimEcho, EchoPuzzleSolution)` in `core.simulation`; rising-edge press counting by walking replay tick sequence; `Result(passed, trace, actionPressCounts)` with per-rule trace lines; 5-sub-test regression section `testEchoPuzzleEvaluator`; 50/50 regression tests pass.
- [x] M6 Echo combat integration (2026-05-08): `SimEcho` gains `W=20/H=28/ATTACK_REACH=14/ECHO_DAMAGE=1` constants, `attackedThisTick`/`prevAttackInput`/`echoKillCount` fields; `step()` computes attack rising-edge; `GameSimulator.tickEchoes()` AABB-tests live enemies on `attackedThisTick`, emits `ECHO_COMBAT_HIT` on hit and `ENEMY_DAMAGED` on kill, increments `echoKillCount`; `EchoPuzzleSolution` gains `minEchoKills` field + `ofKills()` factory; `EchoPuzzleEvaluator` checks kill count; 3-sub-test `testEchoCombatIntegration`; 51/51 regression tests pass.
- [x] Region socket validation HUD (2026-05-08): `PlaytestClient` gains `streamingConstraintWarnings` list; `collectStreamingWarnings()` re-runs `RegionalStreamingConstraintValidator` on loaded manifests, surfaces DISCONNECTED/SOCKET_MISMATCH issues as non-fatal; `RegionLoadException` caught explicitly at all 3 `loadNeighborhood()` call sites to capture fatal issue kinds; `buildOverlaySummary()` prepends warnings to overlay HUD line.
- [x] PlaytestClient CombatSubsystem extraction — ALREADY COMPLETE as of Wave 5 (2026-05-08): `CombatSubsystem` (144 lines) fully extracted; `PlaytestClient` delegates all combat via `combatSubsystem.update()` / `combatSubsystem.tryAttack()` / `combatSubsystem.clearAll()`; no further work needed.
- [x] M5 Faction-driven NPC schedule overlay (2026-05-08): `NpcDefinition` gains `factionId` field; `npc_registry.json` assigns `faction_ember_hearth_order` to `BROTHER_KAI`, `BROTHER_LEN`, `BROTHER_ASH`; `GameDataContracts.factionInfluencedNpcIds(StoryState)` returns NPCs whose faction holds the active plateau and has `tension > 0.5`; `HubManager.resolveScheduledNpcIds()` includes faction-influenced NPCs; 3-sub-test `testFactionInfluencedNpcSchedule`; 51/51 pass.
- [ ] **[AUTHORING DECISION REQUIRED] M4 Authored Act I Plateau Content**: SUMMIT_SHRINE plateau has no narrative beats defined. Requires content decisions before authoring: (1) what quests/beats belong to SUMMIT_SHRINE in Act I, (2) which NPCs are eligible, (3) how it connects to the LANTERN_HEIGHTS → HOLLOW_DEPTHS progression spine. See `data/plateaus.json` for the existing SUMMIT_SHRINE definition. Block on user narrative decisions before starting.

## 5) Documentation Integrity (always active)

- [ ] Update `docs/CURRENT_STATE.md` after each meaningful code milestone.
- [ ] Keep roadmap and backlog status synchronized.
- [ ] Archive superseded claims instead of silently overwriting history.

## 6) Playable Client Track (new high priority)

### Delivered

- [x] Added interactive human-playtest client entrypoint (`com.shadowascent.client.PlaytestClient`).
- [x] Added `runPlayableClient` Gradle task for local launch.
- [x] Replaced fragile `KeyListener` capture with `WHEN_IN_FOCUSED_WINDOW` key bindings (fixes static/no-movement focus-loss behavior).
- [x] Integrated donor-profile runtime movement subset in client loop (run-speed baseline + dash speed/duration/cooldown from `core.physics.PhysicsConstants`).
- [x] Added initial donor-inspired jump/coyote/wall-jump runtime slice (gravity, ground coyote, wall coyote, wall jump, wall-slide clamp).
- [x] Added double-jump behavior and wall-jump input-lock timing parity slice.
- [x] Added wall stamina/exhaustion behavior slice for wall traversal parity.
- [x] Added deterministic controller regression checks (run distance, jump/double-jump, coyote window, dash trigger, wall exhaustion gating).
- [x] Added collision-complexity traversal structures (platforms, wall routes, blockers) in the playable-client arena via chunked collision lookup.
- [x] Added minimap runtime overlay (`M` toggle) for playtest navigation feedback.
- [x] Added persistent playtest session evidence logging (`logs/playtest/playtest_session_*.log`) with timestamped events and periodic snapshots.

### Next

- [x] Import donor movement/physics constants profile from `indie-ninja-adventures` into clean-start core runtime (`core.physics.PhysicsConstants` + `core.physics.PhysicsState`).
- [x] Add movement telemetry summary output (`MOVEMENT_SIGNOFF`) to playtest evidence logs to support manual donor-feel sign-off.
- [x] Complete manual in-client movement-feel sign-off against donor behavior (deterministic baseline tolerances are now locked).
- [x] Expand from bounded traversal sandbox into first authored multi-room traversal topology with denser collision constraints for real playtest routes.
- [x] Add first combat encounter simulation hooks to complement traversal-only ability activators.
- [x] Expand encounter behavior from hit-count clears to telegraphed attack/state patterns.
- [x] Add player-consequence loop for encounters (damage/fail-state/reset flow) to complete first combat QA lane.

## 7) M5 Systemic World Simulation Foundation (active)

Reference: `docs/NORTH_STAR_EXECUTION_MATRIX.md`

### Tasks

- [x] Define `world_state` contract schema and seed dataset (season, weather, corruption pressure, prosperity pressure, route risk).
- [x] Define `faction_state` contract schema and seed dataset (influence, tension, territory pressure, event hooks).
- [x] Define `settlement_state` contract schema and seed dataset (prosperity, safety, supply, morale, memory flags).
- [x] Add `WorldSimulationTick` deterministic runtime loop in `core` (seeded, replayable).
- [x] Emit world-state change events that quest/runtime systems can consume.
- [x] Add regression harness coverage for deterministic tick replay and event stability.
- [x] Add regression scaffold coverage for baseline world-state contract presence and key references.
- [x] Add diagnostics command for world-state contract validity and cross-reference checks.
- [x] Integrate simulation event stream into mission/side-quest availability scoring with traceability output.

## 8) M6 Open-World Runtime Expansion (queued)

Reference: `docs/NORTH_STAR_EXECUTION_MATRIX.md`

### Tasks

- [x] Add regional streaming contract/model and constraint validator (2026-05-08): `core.world.streaming` package; `RegionManifest`, `RegionInstance`, `RegionLoader`, `MutationOverlay`, `RegionalStreamingConstraintValidator`, `RegionFragmentData`, `SocketBinding`, `ZoneOverride`, `ResolvedAnchor`, `RegionLoadException`; four constraint checks (DISCONNECTED, INACCESSIBLE, SOFTLOCK, SOCKET_MISMATCH); 28/28 regression tests pass; Codex CLI validated.
- [x] Add authored region manifest fragments under `data/worldgen/regions/` and `runRegionalStreamingDiagnostics` Gradle task (2026-05-08): 3 fragments (`hub_lantern_heights`, `dungeon_forge_terrace_a`, `region_hollow_shaft`); diagnostics PASS.
- [x] Add mutation overlays (CORRUPTION_SURGE, PROSPERITY_CRISIS, ROUTE_HAZARD, FACTION_CONFLICT, STABILITY_WINDOW) consuming `WorldSimulationPressureSample` signals (2026-05-08): `MutationOverlay.compute/apply/extractSaveState`; `appliedOverrides` field on `RegionInstance` for save round-trip.
- [x] Wire `PlaytestClient.initializeCollisionLayout` to `RegionLoader` (2026-05-08): `buildPlaytestProgressionGraph` (3-node graph), `activeRegions` field, `loadNeighborhood` call at startup, `buildAllDynamicTiles` (traversal + region overlay tiles), `refreshCollisionHashFromRegions` (full rebuild entry point); 28/28 pass; Codex CLI validated.
- [x] Add SAVE_V3 envelope extension + v2→v3 migrator for `region_overlays_b64` persistence (2026-05-08): `OverlayPayloadCodec` added; `SaveMigrationMatrix` promotes V3 to current; `GameState.save(path, overlaysB64)` + `loadOverlaysB64`; `PlaytestClient` save/load wired; 37/37 pass.
- [x] Add quest ecology expansion from world simulation signals (settlement/faction pressure -> generated opportunities).
- [x] Add non-shipping co-op traversal validation harness for recovery safety checks (2026-05-08): `testCoopTraversalRecoveryValidation` regression section.
- [x] Wire region transition in `PlaytestClient` (2026-05-08): `resolveRegionIdForX`, `checkAndApplyRegionTransition`, `transitionToRegion`; `loadNeighborhood` re-called on boundary cross; REGION_TRANSITION evidence emitted; 36/36 pass.
- [x] Replace authored traversal route stub geometry with section-template tile data (2026-05-08): `tiles` array added to `RegionFragmentData` + `RegionLoader.parseFragment/load`; all 3 region fragment JSONs authored with world-space tile geometry; `addSolidTile`/`addPlatformTile` dead helpers removed from `PlaytestClient`; `buildAllDynamicTiles` now sources static tiles exclusively from `RegionInstance.staticTiles()`; 49/49 regression tests pass.
- [x] Capture playtest evidence for mutation visibility and route coherency across reloads (2026-05-08): `UISubsystem.overlayStatusLine` + `setOverlayStatusLine()` added; right HUD panel expanded to show active overlay kinds per region (amber when active, default when none); `buildOverlaySummary()` helper in `PlaytestClient`; `MUTATION_OVERLAY` evidence line emitted on region transition; `MUTATION_OVERLAY_SAVE` / `MUTATION_OVERLAY_LOAD` evidence lines in save/load; `refreshOverlayHud()` wired at init, transition, and load; 49/49 pass.
- [x] Co-op player AABB collision separation (2026-05-08): `tickCoopCollisions()` added to `GameSimulator`; pairwise overlap detection via `overlaps()` helper; minimum-separation-axis push applied to both players (half-overlap each axis); `PLAYER_COLLISION` event emitted per separated pair; dead players excluded from collision; `testCoopPlayerCollisionSeparation` 3-sub-test regression section; 51/51 regression tests pass.

## Verification Commands

```bash
./gradlew runDataContractDiagnostics
./gradlew runWorldSimulationDiagnostics
./gradlew runRegressionTests
```

## Definition of Done for Milestone Updates

A milestone step can be marked complete only when:

1. implementation exists in code,
2. validation command(s) pass,
3. docs are updated in `CURRENT_STATE`, `ROADMAP`, and `INDEX`.
