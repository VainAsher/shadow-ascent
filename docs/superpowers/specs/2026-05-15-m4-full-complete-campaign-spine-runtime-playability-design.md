---
doc_type: spec
status: draft
owner: core-team
last_updated: 2026-05-15
milestone: M4 Full
---
# M4 Full: Complete Campaign Spine Runtime Playability

## Purpose

Define the closure bar for `M4 Full` so the clean-start repository stops being "contract-complete but runtime-incomplete" for the campaign spine.

The milestone closes when `runGame` becomes the truthful runtime host for all currently contract-authored campaign plateau content:

- the critical path is playable across all staged plateaus,
- each plateau's already-authored optional side content is playable,
- authored beats, NPC staging, dialogue, encounters, and gate logic are surfaced in runtime,
- geometry is strong enough to support meaningful traversal rather than flat room-to-room routing,
- the player can reach the `BEACON_CLIFF` climax and return into a bounded post-climax free-roam state inside existing staged rooms,
- that post-climax state survives save/load.

This is a runtime playability milestone with placeholder presentation allowed. It is not a final-art milestone.

## Context And Corrections

The current repo state matters:

- `M4a` is complete: Act I vertical slice playable readiness for `LANTERN_HEIGHTS` and `MISTWOOD` is already landed.
- `M4b` is active and substantially implemented: authoring diagnostics, zero-Java room addition proof, zero-Java side-beat proof, contract-driven route hints, and authoring-pattern docs already exist.
- The largest remaining M4 gap is not data design. It is runtime staging breadth and fidelity across the unstaged plateau families.

Because of that, `M4 Full` must not treat M4b authoring proof as hypothetical new work. It must treat M4b as a prerequisite truth surface that is finalized and then reused for multi-plateau delivery.

## Milestone Definition

`M4 Full` should be treated as:

**Complete Campaign Spine With Authored Traversal-Ready Runtime Staging**

The milestone is complete only when:

1. `runGame` can carry the campaign through all currently contract-authored plateau families.
2. All already-authored optional content for newly staged plateaus is surfaced and playable, not just the mainline route.
3. Each plateau, taken as a whole, supports meaningful up/down/left/right navigation even if some individual rooms remain simple.
4. The player can move from climax into a bounded epilogue/free-roam state in existing staged rooms.
5. Campaign continuity, including save/load across plateau boundaries and post-climax return state, is stable and test-backed.

## In Scope

- M4b closure truth and its reuse for broader room-spec authoring.
- Runtime staging for:
  - `SUMMIT_SHRINE`
  - `HOLLOW_DEPTHS`
  - `EMBER_MONASTERY`
  - `WINDING_SKYROAD`
  - `MIRROR_SUMMIT`
  - `BEACON_CLIFF`
- Continued support and preservation of already-landed `LANTERN_HEIGHTS` and `MISTWOOD` slices.
- All already-authored beats and optional side quests for the above plateaus, as represented in:
  - [narrative_beats.json](/c:/Users/asher/tester/shadow_ascent_integrated_complete_prototype_package/shadow_ascent_clean_start/data/narrative_beats.json)
  - [quests.json](/c:/Users/asher/tester/shadow_ascent_integrated_complete_prototype_package/shadow_ascent_clean_start/data/quests.json)
  - [dialogue.json](/c:/Users/asher/tester/shadow_ascent_integrated_complete_prototype_package/shadow_ascent_clean_start/data/dialogue.json)
  - [npc_registry.json](/c:/Users/asher/tester/shadow_ascent_integrated_complete_prototype_package/shadow_ascent_clean_start/data/npc_registry.json)
  - [plateaus.json](/c:/Users/asher/tester/shadow_ascent_integrated_complete_prototype_package/shadow_ascent_clean_start/data/plateaus.json)
- Room-spec authoring, plateau routing, NPC anchor placement, beat-trigger surfacing, mission/UI surfacing, encounter gating, and save/load continuity needed to make that content real in `runGame`.
- Small targeted runtime extensions where an authored beat or quest exposes a real gap that cannot be expressed through current room-spec, mission, dialogue, transition, or encounter systems.
- Bounded post-climax free-roam state in existing staged rooms.

## Out Of Scope

- New plateau families or new campaign acts beyond the already-authored contract set.
- New narrative design invented solely for M4 closure.
- Final-art geometry polish, final sprite pass, or cinematic presentation work.
- Large subsystem redesigns not forced by an authored runtime requirement.
- Full M6 elastic opportunity-stream integration as a milestone requirement.
- Broad procgen-port scope for its own sake.

## Plateau Runtime Requirements

Each plateau in scope must be runtime-complete as an authored slice, not merely reachable.

For every staged plateau, `runGame` must provide:

- a bounded authored room graph backed by room-spec data,
- valid entry conditions from the preceding plateau or return state,
- valid exits into the next plateau or bounded return state,
- NPC staging anchored to authored rooms instead of generic fallback injection,
- beat surfacing through room arrival, interaction, encounter, or transition logic,
- optional side content that can start, progress, complete, and survive save/load,
- mission and HUD surfacing that distinguishes mainline from optional content,
- encounter and gate semantics tied to authored state rather than generic broad shortcuts,
- world-state changes after major beats where the contracts imply them.

Plateau closure must be judged plateau-by-plateau, not only by a final campaign smoke.

## Geometry And Traversal Fidelity Requirements

`M4 Full` requires authored room geometry that is good enough to support meaningful play with placeholder presentation.

The fidelity bar is plateau-level, not per-room uniformity:

- some rooms may remain simple hubs, dialogue rooms, or transition spaces,
- not every room needs a traversal puzzle,
- but each plateau as a whole must support meaningful navigation across up, down, left, and right.

Each plateau slice must include, across its room graph:

