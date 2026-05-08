# REVISED ACTION PLAN — 30 DAYS
## Clean-Start Primary: M1 Completion + M2 Progression

> Status note (2026-05-07): this document is a historical plan snapshot and contains date-bound tasks that are now superseded.
> For live execution truth and next tasks, use `docs/CURRENT_STATE.md`, `docs/ROADMAP.md`, `docs/IMPLEMENTATION_BACKLOG.md`, and `docs/NORTH_STAR_EXECUTION_MATRIX.md`.

**Revised Plan Date:** May 6, 2026  
**Repository:** `C:\Users\asher\tester\shadow_ascent_integrated_complete_prototype_package\shadow_ascent_clean_start`  
**Objective:** Complete M1 (Act I QA Gate) + Progress M2 (Campaign Spine)

---

## THIS WEEK (May 6–12): M1 Completion Sprint

### Monday–Tuesday: Strict-Mode Contract Validation Toggle

**What:** Implement runtime policy `GameDataContracts.setStrictMode(boolean)`
- `true`: contract validation failures FAIL BUILD
- `false`: warnings only (dev/debug mode)

**Location:** `java/core/src/main/java/com/shadowascent/core/data/GameDataContracts.java`

**Steps:**

```java
// Add to GameDataContracts class:
private static boolean strictMode = true;  // Default: strict

public static void setStrictMode(boolean strict) {
    strictMode = strict;
}

public static void validate(Map<String, Object> contractData) {
    List<String> errors = runValidation(contractData);
    
    if (!errors.isEmpty()) {
        if (strictMode) {
            throw new ContractValidationException(
                "Contract validation failed:\n" + String.join("\n", errors)
            );
        } else {
            System.err.println("CONTRACT WARNINGS:\n" + String.join("\n", errors));
        }
    }
}

// Update diagnostics to check strict mode:
// If strict=true && errors exist → EXIT 1
// If strict=false && errors exist → WARN but continue
```

**Test:** Add to `java/core/src/test/.../.../GameDataContractsTest.java`

```java
@Test void testStrictMode_FailsOnValidationError() {
    GameDataContracts.setStrictMode(true);
    Map<String, Object> badContract = createInvalidContract();
    
    assertThrows(ContractValidationException.class, 
        () -> GameDataContracts.validate(badContract)
    );
}

@Test void testLenientMode_WarnsOnValidationError() {
    GameDataContracts.setStrictMode(false);
    // Should not throw, but should print warning
}
```

**Build command verification:**
```bash
cd shadow_ascent_clean_start
./gradlew clean :core:compileJava runDataContractDiagnostics runRegressionTests
# Must see: "Contract validation: PASS" in output
# If diagnostics shows errors, strict-mode should FAIL the build
```

**Definition of done:** Strict-mode toggle committed, tests PASS, build fails if contracts invalid in strict mode.

---

### Wednesday–Thursday: Run Act I Playtest + Log Evidence

**Prerequisite:** Build succeeds
```bash
./gradlew clean :core:compileJava :client:compileJava runRegressionTests
```

**Playtest Route (Act I golden path):**

1. **Start game** (command TBD based on client entry point; probably Java main or JAR)
2. **Navigate to Lantern Heights hub**
3. **Talk to named NPCs** (Samson, Sophia, Marcel, Hazel)
   - Verify dialogue triggers
   - No crashes
4. **Trigger first mission** (Act I first objective)
   - Mission marker appears
   - Objective-driven completion (not auto-magic)
5. **Complete mission and return**
   - Mission marked COMPLETE
   - Return to hub
   - No crashes
6. **Verify hub state change** (NPCs reposition, mission state updates)

**Duration:** ~15–20 minutes per run

**Evidence Logging:** Create `docs/reports/m1-playtest-evidence.md`

```markdown
# M1 Act I Playtest Evidence

Date: 2026-05-[DATE]

## Run 1

**Date:** 2026-05-08 14:30  
**Version:** 0.0.1+strict-mode  
**Duration:** 18 minutes  

**Route Checklist:**
- [x] Game starts, no crash
- [x] Navigate to Lantern Heights
- [x] Talk to Samson (dialogue OK)
- [x] Talk to Sophia (dialogue OK)
- [x] Talk to Marcel (dialogue OK)
- [x] Talk to Hazel (dialogue OK)
- [x] First mission triggered
- [x] Mission objectives clear
- [x] Mission completed
- [x] Returned to hub
- [x] Hub state updated (NPC positions changed)

**Issues Found:** None

---

## Run 2

[Repeat checklist]

---

## Run 3

[Repeat checklist]

---

## Summary

- Result: 3/3 PASS
- No known blockers
- Act I golden path LOCKED
- M1 QA Gate criteria satisfied
```

