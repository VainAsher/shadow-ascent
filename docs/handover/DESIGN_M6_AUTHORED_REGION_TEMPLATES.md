---
handover_type: design
milestone: M6
topic: authored_region_templates
status: in_progress
created: 2026-05-08
---
# Design — M6 Authored Region Templates (stub geometry elimination)

Replaces the `[WARN] RegionLoader: no template for ...` stub-geometry fallbacks with
authored section template JSON files for the three missing biome/kind combinations.

---

## Problem

`RegionLoader.load()` calls `library.select(biome, kind, seed)`. When no template
matches, it logs a WARN and falls back to a 1×1 stub. The failing combinations seen
in the regression output are:

| biome | kind | nodes affected |
|---|---|---|
| `lantern` | `region_hub` | `dungeon_forge_terrace_a`, `test_region`, `test_overlay_region` |
| `lantern` | `hub` | `hub_lantern_heights` |
| `hollow` | `dungeon` | `region_hollow_shaft` |

## Solution

Author three new template JSON files under `data/worldgen/sections/`:

| File | biome | kind | footprint |
|---|---|---|---|
| `lantern_region_hub.json` | lantern | region_hub | 4×2 |
| `lantern_hub.json` | lantern | hub | 3×2 |
| `hollow_dungeon.json` | hollow | dungeon | 3×3 |

These follow the same schema as existing templates. Each has a minimal set of
`nodeKinds`, `edgeRules`, `mutableZones`, and `anchors` consistent with their role.

---

## Files added

| File | Purpose |
|---|---|
| `data/worldgen/sections/lantern_region_hub.json` | Covers `lantern`/`region_hub` |
| `data/worldgen/sections/lantern_hub.json` | Covers `lantern`/`hub` |
| `data/worldgen/sections/hollow_dungeon.json` | Covers `hollow`/`dungeon` |

No Java changes required. The `SectionTemplateLibrary` picks up new files automatically.

---

## Regression gate

- `runWorldgenDiagnostics`: section template count rises from 10 → 13; issues remain 0.
- `runRegressionTests`: 49/49 PASS (no new tests added; WARN lines suppressed in existing sections).
- Full gate BUILD SUCCESSFUL.

---

## Prior test count: 49 → Target: 49/49 (no new test — template data change only)
