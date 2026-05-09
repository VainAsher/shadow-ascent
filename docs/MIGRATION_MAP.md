---
doc_type: migration_map
status: living
owner: core-team
last_updated: 2026-05-09
version_anchor: 0.0.1
---
# File-by-File Migration Map

This map defines **exact first imports** from donor repos into `shadow_ascent_clean_start`, with bounded scope, effort, and risk.

## Rating Scale

- Effort: `S` (small), `M` (medium), `L` (large), `XL` (very large).
- Risk: `Low`, `Medium`, `High`.

## Wave 0 — Data Contract Bootstrapping (completed)

| Source Repo | Source Path | Target Path | Purpose | Effort | Risk | Status |
|---|---|---|---|---|---|---|
| integrated | `data/story_flags.json` | `data/story_flags.json` | canonical critical flag registry | S | Low | done |
| integrated | `data/narrative_beats.json` | `data/narrative_beats.json` | authored/adaptable beat timeline | S | Low | done |
| integrated | `data/plateaus.json` | `data/plateaus.json` | plateau registry + critical routes | S | Low | done |
| integrated | `data/area_catalog.json` | `data/area_catalog.json` | area families/instances contract | S | Low | done |
| integrated | `data/npc_registry.json` | `data/npc_registry.json` | NPC eligibility contract | S | Low | done |
| integrated | `data/dialogue.json` | `data/dialogue.json` | dialogue reference catalog | S | Low | done |
| integrated | `data/quests.json` | `data/quests.json` | quest chain metadata source | S | Low | done |
| integrated | `data/chunk_grammar.json` | `data/chunk_grammar.json` | chunk category semantics | S | Low | done |
| integrated | `data/adaptation_rules.json` | `data/adaptation_rules.json` | runtime adaptation constraints | S | Low | done |
| integrated | `data/schemas/narrative_data_schema.json` | `data/schemas/narrative_data_schema.json` | schema reference | S | Low | done |
| integrated | `data/README_DATA.md` | `data/README_DATA.md` | contract load-order reference | S | Low | done |

## Wave 1 — Contract Runtime and Diagnostics (completed)

| Source Repo | Source Path | Target Path | Purpose | Effort | Risk | Status |
|---|---|---|---|---|---|---|
| clean-start (new) | n/a | `java/core/src/main/java/com/shadowascent/core/data/GameDataContracts.java` | load/validate contracts; progression query API | M | Medium | done |
| clean-start (new) | n/a | `java/core/src/main/java/com/shadowascent/core/data/BeatDefinition.java` | typed beat contract model | S | Low | done |
| clean-start (new) | n/a | `java/core/src/main/java/com/shadowascent/core/DataContractDiagnostics.java` | CLI diagnostics for contract health | S | Low | done |
| clean-start (new) | n/a | `data/world_state.json` + `data/faction_state.json` + `data/settlement_state.json` | M5 simulation contract seed scaffolding | M | Medium | done |
| clean-start (new) | n/a | `java/core/src/main/java/com/shadowascent/core/data/WorldRegionStateDefinition.java` + `FactionStateDefinition.java` + `SettlementStateDefinition.java` + `GameDataContracts.java` updates | typed world/faction/settlement contract indexing + validation | M | Medium | done |
| clean-start (new) | n/a | `java/core/src/main/java/com/shadowascent/core/WorldSimulationDiagnostics.java` + `build.gradle.kts` task | CLI diagnostics entrypoint for M5 simulation contracts | S | Low | done |
| clean-start | `java/core/src/main/java/com/shadowascent/core/GameState.java` | same | wire contract loading into runtime startup | S | Low | done |
| clean-start | `build.gradle.kts` | same | add `jackson-databind` and diagnostics task | S | Low | done |
| clean-start | `data/npc_registry.json` | same | reconcile beat-referenced NPC IDs and plateau eligibility | S | Low | done |

## Wave 2 — Old Repo Worldgen/Validation Import (completed)

