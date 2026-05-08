You are a bounded validation worker for Shadow Ascent.

Do not edit files.
Do not produce long logs or full Gradle output.
Do not speculate beyond evidence in the output.

Task: Run the full Shadow Ascent CI gate and produce a step-by-step structured summary.

Steps — run in this order and record the result of each before continuing:
1. ./gradlew clean
2. ./gradlew :core:compileJava
3. ./gradlew :client:compileJava
4. ./gradlew :server:compileJava
5. ./gradlew runDataContractDiagnostics
6. ./gradlew runWorldgenDiagnostics
7. ./gradlew runWorldSimulationDiagnostics
8. ./gradlew runRegressionTests

If any step fails, record it and continue running the remaining steps anyway so the full
picture is visible. Do not stop at the first failure.

For compile steps: capture only the first compiler error per module, not the full output.
For diagnostic steps: capture WARN/ERROR summaries only.
For regression: capture section pass/fail counts and names of failed sections.

Do not output full Gradle logs.

Return only:

# Codex Result

## Verdict
PASS | FAIL | PARTIAL

## Commands Run
- ./gradlew clean
- ./gradlew :core:compileJava
- ./gradlew :client:compileJava
- ./gradlew :server:compileJava
- ./gradlew runDataContractDiagnostics
- ./gradlew runWorldgenDiagnostics
- ./gradlew runWorldSimulationDiagnostics
- ./gradlew runRegressionTests

## Files Read
None.

## Files Changed
None.

## Key Findings
1. [clean: PASS/FAIL]
2. [:core:compileJava: PASS/FAIL — error summary if failed]
3. [:client:compileJava: PASS/FAIL — error summary if failed]
4. [:server:compileJava: PASS/FAIL — error summary if failed]
5. [runDataContractDiagnostics: PASS/FAIL — issues summary]
6. [runWorldgenDiagnostics: PASS/FAIL — issues summary]
7. [runWorldSimulationDiagnostics: PASS/FAIL — issues summary]
8. [runRegressionTests: X/Y sections passed — failed section names]

## Risks / Uncertainties
1. [Any step that produced unusual output or non-deterministic results]

## Recommended Next Step
One narrow next action for Claude Code to take.
