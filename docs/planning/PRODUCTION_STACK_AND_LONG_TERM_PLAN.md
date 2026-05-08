---
doc_type: long_term_plan
status: living
owner: core-team
created: 2026-05-08
last_updated: 2026-05-08
---

# Shadow Ascent — Production Stack Assessment and Long-Term Plan

## Purpose

This document defines and justifies the production tech stack, then maps a specific long-horizon development plan from current state to a shippable game. It is the missing document that turns an engineering foundation into a product roadmap.

It lives in `docs/planning/` and should be updated when major architectural decisions are made or phases are completed.

---

## Part 1 — Stack Assessment

### 1.1 The Proposed Stack

```
Java 21 + Gradle multi-module
LibGDX 1.12+ desktop client (lwjgl3 backend)
core.simulation (existing GameSimulator, all entities, PlayerInputController)
GameDataContracts (existing contract/narrative system)
Tiled (.tmx) + JSON authored level content
Custom AABB platformer physics (existing PhysicsConstants/PhysicsState/SpatialHash baseline)
LibGDX audio (Music BGM + Sound SFX)
TextureAtlas + TexturePacker + Animation<TextureRegion> sprite pipeline
Minimal event bus: GameSimulator.drainEvents() between simulation and presentation
```

**Verdict: this stack is coherent, proven, and well-matched to the existing foundation.** Every choice either extends existing work or draws from the donor repo that is already on hand. There are no architectural contradictions. The risks are manageable and specific.

---

### 1.2 Component-by-Component Assessment

#### Java 21 + Gradle

**Verdict: No change needed. Already correct.**

The existing multi-module structure (`core`, `client`, `server`) is the right shape for LibGDX. LibGDX dependencies belong only in the `client` module — `core` stays rendering-free. The current `build.gradle.kts` adds LibGDX to the `:client` `dependencies` block only.

The existing Java 21 toolchain is compatible with all LibGDX and lwjgl3 backends.

**One gap:** The current `build.gradle.kts` has `mainClass.set("com.shadowascent.client.PlaytestClient")` for `runPlayableClient`. The LibGDX launcher will require a different entry point (`Lwjgl3Application(new ShadowAscentGame(), config)` pattern). Both can coexist as separate Gradle tasks during migration.

---

#### LibGDX Desktop Client (lwjgl3 backend)

**Verdict: Best available choice. The donor already uses it — this is a port, not a greenfield build.**

LibGDX provides:
- `SpriteBatch` + `OrthographicCamera` — standard 2D sprite rendering with world-to-screen math
- `TextureAtlas` + `Animation<TextureRegion>` — sprite animation pipeline
- `InputProcessor` / `InputMultiplexer` — cleaner than Swing key bindings, supports gamepad via `Controllers` extension
- `Music` + `Sound` — cross-platform streaming BGM and short SFX clips
- `TmxMapLoader` + `TiledMap` — first-class Tiled map loading
- `AssetManager` — async background asset loading with progress tracking
- `ShapeRenderer` — debug geometry rendering (keep the Swing minimap-style debug view alongside sprites during development)
- `Gdx.files` — cross-platform file abstraction (works on desktop, Android, HTML5)
- `ApplicationListener` lifecycle: `create()` once, `render()` per frame, `resize()` on window change, `dispose()` on exit

Specific concerns:

1. **HTML5/GWT backend**: The GWT backend is technically supported but increases complexity significantly. For a solo dev targeting desktop-first, ignore the GWT backend entirely until after v1.0. Focus on the `lwjgl3` desktop backend.

2. **Scene2D UI**: LibGDX's built-in UI framework (Scene2D) is verbose but functional. For HUD elements (`InventoryPanel`, `ShopPanel`, `CraftingPanel`) that already exist in the Swing prototype, the choices are: port to Scene2D actors, or render directly with SpriteBatch and a bitmap font. The direct-render approach is faster to migrate and easier to control visually. Recommend: port HUD to direct SpriteBatch rendering first; Scene2D is optional polish later.

3. **Coexistence with PlaytestClient**: The Swing `PlaytestClient` should remain runnable throughout the LibGDX migration. It is the regression safety net. The new LibGDX client starts as a new entry point (`ShadowAscentGame.java`), not a replacement. `runPlayableClient` (Swing) and `runGame` (LibGDX) coexist as separate Gradle tasks until the LibGDX client reaches feature parity.

---

#### Existing Simulation Core

**Verdict: Zero changes required to `core.simulation`. This is the architecture paying off.**

The simulation was designed rendering-agnostic. `GameSimulator.tick(float dt)` advances simulation state; `drainEvents()` emits `List<SimEvent>` for the frame. The LibGDX render loop consumes this directly:

