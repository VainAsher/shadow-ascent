# Act I Vertical Slice Readiness Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use `superpowers:subagent-driven-development` (recommended) or `superpowers:executing-plans` to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make `runGame` systems-complete enough to author, tune, and QA a real Act I / Lantern Heights vertical slice on the LibGDX runtime instead of only validating disconnected runtime features.

**Architecture:** Keep `java/core` authoritative for progression, mission state, combat resolution, dialogue selection, traversal truth, and save/load semantics. Keep `java/client` responsible for authored area presentation, room/transition bootstrap, HUD/dialogue feedback, and player-facing validation tools. Use donor repos and the GDD as source material for geometry, mission shape, pacing, and encounter inspiration, but do not import donor monoliths directly.

**Tech Stack:** Java 21, LibGDX 1.12.1, LWJGL3 desktop backend, JUnit 5, `GameSimulator`, `GameState`, `StoryState`, `MissionManager`, `HubManager`, JSON-authored data under `data/`, and planning/GDD material under `docs/` and `game_design_document/`.

---

## Purpose Of This Plan

This plan is not “finish the whole game.”

It is the readiness checklist for a narrower goal:

1. `runGame` becomes the canonical engine/runtime for gameplay authoring.
2. The first Act I route in `LANTERN_HEIGHTS` becomes intentionally authorable.
3. The resulting slice is strong enough to tune movement, mission flow, NPC staging, encounter scripting, dialogue cadence, save/load continuity, and readability before scaling into later plateaus.

## What “Ready For Act I Vertical Slice Authoring” Means

`runGame` should be considered Act I authoring-ready only when all of these are true:

- Lantern Heights room progression is driven by authored area/room identity, not only placeholder geometry.
- The player can complete the opening social route, first mission handoff, first combat route, and first return-to-hub loop without hidden developer assumptions.
- Combat is mechanically honest enough to tune:
  - attacks hit using explicit hitbox logic
  - enemies react, die, and gate progression coherently
  - damage / invulnerability / encounter clear state are legible
- NPC staging, mission prompts, and dialogue selection are authored enough that Act I feels like a designed route rather than a systems sandbox.
- Traversal gates, checkpoint transitions, save/load, and mission-state restoration are robust enough that content authoring does not rest on brittle runtime behavior.
- QA can follow one repeatable Act I route and tell whether a failure is:
  - content authoring
  - mission logic
  - traversal/transition logic
  - combat/system logic
  - presentation/readability

## Current State Summary

### Already strong enough

- Shared simulation loop exists in `GameSimulator`.
- Input, movement, jump, dash, wall behavior, and basic melee now exist on `runGame`.
- `runGame` has title/new game/continue, HUD, minimap, pause/save/load, dialogue, inventory, shop, crafting, and audio placeholders.
- Plateau/area bootstrap exists and now supports explicit Hollow authored gates.
- Contract-backed missions, beat selection, and dialogue data already exist in repo data.

### Still not ready enough for serious Lantern Heights authoring

- Lantern Heights geometry is still not a real authored room/transition graph.
- Transition gates are still simplified trigger bands rather than authored room exits/checkpoints.
- Encounter state is still too lightweight for real Act I combat pacing.
- NPC placement and scene staging remain bootstrap-level rather than scene-authored.
- Dialogue presentation exists, but scene sequencing, multi-line authored delivery, and first-route pacing are still thin.
- Combat readability exists, but not yet at “tune an opening combat slice” quality.
- There is no robust Act I authoring loop yet for:
  - room iteration
  - transition iteration
  - checkpoint iteration
  - encounter-script iteration

## Readiness Matrix

### Green: good enough to build on

- Core movement slice
- Mission manager and flag progression
- Save/load base path
- HUD/minimap/dialogue shell
- Plateau-aware bootstrap seam
- Basic melee hitbox resolution

### Yellow: usable but not authoring-grade

- Area transitions
- NPC staging
- Encounter gating
- Dialogue surfacing
- Combat presentation
- Audio routing

### Red: still blocking real vertical-slice authoring

- Real Lantern Heights room graph
- Authored room geometry/landmarks/checkpoints
- Scene-level transition semantics
- Encounter scripting framework for Act I
- Canonical Act I QA/authoring loop on `runGame`

