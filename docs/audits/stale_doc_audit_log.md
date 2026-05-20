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

---

## Audit 2026-05-16

### Verdict
PARTIAL

### Key Findings

1. **docs/CURRENT_STATE.md** — **STALE (persistent from 2026-05-15)**: `last_updated: 2026-05-09`; never updated since P1 client wiring. Three classes from commit `df9f762` (`InputHandler.java`, `RoomGeometry.java`, `SaveLoad.java`) are absent from the "What Is Implemented" section. Additionally `GameClient.java` (present since initial commit) is undocumented — pre-existing minor gap.

2. **docs/ROADMAP.md** — **STALE (3 items, all persistent from 2026-05-15)**:
   - M4 section header uses `` `active` `` but CLAUDE.md and backlog classify M4 as `queued`/blocked on SUMMIT_SHRINE authoring decisions.
   - "Next open items" P1 entry still says `StubWorldRenderer` + `GameInputProcessor` wiring is pending; both files confirmed at `java/client/src/main/java/com/shadowascent/client/rendering/StubWorldRenderer.java` and `.../client/input/GameInputProcessor.java`.
   - M6 "Delivered" section records "Regression harness: 49/49 PASS"; current harness runs 53 sections per `CURRENT_STATE.md` verification evidence.

3. **docs/MIGRATION_MAP.md** — **STALE (2 items, persistent from 2026-05-14)**:
   - Wave 4 row (~line 81): `SimPlayer.java` status `queued` targeting `core/.../sim/SimPlayer.java` — orphan row never removed when import completed; correct `done` row at ~line 91 targets `core/.../simulation/SimPlayer.java`.
   - Wave 5 table missing entries for `InputHandler.java`, `RoomGeometry.java`, `SaveLoad.java` added in commit `df9f762`.

4. **docs/IMPLEMENTATION_BACKLOG.md** — **STALE (2 section headers, persistent from 2026-05-14)**:
   - Section 7 header: "M5 Systemic World Simulation Foundation (active)" — M5 complete since 2026-05-07.
   - Section 8 header: "M6 Open-World Runtime Expansion (queued)" — M6 is active; all tasks inside are already `[x]`.

5. **docs/guides/DEVELOPER_WORKFLOW.md** — **STALE (2 items, persistent from 2026-05-14)**:
   - Line ~215: `` `RegressionTest.java (~100KB, 49 tests)` `` — current harness runs 53 sections.
   - Wave 4/5 extraction list `(CombatSubsystem, TraversalSubsystem, UISubsystem, MinimapRenderer)` omits three Wave 5 phase-2 extractions completed 2026-05-09: `HudRenderer`, `StoryManager`, `MissionUiCoordinator`.

6. **CLAUDE.md (canonical context)** — **STALE (3rd consecutive cycle)**: M3 milestone still reads `Active — V3 save envelope + checksum guard done; full exit criteria TBD`; all three corroborating sources (`CURRENT_STATE.md`, `ROADMAP.md`, `M3_RELEASE_GATE.md`) confirm M3 closed 2026-05-08.

7. **Missing references** — None: all Gradle tasks (`runRegressionTests`, `runDataContractDiagnostics`, `runWorldgenDiagnostics`, `runWorldSimulationDiagnostics`, `runRegionalStreamingDiagnostics`, `runPlayableClient`, `runGame`) present in `build.gradle.kts`. All data files and referenced docs exist.

### Recommended Next Step
Fix `CLAUDE.md` milestone table immediately — M3 has been stale for three consecutive audit cycles. This is the canonical context loaded at every session start; the mismatch is compounding. Change M3 from `Active — V3 save envelope + checksum guard done; full exit criteria TBD` to `Complete (2026-05-08)` and update the `as of` date to 2026-05-16.

---

## Audit 2026-05-17

### Verdict
PARTIAL

### Key Findings

1. **CLAUDE.md (canonical context)** — **STALE (4th consecutive cycle)**: M3 milestone still reads `Active — V3 save envelope + checksum guard done; full exit criteria TBD`. All three corroborating sources (`docs/CURRENT_STATE.md`, `docs/ROADMAP.md`, `docs/M3_RELEASE_GATE.md`) confirm M3 closed 2026-05-08. No substantive commits since 2026-05-14 — this has not been fixed in three cycles.

2. **docs/CURRENT_STATE.md** — **STALE (persistent from 2026-05-15)**: `last_updated: 2026-05-09`. Three P1 client classes added in commit `df9f762` (`InputHandler.java`, `RoomGeometry.java`, `SaveLoad.java`) remain absent from the "What Is Implemented" section. Verification evidence gate timestamp is 2026-05-08; no updated gate run recorded after P1 wiring.

