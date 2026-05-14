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
