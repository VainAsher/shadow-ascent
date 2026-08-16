---
doc_type: current_state
status: living
owner: core-team
last_updated: 2026-06-14
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
  - M4 campaign completion/content scale: complete (2026-05-15). Gate evidence: `docs/MILESTONE_GATE_M4_FULL.md`.
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
- Wave 5 PlaytestClient decomposition — phase 2 extractions (2026-05-09):
  - `HudRenderer` extracted from `UISubsystem`: owns all mutable HUD state (`feedbackLine`, `overlayStatusLine`, `interactionHint`, `showMinimap`, ability snapshot) + full `drawHud()` body; `UISubsystem` delegates via `hudRenderer.draw(g, state)` + `hudRenderer.tick(x, y, dt)`; ability-unlock log lines routed back through `Consumer<String> eventLogSink` = `UISubsystem::addEventLogLine`,
  - `StoryManager` extracted from `PlaytestClient`: encapsulates `seedState()` + `refreshFromStoryChange()` + `restoreAbilityTriggers()` + `restoreCombatEncounters()`; all 4 inline `hubManager.updateHubState(); missionManager.updateAvailableMissions()` pairs replaced with `storyManager.refreshFromStoryChange()`,
  - `MissionUiCoordinator` extracted from `PlaytestClient`: objective-advance + feedback-flash chain; `feedbackSink` consumer (`uiSubsystem.setMissionFeedback`) + `logSink` consumer; `applyCombatEncounterObjectiveProgress`, `applyAbilityTriggerObjectiveProgress`, `advanceObjective` are 1-liner delegates.
- First authored echo puzzle room (2026-05-09):
  - `echo_puzzle_sentinel` encounter added to PlaytestClient Room 4 (x=2750, radius=80, 1-hit STANDARD pattern, keywords: echo/puzzle/sentinel/summit),
  - `EchoPuzzleSolution.ofKills("summit_echo_room_1", minKills=1)` + `EchoPuzzleEvaluator` wired; `checkEchoPuzzle()` called on sentinel clear,
  - emits `PUZZLE_PASSED` / `PUZZLE_FAILED`; sets `echo_puzzle_summit_cleared` flag on pass.
- Faction tension mutation floor (2026-05-09):
  - `WorldSimulationTick.maxFactionTensionForPlateau()` computes max faction tension for a plateau from `faction_state` contracts,
  - tension computed as `Math.max(region.factionTension(), maxContractTension) + noise`; `faction_veil_covenant` (tension=0.73) now floors HOLLOW_DEPTHS ≥ 0.65 → `MutationOverlay.compute()` fires `FACTION_CONFLICT`.
- LibGDX P1 production client scaffold (2026-05-09):
  - libgdx 1.12.1 deps added to `:client` in `build.gradle.kts`; `runGame` Gradle task targeting `DesktopLauncher`,
  - `DesktopLauncher.java` (1280×720, 60fps LWJGL3 config) + `ShadowAscentGame.java` (Game, creates `HubScreen` on `create()`) + `HubScreen.java` (GL clear per frame, all lifecycle stubs),
  - `GameInputProcessor` (LibGDX `InputAdapter`): WASD/arrows (move), SPACE (jump), SHIFT/C (dash), F (attack), E (interact), I (inventory) wired to `InputCommand` each frame,
  - `.gitattributes` added enforcing `eol=lf` on `gradlew`; GitHub Actions CI passing.
- LibGDX P2 tile rendering + collision + camera (2026-05-09):
  - `StubWorldRenderer.render()` accepts `List<TileRect>`; draws solid tiles (grey) and one-way platforms (green) before entity rects; `ShadowAscentGame` passes `worldTiles` each frame,
  - `ShadowAscentGame.create()` builds stub geometry (solid floor y=360 w=3500px + one-way platform at y=240); creates `CollisionWorld` and injects into `GameSimulator` via `setCollisionWorld()`; player spawns at y=280 and lands via AABB resolution,
  - Y-axis fix: `OrthographicCamera.setToOrtho(true, w, h)` in `show()` and `resize()` — Y-down matches simulation coordinate convention; gravity, floor, and jump direction all render correctly,
  - `CollisionWorld.resolveX(PhysicsState p, float prevX)`: solid tiles block horizontal movement; sets `onWall`/`wallDir`; platforms skipped (one-way vertical only); wired into `GameSimulator.tickPlayers()` before `resolveY`,
  - Camera world-bounds clamping: `HubScreen.show()` derives `worldRight`/`worldBottom` from `game.worldTiles` extents; camera position clamped after each lerp so visible area never extends past tile geometry,
  - Regression: 54/54 pass (new section: `testCollisionWorldXResolution` — right-wall, left-wall, platform pass-through).
