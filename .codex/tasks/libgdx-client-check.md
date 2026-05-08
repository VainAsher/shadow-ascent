You are a bounded validation worker for Shadow Ascent.

Do not edit files.
Do not produce long logs.
Do not speculate beyond evidence in the output.

Task: Validate a LibGDX production client change. Check that the new code respects the
simulation/presentation boundary, does not introduce Box2D or Swing references, wires
GameSimulator.drainEvents() correctly, and does not regress the PlaytestClient QA harness.

Steps:
1. Run: git diff --stat
   List changed files. Note which are in java/client/ vs java/core/.

2. Run: git diff
   For each changed file in java/client/, check:

   a. No Box2D imports (com.badlogic.gdx.physics.box2d.*):
      Shadow Ascent uses custom AABB physics only.

   b. No Swing imports (javax.swing.*, java.awt.*):
      Swing belongs in PlaytestClient only, not in the LibGDX production client.

   c. No direct mutation of SimPlayer, GameSimulator, or other core sim objects
      from render or input threads:
      All sim state changes must go through GameSimulator methods.
      Presentation reads state; it does not write it.

   d. GameSimulator.drainEvents() is the correct integration point for reading
      simulation output. Check whether new render/screen code reads events from
      drainEvents() rather than polling sim state directly.

   e. No hardcoded plateau IDs, beat IDs, story flag keys, or area IDs as string literals.

   f. No references to Arcade Mode or Sandbox Mode.

3. Run: ./gradlew :core:compileJava :client:compileJava
   Record compile result. Capture first error per module if failed.

4. Run: ./gradlew runRegressionTests
   The PlaytestClient harness must not regress. Record section pass/fail counts
   and names of any failing sections.

5. Check whether any new LibGDX Screen class was added without a corresponding
   ShadowAscentGame.setScreen() call path — orphaned screens won't be reachable.

Do not output the full diff or full logs.

Return only:

# Codex Result

## Verdict
PASS | FAIL | PARTIAL

## Commands Run
- git diff --stat
- git diff
- ./gradlew :core:compileJava :client:compileJava
- ./gradlew runRegressionTests

## Files Read
- [changed files inspected]

## Files Changed
None.

## Key Findings
1. [Files changed: N in client, N in core]
2. [Box2D imports: yes/no — list if yes]
3. [Swing imports in LibGDX client: yes/no — list if yes]
4. [Direct sim mutation from render/input: yes/no — describe if yes]
5. [drainEvents() wired correctly: yes/no/not applicable]
6. [Hardcoded contract values: yes/no — list if yes]
7. [Arcade/Sandbox mode references: yes/no]
8. [Compile: core PASS/FAIL, client PASS/FAIL]
9. [Regression: X/Y sections passed — failed section names]
10. [Orphaned Screen classes: yes/no — list if yes]

## Risks / Uncertainties
1. [Any change whose threading or lifecycle implications are ambiguous]
2. [Any new LibGDX asset load that may fail at runtime but not at compile time]

## Recommended Next Step
One narrow next action for Claude Code to take.
