# Act I Milestone Stack Design

## Purpose

Define the next two production milestones for `runGame` so they are useful both to a design/production owner and to future implementation agents. The immediate need is not another loose roadmap entry. The project needs two explicit milestone contracts:

1. a hard gate where `runGame` becomes a truthful, playable, testable, and authorable host for the first Act I vertical slice
2. a follow-on milestone that improves authoring speed and iteration safety for Act I without prematurely broadening into the next plateau

This document is the design source for those two milestones.

## Current Context

The project has already crossed the foundational runtime threshold:

- `runGame` is the primary forward QA surface
- the LibGDX path has title, pause, save/load, dialogue, HUD, minimap, inventory/shop/crafting, room transitions, and bounded authored runtime staging
- Lantern Heights and Mistwood now have an authored room-spec path instead of only fixture/bootstrap logic
- the route is becoming readable, but content fidelity is still uneven

The key remaining question is no longer "can the runtime exist?" It is "what exact quality bar must be met before Act I authoring is considered real, and what comes immediately after?"

## Design Goals

- Create milestone definitions that are strict enough to prevent scope drift.
- Preserve the distinction between runtime readiness and content polish.
- Make milestone language usable by both humans and implementation agents.
- Keep placeholder presentation acceptable for Milestone A.
- Keep deeper authoring velocity and readability improvements as Milestone B, not mixed into the completion definition for Milestone A.
- Ensure optional side content inside the first Act I slice is included, not silently deferred.

## Non-Goals

- This spec does not define the full implementation task list. That belongs in plans.
- This spec does not open `EMBER_MONASTERY` or later plateau delivery as the next primary milestone.
- This spec does not redefine combat around a new combo system or directional branching tree.
- This spec does not require final art, final audio mix, or cinematic-quality scene presentation.

## Milestone Structure

Three structural options were considered:

### Option 1: Runtime-First Milestone Pair

- Milestone A: finish a truthful, playable, authorable Act I slice
- Milestone B: harden authoring velocity and fidelity on top of that slice

Pros:

- clean acceptance boundary
- honest QA gate
- easy to reason about ownership and sequencing
- protects against mixing engine-readiness work with later polish

Cons:

- requires discipline to stop Milestone A at placeholder-complete rather than polish-complete

### Option 2: Single Large Act I Completion Milestone With Internal Phases

- one big milestone containing both vertical-slice readiness and later hardening

Pros:

- simpler on paper

Cons:

- weak acceptance boundaries
- easier for work to sprawl
- harder for agents to know what "done" means

### Option 3: Authoring-System-First Pair

- Milestone A centered on tooling/schema velocity before slice completion
- Milestone B centered on player-facing route closure

Pros:

- maximizes infrastructure reuse early

Cons:

- risks building authoring surfaces against an insufficiently honest runtime
- poor fit for current user concern about real playability and fidelity

## Recommended Structure

Use **Option 1: Runtime-First Milestone Pair**.

This is the best fit for the current project state and the stated priorities:

- deeper Act I fidelity before broader expansion
- `runGame` as the primary runtime
- placeholder presentation acceptable for the readiness milestone
- authoring velocity important, but best improved after the slice host is proven truthful

---

## Relationship to Existing Milestones

Milestone A and Milestone B are sub-milestones of **M4 (Campaign Content Scale)** in `docs/NORTH_STAR_EXECUTION_MATRIX.md`.

- **Milestone A = M4a.** M4a closes when `runGame` is a truthful, playable, and authorable host for the first Act I vertical slice.
- **Milestone B = M4b.** M4b closes when that slice can be extended and tuned quickly through the supported authoring path.
- **M4 closes when both M4a and M4b are complete.**

M6 (Open-World Runtime Expansion) continues in parallel and is not blocked by M4a/M4b. HOLLOW_DEPTHS deepening work (ongoing in M6 and the broader M4 track) proceeds independently and does not gate either sub-milestone.

---

## Milestone A

## Name

**Act I Vertical Slice Playable Readiness**

## Core Intent

Make `runGame` a truthful, fully playable, fully testable, and practically authorable host for the first Act I slice, using placeholder presentation where necessary.

