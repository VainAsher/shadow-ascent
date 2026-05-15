# Shadow Ascent — Stale Doc Audit Log

## Audit 2026-05-14

### Verdict
PARTIAL

### Key Findings

1. **CLAUDE.md** — **STALE**: M3 milestone listed as `Active — V3 save envelope + checksum guard done; full exit criteria TBD`. All three corroborating sources (`docs/CURRENT_STATE.md`, `docs/ROADMAP.md`, `docs/M3_RELEASE_GATE.md`) confirm M3 closed 2026-05-08. This is the canonical context file — the mismatch is highest severity.

2. **docs/CURRENT_STATE.md** — **Accurate**: Milestone statuses, class references, and verification evidence all consistent with code. `last_updated: 2026-05-09`; P1 LibGDX wiring commits (ee6010b, df9f762) updated this file. No staleness detected.

3. **docs/ROADMAP.md** — **STALE** (two items):
   - "Next open items" P1 entry says `StubWorldRenderer` + `GameInputProcessor` wiring is still pending, but both files exist at `java/client/src/main/java/com/shadowascent/client/rendering/StubWorldRenderer.java` and `java/client/src/main/java/com/shadowascent/client/input/GameInputProcessor.java`.
   - M6 "Delivered" section records "Regression harness: 49/49 PASS" but current verified state is 53/53 (confirmed in `CURRENT_STATE.md` verification evidence and backlog).

4. **docs/MIGRATION_MAP.md** — **STALE** (one row): Wave 4 table line 81 has a `SimPlayer.java` row with status `queued` targeting `java/core/.../sim/SimPlayer.java`. A later row (line 91) correctly shows the same file as `done` targeting `java/core/.../simulation/SimPlayer.java`. The first row was not updated when the import completed. `SimPlayer.java` exists at `core/simulation/SimPlayer.java`.

5. **docs/IMPLEMENTATION_BACKLOG.md** — **STALE** (two section headers):
   - Section 7 header: "M5 Systemic World Simulation Foundation (active)" — M5 is complete as of 2026-05-07 per CLAUDE.md and ROADMAP.
   - Section 8 header: "M6 Open-World Runtime Expansion (queued)" — M6 is active per CLAUDE.md and ROADMAP. All tasks inside section 8 are already checked `[x]`.

6. **docs/guides/DEVELOPER_WORKFLOW.md** — **STALE** (two items):
   - Line 215: `RegressionTest.java (~100KB, 49 tests)` — current regression harness runs 53 sections (per `CURRENT_STATE.md` and backlog evidence).
   - "PlaytestClient.java QA harness" notes list Wave 4/5 extractions as `CombatSubsystem, TraversalSubsystem, UISubsystem, MinimapRenderer` but omits three Wave 5 phase-2 extractions completed 2026-05-09: `HudRenderer`, `StoryManager`, `MissionUiCoordinator`.

7. **Missing references** — None: all class names, Gradle task names (`runRegressionTests`, `runDataContractDiagnostics`, `runWorldgenDiagnostics`, `runWorldSimulationDiagnostics`, `runRegionalStreamingDiagnostics`, `runPlayableClient`, `runGame`), and data files referenced in docs were found in the repository. Referenced docs (`M3_RELEASE_GATE.md`, `ACT_I_QA_ROUTE.md`, `NORTH_STAR_EXECUTION_MATRIX.md`, `guides/CODEX_CLI_SETUP.md`) all exist.

### Recommended Next Step
Update `CLAUDE.md` milestone table: change M3 from `Active — V3 save envelope + checksum guard done; full exit criteria TBD` to `Complete (2026-05-08)` — this is the canonical context file read on every session start, making it the highest-impact staleness fix.

---

## Audit 2026-05-15

### Verdict
PARTIAL

### Key Findings

1. **CLAUDE.md** — **STALE (persistent, 2nd cycle)**: M3 milestone still reads `Active — V3 save envelope + checksum guard done; full exit criteria TBD`. Flagged in 2026-05-14 audit; not fixed. All corroborating sources confirm M3 closed 2026-05-08. Highest-priority fix (canonical context loaded every session).

2. **docs/CURRENT_STATE.md** — **STALE (new)**: Three new P1 client classes extracted from `PlaytestClient` in commit `df9f762` — `InputHandler.java`, `RoomGeometry.java`, `SaveLoad.java` — are not documented. `last_updated` header is 2026-05-09; these additions postdate that. All three files confirmed present under `java/client/src/main/java/com/shadowascent/client/`.

3. **docs/ROADMAP.md** — **STALE (three items)**:
   - M4 section header uses `active` label but CLAUDE.md, CURRENT_STATE.md, and the backlog `[AUTHORING DECISION REQUIRED]` block all classify M4 as queued/blocked. No M4 content work has started.
   - "Next open items" P1 entry still says `StubWorldRenderer` + `GameInputProcessor` wiring is pending; both files exist (persistent from 2026-05-14 audit).
   - M6 "Delivered" section still records "Regression harness: 49/49 PASS"; current state is 53 sections (persistent from 2026-05-14 audit).

4. **docs/MIGRATION_MAP.md** — **STALE (two items)**:
   - Wave 4 line 81: `SimPlayer.java` row still has status `queued` targeting `java/core/.../sim/SimPlayer.java`; duplicate of the correct `done` row at line 91 (persistent from 2026-05-14 audit).
   - Wave 5 table missing three P1 client classes: `InputHandler.java`, `RoomGeometry.java`, `SaveLoad.java` (new — added in commit `df9f762`).

5. **docs/IMPLEMENTATION_BACKLOG.md** — **STALE (two section headers, persistent)**:
   - Section 7 header: "M5 Systemic World Simulation Foundation (active)" — M5 complete since 2026-05-07.
   - Section 8 header: "M6 Open-World Runtime Expansion (queued)" — M6 is active; all tasks inside are `[x]`.

6. **docs/guides/DEVELOPER_WORKFLOW.md** — **STALE (two items, persistent)**:
   - Lines 215–216 and 281: `RegressionTest.java (~100KB, 49 tests)` and extraction list `CombatSubsystem, TraversalSubsystem, UISubsystem, MinimapRenderer` omit the three Wave 5 phase-2 extractions (`HudRenderer`, `StoryManager`, `MissionUiCoordinator`) completed 2026-05-09 and the current regression count of 53 sections.

7. **Missing references** — None: all Gradle task names (`runRegressionTests`, `runDataContractDiagnostics`, `runWorldgenDiagnostics`, `runWorldSimulationDiagnostics`, `runRegionalStreamingDiagnostics`, `runPlayableClient`, `runGame`) present in `build.gradle.kts`. All referenced class names found. All data files found. `M3_RELEASE_GATE.md`, `ACT_I_QA_ROUTE.md`, `NORTH_STAR_EXECUTION_MATRIX.md`, `guides/CODEX_CLI_SETUP.md` all exist.

### Recommended Next Step
Fix `CLAUDE.md` milestone table now — M3 has been stale for two consecutive audit cycles. Change `Active — V3 save envelope + checksum guard done; full exit criteria TBD` to `Complete (2026-05-08)` and update the `as of` date from 2026-05-08 to 2026-05-15. This is the canonical context file and the mismatch is compounding.