**Do this 3 times** (you can do them across Wed–Thu, or all in one session).

**Definition of done:** 3 passing runs logged, no blockers, evidence committed.

---

### Friday: Commit M1 Completion + Tag Release

```bash
# Verify all changes committed
git status  # Should be clean

# Add evidence
git add docs/reports/m1-playtest-evidence.md
git add java/core/src/main/java/.../GameDataContracts.java
git add java/core/src/test/.../GameDataContractsTest.java

# Commit
git commit -m "feat: M1 completion — strict-mode toggle + Act I playtest evidence

Strict-Mode Toggle:
- GameDataContracts.setStrictMode(boolean)
- Contracts fail build if strict && invalid
- Warnings only in lenient mode
- Tests added for both modes

Act I Playtest Evidence:
- 3 golden-route runs: ALL PASS
- Route: Lanterns Heights → NPCs → Mission → Return
- No known blockers
- Objective-driven completion verified
- Hub state evolution verified

M1 Gate: COMPLETE
- Builds: PASS
- Tests: PASS (strict-mode + regression)
- Playtest: PASS (3/3 runs)
- Diagnostics: PASS (all contracts valid)

Next: M2 Campaign Spine Integration"

# Tag the release for milestone closure
git tag -a v0.1.0-m1-complete -m "M1 Act I QA Gate Complete

Strict-mode contract validation deployed.
Act I golden route playtested and locked.
Ready for M2: Campaign Spine."

# Verify tag created
git tag -l | grep m1-complete
```

**Update** `docs/CURRENT_STATE.md`:

```markdown
## Current Milestone Status (2026-05-10)

- **M0 Foundation:** COMPLETE ✅ (2026-05-06)
- **M1 Act I QA Gate:** COMPLETE ✅ (2026-05-10)
  - Evidence: `docs/reports/m1-playtest-evidence.md` (3/3 runs PASS)
  - Strict-mode toggle deployed and tested
  - All regression tests PASS
  - All contract diagnostics PASS
  - Act I golden route locked

- **M2 Campaign Spine Integration:** ACTIVE (starting 2026-05-13)
  - Next: Wave 2 import (worldgen validation)
  - Next: Extend contracts for Acts II–III
  - Target completion: 2026-06-02
```

**Definition of done:** M1 complete, tagged, evidence committed, CURRENT_STATE updated.

---

## NEXT WEEK (May 13–19): M2 Start + Wave 2 Import

### Monday: Pre-Flight Check
```bash
git log --oneline -5  # Verify M1 commit is there
git tag -l | grep m1  # Verify tag exists
./gradlew runDataContractDiagnostics  # PASS?
./gradlew runRegressionTests  # PASS?
```

If all green: proceed to M2.

### Tuesday–Wednesday: Wave 2 Import Planning

**Goal:** Define what to import from old repo for worldgen support.

**Reference:** `docs/MIGRATION_MAP.md` (Wave 2 section)

**Work:**
1. Create `java/core/src/main/java/com/shadowascent/core/world/sections/` directory structure
2. Read old repo classes:
   - `java/shadowascent/src/main/java/com/indieniinja/world/sections/SectionTemplate.java`
   - `SectionTemplateLibrary.java`
   - `SectionTemplateValidator.java`
3. Create `docs/migration/WAVE2_IMPORT_PLAN.md`:

```markdown
# Wave 2 Import Plan

## Source Classes (old repo)

### SectionTemplate.java
- Current path: indie-ninja-adventures/java/shadowascent/.../SectionTemplate.java
- Target path: clean-start/java/core/.../world/sections/SectionTemplate.java
- Why: Model for worldgen section definitions
- Risk: Medium (depends on external models?)
- Effort: M (medium, might need model adapters)
- Tests: SectionTemplateLibraryTest exists in old repo

### SectionTemplateLibrary.java
- Load section templates from files/registry
- Will need mapping to clean-start's data contract layer
- Consider: Should section templates BE in contracts, or sourced differently?
- Risk: High (integration point unclear)
- Effort: M

### GenerationValidationPlanner.java
- Repair action planning for broken generation
- Might need refactoring to work with clean-start validation layer
- Risk: Medium
- Effort: M

## Decision Questions

1. Should section templates live in `data/sections/` (contract-driven)?
   Or remain in code/JAR (old repo style)?
   
2. Does SectionTemplateValidator follow the same pattern
   as GameDataContracts validator?
   
3. Do we use old repo's ValidationIssue/Report models, or adapt?

## Next Step
- Answer decision questions
- Create bounded imports (don't copy GameSimulator!)
- Add test for each import
- Verify layer boundaries respected
```