This milestone ends after the first return-to-hub emotional change. In contract terms: the slice ends when `beat_npc_withdrawal` fires and flag `npc_withdrawal_started` is set, entering the `area_lantern_heights_hub_dimming` hub variant where shops close early and NPCs begin pulling away. This milestone includes both the critical path and the intended optional side content that lives inside that slice.

## Why This Milestone Exists

The project needs a point where the answer to "is the engine and systems host ready for real vertical-slice authoring?" becomes "yes" for Act I.

Without this milestone, the project risks getting trapped in a misleading middle state:

- technically impressive runtime work
- partially authored rooms
- partially staged NPCs
- partially working side content
- unclear boundary between "system incomplete" and "content incomplete"

Milestone A eliminates that ambiguity.

## In Scope

### Mainline route

The slice must support the full intended first-loop flow:

- Lantern Heights opening
- Lantern Heights social progression
- early mission handoff
- Mistwood outbound traversal
- first bounded critical encounter
- return to Lantern Heights
- first return-hub emotional change

### Optional side-content layer inside the slice

The first slice is not complete if only the critical route works. It must also include the intended playable optional layer inside that same slice. The following content is explicitly in scope, as authored in `data/quests.json`:

**ACT_0 quest steps (LANTERN_HEIGHTS):**

- `samson_q1_unfinished_sparring_match` — training combat challenge; areas: `area_training_dojo`, `elastic_lh_training_loop`
- `sophia_q1_lantern_cartography` — map shard collection; areas: `area_lantern_heights_hub`, `area_mistwood_path`, `elastic_lh_lore_walk`
- `marcel_q1_guard_the_forge` — forge defense wave; areas: `area_training_dojo`, `area_lantern_forge`
- `hazel_q1_gentle_glow` — gathering errand; areas: `area_lantern_heights_hub`, `elastic_lh_social_errand`

**Optional NPC interactions eligible in LANTERN_HEIGHTS:**

- `old_man_riku` — Storykeeper; unlocks lore tablets when brought relics
- `lantern_kid` — early-to-late hub presence; authored slice feel, no quest chain required

All four quest steps must be startable, progressable, saveable, loadable, and completable without corrupting the critical route. Optional NPC interactions must be discoverable and must not block or confuse mainline progression.

### Runtime and authoring obligations

- a validated room-spec schema (`RoomSpec`, `RoomTransitionSpec`, `EncounterSpec`) loaded from `data/room_specs/` at startup, with actionable diagnostics for authoring mistakes; schema drives room selection — no ad hoc bootstrap switches
- authored room graph for the slice driven by that schema
- authored room transitions
- typed encounter gating where relevant
- scene-aware NPC staging
- mission and objective truth for both mainline and optional content
- room-aware or scene-aware dialogue routing
- save/load continuity for route-critical and side-content state
- readable route surfacing in HUD/minimap/event feed
- enough combat truth and feedback to tune the first Mistwood encounter

## Out Of Scope

- final visual art pass
- final audio pass or production mix
- broad plateau expansion
- full combat-system redesign
- generalized content editor
- cinematic presentation polish
- deep optional-content breadth beyond what belongs inside the first Act I slice

## Acceptance Criteria

Milestone A is complete only when all of the following are true:

### Playability

- A tester can complete the intended first-loop Act I route inside `runGame` without external explanation or debug-only shortcuts.
- The optional side content intended to be playable inside that loop can also be started and progressed without destabilizing the mainline.

### Truthfulness

- Mainline progression truth is driven by authored/runtime state, not hidden hand-authored exceptions.
- Optional content is clearly optional and does not masquerade as required progression.
- Mission surfacing distinguishes mainline from optional work clearly enough that route confusion is not caused by the runtime itself.

### Authorability

- A validated room-spec schema (`RoomSpec`, `RoomTransitionSpec`, `EncounterSpec`) exists, is loaded from `data/room_specs/` at startup with actionable diagnostics for authoring mistakes, and drives room selection for the slice. This is a hard criterion: acceptance cannot be satisfied by hardcoded routing with annotated data alongside it.
- The slice can be meaningfully iterated through room/data authoring rather than requiring repeated Java-only rewrites for ordinary content changes.
- Adding or revising a room, NPC staging pattern, or optional conversation/objective inside the slice is a bounded authoring task.