3. **docs/ROADMAP.md** — **STALE (3 items, all persistent from 2026-05-14)**:
   - M4 section header tag: `` `active` `` — CLAUDE.md, CURRENT_STATE.md, and the `[AUTHORING DECISION REQUIRED]` backlog block all classify M4 as `queued`/blocked.
   - "Next open items" P1 LibGDX entry still lists `StubWorldRenderer` + `GameInputProcessor` wiring as pending; both files confirmed present at `java/client/src/main/java/com/shadowascent/client/rendering/StubWorldRenderer.java` and `.../client/input/GameInputProcessor.java`.
   - M6 "Delivered" section records "Regression harness: 49/49 PASS"; current harness runs 53 sections per `CURRENT_STATE.md` verification evidence and backlog entries.

4. **docs/MIGRATION_MAP.md** — **STALE (2 items, persistent from 2026-05-14)**:
   - Wave 4 row (~line 81): `SimPlayer.java` status `queued` targeting `core/.../sim/SimPlayer.java` — orphan row never removed when import completed; correct `done` row at ~line 91 targets `core/.../simulation/SimPlayer.java`.
   - Wave 5 table missing entries for `InputHandler.java`, `RoomGeometry.java`, `SaveLoad.java` added in commit `df9f762`.

5. **docs/IMPLEMENTATION_BACKLOG.md** — **STALE (2 section headers, persistent from 2026-05-14)**:
   - Section 7 header: "M5 Systemic World Simulation Foundation (active)" — M5 complete since 2026-05-07; all tasks `[x]`.
   - Section 8 header: "M6 Open-World Runtime Expansion (queued)" — M6 is active per CLAUDE.md and ROADMAP; all tasks inside are already `[x]`.

6. **docs/guides/DEVELOPER_WORKFLOW.md** — **STALE (2 items, persistent from 2026-05-14)**:
   - Line ~215: `` `RegressionTest.java (~100KB, 49 tests)` `` — current harness runs 53 sections.
   - Wave 4/5 extraction list `(CombatSubsystem, TraversalSubsystem, UISubsystem, MinimapRenderer)` omits three Wave 5 phase-2 extractions completed 2026-05-09: `HudRenderer`, `StoryManager`, `MissionUiCoordinator`.

7. **Missing references** — None: all Gradle tasks (`runRegressionTests`, `runDataContractDiagnostics`, `runWorldgenDiagnostics`, `runWorldSimulationDiagnostics`, `runRegionalStreamingDiagnostics`, `runPlayableClient`, `runGame`) present in build files. All referenced class names exist under `java/`. All data files exist under `data/`. Referenced docs (`M3_RELEASE_GATE.md`, `ACT_I_QA_ROUTE.md`, `NORTH_STAR_EXECUTION_MATRIX.md`, `DOC_MAINTENANCE_PLAN.md`, `planning/PRODUCTION_STACK_AND_LONG_TERM_PLAN.md`) all exist.

### Recommended Next Step
Fix `CLAUDE.md` milestone table now — M3 staleness is in its 4th consecutive audit cycle with no fix applied. Change M3 from `Active — V3 save envelope + checksum guard done; full exit criteria TBD` to `Complete (2026-05-08)`, update the `as of` date to 2026-05-17, and simultaneously mark M4 as `Active` to match ROADMAP.md (or resolve the M4 active/queued discrepancy between ROADMAP.md and the other sources).

---

## Audit 2026-05-18

### Verdict
PARTIAL

### Key Findings

1. **CLAUDE.md (canonical context)** — **STALE (5th consecutive cycle)**: M3 milestone still reads `Active — V3 save envelope + checksum guard done; full exit criteria TBD`. All three corroborating sources (`docs/CURRENT_STATE.md`, `docs/ROADMAP.md`, `docs/M3_RELEASE_GATE.md`) confirm M3 closed 2026-05-08. No substantive code commits since 2026-05-14 — this has not been fixed across five consecutive audit cycles. Highest-priority fix: this file is loaded on every session start.

2. **docs/CURRENT_STATE.md** — **STALE (persistent from 2026-05-15)**: `last_updated: 2026-05-09`. Three P1 client decomposition classes added in commit `df9f762` (`InputHandler.java`, `RoomGeometry.java`, `SaveLoad.java`) remain absent from the "What Is Implemented" section. Verification evidence gate timestamp is 2026-05-08; no updated gate run has been recorded after P1 wiring.

