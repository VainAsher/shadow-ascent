You are a bounded validation worker for Shadow Ascent.

Do not edit files.
Do not produce long logs or full Gradle output.
Do not speculate beyond evidence in the output.

Task: Run the Shadow Ascent worldgen and world simulation diagnostics and summarise results.

Steps:
1. Run: ./gradlew runWorldgenDiagnostics
2. Run: ./gradlew runWorldSimulationDiagnostics
3. For each, capture:
   - Whether it completed without error
   - Any WARN or ERROR entries (summarised, not full log dumps)
   - Any generated region counts, chunk counts, or simulation tick stats if printed
4. Note any references to plateau IDs, chunk grammar tags, or section templates that
   failed to resolve — these indicate contract drift.

Do not output full Gradle logs.

Return only:

# Codex Result

## Verdict
PASS | FAIL | PARTIAL

## Commands Run
- ./gradlew runWorldgenDiagnostics
- ./gradlew runWorldSimulationDiagnostics

## Files Read
None.

## Files Changed
None.

## Key Findings
1. [runWorldgenDiagnostics: PASS/FAIL — summary]
2. [runWorldSimulationDiagnostics: PASS/FAIL — summary]
3. [Any unresolved plateau IDs, tag ranges, or grammar violations]
4. [Any simulation tick failures or entity dispatch errors]

## Risks / Uncertainties
1. [Any worldgen output that differs from expected region/chunk structure]
2. [Any faction or settlement state that failed to load]

## Recommended Next Step
One narrow next action for Claude Code to take.
