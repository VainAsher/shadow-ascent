---
doc_type: plan
status: living
owner: core-team
last_updated: 2026-05-09
version_anchor: 0.0.1
---
# Shadow Ascent Start-to-Finish Game Plan

## Purpose
Define the full-campaign product direction and execution guardrails for the clean-start repository.

## Product Thesis
Shadow Ascent is a full narrative Metroidvania campaign, not a one-slice prototype. Act I exists as a quality gate and onboarding experience inside a broader multi-act progression.

North-star trajectory: after campaign spine hardening, extend into a systemic open-world narrative RPG architecture through explicit staged milestones rather than direct scope jump.

## Current Baseline (Truth, as of 2026-05-09)

- Clean-start scaffold operational; GitHub repo live at VainAsher/shadow-ascent; CI green (53/53 regression sections).
- Data contracts loaded and validated at runtime; FAIL_FAST mode active on CI.
- Act I QA route complete: donor-physics parity signed off (2026-05-07), four-room traversal topology, combat encounter, echo puzzle room, faction tension mutation.
- SAVE_V3 envelope active with SHA-256 checksum; V0/V1/V2 migration chain implemented.
- LibGDX P1 production client bootstrap done: `DesktopLauncher` + `ShadowAscentGame` + `HubScreen` + `StubWorldRenderer` + `GameInputProcessor` wired; OrthographicCamera follow and stub gravity integrated.
- M5 world simulation foundation complete: world/faction/settlement contracts, deterministic tick/event runtime, WorldSimulationTick.
- M6 regional streaming foundation done: RegionManifest/RegionLoader/MutationOverlay, three authored region fragments, SAVE_V3 overlay persistence.
- Donor repos remain reference sources, not direct release candidates.

## Vision Anchors

- authored critical path with clear emotional progression,
- elastic optional content that never invalidates campaign truth,
- evolving hubs and NPC states tied to story flags,
- deterministic systems suitable for QA and regression validation,
- maintainable architecture that supports long-term completion.

## Scope

### In Scope

- campaign progression across acts,
- mission and story-state continuity,
- contract-driven narrative/world rules,
- save/load stability,
- internal QA and deterministic diagnostics.

### Out of Scope (for now)

- final art/audio/cinematic polish,
- advanced multiplayer/co-op expansion,
- non-critical experimental systems unrelated to campaign completion.

## Implementation Principles

1. **Truth over aspiration**
   Documentation status must match implemented behavior.

2. **Contracts over hardcoding**
   Narrative/world progression is sourced from `data/` contracts.

3. **Bounded migration**
   Import donor systems in slices, with explicit ownership and tests.

4. **Gate-based progress**
   No milestone promotion without validation evidence.

## Milestone Intent

- M0: scaffold and baseline docs — **complete**.
- M1: objective-driven Act I QA route — **complete** (2026-05-07).
- M2: contract-backed campaign spine — **complete** (2026-05-07).
- M3: save/version stability and release evidence — **active** (SAVE_V3 + checksum done; full exit criteria TBD).
- M4: campaign coverage expansion and polish — **queued** (SUMMIT_SHRINE narrative decisions provided; authoring next).
- M5: systemic world simulation foundation — **complete** (2026-05-07).
- M6: open-world runtime expansion — **active** (streaming foundation done; open-world runtime design TBD).

Reference matrix: `docs/NORTH_STAR_EXECUTION_MATRIX.md`.

## Donor Repositories

- `indie-ninja-adventures`: engineering donor for simulation/worldgen/validation architecture.
- `shadow_ascent_integrated_package`: data-contract and narrative-model donor.

Use `docs/MIGRATION_MAP.md` for exact file-level migration order.
