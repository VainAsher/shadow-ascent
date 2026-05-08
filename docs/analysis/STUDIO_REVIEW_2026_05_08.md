---
doc_type: studio_review
snapshot_date: 2026-05-08
revision: v2 (codex cross-check applied 2026-05-08)
status: snapshot (immutable after creation)
reviewer_perspective: critically acclaimed studio team → solo developer
---

# Shadow Ascent — Studio Review

## Snapshot: 2026-05-08 (Revised)

> **Revision note.** This document supersedes the v1 draft. A second-pass codex cross-check was applied on the same date. That cross-check raised six valid structural concerns (framing of playable client, shipping-path ambiguity, truth-drift risk, stale docs audit, roadmap criticism framing, near-term budget risk). It also raised one factually incorrect claim — that the build is currently broken with compile failures in `RegionalStreamingDiagnostics.java`. That claim was verified against the live codebase: both `:core:compileJava` and `:client:compileJava` pass cleanly. `RegionFragmentData` is a standalone public record, not an inner class; the cited errors do not exist. The revision incorporates the valid structural improvements and corrects the false build-health claim.

---

> **How to read this document.** This is a timestamped snapshot written as if from an experienced cross-discipline studio review team evaluating a solo developer's project. It is direct, honest, and constructive — not a summary of your own docs reworded back at you. It will tell you what you have actually built, what it would take for a player to experience it as a shipped game, and where you are burning engineering budget in ways that may not close that gap. Future snapshots in `docs/analysis/` will form a longitudinal record of state so you can see velocity and drift over time.

---

## 1. Headline Verdict

You have built an **exceptionally disciplined engineering foundation** for a solo project. The data-contract architecture, wave import discipline, regression gate hygiene, and documentation coherence are genuinely at a level we rarely see from a team of ten, let alone one person. That is not flattery — it is the reason this review can be honest about the gaps without worrying you will throw the foundation away.

The hard truth: **you have built a serious internal game-development platform, not yet a convincing shipped game candidate.** A player willing to pull the repo and compile it can run a prototype route, navigate four rooms, fight enemies, and open an inventory. What they cannot experience is a commercially legible game — no sprites, no audio, no title screen, no narrative arc beyond a QA route. That gap is not primarily a code quality problem. It is a product definition problem: the roadmap has not yet chartered the path from engineering foundation to player-facing experience.

The second hard truth: the most urgent risk is not missing art or audio. It is the discipline of keeping executable truth, milestone claims, and documentation tightly synchronized as system complexity increases. More on this in Section 5.

---

## 2. Code State Assessment

### 2.1 Snapshot Numbers

| Metric | Value |
|---|---|
| Java source files | 122 |
| Total Java source size | ~841 KB |
| Regression tests (as of 2026-05-08) | 49 / 49 PASS |
| Build health (verified 2026-05-08) | `:core:compileJava` PASS, `:client:compileJava` PASS |
| Simulation package files | 33 |
| Client package files | 19 |
| Core package files | 18 |
| Streaming package files | 11 |
| Largest file: `RegressionTest.java` | ~187 KB |
| Second largest: `PlaytestClient.java` | ~80 KB |

### 2.2 The Core / Simulation Layer — Strong

`core.simulation` is the best part of this codebase. You have 33 files covering:

- Authoritative entity models (`SimPlayer`, `SimEnemy`, `SimBoss`, `SimNPC`, `SimPickup`, `SimShuriken`, `SimPortal`, `SimMovingPlatform`, `SimEcho`)
- A bounded `GameSimulator` coordinator with event drain API
- Full player input state machine (`PlayerInputController`) covering movement, combat, stance, ninjutsu, teleport, wall-slide, animation state
- Boss psychological pattern dispatch (`BossPatternLibrary`) with 5 narrative boss archetypes
- Echo recorder / replay engine (600-frame ring buffer → `SimEcho` evaluation)
- Crafting, inventory, and shop runtime
- World simulation tick (deterministic, seeded, pressure signals)
- Quest ecology engine (opportunity generation from world pressure)
- Co-op scaffolding (proximity detection, revive mechanics)

This is server-authoritative simulation logic of the kind you would build for a multiplayer game. For a single-player narrative Metroidvania it is actually ahead of where it needs to be right now — but it is the correct long-term target and the work is clean.