| Source Repo | Source Path | Target Module/Path | Purpose | Effort | Risk | Status |
|---|---|---|---|---|---|---|
| old | `java/shadowascent/src/main/java/com/indieniinja/world/sections/SectionTemplate.java` | `java/core/.../world/sections/SectionTemplate.java` | core section template model | M | Medium | done |
| old | `java/shadowascent/src/main/java/com/indieniinja/world/sections/SectionTemplateLibrary.java` | `java/core/.../world/sections/SectionTemplateLibrary.java` | load section templates with strict-mode option | M | Medium | done |
| old | `java/shadowascent/src/main/java/com/indieniinja/world/sections/SectionTemplateValidator.java` | `java/core/.../world/sections/SectionTemplateValidator.java` | schema/contract-level validation | M | Medium | done |
| old | `java/shadowascent/src/main/java/com/indieniinja/world/sections/SectionTemplateValidationIssue.java` | `java/core/.../world/sections/SectionTemplateValidationIssue.java` | structured validation issue reporting | S | Low | done |
| old | `java/shadowascent/src/main/java/com/indieniinja/world/validation/GenerationValidationPlanner.java` | `java/core/.../world/validation/GenerationValidationPlanner.java` | bounded repair action planning | M | Medium | done |
| old | `java/shadowascent/src/main/java/com/indieniinja/world/validation/GenerationValidationReport.java` | `java/core/.../world/validation/GenerationValidationReport.java` | validation result envelope | S | Low | done |
| old | `java/shadowascent/src/main/java/com/indieniinja/world/progression/ProgressionValidator.java` | `java/core/.../world/progression/ProgressionValidator.java` | progression graph integrity checks | M | Medium | done |
| old | `java/shadowascent/src/main/java/com/indieniinja/world/progression/WorldProgressionGraph.java` | `java/core/.../world/progression/WorldProgressionGraph.java` | progression model backbone | M | Medium | done |
| clean-start (new) | n/a | `java/core/src/main/java/com/shadowascent/core/world/progression/ProgressionValidationResult.java` | normalized progression validation result object | S | Low | done |
| clean-start (new) | n/a | `java/core/src/main/java/com/shadowascent/core/WorldgenDiagnostics.java` + `build.gradle.kts` task | CLI verification entrypoint for imported worldgen slices | S | Low | done |
| old | `java/shadowascent/src/test/java/com/indieniinja/world/sections/SectionTemplateLibraryTest.java` + `SectionTemplateValidatorTest.java` + `SectionTemplateVarietyDataTest.java` | `java/core/src/main/java/com/shadowascent/core/RegressionTest.java` (parity checks) | port donor worldgen library/validator/variety assertions into clean-start regression harness | M | Low | done |
| old | `data/worldgen/sections/*.json` + `data/worldgen/progressions/act1.json` | `data/worldgen/sections/*.json` + `data/worldgen/progressions/act1.json` | import initial authored worldgen datasets and non-zero diagnostics path | S | Low | done |
| old | `java/shadowascent/src/test/java/com/indieniijah/world/validation/GenerationValidationPlannerTest.java` | `java/core/src/main/.../RegressionTest.java` (`testGenerationValidationPlannerPlanning`) | planner tests ported into regression harness; hub-in-worldNodes usage note captured in CODEX doc | M | Low | done |

## Wave 3 — Mission/Story Runtime Realignment (completed)

| Source Repo | Source Path | Target Module/Path | Purpose | Effort | Risk | Status |
|---|---|---|---|---|---|---|
| integrated | `data/quests.json` + `data/narrative_beats.json` | `java/core/src/main/java/com/shadowascent/core/data/ContractMissionTemplateCatalog.java` + `MissionTemplateDefinition.java` | contract-backed mission template catalog | M | Medium | done |
| integrated | `data/quests.json` chains/steps | `java/core/src/main/java/com/shadowascent/core/data/SideQuestStepDefinition.java` + `ContractMissionTemplateCatalog.java` + `MissionManager.java` + `StoryState.java` | side-quest chain availability/completion runtime rules, per-step objective decomposition, and typed reward-effect application/persistence (`required_flags`, `act`, `plateau`, `sets_flags`, `objective`, `area_pool`, `enemy`, explicit `reward_effects` with `type`/`magnitude`/`payload`) | M | Medium | done |
| clean-start | `java/core/src/main/java/com/shadowascent/core/Mission.java` | same | add objective progress state and completion evidence | M | Medium | done |
| clean-start | `java/core/src/main/java/com/shadowascent/core/MissionManager.java` | same | replace auto-complete placeholder logic | M | High | done |
| clean-start | `java/core/src/main/java/com/shadowascent/core/HubManager.java` + `GameDataContracts.java` | same | align hub transitions with beat/flag progression, then derive NPC availability from beat-context + `npc_registry` contracts (fallback tables only for empty-schedule edge cases) | M | Medium | done |
| clean-start | `java/core/src/main/java/com/shadowascent/core/HubManager.java` + `GameDataContracts.java` + `data/dialogue.json` | same | route high-frequency hub dialogue selection through contract-authored dialogue mappings with legacy fallback | M | Medium | done |
| clean-start | `java/core/src/main/java/com/shadowascent/core/StoryState.java` + `GameState.java` | same | add versioned save envelope support with backward-compatible legacy load path | M | Medium | done |

