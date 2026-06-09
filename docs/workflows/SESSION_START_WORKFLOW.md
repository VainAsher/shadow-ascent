---
doc_type: workflow
status: living
owner: core-team
last_updated: 2026-05-10
version_anchor: 0.0.1
---
# Session Start Workflow

Start-of-session workflow for preventing stale context, duplicate work, and repo-truth drift.

## Rules

1. Read `version.json` before implementing anything.
2. Treat `docs/CURRENT_STATE.md` as the runtime truth source for the session.
3. Read `docs/INDEX.md` and confirm the canonical route for the task.
4. Read the active plan or backlog item before selecting work.
5. Compare intended work against recent commits before re-implementing a feature.
6. Write a short session intent note before coding.

## Canonical Loop

1. Read:
   - `version.json`
   - `docs/INDEX.md`
   - `docs/CURRENT_STATE.md`
   - active plan, backlog note, or GDD area
   - latest `git log --oneline -10`
2. Confirm current version anchor and active milestone.
3. Identify one primary target and one stop condition.
4. Write a 3-line session note:
   - target
   - reason
   - stop condition
5. Begin implementation only after the note reflects the real repo state.

## Session Note Minimum

- Date
- Branch
- Current version
- Primary target
- First validation command
- Resume risk notes: `none`, `stale-context`, `runtime`, or `docs`

## Failure Path

If `version.json`, `docs/CURRENT_STATE.md`, the active plan, and the session summary do not align:

1. Stop implementation.
2. Record the mismatch.
3. Audit recent commits and active docs.
4. Re-scope the task against actual repo HEAD.

## Related Workflows

- `TASK_INTAKE_AND_IMPLEMENTATION_BRIEF.md`
- `CURRENT_STATE_HYGIENE_WORKFLOW.md`