- LibGDX P3 asset pipeline + state presentation (2026-05-10):
  - root `packSprites` Gradle task now generates deterministic placeholder assets at `assets/sprites/packed/sprites.png` + `sprites.atlas` via `SpritePackerTool`,
  - `ShadowAscentGame.create()` loads the packed atlas through `AssetManager` and routes render work to `SpriteWorldRenderer`,
  - `SpriteWorldRenderer` now draws tiles, players, enemies, and NPCs from atlas regions instead of active rectangle rendering,
  - placeholder presentation now maps visible runtime state onto distinct sprites: player idle/run/jump/dash/attack/dead and enemy patrol/alerted/attack/stunned,
  - enemy patrol presentation now distinguishes the current donor-imported runtime types (`goblin`, `bat`, `slime`, `skeleton`, `wolf`) with separate placeholder silhouettes/colors,
  - entity facing now flips sprite presentation left/right without changing simulation coordinates,
  - `StubWorldRenderer` remains available as the rectangle fallback implementation, but the active `runGame` path is atlas-backed.
- LibGDX UI overlay port (2026-05-13):
  - `runGame` now renders a persistent top-left HUD status block, bottom-left event feed, and docked minimap through `HudOverlayRenderer`, `HudOverlayState`, and `MinimapOverlayRenderer`,
  - shared modal lifecycle is now owned by `ModalOverlayManager` + `HubScreen`, with `GameInputProcessor` suppressing gameplay input while overlays are active and exposing one-frame UI signals for toggles, confirm, cancel, interact, and directional menu input,
  - inventory overlay is now live in LibGDX: `I` toggles it, arrow/WASD navigation is edge-triggered, `Enter` uses/equips items through the real player `SimInventory`, and feedback is appended to the HUD event feed,
  - merchant shop overlay is now live in LibGDX: `E` near the hub merchant fixture opens a `SimShop("merchant_npc", 2, 12345L)` modal, left/right swap buy-sell focus, up/down move selection, `Enter` executes one-item trades, and the result is surfaced in the event feed,
  - crafting overlay is now live in LibGDX: `T` toggles a recipe list backed by `RecipeBook`, up/down changes recipe selection, `Enter` crafts the selected recipe against the real player inventory, and feedback persists through the feed,
  - `ShadowAscentGame` now seeds a bounded starter inventory/currency set for the production-client slice so inventory, shop, and crafting are testable in `runGame`,
  - dialogue interaction is now live in LibGDX: `E` prefers nearby NPC dialogue before merchant fallback, opens a dedicated dialogue modal, and advances/closes on `Enter`/`Esc`,
  - pause/save/load is now live in LibGDX: `Esc` opens a pause modal when no other overlay is active, simulation time freezes while paused, `F5`/`F9` expose direct one-frame save/load signals, and the `runGame` runtime now persists a bounded player/enemy snapshot sidecar in addition to story state,
  - title/new-game/continue routing is now live in LibGDX: `ShadowAscentGame` exposes explicit `startNewGame()` / `continueFromSave()` entry points, `TitleScreen` is the default `runGame` entry surface, and pause-menu quit-to-title now routes back through that screen flow,
  - first-pass audio routing is now live in LibGDX: `AudioManager` resolves key simulation events like `PLAYER_DAMAGED`, `ENEMY_DEFEATED`, and `PORTAL_ACTIVATED` into stable sound keys, with playback intentionally left as a safe no-op until asset binding lands,
  - HUD interaction hinting is now richer in LibGDX: the HUD separates long-lived contextual status from immediate interaction prompts like NPC talk and merchant access,
  - `Esc` now closes any active modal without leaking stale UI signals into later frames; modal replacement semantics are test-backed for inventory/shop/crafting transitions.