```java
// In ShadowAscentGame.render():
float delta = Gdx.graphics.getDeltaTime();
simulator.tick(delta);
List<SimEvent> events = simulator.drainEvents();
presentationLayer.processEvents(events);      // audio + animation + UI updates
worldRenderer.render(batch, simulator);       // read simulation state, draw sprites
```

The 49-test regression harness continues to run unchanged against `core.simulation`. The new LibGDX client adds no new test surface in `core` — client-layer rendering is validated manually, not by unit tests.

One future improvement: the simulation currently drives time via a raw `float dt` from the LibGDX frame timer. For deterministic replay and co-op, this should eventually become a fixed-timestep accumulator with render interpolation. That is a Wave 9+ concern — do not block Phase 1 on it.

---

#### Existing Data-Contract System

**Verdict: No changes needed. Jackson runs fine in a LibGDX context.**

`GameDataContracts` loads synchronously at startup. LibGDX's `create()` method is the right place to invoke contract loading — it runs once before the first frame. There is no conflict with `AssetManager` (which is for graphical assets, not JSON contracts).

One practical note: LibGDX's own `Json` class is available but should not replace Jackson for this project. The existing contract validation, cross-reference checks, and typed model classes (`BeatDefinition`, `WorldRegionStateDefinition`, etc.) are all built on Jackson. The system works; do not migrate it.

---

#### Tiled / JSON Authored Content

**Verdict: Correct choice. This is the path that eliminates authored-rectangle geometry permanently.**

Tiled maps (.tmx) replace the `buildAllDynamicTiles` rectangle geometry in `PlaytestClient` and `RegionLoader`. They integrate with the existing section template system as follows:

**Current architecture:**
- `SectionTemplate` JSON (biome, kind, footprint, sockets, mutable zones) → `RegionLoader` resolves template → `buildAllDynamicTiles` generates `TileRect` geometry from hard-coded bounds

**Target architecture:**
- `SectionTemplate` JSON (same as now, unchanged) → `RegionLoader` resolves template → loads corresponding `.tmx` file → extracts collision layer + object layer → `SpatialHash` is populated from TMX tile data, not hard-coded geometry

**The key mapping:**
- Each `SectionTemplate` (e.g. `lantern_region_hub`, biome=lantern, kind=region_hub) gets one corresponding `.tmx` file at `assets/maps/sections/lantern_region_hub.tmx`
- The `.tmx` file contains: a `ground` tile layer (visual), a `collision` object layer (AABB rects for physics), an `objects` object layer (enemy spawns, NPC positions, pickup locations, portal positions, trigger zones)
- `RegionLoader` maps template id → `.tmx` file path and hands the loaded map to the collision world

This is Wave 9 scope (post-rendering integration). During Phases 1–4 the existing rectangle geometry remains. Tiled integration comes after the rendering pipeline is stable.

**Content workflow with Tiled:**
1. Designer opens Tiled, creates a `.tmx` file matching a `SectionTemplate` footprint (e.g. 4×2 grid = 384×192 pixels at 96px/tile)
2. Paints tile layers for visual appearance
3. Draws collision rectangles on the `collision` object layer
4. Places objects (enemy_spawn, npc_anchor, portal, etc.) on the `objects` layer
5. Commits `.tmx` to `assets/maps/sections/`
6. `RegionLoader` finds and loads it automatically by template ID

---

#### Custom AABB Platformer Physics

**Verdict: Correct. Do not use Box2D.**

Box2D is physics-accurate but fights a platformer's needs at every seam:
- Dash requires instant velocity override, not force application
- Coyote time requires precise ground-state tracking that Box2D doesn't surface
- Wall jump requires input-lock frames that rigid bodies resist
- Platform riding (carry motion) is notoriously difficult with Box2D constraints
- Deterministic replay (required for echo puzzles) is impossible with Box2D's non-deterministic solver

The existing `PhysicsConstants` + `PhysicsState` + `PlayerInputController` is already the correct custom physics model. `SpatialHash` + `TileRect` is the collision representation.

**The one gap:** collision response is currently embedded in `PlaytestClient` (the `resolveCollision` / floor/ceiling/left/right-wall responses). This should be extracted into `core.physics.CollisionWorld` — a class that takes the `SpatialHash` plus a `PhysicsState` and resolves AABB contacts. This extraction is Phase 2 work and enables the LibGDX client to use the same collision resolution as the playtest client.

---

#### LibGDX Audio

**Verdict: Correct. Wire the hooks before sourcing assets.**

