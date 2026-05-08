# REVISED COMPREHENSIVE REVIEW
## Clean-Start as Primary — Better Architecture, Better Habits

> Status note (2026-05-07): this document is a historical deep-dive snapshot and contains superseded milestone details.
> Canonical live runtime status now lives in `docs/CURRENT_STATE.md`, `docs/ROADMAP.md`, `docs/IMPLEMENTATION_BACKLOG.md`, `docs/MIGRATION_MAP.md`, and `docs/NORTH_STAR_EXECUTION_MATRIX.md`.

**Revision Date:** May 6, 2026 (Updated)  
**Decision:** Use `shadow_ascent_clean_start` as the canonical production repository  
**Focus:** Data-contract-first development with enforced modularity

---

## WHY CLEAN-START IS THE RIGHT CHOICE

### The Original Recommendation Was Wrong

I recommended consolidating on `indie-ninja-adventures` because it was "further along." But you were right to override that:

**indie-ninja-adventures** has:
- ✅ Code-first architecture (ECS, physics, rendering all working)
- ❌ Narrative mixed into code (story state hardcoded, progression implicit)
- ❌ Monolithic systems (GameSimulator is XL, hard to slice)
- ❌ Architecture emergent, not planned upfront

**shadow_ascent_clean_start** has:
- ✅ **Data-contract-first architecture** (plateaus, beats, quests in JSON)
- ✅ **Explicit layering** (Data → Core → Client → Server → Validation)
- ✅ **Validation gates built in** (runDataContractDiagnostics, runRegressionTests)
- ✅ **Bounded imports** (Wave 1, 2, 3, 4, 5 with effort/risk estimates)
- ✅ **Better separation of concerns** (no narrative leaking into simulation)
- ✅ **Modularity enforced** (non-import list, governance rules)

**Your instinct was correct:** Starting "clean" with better habits now beats shipping messy code faster.

---

## CURRENT STATE: M0–M2 IN FLIGHT

### M0 Foundation — ✅ COMPLETE (2026-05-06)
- Scaffold complete
- Module boundaries defined (core/client/server)
- Initial docs written
- Build system working

### M1 Act I QA Gate — 🔄 IN PROGRESS (2026-05-06)
- **Done:**
  - Objective-driven mission runtime ✅
  - Contract-backed mission templates ✅
  - Side-quest chain runtime ✅
  - Typed reward effects ✅
  - Save versioning (legacy → v1) ✅
  - Hub state contract-driven ✅
  - Regression test harness ✅
  - Data contract diagnostics ✅

- **Remaining:**
  - Strict-mode contract validation toggle (warn vs. fail-fast)
  - Act I route hardening (playtest evidence)
  - Additional edge case coverage

### M2 Campaign Spine — 🔄 IN PROGRESS (2026-05-06)
- **Done:**
  - Data contracts validate at startup ✅
  - Story state enums and progression scaffolding ✅
  - Next-critical-beat resolution ✅

- **Remaining:**
  - Full beat/flag progression testing
  - Elastic optional content separation
  - Cross-act transition validation

### M3 Stability — ⏱️ QUEUED (Post-M1/M2)
- Will add: save migration matrix, strict-mode toggle, deeper domain validation

---

## ARCHITECTURAL STRENGTH ANALYSIS

### Layer 1: Data Contracts ✅ EXCELLENT
**Current:** plateaus.json, narrative_beats.json, story_flags.json, npc_registry.json, quests.json

**What This Provides:**
- Narrative is authored *once*, reused everywhere
- Runtime validation happens *at load*, not at gameplay
- Contract diagnostics expose misconfigurations before runtime
- Schema evolution planned (see MIGRATION_MAP.md)

**Enforcement Rules:**
- All progression decisions reference a flag or beat in the contract
- No hardcoded story beats in Java code
- Every NPC/quest sync is verified at startup

