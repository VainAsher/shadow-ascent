---
doc_type: maintenance_plan
status: living
owner: core-team
last_updated: 2026-05-08
---
# Documentation Maintenance Plan

## Purpose

Define ownership, update triggers, and discoverability conventions for the Shadow Ascent doc set so that docs stay accurate, non-redundant, and easy to navigate.

---

## Document Ownership Table

| File | Owns | Must Not Duplicate |
|---|---|---|
| `ROADMAP.md` | Milestone status (`completed` / `active` / `queued`), deliverable summaries per milestone, "Next open items" | Acceptance test definitions (owned by matrix), implementation bullets (owned by CURRENT_STATE) |
| `NORTH_STAR_EXECUTION_MATRIX.md` | Acceptance test definitions and pass conditions per milestone; harness method names | Milestone status (owned by ROADMAP), implementation evidence (owned by CURRENT_STATE) |
| `CURRENT_STATE.md` | "What Is Implemented" bullets (one bullet per shipped feature); "Open Issues / Remaining Work"; single Latest Gate evidence block; Active Risks (genuine only); Next Actions (open only) | Historical gate runs (owned by handover CODEX docs), acceptance test definitions |
| `IMPLEMENTATION_BACKLOG.md` | Task-level checklist per wave/milestone; `[x]` closed items remain for history; section headers carry `(completed)` / `(active)` status | Milestone status (owned by ROADMAP), feature description prose |
| `MIGRATION_MAP.md` | Wave import status (`done` / `active` / `pending`), per-wave class inventory | Feature behavior description (owned by CURRENT_STATE) |
| `CLAUDE.md` | Summary mirrors only — save schema version, milestone table, key file warnings, naming conventions | Detailed implementation bullets (owned by CURRENT_STATE), acceptance tests (owned by matrix) |
| `docs/handover/DESIGN_*.md` | Pre-implementation design contract for a single option; immutable after implementation begins | — |
| `docs/handover/CODEX_*_VALIDATE.md` | Post-gate evidence for a single option (test counts, BUILD SUCCESSFUL); immutable after creation | — |
| `docs/handover/README.md` | Index of all handover docs (one row per DESIGN/CODEX pair) | Content of individual handover docs |

---

## Update Triggers

| Event | Required doc updates |
|---|---|
| Milestone status change (`queued` → `active` → `completed`) | `ROADMAP.md` status + deliverables; `CURRENT_STATE.md` Product State block; `CLAUDE.md` milestone table; `NORTH_STAR_EXECUTION_MATRIX.md` milestone row |
| New feature shipped (regression gate passes) | `CURRENT_STATE.md` "What Is Implemented" (add bullet); `CURRENT_STATE.md` Latest Gate block (update counts); `IMPLEMENTATION_BACKLOG.md` (tick `[x]`); `docs/handover/README.md` (add CODEX row) |
| Wave import completed | `MIGRATION_MAP.md` wave status → `done`; `IMPLEMENTATION_BACKLOG.md` wave header → `(completed)` |
| New open risk identified | `CURRENT_STATE.md` Active Risks (add item); do NOT add completed items here |
| Risk resolved | Remove from Active Risks immediately — do not leave struck-through items |
| New open next action | `CURRENT_STATE.md` Next Actions (add item); `ROADMAP.md` "Next open items" if milestone-level |
| Action completed | Remove from Next Actions immediately — do not leave struck-through items |
| Save schema version bump | `CLAUDE.md` save schema section; `CURRENT_STATE.md` save-state bullet; `ROADMAP.md` M3 deliverables |
| Regression test count changes | `CURRENT_STATE.md` Latest Gate block; CODEX handover doc for that option |

---

## Staleness Prevention Rules

1. **No struck-through items in Active Risks or Next Actions.** Resolved risks and completed actions are deleted, not crossed out. History lives in git and in CODEX handover docs.
2. **Latest Gate block shows one result only.** When a new gate run supersedes the previous, replace the block — do not append. Historical runs are in `docs/handover/CODEX_*.md`.
3. **CURRENT_STATE.md "What Is Implemented" is additive only.** Bullets are never deleted (they record what the system can do). If a feature is removed, add a note to the relevant bullet.
4. **CLAUDE.md mirrors, never leads.** Any information in CLAUDE.md that also lives in another doc (milestone status, save version) must be updated in both places at the same time. When in doubt, ROADMAP.md / CURRENT_STATE.md are authoritative.
5. **Section headers in IMPLEMENTATION_BACKLOG.md must match wave status.** `(active)` is only valid while work is in flight; flip to `(completed)` when all tasks are ticked.
6. **Acceptance tests in NORTH_STAR_EXECUTION_MATRIX.md must name real harness methods.** Update method names whenever they are renamed in `RegressionTest.java`.

---

## Discoverability Conventions

### Entry points by audience

| Question | Start here |
|---|---|
| "What has been built?" | `CURRENT_STATE.md` → What Is Implemented |
| "What is the current milestone / what's next?" | `ROADMAP.md` → Milestones |
| "What are the acceptance tests for milestone X?" | `NORTH_STAR_EXECUTION_MATRIX.md` |
| "What donor code was imported and when?" | `MIGRATION_MAP.md` |
| "What tasks are open or closed for a given wave?" | `IMPLEMENTATION_BACKLOG.md` |
| "What are the code conventions and layer rules?" | `CLAUDE.md` |
| "What was the design intent for feature X?" | `docs/handover/DESIGN_*.md` |
| "What was the gate evidence for feature X?" | `docs/handover/CODEX_*_VALIDATE.md` |

### Handover doc naming

```
DESIGN_<SCOPE>_<TOPIC>.md          — pre-implementation
CODEX_<SCOPE>_<TOPIC>_VALIDATE.md  — post-gate evidence
```

`SCOPE` matches the wave or milestone (e.g. `WAVE4`, `M6`, `M3`). `TOPIC` is a short snake_case descriptor. Both files for a feature are indexed in `docs/handover/README.md`.

---

## Review Cadence

- **After each shipped option**: run the update triggers table above; verify CURRENT_STATE Latest Gate is current.
- **After each milestone promotion**: audit ROADMAP.md, NORTH_STAR_EXECUTION_MATRIX.md, and CLAUDE.md in one pass.
- **Quarterly** (or on major scope change): read through this plan and verify ownership rules still hold.
