---
doc_type: workflow
status: living
owner: core-team
last_updated: 2026-05-10
version_anchor: 0.0.1
---
# Documentation Canonical Surface Workflow

Workflow for preventing canonical-surface overload.

## Purpose

Keep the set of docs that a developer must read first intentionally small.

## Rules

1. `docs/INDEX.md` is a routing surface, not a complete inventory.
2. Only the smallest set of always-needed docs belongs in `Core Canonical`.
3. Supporting material should be grouped under one route, not promoted to root-level canon by default.
4. Archives, historical reviews, and retired plans must never appear as first-read canon.
5. If a new doc increases lookup cost more than decision clarity, it should not be promoted.

## Canonical Tiers

### Core Canonical

Always-read route documents such as:

- `README.md`
- `docs/INDEX.md`
- `docs/CURRENT_STATE.md`
- `docs/ROADMAP.md`
- one active validation or route doc when needed

### Supporting Canonical

Important but not universal docs, grouped by area:

- architecture plans
- migration maps
- GDD branches
- workflow indexes

### Archive Only

Historical or deep-reference material:

- handovers
- archived analysis
- completed plans no longer needed for routing
- retired docs

## Promotion Test

Before adding a doc to `docs/INDEX.md`, answer:

1. Must most contributors read this first?
2. Does it reduce ambiguity more than it increases navigation cost?
3. Is it current, maintained, and clearly owned?
4. Is there already another doc that should route to it instead?

If any answer is `no`, do not promote it to the root canonical surface.

## Failure Path

If `docs/INDEX.md` becomes broad or noisy:

1. Remove category-level sprawl from the root index.
2. Route to one sub-index instead.
3. Demote historical or specialized docs out of canonical routing.

## Related Workflows

- `DOCUMENTATION_GOVERNANCE_GUIDE.md`
- `CURRENT_STATE_HYGIENE_WORKFLOW.md`