3. **docs/ROADMAP.md** — **STALE (3 items, all persistent from 2026-05-14)**:
   - M4 section header tag: `` `active` `` — CLAUDE.md, backlog `[AUTHORING DECISION REQUIRED]` block, and CURRENT_STATE.md all classify M4 as `queued`/blocked on SUMMIT_SHRINE authoring decisions.
   - "Next open items" P1 LibGDX entry still says `StubWorldRenderer` + `GameInputProcessor` wiring is pending; both files confirmed present at `java/client/.../rendering/StubWorldRenderer.java` and `java/client/.../input/GameInputProcessor.java`.
   - M6 "Delivered" section records "Regression harness: 49/49 PASS"; current harness runs 53 sections per `CURRENT_STATE.md` verification evidence and backlog entries.

4. **docs/MIGRATION_MAP.md** — **STALE (2 items, persistent from 2026-05-14)**:
   - Wave 4 row (~line 81): `SimPlayer.java` status `queued` targeting `core/.../sim/SimPlayer.java` — orphan row never removed when import completed; correct `done` row at ~line 91 targets `core/.../simulation/SimPlayer.java`.
   - Wave 5 table missing entries for `InputHandler.java`, `RoomGeometry.java`, `SaveLoad.java` added in commit `df9f762`.

5. **docs/IMPLEMENTATION_BACKLOG.md** — **STALE (2 section headers, persistent from 2026-05-14)**:
   - Section 7 header: "M5 Systemic World Simulation Foundation (active)" — M5 complete since 2026-05-07; all tasks marked `[x]`.
   - Section 8 header: "M6 Open-World Runtime Expansion (queued)" — M6 is active per CLAUDE.md and ROADMAP; all tasks inside are already `[x]`.

6. **docs/guides/DEVELOPER_WORKFLOW.md** — **STALE (2 items, persistent from 2026-05-14)**:
   - Line ~215: `` `RegressionTest.java (~100KB, 49 tests)` `` — current harness runs 53 sections per `CURRENT_STATE.md` verification evidence.
   - Wave 4/5 extraction list `(CombatSubsystem, TraversalSubsystem, UISubsystem, MinimapRenderer)` omits three Wave 5 phase-2 extractions completed 2026-05-09: `HudRenderer`, `StoryManager`, `MissionUiCoordinator`.

7. **Missing references** — None: all Gradle tasks (`runRegressionTests`, `runDataContractDiagnostics`, `runWorldgenDiagnostics`, `runWorldSimulationDiagnostics`, `runRegionalStreamingDiagnostics`, `runPlayableClient`, `runGame`) present in `build.gradle.kts`. All referenced class names exist under `java/`. All data files exist under `data/`. Referenced docs (`M3_RELEASE_GATE.md`, `ACT_I_QA_ROUTE.md`, `NORTH_STAR_EXECUTION_MATRIX.md`, `DOC_MAINTENANCE_PLAN.md`, `planning/PRODUCTION_STACK_AND_LONG_TERM_PLAN.md`, `guides/CODEX_CLI_SETUP.md`) all exist.

### Recommended Next Step
Fix `CLAUDE.md` milestone table immediately — M3 staleness has now entered its 5th consecutive audit cycle with no fix applied. In the same edit: change M3 from `Active — V3 save envelope + checksum guard done; full exit criteria TBD` to `Complete (2026-05-08)`, update the `as of` date to 2026-05-18, and resolve the M4 active/queued discrepancy (ROADMAP.md says `active`; CLAUDE.md, CURRENT_STATE.md, and backlog say `queued`/blocked). This is a two-minute change on the most-read file in the project.

---

## Audit 2026-05-19

### Verdict
PARTIAL

### Key Findings

1. **CLAUDE.md (canonical context)** — **STALE (6th consecutive cycle)**: M3 milestone still reads `Active — V3 save envelope + checksum guard done; full exit criteria TBD`. All three corroborating sources (`docs/CURRENT_STATE.md`, `docs/ROADMAP.md`, `docs/M3_RELEASE_GATE.md`) confirm M3 closed 2026-05-08. No substantive code commits since 2026-05-14 (only audit/briefing log commits); this has not been fixed across six consecutive audit cycles.

2. **docs/CURRENT_STATE.md** — **STALE (persistent from 2026-05-15)**: `last_updated: 2026-05-09`. Three P1 client decomposition classes added in commit `df9f762` (`InputHandler.java`, `RoomGeometry.java`, `SaveLoad.java`) remain absent from the "What Is Implemented" section. All three files confirmed present at `java/client/src/main/java/com/shadowascent/client/`. Verification evidence gate timestamp is still 2026-05-08; no updated gate run recorded after P1 wiring.

