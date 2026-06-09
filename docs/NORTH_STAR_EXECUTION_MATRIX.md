---
doc_type: milestone_matrix
status: living
owner: core-team
last_updated: 2026-05-15
version_anchor: 0.0.1
---
# North-Star to Executable Milestone Matrix

## Purpose

Translate the long-horizon systemic RPG vision into bounded, testable delivery slices.

Execution rule: no milestone is promoted without passing its acceptance tests and recording evidence artifacts.

## Milestone Matrix

| Milestone | North-Star Capability | Executable Slice | Acceptance Tests | Evidence Artifacts |
|---|---|---|---|---|
| M1 (`completed`) | Human-playable authored route with readable mission/combat feedback | Act I QA route with traversal, telegraphed encounter windows, and consequence loop | `./gradlew runRegressionTests`; manual route completion with persisted session logs | `logs/playtest/playtest_session_*.log` + QA notes in `docs/ACT_I_QA_ROUTE.md` |
| M2 (`completed`) | Narrative truth and progression integrity from contracts | Mission/side-quest/hub runtime decisions are contract-driven | `./gradlew runDataContractDiagnostics`; regression checks for mission template loading, side-quest gating, and hub progression | Diagnostics output snapshots + regression pass logs |
| M3 (`completed`) | Persistence continuity under evolving schemas | Versioned saves + backward compatibility + forward policy guards + checksum integrity | `./gradlew runRegressionTests` save compatibility suite (`legacy`, `v1`, `v2`, `v3`, unsupported future versions); `testSaveChecksumGuard` | `docs/M3_RELEASE_GATE.md` (12-item checklist, all green, 2026-05-08) |
| M4 (`completed`) | Complete campaign spine runtime playability with authored traversal-ready plateau staging | All seven plateau families room-spec staged in `runGame`, plateau-local optional content surfaced, continuity preserved through bounded post-climax free-roam | `./gradlew runAuthoringDiagnostics`; `./gradlew runRegressionTests`; plateau/bootstrap/route closure suite | `docs/MILESTONE_GATE_M4_FULL.md` |
| └─ M4a (`completed`) | Truthful, playable, authorable Act I vertical slice in `runGame` | Room-spec schema + Lantern Heights / Mistwood room graph + typed transitions + optional side quests (samson/sophia/marcel/hazel q1) + save/load + smoke coverage through `beat_npc_withdrawal` | `RoomSpecCatalogTest`, `ActIVerticalSliceBootstrapTest`, `RunGameAreaTransitionTest`, `ActIRouteStateSmokeTest`, `SaveLoadRuntimeStateTest`; `./gradlew runRegressionTests` | `docs/MILESTONE_A_GATE.md` + `docs/ACT_I_QA_ROUTE.md` |
| └─ M4b (`completed`) | Act I authoring velocity and fidelity hardening | New room or beat addable via JSON alone with zero Java changes, provable by test; content-authoring diagnostics; readability improvements from stronger authored structure | `./gradlew runActIAuthoringDiagnostics`; zero-Java proof tests | `docs/MILESTONE_B_GATE.md` + `docs/MILESTONE_B_AUTHORING_PATTERNS.md` |
| M5 (`completed`) | Systemic world simulation foundation | Contract-backed `world_state`/`faction_state`/`settlement_state` + deterministic tick loop + quest-cause signals | `./gradlew runWorldSimulationDiagnostics`; `./gradlew runRegressionTests` (includes M5 tick determinism + quest signal traceability) | Simulation diagnostics report + deterministic replay logs |
| M6 (`active`) | Open-world runtime expansion with mutation and cooperative validation | Regional streaming constraints + mutation overlays + persistence + co-op session scaffolding | `testRegionalStreamingConstraints`; `testMutationPersistenceRoundTrip`; `testCoopTraversalRecoveryValidation`; `testRegionTransitionNeighborhoodReload`; `testSaveV3OverlayPersistence`; `testCoopSessionScaffolding` | Constraint reports + mutation save/load replay logs + co-op validation report; all in `RegressionTest.java` |

## Acceptance Test Definitions

1. `M1_ROUTE_REPLAYABLE`
- Requirement: first-session route can be completed without scripted shortcuts and replayed.
- Pass condition: deterministic regressions pass and at least one manual playtest log records full route progression.

2. `M2_CONTRACT_AUTHORITY`
- Requirement: story/mission/hub decisions resolve from contracts rather than ad-hoc hardcoding.
- Pass condition: contract diagnostics valid and regression suite shows contract-backed mission/hub behavior.

3. `M3_SAVE_COMPATIBILITY`
- Requirement: save schema changes do not break existing player progress.
- Pass condition: compatibility tests pass for legacy + supported versions; unsupported future versions fail safely.

4. `M4_CONTENT_VALIDITY`
- Requirement: the authored campaign spine is runtime-playable and continuity-safe across all staged plateaus.
- Pass condition: `runAuthoringDiagnostics`, the plateau/bootstrap/route closure suite, and `runRegressionTests` all pass; `docs/MILESTONE_GATE_M4_FULL.md` is green.

4a. `M4A_VERTICAL_SLICE_READY`
- Requirement: `runGame` is a truthful, playable, and authorable host for the first Act I vertical slice through `beat_npc_withdrawal`.
- Pass condition: `RoomSpecCatalogTest`, `ActIVerticalSliceBootstrapTest`, `RunGameAreaTransitionTest`, `ActIRouteStateSmokeTest`, and `SaveLoadRuntimeStateTest` all pass; `docs/MILESTONE_A_GATE.md` checklist is green; `docs/ACT_I_QA_ROUTE.md` exists and is aligned to actual room IDs.

4b. `M4B_AUTHORING_VELOCITY`
- Requirement: Act I content can be extended and tuned through the supported authoring path with zero Java changes for ordinary room or beat additions.
- Pass condition: a new room or beat is demonstrably addable via JSON alone, proved by a passing test; content-authoring mistake diagnostics exist and fail fast; `docs/MILESTONE_B_GATE.md` checklist is green.

5. `M5_SIM_DETERMINISM`
- Requirement: world simulation is reproducible and explainable.
- Pass condition: same seed produces identical simulation event stream; quest opportunities include traceable source signals.

6. `M6_STREAMING_AND_MUTATION_SAFETY`
- Requirement: expanded regional runtime avoids softlocks and preserves state across saves.
- Pass condition: streaming constraints pass accessibility/connectivity checks, mutation state survives save/load, co-op recovery checks pass in harness scenarios.
- Harness methods (all in `RegressionTest.java`): `testRegionalStreamingConstraints`, `testMutationPersistenceRoundTrip`, `testCoopTraversalRecoveryValidation`, `testRegionTransitionNeighborhoodReload`, `testSaveV3OverlayPersistence`, `testCoopSessionScaffolding`.

## Scope Guardrails

- M5/M6 do not bypass M1-M4 quality gates.
- Co-op in M6 is validation-harness scope unless explicitly promoted in product scope docs.
- No monolith donor imports; all expansion work remains slice-based and test-backed.