## Wave 4 — Simulation System Import From Old Repo (staged)

| Source Repo | Source Path | Target Module/Path | Purpose | Effort | Risk | Status |
|---|---|---|---|---|---|---|
| old | `java/shadowascent/src/main/java/com/indieniinja/sim/SimPlayer.java` | `java/core/.../sim/SimPlayer.java` | movement/combat actor model | L | High | queued |
| old | `java/shadowascent/src/main/java/com/indieniinja/sim/EnemyAIState.java` | `java/core/.../simulation/EnemyAIState.java` | enemy AI state enum | S | Low | done |
| old | `java/shadowascent/src/main/java/com/indieniinja/sim/EnemyAwarenessState.java` | `java/core/.../simulation/EnemyAwarenessState.java` | enemy awareness tier enum | S | Low | done |
| old | `java/shadowascent/src/main/java/com/indieniinja/sim/SimEnemy.java` | `java/core/.../simulation/SimEnemy.java` | server-side enemy entity model | S | Low | done |
| old | `java/shadowascent/src/main/java/com/indieniinja/sim/ItemDatabase.java` | `java/core/.../simulation/ItemDatabase.java` | static item definition catalog | S | Low | done |
| old | `java/shadowascent/src/main/java/com/indieniinja/sim/SimInventory.java` | `java/core/.../simulation/SimInventory.java` | 20-slot player inventory runtime | S | Low | done |
| old | `java/core/src/main/java/com/indieniinja/network/InputCommand.java` | `java/core/.../simulation/InputCommand.java` | input snapshot value type | S | Low | done |
| old | `java/shadowascent/src/main/java/com/indieniinja/sim/YinYangComponent.java` | `java/core/.../simulation/YinYangComponent.java` | Yin/Yang emotional balance component | S | Low | done |
| old | `java/shadowascent/src/main/java/com/indieniinja/sim/LanternComponent.java` | `java/core/.../simulation/LanternComponent.java` | Lantern clarity component | S | Low | done |
| old | `java/shadowascent/src/main/java/com/indieniinja/sim/EchoRecorder.java` | `java/core/.../simulation/EchoRecorder.java` | 10s input ring buffer for echo playback | S | Low | done |
| old | `java/shadowascent/src/main/java/com/indieniinja/sim/SimPlayer.java` | `java/core/.../simulation/SimPlayer.java` | server-side player simulation state | M | Low | done |
| old | `java/shadowascent/src/main/java/com/indieniinja/sim/BossAIState.java` | `java/core/.../simulation/BossAIState.java` | boss AI state enum | S | Low | done |
| old | `java/shadowascent/src/main/java/com/indieniinja/sim/BossType.java` | `java/core/.../simulation/BossType.java` | boss type definitions | S | Low | done |
| old | `java/shadowascent/src/main/java/com/indieniijah/sim/SimBoss.java` | `java/core/.../simulation/SimBoss.java` | server-side boss simulation entity | M | Low | done |
| old | `java/shadowascent/src/main/java/com/indieniinja/sim/BossPatternLibrary.java` | `java/core/.../simulation/BossPatternLibrary.java` | boss psychological pattern dispatch | M | Low | done |
| old | `java/shadowascent/src/main/java/com/indieniinja/sim/ReplayPlayer.java` | `java/core/.../simulation/ReplayPlayer.java` | input-log replay engine for echo puzzle playback | S | Low | done |
| old | `java/shadowascent/src/main/java/com/indieniinja/sim/SimNPC.java` | `java/core/.../simulation/SimNPC.java` | server-side NPC patrol entity | S | Low | done |
| old | `java/shadowascent/src/main/java/com/indieniinja/sim/SimEcho.java` | `java/core/.../simulation/SimEcho.java` | echo playback entity for replay-puzzle evaluation | S | Low | done |
| old | `java/shadowascent/src/main/java/com/indieniinja/sim/SimMovingPlatform.java` | `java/core/.../simulation/SimMovingPlatform.java` | horizontal oscillating platform entity | S | Low | done |
| old | `java/shadowascent/src/main/java/com/indieniinja/sim/SimPickup.java` | `java/core/.../simulation/SimPickup.java` | authoritative pickup collection entity | S | Low | done |
| old | `java/shadowascent/src/main/java/com/indieniinja/sim/SimShuriken.java` | `java/core/.../simulation/SimShuriken.java` | server-side shuriken projectile entity | S | Low | done |
| old | `java/shadowascent/src/main/java/com/indieniijah/sim/SimPortal.java` | `java/core/.../simulation/SimPortal.java` | portal entity with ability gate; toState() replaced with toMap() | S | Low | done |
| old | `java/shadowascent/src/main/java/com/indieniinja/sim/CraftingRecipe.java` | `java/core/.../simulation/CraftingRecipe.java` | recipe definition with ingredient + craft logic | S | Low | done |
| old | `java/shadowascent/src/main/java/com/indieniinja/sim/RecipeBook.java` | `java/core/.../simulation/RecipeBook.java` | static recipe registry with category lookup | S | Low | done |
| old | `java/shadowascent/src/main/java/com/indieniinja/sim/SimShop.java` | `java/core/.../simulation/SimShop.java` | tiered NPC shop with buy/sell transaction runtime | S | Low | done |
| old | `java/shadowascent/src/main/java/com/indieniinja/sim/GameSimulator.java` (applyPlayerInput) | `java/core/.../simulation/PlayerInputController.java` | bounded extraction of full player input state machine; external deps inlined/stubbed | L | Medium | done |
| old | `java/shadowascent/src/main/java/com/indieniinja/sim/GameSimulator.java` | decomposed across `sim/` subsystems | authoritative simulation core (must be sliced, not copied whole) | XL | High | done (bounded slice) |
| old | `java/core/src/main/java/com/indieniinja/physics/PhysicsConstants.java` + `PhysicsState.java` | `java/core/.../physics/*` | donor movement/physics profile baseline for playable client parity | M | Medium | done |
| old | `java/core/src/main/java/com/indieniinja/physics/TileType.java` + `TileRect.java` + `SpatialHash.java` | `java/core/.../physics/*` | modular collision/hash primitives for chunked traversal candidate lookup | M | Medium | done |
| old | `java/shadowascent/src/main/java/com/indieniinja/sim/GameSimulator.java` (`applyPlayerInput` slice) | `java/client/.../controller/*` + `java/core/.../physics/*` | jump/dash/coyote/wall movement control loop import in bounded adapter form | L | High | done |
| clean-start (new) | n/a | `java/client/src/main/java/com/shadowascent/client/PlaytestClient.java` | bounded dynamic traversal layer: moving platforms + ability-gated blockers mapped to story abilities | M | Medium | done |
| clean-start (new) | n/a | `java/client/src/main/java/com/shadowascent/client/PlaytestClient.java` | first authored multi-room traversal topology with camera-follow across bounded route segments | M | Medium | done |
| clean-start (new) | n/a | `java/client/src/main/java/com/shadowascent/client/PlaytestClient.java` | ability-execution trigger layer (dash-pass + interact activators) with route-platform unlock side effects | M | Medium | done |
| clean-start (new) | n/a | `java/client/src/main/java/com/shadowascent/client/PlaytestClient.java` | first combat encounter loop slice (attack input, telegraphed state windows, encounter clear state, combat-seal barriers, mission objective hooks) | M | Medium | done |
| clean-start (new) | n/a | `java/core/src/main/java/com/shadowascent/core/simulation/WorldSimulationTick.java` + `WorldSimulationTickResult.java` + `WorldSimulationEvent.java` + `WorldSimulationPressureSample.java` | deterministic M5 world-simulation tick + event emission scaffold | M | Medium | done |