### Testability

The following test classes must exist and pass before Milestone A is signed off:

- `RoomSpecCatalogTest` — loads Lantern Heights and Mistwood specs; rejects duplicate room IDs, unknown transition targets, and missing encounter definitions
- `ActIVerticalSliceBootstrapTest` — proves the correct room is selected for each flag state in the slice
- `RunGameAreaTransitionTest` — typed transition resolution for all five slice transition types
- `ActIRouteStateSmokeTest` — deterministic coverage for the full main route state machine: initial room → social progression → mission start → Mistwood entry → encounter clear → return-state room selection
- `SaveLoadRuntimeStateTest` — route-critical save/load transitions across outbound and return phases

Minimum Gradle gate before sign-off:

```bash
./gradlew :client:test --tests "com.shadowascent.client.world.RoomSpecCatalogTest"
./gradlew :client:test --tests "com.shadowascent.client.world.ActIVerticalSliceBootstrapTest"
./gradlew :client:test --tests "com.shadowascent.client.RunGameAreaTransitionTest"
./gradlew :client:test --tests "com.shadowascent.client.ActIRouteStateSmokeTest"
./gradlew :client:test --tests "com.shadowascent.client.SaveLoadRuntimeStateTest"
./gradlew runRegressionTests
```

### Placeholder-ready presentation

- Placeholder visuals are acceptable, but they must remain readable:
  - room identity must be understandable
  - transition points must be readable
  - mission-critical NPCs must be discoverable
  - blocked gates and encounter state must be interpretable

## Failure Conditions

Milestone A is not complete if any of the following remain true:

- the mainline route works, but the intended optional side content in the slice is still broken or misleading
- the player can reach unintended states that confuse route truth
- save/load can silently corrupt the return-hub change or optional-progress state
- authored room specs exist, but ordinary slice iteration still depends on ad hoc code surgery
- `runGame` still behaves like a prototype validation shell rather than a dependable authoring host

## Edge Cases That Must Be Covered

### Progression

- player attempts optional content before finishing intended mainline conversations
- player ignores optional content entirely and still completes the mainline
- player partially progresses an optional side thread, then returns to the critical route
- player talks to relevant NPCs in a non-ideal order

### State recovery

- save during outbound traversal
- save after optional progress but before Mistwood completion
- save during or immediately after the first Mistwood encounter
- save after return-hub state change but before all changed-scene conversations are exhausted

### Authoring ambiguity

- multiple room or beat candidates satisfy the same progression state
- optional content and mainline content both want HUD prominence at the same time
- room transitions and NPC interaction prompts compete for priority

### QA readability

- tester can tell whether a failure is:
  - content missing
  - state bug
  - route bug
  - encounter not yet cleared
  - optional content being mistaken for required content

## Recommended Evidence

Milestone A acceptance applies to `runGame` only. `PlaytestClient` (the Swing QA harness) is not a substitute for gaps in the LibGDX path.

Milestone A should be signed off with:

- all tests in the Testability section passing via `./gradlew runRegressionTests`
- `docs/ACT_I_QA_ROUTE.md` — canonical Act I QA route aligned to actual room IDs and state transitions; must not claim authoring readiness before `ActIRouteStateSmokeTest` is green
- `docs/MILESTONE_A_GATE.md` — formal sign-off checklist following the `docs/M3_RELEASE_GATE.md` pattern, recording:
  - mainline route completion through `beat_npc_withdrawal` / `npc_withdrawal_started`
  - each of the four Lantern Heights ACT_0 quest steps started and resolved
  - `old_man_riku` and `lantern_kid` interactions confirmed discoverable
  - Mistwood first encounter completion
  - `area_lantern_heights_hub_dimming` return-state hub change confirmed readable in `runGame`

The implementation plan for this milestone is `docs/superpowers/plans/2026-05-14-m4a-act-i-vertical-slice-playable-readiness-implementation.md`.