### Layer 2: Core Runtime ✅ SOLID FOUNDATION
**Current:** GameState, StoryState, Mission, MissionManager, HubManager

**What This Provides:**
- Single source of progression truth
- Objective-driven completion (not auto-magic)
- Versioned save/load with backward compatibility
- Contract-driven hub evolution

**Enforcement Rules:**
- Mission completion gated on objectives met
- Hub transitions driven by beat/flag state
- Save envelope versioned (v1 minimum)
- No client-side progression logic

### Layer 3: Client Runtime ⏳ TO BE IMPORTED
**Gap:** Player-facing UI, rendering, input handling not yet ported from old repo

**Strategy:** Import as bounded modules matching the old repo structure, not monolith

### Layer 4: Server Runtime ⏳ SKELETON ONLY
**Gap:** Authoritative orchestration pattern defined but not implemented

**Strategy:** Mirror core progression rules exactly (design-first before coding)

### Layer 5: Validation & Diagnostics ✅ ACTIVE
**Current:** DataContractDiagnostics, RegressionTests, GameDataContracts validator

**What This Provides:**
- `runDataContractDiagnostics` validates contracts before game starts
- `runRegressionTests` checks scenario/progression paths
- Build FAILS if contracts invalid (strict mode future)

**Enforcement Rules:**
- Every release must have green diagnostics + regression tests
- New contracts must have corresponding diagnostics
- Validation gaps are tracked in CURRENT_STATE as "active risks"

---

## MIGRATION STRATEGY

Instead of "consolidate everything," use **Wave-Based Bounded Imports** from donor repos.

### Wave 0 — COMPLETE ✅
- Data contracts imported (all JSON files from integrated-package)
- Runtime loader/validator created (clean-start code)
- Diagnostics CLI built (clean-start code)

### Wave 1 — COMPLETE ✅
- Mission/story runtime hardening (contract-backed templates)
- Save versioning (legacy → v1 envelope)
- Hub state alignment (beat/flag driven, not hardcoded)

### Wave 2 — NEXT (After M1)
- Worldgen section templates + validation from old repo
- Progression validator from old repo
- Import 6 classes, 4 test suites — bounded, with risk assessment

### Wave 3 — LATER (Post-M2)
- Client-side story/mission UIs from old repo (adapted, not copied)
- HUD and minimap renderers
- Input handling for mission affordances

### Wave 4 — STAGED (Post-M3)
- Simulation systems (SimPlayer, SimEnemy, inventory, boss patterns)
- **EXPLICIT non-import:** Don't copy GameSimulator as monolith; slice first

### Wave 5 — POLISH (Post-core parity)
- Advanced client features (animations, effects, multiplayer handshake)

**Governance:** Update MIGRATION_MAP.md when each wave completes. Validate that imports don't leak across layer boundaries.

---

## CRITICAL DIFFERENCES: CLEAN-START vs. OLD REPO

| Aspect | Old Repo (indie-ninja-adventures) | Clean-Start |
|--------|---|---|
| **Progression Truth** | Code (GameState, hardcoded checks) | **Data (contracts, validated at load)** |
| **Architecture** | Emergent (built systems first) | **Designed (layered, planned)** |
| **Modularity** | Loose (import whole game) | **Enforced (wave-based, bounded)** |
| **Validation** | Manual (tests only) | **Built-in (diagnostics CLI)** |
| **Story/Code Coupling** | High (beats in StoryManager) | **Low (beats in JSON)** |
| **Test-First** | Partial (13 test files) | **Enforced (regression + contract harness)** |
| **Extensibility** | Add code → modify core | **Add contract → validate → wire into core** |

---

## HABITS TO ENFORCE IMMEDIATELY

### Habit 1: Contract-First Progression
**Rule:** No story progression decision happens in Java code without a matching contract entry.

**Example:**
- ❌ BAD: `if (playerLevel > 5) { unlockBoss.true; }`
- ✅ GOOD: `if (storyFlags.contains("act2_started")) { let nextBeat = contracts.nextCriticalBeat(...); }`

