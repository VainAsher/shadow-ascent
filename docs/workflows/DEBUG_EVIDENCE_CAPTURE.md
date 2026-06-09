---
doc_type: workflow
status: living
owner: core-team
last_updated: 2026-05-10
version_anchor: 0.0.1
---
# Debug Evidence Capture

Workflow for capturing enough evidence to diagnose a bug without guessing.

## Rules

1. “It broke” is not a useful bug report.
2. Capture evidence before proposing a fix whenever feasible.
3. Separate reproduction facts from interpretation.
4. If the issue is documentation-related, capture the conflicting sources explicitly.

## Evidence Minimum

- current version
- exact reproduction steps
- expected behavior
- actual behavior
- logs, screenshots, or validation output
- affected files or docs

## Canonical Loop

1. Record version and branch.
2. Record exact reproduction steps.
3. Record expected vs actual behavior.
4. Capture logs, screenshots, command output, or doc references.
5. Classify the issue:
   - runtime bug
   - validation failure
   - documentation drift
   - canonical-source conflict
6. Fix only after the evidence bundle is sufficient to re-check the result.

## Documentation Drift Variant

If the bug is documentation drift, capture:

- canonical doc that claims one thing
- conflicting doc that claims another
- version anchors or dates on both docs
- the user-facing confusion this creates

## Related Workflows

- `READY_DONE_WORKFLOW.md`
- `DOCUMENTATION_FRESHNESS_AND_VERSION_SYNC_WORKFLOW.md`
