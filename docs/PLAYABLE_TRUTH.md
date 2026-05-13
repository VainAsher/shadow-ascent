---
doc_type: playable_truth
status: living
owner: qa-team
last_updated: 2026-05-13
version_anchor: 0.0.1
---
# Playable Truth

Honest state of what is playable and worth judging right now in the clean-start repository.

Read this before running a playtest. Read this before giving feedback. Read this before treating the current runtime as broader than it is.

## Current Build

- Version: `0.0.1`
- Platform: local desktop developer runtime
- Audience: internal development and controlled QA only
- Primary runtime: `runPlayableClient`
- Product direction: full campaign narrative Metroidvania, with the current runtime serving as a bounded validation slice rather than a content-complete game

## The One Approved QA Route

Use [ACT_I_QA_ROUTE.md](ACT_I_QA_ROUTE.md) as the repeatable first-session route and evidence gate.

Current approved focus:

1. Launch the playable client.
2. Verify the opening hub is readable.
3. Progress through the first social mission path.
4. Confirm objective updates, hub-state changes, and save/load continuity.

Pass criteria live in [ACT_I_QA_ROUTE.md](ACT_I_QA_ROUTE.md).

## What Is Working Well Enough To Judge

| System | Status | Notes |
| --- | --- | --- |
| Core movement slice | Working | Run, jump, wall-jump, dash, coyote behavior, and wall stamina are implemented and regression-backed |
| First-session traversal route | Working | Multi-room authored traversal topology exists and is suitable for route validation |
| Objective-driven mission progression | Working | Mission progress is contract-backed rather than placeholder auto-complete |
| Hub progression logic | Working | Hub state resolves from progression flags rather than only manual stepping |
| NPC schedule and dialogue routing | Partial but judgeable | Contract-authored schedules and dialogue selection exist, with fallback behavior where authoring is incomplete |
| Save/load continuity | Working | Versioned save envelope and legacy migration path are implemented |
| Session evidence logging | Working | Play sessions emit logs and movement sign-off telemetry |
| Minimap and HUD readability aids | Working | The runtime exposes minimap, objective progress, and gate state for route validation |

## What Exists But Is Not Yet A Good Target For Broad Feedback

| System | State | Why feedback should stay narrow |
| --- | --- | --- |
| Campaign content breadth | Partial | Runtime spine exists, but content scale is not the current proof target |
| Combat depth | Partial | The bounded encounter loop is present, but not yet representative of the full intended combat game |
| LibGDX production client | Early but more legible | `runGame` now renders atlas-backed placeholder sprites plus a title/new-game/continue flow, persistent HUD, toggleable minimap, dialogue modal, pause/save/load flow, modal inventory/shop/crafting overlays, audio event-key routing, and explicit interaction hints, but it is still a bounded production-client validation slice rather than the main QA route |
| Open-world systemic simulation | Active expansion lane | M6 is active, but it should be judged as foundation work, not finished player-facing design |

## What Is Scaffolded Or Experimental

These exist in the repo and may be visible in docs or code, but should not be treated as finished playtest targets:

| System | State |
| --- | --- |
| Multi-act content authoring | Scaffolded / queued beyond current route proof |
| Full open-world runtime expansion | Active architecture lane |
| Broader co-op and advanced systemic simulation behavior | Foundation present, not current review target |
| Late-game narrative plateaus and post-Act I content breadth | Contract and planning surface present, not runtime-complete |

## What Is Frozen For The Current Feedback Loop

Do not use current sessions to broaden scope into these areas unless the task explicitly targets them:

| Area | Freeze reason |
| --- | --- |
| Broad content-complete balancing | Current runtime is still a slice, not a final tuning target |
| Late-game progression judgment | Current route does not prove late-game pacing or content density |
| Documentation-driven assumptions about unimplemented content | GDD and planning material can outrun runtime truth |

## What Feedback We Want Right Now

- Did the approved route remain understandable without hidden state shortcuts?
- Did movement feel readable and controllable in the bounded route?
- Did mission objectives, hub changes, and save/load continuity behave coherently?
- Did logs and evidence make the session reviewable after the fact?

## What Feedback Is Out Of Scope Right Now

- Final content-complete balance
- Late-game progression or narrative pacing
- Production-level art or polish judgments on placeholder-heavy runtime slices
- Scope-expanding feature requests not tied to the current milestone or route

## Canonical Relationship

- Runtime truth: [CURRENT_STATE.md](CURRENT_STATE.md)
- Repeatable route and evidence gate: [ACT_I_QA_ROUTE.md](ACT_I_QA_ROUTE.md)
- Canonical docs route: [INDEX.md](INDEX.md)