4. Review decision questions, document assumptions
5. Update MIGRATION_MAP.md: mark Wave 2 ACTIVE

**Definition of done:** Wave 2 plan documented, assumptions clear, ready to code.

### Thursday–Friday: Execute Wave 2 Import Phase 1

**If the plan is clear, import first 2 classes:**

```bash
# 1. Copy SectionTemplate model from old repo
cp indie-ninja-adventures/java/shadowascent/...SectionTemplate.java \
   shadow_ascent_clean_start/java/core/src/main/java/.../world/sections/SectionTemplate.java

# 2. Copy SectionTemplateValidator from old repo
cp indie-ninja-adventures/java/shadowascent/...SectionTemplateValidator.java \
   shadow_ascent_clean_start/java/core/src/main/java/.../world/sections/SectionTemplateValidator.java

# 3. Add compile checks
./gradlew :core:compileJava  # Should compile without errors; may have unresolved references

# 4. Add test scaffold
vim java/core/src/test/java/.../world/sections/SectionTemplateValidatorTest.java
```

**Test skeleton for validator:**

```java
package com.shadowascent.core.world.sections;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class SectionTemplateValidatorTest {
    
    @Test void testValidate_ValidTemplate_Passes() {
        SectionTemplate valid = /* create valid template */;
        SectionTemplateValidator validator = new SectionTemplateValidator();
        
        validator.validate(valid);  // Should not throw
    }
    
    @Test void testValidate_InvalidTemplate_Fails() {
        SectionTemplate invalid = /* create invalid template */;
        SectionTemplateValidator validator = new SectionTemplateValidator();
        
        assertThrows(ValidationException.class, 
            () -> validator.validate(invalid)
        );
    }
}
```

**Commit (if phase 1 compiles):**

```bash
git add java/core/src/main/java/.../world/sections/
git add java/core/src/test/.../world/sections/

git commit -m "chore: begin Wave 2 import (worldgen validation)

Imported classes:
- SectionTemplate.java (model)
- SectionTemplateValidator.java (validation logic)

Work in progress. Tests pending.

References:
- docs/MIGRATION_MAP.md (Wave 2)
- docs/migration/WAVE2_IMPORT_PLAN.md (decisions)"

# Do not tag yet; this is work-in-progress
```

**Definition of done:** Wave 2 Phase 1 code in place (even if not fully integrated), tests stubbed, plan documented.

---

## WEEKS 3–4 (May 20–Jun 2): M2 Core Progress

### Parallel Tracks

**Track A: Finish Wave 2 Integration**
- Resolve imports and dependencies
- Add test cases
- Verify validation layer works with clean-start patterns

**Track B: Extend Contracts for Acts II–III**
- Expand `data/narrative_beats.json` with Acts II–III beats
- Add new quests to `data/quests.json`
- Add new story flags to `data/story_flags.json`
- Run diagnostics (should validate cleanly)

**Track C: Test Multi-Act Progression**
- Add regression tests for Act transitions
- Test: "Can player move from Act I → Act II given flag states?"
- Test: "Does next-critical-beat resolve correctly across act transitions?"

### Example Task (Track B): Extend Acts II–III Contracts

```bash
# Edit data/narrative_beats.json
vim data/narrative_beats.json

# Add Act II beats alongside Act I (already exist):
{
  "beats": [
    // Act I beats (existing)
    { "id": "act1_lantern_start", "act": 1, "plateau": "lantern_heights", ... },
    
    // Act II beats (new)
    { "id": "act2_summit_shrine_entry", "act": 2, "plateau": "summit_shrine", 
      "required_flags": ["act1_complete"], "sets_flags": ["act2_started"], ... },
    { "id": "act2_summit_shrine_boss", "act": 2, "plateau": "summit_shrine",
      "required_flags": ["act2_started"], "sets_flags": ["act2_summit_cleared"], ... },
    
    // Act III beats (new)
    { "id": "act3_hollow_depths_entry", "act": 3, "plateau": "hollow_depths",
      "required_flags": ["act2_complete"], "sets_flags": ["act3_started"], ... },
  ]
}

# Validate
./gradlew runDataContractDiagnostics
# Expected output includes: "beats=63" (or higher) if you added new ones
# If validation fails, fix contract structure
```