LibGDX `Music` (streaming, for BGM) and `Sound` (in-memory, for SFX) are straightforward. The event bus already emits the right hooks:

| SimEvent type | Suggested audio trigger |
|---|---|
| `ENEMY_AGGRO` | aggro sting SFX |
| `PLAYER_DAMAGED` | player hurt SFX |
| `PLAYER_DIED` | death sound |
| `ENEMY_DEFEATED` | enemy death SFX |
| `BOSS_INTRO_DONE` | boss music layer in |
| `BOSS_PHASE_TRANSITION` | phase sting SFX |
| `BOSS_DEFEATED` | boss death + music fade |
| `ECHO_STARTED` | echo shimmer SFX |
| `ECHO_COMPLETED` | echo resolve SFX |
| `PORTAL_ACTIVATED` | portal activation SFX |
| `COOP_REVIVE` | revive SFX |
| `REGION_TRANSITION` | ambient crossfade |

**Practical approach:** Build an `AudioManager` class in Phase 5 that reads `drainEvents()` output and dispatches to named `Sound`/`Music` instances. Wire it before sound files exist (null-safe dispatch pattern — log missing assets as warnings, do not crash). Source audio last; the wiring is the structural work.

---

#### TextureAtlas + Sprite Animation Pipeline

**Verdict: Industry standard for 2D LibGDX games. Correct choice.**

LibGDX's offline `TexturePacker` tool packs sprite sheets into `.atlas` + `.png` pairs at build time. At runtime, `TextureAtlas` loads the pack and `Animation<TextureRegion>` plays frame sequences.

**State machine design (critical):**

The simulation already owns entity state (`SimPlayer` has health, velocity, invincibility frames, etc.). The presentation layer maps simulation state → animation state name:

```java
// EntityPresenter pattern (client.presentation)
String animKey = resolveAnimKey(player, events);
// e.g. "player_idle", "player_run", "player_dash", "player_hurt", "player_die"
Animation<TextureRegion> anim = atlas.findAnimation(animKey);
TextureRegion frame = anim.getKeyFrame(stateTime, looping);
batch.draw(frame, player.physics.x, player.physics.y);
```

The simulation does not know or care about `animKey`. The presentation layer reads simulation state snapshot + events and derives animation state. This is the clean separation the event bus enforces.

**Minimum viable sprite set for first playable build:**
- Player: idle, run, jump, fall, dash, hurt, die (7 animation states)
- Enemy (generic): idle, walk, alert, attack, die (5 states)
- Boss: intro, phase1, phase2, attack, die (5 states minimum)
- NPC: idle, talk (2 states)
- Tileset: ground, platform, wall, background (1 tileset)

This is 100–150 individual sprite frames — achievable in a focused art sprint.

---

### 1.3 Architecture Summary: How It All Connects

```
[data/] contracts.json + section_templates.json + tiled .tmx maps
         ↓ (loaded at startup via GameDataContracts + RegionLoader + TmxMapLoader)
[core.simulation] GameSimulator.tick(dt) → drainEvents() → List<SimEvent>
         ↓ (consumed each frame by the LibGDX client)
[client.presentation]
  AudioManager.handle(events)           → Music / Sound playback
  AnimationManager.handle(events)       → update animation states per entity
  WorldRenderer.render(batch, sim)      → SpriteBatch draws entities + tilemap
  HudRenderer.render(batch, sim)        → SpriteBatch draws HUD (health, missions, minimap)
  InputProcessor.keyDown/keyUp()        → routes to PlayerInputController.applyInput()
         ↓
[Lwjgl3Application] 60fps render loop
```

The simulation core has no dependency on LibGDX. The LibGDX client has no duplicate progression logic. The event bus is the only crossing point.

---

## Part 2 — Long-Term Development Plan

### Phase Structure Overview

| Phase | Name | Estimated Duration | Gate |
|---|---|---|---|
| P0 | Close current work (M3, M6 remaining) | 2–4 weeks | M3 milestone closed |
| P1 | LibGDX client bootstrap | 4–6 weeks | Simulation renders in LibGDX window |
| P2 | Collision extraction + physics parity | 3–4 weeks | `CollisionWorld` in `core.physics`; PlaytestClient and LibGDX client collision-equivalent |
| P3 | Asset pipeline + placeholder rendering | 4–6 weeks | All entities render as placeholder sprites; TextureAtlas loaded |
| P4 | Tiled map integration | 6–8 weeks | One authored section renders from `.tmx`; rectangle geometry retired for that section |
| P5 | Animation system | 4–6 weeks | Player has 7 animation states; enemies have 3 |
| P6 | Audio integration | 3–4 weeks | BGM plays; 5+ SFX wired to events |
| P7 | HUD port + game flow | 4–6 weeks | Title screen, main menu, in-game HUD, save slot selection |
| P8 | M4 campaign content (Act 2) | 3–5 months | Act 2 authored, validates with existing gates |
| P9 | Vertical slice + polish | 2–3 months | Completable Act 1 in LibGDX client with art + audio |
| P10 | Act 3+ and full campaign | 4–8 months | Full campaign playable |
| P11 | Release preparation | 2–3 months | Steam/itch.io distribution, platform builds |

