# QUICK REFERENCE - CLEAN START
## Use This for Daily Execution

> Status note (2026-05-07): this is a workflow companion.
> Canonical runtime truth lives in `docs/CURRENT_STATE.md`, `docs/ROADMAP.md`, `docs/IMPLEMENTATION_BACKLOG.md`, `docs/MIGRATION_MAP.md`, and `docs/NORTH_STAR_EXECUTION_MATRIX.md`.

---

## Daily Start Checklist

```bash
cd C:\Users\asher\tester\shadow_ascent_integrated_complete_prototype_package\shadow_ascent_clean_start

# Optional: if git repo is initialized
git status

# Contract diagnostics
.\gradlew.bat runDataContractDiagnostics

# Worldgen diagnostics
.\gradlew.bat runWorldgenDiagnostics

# World simulation diagnostics (M5 scaffold)
.\gradlew.bat runWorldSimulationDiagnostics

# Regression coverage
.\gradlew.bat runRegressionTests

# Read current milestone truth
cat docs/CURRENT_STATE.md
```

If any command fails, fix that before new feature work.

---

## Before Commit Checklist

```bash
.\gradlew.bat clean build
.\gradlew.bat runDataContractDiagnostics
.\gradlew.bat runWorldgenDiagnostics
.\gradlew.bat runWorldSimulationDiagnostics
.\gradlew.bat runRegressionTests
```

Release claim rule:
1. Build passes.
2. Diagnostics pass.
3. Regression suite passes.
4. Docs updated to match implementation.

---

## Current Milestone Snapshot

- `M0`: completed.
- `M1`: active (route runtime implemented; manual movement-feel sign-off still pending).
- `M2`: active (contract-backed campaign spine implemented).
- `M3`: queued (next save migrator pending when schema evolves).
- `M4`: queued (content-scale expansion).
- `M5`: active (world/faction/settlement contract and diagnostics scaffold landed; deterministic tick pending).
- `M6`: queued (regional streaming + mutation + co-op validation harness).

---

## Working Habits

### 1) Contract First
Define progression/world behavior in `data/` before runtime wiring.

### 2) Validate Early
Run diagnostics and regressions before and after each meaningful change.

### 3) Bounded Imports
Use migration waves; do not import monolith donor classes directly.

### 4) Layer Discipline
Data -> Core -> Client -> Server.
No layer shortcuts for progression logic.

### 5) Versioned Persistence
Any save schema change requires migration behavior plus regression coverage.

---

## Current Logical Work Order

1. Capture manual movement-feel sign-off notes against donor behavior.
2. Continue Wave 4 modular simulation imports (slice-based, test-backed).
3. Implement next forward save migrator when `SAVE_V3` fields are introduced.
4. Continue M5 by adding first quest-ecology opportunity generation from simulation pressures.

---

## Common Commands

```bash
# Compile modules
.\gradlew.bat :core:compileJava :client:compileJava :server:compileJava

# Launch playable client
.\gradlew.bat runPlayableClient

# Contract diagnostics
.\gradlew.bat runDataContractDiagnostics

# Worldgen diagnostics
.\gradlew.bat runWorldgenDiagnostics

# World simulation diagnostics
.\gradlew.bat runWorldSimulationDiagnostics

# Regression suite
.\gradlew.bat runRegressionTests
```

---

## Decision Guide

### Should I add this feature now?
1. Is it represented in contracts?
2. Is there a milestone/backlog slot for it?
3. Can I verify it with diagnostics/tests?

If any answer is "no", define the contract/test first.

### Should I import donor code?
1. Is it listed in `docs/MIGRATION_MAP.md`?
2. Is the import bounded and testable?
3. Does it preserve layer boundaries?

If not, slice/refactor before importing.

---

## Evidence Locations

- Playtest session logs: `logs/playtest/playtest_session_*.log`
- Core status and risks: `docs/CURRENT_STATE.md`
- Execution tasks: `docs/IMPLEMENTATION_BACKLOG.md`
- Milestone criteria: `docs/ROADMAP.md`
- North-star execution gates: `docs/NORTH_STAR_EXECUTION_MATRIX.md`