## Wave 5 — Client Runtime Systems (post-core parity)

| Source Repo | Source Path | Target Module/Path | Purpose | Effort | Risk | Status |
|---|---|---|---|---|---|---|
| old | `java/client/src/main/java/com/indieniinja/client/game/StoryManager.java` | `java/client/.../StoryManager.java` | client-side story presentation integration | M | Medium | done (2026-05-09) |
| old | `java/client/src/main/java/com/indieniinja/client/game/MissionManager.java` | `java/client/.../MissionUiCoordinator.java` | mission UI/runtime coordination (adapted) | M | Medium | done (2026-05-09) |
| old | `java/client/src/main/java/com/indieniinja/client/ui/MinimapRenderer.java` | `java/client/.../ui/MinimapRenderer.java` | minimap gameplay readability | M | Medium | done |
| old | `java/client/src/main/java/com/indieniinja/client/rendering/HudRenderer.java` | `java/client/.../ui/HudRenderer.java` | HUD evolution path | M | Medium | done (2026-05-08) |
| clean-start (new) | n/a | `java/client/src/main/java/com/shadowascent/client/PlaytestClient.java` | MVP human-playtest window to exercise mission/hub runtime interactively | M | Medium | done |
| clean-start (new) | n/a | `java/client/src/main/java/com/shadowascent/client/PlaytestClient.java` | bounded minimap and collision-geometry playability overlays for traversal QA | M | Medium | done |
| clean-start (new) | n/a | `java/client/src/main/java/com/shadowascent/client/PlaytestClient.java` | mission/HUD feedback polish (objective progress surfacing, ability status, gate status, mission feed) | M | Medium | done |
| clean-start (new) | n/a | `java/client/src/main/java/com/shadowascent/client/PlaytestClient.java` | contextual ability-interaction hinting + minimap viewport framing for route readability | S | Low | done |
| clean-start (new) | n/a | `java/client/src/main/java/com/shadowascent/client/PlaytestClient.java` | combat-state HUD/minimap surfacing (attack cooldown, encounter focus, encounter markers) | S | Low | done |
| clean-start (new) | n/a | `java/client/src/main/java/com/shadowascent/client/InventoryPanel.java` + `ShopPanel.java` + `CraftingPanel.java` | UI overlay panels: inventory grid, shop buy/sell, crafting recipe execution; LibGDX patterns adapted to Swing | M | Medium | done |
| clean-start (new) | n/a | `java/client/src/main/java/com/shadowascent/client/MinimapRenderer.java` | extracted minimap rendering from UISubsystem; standalone client class | S | Low | done |
| clean-start (new) | n/a | `java/client/src/main/java/com/shadowascent/client/DesktopLauncher.java` | LibGDX LWJGL3 entry point for production client | S | Low | done (2026-05-09) |
| clean-start (new) | n/a | `java/client/src/main/java/com/shadowascent/client/ShadowAscentGame.java` | LibGDX Game subclass; wires GameSimulator + StubWorldRenderer + GameInputProcessor | S | Low | done (2026-05-09) |
| clean-start (new) | n/a | `java/client/src/main/java/com/shadowascent/client/HubScreen.java` | LibGDX Screen; owns OrthographicCamera, delta-capped tick loop, camera lerp-follow | S | Low | done (2026-05-09) |
| clean-start (new) | n/a | `java/client/src/main/java/com/shadowascent/client/rendering/StubWorldRenderer.java` | P1 entity renderer as ShapeRenderer rectangles; accepts Matrix4 projection matrix | S | Low | done (2026-05-09) |
| clean-start (new) | n/a | `java/client/src/main/java/com/shadowascent/client/input/GameInputProcessor.java` | LibGDX InputAdapter; routes WASD/Space/Shift/F/E/I to InputCommand per frame | S | Low | done (2026-05-09) |