- LibGDX authored-runtime deepening (2026-05-14):
  - `AudioManager` is now registry-backed rather than key-only: `audio/audio_registry.json` is loaded at runtime, event sounds resolve to concrete asset paths, and playback requests degrade safely when assets are absent or not yet mounted in LibGDX,
  - `runGame` authored area resolution now follows unresolved plateau-authored beats, not only the strict critical subset, which allows `HOLLOW_DEPTHS` to advance from camp into caves/arena/support areas as story flags move forward,
  - `AuthoringWorldBootstrap` now exposes a broader Hollow Depths slice with area-specific geometry, spawn points, NPC anchor placement, and enemy placement for `area_hollow_depths_camp`, `area_hollow_depths_caves`, `area_echo_galleries`, `area_weightbound_mines_arena`, `area_hollow_hub_first_sparks`, `area_shatter_moth_nest`, `area_fractured_contact_high_winds`, `area_stone_judge_maze`, and `area_abyssal_gate`,
  - `runGame` HUD mission surfacing now prefers plateau-relevant available missions and unresolved authored plateau beats over cross-plateau generic availability when no mission is active,
  - `runGame` is now the primary forward QA surface; `runPlayableClient` is retained as a legacy Swing prototype/reference layer rather than the lead play surface.
- M4a Act I vertical slice playable readiness (2026-05-14):
  - room-spec routing is now the hard gate for Lantern Heights: hardcoded NPC fallback injection removed from `AuthoringWorldBootstrap`; areas without a room-spec emit a `[AuthoringWorldBootstrap] WARNING` rather than silently injecting content,
  - ACT_0 optional quests (`sq_samson_q1_unfinished_sparring_match`, `sq_sophia_q1_lantern_cartography`, `sq_marcel_q1_guard_the_forge`, `sq_hazel_q1_gentle_glow`) are now wired: available after `village_bonds` completes, started via NPC interaction, advanced via encounter clears, and completion-synchronized via story flags through `synchronizeMissionCompletion`,
  - `optional_npc` role bypass in `shouldStageNpcAnchor` makes OLD_MAN_RIKU and LANTERN_KID appear in their anchored rooms without requiring explicit NPC activation in story state; LANTERN_KID dialogue line `bark_lk_hope_01` added to `dialogue.json`,
  - typed transition gate feedback: `encounter_gate`, `return_gate`, `mission_gate`, `npc_handoff_gate` transition types all produce distinct player-visible blocked messages via `blockedReasonForType`; encounter clears call `updateAvailableMissions()`,
  - `authoredDialogueLines` now treats empty `npc_ids` in a beat as "any NPC in this area" rather than zero-match, and `isRuntimeBeat` now covers `authored_critical` and `authored_milestone` beat types — fixing two compounding bugs that silenced all key Lantern Heights beat dialogue,
  - HUD optional quest clarity: active side quests display `[Side Quest]` prefix; `missionRoutePrompt` handles all four ACT_0 quest IDs; `authored_critical`/`authored_milestone` beats now surface in the HUD when no mission is active,
  - combat feedback: 2-hit combo events emit `PLAYER_MELEE_HIT` with `comboStep` 1 and 2; enemy kill emits `ENEMY_DEFEATED` with alive-count drop to 0; coverage added to `GameSimulatorMeleeCombatTest`,
  - save/load hardening: `village_bonds` partial objective progress now restores from `talked_to_*` story flags in `synchronizeMainlineMissionStatesFromFlags` via `restoreVillageBondsObjectiveProgress()`, so a save mid-village-bonds no longer loses partial NPC progress after reload,
  - smoke coverage expanded: `ActIRouteStateSmokeTest.npcWithdrawalWarningsAdvanceThroughBeatToWarningsHeard` covers the warning NPC interaction flow; `ActIOptionalQuestFlowTest` (new file, 3 tests) covers objective progress restoration, optional quest availability, and side-quest start from NPC interaction,
  - gate doc at `docs/MILESTONE_A_GATE.md`; QA route updated in `docs/ACT_I_QA_ROUTE.md`.