- at least one meaningful vertical route,
- at least one return or backtrack path where returning changes interpretation or function,
- at least one encounter, objective, or interaction that requires moving through more than one elevation band or subspace,
- transitions that feel spatially motivated rather than like arbitrary edge bands,
- spawn anchors, NPC anchors, and gate placement that reinforce room purpose.

Failure conditions for this milestone include:

- plateau slices that are effectively flat corridors with renamed rooms,
- vertical traversal that is technically possible but never meaningfully required,
- mainline or optional content that only works because invisible triggers bridge shallow geometry,
- optional content placed into spaces with no navigation identity.

## Donor Layout Generation As A Resource

At least one donor repo includes a useful layered procedural room-carving approach. That technique is explicitly in scope as a reference or helper for room-shape authoring.

Boundary:

- the milestone does not require end-to-end procedural generation parity,
- but donor carving logic may be adapted if it materially improves authored room shells, ledges, elevation bands, or reusable layout patterns,
- any such use should serve authored plateau fidelity and authoring velocity rather than expanding procgen scope for its own sake.

## Campaign Continuity And Epilogue Requirements

`M4 Full` does not close when the player merely reaches the climax.

It closes only when:

- the player can reach the `BEACON_CLIFF` climax,
- the climax resolves into a bounded post-climax free-roam state inside existing staged rooms,
- the post-climax state has coherent flags, NPC presence, dialogue changes, and mission truth,
- save/load preserves that post-climax state correctly.

This means one-way demo routing is insufficient. The runtime must support campaign completion truth.

## Testing And Gate Requirements

`M4 Full` needs a stronger gate than M4a or M4b because the risk is cross-plateau drift.

Required test layers:

1. Plateau bootstrap tests
- One runtime bootstrap test per newly staged plateau.
- Must verify room selection, entry routing, NPC staging, beat-triggered state changes, and plateau exits.

2. Plateau optional-content tests
- One focused test surface per newly staged plateau for already-authored optional content.
- Must verify start, progress, completion, and persistence.

3. Traversal and geometry truth tests
- Must verify meaningful authored traversal, including vertical and return paths where authored.
- Transition existence alone is insufficient.

4. Campaign continuity tests
- The existing `Campaign Continuity` regression failure must be fixed, not documented away.
- Save/load continuity must hold across plateau boundaries, pre-boss states, post-boss states, and post-climax epilogue state.

5. End-to-end campaign route test
- A dedicated full-campaign smoke must validate:
  - fresh start,
  - all plateau traversal,
  - critical beat progression,
  - selected optional-content progression,
  - climax,
  - bounded post-climax free-roam state.

6. Authoring diagnostics gate
- The current Act I authoring diagnostics gate must be expanded or generalized so multi-plateau room-spec authoring fails fast on:
  - stale IDs,
  - broken transitions,
  - missing anchors,
  - missing dialogue refs,
  - broken encounter links.

Required closure evidence:

- all plateau bootstrap tests green,
- all plateau optional-content tests green,
- full campaign route smoke green,
- `Campaign Continuity` green,
- diagnostics gate green,
- full regression gate green,
- milestone gate doc updated with explicit evidence.

## Risks

### Authoring / Runtime Mismatch

Some already-authored beats or side quests may assume spatial or encounter semantics that the current runtime still expresses weakly.

Mitigation:

- allow only small targeted runtime extensions,
- tie each extension to a concrete authored runtime requirement,
- require tests and diagnostics for every new extension path.

### Bootstrap Sprawl

`AuthoringWorldBootstrap` may become an unmaintainable plateau-switch monolith if multi-plateau staging is handled entirely through hardcoded branching.

Mitigation:

- prefer routing tables, plateau-local helpers, and more data-driven selection paths,
- avoid indefinite growth of large hardcoded switch ladders.

### Geometry Quality Drift

It is easy to satisfy staging with shallow room graphs that technically route but do not feel intentional.

Mitigation:

- require plateau-level multi-axis traversal evidence,
- treat geometry truth as a milestone requirement, not polish.

### Optional Content Dishonesty

Optional quests may appear in HUD/contracts but remain unplayable or route-breaking.

Mitigation:

- plateau-specific optional-content tests are required,
- optional content is part of closure, not a stretch goal.

### Continuity Fragility

Cross-plateau save/load and post-climax state are likely places for hidden state bugs.

Mitigation:

- continuity is a first-class gate,
- not a final cleanup pass.

### Donor Over-Import

The donor layered room-carving approach is useful, but a wholesale procgen port would create avoidable scope drift.

Mitigation:

- use donor techniques only where they improve authored layout production or reusable shape patterns,
- keep acceptance focused on authored runtime quality.

## Failure Conditions

`M4 Full` must not be promoted complete if any of the following are still true:

- one or more plateau families remain contract-authored but lack runtime room-spec staging,
- plateau-local optional content exists in contracts but is not playable in `runGame`,
- the player can reach the climax but not persist a stable post-climax state,
- geometry remains broadly flat and transition-driven rather than traversal-driven,
- `Campaign Continuity` still fails,
- authoring diagnostics remain Act-I-only in practice and do not protect multi-plateau room growth.

## Milestone Handoff

If `M4 Full` closes successfully, the project should no longer have a campaign-existence problem.

At that point:

- `runGame` is the real campaign host rather than a partial slice host,
- all currently-authored campaign plateau content is runtime-playable,
- room-spec authoring is proven beyond Act I,
- future work can focus on fidelity, scale, authoring acceleration, or M6 opportunity integration instead of basic campaign staging.

The natural next milestone after `M4 Full` is not "make the campaign exist." It is one of:

- deeper campaign fidelity and readability,
- broader authoring acceleration across all plateaus,
- or selective M6 runtime/world opportunity integration on top of a now-complete campaign host.