## Wave 6 — M6 Open-World Runtime (new)

| Source Repo | Source Path | Target Module/Path | Purpose | Effort | Risk | Status |
|---|---|---|---|---|---|---|
| clean-start (new) | n/a | `java/core/.../world/streaming/RegionManifest.java` + `RegionInstance.java` + `RegionLoader.java` + `MutationOverlay.java` + `RegionalStreamingConstraintValidator.java` + supporting types | regional streaming constraint model + loader + mutation overlay | L | Medium | done |
| clean-start (new) | n/a | `data/worldgen/regions/*.json` (3 fragment files) | authored region fragment data (hub, forge terraces, hollow shaft) | S | Low | done |
| clean-start (new) | n/a | `java/client/.../PlaytestClient.java` (RegionLoader wiring) | `buildPlaytestProgressionGraph` + `initializeCollisionLayout` + `buildAllDynamicTiles` + `refreshCollisionHashFromRegions` | M | Medium | done |
| clean-start (new) | n/a | `java/core/.../data/OverlayPayloadCodec.java` + SAVE_V3 envelope + v2→v3 migrator | overlay mutation persistence in save envelope with SHA-256 checksum | M | Medium | done |
| clean-start (new) | n/a | `data/worldgen/sections/lantern_region_hub.json` + `lantern_hub.json` + `hollow_dungeon.json` | authored section templates covering 3 previously stub-only biome/kind combos | S | Low | done |

## Explicit Non-Import List

The following donor artifacts are reference-only and should not be imported directly into clean-start:

- monolithic Swing prototype files from integrated repo (`ShadowAscentIntegratedPrototype.java`, legacy slice launchers),
- old repo archive/process-heavy docs under `docs/archive`,
- binary build artifacts and generated outputs.

## Governance

- Update this map when a file moves from `queued` to `active` or `done`.
- Every `done` row must have validation evidence recorded in `docs/CURRENT_STATE.md`.
