You are a bounded validation worker for Shadow Ascent.

Do not edit files.
Do not produce long logs.
Do not speculate beyond evidence in the output.

Task: Audit Shadow Ascent documentation for staleness. Identify docs that contradict the
current code or milestone state, referencing removed classes, wrong milestone status, or
outdated wave completion claims.

Steps:
1. Run: git log --oneline -20
   Note the most recent 20 commits to understand what has changed recently.

2. Run: git diff --stat HEAD~5 HEAD
   Identify which non-doc files changed in the last 5 commits.

3. Read the following docs and check each against the current codebase state:

   docs/CURRENT_STATE.md
   - Does it correctly describe the active milestones? (M3 and M6 are active as of 2026-05-08)
   - Does it reference any classes or files that no longer exist?
   - Does its "verification evidence" section match what the regression suite actually tests?

   docs/ROADMAP.md
   - Do milestone completion statuses match the CLAUDE.md milestone table?
   - Are any milestones listed as "queued" that have since been started or completed?

   docs/MIGRATION_MAP.md
   - Does it correctly show Waves 1-5 as complete?
   - Does it reference any donor classes that were not imported or were renamed on import?

   docs/IMPLEMENTATION_BACKLOG.md
   - Are any backlog items listed as "todo" that are already implemented in code?
   - Are any items missing that are clearly needed given current milestone state?

   docs/guides/DEVELOPER_WORKFLOW.md
   - Does the PlaytestClient.java size estimate (listed as 120KB) still match reality?
   - Are any referenced subsystem extractions listed as pending that are actually complete?

4. Check for references in any doc to:
   - Classes named in docs that do not exist in java/core/, java/client/, or java/server/
   - Gradle tasks named in docs that are not defined in any build.gradle.kts
   - Data files referenced in docs that do not exist in data/

Do not read entire Java source files. Use file existence checks and targeted searches only.

Return only:

# Codex Result

## Verdict
PASS | FAIL | PARTIAL

## Commands Run
- git log --oneline -20
- git diff --stat HEAD~5 HEAD

## Files Read
- docs/CURRENT_STATE.md
- docs/ROADMAP.md
- docs/MIGRATION_MAP.md
- docs/IMPLEMENTATION_BACKLOG.md
- docs/guides/DEVELOPER_WORKFLOW.md

## Files Changed
None.

## Key Findings
1. [CURRENT_STATE.md: accurate / stale — specific issues if stale]
2. [ROADMAP.md: accurate / stale — specific issues if stale]
3. [MIGRATION_MAP.md: accurate / stale — specific issues if stale]
4. [IMPLEMENTATION_BACKLOG.md: accurate / stale — specific issues if stale]
5. [DEVELOPER_WORKFLOW.md: accurate / stale — specific issues if stale]
6. [References to missing classes, tasks, or data files: list if found]

## Risks / Uncertainties
1. [Any doc that may be authoritative for a decision but whose accuracy is uncertain]

## Recommended Next Step
One narrow next action for Claude Code to take.
