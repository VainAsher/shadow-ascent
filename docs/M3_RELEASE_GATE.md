---
doc_type: release_gate
milestone: M3
status: closed
closed_date: 2026-05-08
version_anchor: 0.0.1
---
# M3 Release Gate — Stability and Release Readiness

Formal release-candidate checklist for Milestone 3. All items confirmed green as of 2026-05-08.

## Gate Criteria

| # | Criterion | Evidence | Status |
|---|---|---|---|
| 1 | Regression suite passes in full | `runRegressionTests`: 49/49 PASS (2026-05-08) | PASS |
| 2 | Contract diagnostics clean | `runDataContractDiagnostics`: contracts_loaded=true valid=true beats=45 critical_flags=61 plateaus=7 (2026-05-08) | PASS |
| 3 | Worldgen diagnostics clean | `runWorldgenDiagnostics`: 13 templates, 0 validation issues (2026-05-08) | PASS |
| 4 | Save compatibility — legacy (v0) loads with warning | `testSaveCompatibility` legacy sub-test in regression harness | PASS |
| 5 | Save compatibility — SAVE_V1 migrates to current | `testSaveCompatibility` v1 sub-test; `v1→v2→v3` migration path in `SaveMigrationMatrix` | PASS |
| 6 | Save compatibility — SAVE_V2 migrates to current | `testSaveCompatibility` v2 sub-test; `v2→v3` migrator | PASS |
| 7 | Save compatibility — SAVE_V3 round-trips correctly | `testSaveCompatibility` v3 sub-test; `testSaveV3OverlayPersistence` | PASS |
| 8 | Unsupported future version fails safely | `testSaveCompatibility` unsupported-future sub-test | PASS |
| 9 | Checksum guard detects tampering | `testSaveChecksumGuard` (4 sub-tests): valid load, tampered payload rejected, absent checksum skipped, region overlay checksum round-trip | PASS |
| 10 | CI fail-fast contract mode active | `ci.yml` sets `SHADOWASCENT_CONTRACTS_VALIDATION_MODE=fail_fast`; local default remains `WARN` | PASS |
| 11 | Migration matrix policy documented | `SaveMigrationMatrix.java`; V3 is current; V4+ reserved/unsupported until concrete migrators are implemented | PASS |
| 12 | Route reproducibility (save/load continuity) | M1 QA evidence in `docs/ACT_I_QA_ROUTE.md`; in-session save/load hooks verified during M1 sign-off (2026-05-07) | PASS |

## Full Gate Command

```bash
./gradlew clean :core:compileJava :client:compileJava :server:compileJava \
  runDataContractDiagnostics runWorldgenDiagnostics runWorldSimulationDiagnostics \
  runRegressionTests
```

Output (2026-05-08):
```
BUILD SUCCESSFUL in 45s
runDataContractDiagnostics: contracts_loaded=true valid=true beats=45 critical_flags=61
  plateaus=7 world_regions=3 factions=3 settlements=3
runWorldgenDiagnostics: Section templates loaded: 13, validation issues: 0
runWorldSimulationDiagnostics: Validation issues: none
runRegressionTests: 49/49 PASS
```

## Save Schema Policy at Gate Close

| Version | Status | Policy |
|---|---|---|
| v0 (legacy unversioned) | Migratable | Loads with warning; no migration path — treated as best-effort |
| SAVE_V1 | Migratable | Full v1→v2→v3 migration path in `SaveMigrationMatrix` |
| SAVE_V2 | Migratable | v2→v3 migrator |
| SAVE_V3 | Current | Envelope: `SAVE_V3\|story_state_b64=...\|region_overlays_b64=...\|checksum_sha256=...\|encoding=utf8_base64` |
| SAVE_V4+ | Reserved | Unsupported until concrete migrator implemented before shipping |

## Gate Conclusion

M3 exit criteria are fully met. Milestone is promoted to `completed` as of 2026-05-08.

**Unblocked by M3 close:** M4 campaign content expansion (authored act coverage, optional plateau content with worldgen gates).
