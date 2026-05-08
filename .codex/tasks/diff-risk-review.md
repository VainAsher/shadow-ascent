You are a bounded validation worker for Shadow Ascent.

Do not edit files.
Do not produce long logs.
Do not speculate beyond evidence in the output.

Task: Review the current uncommitted diff or staged changes for risk before committing
or before handing off to Claude Code for review. Identify layer violations, hardcoded
contract values, behavioural changes, and scope creep.

Steps:
1. Run: git diff --stat
   List changed files and line counts.

2. Run: git diff
   Read the full diff. For each changed file, assess:

   a. Layer boundary check:
      - Does any core class (java/core/) now import from client or server packages?
      - Does any client class (java/client/) import from server packages?

   b. Contract value check:
      - Are any plateau IDs, beat IDs, story flag keys, quest IDs, NPC IDs, or area IDs
        hardcoded as string literals outside of the canonical data files?
      - String literals to flag: anything matching patterns like "plateau_", "beat_",
        "flag_", "quest_", "npc_", "area_" that appear in Java source.

   c. Behaviour change check:
      - Does any change alter combat damage values, movement parameters, or physics
        constants without an accompanying comment explaining why?
      - Does any change alter save schema fields without a corresponding migration?

   d. Scope creep check:
      - Does the diff contain changes to files unrelated to the stated task?
      - Does any change touch narrative canon (plateau IDs, beat sequences) that was not
        part of the intended task?
      - Are there any new references to Arcade Mode or Sandbox Mode?

   e. Test coverage check:
      - Were any new public methods or classes added without corresponding test coverage
        in RegressionTest.java?

3. Run: ./gradlew :core:compileJava :client:compileJava :server:compileJava
   Confirm the diff compiles before flagging it as ready.

Do not output the full diff text in your result — summarise findings only.

Return only:

# Codex Result

## Verdict
PASS | FAIL | PARTIAL

## Commands Run
- git diff --stat
- git diff
- ./gradlew :core:compileJava :client:compileJava :server:compileJava

## Files Read
- [changed files inspected]

## Files Changed
None.

## Key Findings
1. [Files changed: N files, +X/-Y lines]
2. [Layer violations: yes/no — list if yes]
3. [Hardcoded contract values: yes/no — list file and value if yes]
4. [Behavioural changes without justification: yes/no — describe if yes]
5. [Save schema changes without migration: yes/no]
6. [Scope creep detected: yes/no — describe if yes]
7. [New public API without test coverage: yes/no — list if yes]
8. [Compiles: core PASS/FAIL, client PASS/FAIL, server PASS/FAIL]

## Risks / Uncertainties
1. [Any change whose intent is ambiguous from the diff alone]
2. [Any change that passes compile but may introduce a runtime regression]

## Recommended Next Step
One narrow next action for Claude Code to take.