**Assessment:** Production-grade foundation. The bounded slice discipline (no monolith dumps) is exactly right. Continue.

### 2.3 The Data Contract Layer — Strong

`GameDataContracts.java` (~37 KB) correctly owns:

- Beat / flag / plateau / quest loading and cross-reference validation
- World / faction / settlement simulation contract validation
- NPC schedule resolution
- Dialogue line selection
- Mission template hydration

The fail-fast / warn mode toggle is wired, CI defaults are set, and diagnostics commands work. This is the foundational investment that protects you from the "story breaks on every merge" failure mode that kills narrative games.

**Assessment:** Excellent. No changes needed at this milestone.

### 2.4 The Client Layer — Internal QA Harness, Not Shipping Path

`PlaytestClient.java` (~80 KB) is a Swing Java2D prototype that works well as an internal QA harness. It is not the rendering system for a shipped game.

What it does well:

- Exercises the core systems interactively with keyboard input
- Provides a playable gate for route/combat verification with four authored rooms
- Session evidence logging provides real design telemetry
- Subsystem extractions (`CombatSubsystem`, `TraversalSubsystem`, `UISubsystem`, `MinimapRenderer`) have created the right seams for a future rendering layer swap

What it is not:

- A sprite renderer or animation system
- A real camera system (it translates a `Graphics2D` viewport offset)
- A real audio system (there is none)
- A game loop with fixed timestep decoupled from render rate
- A level loading pipeline with external assets

A player willing to compile and run the project can interact with a prototype. They cannot experience a game. The distinction matters for planning: content investment made against this Swing harness will survive a rendering-engine migration only if the seams stay clean. The subsystem extractions so far mean those seams are in reasonable shape. But the migration has not been scheduled.

**Assessment:** Appropriate for development/QA purposes. Insufficient as a shipping client. Architecture pivot decision required and unrecorded.

### 2.5 The Physics Layer — Imported Baseline Only

`core.physics` has the donor movement profile (`PhysicsConstants`, `PhysicsState`, `PlayableControllerModel`) and collision primitives (`TileType`, `TileRect`, `SpatialHash`). The movement feel is reasonable because the constants were tuned to the donor.

What is absent:

- A physics simulation that owns the game world tick (the PlaytestClient has an ad-hoc integration loop)
- Proper collision resolution (narrow-phase AABB response, not just spatial hash lookup)
- A physics timestep decoupled from frame rate

**Assessment:** Adequate for current QA scope. Will require revision before production.

### 2.6 The ECS Layer — Scaffolded, Unused

`core.ecs` and `core.components` exist (5 + 5 files). The simulation layer does not use them — `SimPlayer`, `SimEnemy` etc. are plain Java objects. The donor ECS was intentionally dropped during Wave 4 to avoid base-class complexity, which was correct at the time.

If the long-term plan is a proper ECS-driven simulation the current entities will need promotion. If not, the ECS scaffold should be retired. There is currently no answer to this question in the roadmap.

**Assessment:** Decision required before M6 deepens simulation entity complexity further.

### 2.7 The Server Layer — Skeleton Only

`java/server/` has 1 file. Correctly scoped for current milestones. Co-op is validation-harness scope and does not need a real server.

---

## 3. Documentation State Assessment

### 3.1 What Is Now Excellent

The core living docs are in the best shape they have been. After the audit pass completed on 2026-05-08:

- `ROADMAP.md` — milestone status accurate; deliverable summaries current; "Next open items" clean.
- `NORTH_STAR_EXECUTION_MATRIX.md` — acceptance test definitions name real harness methods; M3/M6 states accurate.
- `CURRENT_STATE.md` — "What Is Implemented" comprehensive; "Open Issues" genuinely open; "Active Risks" contains only real risks; "Next Actions" contains only open actions; Verification Evidence condensed to one dated block.
- `MIGRATION_MAP.md` — all wave statuses accurate; Wave 6 section added; no stale `(active)` headers.
- `IMPLEMENTATION_BACKLOG.md` — all wave headers correct.
- `CLAUDE.md` — save schema current; milestone table accurate.
- `docs/DOC_MAINTENANCE_PLAN.md` — new (2026-05-08); ownership table, update trigger rules, and staleness prevention established.
- `docs/handover/` — 28+ DESIGN/CODEX pairs, one per delivered option; immutable evidence trail.

