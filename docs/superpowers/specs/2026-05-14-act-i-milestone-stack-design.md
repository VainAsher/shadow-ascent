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

## Milestone A

## Name

**Act I Vertical Slice Playable Readiness**

## Core Intent

Make `runGame` a truthful, fully playable, fully testable, and practically authorable host for the first Act I slice, using placeholder presentation where necessary.

This milestone ends after the first return-to-hub emotional change and includes both the critical path and the intended optional side content that lives inside that slice.

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

The first slice is not complete if only the critical route works. It must also include the intended playable optional layer inside that same slice:

- early optional villager or side-quest interactions
- bounded optional objectives that are meant to coexist with the first route
- optional content that can be started, progressed, saved, loaded, and completed without corrupting the critical route

### Runtime and authoring obligations

- authored room graph for the slice
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

- The slice can be meaningfully iterated through room/data authoring rather than requiring repeated Java-only rewrites for ordinary content changes.
- Adding or revising a room, NPC staging pattern, or optional conversation/objective inside the slice is a bounded authoring task.

### Testability

- There is deterministic smoke coverage for the main route skeleton.
- Route-critical save/load transitions are covered.
- Encounter-gated transitions and return-state hub selection are verifiable without manual guesswork.

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

Milestone A should be signed off with:

- passing route smoke coverage
- passing save/load continuity checks for the slice
- a canonical Act I QA route document aligned to actual room IDs and state transitions
- a short manual sign-off record demonstrating:
  - mainline completion
  - optional side-content functionality
  - Mistwood encounter completion
  - return-hub state change readability

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

- future Act I content work can proceed faster than it could at the Milestone A boundary
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

- evidence that new Act I content can be added or modified through the supported authoring path with bounded code changes
- updated validation coverage for content-authoring failure cases
- a documented content-authoring pattern for:
  - new room
  - new optional side beat
  - new room-state variant
  - new bounded encounter
- proof that readability improved as a consequence of better authored structure

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

The next implementation planning wave should likely split into:

1. a Milestone A completion plan:
   - route truth
   - optional side-content truth
   - save/load stability
   - encounter and transition clarity
   - room/dialogue/mission authoring completeness for the first slice

2. a Milestone B hardening plan:
   - schema and validation upgrades
   - reusable authoring patterns
   - readability improvements driven by stronger authored structures
   - increased Act I content velocity

## Review Questions

Before this milestone stack is treated as locked, the reviewer should confirm:

- Does Milestone A include enough optional side-content truth, or is any intended early side content still being treated as "later" by accident?
- Is the placeholder-presentation boundary strict enough to stop polish creep?
- Is the Milestone A / Milestone B handoff clear enough that agents will not mix them?
- Does Milestone B prioritize authoring velocity strongly enough before readability polish broadens?
- Is the next plateau correctly excluded from the immediate milestone pair?

## Recommended Decision

Adopt this milestone stack as the canonical near-term structure:

- **Milestone A:** Act I Vertical Slice Playable Readiness
- **Milestone B:** Act I Authoring Velocity And Fidelity Hardening

This is the most defensible sequencing for the current repository state, current user priorities, and current `runGame` maturity.