---

## Task 1: Lock The Exact Act I Vertical Slice Target

**Why this must happen first:** without a bounded slice, “engine readiness” expands forever.

**Deliverable:** one canonical Lantern Heights route to build against.

**Must define explicitly:**

- Act I start point:
  - balcony intro
  - hub social route
  - post-social first mission handoff
- Act I end point for the slice:
  - first return from Mistwood-style route
  - first “hub has changed” moment
  - or first strong emotional inversion if you want the slice to go deeper
- Required NPCs:
  - Instructor Tai
  - Merchant Rilu
  - Smith Jenro
  - Samson
  - Sophia
  - Marcel
  - Hazel
  - Veil Maiden if included in slice endpoint
- Required authored spaces:
  - balcony opener
  - main hub
  - route-out traversal path
  - first combat/mission route
  - return state

**Edge cases to decide now:**

- Is the vertical slice “social-first only,” or must it include the first real combat outing?
- Is Mistwood part of the first slice, or is the goal to finish Lantern Heights authoring before the outbound mission area?
- Do you want the slice to include the first signs of NPC withdrawal, or stop before the emotional inversion?

**Acceptance:**

- One sentence defines slice start.
- One sentence defines slice end.
- One list defines mandatory NPCs and rooms.
- One list defines mandatory systems that must be judgeable in this slice.

## Task 2: Replace Placeholder Lantern Heights Geometry With An Authored Room Graph

**Goal:** make Lantern Heights level structure authorable instead of bootstrap-flat.

**Primary files:**

- `java/client/src/main/java/com/shadowascent/client/world/AuthoringWorldBootstrap.java`
- `java/client/src/main/java/com/shadowascent/client/world/AreaPlacementResolver.java`
- `java/client/src/main/java/com/shadowascent/client/world/RunGameContentProfile.java`
- likely new room/area helper classes under `java/client/src/main/java/com/shadowascent/client/world/`
- `data/area_catalog.json`
- possibly new room-spec data file(s) under `data/`

**Required work:**

- Introduce a Lantern Heights room graph model:
  - room id
  - area id
  - room bounds
  - geometry primitives
  - spawn points
  - NPC anchors
  - encounter anchors
  - exit gates
- Separate:
  - “plateau/area selection”
  - from “room geometry assembly”
  - from “room transition rules”
- Make the initial room graph small and deliberate:
  - `area_lantern_heights_balcony`
  - `area_lantern_heights_hub`
  - one outbound route room
  - one first mission/combat room if included

**Edge cases:**

- save/load from room A then continue after data changes
- player spawned inside a wall due to geometry edits
- multiple exits from a room
- re-entering a previously visited room
- authoring a room with no encounter, no NPCs, or no platform tiles beyond floor

**Acceptance:**

- Lantern Heights no longer depends on one generic bootstrap floor plus a few platforms.
- Each room can be iterated independently without changing `HubScreen`.

## Task 3: Make Transitions Authorable, Visible, And State-Aware

**Goal:** transitions become authored room exits/checkpoints, not invisible generic trigger strips.

**Primary files:**

- `java/client/src/main/java/com/shadowascent/client/RunGameAreaTransition.java`
- `java/client/src/main/java/com/shadowascent/client/HubScreen.java`
- `java/client/src/main/java/com/shadowascent/client/ui/MinimapOverlayRenderer.java`
- `java/client/src/main/java/com/shadowascent/client/world/RunGameContentProfile.java`
- transition/room spec data introduced in Task 2

**Required work:**

- Add explicit transition types:
  - free traversal exit
  - mission-gated exit
  - encounter-clear exit
  - dialogue/NPC-triggered handoff exit
  - checkpoint/return exit
- Expose transition metadata to HUD/minimap:
  - target room/area
  - blocked reason
  - active objective relationship
- Add visual/runtime distinction between:
  - “near an exit”
  - “exit is blocked”
  - “exit is unlocked”

**Edge cases:**

- player straddling two nearby gates
- player presses interact while also near NPC/shop gate
- blocked gate after mission rollback or load
- transition unlock depends on both cleared enemies and story flags
- returning from outbound area to hub must not duplicate mission-start state

**Acceptance:**