### 3.2 Specific Stale Documents (Verified)

The following docs were checked against current code state and found to have material drift:

**`docs/PLAYABLE_CLIENT_PLAN.md` — stale in several places.**

- Line 40 states that `UISubsystem` extraction is "pending" and the `TraversalSubsystem` Codex CLI gate is pending. Both were completed in this session (2026-05-08 evidence in handover docs).
- Phase P1 lists several items as "active" that are now complete.
- This doc should be updated to reflect delivered state, or explicitly flagged as a historical record of the plan rather than current state.

**`docs/START_TO_FINISH_GAME_PLAN.md` — minor staleness.**

- Baseline section still says "manual movement-feel sign-off and later-act scale work remain" — movement sign-off is closed (2026-05-07). Not misleading in context but should be updated.
- Principles and scope sections remain accurate.

**`docs/CANONICAL_ARCHITECTURE_PLAN.md` — minor staleness.**

- Layer 3 describes the client's purpose as "route debugging visibility for internal QA." This is narrower than what the client now does (inventory, shop, crafting, combat, co-op scaffolding, minimap). Not wrong but understated.

**`docs/analysis/`** contains two earlier review documents (`COMPREHENSIVE_GAME_DEV_REVIEW_CLEAN_START.md`, `EXECUTIVE_SUMMARY_CLEAN_START.md`) that are flagged as superseded. Consider moving them to `docs/analysis/archive/` to avoid confusion for anyone reading the folder.

### 3.3 Overall Documentation Score

For a solo developer project at this stage: **8.5/10**, improved from what it was before this session's audit. The remaining improvement available is updating `PLAYABLE_CLIENT_PLAN.md`, resolving the analysis folder clutter, and documenting the rendering engine decision when it is made.

---

## 4. Progress Toward a Fully Featured Game

### 4.1 What a Player Experiences Today

A player who compiles and runs `runPlayableClient` can:

- Move through a four-room authored traversal route
- Use dash and interact to unlock route segments
- Encounter enemies with telegraphed state windows and timed attacks
- See a mission HUD, minimap, and objective progress
- Open inventory, shop, and crafting panels
- Save and reload story state across sessions

What that player cannot experience:

- Visual art of any kind (all geometry is colored rectangles)
- Sound or music of any kind
- A title screen, main menu, or any game-flow UI
- More than one authored route (Act I QA route only)
- Any second-act content (M4 is queued, not started)
- Visual feedback for world simulation state
- A commercially legible opening 30 seconds

This is a functional internal QA harness. A player can test your mechanics. They cannot experience your game.

### 4.2 The Gap Map

The following categories are absent or scaffolded only:

| Category | Current State | Roadmap Coverage | Estimated Solo Effort |
|---|---|---|---|
| Rendering engine (sprites, real camera) | Swing Graphics2D only | **Not in roadmap** | L–XL (2–4 months) |
| Audio (music, SFX) | None | **Not in roadmap** | M (1–2 months) |
| Animation system (sprite sheets, blending) | None | **Not in roadmap** | M–L (2–3 months) |
| Main menu / game flow (title, options, credits) | None | **Not in roadmap** | M (3–6 weeks) |
| Visual character identity (art) | None | **Not in roadmap** | XL (art pipeline, months–years) |
| Act 2+ authored content | Scaffold only | M4 queued | L per act (2–3 months each) |
| Real section-template geometry (not placeholder rectangles) | Stub templates | M6 partial | L (2–3 months) |
| Echo puzzle evaluation logic | SimEcho steps, no evaluator | M6 remaining | S (1–2 weeks) |
| Co-op as real feature | Proximity + revive harness | Not promoted | XL (3–6 months) |
| Platform-specific distribution | Not addressed | Not in roadmap | M (3–6 weeks per platform) |

**Critical observation:** The three largest missing pieces — rendering engine, audio, and animation — are not in the current roadmap at all. They exist in the donor repo (`indie-ninja-adventures` has LibGDX rendering and audio) but have not been scheduled for import. This is not a "missing row" problem. It is a **missing shipping path**. The roadmap is optimized for migration discipline and internal validation; it has not yet been extended to chart the course from prototype to player-facing product.

### 4.3 What the Donor Repos Still Contain (Unimported)

**`indie-ninja-adventures` — Major Unimported Systems:**