- LibGDX audio and authored-area presence follow-up (2026-05-14):
  - runtime placeholder WAV assets now ship in `java/client/src/main/resources/audio/`, which makes the existing audio routing audible in `runGame` for title, Lantern Heights, Hollow Depths, Ember Monastery, player hurt, enemy defeat, and portal activation events,
  - `AudioManager` now selects title music and plateau-aware gameplay music keys rather than treating music as a single fallback slot,
  - Hollow authored area placement is now stricter about which NPCs belong in which room-sized slice, so areas like `area_hollow_hub_first_sparks` and `area_hollow_depths_caves` stop inheriting the whole active plateau roster when the authored beat implies a smaller cast.
- M4 SUMMIT_SHRINE content authoring (2026-05-09):
  - 5 critical narrative beats in `narrative_beats.json` (`beat_empty_hub_only_maiden` → `beat_aen_hollowed`, route_order 100–140),
  - NPC registry entries (YIN, YANG, VEIL_MAIDEN, SIREN_OF_MASKS with role/eligibility); SIREN_OF_MASKS encounter block (scripted_loss, force-loss at tick 510, 4-event timeline),
  - `plateau_quest_policy.SUMMIT_SHRINE` in `quests.json` (critical_only_no_side_quests, `quest_chains_permitted: false`, `elastic_content_permitted: true`),
  - `plateau_tag_rules.SUMMIT_SHRINE` in `chunk_grammar.json` (6 tag rules: 3 elastic-eligible, 3 authored-critical-only with `elastic_eligible: false`),
  - 3 elastic chunk templates in `elastic_chunk_templates.json` (mask_gallery, pressure_corridor, circular_shrine),
  - story flags: `mask_truth_seen`, `siren_fight_started`, `yin_yang_taken`, `hollowed`, `act2_unlocked`.
- M4 HOLLOW_DEPTHS side quest chain authoring (2026-05-09):
  - 4 side quest chains in `quests.json` (`hollow_depths_chains`): SHADE_HERMIT (circles_of_survival), SMITH_MONK (coals_of_motion), LISTENING_ELDER (listen_before_light), ADVOCATE (the_open_verdict); 2 steps each,
  - 9 `hd_*_complete` flags in `story_flags.json`; NPC registry entries and dialogue lines authored for all 4 NPCs,
  - all quest gate flags verified against beat `sets_flags` — all 7 required flags (`awoke_in_depths`, `hollow_weight_understood`, `weightbound_ogre_defeated`, `dash_restored`, `echoes_encountered`, `double_jump_restored`, `stone_judge_defeated`) set by the 10 existing HOLLOW_DEPTHS critical-route beats.