Total estimate to v1.0: **24–36 months** from 2026-05-08, solo developer, assuming art production runs in parallel from Phase 3 onward.

---

### P0 — Close Current Open Work

**Duration:** 2–4 weeks  
**Prerequisite for:** everything  
**Goal:** Eliminate accumulated technical debt before starting the rendering layer. Do not begin LibGDX integration with unresolved open issues.

**Specific tasks:**

1. **Close M3 formally.**
   - Define the release-candidate checklist (formal doc in `docs/`).
   - Execute a fresh full-gate run with date: `.\gradlew.bat clean :core:compileJava :client:compileJava :server:compileJava runDataContractDiagnostics runWorldgenDiagnostics runWorldSimulationDiagnostics runRegressionTests`.
   - Capture manual QA route reproducibility: run `runPlayableClient`, complete the Act I route, confirm session log captures full progression.
   - Update `ROADMAP.md` M3 status to `completed` with evidence.

2. **Update stale planning docs.**
   - `docs/PLAYABLE_CLIENT_PLAN.md`: mark delivered items complete; update Phase P1 status.
   - `docs/START_TO_FINISH_GAME_PLAN.md`: update baseline section; movement sign-off is closed.
   - `docs/CANONICAL_ARCHITECTURE_PLAN.md`: expand Layer 3 description to match current client scope.

3. **Archive superseded analysis docs.**
   - Move `docs/analysis/COMPREHENSIVE_GAME_DEV_REVIEW_CLEAN_START.md` and `docs/analysis/EXECUTIVE_SUMMARY_CLEAN_START.md` to `docs/analysis/archive/`.

4. **Implement M6 remaining open items.**
   - Echo puzzle evaluation logic (`SimEcho` step trace → solution evaluator).
   - Capture playtest evidence for mutation visibility and route coherency across reloads (the one open `[ ]` item in `IMPLEMENTATION_BACKLOG.md` M6 section).

**Gate:** M3 status = `completed` in `ROADMAP.md`; all open `[ ]` items in IMPLEMENTATION_BACKLOG.md for M3 and M6 are ticked; all three stale planning docs updated.

---

### P1 — LibGDX Client Bootstrap

**Duration:** 4–6 weeks  
**Prerequisite:** P0 complete  
**Goal:** A LibGDX application window opens, connects to the simulation, renders entity positions as colored rectangles (same as Swing, but in LibGDX), and can run the existing Act I route with keyboard input.

**This phase deliberately does not add art.** The goal is a correct wiring, not a beautiful result.

**Specific tasks:**

1. **Add LibGDX dependencies to `build.gradle.kts`.**

   Add to `:client` dependencies:
   ```kotlin
   val gdxVersion = "1.12.1"
   implementation("com.badlogicgames.gdx:gdx:$gdxVersion")
   implementation("com.badlogicgames.gdx:gdx-backend-lwjgl3:$gdxVersion")
   implementation("com.badlogicgames.gdx:gdx-platform:$gdxVersion:natives-desktop")
   ```

   Add new Gradle task:
   ```kotlin
   task<JavaExec>("runGame") {
       group = "application"
       description = "Launch Shadow Ascent via LibGDX"
       dependsOn(":client:classes")
       classpath = project(":client").mainSourceSet().runtimeClasspath
       mainClass.set("com.shadowascent.client.desktop.DesktopLauncher")
   }
   ```

   Keep `runPlayableClient` (Swing) intact. Both tasks coexist.

2. **Create `DesktopLauncher.java`** in `com.shadowascent.client.desktop`.

   ```java
   public class DesktopLauncher {
       public static void main(String[] args) {
           Lwjgl3ApplicationConfiguration config = new Lwjgl3ApplicationConfiguration();
           config.setTitle("Shadow Ascent");
           config.setWindowedMode(1280, 720);
           config.useVsync(true);
           config.setForegroundFPS(60);
           new Lwjgl3Application(new ShadowAscentGame(), config);
       }
   }
   ```

