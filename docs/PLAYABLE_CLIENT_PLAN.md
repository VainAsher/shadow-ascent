---
doc_type: playable_client_plan
status: living
owner: core-team
last_updated: 2026-05-08
version_anchor: 0.0.1
---
# Playable Client Plan

## Goal

Deliver a human-playable client that supports reliable Act I playtests and preserves donor-quality movement feel.

Decision lock: player feel is measured against `indie-ninja-adventures` movement/physics behavior, even when architecture is refactored.

## Current Baseline

- `PlaytestClient` exists as an MVP interactive surface.
- It validates mission/hub progression with keyboard interaction.
- Input capture now uses Swing key bindings (`WHEN_IN_FOCUSED_WINDOW`) to avoid focus-related static controls.
- Movement now includes donor-profile-derived compact-space tuning (scaled run/jump/wall-jump and short-burst dash) with optional precision walk (`ALT`) for tighter platforming.
- Initial jump/coyote/wall-jump controller slice is integrated (including gravity + wall-slide clamp).
- Double-jump and wall-jump input-lock timing are integrated.
- Wall stamina/exhaustion behavior slice is integrated.
- Deterministic controller regression evidence is integrated (`runRegressionTests` checks run/jump/double-jump/coyote/dash/wall exhaustion behavior).
- Deterministic tolerance calibration is now tightened against compact-space baseline measurements (`runDistance ~172.8`, jump/double-jump/coyote `vy ~ -12.36`, dash `vx ~14.72`).
- Collision-complexity traversal layout is now integrated in the playable client through chunked collision lookup (`SpatialHash` + authored blocker/platform geometry).
- Dynamic traversal extensions are now integrated: moving platforms (including grounded carry) and ability-gated blockers that unlock via story-earned abilities.
- Minimap overlay is now integrated (`M` toggle) to support route readability during playtests.
- Mission/HUD feedback has been expanded with objective-progress bars, next-objective hints, ability unlock surfacing, gate state summaries, and mission-feed toasts.
- First authored multi-room traversal topology is now integrated with camera-follow across hub, forge, shaft, and summit traversal spaces.
- Ability-execution interactions are now integrated (dash-pass sigils and interact altars) to unlock route platforms and drive compatible objective progression.
- First combat encounter hooks are now integrated (attack input, encounter activation/clear loops, combat-seal blockers, and objective-progression hooks).
- Session evidence artifacts are now persisted per launch to `logs/playtest/playtest_session_*.log` (timestamped events + periodic snapshots for QA review).
- Session evidence now emits a final `MOVEMENT_SIGNOFF` metrics envelope (distance/speed/jump/dash/wall/death counters) to support manual donor-feel sign-off notes.
- Dash activation reliability hardening is integrated (`SHIFT` + `C` trigger paths) with explicit runtime feedback lines and reduced control-lock duration.
- Telegraphed encounter timing windows are now integrated (telegraph/vulnerable/recover) and attacks only count during vulnerable states.
- Full donor-physics parity sign-off complete (2026-05-07): dash direction-lock bug fixed (SHIFT modifier key binding gap), dash cooldown raised to 1.0s. Evidence in `docs/ACT_I_QA_ROUTE.md`.
- `CombatSubsystem` extracted from `PlaytestClient` (2026-05-07): `CombatEncounterPhase`, `EncounterPattern`, `CombatEncounter`, `CombatSubsystem` as standalone package-level types; `PlaytestClient` fully delegates.
- `TraversalSubsystem` extracted from `PlaytestClient` (2026-05-08): `TriggerMode`, `MovingPlatform`, `AbilityGate`, `AbilityTrigger`, `CombatBarrier`, `TriggeredPlatform`, `TraversalSubsystem` as standalone package-level types; `PlaytestClient` inner class declarations deleted, all delegation wired; 25/25 regression tests pass. Codex CLI final gate pending. Next decomposition target: `UISubsystem` (HUD/rendering).

## Phase Plan

### Phase P0 - MVP Interaction Surface (done)

- Windowed runtime loop
- Keyboard movement + NPC interaction
- Mission start/progress hooks
- Save/load quick hooks

### Phase P1 - Donor Mechanics Profile Import (active)

Source: `indie-ninja-adventures`

First imports:

1. `physics/PhysicsConstants.java` -> done (`core.physics.PhysicsConstants`)
2. `physics/PhysicsState.java` -> done (`core.physics.PhysicsState`)
3. `GameSimulator.applyPlayerInput` movement subset (bounded adapter) -> partial (run + dash + jump/coyote/wall-jump + double-jump + wall-jump lock + stamina/exhaustion integrated; deterministic baseline calibration complete, manual feel sign-off pending)
4. Collision/hash starter slice -> done (`core.physics.TileType`, `TileRect`, `SpatialHash`) and consumed by playable-client traversal geometry.

Target outcomes:

- jump/dash/coyote-time feel parity for Act I traversal,
- deterministic controller behavior suitable for regression checks,
- no monolithic `GameSimulator` copy.

### Phase P2 - Collision/Traversal Fidelity (active)

- Import bounded collision slices required for grounded feel. (starter slice + moving-platform/gate slice landed)
- Add movement regression tests (jump arc, dash distance, coyote window). (done)
- Validate against donor baseline tolerances. (deterministic baseline complete; subjective manual feel sign-off pending)
- Expand from bounded arena geometry to first authored multi-room traversal route with denser constraints. (done)
- Add first authored combat encounter geometry + trigger loop to validate traversal/combat pacing interplay. (done)
- Expand encounter behavior to telegraphed patterns with counter-play windows. (done)
- Add player consequence loop for encounter mistakes (damage/fail-state/recovery) and verify reset behavior in playtests. (done)

### Phase P3 - Playtest Readiness Gate (queued)

- Define human playtest checklist for Act I route.
- Capture repeatable evidence logs from playable client sessions.
- Validate mission/HUD feedback readability and gate messaging in manual route passes.
- Validate ability-trigger discoverability and abuse cases (repeat activation, missing-ability messaging, save/load persistence).
- Promote M1 sign-off once route is playable and repeatable without scripted shortcuts.

## Non-Goals (for this lane)

- final rendering stack parity,
- full combat system parity,
- asset-polish parity.

## Commands

```bash
./gradlew :client:compileJava
./gradlew runPlayableClient
./gradlew runRegressionTests
```