- PlaytestClient decomposition — Wave 5 phase 3 extractions (2026-05-09):
  - `InputHandler`: owns `pressedKeys` Set + 16 queue boolean flags; `install(JComponent, BooleanSupplier, Runnable)` replaces `GamePanel.configureKeyBindings()` + `bindHold`/`bindPress`; ENTER dual-purpose + M minimap toggle delegated via callbacks,
  - `RoomGeometry`: static constants for room boundaries (`FLOOR_Y`, `CEILING_Y`, `WORLD_LEFT_X`, 4 room-end X markers, `PLAYER_SPAWN_X`, `SHOP_NPC_X`) + `resolveRegionIdForX(float x)`,
  - `SaveLoad`: `record LoadResult`; `buildOverlaysB64()` / `save()` / `load()` encapsulate all persistence I/O; PlaytestClient retains post-load refresh callbacks; removed 6 stale imports.
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
- ~~**M4 campaign content**~~ — resolved 2026-05-15. All seven plateau families are now room-spec staged on the `runGame` path, plateau-local optional content already authored in contracts is surfaced, continuity survives save/load, and the campaign resolves into a bounded post-climax free-roam state. Gate evidence: `docs/MILESTONE_GATE_M4_FULL.md`.
- ~~**Chunk-streaming geometry**~~ — resolved 2026-05-08. All 3 region fragment JSONs now carry authored world-space tile geometry; `RegionLoader` sources static tiles from fragment data; stub fallback retained only for missing fragments; dead `addSolidTile`/`addPlatformTile` helpers removed.
- ~~**Echo puzzle evaluation**~~ — resolved 2026-05-08. `EchoPuzzleSolution` + `EchoPuzzleEvaluator` in `core.simulation`; rising-edge press counting; completion/tick/action-minimum checks with per-rule trace; 51/51 pass.
- ~~**Echo / enemy / player collision**~~ — resolved 2026-05-08. `SimEcho` AABB-tests live enemies on `attackedThisTick`; emits `ECHO_COMBAT_HIT` / `ENEMY_DAMAGED`; `echoKillCount` tracked; `EchoPuzzleSolution.ofKills()` evaluates echo kills; 51/51 pass.
- ~~**Co-op collision model**~~ — resolved 2026-05-08. `tickCoopCollisions()` added to `GameSimulator`; pairwise AABB separation with MSA push; `PLAYER_COLLISION` event; dead players excluded; 51/51 pass.
- ~~**PlaytestClient decomposition**~~ — resolved 2026-05-09. `InputHandler` (key bindings + 16 queue flags), `RoomGeometry` (boundary constants + region resolver), `SaveLoad` (persistence I/O + `LoadResult` record) all extracted; `PlaytestClient` delegates fully to all three.
- ~~**Stub physics floor (P2)**~~ — resolved 2026-05-09. `CollisionWorld` injected into `GameSimulator`; `resolveX` + `resolveY` called each tick; spawnY stub is the fallback only when `collisionWorld == null` (regression tests unaffected).
- ~~**LibGDX world geometry**~~ — resolved 2026-05-09. `StubWorldRenderer` accepts `List<TileRect>`; solid floor and platform drawn as coloured rects; `ShadowAscentGame` builds and passes stub tile geometry.
- **Next LibGDX production-client follow-up**: `runGame` is now the main QA surface and the full campaign host; the next work is campaign fidelity/polish, stronger authored density, and broader authoring acceleration rather than basic plateau existence.
- **M4a Act I vertical slice**: complete as of 2026-05-14. All 12 exit criteria green (see `docs/MILESTONE_A_GATE.md`). Pre-existing `Campaign Continuity` regression failure not caused by M4a scope — tracked separately.
- **M4b Act I authoring velocity and fidelity hardening**: complete as of 2026-05-15. The repo now has explicit authoring diagnostics, zero-Java room/side-beat proofs, contract-driven route-hint/mainline metadata, fixture authoring coverage, and durable save/load coverage for growth-state room IDs.

## Verification Evidence

Latest focused gate (2026-05-14) — LibGDX authored-runtime and audio-routing deepening:

```text
.\gradlew.bat :client:test --tests "com.shadowascent.client.audio.AudioManagerEventRoutingTest"
BUILD SUCCESSFUL

.\gradlew.bat :client:test --tests "com.shadowascent.client.world.AuthoringWorldBootstrapTest"
BUILD SUCCESSFUL

.\gradlew.bat :client:compileJava
BUILD SUCCESSFUL
```

Previous gate (2026-05-13) — LibGDX production-client parity tranche on top of the production-client render path:

```text
.\gradlew.bat :client:test --tests "com.shadowascent.client.ui.DialogueOverlayRendererStateTest"
BUILD SUCCESSFUL

.\gradlew.bat :client:test --tests "com.shadowascent.client.ui.PauseMenuOverlayRendererStateTest" --tests "com.shadowascent.client.input.GameInputProcessorUiRoutingTest" --tests "com.shadowascent.client.SaveLoadRuntimeStateTest"
BUILD SUCCESSFUL

.\gradlew.bat :client:test --tests "com.shadowascent.client.ShadowAscentGameStructureTest"
BUILD SUCCESSFUL

.\gradlew.bat :client:test --tests "com.shadowascent.client.audio.AudioManagerEventRoutingTest" --tests "com.shadowascent.client.ui.HudOverlayStateTest"
BUILD SUCCESSFUL

.\gradlew.bat clean :client:compileJava
BUILD SUCCESSFUL

.\gradlew.bat packSprites
BUILD SUCCESSFUL
  wrote assets/sprites/packed/sprites.png
  wrote assets/sprites/packed/sprites.atlas

.\gradlew.bat runRegressionTests
exit code 0
```