### Example Task (Track C): Add Regression Test for Act Transitions

```java
// java/core/src/test/.../story/MultiActProgressionTest.java

@Test void testProgressFromActIToActII_GivenFlagsSet() {
    // Setup: Player has completed Act I
    GameState state = new GameState();
    state.setStoryFlag("act1_complete", true);
    state.setStoryFlag("act1_lantern_full_route", true);
    
    // Query: What's the next critical beat?
    BeatDefinition nextBeat = contracts.nextCriticalBeat(state);
    
    // Assert: Should be Act II
    assertEquals("act2_summit_shrine_entry", nextBeat.id());
    assertEquals(2, nextBeat.act());
}

@Test void testCannotProgressToActII_WithoutRequiredFlags() {
    // Setup: Player has NOT completed Act I
    GameState state = new GameState();
    state.setStoryFlag("act1_started", true);  // But not complete
    
    // Query: Can we reach Act II beats?
    List<BeatDefinition> reachable = contracts.reachableBeats(state);
    
    // Assert: Act II beats should NOT be reachable
    assertTrue(reachable.stream()
        .noneMatch(b -> b.act() == 2)
    );
}
```

**Commit (periodic, as you complete tracks):**

```bash
git add data/narrative_beats.json data/quests.json data/story_flags.json
git commit -m "feat: extend contracts for Acts II–III

Added beats:
- act2_summit_shrine_entry, act2_summit_shrine_boss
- act3_hollow_depths_entry, act3_hollow_depths_boss
- [add more as you design]

Added quests:
- [list new quest chains]

Added story flags:
- [list new progression flags]

Validation: PASS (runDataContractDiagnostics)
Tests: PASS (MultiActProgressionTest)"

# Later, when Wave 2 is complete:
git add java/core/src/main/java/.../world/sections/
git add java/core/src/test/.../world/sections/

git commit -m "feat: Wave 2 complete (worldgen validation)

Integrated:
- SectionTemplate model
- SectionTemplateValidator logic
- …other Wave 2 classes

Tests: PASS
Validation: PASS
MIGRATION_MAP.md updated

M2 Campaign Spine: In progress
- Multi-act progression tested
- Contracts extended through Act III"
```

---

## MONTH-END CHECKPOINT (June 2, 2026)

### Verification Commands

```bash
cd shadow_ascent_clean_start

# 1. Build clean
./gradlew clean build
# Expected: BUILD SUCCESSFUL

# 2. Contracts valid
./gradlew runDataContractDiagnostics
# Expected: contracts_loaded=true valid=true beats=50+ quests=20+ flags=70+

# 3. Regression tests pass
./gradlew runRegressionTests
# Expected: all checks PASS, including:
#   - Act I golden route completable
#   - Multi-act progression correct
#   - Save versioning backward-compatible

# 4. Code compiles
./gradlew :core:compileJava :client:compileJava :server:compileJava
# Expected: BUILD SUCCESSFUL (no warnings on old repo imports)

# 5. Verify git state
git log --oneline -10
# Expected: M1 completion commit + Wave 2 commit + contract extensions visible

git tag -l | grep -E "v0.1|m[0-9]"
# Expected: v0.1.0-m1-complete present
```

### Success Checklist

- [ ] M0: COMPLETE (scaffold, docs, build)
- [ ] M1: COMPLETE (strict-mode toggle, playtest evidence 3/3 PASS, tagged v0.1.0-m1-complete)
- [ ] M2: ACTIVE (Wave 2 started, contracts extended, multi-act tests added)
- [ ] Version: 0.0.1 → 0.1.0 (M1 tagged release)
- [ ] All tests PASS (strict-mode + regression + Wave 2 imports)
- [ ] Docs CURRENT:
  - [ ] CURRENT_STATE.md (M0/M1 complete, M2 active)
  - [ ] MIGRATION_MAP.md (Wave 2 marked ACTIVE or DONE)
  - [ ] docs/reports/m1-playtest-evidence.md (3 runs logged)
  - [ ] docs/migration/WAVE2_IMPORT_PLAN.md (decisions documented)
  - [ ] Code comments link to contracts (no magic numbers)

### If Behind Schedule

