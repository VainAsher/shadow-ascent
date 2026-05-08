# Data Directory

The data files in this directory are designed to drive an elastic, narrative-state-locked procedural generation system.

Recommended load order:

1. `schemas/narrative_data_schema.json`
2. `plateaus.json`
3. `story_flags.json`
4. `narrative_beats.json`
5. `area_catalog.json`
6. `npc_registry.json`
7. `dialogue.json`
8. `quests.json`
9. `adaptation_rules.json`
10. `chunk_grammar.json`
11. `world_state.json`
12. `faction_state.json`
13. `settlement_state.json`

Runtime rule: select content by current plateau first, then flags, abilities, socket request, recent pacing history, and validation.