3. **Create `ShadowAscentGame.java`** implementing `ApplicationListener`.

   Responsibilities:
   - `create()`: load data contracts, initialize `GameSimulator`, initialize stub presentation layer, initialize `InputProcessor`.
   - `render()`: advance simulation tick, drain events, update presentation, render.
   - `resize()`: update `OrthographicCamera` viewport.
   - `dispose()`: release resources.

4. **Create `StubWorldRenderer.java`** in `com.shadowascent.client.rendering`.

   Renders all simulation entities as colored `ShapeRenderer` rectangles — exactly like the Swing client visually, but on LibGDX. This is a temporary bridge class that makes the migration testable before art exists. It reads from `GameSimulator` snapshot / entity state directly.

5. **Create `GameInputProcessor.java`** in `com.shadowascent.client.input`.

   Implements LibGDX `InputProcessor`. Maps `keyDown`/`keyUp` events to `InputCommand` values and routes them to `PlayerInputController.applyInput()`. Replaces the Swing `WHEN_IN_FOCUSED_WINDOW` key bindings.

6. **Connect the event loop:**

   In `ShadowAscentGame.render()`:
   ```java
   float delta = Math.min(Gdx.graphics.getDeltaTime(), 0.05f);  // cap delta
   simulator.tick(delta);
   List<SimEvent> events = simulator.drainEvents();
   stubRenderer.render(shapeRenderer, simulator);
   ```

7. **Regression gate:** run `runPlayableClient` (Swing) — all 49 tests still pass. Run `runGame` manually and verify the window opens and simulation state is visible as shapes.

**Acceptance test (manual):**
- LibGDX window opens at 1280×720.
- Player entity visible as a colored rectangle.
- WASD/arrow movement works; dash and jump work.
- Enemies are visible; combat encounters trigger.
- Mission HUD text renders (bitmap font or ShapeRenderer text).
- Session exits cleanly without exceptions.
- `runPlayableClient` (Swing) still passes all 49 regression tests unchanged.

---

### P2 — Collision Extraction and Physics Parity

**Duration:** 3–4 weeks  
**Prerequisite:** P1 complete  
**Goal:** `CollisionWorld` extracted to `core.physics`; both Swing and LibGDX clients use the same collision resolution. Physics behavior is identical between the two clients, verified by regression harness.

**Specific tasks:**

1. **Extract `CollisionWorld.java`** to `core.physics`.

   Encapsulates:
   - The `SpatialHash` instance
   - AABB floor/ceiling/left-wall/right-wall resolution methods (currently embedded in `PlaytestClient`)
   - `addRect(TileRect)`, `removeRect(TileRect)`, `clear()` + `rebuild()` APIs
   - `queryAABB(float x, float y, float w, float h)` for collision candidates

   `PlaytestClient` delegates to `CollisionWorld` rather than owning the hash directly. `ShadowAscentGame` also delegates to the same `CollisionWorld` instance.

2. **Add regression coverage** for `CollisionWorld` in `RegressionTest.java`:
   - Floor landing (player falls onto a platform tile)
   - Ceiling bounce (player jumps into ceiling)
   - Left/right wall block
   - Corner resolution (diagonal contact prefers floor)

3. **Wire LibGDX client** to use `CollisionWorld` instead of ad-hoc geometry.

**Gate:** New regression section passes; both clients produce identical collision behavior for the standard Act I route.

---

### P3 — Asset Pipeline and Placeholder Sprites

**Duration:** 4–6 weeks  
**Prerequisite:** P1 complete (can run in parallel with P2)  
**Goal:** TextureAtlas is loaded; all entities render with placeholder sprites (simple drawn sprites, not art-quality). The `StubWorldRenderer` ShapeRenderer is replaced by a `SpriteWorldRenderer` that draws actual `TextureRegion` frames.

**Specific tasks:**

1. **Set up `TexturePacker` in build pipeline.**

   - Create `assets/sprites/raw/` for individual sprite frames.
   - Gradle task `packSprites` runs LibGDX `TexturePacker` to produce `assets/sprites/packed/sprites.atlas` + `sprites.png`.
   - The `:client:compileJava` target depends on `packSprites` so the atlas is always fresh.

2. **Create placeholder sprites** (solid-color rectangles drawn to PNGs, or simple shapes):

   - `player_idle_0.png`, `player_run_0.png`, `player_run_1.png`, `player_run_2.png` — rough pixel art or even solid blocks initially.
   - `enemy_walk_0.png`, `enemy_walk_1.png`
   - `npc_idle_0.png`
   - `tile_ground.png`, `tile_platform.png`

   These do not need to be art-quality. They are placeholders that prove the pipeline works and give visual distinction between entity types. Real art replaces them in Phase 5.

