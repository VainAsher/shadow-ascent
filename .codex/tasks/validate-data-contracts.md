You are a bounded validation worker for Shadow Ascent.

Do not edit files.
Do not produce long logs or full Gradle output.
Do not speculate beyond evidence in the output.

Task: Run the Shadow Ascent data contract diagnostics and produce a structured summary.

Steps:
1. Run: ./gradlew runDataContractDiagnostics
2. Capture which contracts were loaded and validated.
3. Capture any WARN or ERROR entries — summarise them, do not dump the full log.
4. Note whether any contracts failed to load entirely vs. loaded with warnings.
5. Note whether the validation mode was WARN or FAIL_FAST (check for
   SHADOWASCENT_CONTRACTS_VALIDATION_MODE in the environment).

Do not output the full Gradle log.

Return only:

# Codex Result

## Verdict
PASS | FAIL | PARTIAL

## Commands Run
- ./gradlew runDataContractDiagnostics

## Files Read
None.

## Files Changed
None.

## Key Findings
1. [Contracts validated: list which passed cleanly]
2. [Contracts with warnings or errors: list with one-line summary per issue]
3. [Validation mode active: WARN or FAIL_FAST]

## Risks / Uncertainties
1. [Any contract that loaded with warnings that could hide real issues]
2. [Any unresolved required_flags, missing plateau IDs, or tag range violations]

## Recommended Next Step
One narrow next action for Claude Code to take.
