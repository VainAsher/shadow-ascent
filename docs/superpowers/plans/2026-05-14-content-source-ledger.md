# Content Source Ledger

**Purpose:** Identify which existing files should drive missions, plateaus, level inspiration, NPC returns, and authored runtime progression without forcing contributors to re-mine donor repos.

## Source Precedence

1. Clean-start executable/data truth
2. Living GDD guidance
3. Integrated donor package reference
4. `indieniinja` engineering donor reference

## Canonical Runtime-Driving Sources

| Topic | Primary source | Supporting source | Notes |
| --- | --- | --- | --- |
| Plateau order and emotional/mechanical identity | `data/plateaus.json` | `game_design_document/09_level_and_content_plan/01_level_plan_overview/README.md` | canonical plateau sequence and pacing bias live in repo data |
| Critical beat ordering | `data/narrative_beats.json` | `game_design_document/06_story_and_narrative/03_plot_progression/README.md` | beats already map to plateaus, area IDs, flags, and objective prompts |
| Area families and authored area IDs | `data/area_catalog.json` | GDD world/level branches | strongest current source for authored bootstrap and placement targets |
| NPC availability and identity | `data/npc_registry.json` | `game_design_document/15_character_dossiers/` | use dossiers for behavior tone, not as a replacement for runtime IDs |
| Dialogue | `data/dialogue.json` | character dossiers, plot progression docs | dialogue refs and plateau/beat mapping already exist in runtime data |
| Quests and side-content anchors | `data/quests.json` | GDD optional-content and progression branches | main source for authored objective language and side-quest structure |

## GDD Branches To Use Deliberately

| GDD branch | Why it matters |
| --- | --- |
| `game_design_document/09_level_and_content_plan/01_level_plan_overview/` | plateau-by-plateau campaign structure |
| `game_design_document/09_level_and_content_plan/02_main_dungeon_and_region_matrix/` | named dungeon/checkpoint order and purpose |
| `game_design_document/09_level_and_content_plan/04_procedural_room_intent_catalog/` | optional-content and room-intent vocabulary |
| `game_design_document/11_production_scope_and_roadmap/02_vertical_slice_target/` | strongest current emotional proof route |
| `game_design_document/14_appendices/02_region_implementation_checklist/` | per-region implementation checklist |
| `game_design_document/14_appendices/04_room_design_template/` | room-spec template for authored bootstrap follow-up |
| `game_design_document/14_appendices/05_example_room_spec/` | concrete example room framing |
| `game_design_document/15_character_dossiers/` | NPC role-state, mission tone, and interaction behavior |

## Integrated Donor Surfaces Worth Consulting

| Donor source | Use | Import policy |
| --- | --- | --- |
| `..\\shadow_ascent_integrated_package\\data\\elastic_chunk_templates.json` | optional room-intent and plateau-local side-content inspiration | reference only unless adapted into clean-start-owned data |
| `..\\shadow_ascent_integrated_package\\docs\\PLAYABLE_SLICE_ROADMAP.md` | campaign-first planning rationale and tranche structure | reference only |
| `..\\shadow_ascent_integrated_package\\docs\\01_PRODUCT_AND_TECH_SPEC.md` | plateau/system framing and validation intent | reference only |
| `..\\shadow_ascent_integrated_package\\docs\\02_ARCHITECTURE.md` | authored-vs-elastic architecture framing | reference only |
| `..\\shadow_ascent_integrated_package\\docs\\17_EMOTIONAL_VERTICAL_SLICE_REBUILD.md` | emotional-sequence proofing and slice intent | reference only |
| `..\\shadow_ascent_integrated_package\\docs\\21_NEXT_BUILD_RULES.md` | previous bounded-next-pass thinking | reference only |
| `..\\shadow_ascent_integrated_package\\docs\\28_INTEGRATED_SOURCE_ANALYSIS_AND_IMPLEMENTATION.md` | donor import history and rationale | reference only |

## Practical Mapping For Next Work

| Implementation need | Start here | Then consult |
| --- | --- | --- |
| Authored area bootstrap | `data/area_catalog.json` | `data/narrative_beats.json`, GDD level-plan overview |
| Authored NPC placement | `data/npc_registry.json` | `data/narrative_beats.json`, `data/dialogue.json`, character dossiers |
| Mission/objective HUD surfacing | `data/narrative_beats.json` | `data/quests.json`, `data/dialogue.json` |
| Ember Monastery tranche | `data/narrative_beats.json` Ember entries | GDD dungeon matrix, character dossiers for Samson/Marcel/Sophia/Hazel/Roga |
| Room or plateau inspiration | GDD level/content branches | donor `elastic_chunk_templates.json` |
| Transition-gate language | `data/narrative_beats.json` objective prompts and flags | `data/plateaus.json`, GDD progression docs |

## Not Canonical By Themselves

These may influence implementation, but should not override clean-start runtime/data truth on their own:

- free-form donor docs that describe broader future ambition without matching repo data
- GDD branches describing aspirational art/audio tone with no runtime binding yet
- archived analysis docs under `docs/analysis/archive/`

## Maintenance Rule

When new content is authored:

1. add or update the clean-start-owned runtime data first
2. update this ledger only if the canonical source location changes
3. do not point future contributors at an entire donor tree when one clean-start file can answer the question