3. **Create `SpriteWorldRenderer.java`** to replace `StubWorldRenderer`.

   - `AssetManager` loads `sprites.atlas` during `create()` loading screen.
   - `render()` draws each entity using the appropriate `TextureRegion`.
   - Camera follow: `OrthographicCamera.position` lerps toward player position each frame.

4. **Create `LoadingScreen`** (if using multiple LibGDX screens):

   - `AssetManager.update()` called in `render()` until `isFinished()`.
   - Progress bar drawn.
   - Transitions to `GameScreen` when loading completes.

**Gate:** LibGDX window shows sprite-textured entities (even if placeholder art). Camera follows player. The Swing client remains unchanged.

---

### P4 — Tiled Map Integration

**Duration:** 6–8 weeks  
**Prerequisite:** P2 + P3 complete  
**Goal:** At least one authored region section loads from a `.tmx` file. Collision geometry is derived from the Tiled collision layer, not from hard-coded `TileRect` lists.

**Specific tasks:**

1. **Design the section template → TMX mapping.**

   Convention: `data/worldgen/sections/{templateId}.json` → `assets/maps/sections/{templateId}.tmx`

   `RegionLoader` (or a new `TiledSectionLoader`) accepts a `SectionTemplate` and loads the corresponding `.tmx` via `TmxMapLoader`.

2. **Author the first `.tmx` file** for `lantern_heights_hub` (the Act I starting room).

   Layers:
   - `background` (tile layer, visual only)
   - `ground` (tile layer, visual)
   - `collision` (object layer, rectangle objects — each becomes a `TileRect` in `CollisionWorld`)
   - `objects` (object layer, typed: `enemy_spawn`, `npc_anchor`, `portal`, `mission_trigger`, `ability_gate`)

3. **Wire `CollisionWorld.rebuild()` from TMX data.**

   Extract all rectangles from the `collision` layer → `TileRect` list → `CollisionWorld.addRect()`.

4. **Wire entity spawns from TMX `objects` layer.**

   On region load: iterate `objects` layer, spawn `SimEnemy`/`SimNPC`/`SimPortal` into `GameSimulator` at the authored positions.

5. **Replace rectangle geometry for the hub room** in both Swing client and LibGDX client.

6. **Iterate through remaining 12 section templates**, authoring a `.tmx` for each.

**Gate:** Complete Act I route navigable with TMX-derived collision. `buildAllDynamicTiles` hard-coded geometry retired for all authored sections. `runRegionalStreamingDiagnostics` still passes.

---

### P5 — Animation System

**Duration:** 4–6 weeks  
**Prerequisite:** P3 complete  
**Goal:** Player has 7 animation states; enemies have 3; bosses have 3. Transitions fire from simulation events.

**Specific tasks:**

1. **Create `AnimationStateManager.java`** in `com.shadowascent.client.animation`.

   Holds a map of `entityId → AnimationState`. `processEvent(SimEvent)` updates state. `getFrame(entityId, stateTime)` returns current `TextureRegion`.

2. **Define animation state keys** (strings matching atlas region names):

   Player: `player_idle`, `player_run`, `player_jump_rise`, `player_jump_fall`, `player_dash`, `player_hurt`, `player_die`, `player_wall_slide`

   Enemy: `enemy_patrol`, `enemy_alert`, `enemy_attack`, `enemy_hurt`, `enemy_die`

   Boss: `boss_intro`, `boss_idle`, `boss_attack`, `boss_phase_transition`, `boss_die`

3. **Map simulation events to transitions:**

   | Event | Animation transition |
   |---|---|
   | `PLAYER_DAMAGED` | → `player_hurt` (timed return to idle) |
   | `PLAYER_DIED` | → `player_die` (hold last frame) |
   | `ENEMY_AGGRO` | → `enemy_alert` then `enemy_chase` |
   | `ENEMY_DEFEATED` | → `enemy_die` then remove |
   | `BOSS_INTRO_DONE` | → `boss_idle` |
   | `BOSS_PHASE_TRANSITION` | → `boss_phase_transition` (timed) |
   | `BOSS_DEFEATED` | → `boss_die` |

4. **Map per-frame simulation state to animation:**

   Each render frame (outside events): read `SimPlayer.physics.vx`, `vy`, `isDead`, `invincibilityTicks`, wall-slide flag → drive idle/run/jump/fall/wall-slide transitions.

5. **Source minimum viable sprite art** (or commission placeholder) for all 7 player states. Enemies can use 2-frame cycles initially.

**Gate:** Player visually reads as different states under different conditions. A first-time observer can tell when the player is running vs. jumping vs. dashing vs. hurt.

---

### P6 — Audio Integration