**Enforcement:** Code review must ask "Is there a contract for this?"

### Habit 2: Validation Before Feature
**Rule:** Before adding a new mission, quest, or beat:
1. Add to `quests.json` or `narrative_beats.json`
2. Run `runDataContractDiagnostics`
3. Add test case to regression harness
4. ONLY THEN implement runtime behavior

**Example Workflow:**
```bash
# Step 1: Define new quest in data
vim data/quests.json  # Add new_quest_id

# Step 2: Validate
./gradlew runDataContractDiagnostics  # Must pass

# Step 3: Test the progression rule
vim java/core/src/test/.../GameDataContractsTest.java  # Add test case

# Step 4: Wire into runtime
vim java/core/.../MissionManager.java  # Implement mission loading from contract

# Step 5: Test integration
./gradlew runRegressionTests  # Must pass
```

### Habit 3: Wave-Based Imports
**Rule:** Never copy large subsystems. Always:
1. Understand why you're importing (solves what problem?)
2. Slice the source into bounded modules
3. Test the module standalone first
4. Wire into clean-start core
5. Document in MIGRATION_MAP.md

**Example:** Don't import "GameSimulator" (XL, high risk). Instead import:
- "SimPlayer" (player movement/combat)
- "SimEnemy" (enemy behaviors)
- "SimInventory" (item management)
- And compose them with clean-start's orchestration

### Habit 4: Layering Discipline
**Rule:** No cross-layer shortcuts. Data contracts talk to Core only. Core never talks directly to Client-specific code.

```
✅ DATA → CORE → CLIENT (one direction)
❌ CLIENT → DATA (cross-cutting, forbidden)
❌ CORE → CLIENT → CORE (feedback loops, forbidden)
```

**Enforcement:** Code review must check layer boundaries.

### Habit 5: Versioned Everything
**Rule:** Every release increments version. Version changes cascade:
- Code → version.json
- Contracts change → generator_schema_version
- Save format change → SAVE_v[N] envelope
- Migration documented

**Enforcement:** Pre-commit hook checks version sync.

---

## IMMEDIATE RISKS & MITIGATIONS

### Risk 1: M1 Won't Close Without Playtest Evidence
**Severity:** 🔴 HIGH  
**Status:** Blocking M2

