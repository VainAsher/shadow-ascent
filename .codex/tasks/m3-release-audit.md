You are a bounded validation worker for Shadow Ascent.

Do not edit files.
Do not produce long logs or full file contents.
Do not speculate beyond evidence in the output.

Task: Audit M3 (Stability/Release) exit criteria gaps for Shadow Ascent.
Determine which M3 acceptance targets are met, which are not, and what remains before the
release gate can close.

Steps — run in order, recording the result of each:

1. Read: docs/NORTH_STAR_EXECUTION_MATRIX.md
   Identify every M3 acceptance criterion listed. Record each criterion as a bullet with
   the exact text from the doc.

2. Read: docs/CURRENT_STATE.md
   Note the current milestone status, any listed risks, and the V3 save envelope status.

3. Run: ./gradlew :core:compileJava :client:compileJava :server:compileJava
   Record PASS/FAIL per module. Capture first compiler error per module if failed.

4. Run: ./gradlew runRegressionTests
   Record section pass/fail counts. List every failed section name.

5. Run: ./gradlew runDataContractDiagnostics
   Record PASS/FAIL. Summarise any WARN/ERROR entries.

6. Run: ./gradlew runWorldSimulationDiagnostics
   Record PASS/FAIL. Summarise any WARN/ERROR entries.

7. Cross-reference: For each M3 criterion from step 1, mark it as:
   - MET — evidence exists in compile, regression, or doc state
   - PARTIAL — partially addressed but not fully verified
   - GAP — no evidence it has been met

8. Read: java/core/src/main/java/com/shadowascent/core/SaveMigrationMatrix.java
   Confirm SAVE_V1, SAVE_V2 migratable paths exist and SAVE_V3 envelope is present.
   Note any missing migrators.

Do not output full file contents or full Gradle logs.

Return only:

# Codex Result

## Verdict
PASS | FAIL | PARTIAL

## Commands Run
- ./gradlew :core:compileJava :client:compileJava :server:compileJava
- ./gradlew runRegressionTests
- ./gradlew runDataContractDiagnostics
- ./gradlew runWorldSimulationDiagnostics

## Files Read
- docs/NORTH_STAR_EXECUTION_MATRIX.md
- docs/CURRENT_STATE.md
- java/core/src/main/java/com/shadowascent/core/SaveMigrationMatrix.java

## Files Changed
None.

## M3 Criteria Audit
| Criterion | Status | Evidence |
|---|---|---|
| [criterion text] | MET/PARTIAL/GAP | [one-line evidence] |

## Key Gaps (GAP and PARTIAL items only)
1. [Gap description — what is missing and why it blocks the M3 close]

## Save Schema Status
- SAVE_V1 migration: PRESENT/MISSING
- SAVE_V2 migration: PRESENT/MISSING
- SAVE_V3 envelope: PRESENT/MISSING
- Checksum guard: PRESENT/MISSING

## Risks / Uncertainties
1. [Any criterion that is ambiguous or not verifiable from code alone]

## Recommended Next Step
One narrow next action for Claude Code to take to close the largest M3 gap.