---

## Milestone B

## Name

**Act I Authoring Velocity And Fidelity Hardening**

## Core Intent

Once `runGame` is a truthful host for the first Act I slice, make that slice faster to extend, safer to modify, and easier to tune. This milestone prioritizes authoring velocity first, with player-facing readability and feel improvements following from better content-authoring surfaces.

## Why This Milestone Exists

A playable vertical slice is not automatically a productive authoring environment.

Without Milestone B, the project risks reaching a weak equilibrium:

- the slice works
- adding content remains slow and brittle
- readability improvements become patchy UI fixes instead of authored structural improvements
- future rooms, optional scenes, and tuning passes stay too expensive

Milestone B exists to turn the first slice from "playable" into "practical to build on."

## In Scope

### Authoring velocity improvements

- strengthen room-spec and related slice-authoring patterns
- reduce the amount of Java surgery needed for ordinary Act I changes
- add validation around common authoring mistakes
- make it easier to add or tune:
  - room variants
  - optional side-content beats
  - scene-state transitions
  - encounter variants
  - NPC staging changes
  - return-loop pacing changes

### Readability and feel improvements that follow from better tooling

- clearer room identity
- clearer gate and route legibility
- clearer mission-critical NPC emphasis
- better dialogue/scene pacing consistency
- stronger encounter-readiness cues
- cleaner optional-vs-mainline presentation

### Safe content expansion inside Act I

Milestone B can broaden the Act I slice internally if the new breadth improves iteration value without changing the milestone family:

- more Mistwood room depth
- richer return-hub sequencing
- stronger optional side-quest pacing
- more reusable scene-state patterns

## Out Of Scope

- next plateau as the primary target
- final art migration
- broad systems redesign unrelated to Act I authoring pain
- standalone editor initiative unless a very small helper surface is clearly the lowest-cost route

## Acceptance Criteria

Milestone B is complete only when all of the following are true:

### Authoring efficiency

- ordinary Act I content changes are primarily data/schema-driven rather than code-driven
- adding a new room or scene variant is a bounded, understandable task
- adding optional side content inside Act I does not require rediscovering fragile runtime rules

### Validation

- common authoring mistakes fail fast with actionable diagnostics
- route ambiguity and content collisions are easier to detect before playtest
- save/load and state-transition risks from content changes are easier to verify

### Readability improvements

- the player-facing route reads more clearly because the authored structure is better, not just because of more HUD text
- side content and mainline content remain distinguishable under growth
- Act I scene changes feel intentionally staged instead of mechanically toggled

### Sustainability

- adding a new room or optional side beat is achievable by editing room-spec JSON alone, with zero Java changes required; this must be demonstrable by a test that loads the modified spec from data without touching runtime code
- the slice becomes a stable production base for additional Act I tuning and density

## Failure Conditions

Milestone B is not complete if:

- the slice technically supports authoring, but iteration is still slow and brittle
- readability problems are still being solved mostly through one-off UI patches
- adding optional content continues to threaten mainline truth
- authoring more rooms or scene variants still requires too much runtime rediscovery

## Edge Cases That Must Be Covered

### Content growth

- multiple optional side threads active in the same social phase
- richer Mistwood room graph with more than one outbound encounter space
- alternate return-hub conversations sharing the same broad area

### Validation and drift

- stale room IDs referenced by dialogue or beats
- transitions targeting removed spawns
- optional content that accidentally hard-blocks the mainline
- scene-state variants that overlap unintentionally

### Iteration pressure

- rapidly retuning room geometry invalidates NPC anchors or transition bands
- adding a new optional beat overloads mission surfacing
- adding a room variant breaks save compatibility assumptions for the slice

## Recommended Evidence

Milestone B should be signed off with:

- a Gradle validation command (added as part of this milestone) demonstrating that common authoring mistakes — stale room IDs, missing NPC anchors, broken transition targets, optional content that hard-blocks the mainline — fail fast with actionable diagnostics
- evidence that a new room or beat was added via JSON alone with zero Java changes, backed by a passing test
- `docs/MILESTONE_B_AUTHORING_PATTERNS.md` — content authoring pattern guide covering: new room, new optional side beat, new room-state variant, new bounded encounter
- `docs/MILESTONE_B_GATE.md` — formal sign-off checklist recording the above evidence and confirming readability improvements derived from stronger authored structure

