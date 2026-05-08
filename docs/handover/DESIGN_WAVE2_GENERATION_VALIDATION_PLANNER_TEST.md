---
handover_type: design
milestone: Wave2
topic: generation_validation_planner_test
status: in_progress
created: 2026-05-08
---
# Design — Wave 2 GenerationValidationPlannerTest Port

Ports the donor `GenerationValidationPlannerTest` intent into the clean-start regression
harness as a new labelled section `testGenerationValidationPlannerPlanning`.

`GenerationValidationPlanner` and `GenerationValidationReport` already exist in
`core.world.validation`; `ProgressionValidator` and `WorldProgressionGraph` in
`core.world.progression`. No new source files are needed — this is a test-only addition.

---

## Scope

### In scope
- `testGenerationValidationPlannerPlanning` regression section (5 sub-tests)
- Dispatch entry `allPassed &= testGenerationValidationPlannerPlanning();`
- MIGRATION_MAP.md row status `queued` → `done`

### Out of scope
- Layout/socket/anchor inputs to the planner (deferred in planner itself)
- New source files

---

## Layer contract
- Test calls only `core.world.validation.*` and `core.world.progression.*` — no client/server imports.

---

## Files modified

| File | Change |
|---|---|
| `RegressionTest.java` | New section `testGenerationValidationPlannerPlanning`; dispatch entry; 41→42 tests |
| `MIGRATION_MAP.md` | Wave 2 `GenerationValidationPlannerTest` row `queued` → `done` |

---

## Regression tests (5 sub-tests)

### `testGenerationValidationPlannerPlanning`

Build helper: `WorldProgressionGraph.ProgressionNode` factory with `NodeKind`, biome, requires/grants/children/tags, difficultyBand, optional.

1. **valid graph** — hub → nodeA (granted ability) → nodeB (requires granted ability); both non-optional; `validate()` → `report.valid()==true`, `progressionValid()==true`, `issues.isEmpty()`, `repairActions.isEmpty()`
2. **blocked non-optional node** — hub → nodeA (requires "missing_ability"); ability never granted; `validate()` → `valid()==false`, `issues.size()==1`, `issues.get(0).kind().equals("blocked_progression_node")`, `issues.get(0).scopeId().equals("nodeA")`; `repairActions.size()==1`, `repairActions.get(0).tier().equals("regenerate")`
3. **optional blocked node** — same unreachable setup but `optional=true`; `validate()` → `valid()==true` (optional nodes are exempt from validity)
4. **multiple blocked nodes** — hub with no grants; nodeA and nodeB both require "ability_never_granted", both non-optional; `validate()` → `issues.size()==2`, `repairActions.size()==2`
5. **toSnapshot keys** — run valid graph through planner; call `report.toSnapshot()`; verify keys `valid`, `progressionValid`, `issueCount`, `repairActionCount`, `issues`, `repairActions` are all present

---

## Prior test count: 41 → Target: 42/42