**If M1 not done by May 12:** Debug the issue. Is it:
- Strict-mode toggle complexity? → Simplify to just a boolean flag
- Playtest failures? → Log the blocker, fix it, re-run
- Test coverage? → Cut non-essential tests, focus on regression suite

**If Wave 2 stalled by May 26:** Reduce scope:
- Import SectionTemplate only (skip library, validator for now)
- Test with hard-coded data
- Full Wave 2 becomes Wave 2B (June)

**Core principle:** Finish M1, progress M2, but don't sacrifice M1 stability for M2 speed.

---

## ENFORCED HABITS CHECKLIST

Every day before coding, verify:

### Habit 1: Contract-First
- [ ] Am I adding a feature?
- [ ] Did I define it in a contract (`quests.json`, `narrative_beats.json`, `story_flags.json`)?
- [ ] Did I run `runDataContractDiagnostics` and check for validation errors?
- [ ] Only THEN did I wire it into Java code

### Habit 2: Validation Before Feature
- [ ] Before committing, did I run `runRegressionTests`?
- [ ] Did I run `runDataContractDiagnostics`?
- [ ] Build is clean? (`./gradlew build`)
- [ ] If any fails: don't commit, fix first

### Habit 3: Wave-Based Imports
- [ ] Am I importing from old repo?
- [ ] Is it in MIGRATION_MAP.md (Wave 0–5)?
- [ ] Did I isolate it to a bounded module (not copy-paste monolith)?
- [ ] Did I add tests for just that module?
- [ ] Did I update MIGRATION_MAP.md status?

### Habit 4: Layering Discipline
- [ ] Is my data in contracts? (not hardcoded in code)
- [ ] Is progression logic in Core? (not in Client)
- [ ] Is UI/rendering only in Client? (stateless consumption of Core)
- [ ] No circular dependencies between layers?

### Habit 5: Versioned Everything
- [ ] Did version.json change? → Is it reflected in tag/release?
- [ ] Did save format change? → Is SAVE_v[N] envelope bumped?
- [ ] Did contract schema change? → Is there a migration test?

---

## KEY SUCCESS METRICS (Track Every 2 Weeks)

| Metric | Target | Week 1 | Week 2 | Week 3–4 |
|--------|--------|--------|--------|----------|
| M1 Status | COMPLETE | In progress | COMPLETE ✓ | COMPLETE ✓ |
| Playtest Passes | 3/3 | 0/3 | 3/3 ✓ | 3/3 ✓ |
| M2 Progress | Wave 2 started | 0% | 10% | 40–50% |
| Tests PASS | 100% | 90% | 100% | 100% |
| Build Time | <30s | ~60s | ~40s | <30s |
| Docs Current | Yes | 50% | 80% | 100% |

---

## WHEN YOU'RE STUCK

### "Is this a code problem or a contract problem?"
→ Check CURRENT_STATE.md. If a feature isn't wired, it's a contract problem — add to JSON first, then wire.

### "Should I refactor the ECS?"
→ No. Clean-start doesn't have ECS yet. You're building **story systems**, not physics. Focus.

### "Can I skip the Wave 2 import?"
→ For M1/M2, maybe. But you'll need worldgen soon. Do Wave 2 by end of June.

### "How big should Acts II–III be?"
→ Keep the pattern from Act I. If Act I is 5–7 critical beats, Acts II–III should be similar (7–10 each).

### "When can I play the game?"
→ Once M1 is done (Act I playable). M2 will add more content but core loop stays same.

---

## THE DISCIPLINE THAT MATTERS

This plan works **only if you enforce the 5 habits**:

1. **Data-first** (not code-first)
2. **Validate before shipping** (tests + diagnostics)
3. **Bounded imports** (not monolith copies)
4. **Layering strict** (data → core → client → server)
5. **Version everything** (saves, contracts, code)

Every shortcut you take now costs 2 hours later.

Stay disciplined. The architecture is good because of these rules.

---

## NEXT MONDAY (May 6, 2026)

```bash
cd C:\Users\asher\tester\shadow_ascent_integrated_complete_prototype_package\shadow_ascent_clean_start

# Start M1 completion
# Step 1: Open GameDataContracts.java
vim java/core/src/main/java/com/shadowascent/core/data/GameDataContracts.java

# Step 2: Add strict-mode toggle (as shown above)
# Step 3: Run tests
./gradlew :core:test

# If green: move to playtest (Wed–Thu)
```

Go.
