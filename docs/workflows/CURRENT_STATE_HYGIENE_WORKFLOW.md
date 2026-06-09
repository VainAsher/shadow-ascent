---
doc_type: workflow
status: living
owner: core-team
last_updated: 2026-05-10
version_anchor: 0.0.1
---
# Current State Hygiene Workflow

Workflow for protecting `docs/CURRENT_STATE.md` as a fast operational snapshot.

## Purpose

`docs/CURRENT_STATE.md` should tell a reader what is true now, not force them to parse a full release history.

## What Belongs In `CURRENT_STATE.md`

- current version and milestone truth
- current product direction
- current implemented surfaces
- current blockers, active lanes, and next required action
- links to evidence, handovers, or reports

## What Does Not Belong There

- long rolling release ledgers
- full historical session notes
- deep implementation diaries
- retired slice summaries
- archive-grade narrative of what happened across many versions

## Rules

1. Keep the top section readable as a fast snapshot.
2. Link to evidence; do not inline entire histories.
3. Move historical slices to `docs/handover/`, `docs/reports/`, or archive areas early.
4. If a past slice is still useful, summarize it in one line and link out.

## Canonical Loop

1. Update current truth first.
2. Ask whether a new note is snapshot truth or historical detail.
3. If historical detail, store it outside `CURRENT_STATE.md`.
4. Leave behind one concise summary line with a link if needed.

## Smell Test

If `CURRENT_STATE.md` starts answering “what happened over the last ten versions?” instead of “what is true now?”, it has drifted.

## Related Workflows

- `DOCUMENTATION_FRESHNESS_AND_VERSION_SYNC_WORKFLOW.md`
- `DOCUMENTATION_CANONICAL_SURFACE_WORKFLOW.md`