- Every Act I room exit has an explicit reason it is open or blocked.
- The player can understand where an exit goes before using it.

## Task 4: Upgrade Encounter State From “Enemies Exist” To “Encounter Can Be Authored”

**Goal:** support real Act I combat pacing rather than generic enemy placement.

**Primary files:**

- `java/core/src/main/java/com/shadowascent/core/simulation/GameSimulator.java`
- `java/core/src/main/java/com/shadowascent/core/simulation/SimEnemy.java`
- `java/client/src/main/java/com/shadowascent/client/world/AuthoringWorldBootstrap.java`
- likely new client/core encounter helper(s)
- tests in `java/core/src/test/java/...` and `java/client/src/test/java/...`

**Required work:**

- Introduce encounter identity separate from raw enemy list:
  - encounter id
  - participating enemies
  - clear condition
  - reward/set-flag behavior
  - tied gate(s)
- Support Act I encounter behaviors such as:
  - defeat all enemies
  - defeat elite enemy
  - survive first wave
  - clear encounter before exit opens
- Push encounter completion into event feed/HUD and progression flags.

**Edge cases:**

- enemy falls off map / dies unexpectedly / is removed by load mismatch
- encounter started but player leaves room
- encounter completed, then save/load before reward prompt/flag display
- one enemy remains alive but inactive/stuck
- encounter clear should not fire twice

**Acceptance:**

- First Act I combat encounter can be scripted intentionally.
- Transitions and mission progression can depend on encounter completion, not only enemy presence.

## Task 5: Finish Combat Readiness For Opening-Slice Tuning

**Goal:** combat is honest enough to author and tune early encounters.

**Primary files:**

- `java/core/src/main/java/com/shadowascent/core/simulation/GameSimulator.java`
- `java/core/src/main/java/com/shadowascent/core/simulation/PlayerInputController.java`
- `java/core/src/main/java/com/shadowascent/core/simulation/SimPlayer.java`
- `java/client/src/main/java/com/shadowascent/client/rendering/SpriteWorldRenderer.java`
- `java/client/src/main/java/com/shadowascent/client/HubScreen.java`

**Required work:**

- Validate all opening-combat essentials:
  - attack startup / active / recovery windows
  - enemy hurt reaction visibility
  - enemy death clarity
  - player damage clarity
  - combo queue visibility
  - directionality for grounded opening combat
- Decide whether Act I vertical slice needs:
  - only 2-hit queue
  - or full directional input-driven combo branching
- Ensure first-slice combat does not depend on future mechanics like full aerial combo trees.

**Edge cases:**

- combo queue buffered during hitstop/recovery
- attack while touching NPC/gate/interact zone
- save/load mid-attack already partially handled, but re-verify after new combat state additions
- enemy hurtbox overlap with multiple enemies in small rooms
- blocked/traversal gates near combat should not steal player intent

**Acceptance:**

- Opening encounter can be balanced around what the player can really do.
- QA can tell whether a combat failure is design, tuning, or bug.

## Task 6: Make NPC Staging Scene-Authorable Instead Of Only Area-Authorable

**Goal:** Act I social route can be paced like a designed sequence.

**Primary files:**

- `java/client/src/main/java/com/shadowascent/client/world/AuthoringWorldBootstrap.java`
- `java/client/src/main/java/com/shadowascent/client/HubScreen.java`
- `java/core/src/main/java/com/shadowascent/core/data/GameDataContracts.java`
- `data/npc_registry.json`
- possibly new Act I staging data file(s)

**Required work:**

- Move from “NPC belongs to area” toward “NPC belongs to scene/room state.”
- Support stage variants such as:
  - opening balcony state
  - social hub full roster
  - pre-mission staging
  - return-state reduced roster
- Allow authored per-scene positions, not only generic anchor X values.

**Edge cases:**

- NPC is story-eligible but should not appear in the current room variant
- two NPCs share the same anchor by bad authoring
- mission-giver NPC absent because story state and room state disagree
- save/load after scene-state change

**Acceptance:**

- Lantern Heights social scenes can be staged intentionally.
- The first route no longer feels like the full plateau roster dumped into one room.

## Task 7: Make Dialogue Presentation Good Enough For First-Route Pacing

**Goal:** dialogue in `runGame` is paced and authored enough to support a vertical slice.