3. **docs/ROADMAP.md** — **STALE (3 items, all persistent from 2026-05-14)**:
   - M4 section header tag: `` `active` `` — CLAUDE.md (`Queued`), `CURRENT_STATE.md`, and the backlog `[AUTHORING DECISION REQUIRED]` block all classify M4 as queued/blocked on SUMMIT_SHRINE authoring decisions.
   - "Next open items" P1 LibGDX entry still lists `StubWorldRenderer` + `GameInputProcessor` wiring as pending; both files confirmed present at `java/client/src/main/java/com/shadowascent/client/rendering/StubWorldRenderer.java` and `.../client/input/GameInputProcessor.java`.
   - M6 "Delivered" section records "Regression harness: 49/49 PASS"; current harness runs 53 sections per `CURRENT_STATE.md` verification evidence and backlog entries.

4. **docs/MIGRATION_MAP.md** — **STALE (2 items, persistent from 2026-05-14)**:
   - Wave 4 row (~line 81): `SimPlayer.java` status `queued` targeting `core/.../sim/SimPlayer.java` — orphan row never removed when import completed; correct `done` row at ~line 91 targets `core/.../simulation/SimPlayer.java`.
   - Wave 5 table missing entries for `InputHandler.java`, `RoomGeometry.java`, `SaveLoad.java` added in commit `df9f762`.

5. **docs/IMPLEMENTATION_BACKLOG.md** — **STALE (2 section headers, persistent from 2026-05-14)**:
   - Section 7 header: "M5 Systemic World Simulation Foundation (active)" — M5 complete since 2026-05-07; all tasks marked `[x]`.
   - Section 8 header: "M6 Open-World Runtime Expansion (queued)" — M6 is active per CLAUDE.md and ROADMAP; all tasks inside are already `[x]`.

6. **docs/guides/DEVELOPER_WORKFLOW.md** — **STALE (2 items, persistent from 2026-05-14)**:
   - Line 215: `` `RegressionTest.java (~100KB, 49 tests)` `` — current harness runs 53 sections per `CURRENT_STATE.md` verification evidence.
   - Wave 4/5 extraction list `(CombatSubsystem, TraversalSubsystem, UISubsystem, MinimapRenderer)` omits three Wave 5 phase-2 extractions completed 2026-05-09: `HudRenderer`, `StoryManager`, `MissionUiCoordinator`.

7. **Missing references** — None: all Gradle tasks (`runRegressionTests`, `runDataContractDiagnostics`, `runWorldgenDiagnostics`, `runWorldSimulationDiagnostics`, `runRegionalStreamingDiagnostics`, `runPlayableClient`, `runGame`) present in `build.gradle.kts`. All referenced class names confirmed under `java/`. All data files exist under `data/`. Referenced docs (`M3_RELEASE_GATE.md`, `ACT_I_QA_ROUTE.md`, `NORTH_STAR_EXECUTION_MATRIX.md`, `DOC_MAINTENANCE_PLAN.md`, `planning/PRODUCTION_STACK_AND_LONG_TERM_PLAN.md`, `guides/CODEX_CLI_SETUP.md`) all exist.

### Recommended Next Step
Fix `CLAUDE.md` milestone table now — M3 staleness is in its 6th consecutive audit cycle with no fix applied. Change M3 from `Active — V3 save envelope + checksum guard done; full exit criteria TBD` to `Complete (2026-05-08)`, update the `as of` date to 2026-05-19, and resolve the M4 active/queued discrepancy between `ROADMAP.md` (`active`) and `CLAUDE.md`/backlog (`Queued`/blocked). This is the highest-impact single edit in the project: the canonical context file is wrong on every session start.

---

## Audit 2026-05-20

### Verdict
PARTIAL

### Key Findings

1. **CLAUDE.md (canonical context)** — **STALE (7th consecutive cycle)**: M3 milestone still reads `Active — V3 save envelope + checksum guard done; full exit criteria TBD`. All three corroborating sources (`docs/CURRENT_STATE.md`, `docs/ROADMAP.md`, `docs/M3_RELEASE_GATE.md`) confirm M3 closed 2026-05-08. No substantive code commits since 2026-05-14; this has not been fixed across seven consecutive audit cycles.

