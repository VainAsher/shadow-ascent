---
doc_type: gate
status: complete
owner: core-team
last_updated: 2026-05-14
milestone: M4b - Act I Authoring Velocity And Fidelity Hardening
---
# Milestone B Gate

## Purpose

Track the closure evidence for M4b: Act I authoring velocity and fidelity hardening on the `runGame` path.

## Current Status

M4b is complete. The authoring-velocity proof, diagnostics, contract-driven readability surfaces, and content-growth hardening are all in place, and the broader regression tranche is green on the current branch state.

## Exit Criteria

### Diagnostics

- [x] `runActIAuthoringDiagnostics` exists and exits non-zero on authoring failures.
- [x] Production Act I room-spec and contract data pass `runActIAuthoringDiagnostics`.
- [x] Diagnostics name the exact room, transition, NPC, beat, or dialogue line causing failures.

### Zero-Java Authoring Proofs

- [x] A new room can be added through JSON alone and selected by runtime flag predicates.
- [x] A new side beat can be added through `data/quests.json` and started from NPC interaction with no new mission switch branch.
- [x] Room-state variant precedence is documented and test-backed.

### Readability From Authored Metadata

- [x] Mainline route hints come from quest-contract metadata rather than a hardcoded `HubScreen` mission switch.
- [x] Optional side-beat route hints come from quest-step metadata.
- [x] Mainline-vs-optional mission classification comes from contract metadata.

### Content-Growth Hardening

- [x] Mistwood has at least one deeper authored post-clear room using existing transition and encounter semantics.
- [x] Save/load tests cover content-growth room IDs and fixture side-beat mission state.

### Evidence Tests

- [x] `RoomSpecCatalogTest`
- [x] `ActIAuthoringFixtureRoundTripTest`
- [x] `ActIAuthoringDiagnosticsTest`
- [x] `ActIVerticalSliceBootstrapTest`
- [x] `RunGameAreaTransitionTest`
- [x] `RunGameMissionInteractionTest`
- [x] `ActIOptionalQuestFlowTest`
- [x] `SaveLoadRuntimeStateTest`
- [x] `HudOverlayStateTest`

## Closure Evidence

- [x] Broader regression tranche re-run with no new failures introduced by the M4b surface.
- [x] Remaining Act I authoring friction is now treated as M4 Full or later fidelity work, not as a blocker to M4b closure.
