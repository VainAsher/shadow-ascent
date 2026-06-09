---
doc_type: workflow
status: living
owner: core-team
last_updated: 2026-05-10
version_anchor: 0.0.1
---
# Documentation Freshness And Version Sync Workflow

Workflow for keeping metadata, version anchors, and active references synchronized.

## Purpose

Prevent trust erosion caused by stale anchors, mismatched dates, or “active” docs that reference retired states.

## Required Metadata For Canonical Docs

Canonical workflow, state, and operating docs should declare:

- `doc_type`
- `status`
- `owner`
- `last_updated`
- `version_anchor` when the doc tracks a versioned runtime or release state

## Sync Set

When versioned product truth changes, review the smallest relevant sync set:

- `version.json`
- `README.md`
- `docs/INDEX.md`
- `docs/CURRENT_STATE.md`
- `docs/ROADMAP.md`
- any active QA route, release gate, or handover doc named as current

## Rules

1. A doc marked current or active must not point at a stale version by name.
2. `docs/INDEX.md` must not advertise a document as active if a newer state doc supersedes it.
3. Version anchors may differ only when the document is intentionally non-runtime or non-release specific.
4. If a doc is not maintained, demote it from canonical routing.

## Canonical Loop

1. Identify whether the change affects runtime truth, milestone truth, or only supporting detail.
2. Update `version.json` and any runtime-truth docs if release truth changed.
3. Review the sync set for stale anchors, stale active references, and stale dates.
4. Update or demote inconsistent docs.
5. Record any intentionally unsynced docs as archive or supporting reference, never as active canon.

## Failure Path

If canonical docs disagree:

1. Treat `version.json` and `docs/CURRENT_STATE.md` as the first correction targets.
2. Repair `docs/INDEX.md` routing next.
3. Remove stale “active” labels before doing any broader cleanup.

## Related Workflows

- `CURRENT_STATE_HYGIENE_WORKFLOW.md`
- `READY_DONE_WORKFLOW.md`
