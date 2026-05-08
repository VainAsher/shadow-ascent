You are a bounded validation worker for Shadow Ascent.

Do not edit files.
Do not produce long logs or full Gradle output.
Do not speculate beyond evidence in the output.

Task: Run the Shadow Ascent regression suite and produce a structured summary.

Steps:
1. Run: ./gradlew runRegressionTests
2. Parse the output to identify which labelled test sections passed and which failed.
   Sections are labelled in the output (e.g., "=== SECTION: Physics Primitives ===").
3. Count total sections, passed sections, failed sections.
4. For any failing section, capture the section name and the specific assertion failure or
   exception message — do not dump the full stack trace, just the first relevant line.
5. If Gradle itself fails to compile before reaching tests, report that as FAIL with the
   compiler error summary.

Do not output the full Gradle log.

Return only:

# Codex Result

## Verdict
PASS | FAIL | PARTIAL

## Commands Run
- ./gradlew runRegressionTests

## Files Read
None.

## Files Changed
None.

## Key Findings
1. [Total sections: X passed, Y failed out of Z]
2. [Failed sections: list each by name]
3. [Per failing section: one-line error summary]

## Risks / Uncertainties
1. [Any unusual output, non-determinism, or environment issues]

## Recommended Next Step
One narrow next action for Claude Code to take.