The Milestone B implementation plan will be created after Milestone A closes, targeting the highest-friction authoring surfaces revealed by the M4a completion work.

---

## Boundary Between The Milestones

This boundary must stay explicit.

### Milestone A answers:

"Can `runGame` honestly host the first Act I slice with both mainline and intended optional content, using placeholder presentation?"

### Milestone B answers:

"Can the team now iterate on that slice quickly, safely, and with improving readability because the authoring path itself is stronger?"

If a piece of work is required to make the first slice truthful, it belongs in Milestone A.
If a piece of work mainly makes the slice easier to extend, safer to modify, or cleaner to tune after truth is established, it belongs in Milestone B.

---

## Recommended Sequencing

1. Finish Milestone A first.
2. Do not widen into the next plateau before Milestone A is signed off.
3. Begin Milestone B by targeting the highest-friction authoring surfaces revealed by Act I completion work.
4. Use Milestone B to stabilize the content-production base before broader campaign expansion resumes.

## Recommended Planning Implications

The Milestone A implementation plan already exists:

`docs/superpowers/plans/2026-05-14-m4a-act-i-vertical-slice-playable-readiness-implementation.md`

It covers all 16 tasks for M4a: room-spec schema, bootstrap replacement, room geometry primitives, typed transitions, NPC staging, dialogue sequencing, mission triggers, HUD/minimap surfacing, combat feedback, save/load hardening, smoke tests, and QA route docs. The pre-flight steps in that plan must be completed before Task 1 begins.

The Milestone B implementation plan will be created after Milestone A closes, targeting the highest-friction authoring surfaces revealed by M4a completion work. Anticipated scope:

- schema and validation upgrades
- reusable authoring patterns
- readability improvements driven by stronger authored structures
- increased Act I content velocity

## Review Questions — Resolved

These questions were open at initial drafting. They are answered here.

**Does Milestone A include enough optional side-content truth?**
Yes. The four ACT_0 quest steps (`samson_q1`, `sophia_q1`, `marcel_q1`, `hazel_q1`) and the two LANTERN_HEIGHTS-eligible optional NPCs (`old_man_riku`, `lantern_kid`) are now named explicitly in scope. Optional content not in that list is out of scope for Milestone A.

**Is the placeholder-presentation boundary strict enough to stop polish creep?**
Yes. The acceptance criteria require only: readable room identity, readable transition points, discoverable mission-critical NPCs, and interpretable gate and encounter state. The existing sprite atlas placeholder presentation in `runGame` already satisfies this bar. Final art, cinematic presentation, and audio mix remain out of scope.

**Is the Milestone A / Milestone B handoff clear enough that agents will not mix them?**
Yes. The boundary section defines it precisely: work that makes the slice truthful belongs in A; work that makes the slice faster to extend or safer to modify after truth is established belongs in B. Agents should use the named implementation plans as execution input, not the GDD or open-ended wish lists.

**Does Milestone B prioritize authoring velocity strongly enough before readability polish broadens?**
Yes. The acceptance criteria for Milestone B lead with authoring efficiency and validation before readability improvements. The sustainability criterion now has a binary-testable form: a new room or beat must be addable via JSON alone with zero Java changes, provable by a test.

**Is the next plateau correctly excluded?**
Yes. EMBER_MONASTERY is excluded from both milestones. HOLLOW_DEPTHS authored area deepening is ongoing in M6 and the broader M4 track and proceeds independently of M4a/M4b — it does not gate either sub-milestone and is not in scope for the first Act I vertical slice.

## Recommended Decision

Adopt this milestone stack as the canonical near-term structure:

- **Milestone A:** Act I Vertical Slice Playable Readiness
- **Milestone B:** Act I Authoring Velocity And Fidelity Hardening

This is the most defensible sequencing for the current repository state, current user priorities, and current `runGame` maturity.