Previous gate (2026-05-10) — LibGDX P3 asset pipeline + state-driven placeholder rendering:

```text
.\gradlew.bat :client:test
BUILD SUCCESSFUL in 43s
3 tests completed, 0 failed
  verifies: atlas generation, state-region selection, enemy-type placeholder selection, single-page atlas parse

.\gradlew.bat packSprites
BUILD SUCCESSFUL
  wrote assets/sprites/packed/sprites.png
  wrote assets/sprites/packed/sprites.atlas
```

Previous gate (2026-05-09) — LibGDX P2: CollisionWorld.resolveX, camera bounds clamping, Y-axis orientation fix; 54/54 regression sections:

```text
.\gradlew.bat :core:compileJava :client:compileJava runRegressionTests
BUILD SUCCESSFUL in 44s
runRegressionTests: All sections PASS (54 sections)
  new section: testCollisionWorldXResolution (right-wall, left-wall, platform pass-through)
```

Previous gate (2026-05-09) — M4 SUMMIT_SHRINE + HOLLOW_DEPTHS contract authoring; PlaytestClient decomposition (InputHandler, RoomGeometry, SaveLoad); LibGDX P1 scaffold + tile rendering:

```text
.\gradlew.bat runDataContractDiagnostics runWorldgenDiagnostics
BUILD SUCCESSFUL in 2m 12s
runDataContractDiagnostics: contracts_loaded=true valid=true beats=45 critical_flags=69
  plateaus=7 world_regions=3 factions=3 settlements=3
  Validation issues: none
runWorldgenDiagnostics: Section templates loaded: 13, validation issues: 0
```

Previous gate (2026-05-08) — Wave 5 extractions + LibGDX scaffold + echo puzzle room + faction tension mutation + M3 save envelope / checksum guard:

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
2. **M4 campaign content** — continue from the authored `SUMMIT_SHRINE` + `HOLLOW_DEPTHS` base by finishing the `HOLLOW_DEPTHS` runtime slice first, then move into later plateau delivery (`EMBER_MONASTERY`, `WINDING_SKYROAD`, `MIRROR_SUMMIT`, `BEACON_CLIFF`) with explicit area bootstrap, NPC placement, and mission surfacing.
3. ~~**Chunk-streaming geometry (M6)**~~ — resolved 2026-05-08. `RegionFragmentData.tiles` + `RegionLoader` parse path; all 3 region fragment JSONs authored; `addSolidTile`/`addPlatformTile` removed.
3a. ~~**Mutation visibility evidence (M6)**~~ — resolved 2026-05-08. HUD overlay line (amber when active), `MUTATION_OVERLAY_SAVE`/`LOAD` evidence lines, `refreshOverlayHud()` wired at init/transition/load.
4. ~~**Echo puzzle evaluation (M6)**~~ — resolved 2026-05-08. `EchoPuzzleSolution` + `EchoPuzzleEvaluator` wired; first authored echo puzzle room (summit_echo_room_1, minKills=1) in PlaytestClient Room 4; PUZZLE_PASSED/PUZZLE_FAILED emitted.
5. ~~**Co-op collision model (M6)**~~ — resolved 2026-05-08. `tickCoopCollisions()` in `GameSimulator`; pairwise AABB separation with MSA push; `PLAYER_COLLISION` event; dead players excluded.
6. ~~**P2: `core.physics.CollisionWorld`**~~ — closed 2026-05-09. `resolveX` + `resolveY` both wired; `GameSimulator.tickPlayers()` uses real AABB resolution; spawnY stub is fallback only when no `CollisionWorld` injected.
7. ~~**LibGDX world geometry rendering**~~ — closed 2026-05-09. `StubWorldRenderer` renders tile geometry; `ShadowAscentGame` builds and passes stub tile list; camera bounds derived from tile extents.
8. **Next LibGDX follow-up** — keep expanding authored production-client geometry, authored placement, content-complete interaction flow, and state fidelity in `runGame`, without broadening into full art migration.
