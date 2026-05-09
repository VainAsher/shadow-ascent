---
doc_type: playable_client_plan
status: living
owner: core-team
last_updated: 2026-05-09
version_anchor: 0.0.1
---
# Playable Client Plan

## Goal

Deliver a human-playable client that supports reliable Act I playtests and preserves donor-quality movement feel, then evolve it into the LibGDX production client.

Decision lock: player feel is measured against `indie-ninja-adventures` movement/physics behavior, even when architecture is refactored.

## Current Baseline (2026-05-09)

**PlaytestClient (Swing QA harness) — feature-complete for Act I:**

- Full donor-physics parity sign-off complete (2026-05-07): dash direction-lock bug fixed, dash cooldown raised to 1.0s. Evidence in `docs/ACT_I_QA_ROUTE.md`.
- Four-room authored traversal topology with camera-follow, ability-gated blockers, moving platforms.
- Full combat encounter runtime (telegraphed windows, player health/death/reset, objective integration).
- `InventoryPanel`, `ShopPanel`, `CraftingPanel` UI overlays (I/T/E keys).
- `MinimapRenderer` extracted (`M` toggle).
- Subsystem decomposition complete:
  - `CombatSubsystem` (2026-05-07)
  - `TraversalSubsystem` (2026-05-08)
  - `UISubsystem` (2026-05-08)
  - `MinimapRenderer` (2026-05-08)
  - `HudRenderer` (2026-05-09)
  - `StoryManager` (2026-05-09)
  - `MissionUiCoordinator` (2026-05-09)
- Echo puzzle room authored in Room 4; `EchoPuzzleEvaluator` wired; PUZZLE_PASSED/FAILED emitted.
- Faction tension mutation floor active in WorldSimulationTick.
- Session evidence logs: `logs/playtest/playtest_session_*.log`.

**LibGDX production client (Phase P1 — in progress):**

- libgdx 1.12.1 + LWJGL3 backend deps in `:client`.
- `DesktopLauncher.java` + `ShadowAscentGame.java` + `HubScreen.java` created (`runGame` Gradle task).
- `StubWorldRenderer.java` renders players/enemies/NPCs as coloured ShapeRenderer rectangles.
- `GameInputProcessor.java` routes WASD/space/shift/F/E/I keys to `InputCommand` → `GameSimulator.applyInput()`.
- `GameSimulator` wired in `ShadowAscentGame.create()`; ticked each frame from `HubScreen.render()`; events drained per frame.
- CI green; 53/53 regression sections pass.

## Phase Plan

### Phase P0 — MVP Interaction Surface (`done`)

- Windowed runtime loop, keyboard movement, NPC interaction, mission hooks, save/load hooks.

### Phase P1 — Donor Mechanics Profile Import (`done` for PlaytestClient; LibGDX bootstrap `active`)

- Physics constants + state + movement subset imported (`core.physics`). Done.
- Collision/hash starter slice + traversal geometry imported. Done.
- Deterministic controller regression baseline locked. Done.
- LibGDX: DesktopLauncher + ShadowAscentGame + HubScreen + StubWorldRenderer + GameInputProcessor wired. Done (2026-05-09).
- **Next P1 step**: OrthographicCamera follow + delta-capped physics integration in LibGDX render loop.

### Phase P2 — Collision Extraction and Physics Parity (`queued`)

- Extract `CollisionWorld` from PlaytestClient into `core.physics`.
- Wire both PlaytestClient and LibGDX client to use `CollisionWorld` for AABB resolution.
- Add regression coverage for floor/ceiling/wall/corner resolution.
- Gate: identical collision behavior verified between both clients.

### Phase P3 — Asset Pipeline + Placeholder Sprites (`queued`)

- TexturePacker Gradle task; `assets/sprites/` pipeline.
- `SpriteWorldRenderer` replaces `StubWorldRenderer`.
- Camera lerp follow.

### Phase P4–P7 — Tiled Maps, Animation, Audio, HUD Port (`queued`)

- See `docs/planning/PRODUCTION_STACK_AND_LONG_TERM_PLAN.md` for full phase plan.

## Non-Goals (for current phases)

- Final art or audio assets before P3/P6.
- Scene2D UI (direct SpriteBatch rendering is the HUD path).
- Box2D physics (custom AABB only).
- HTML5/GWT backend (out of scope for v1.0).

## Commands

```bash
./gradlew runPlayableClient        # Swing QA harness
./gradlew runGame                  # LibGDX production client
./gradlew runRegressionTests
./gradlew :client:compileJava
```
