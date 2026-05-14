# Act I QA Route

## Purpose

Define the first-session quality gate for Act I and make it repeatable as a regression route.

## Scope

This is a **quality gate**, not the product boundary.

- validates opening hub readability,
- validates NPC interaction and mission progression flow,
- validates hub state changes,
- validates save/load continuity.

## Current Reality

The route is runnable with objective-driven mission completion. Hub transitions are progression-bound, save/load uses a versioned envelope with legacy compatibility, and session evidence logs are now captured per run. Historical movement-feel sign-off was captured in `runPlayableClient`, but forward route validation should now be executed in `runGame` unless a task explicitly targets the legacy Swing surface.

## Approved Evidence Route

1. Launch the game.
2. Confirm opening hub state is readable (`VIBRANT`) and key NPCs are present.
3. Start the first social mission path.
4. Progress mission objectives through NPC interactions.
5. Trigger veil-influence transition and confirm hub/NPC changes.
6. Continue toward next mission availability.
7. Save state.
8. Reload and verify route-critical progress persists.

## Pass Criteria

- no crash, softlock, or dead-end,
- mission progression is understandable,
- hub transition effects are visible,
- save/load preserves route-critical story state.

## Evidence Artifacts

- Every playable-client session writes a reviewable log to `logs/playtest/playtest_session_*.log`.
- Logs include timestamped events (mission start/progress/completion, ability unlocks, triggers, encounters, save/load), periodic runtime snapshots, and a final `MOVEMENT_SIGNOFF` summary line.
- QA sign-off should attach the session log file path together with manual notes.

## Movement Feel Sign-Off Procedure (Pending Manual Closure)

1. Launch `runPlayableClient` and complete a 2-5 minute traversal-heavy route (run, jump, wall-jump, dash, recovery after damage).
2. Exit the client cleanly so the `MOVEMENT_SIGNOFF` line is written at session end.
3. Extract the line from the latest log:

```powershell
Get-ChildItem logs/playtest/playtest_session_*.log |
  Sort-Object LastWriteTime -Descending |
  Select-Object -First 1 |
  Get-Content |
  Select-String "MOVEMENT_SIGNOFF"
```

1. Record subjective notes against donor feel (acceleration/readability/control confidence) and attach the extracted metrics line.

Manual sign-off remains open until this note is captured and reviewed.

### Captured Evidence (2026-05-07)

- Session log captured: `logs/playtest/playtest_session_20260507_101414.log`.
- `MOVEMENT_SIGNOFF` captured at `2026-05-07T10:23:48.080Z` with:
  - `session_seconds=572.40`, `jumps_ground=67`, `jumps_wall=13`, `jumps_double=32`,
  - `dashes=0`, `wall_exhaust_events=0`, `damage_events=7`, `deaths=2`.
- Follow-up session captured: `logs/playtest/playtest_session_20260507_123016.log`.
- `MOVEMENT_SIGNOFF` captured at `2026-05-07T12:31:58.258Z` with:
  - `session_seconds=101.59`, `jumps_ground=21`, `jumps_wall=7`, `jumps_double=10`,
  - `dashes=28`, `wall_exhaust_events=1`, `damage_events=0`, `deaths=0`.
- Assessment: dash activation now records consistently in telemetry; final manual sign-off remains open pending confirmation of post-tuning dash-range/control feel in the next pass.

### Final Movement Sign-Off (2026-05-07 — Post-Tuning Pass)

**Verdict:** CONDITIONAL PASS — dash range acceptable; two issues identified and fixed before closure.

**Session log:** `logs/playtest/playtest_session_20260507_175143.log`

**MOVEMENT_SIGNOFF:** `session_seconds=1140.37 total_distance=246128.0 jumps_ground=245 jumps_wall=89 jumps_double=120 dashes=282 wall_exhaust_events=16 damage_events=0 deaths=0 peak_abs_vx=14.72`

**Observations:**

- Dash range: acceptable — burst covers traversal gaps without feeling floaty or too short.
- Direction lock (SHIFT): on edge cases in Hollow Depths, pressing SHIFT to dash while holding A/D caused direction to pin after the dash ended. Holding opposite direction or wall-jumping was needed to break free. Root cause: Java keystroke release events with SHIFT modifier were not matched by `bindHold` release bindings (modifier mismatch silently dropped the A/D release). **Fixed** — `bindHold` now registers SHIFT-masked press/release variants.
- Cooldown: far too short at 0.45s — 282 dashes in a 19-minute session confirmed spammable. Dash should represent an energy expenditure with a meaningful recovery gap. **Fixed** — `DASH_COOLDOWN` raised to 1.0s in `PhysicsConstants`.
- C key: no direction-lock issues observed. SHIFT-specific to the modifier-key binding gap.

**Gate status:** Both issues resolved. Regression gate passes. M1 movement sign-off **CLOSED** — 2026-05-07.

## Regression Gate Checklist

- [x] data contracts load validly before route execution,
- [x] mission objectives progress without manual state shortcuts,
- [x] hub state updates align with progression flags,
- [x] save/load roundtrip preserves flags and active mission context.

## Out of Scope for This Gate

- final content polish,
- late-game balance,
- post-Act I advanced systems.
