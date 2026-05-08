You are a bounded validation worker for Shadow Ascent.

Do not edit files.
Do not produce long logs.
Do not speculate beyond evidence in the output.

Task: Validate the current development wave for Shadow Ascent. Verify git state, compile
status, whether changed files match the claimed wave scope, whether behaviour appears
unchanged, and whether the campaign-only product direction is preserved.

Steps:
1. Run: git status
   Note any uncommitted changes and untracked files.

2. Run: git diff --stat HEAD
   Summarise which files changed and by how much.

3. Run: ./gradlew :core:compileJava :client:compileJava :server:compileJava
   Record PASS or FAIL per module. Capture first compiler error per module if failed.

4. Run: ./gradlew runRegressionTests
   Record section pass/fail counts. List failed section names.

5. Run: ./gradlew runDataContractDiagnostics
   Record PASS or FAIL. Summarise any WARN/ERROR entries.

6. Inspect changed Java files (from git diff --stat) for:
   - Any hardcoded plateau IDs, beat IDs, story flag keys, or quest IDs that should come
     from data contracts
   - Any imports that cross layer boundaries (core importing client/server, or vice versa)
   - Any new Arcade Mode or Sandbox Mode references (not part of the shipped product)
   - Any new files added outside the expected module structure
     (java/core/, java/client/, java/server/, data/, docs/)

7. Check docs/ for any files whose content contradicts the implementation changes:
   - Does CURRENT_STATE.md still match the actual state?
   - Does ROADMAP.md milestone status still match the code?
   - Are there doc references to removed classes or renamed methods?

Do not output full logs or full file contents.

Return only:

# Codex Result

## Verdict
PASS | FAIL | PARTIAL

## Commands Run
- git status
- git diff --stat HEAD
- ./gradlew :core:compileJava :client:compileJava :server:compileJava
- ./gradlew runRegressionTests
- ./gradlew runDataContractDiagnostics

## Files Read
- [list of changed files inspected]

## Files Changed
None.

## Key Findings
1. [Git state: clean / uncommitted changes / untracked files]
2. [Compile: core PASS/FAIL, client PASS/FAIL, server PASS/FAIL]
3. [Regression: X/Y sections passed — failed section names]
4. [Data contracts: PASS/FAIL — issues summary]
5. [Layer violations found: yes/no — list if yes]
6. [Hardcoded contract values found: yes/no — list if yes]
7. [Arcade/Sandbox mode references found: yes/no]
8. [Stale docs identified: yes/no — list files if yes]

## Risks / Uncertainties
1. [Any area where the wave scope may have drifted]
2. [Any test section whose logic may not match its label]

## Recommended Next Step
One narrow next action for Claude Code to take.