**Duration:** 3–4 weeks  
**Prerequisite:** P1 complete (can run partially in parallel with P5)  
**Goal:** At least one BGM track plays; 8+ SFX are wired to simulation events. No missing-asset crash.

**Specific tasks:**

1. **Create `AudioManager.java`** in `com.shadowascent.client.audio`.

   - `loadAssets(AssetManager)` queues all audio files for loading.
   - `processEvents(List<SimEvent>)` dispatches SFX.
   - `playBgm(String trackId)` fades BGM in/out.
   - All dispatches null-checked: if asset not loaded, log warning and continue.

2. **Define audio asset registry** in `data/audio_registry.json`:

   ```json
   {
     "sfx": {
       "player_hurt": "audio/sfx/player_hurt.ogg",
       "enemy_defeated": "audio/sfx/enemy_die.ogg",
       "boss_phase": "audio/sfx/boss_phase_sting.ogg",
       "portal_activate": "audio/sfx/portal.ogg",
       "echo_start": "audio/sfx/echo_shimmer.ogg"
     },
     "music": {
       "hub": "audio/music/lantern_heights.ogg",
       "dungeon": "audio/music/hollow_descent.ogg",
       "boss": "audio/music/boss_confrontation.ogg"
     }
   }
   ```

3. **Wire BGM transitions** to `REGION_TRANSITION` events.

4. **Source or create audio assets.** Minimum viable: 1 BGM per zone type (hub, dungeon, boss fight) + 8 essential SFX. Tools: BFXR/jsfxr for SFX prototyping; Bandcamp licensing or original composition for BGM.

**Gate:** Running `runGame`, entering the hub room triggers hub BGM. Combat events trigger SFX. Audio is noticeably absent only for unimplemented assets, not due to missing wiring.

---

### P7 — HUD Port and Game Flow

**Duration:** 4–6 weeks  
**Prerequisite:** P3 + P5 complete  
**Goal:** Full game flow: title screen → new game / continue → in-game → pause → save slot → exit. In-game HUD is functional (health, mission objective, minimap, ability status).

**Specific tasks:**

1. **Implement `ScreenManager`** with LibGDX `Screen` transitions:
   - `TitleScreen` — title art, "New Game", "Continue", "Quit"
   - `GameScreen` — main gameplay loop
   - `PauseScreen` — overlay; resume / save and quit
   - `LoadingScreen` — asset loading progress bar

2. **Port HUD to LibGDX rendering** (`HudRenderer.java`):
   - Health bar (current HP as colored rect or sprite)
   - Active mission + current objective text (BitmapFont)
   - Ability status icons (dash, combat, etc.)
   - Minimap (top-corner overlay using `ShapeRenderer` or minimap texture)
   - Mission-feed toast (timed pop-up lines)

3. **Implement save slot selection screen.**
   - Three save slots, displayed with save summary (act, time played, date).
   - Wire to existing `GameState.save(path, overlaysB64)` / `loadOverlaysB64` APIs.

4. **Remove Swing-specific HUD code paths** from `PlaytestClient` once LibGDX HUD achieves feature parity.

**Gate:** A player can start the game from the title screen, play the Act I route, save at a checkpoint, quit, reopen, continue from save, complete the route, and exit cleanly. All HUD elements are readable.

---

### P8 — M4 Campaign Content (Act 2)

**Duration:** 3–5 months  
**Prerequisite:** P7 complete (LibGDX client is stable); M3 closed  
**Goal:** Act 2 authored and playable end-to-end with the LibGDX client. New zone, new boss, new narrative beats.

**Specific tasks:**

1. **Extend data contracts** for Act 2:
   - New beats in `narrative_beats.json` (act 2 milestones)
   - New story flags in `story_flags.json`
   - New quest chains in `quests.json`
   - New NPCs in `npc_registry.json`
   - New dialogue in `dialogue.json`

2. **Author 4–6 new section templates** for the Act 2 zone:
   - JSON template definitions + corresponding `.tmx` files
   - New biome/kind combos as needed
   - All validated by `runWorldgenDiagnostics` (zero issues)

3. **Create Act 2 region fragments** in `data/worldgen/regions/`.

4. **Author Act 2 boss pattern** in `BossPatternLibrary` or via a new boss entry in the simulation.

5. **Validate full progression gate:** Act 1 complete → Act 2 unlocks → Act 2 completable → acts transition correctly.

**Gate:** `runDataContractDiagnostics` PASS; `runWorldgenDiagnostics` PASS; manual playthrough of Acts 1+2 in LibGDX client succeeds with all objectives completable.

---

### P9 — Vertical Slice and Polish

