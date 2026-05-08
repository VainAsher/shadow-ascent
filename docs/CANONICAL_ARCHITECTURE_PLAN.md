---
doc_type: architecture_plan
status: living
owner: core-team
last_updated: 2026-05-07
version_anchor: 0.0.1
---
# Canonical Architecture Plan

## Goal
Build a maintainable architecture that supports full campaign delivery while preserving deterministic, testable behavior.

## Layered Model

## Layer 1: Data Contracts (`data/`)

Canonical narrative/world rules:

- plateaus and critical routes,
- story flags,
- narrative beats,
- NPC eligibility,
- quest chain metadata,
- adaptation/grammar constraints.

Runtime requirements:

- contracts must load at startup,
- cross-reference validation must run before gameplay systems trust data,
- diagnostics must expose validation failures clearly.

## Layer 2: Core Runtime (`java/core`)

Responsibilities:

- story state ownership,
- mission lifecycle and objective progression,
- hub/NPC state transitions,
- save/load serialization and migration,
- contract-driven progression resolution.

Rules:

- no product-critical progression hardcoded without contract traceability,
- mission completion must be objective-evidence based,
- save format must be versioned.

## Layer 3: Client Runtime (`java/client`)

Responsibilities:

- player-facing loop,
- hub/mission readout,
- route debugging visibility for internal QA.

Rules:

- client should consume core state and contract outputs,
- avoid re-implementing progression logic client-side.

## Layer 4: Server Runtime (`java/server`)

Responsibilities:

- authoritative orchestration (future expansion),
- persistence and multiplayer-safe state evolution.

Rules:

- mirror core progression rules,
- do not fork narrative logic from core.

## Layer 5: Validation and Diagnostics

Core commands:

- `runRegressionTests` for scenario/regression checks,
- `runDataContractDiagnostics` for contract validity and beat resolution.

Future additions:

- mission/quest signal traceability integration over M5 simulation events,
- regional streaming + mutation safety validation (`M6`),
- expanded persistence migration checks for post-`SAVE_V2` schemas.

## Migration Architecture Strategy

- Use donor repos as component sources, not as direct foundations.
- Import in slices with explicit ownership and tests.
- Avoid monolith imports; refactor donor logic into bounded modules before adoption.

Reference: `docs/MIGRATION_MAP.md`.

## Current Core Gap Summary

1. Mission objectives/hub/dialogue contract binding is implemented; remaining M1 gate work is manual movement-feel sign-off evidence against donor behavior.
2. Save format is versioned with backward compatibility and a concrete `v1 -> v2` migrator; next forward migrator (`SAVE_V3`) is still pending schema evolution.
3. Wave 2 worldgen imports and donor-parity regression checks are implemented; remaining migration depth is in Wave 4/5 simulation and UI slices.
4. M5 expansion lane is active with world/faction/settlement contracts, diagnostics, and deterministic tick/event runtime; remaining M5 gap is mission/quest consumption of simulation events, plus M6 regional mutation/streaming validation.

These gaps define the immediate architecture work for M1 completion hardening, M3 persistence evolution, and staged M5/M6 expansion.
