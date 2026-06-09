# M4a Pre-Flight Validation

You are a bounded validation worker for Shadow Ascent.

Do not edit files.
Do not produce long logs.
Do not speculate beyond evidence.

## Task

Validate that the M4a pre-flight data additions (Tasks 1 and 2) are correct and do not break existing diagnostics.

## Steps

1. Run data contract diagnostics:
   `./gradlew.bat --console=plain runDataContractDiagnostics`

2. Run worldgen diagnostics:
   `./gradlew.bat --console=plain runWorldgenDiagnostics`

3. Compile both modules:
   `./gradlew.bat --console=plain :core:compileJava :client:compileJava`

4. Run the full regression suite:
   `./gradlew.bat --console=plain runRegressionTests`

5. Verify the following are true after step 1:
   - `contracts_loaded=true`
   - `valid=true`
   - `Validation issues: none` (or no new issues vs. prior baseline)

6. Verify area_catalog.json now contains entries for:
   - `area_training_dojo`
   - `area_lantern_forge`

7. Verify npc_registry.json now contains entries for:
   - `OLD_MAN_RIKU`
   - `LANTERN_KID`

8. Verify room spec JSON files are valid by checking:
   - `data/room_specs/lantern_heights_vertical_slice.json` — 7 rooms, 2 encounter definitions
   - `data/room_specs/mistwood_vertical_slice.json` — 2 rooms, 1 encounter definition

## Return only the standard Codex Result format:

# Codex Result

## Verdict
PASS | FAIL | PARTIAL

## Commands Run
- command 1

## Files Read
- path

## Files Changed
- (none — validation only)

## Key Findings
1. finding

## Risks / Uncertainties
1. risk

## Recommended Next Step
One narrow next action for Claude Code.