| System | Status | Notes |
|---|---|---|
| LibGDX rendering layer (SpriteBatch, TextureAtlas, camera) | Not imported | Single most important missing import |
| Animation system (AnimationController, sprite blending) | Not imported | Depends on rendering layer |
| Audio system (SoundManager, BGM, SFX) | Not imported | Could be imported independently of rendering |
| Asset pipeline (pack/load textures, fonts, audio) | Not imported | Required before rendering |
| `StoryManager.java` (client story presentation) | Wave 5 queued | Unblocked, small effort |
| `MissionUiCoordinator.java` (mission UI coordination) | Wave 5 queued | Unblocked, medium effort |
| `HudRenderer.java` (HUD evolution path) | Wave 5 queued | Depends on rendering layer |
| Remaining `GameSimulator` logic (AI movement, inter-entity physics) | Bounded slice done; remainder deferred | Not scheduled |
| Particle system | Not imported | Visual polish |
| Localization / string table | Not imported | Required before shipping |

**`shadow_ascent_integrated_package` — Major Unimported:**

| System | Status | Notes |
|---|---|---|
| Act 2+ narrative beats / quest chains | Not imported | M4 scope |
| Act 2+ plateau / authored templates | Not imported | M4 scope |
| Emotional mechanics at gameplay level (Yin/Yang rules) | Components imported; gameplay rules absent | Requires design document |

### 4.4 Realistic Completion Estimate

| Phase | Scope | Estimated Solo Effort |
|---|---|---|
| M3 completion (release candidate gate) | Define checklist, pass tests, sign off | 1–2 weeks |
| M4 campaign content (Act 2–3 authored) | 2 additional acts with worldgen-backed content | 3–5 months |
| Rendering engine integration (Wave 7) | LibGDX binding, camera, sprite rendering, asset pipeline | 2–4 months |
| Animation system (Wave 8) | Sprite sheet loading, frame blending, state machine wiring | 1–2 months |
| Audio system (Wave 8 parallel) | BGM, SFX, ambient; asset sourcing is a separate track | 1–2 months |
| Main menu / game flow (Wave 9) | Title, settings, save slot selection, credits | 3–6 weeks |
| Art assets (ongoing parallel track) | Character sprites, environment tilesets, UI art | Variable (months–years) |
| Polish, playtesting, bug fixing | Continuous from first playable build | 3–6 months minimum |

**Honest bottom line:** A fully featured v1.0 release is **18–30 months of solo development** from today's state, assuming continued velocity and art assets available in parallel. The simulation and narrative foundation built so far will hold. The bottleneck is now product surface, not architecture.

---

## 5. Near-Term Risk: Truth Drift

This risk deserves its own section because it is the easiest to overlook and the most dangerous for a solo developer.

**The pattern:** When a project is producing a lot of work at high velocity, there is a temptation to promote milestone evidence (test counts, BUILD SUCCESSFUL entries) as permanent present-tense truth rather than as dated snapshots. Over time, docs drift from executable state and milestone claims become promotional rather than factual.

**Where this has started.** `docs/PLAYABLE_CLIENT_PLAN.md` says UISubsystem extraction is pending and the TraversalSubsystem gate is pending — both are long complete. `docs/START_TO_FINISH_GAME_PLAN.md` says movement sign-off is pending — it closed on 2026-05-07. These are small drifts, but they are exactly the pattern that compounds.

**Why it matters for a solo developer specifically.** A team has redundant memory. A solo developer's project memory is primarily in the docs. If the docs are not kept synchronized with executable truth, the docs become unreliable, which means future decisions are made on incorrect foundations, which compounds into larger drift. The `docs/DOC_MAINTENANCE_PLAN.md` created in this session addresses this directly — but only if followed.

**The rule:** Every milestone-status promotion should require a freshly run, dated, full-gate evidence block (`.\gradlew.bat clean :core:compileJava :client:compileJava ...`). Not a reference to a previous run. Not "tests still pass." A new run, dated. This is already mostly practiced in the handover CODEX docs. The gap is in the living planning docs that get stale between option cycles.

---

## 6. Specific Praise and Concerns

### What This Team Does Very Well

1. **Wave import discipline.** Bringing in only what the current wave requires, compiling and testing before the next slice begins, is the single practice most likely to result in a maintainable codebase at the end. Do not abandon this.

