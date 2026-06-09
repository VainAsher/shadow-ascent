---
doc_type: workflow
status: living
owner: core-team
last_updated: 2026-05-10
version_anchor: 0.0.1
---
# Ready / Done Workflow

Workflow for defining when a task is allowed to start and what must be true before it is considered complete.

## Ready Definition

A task is ready only when all of the following are true:

- desired behavior is clear
- canonical doc is identified
- dependencies are known
- validation path is known
- target scope is bounded

## Done Definition

A task is done only when all of the following are true:

- code or docs are internally consistent
- required validation passed
- smoke path checked where relevant
- canonical docs updated or explicitly marked not needed
- evidence attached for runtime or behavioral changes
- next-reader ambiguity has been reduced, not increased

## Rules

1. “Work started” is not ready.
2. “Code written” is not done.
3. Ambiguous acceptance means not ready.
4. Missing documentation decisions mean not done.
5. Runtime behavior changes require evidence, not memory.

## Done Checklist

- [ ] Build, lint, or document validation passed as appropriate
- [ ] Required tests passed
- [ ] Smoke validation completed when relevant
- [ ] Canonical docs updated
- [ ] Evidence attached for runtime changes
- [ ] Open risks or follow-ups explicitly recorded

## Documentation-Specific Done Criteria

If the task touched canonical docs, also verify:

- [ ] `docs/INDEX.md` still routes clearly
- [ ] active references do not point at stale versions or retired docs
- [ ] `docs/CURRENT_STATE.md` remains a snapshot, not a release diary
- [ ] new documents have metadata frontmatter when they are canonical workflow/state docs

## Failure Path

If a task reaches review with missing ready or done conditions:

1. Move it back out of ready-for-review state.
2. Fill the missing condition.
3. Re-run validation if the gap affected behavior or documentation truth.
4. Only then resume review or completion.

## Related Workflows

- `TASK_INTAKE_AND_IMPLEMENTATION_BRIEF.md`
- `DOCUMENTATION_FRESHNESS_AND_VERSION_SYNC_WORKFLOW.md`