2. **docs/CURRENT_STATE.md** — **STALE (persistent from 2026-05-15)**: `last_updated: 2026-05-09`. Three P1 client decomposition classes added in commit `df9f762` (`InputHandler.java`, `RoomGeometry.java`, `SaveLoad.java`) remain absent from the "What Is Implemented" section. All three files confirmed present at `java/client/src/main/java/com/shadowascent/client/`. Verification evidence gate timestamp is 2026-05-08; no updated gate run recorded since P1 wiring landed.

3. **docs/ROADMAP.md** — **STALE (3 items, all persistent from 2026-05-14)**:
   - M4 section header tag: `` `active` `` — CLAUDE.md (`Queued`), `CURRENT_STATE.md`, and the backlog `[AUTHORING DECISION REQUIRED]` block all classify M4 as queued/blocked on SUMMIT_SHRINE authoring decisions.
   - "Next open items" P1 LibGDX entry still lists `StubWorldRenderer` + `GameInputProcessor` wiring as pending; both files confirmed present at `java/client/src/main/java/com/shadowascent/client/rendering/StubWorldRenderer.java` and `.../client/input/GameInputProcessor.java`.
   - M6 "Delivered" section records "Regression harness: 49/49 PASS"; current harness runs 53 sections per `CURRENT_STATE.md` verification evidence and backlog entries.

4. **docs/MIGRATION_MAP.md** — **STALE (2 items, persistent from 2026-05-14)**:
   - Wave 4 row (~line 81): `SimPlayer.java` status `queued` targeting `core/.../sim/SimPlayer.java` — orphan row never removed when import completed; correct `done` row at ~line 91 targets `core/.../simulation/SimPlayer.java`.
   - Wave 5 table missing entries for `InputHandler.java`, `RoomGeometry.java`, `SaveLoad.java` added in commit `df9f762`.

5. **docs/IMPLEMENTATION_BACKLOG.md** — **STALE (2 section headers, persistent from 2026-05-14)**:
   - Section 7 header: "M5 Systemic World Simulation Foundation (active)" — M5 complete since 2026-05-07; all tasks marked `[x]`.
   - Section 8 header: "M6 Open-World Runtime Expansion (queued)" — M6 is active per CLAUDE.md and ROADMAP; all tasks inside are already `[x]`.

6. **docs/guides/DEVELOPER_WORKFLOW.md** — **STALE (3 items; 1 new this cycle)**:
   - Line ~215: `` `RegressionTest.java (~100KB, 49 tests)` `` — current harness runs 53 sections per `CURRENT_STATE.md` verification evidence (persistent from 2026-05-14).
   - Wave 4/5 extraction list `(CombatSubsystem, TraversalSubsystem, UISubsystem, MinimapRenderer)` omits three Wave 5 phase-2 extractions completed 2026-05-09: `HudRenderer`, `StoryManager`, `MissionUiCoordinator` (persistent from 2026-05-14).
   - **NEW**: CI section claims `runWorldSimulationDiagnostics` runs only "on merge", but the actual `.github/workflows/ci.yml` runs all tasks (`runDataContractDiagnostics`, `runWorldgenDiagnostics`, `runWorldSimulationDiagnostics`, `runRegressionTests`) on every push and pull_request trigger — not split by merge.

7. **Missing references** — None: all Gradle tasks (`runRegressionTests`, `runDataContractDiagnostics`, `runWorldgenDiagnostics`, `runWorldSimulationDiagnostics`, `runRegionalStreamingDiagnostics`, `runPlayableClient`, `runGame`) present in `build.gradle.kts`. All referenced class names confirmed under `java/`. All data files exist under `data/`. Referenced docs (`M3_RELEASE_GATE.md`, `ACT_I_QA_ROUTE.md`, `NORTH_STAR_EXECUTION_MATRIX.md`, `DOC_MAINTENANCE_PLAN.md`, `planning/PRODUCTION_STACK_AND_LONG_TERM_PLAN.md`, `guides/CODEX_CLI_SETUP.md`) all exist.

### Recommended Next Step
Fix `CLAUDE.md` milestone table immediately — M3 staleness is in its 7th consecutive audit cycle with no fix applied. Change M3 from `Active — V3 save envelope + checksum guard done; full exit criteria TBD` to `Complete (2026-05-08)`, update the `as of` date to 2026-05-20, and resolve the M4 active/queued discrepancy between `ROADMAP.md` (`active`) and `CLAUDE.md`/backlog (`Queued`/blocked). In the same session, fix the new DEVELOPER_WORKFLOW.md CI section to reflect that all gate tasks run on every push/PR.