2. **Contract-first narrative architecture.** Having 45 beats, 61 flags, and 7 plateaus in JSON before significant code is a decision that will pay dividends across every act of development. The `GameDataContracts` validation layer is genuinely mature.

3. **Regression harness hygiene.** 49 tests, all passing, covering physics, save compatibility, simulation tick determinism, UI panel behavior, region loading, co-op scaffolding, and echo integration — all in one `RegressionTest.java` without a test framework dependency. This is unconventional but it works, and the gate is always runnable without setup friction.

4. **Documentation as engineering artifact.** The handover DESIGN/CODEX doc pairs, the ownership model, the update trigger rules — these are professional practices that most solo developers skip entirely. You will benefit from them in six months when you return to an area you haven't touched in a while.

5. **Decomposition momentum.** `PlaytestClient` went from ~120 KB monolith to ~80 KB with three substantial subsystems extracted. The seams for a rendering swap are now correct.

### What This Team Should Address

1. **Define the shipping path before deepening migration.** The most urgent missing artifact is not a Wave 7 MIGRATION_MAP row — it is a document that describes how the project gets from current state to a player-facing experience. What rendering layer, what asset pipeline, what milestones look like after M4. Without this, continued Wave 4/5/6 expansion runs the risk of building a progressively more sophisticated simulation for a client that will be thrown away.

2. **Close M3 properly.** M3's exit criteria are defined but unexecuted. A real release-candidate checklist, a freshly run full regression sign-off, and a manual QA route reproducibility gate should be captured as a milestone-closing artifact. M3 is exactly the kind of stability record that gives you confidence before the much larger content investment of M4.

3. **Update `PLAYABLE_CLIENT_PLAN.md`.** It says UISubsystem extraction and the TraversalSubsystem gate are pending. Both are done. Either update it to current state or mark it explicitly as a historical plan document so new readers are not misled.

4. **Record the ECS decision.** Either promote ECS as the path for simulation entities (requires a migration plan) or retire the scaffold. The ambiguity will grow as more simulation entities are added.

5. **Schedule audio earlier than it feels necessary.** Audio is almost always treated as polish and deferred. At that point, retrofitting audio event hooks into every system requires touching code that was never designed with audio in mind. The simulation already emits events (`ENEMY_DEFEATED`, `ECHO_COMPLETED`, `BOSS_PHASE_TRANSITION`, `PORTAL_ACTIVATED`) that are natural audio trigger points. A Wave 8 audio definition now — even if execution is six months away — ensures those hooks stay clean.

6. **Be willing to cut simulation breadth if a vertical slice deadline arrives.** The project currently has deep and widening simulation coverage (co-op, echo puzzles, boss patterns, quest ecology, regional streaming). None of this is playable by an external user yet. At some point — probably when rendering integration begins — a decision will need to be made about what scope gets cut or deferred in favour of a shippable vertical slice. Making that decision early is better than making it under deadline pressure.

---

## 7. Summary Scorecard

| Dimension | Score | Notes |
|---|---|---|
| Simulation foundation | 8/10 | Server-authoritative entities, event drain, deterministic tick — strong |
| Narrative / contract layer | 9/10 | Contract-first, cross-validated, runtime-authoritative — excellent |
| Rendering / visual layer | 2/10 | Swing prototype only; no sprites, no animation, no real pipeline |
| Audio layer | 0/10 | Does not exist |
| Content (authored) | 2/10 | One QA route, one act stub, 13 section templates |
| Save system | 8/10 | V3 envelope, migration matrix, checksum guard — production-grade |
| Physics | 5/10 | Donor constants imported, ad-hoc integration in client; not production loop |
| Test coverage | 9/10 | 49 tests, all pass (2026-05-08), broad coverage, no framework dep |
| Documentation | 8.5/10 | Ownership model, trigger rules, handover trail — professional; 2–3 planning docs stale |
| Roadmap completeness | 5/10 | Current milestones well-defined; rendering, audio, art tracks absent; no shipping path |
| Playable prototype | 5/10 | Compiles, runs, real mechanics — but shapes on a white background |
| Progress toward shippable game | 3/10 | Strong foundation; significant untouched surface area; no shipping path chartered |

---

Next review recommended: when M3 is formally closed and the rendering engine decision is documented. That is the inflection point where the project transitions from "engineering foundation" to "building a visible game."
