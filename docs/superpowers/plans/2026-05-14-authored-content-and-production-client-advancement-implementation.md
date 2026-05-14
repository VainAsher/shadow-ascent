# Authored Content And Production-Client Advancement Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development` (recommended) or `superpowers:executing-plans` to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Move `shadow_ascent_clean_start` from a verified production-client slice into the next campaign-building tranche by normalizing milestone truth, promoting authored plateau data into the LibGDX runtime, deepening mission/NPC/world presentation, and turning donor/GDD content into concrete implementation work instead of passive reference material.

**Architecture:** Keep `shadow_ascent_clean_start` as the canonical implementation home. Keep `java/core` authoritative for campaign state, plateau data, mission state, NPC definitions, save/load, and progression truth. Keep `java/client` responsible for LibGDX presentation, authored runtime placement, and player-facing interaction surfaces. Treat donor repos and the GDD as source material and reference contracts, not as monoliths to import wholesale.

**Tech Stack:** Java 21, LibGDX 1.12.1, LWJGL3 desktop backend, JUnit 5, existing `GameSimulator`/`GameState`/`MissionManager`/`HubManager`/`StoryManager`/`SaveLoad`, JSON-backed campaign data under `data/`, docs governance scripts under `scripts/`, and donor references under `..\shadow_ascent_integrated_package\`.

---

## Current Branch Status

Already true on this branch and reflected by recent commits:

- `runGame` now has title/new-game/continue flow.
- `runGame` now has HUD, minimap, inventory, shop, crafting, dialogue, pause/save/load, audio event routing, and interaction hints.
- `:client:test`, `clean :client:compileJava`, `packSprites`, `runRegressionTests`, and docs freshness were recently reported passing.
- M0, M1, M2, M3, and M5 are effectively complete.
- M4 is partially delivered and still active, despite wording drift across docs.
- M6 groundwork is already substantial: region streaming, persistence, validation, and co-op scaffolding exist.

What remains strategically underdelivered:

- `runGame` still depends on bounded/fixture-style world setup rather than authored plateau/area placement.
- plateau and area data exist, but the LibGDX runtime is not yet visibly driven by them.
- the next campaign-content tranche after `SUMMIT_SHRINE` and `HOLLOW_DEPTHS` is not yet translated into executable runtime progression.
- mission/NPC surfacing is still shallower in `runGame` than the repo’s available story data justifies.
- docs are broadly healthy, but milestone wording and primary-QA-surface guidance need a deliberate sync pass.
- `runGame` player melee combat is not yet at donor parity: attack input and animation state exist, but front-facing melee hitbox resolution against enemies still needs an explicit runtime slice.

## Source Of Truth And Input Hierarchy

Use inputs in this order when implementing the plan:

1. **Executable truth in `shadow_ascent_clean_start`**
   - `java/core/src/main/java/...`
   - `java/client/src/main/java/...`
   - `data/plateaus.json`
   - `data/narrative_beats.json`
   - `data/area_catalog.json`
   - `data/npc_registry.json`
   - `data/dialogue.json`
   - `data/quests.json`
   - `docs/CURRENT_STATE.md`
   - `docs/ROADMAP.md`
   - `docs/PLAYABLE_TRUTH.md`
   - `docs/MIGRATION_MAP.md`

2. **Living GDD in `game_design_document/`**
   - `09_level_and_content_plan/`
   - `11_production_scope_and_roadmap/`
   - `12_qa_telemetry_and_acceptance_criteria/`
   - `14_appendices/02_region_implementation_checklist/`
   - `14_appendices/04_room_design_template/`
   - `14_appendices/05_example_room_spec/`
   - `15_character_dossiers/`
   - `00_governance/04_decision_log/2026-05-09-project-and-donor-review.md`

3. **Integrated donor package**
   - `..\shadow_ascent_integrated_package\data\`
   - `..\shadow_ascent_integrated_package\docs\PLAYABLE_SLICE_ROADMAP.md`
   - `..\shadow_ascent_integrated_package\docs\01_PRODUCT_AND_TECH_SPEC.md`
   - `..\shadow_ascent_integrated_package\docs\02_ARCHITECTURE.md`
   - `..\shadow_ascent_integrated_package\docs\17_EMOTIONAL_VERTICAL_SLICE_REBUILD.md`
   - `..\shadow_ascent_integrated_package\docs\21_NEXT_BUILD_RULES.md`
   - `..\shadow_ascent_integrated_package\docs\28_INTEGRATED_SOURCE_ANALYSIS_AND_IMPLEMENTATION.md`

4. **Embedded engineering donor reference**
   - `..\shadow_ascent_integrated_package\reference\indieniinja_source_reference\`

Interpretation rules:

- If repo runtime truth conflicts with donor material, prefer the clean-start repo.
- If repo data and GDD align, treat that as strong implementation direction.
- If the GDD or donor implies a feature that has no runtime support yet, treat it as planned work, not current fact.
- Do not import large donor monoliths directly; slice behavior into bounded modules and tests.

## Donor And GDD Data To Exploit Deliberately

This plan assumes the following content/data is not decorative and should drive implementation:

- `data/plateaus.json`: plateau order, mood, elastic scope, route identity, progression expectations
- `data/narrative_beats.json`: authored campaign beat sequencing
- `data/area_catalog.json`: region families, area IDs, source beats, transition anchors
- `data/npc_registry.json`: NPC availability and role states by plateau
- `data/dialogue.json`: plateau-specific and beat-specific dialogue
- `data/quests.json`: authored side-quest anchors and plateau eligibility
- donor `elastic_chunk_templates.json`: optional-content and room-intent inspiration
- GDD `09_level_and_content_plan/02_main_dungeon_and_region_matrix/README.md`: checkpoint order
- GDD `09_level_and_content_plan/01_level_plan_overview/README.md`: plateau-to-plateau campaign structure
- GDD `11_production_scope_and_roadmap/02_vertical_slice_target/README.md`: still-valid emotional proof route
- GDD appendices/templates: room specs, region checklists, acceptance framing

These are the concrete sources for missions, plateaus, level inspiration, NPC returns, and progression gates.

## File Structure

Primary docs to modify:

- Modify: `docs/CURRENT_STATE.md`
- Modify: `docs/ROADMAP.md`
- Modify: `docs/INDEX.md`
- Modify: `docs/IMPLEMENTATION_BACKLOG.md`
- Modify: `docs/PLAYABLE_TRUTH.md`
- Modify: `docs/MIGRATION_MAP.md`
- Modify: `docs/reports/docs_freshness_report.md` via script output only

Primary runtime files likely to modify:

- Modify: `java/client/src/main/java/com/shadowascent/client/ShadowAscentGame.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/HubScreen.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/rendering/SpriteWorldRenderer.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/audio/AudioManager.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/ui/HudOverlayState.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/ui/HudOverlayRenderer.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/StoryManager.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/SaveLoad.java`

Primary core/data files likely to modify:

- Modify: `java/core/src/main/java/com/shadowascent/core/data/...`
- Modify: `java/core/src/main/java/com/shadowascent/core/simulation/...`
- Modify: `data/plateaus.json`
- Modify: `data/narrative_beats.json`
- Modify: `data/area_catalog.json`
- Modify: `data/npc_registry.json`
- Modify: `data/dialogue.json`
- Modify: `data/quests.json`

Likely new runtime files:

- Create: `java/client/src/main/java/com/shadowascent/client/world/AuthoringWorldBootstrap.java`
- Create: `java/client/src/main/java/com/shadowascent/client/world/AreaPlacementResolver.java`
- Create: `java/client/src/main/java/com/shadowascent/client/world/RunGameContentProfile.java`
- Create: `java/client/src/test/java/com/shadowascent/client/world/AuthoringWorldBootstrapTest.java`
- Create: `java/client/src/test/java/com/shadowascent/client/RunGameFlowSmokeTest.java`

## Decomposition Notes

This plan intentionally separates three kinds of work:

1. **Truth-alignment work**
   - docs, milestone language, QA-surface policy, backlog clarity

2. **Runtime-deepening work**
   - authored geometry, authored NPC placement, mission surfacing, animation/audio, smoke coverage

3. **Campaign-content work**
   - the next M4 tranche using donor data and GDD guidance

That separation matters. The repo is no longer blocked by systems plumbing. The next risk is mixing content authoring, runtime migration, and doc maintenance into one untestable tranche.

---

## Task 1: Normalize Milestone Truth Across Docs

**Goal:** Eliminate ambiguity about M4, M6, and the current LibGDX client status before further implementation work.

**Files:**
- Modify: `docs/CURRENT_STATE.md`
- Modify: `docs/ROADMAP.md`
- Modify: `docs/INDEX.md`
- Modify: `docs/IMPLEMENTATION_BACKLOG.md`
- Modify: `docs/PLAYABLE_TRUTH.md`

- [ ] Align all status docs so they agree that:
  - M4 is **active and partially delivered**, not queued.
  - `SUMMIT_SHRINE` and `HOLLOW_DEPTHS` are already partially authored and playable.
  - `runGame` is now a serious production-client slice, but not yet the sole QA surface.
  - M6 foundation work is active and already materially landed.
- [ ] Add a concise “what is left in M4” line to the roadmap rather than leaving campaign completion implied.
- [ ] Update `docs/INDEX.md` so the canonical navigation reflects the newer production-client and GDD planning documents.
- [ ] Preserve historical evidence rather than silently overwriting earlier milestone claims.

**Acceptance:**

- [ ] No contradiction remains between `ROADMAP`, `CURRENT_STATE`, and `PLAYABLE_TRUTH` on M4 status.
- [ ] `INDEX.md` routes readers to current plan and status surfaces.

## Task 2: Define The Remaining M4 Campaign Spine Explicitly

**Goal:** Turn “campaign completion/content scale” into an executable milestone with named plateaus, dungeons, and gates.

**Files:**
- Modify: `docs/ROADMAP.md`
- Modify: `docs/IMPLEMENTATION_BACKLOG.md`
- Create: `docs/superpowers/plans/2026-05-14-m4-campaign-spine-content-plan.md` if the scope needs a dedicated content-only child plan
- Input: `game_design_document/09_level_and_content_plan/...`
- Input: `data/plateaus.json`
- Input: `data/narrative_beats.json`

- [ ] Enumerate the full critical-path sequence already implied by repo data and GDD:
  - `LANTERN_HEIGHTS`
  - `SUMMIT_SHRINE`
  - `HOLLOW_DEPTHS`
  - `EMBER_MONASTERY`
  - `WINDING_SKYROAD`
  - `MIRROR_SUMMIT`
  - `BEACON_CLIFF`
- [ ] Map each plateau to one primary emotional job, one primary mechanical job, and one runtime-deliverable expectation.
- [ ] Translate the GDD dungeon matrix into implementation-facing checkpoints:
  - Weightbound Mines
  - Shatter Moth Nest
  - Stone Judge Maze
  - Hearth of Brothers
  - Mentor Roga’s Dojo
  - Winding Skyroad Ascent
  - Mirror Summit
  - Beacon of Return
- [ ] Define what “M4 done” means in observable runtime terms, not only narrative terms.

**Acceptance:**

- [ ] M4 has a bounded finish definition.
- [ ] Plateau/dungeon ordering is canonical in one place.

## Task 3: Build A Source-Backed Content Inventory From Repo Data, Donor Data, And GDD

**Goal:** Create a practical import ledger for campaign content so runtime work uses known source material instead of ad hoc hand placement.

**Files:**
- Modify: `docs/MIGRATION_MAP.md`
- Create: `docs/superpowers/plans/2026-05-14-content-source-ledger.md` if needed
- Input: `data/*.json`
- Input: `..\shadow_ascent_integrated_package\data\*.json`
- Input: `game_design_document/09_level_and_content_plan/...`
- Input: `game_design_document/15_character_dossiers/...`

- [ ] Inventory authored beats, areas, dialogue, NPCs, quests, and optional-content ideas already present in clean-start.
- [ ] Compare those with the integrated donor package to identify:
  - already-imported assets/data
  - partially imported concepts
  - donor-only content still useful as design inspiration
- [ ] Mark which content is production-ready data, which is design-direction only, and which still needs clean-start-specific adaptation.
- [ ] Record this as a bounded source ledger, not a vague “check donor later” note.

**Acceptance:**

- [ ] The team can answer “which source file should drive this mission/plateau/NPC?” without rediscovery work.
- [ ] No future contributor needs to mine the donor repos blindly for plateau content.

## Task 4: Replace Fixture World Bootstrap In `runGame` With Authored Area Bootstrap

**Goal:** Stop relying on bounded placeholder room setup and move `runGame` onto an authored, data-backed area bootstrap path.

**Files:**
- Modify: `java/client/src/main/java/com/shadowascent/client/ShadowAscentGame.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/HubScreen.java`
- Create: `java/client/src/main/java/com/shadowascent/client/world/AuthoringWorldBootstrap.java`
- Create: `java/client/src/main/java/com/shadowascent/client/world/AreaPlacementResolver.java`
- Test: `java/client/src/test/java/com/shadowascent/client/world/AuthoringWorldBootstrapTest.java`
- Input: `data/area_catalog.json`
- Input: `data/plateaus.json`
- Input: donor section/worldgen references where useful

- [ ] Design a data-backed bootstrap API that can select a starting authored area and instantiate world geometry, traversal anchors, and NPC slots from area identity rather than hardcoded coordinates.
- [ ] Use plateau/area data as the first selector, even if some geometry remains simplified in the first pass.
- [ ] Keep the bootstrap path modular enough to support future region streaming rather than baking everything into `HubScreen`.
- [ ] Preserve fallback behavior for current smoke testing while the authored bootstrap path hardens.

**Acceptance:**

- [ ] `runGame` can start from an authored area identity instead of a fixture-only room setup.
- [ ] The bootstrap path is test-backed and not entangled with overlay/UI code.

## Task 5: Replace Fixture NPC Placement With Authored Runtime Placement

**Goal:** Make merchant, instructor, and future story-NPC placement come from data-backed role selection and area rules.

**Files:**
- Modify: `java/client/src/main/java/com/shadowascent/client/ShadowAscentGame.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/HubScreen.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/StoryManager.java`
- Modify: `java/core/src/main/java/com/shadowascent/core/data/...`
- Input: `data/npc_registry.json`
- Input: `data/dialogue.json`
- Input: `data/narrative_beats.json`
- Input: GDD character dossiers and plateau docs

- [ ] Introduce a placement/resolution layer that maps:
  - current plateau
  - current beat/flags
  - current authored area
  - NPC role availability
  into visible runtime NPC placement.
- [ ] Move merchant/shop, trainer/dojo, and mission-giver presence away from hardcoded coordinates wherever practical.
- [ ] Make NPC identity available to the HUD, dialogue modal, and mission handoff logic in a consistent way.
- [ ] Keep authored placement deterministic and save/load compatible.

**Acceptance:**

- [ ] NPC presentation in `runGame` reflects plateau and beat state rather than a static sandbox setup.
- [ ] Dialogue and interaction hints can name authored NPCs reliably.

## Task 6: Deepen Mission, Objective, And Interaction Surfacing In `runGame`

**Goal:** Use the existing quest/beat/dialogue data to make `runGame` read like a campaign client rather than only a systems demo.

**Files:**
- Modify: `java/client/src/main/java/com/shadowascent/client/ui/HudOverlayState.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/ui/HudOverlayRenderer.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/HubScreen.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/StoryManager.java`
- Test: `java/client/src/test/java/com/shadowascent/client/ui/HudOverlayStateTest.java`
- Input: `data/quests.json`
- Input: `data/narrative_beats.json`
- Input: `data/dialogue.json`
- Input: `PlaytestClient` / `MissionUiCoordinator` behavior as reference

- [ ] Expand HUD state to surface:
  - current mission title
  - current authored objective
  - contextual interact prompt
  - next meaningful route or gate hint
  - beat-driven event feed lines
- [ ] Make mission progression and dialogue handoff read from actual beat/quest state rather than generic fallback text.
- [ ] Add support for authored gate-state messages such as “return to forge,” “speak to Sophia,” or “cross to Summit approach.”
- [ ] Keep the surface compact and readable; do not convert the HUD into a debug wall.

**Acceptance:**

- [ ] `runGame` makes the next critical action legible to a player without reading docs.
- [ ] Mission surfacing is driven by repo data, not hand-authored UI strings scattered in the client.

## Task 6A: Restore Player Melee Combat Parity In `runGame`

**Goal:** Close a core gameplay-readiness gap by making player attack input resolve through a real front-facing melee hitbox/hurtbox path instead of only animation/state changes.

**Files:**
- Modify: `java/core/src/main/java/com/shadowascent/core/simulation/GameSimulator.java`
- Modify: `java/core/src/main/java/com/shadowascent/core/simulation/SimPlayer.java`
- Modify: `java/core/src/main/java/com/shadowascent/core/simulation/PlayerInputController.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/rendering/SpriteWorldRenderer.java` only if presentation support is needed
- Test: `java/core/src/test/java/com/shadowascent/core/simulation/GameSimulatorMeleeCombatTest.java`
- Input: donor melee/hitbox behavior from integrated and `indieniinja` references

- [ ] Add a focused simulation test that proves the current regression:
  - attack hits an enemy in front of the player
  - attack does not hit an enemy behind the player
  - one swing does not apply repeated damage every active tick
- [ ] Implement a bounded first-pass melee hitbox using the player’s facing direction and current melee reach/height constants.
- [ ] Emit combat events consistently so HUD/event-feed/audio routing can observe successful hits and kills.
- [ ] Keep this tranche intentionally small:
  - no combo tree yet
  - no eight-direction input redesign yet
  - no weapon-specific branching yet
- [ ] Record follow-up opportunities explicitly:
  - input-direction aimed attacks
  - basic combo sequencing
  - weapon/profile-specific attack arcs

**Acceptance:**

- [ ] `runGame` player attacks can damage and kill enemies in front of the player.
- [ ] The melee path is covered by focused automated tests.
- [ ] Future combo/directional attack work can layer on top of this instead of replacing it.

## Task 7: Expand `runGame` Animation And State Fidelity

**Goal:** Better reflect the simulation states already present so the production client feels less like a debug shell.

**Files:**
- Modify: `java/client/src/main/java/com/shadowascent/client/rendering/SpriteWorldRenderer.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/ShadowAscentGame.java`
- Test: targeted rendering/state mapping tests as needed
- Input: current atlas assets
- Input: donor animation/state mapping references from `indieniinja`

- [ ] Audit which simulation states already exist but are not visually distinguished well enough:
  - attack
  - dash
  - jump/fall
  - wall interaction
  - damage/hurt
  - death/defeat
  - enemy alert/idle/patrol differences
- [ ] Expand state-to-animation mapping without destabilizing the renderer.
- [ ] Keep placeholder visuals acceptable where no final sprites exist, but improve legibility through frame choice, tinting, pose choice, and timing.
- [ ] Preserve atlas packing and sprite-pipeline compatibility.

**Acceptance:**

- [ ] A tester can reliably tell what the player and major enemies are doing.
- [ ] State presentation matches simulation truth more closely.

## Task 8: Finish Real Audio Playback On Top Of The Existing Audio Routing Layer

**Goal:** Convert the existing audio event-key routing into actual runtime playback with plateau-aware music and interaction/combat cues.

**Files:**
- Modify: `java/client/src/main/java/com/shadowascent/client/audio/AudioManager.java`
- Modify: `java/client/src/main/java/com/shadowascent/client/ShadowAscentGame.java`
- Modify: `java/client/src/main/resources/audio/audio_registry.json`
- Test: `java/client/src/test/java/com/shadowascent/client/audio/AudioManagerEventRoutingTest.java`
- Input: GDD audio system/music branches
- Input: donor references where useful

- [ ] Load and validate the audio registry at runtime instead of resolving keys only.
- [ ] Bind SFX to current interaction/combat events already flowing through the client.
- [ ] Introduce plateau-aware or screen-aware music selection where assets exist.
- [ ] Fail safely when assets are missing so CI and headless tests do not break.
- [ ] Keep audio behavior deterministic enough for smoke validation.

**Acceptance:**

- [ ] Event routing produces audible output when assets exist.
- [ ] Missing assets degrade safely instead of crashing the client.

## Task 9: Add Production-Client Smoke Coverage For Real Screen And Flow Transitions

**Goal:** Add automated protection around the runtime paths that have now grown beyond unit-scale overlay tests.

**Files:**
- Create: `java/client/src/test/java/com/shadowascent/client/RunGameFlowSmokeTest.java`
- Modify: related screen/controller tests
- Input: `TitleScreen`, `ShadowAscentGame`, `HubScreen`, `SaveLoad`

- [ ] Add smoke tests for:
  - title -> new game
  - title -> continue
  - pause -> resume
  - pause -> save
  - pause -> load
  - pause -> quit to title
  - dialogue open/advance/close
  - inventory/shop/crafting modal exclusivity
- [ ] Add one authored-world bootstrap smoke path once Task 4 lands.
- [ ] Keep rendering assertions lightweight; test state transitions and screen ownership first.

**Acceptance:**

- [ ] The main player-facing flow in `runGame` is guarded against obvious regressions.
- [ ] Future content/runtime work can proceed without relying only on manual playtesting.

## Task 10: Deliver The Next M4 Content Tranche And Formalize The Primary QA Surface

**Goal:** Use the now-stronger runtime path and source ledger to author the next campaign tranche and decide whether `runGame` becomes the main forward QA route.

**Files:**
- Modify: `data/narrative_beats.json`
- Modify: `data/dialogue.json`
- Modify: `data/quests.json`
- Modify: `data/area_catalog.json`
- Modify: `docs/PLAYABLE_TRUTH.md`
- Modify: `docs/CURRENT_STATE.md`
- Modify: `README.md`
- Input: GDD plateau/dungeon matrix
- Input: donor expanded narrative/package docs

- [ ] Choose the immediate next content tranche after current verified work.
  Recommended first tranche:
  - complete the `HOLLOW_DEPTHS` recovery arc into at least one more named dungeon/ability beat
  - or advance into `EMBER_MONASTERY` support-and-training content
- [ ] Add only the content data and runtime hooks needed for that tranche, not a full-campaign dump.
- [ ] Once authored bootstrap and smoke tests exist, make an explicit repo-level decision:
  - `runPlayableClient` remains primary QA surface, or
  - `runGame` becomes the primary forward QA surface with `runPlayableClient` retained as a reference harness
- [ ] Record that decision in the top-level docs.

**Acceptance:**

- [ ] There is one clearly named next campaign slice implemented or queued with explicit files and gates.
- [ ] The repo no longer leaves the “which client should we validate?” question implicit.

---

## Recommended Execution Order

Implement in this order:

1. Task 1: milestone/doc normalization
2. Task 2: explicit M4 spine definition
3. Task 3: source-backed content ledger
4. Task 4: authored area bootstrap
5. Task 5: authored NPC placement
6. Task 6: mission/objective surfacing
7. Task 6A: melee combat parity
8. Task 9: screen-flow smoke coverage
9. Task 7: animation/state fidelity
10. Task 8: real audio playback
11. Task 10: next M4 content tranche + QA-surface decision

Reasoning:

- The first three tasks reduce ambiguity and prevent runtime work from drifting.
- Tasks 4 through 6 are the core shift from slice prototype to authored campaign runtime.
- Task 6A closes the most obvious gameplay-readiness gap before the client is treated as a richer QA surface.
- Smoke tests belong before broader polish so the next tranche has guardrails.
- Content expansion should happen after the runtime can represent authored content properly.

## Verification Strategy

Run after each meaningful tranche:

- `python scripts/check_docs_freshness.py --emit-report`
- `.\gradlew.bat --console=plain :client:test`
- `.\gradlew.bat --console=plain :core:test`
- `.\gradlew.bat --console=plain clean :client:compileJava`
- `.\gradlew.bat --console=plain packSprites`
- `.\gradlew.bat --console=plain runRegressionTests`

Task-specific checks:

- authored bootstrap tests after Task 4
- dialogue/NPC/mission smoke paths after Tasks 5 and 6
- focused melee combat tests after Task 6A
- audio routing tests after Task 8
- full `runGame` flow smoke tests after Task 9

## Risks And Controls

- **Risk:** donor-code sprawl
  - Control: only import bounded behavior slices; update `MIGRATION_MAP.md`
- **Risk:** content claims outrun runtime truth
  - Control: treat data-first content as planned until visible in `runGame`
- **Risk:** GDD and roadmap drift
  - Control: Task 1 and docs freshness gate are mandatory, not optional
- **Risk:** `HubScreen` becomes a second monolith
  - Control: move authored bootstrap and placement logic into dedicated helper classes
- **Risk:** QA splits across two clients indefinitely
  - Control: Task 10 requires an explicit policy decision

## Self-Review

Coverage check:

- docs normalization: covered by Task 1
- explicit remaining M4 work: covered by Task 2
- donor/GDD content exploitation: covered by Task 3 and source sections above
- authored geometry/runtime bootstrap: covered by Task 4
- authored NPC/world placement: covered by Task 5
- mission and interaction surfacing: covered by Task 6
- animation/audio polish: covered by Tasks 7 and 8
- smoke coverage: covered by Task 9
- next campaign-content tranche and QA policy: covered by Task 10

Placeholder scan:

- No task says “use donor repos later” without naming the actual donor/GDD files.
- No task depends on copying donor monoliths directly.
- Each task has concrete file targets and acceptance criteria.

Plan complete and saved to `docs/superpowers/plans/2026-05-14-authored-content-and-production-client-advancement-implementation.md`. Two execution options:

1. Subagent-Driven (recommended) - dispatch parallel bounded tasks with review checkpoints
2. Inline Execution - execute the plan in this session in order

Which approach?
