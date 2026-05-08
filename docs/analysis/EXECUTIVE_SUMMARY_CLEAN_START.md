# Executive Summary - Clean Start

Date: 2026-05-07
Primary repository: `shadow_ascent_clean_start`

## Strategic Decision

The clean-start repository remains the correct production base:

- data-contract-first progression architecture,
- clear module boundaries (`core`, `client`, `server`),
- bounded migration from donor repos instead of monolith carry-over.

This keeps long-term campaign scale and save compatibility manageable.

## Milestone Snapshot

- M0 Foundation: complete.
- M1 Act I QA Gate: active (runtime requirements implemented; session evidence logs are captured, manual movement-feel sign-off notes remain pending).
- M2 Campaign Spine Integration: active (contract-backed mission/side-quest progression in place).
- M3 Stability/Release: queued, with persistence hardening and CI contract-gate defaults active.
- M5/M6 Expansion Tracks: M5 active (contract/diagnostics scaffold landed), M6 queued.

## What Is Implemented (Verified)

- Contract-backed mission template loading from `quests.json` + `narrative_beats.json`.
- Side-quest chain runtime gating/completion rules from contract fields (`act`, `plateau`, `required_flags`, `sets_flags`).
- Per-step objective decomposition from quest data (replacing generic placeholders).
- Typed reward effects from explicit schema fields (`type`, `magnitude`, optional `payload`).
- Strict reward payload validation by effect type.
- Contract validation policy toggle:
  - `WARN` (default) or `FAIL_FAST`.
  - Config via `shadowascent.contracts.validation.mode` or `SHADOWASCENT_CONTRACTS_VALIDATION_MODE`.
- Hub transitions directly bound to progression flags/next-critical-beat context.
- Hub dialogue routing moved to contract-backed `dialogue.json` line selection with legacy fallback.
- Save envelope hardening:
  - legacy raw state load support (`v0`),
  - current envelope (`SAVE_V2|story_state_b64=...|encoding=utf8_base64`),
  - concrete `v1 -> v2` forward migration handler,
  - forward-version handling policy with explicit unsupported `v3` branch.
- Wave 2 worldgen validation import:
  - section template model/library/validator/issues,
  - progression validator + graph,
  - generation validation report + planner,
  - donor-authored section datasets imported (`data/worldgen/sections`),
  - worldgen diagnostics entrypoint and donor-parity regression checks.
- CI fail-fast contract defaults:
  - `.github/workflows/ci.yml` sets `SHADOWASCENT_CONTRACTS_VALIDATION_MODE=fail_fast`,
  - local developer ergonomics remain warn-mode by default.
- Wave 4 starter slice:
  - donor-profile `TileType`/`TileRect`/`SpatialHash` primitives imported,
  - regression coverage added for chunked candidate lookup and raycast behavior.
- Wave 5 starter slice:
  - playable-client minimap overlay and authored traversal collision geometry integrated for human QA.

## Verification Evidence

Most recent full check:

```bash
./gradlew clean :core:compileJava :client:compileJava :server:compileJava runDataContractDiagnostics runWorldgenDiagnostics runRegressionTests
```

Observed:

- build/compile: pass,
- data contract diagnostics: pass,
- worldgen diagnostics: pass (10 templates loaded, 0 validation issues),
- regression tests: pass (including strict-mode policy behavior, donor worldgen parity checks, and `v2/v1/legacy` save compatibility behavior).

## Remaining Gaps

1. M1 sign-off evidence is incomplete: manual Act I QA route logs need to be captured and archived.
2. Future save migration handlers beyond `v2` remain to be implemented as schema expands (`v3+`).
3. Playable movement deterministic baselines are calibrated, but manual in-client feel sign-off still needs captured evidence.
4. Further Wave 4/Wave 5 imports are still needed for donor-level combat/ability and chunk-streaming depth.

## Recommended Next Execution Order

1. Complete manual Act I QA evidence passes and attach results to docs.
2. Capture manual playable-client movement-feel evidence against donor behavior.
3. Continue bounded Wave 4/Wave 5 imports for gameplay depth while avoiding monolith carry-over.
4. Implement the next forward save migrator when `SAVE_V3` schema fields are introduced.

## Canonical Documents

For source-of-truth status, use:

- `docs/CURRENT_STATE.md`
- `docs/ROADMAP.md`
- `docs/IMPLEMENTATION_BACKLOG.md`
- `docs/MIGRATION_MAP.md`
- `docs/NORTH_STAR_EXECUTION_MATRIX.md`
