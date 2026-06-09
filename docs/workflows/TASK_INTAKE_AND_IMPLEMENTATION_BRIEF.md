---
doc_type: workflow
status: living
owner: core-team
last_updated: 2026-05-10
version_anchor: 0.0.1
---
# Task Intake And Implementation Brief

Pre-implementation workflow for forcing scope clarity before code or documentation changes begin.

## Rules

1. No substantial task starts without a written brief.
2. The brief must be short enough to read in under two minutes.
3. If the brief cannot identify the canonical doc and acceptance check, the task is not ready.
4. Scope creep discovered during implementation requires a brief update before work continues.

## Brief Minimum

Every implementation brief must answer:

- goal
- user or player-facing impact
- systems or documents touched
- risks
- required validation
- required docs to update
- rollback plan

## Canonical Loop

1. Pull the task from backlog, bug report, review finding, or GDD need.
2. Write the brief in the task note, commit note, or working scratchpad.
3. Identify canonical docs and runtime systems touched.
4. Define acceptance validation before implementation.
5. Confirm the rollback path.
6. Start work only after the brief is complete.

## Scope Guardrails

A brief must explicitly call out when the task touches any of the following:

- persistence or schema
- versioning or release metadata
- canonical docs or indexes
- gameplay behavior or tuning
- migration or donor imports
- GDD canon or narrative source-of-truth material

## Failure Path

If implementation reveals hidden systems or risk not captured in the brief:

1. Stop broadening the change silently.
2. Update the brief.
3. Reconfirm validation and rollback plan.
4. Re-scope or split the task before continuing.

## Related Workflows

- `READY_DONE_WORKFLOW.md`
- `DOCUMENTATION_CANONICAL_SURFACE_WORKFLOW.md`
