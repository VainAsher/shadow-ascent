---
doc_type: architecture_plan
status: living
owner: core-team
last_updated: 2026-05-09
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

**Shipping client decision (2026-05-08): LibGDX (`lwjgl3` backend).**

- `DesktopLauncher` + `ShadowAscentGame` are the LibGDX entry points.
- `HubScreen` owns the `OrthographicCamera`, render loop, and input submission.
- `StubWorldRenderer` (Phase P1) renders entities as `ShapeRenderer` rectangles; replaced by `SpriteWorldRenderer` in P3.
- `GameInputProcessor` routes keyboard input to `InputCommand` → `GameSimulator.applyInput()`.
- `PlaytestClient` (Swing) coexists as the regression/QA harness until LibGDX reaches feature parity; both run via separate Gradle tasks (`runPlayableClient`, `runGame`). Do not delete `PlaytestClient` during migration.
- `GameSimulator.drainEvents()` is the canonical event bus between simulation and presentation. No pub/sub framework.
- Custom AABB physics only — Box2D is ruled out. HTML5/GWT and multiplayer networking are out of scope for v1.0.

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
- `runWorldgenDiagnostics` for worldgen section/progression graph checks.
- `runWorldSimulationDiagnostics` for M5 world/faction/settlement simulation contract checks.

Full CI gate: `./gradlew clean :core:compileJava :client:compileJava :server:compileJava runDataContractDiagnostics runWorldgenDiagnostics runWorldSimulationDiagnostics runRegressionTests`

Active/future additions:

- mission/quest signal traceability integration over M5 simulation events,
- regional streaming + mutation safety validation (`M6`),
- expanded persistence migration checks for `SAVE_V4+` schemas (SAVE_V3 is current; V1/V2/V0 migratable).

## Migration Architecture Strategy

- Use donor repos as component sources, not as direct foundations.
- Import in slices with explicit ownership and tests.
- Avoid monolith imports; refactor donor logic into bounded modules before adoption.

Reference: `docs/MIGRATION_MAP.md`.

## Current Core Gap Summary (as of 2026-05-09)

1. **M0–M3 complete.** M1 movement-feel sign-off done (donor-physics parity 2026-05-07). SAVE_V3 envelope active with SHA-256 checksum and V0/V1/V2 migration chain.
2. **M4 (Campaign Content Scale) — queued.** SUMMIT_SHRINE narrative decisions provided; authoring of beats, flags, quests, and NPC eligibility entries is next.
3. **M5 complete.** World/faction/settlement simulation contracts, diagnostics, and deterministic tick/event runtime all shipped (2026-05-07).
4. **M6 (Open-World Runtime) — active.** Regional streaming constraint model + authored region fragments done. Remaining M6 gap: open-world runtime design (mutation loop wiring, mission/quest consumption of simulation events, regional mutation safety).
5. **LibGDX P1 bootstrap done (2026-05-09).** DesktopLauncher + ShadowAscentGame + HubScreen + StubWorldRenderer + GameInputProcessor wired. OrthographicCamera follow, stub gravity, and position integration added. Next P1 step: CollisionWorld extraction (Phase P2) for AABB resolution parity.

These gaps define the immediate architecture work for M4 content authoring, M6 runtime design, and LibGDX P2 collision extraction.
