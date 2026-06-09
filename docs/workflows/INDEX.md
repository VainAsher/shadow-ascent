---
doc_type: workflow_index
status: living
owner: core-team
last_updated: 2026-05-10
version_anchor: 0.0.1
---
# Workflow Index

Canonical workflow entry point for the clean-start repository.

## Core Execution

- `SESSION_START_WORKFLOW.md` - prevent stale context before work starts
- `TASK_INTAKE_AND_IMPLEMENTATION_BRIEF.md` - force scope clarity before touching code or docs
- `READY_DONE_WORKFLOW.md` - define when work is allowed to start and when it is allowed to be called complete
- `DEBUG_EVIDENCE_CAPTURE.md` - capture reproducible evidence before diagnosing or fixing bugs

## Documentation Governance

- `DOCUMENTATION_CANONICAL_SURFACE_WORKFLOW.md` - keep the canonical documentation surface intentionally small
- `DOCUMENTATION_FRESHNESS_AND_VERSION_SYNC_WORKFLOW.md` - keep metadata, anchors, and active references synchronized
- `CURRENT_STATE_HYGIENE_WORKFLOW.md` - protect `docs/CURRENT_STATE.md` from turning into a rolling archive

## Practical Guide

- `DOCUMENTATION_GOVERNANCE_GUIDE.md` - maps known documentation failure modes to clean-start policy and maintenance habits

## Operating Rule

If you are unsure where to start:

1. Run `SESSION_START_WORKFLOW.md`.
2. If the task changes code or docs, run `TASK_INTAKE_AND_IMPLEMENTATION_BRIEF.md`.
3. Before claiming completion, run `READY_DONE_WORKFLOW.md`.
4. If the task touches canonical docs, also use the three documentation-governance workflows.
