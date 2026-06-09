---
doc_type: guide
status: living
owner: core-team
last_updated: 2026-05-10
version_anchor: 0.0.1
---
# Documentation Governance Guide

Practical guide for resolving documentation drift and avoiding donor-style sprawl in the clean-start repository.

## Operating Principle

Import rigor, not sprawl.

The donor repo is strong on honesty, evidence, and metadata discipline. It is weak on canonical-surface control, freshness synchronization, and `CURRENT_STATE` hygiene. This repo should copy the strengths and reject the failure modes.

## Finding: Canonical-Surface Overload

### Failure Mode

The top-level documentation route expands until too many documents feel “canonical”, raising lookup cost and creating uncertainty about what to read first.

### Clean-Start Policy

- Keep `docs/INDEX.md` intentionally small.
- Route to one sub-index for workflows rather than listing every workflow at root.
- Promote only always-needed docs to `Core Canonical`.

### Maintenance Habit

Before adding a document to the root docs index, apply the promotion test from `DOCUMENTATION_CANONICAL_SURFACE_WORKFLOW.md`.

### Concrete Action In This Repo

- Keep workflow detail in `docs/workflows/INDEX.md`.
- Keep `docs/INDEX.md` as a route map, not a full directory mirror.

## Finding: Version Anchor And Active-Reference Drift

### Failure Mode

Different canonical docs claim different active versions, dates, or “current” references, which weakens trust in the documentation system.

### Clean-Start Policy

- Treat `version.json` and `docs/CURRENT_STATE.md` as the first correction targets.
- Require metadata on canonical workflow/state docs.
- Demote stale active docs instead of leaving them in the route.

### Maintenance Habit

Whenever runtime truth changes, review the sync set defined in `DOCUMENTATION_FRESHNESS_AND_VERSION_SYNC_WORKFLOW.md`.

### Concrete Action In This Repo

- Keep version-sensitive routing in a small sync set.
- Avoid naming older handovers or route docs as current without explicit confirmation.

## Finding: `CURRENT_STATE.md` Becoming A Release Diary

### Failure Mode

The current-state doc mixes fast operational truth with deep historical narrative, slowing down session starts and obscuring present reality.

### Clean-Start Policy

- `docs/CURRENT_STATE.md` is for current truth only.
- Historical detail lives in `docs/handover/`, `docs/reports/`, or archive branches.

### Maintenance Habit

When adding a new note, ask: “Is this true now, or is this history?” If it is history, link out instead of inlining it.

### Concrete Action In This Repo

- Preserve the current snapshot-oriented structure.
- Move future slice histories out early rather than letting them accumulate in one document.

## Strength To Preserve: Metadata Discipline

Use frontmatter on canonical docs with:

- `doc_type`
- `status`
- `owner`
- `last_updated`
- `version_anchor` when relevant

Not every file in the repo needs this. Canonical routing, runtime truth, and workflow docs do.

## Strength To Preserve: Honest Playable Truth

The clean-start repo now includes `docs/PLAYABLE_TRUTH.md`, which should continue to state:

- what is working
- what is partial
- what is broken
- what is frozen
- what feedback is in scope

This should complement `docs/ACT_I_QA_ROUTE.md`, not replace it.

## Strength To Preserve: Evidence-Backed Claims

Prefer status backed by:

- validation commands
- session logs
- QA routes
- named reports

Avoid unsupported claims like “complete”, “ready”, or “active” without a linked artifact.

## Minimal Governance Loop

1. Start from `docs/INDEX.md`.
2. Keep the root canonical surface small.
3. Update `docs/CURRENT_STATE.md` only with present truth.
4. Sync version-sensitive docs together.
5. Demote stale docs instead of leaving them in active routing.

## Related Workflows

- `DOCUMENTATION_CANONICAL_SURFACE_WORKFLOW.md`
- `DOCUMENTATION_FRESHNESS_AND_VERSION_SYNC_WORKFLOW.md`
- `CURRENT_STATE_HYGIENE_WORKFLOW.md`