**Primary files:**

- `java/client/src/main/java/com/shadowascent/client/RunGameMissionInteraction.java`
- `java/client/src/main/java/com/shadowascent/client/ui/DialogueOverlayRenderer.java`
- `java/client/src/main/java/com/shadowascent/client/HubScreen.java`
- `java/core/src/main/java/com/shadowascent/core/data/GameDataContracts.java`
- `data/dialogue.json`
- `data/narrative_beats.json`

**Required work:**

- Support beat-specific multi-line dialogue sequences for the Act I route.
- Distinguish:
  - incidental NPC line
  - mission handoff line
  - authored scene line
  - return-state line
- Ensure authored line selection respects:
  - active plateau
  - current room/area
  - unresolved beat
  - NPC identity

**Edge cases:**

- speaker has both authored beat line and generic fallback line
- multiple valid beats for same NPC in same area
- player re-talks to an NPC after objective advanced
- dialogue interrupted by save/load or exit
- authored line missing for one NPC in the route

**Acceptance:**

- Lantern Heights conversations feel intentionally sequenced.
- Missing authored lines degrade cleanly to contract fallback, not blank output.

## Task 8: Add Act I Mission Trigger And Objective Authoring Hooks

**Goal:** mission flow can be authored as a route, not inferred from generic mission availability.

**Primary files:**

- `java/client/src/main/java/com/shadowascent/client/RunGameMissionInteraction.java`
- `java/client/src/main/java/com/shadowascent/client/HubScreen.java`
- `java/core/src/main/java/com/shadowascent/core/MissionManager.java`
- `data/quests.json`
- `data/narrative_beats.json`

**Required work:**

- Add explicit scene-aware mission triggers for the slice:
  - social introductions
  - first mission handoff
  - outbound route objective updates
  - return-to-hub objective resolution
- Allow objective updates from:
  - NPC talk
  - gate use
  - room arrival
  - encounter clear

**Edge cases:**

- player talks to NPCs out of expected order
- player returns to hub without completing outbound objective
- player saves between objective substeps
- player reaches an exit early by movement exploit

**Acceptance:**

- The full Act I slice route can be authored without hidden assumptions in code.

## Task 9: Harden Save/Load For Authoring Iteration

**Goal:** content iteration does not become unsafe because runtime state is too transient.

**Primary files:**

- `java/client/src/main/java/com/shadowascent/client/SaveLoad.java`
- `java/client/src/main/java/com/shadowascent/client/ShadowAscentGame.java`
- relevant tests under `java/client/src/test/java/...`

**Required work:**

- Re-verify save/load for all newly authoring-critical state:
  - current room/area
  - transition/gate state
  - encounter-clear state
  - mission stage state
  - transient combat state reset rules
- Decide which state is intentionally persisted and which is intentionally rebuilt.

**Edge cases:**

- saving inside an encounter
- loading after geometry/staging data changed
- loading at a gate that is now blocked
- loading after NPC staging/state changes

**Acceptance:**

- Save/load behavior is predictable enough for real content iteration.

## Task 10: Add Vertical-Slice QA Instrumentation And Failure Visibility

**Goal:** content iteration produces useful debugging evidence instead of vague playtest impressions.

**Primary files:**

- `java/client/src/main/java/com/shadowascent/client/HubScreen.java`
- `java/client/src/main/java/com/shadowascent/client/ui/HudOverlayRenderer.java`
- `docs/ACT_I_QA_ROUTE.md`
- tests as needed

**Required work:**

- Make the runtime expose enough evidence during Act I playtests:
  - current room/area
  - current beat title
  - current objective
  - blocked-gate reason
  - encounter state
- Keep this player-readable, not developer-noisy.

**Edge cases:**

- too much HUD text making route unreadable
- stale beat/objective text after transitions
- conflicting prompts from NPC + gate + encounter state

**Acceptance:**

- A playtester can explain what the game currently expects from them.
- A developer can trace why a route broke.

## Task 11: Author The First Lantern Heights Vertical Slice On Top Of The Runtime

**Goal:** once systems are ready, actually author the slice instead of deferring forever.

**Primary content sources:**