**Duration:** 2–3 months  
**Prerequisite:** P8 complete; act 1+2 art is available or underway  
**Goal:** A complete, polished Act 1 experience that a player picked up off the street could enjoy and understand without guidance.

**Specific tasks:**

1. **Art pass** — replace all placeholder sprites with final-quality art for Act 1 entities and zones.
2. **Audio pass** — full BGM coverage; all major SFX wired; audio mix balanced.
3. **Juice pass** — screen shake on boss phase; screen flash on player damage; particle effects on portal activation / enemy defeat; hit-stop frames.
4. **UI polish** — consistent font/color scheme; ability unlock animation; save confirmation UI; settings menu (volume, keybindings).
5. **First external playtest** — share a build with 3–5 trusted people outside development. Observe without guiding. Fix the top 5 friction points.
6. **Performance profiling** — verify 60fps on min-spec hardware; profile GC pressure from event list allocation.

**Gate:** External playtest session, one or more players completes Act 1 without developer guidance, score rated "would play again" by majority.

---

### P10 — Act 3+ and Full Campaign

**Duration:** 4–8 months (per additional act)  
**Prerequisite:** P9 complete  
**Goal:** Full authored campaign as defined by the narrative beat timeline.

**Approach:** Apply the same content-authoring workflow from P8 for each additional act. The infrastructure built in P1–P9 is stable; the work from here is primarily content and narrative.

**Key milestones:**
- Act 3: new zone, new boss, mid-campaign story twist
- Act 4: penultimate zone, story convergence
- Act 5 / finale: endgame zone, final boss, narrative resolution
- Post-credits: optional plateau content (M4 elastic content)

---

### P11 — Release Preparation

**Duration:** 2–3 months  
**Prerequisite:** P10 complete  
**Goal:** Shippable distribution on at least one platform.

**Specific tasks:**

1. **Desktop build** — `.exe` / `.app` / Linux bundle via LibGDX's `gdx-setup` or manual Gradle packaging.
2. **Steam integration** (if targeting Steam) — Steamworks Java wrapper; achievements; cloud saves.
3. **itch.io build** — simpler fallback distribution path.
4. **Store page** — description, screenshots, trailer.
5. **Final QA pass** — end-to-end playthrough on clean machine; no placeholder assets remaining; all known bugs resolved.
6. **Pricing and marketing** — outside the scope of this plan, but must be decided before release date.

---

## Part 3 — Immediate Next Step Registry

These are the first concrete actions to take from this plan, in priority order:

| Priority | Action | Phase | Owner artifact |
|---|---|---|---|
| 1 | Define and execute M3 release-candidate checklist | P0 | `docs/M3_RELEASE_CHECKLIST.md` |
| 2 | Update `PLAYABLE_CLIENT_PLAN.md` to current delivered state | P0 | `docs/PLAYABLE_CLIENT_PLAN.md` |
| 3 | Add LibGDX to `:client` `build.gradle.kts` | P1 | `build.gradle.kts` |
| 4 | Create `DesktopLauncher.java` + `ShadowAscentGame.java` skeleton | P1 | `java/client/` |
| 5 | Create `StubWorldRenderer.java` (shapes only) | P1 | `java/client/rendering/` |
| 6 | Create `GameInputProcessor.java` | P1 | `java/client/input/` |
| 7 | Extract `CollisionWorld` to `core.physics` | P2 | `java/core/physics/` |
| 8 | Set up `TexturePacker` Gradle task | P3 | `build.gradle.kts` |
| 9 | Record this plan's decisions in `CLAUDE.md` | P0 | `CLAUDE.md` |

---

## Part 4 — Decisions to Record

The following decisions made in this plan should be captured in `CLAUDE.md` and referenced in `CANONICAL_ARCHITECTURE_PLAN.md`:

1. **LibGDX `lwjgl3` backend** is the shipping client. Not Swing. Not Unity. Not Godot.
2. **Custom AABB physics** — `Box2D` is explicitly ruled out.
3. **No Scene2D UI** for HUD — direct `SpriteBatch` rendering is the primary HUD path.
4. **`PlaytestClient` (Swing) remains** as the regression/QA harness until the LibGDX client passes feature parity. It is not deleted during migration.
5. **`GameSimulator.drainEvents()`** is the canonical event bus between simulation and presentation. No additional pub/sub framework is needed.
6. **Tiled `.tmx` maps** are the authored level format. `data/worldgen/sections/{id}.json` is the semantic metadata; `assets/maps/sections/{id}.tmx` is the geometry.
7. **HTML5/GWT backend** is out of scope for v1.0.
8. **Co-op networking** is out of scope for v1.0; the validation harness remains as-is.