**Playtest needed:** Act I first-session route completion (similar to old repo's P0-10)
- Start at Lantern Heights
- Talk to named NPCs, trigger mission
- Complete Act I golden route without blockers
- Evidence: 3+ passing runs logged

**Mitigation:** Schedule playtest THIS WEEK. Don't defer.

### Risk 2: Migration Scope Creep
**Severity:** 🟠 MEDIUM  
**Status:** Ongoing risk

**Problem:** Might import old repo's "everything works" instead of "bounded module"

**Mitigation:** 
- Strict governance in MIGRATION_MAP.md
- Every import must have a "why" and a "risk" column
- Code review gates imports (no PR without MIGRATION_MAP.md update)

### Risk 3: Contract Evolution Breaks Old Saves
**Severity:** 🟠 MEDIUM  
**Status:** Post-M1, before M3

**Problem:** If you change narrative_beats.json, old saves might reference deleted beats

**Mitigation:** 
- Define save schema migration matrix (v1 → v2, etc.)
- Regression test for each migration path
- Don't allow breaking changes to contracts without migration code

### Risk 4: Validation False Negatives
**Severity:** 🟡 LOW  
**Status:** Known gap

**Problem:** Current diagnostics validate structure, not semantics (e.g., "is this NPC actually reachable in this beat?")

**Mitigation:** Add deeper domain checks post-M1:
- Reachability analysis (can player reach this NPC at this beat?)
- Dependency checks (required flags exist?)
- Cycle detection (circular quest prerequisites?)

---

## 30-DAY MILESTONE

### Week 1 (May 6–12): M1 Completion
**Goal:** Finish strict-mode toggle + playtest evidence

**Tasks:**
- [ ] Implement contract validation strict-mode toggle (`GameDataContracts.setStrictMode(true)`)
- [ ] Run Act I playtest 3 times (golden route)
- [ ] Log evidence to `docs/reports/m1-playtest-evidence.md`
- [ ] All regression tests PASS
- [ ] All data contract diagnostics PASS

### Week 2 (May 13–19): M1 Sign-Off + M2 Start
**Goal:** Lock M1, begin M2 campaign spine

**Tasks:**
- [ ] Review playtest evidence; declare M1 COMPLETE if 3+ passes
- [ ] Begin Wave 2 import (worldgen validation)
- [ ] Start designing Act II contract extensions (new beats, flags, quests)
- [ ] Update MIGRATION_MAP.md: mark Wave 1 DONE, Wave 2 ACTIVE

### Week 3–4 (May 20–Jun 2): M2 Core Progress
**Goal:** Multi-act progression tested, contract-driven

**Tasks:**
- [ ] Wave 2 import: SectionTemplate, SectionTemplateValidator classes
- [ ] Extend narrative_beats.json for Acts II–III
- [ ] Add regression tests for multi-act progression
- [ ] Update CURRENT_STATE.md with evidence

---

## MONTH-END (June 2) CHECKLIST

- [ ] M0: COMPLETE (scaffold, docs, build)
- [ ] M1: COMPLETE with evidence (playtest, strict-mode on, all tests pass)
- [ ] M2: ACTIVE with Wave 2 half-done (worldgen import progressing)
- [ ] Version: 0.1.0 (released, M0+M1 stable)
- [ ] All docs CURRENT (CURRENT_STATE.md, MIGRATION_MAP.md, ROADMAP.md updated)
- [ ] Zero active TODOs in code (all use contracts or have issues filed)

---

## THE REAL ADVANTAGE

You chose correctly because **better architecture now saves rework later**.

If you'd used the old repo (code-first):
- Acts I–IV shipped
- Realized: "Story beats are hardcoded, can't enable modding"
- Rework: Extract beats to JSON, rewrite progression layer
- 2–3 weeks thrown away

By choosing clean-start (contract-first):
- All progression is data, code is just orchestration
- Future: Add new beats without touching Java
- Modding ready: Users can add custom quests via contract extensions
- Modular: Swap rendering engines without touching story logic

**The cost:** Takes longer to reach "first playable" (maybe 1–2 weeks more)  
**The benefit:** Reach "maintainable, extensible campaign" instead of "working but bedrock is code"

---

## GOING FORWARD: MIND THE LAYERING

Every time you're about to add a feature, ask yourself:

```
Is this a DATA question (what should happen)?
  → Contract (quests.json, narrative_beats.json)
  → Validate with diagnostics
  → Wire into Core

Is this a LOGIC question (how should it happen)?
  → Core runtime (GameState, MissionManager, StoryState)
  → Test with regression harness
  → No product-critical logic client-side

Is this a PRESENTATION question (how does it look)?
  → Client runtime (UI, rendering)
  → Consume Core state, don't drive progression
  → Can be swapped without touching data

Is this a PERSISTENCE question (how do we save)?
  → StoryState + save envelope
  → Versioned (v1 minimum)
  → Migration path documented
```

This discipline makes all the difference.

---

## DOCUMENT SUMMARY

This is the analysis for **clean-start as primary**. Compare to old repo (now reference/donor):

**Your Repositories:**
- `shadow_ascent_clean_start` ← **PRIMARY** (data-first, layered, modular)
- `indie-ninja-adventures` → REFERENCE (worldgen, simulation source)
- `integrated-package` → REFERENCE (narrative data already imported)

**Your Decision Is Right:** Better foundation beats faster shipping.

Now execute it with discipline.