- `data/narrative_beats.json`
- `data/dialogue.json`
- `data/quests.json`
- `data/area_catalog.json`
- GDD:
  - `game_design_document/09_level_and_content_plan/...`
  - `game_design_document/15_character_dossiers/...`
  - relevant donor docs and vertical-slice notes

**Authoring targets:**

- opening balcony
- social route through Lantern Heights
- mission handoff scene
- first outbound route
- first bounded combat/mission objective
- return state with changed NPC staging or world tone

**Edge cases:**

- route too long for first QA loop
- too many NPC interactions before first movement/combat payoff
- vertical slice ends too early to validate mission return loop
- vertical slice ends too late and becomes half of Act I

**Acceptance:**

- The first Act I route is playable, understandable, and tunable in `runGame`.

---

## Readiness Gates Before Serious Act I Authoring Starts

Do not treat Lantern Heights as ready for heavy authoring until these are all true:

- [ ] Lantern Heights room graph exists.
- [ ] Room transitions are authored and visible.
- [ ] At least one encounter can lock and unlock route progression intentionally.
- [ ] NPC staging is room/scene-aware.
- [ ] Authored Act I dialogue lines display in `runGame`.
- [ ] Mission triggers can bind to NPCs, rooms, and encounters.
- [ ] Save/load preserves enough route truth for iteration.
- [ ] QA route can identify current room, beat, objective, and blocked reason.

## Recommended Implementation Order

1. Lock slice target.
2. Build Lantern Heights room graph.
3. Make transitions authored and visible.
4. Upgrade encounter state.
5. Finish combat readiness.
6. Scene-author NPC staging.
7. Dialogue pacing improvements.
8. Mission trigger authoring hooks.
9. Save/load hardening.
10. QA instrumentation.
11. Author the slice itself.

## Design / Preference Questions For You

These decisions materially affect the right implementation order:

1. Should the Act I vertical slice end **before** the first outbound combat mission, **after** the first outbound combat mission, or **after** the first return-to-hub emotional change?
2. For Lantern Heights authoring, do you want to start with:
   - hand-authored room geometry primitives in code/data first,
   - or a stronger push toward a reusable room-spec schema immediately?
3. For early combat, do you want the vertical slice tuned around:
   - a simple grounded 2-hit combo as the canonical baseline,
   - or should I treat directional combo branching as required before serious Act I combat authoring begins?
4. Should first-slice Act I keep `Mistwood` inside the vertical slice scope, or do you want Lantern Heights itself stabilized first and outbound mission content treated as the next tranche?

## Locked Decisions

The following decisions are now confirmed for implementation:

- The first Act I vertical slice ends **after the first return-to-hub emotional change**.
- Lantern Heights authoring starts from a **reusable room-spec schema immediately**, not from one-off hardcoded room primitives.
- Early combat is tuned around the **current bounded 2-hit combo baseline**.
- `Mistwood` is **inside** the first vertical-slice scope.

## Recommendation

My recommendation is:

- end the first vertical slice **after the first outbound mission return**, because that validates the full loop:
  - social hub
  - mission handoff
  - traversal/combat
  - return-state change
- start with a **reusable room-spec schema immediately**, but keep the first implementation tiny and Act-I-specific
- tune early combat around the **current bounded 2-hit combo baseline**, not full branching combos yet
- keep **Mistwood included**, but only as a compact bounded route, not a broad content expansion

That gives you a real slice instead of a lobby demo, without trying to finish half the game before authoring begins.

## Revised Scope Consequences

Because the slice ends after the first return-to-hub emotional change, the vertical slice must now prove all of these in one route:

- opening balcony readability
- full Lantern Heights social introduction pass
- mission handoff clarity
- outbound traversal to Mistwood
- first bounded combat and mission objective resolution
- successful return path
- changed hub/NPC emotional state after return

This means the runtime is **not** ready for serious Act I authoring until it can support:

- authored multi-room hub staging
- at least one outbound area chain beyond the Lantern Heights hub
- return-state room/NPC variation
- mission-state-sensitive dialogue before and after the outing
- save/load continuity across outbound and return phases

It also means the first execution plan after readiness work should target:

1. reusable room-spec schema
2. Lantern Heights authored room graph
3. Mistwood outbound route rooms
4. first encounter/mission return loop
5. post-return hub-state emotional change
