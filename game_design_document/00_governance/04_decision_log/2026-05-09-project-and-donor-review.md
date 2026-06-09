# 2026-05-09 Project And Donor Review

## Scope Of Review
This review compared three sources:

1. `shadow_ascent_clean_start`
2. `..\shadow_ascent_integrated_package`
3. `..\shadow_ascent_integrated_package\reference\indieniinja_source_reference`

The goal was not to restate every file. The goal was to identify what the projects already prove strongly enough to seed the new GDD without inventing unsupported design detail.

## High-Confidence Truths

### Product Direction
- The clean-start repo consistently describes the game as a **full-campaign narrative Metroidvania**.
- The long-term trajectory is broader than a strict Metroidvania: the project explicitly aims toward a **systemic open-world narrative RPG architecture** after the campaign spine is hardened.
- The clean-start repo is the canonical production destination. Donor repos are references, not release candidates.

### Current Playable Reality
- A human-playable client exists.
- The player can move, jump, wall-jump, dash, attack, interact, save, load, and route through a bounded authored slice.
- The current playable surface is still internal-quality rather than market-ready: placeholder rendering, no finished art pass, and no finished audio stack.

### Narrative Shape
- The authored campaign currently resolves around plateau progression:
  - `LANTERN_HEIGHTS`
  - `SUMMIT_SHRINE`
  - `HOLLOW_DEPTHS`
  - `EMBER_MONASTERY`
  - `WINDING_SKYROAD`
  - `MIRROR_SUMMIT`
  - `BEACON_CLIFF`
- The protagonist is `AEN`.
- `YIN` and `YANG` are present companions before the scripted loss and then shift into memory/star motifs.
- `VEIL_MAIDEN` / `SIREN_OF_MASKS` is an early antagonist.
- `HOLLOW_REFLECTION` is explicitly reserved as the final release boss.

### Mechanical Identity
- The movement model is donor-calibrated and currently includes platforming verbs expected of a Metroidvania.
- Progression is strongly tied to restored or gained traversal/combat capability.
- Save/load, contract validation, and regression evidence are treated as core engineering features, not afterthoughts.

## Donor Contributions

### Integrated Narrative Donor
- Provides most of the campaign story structure, beats, flags, quest chains, NPC registry, plateau definitions, and area families.
- Gives the clearest evidence for setting, story progression, named locations, major characters, and emotional arc.

### Indieniinja Engineering Donor
- Provides the deeper simulation, worldgen, boss-pattern, UI-pattern, and rendering/audio reference surface.
- Shows that the intended game is broader than the current QA harness and that a more production-grade client path exists.
- Also shows what has **not** yet been fully imported into clean-start: major rendering, animation, audio, and asset-pipeline systems.

## Important GDD Conclusions

### What Can Be Filled Now
- working title and document scope
- genre and product framing
- current player model
- core loop
- high-level objective and progression structure
- world / plateau map
- main cast and major enemy list
- current implementation truth for movement, saving, and route structure

### What Should Stay Explicitly Unresolved
- commercial audience proof and demographic claims
- final art style
- final music language
- exact economy model for shipped content
- exact cutscene cadence
- final menu / options surface
- final character backstories beyond what the current contracts imply

## Risks For GDD Accuracy
- The repo has strong internal truth, but some planning docs are historical snapshots. When a planning note conflicts with `docs/CURRENT_STATE.md`, prefer `CURRENT_STATE.md`.
- The donor repos imply a larger shipping surface than the current build implements. The GDD should label those items as direction, not current fact.
- The current product has enough narrative structure to document plot and world, but not enough market evidence to overstate audience fit.

## Recommended Next Fill Order
1. `02_game_overview`
2. `03_gameplay`
3. `06_story_and_narrative`
4. `08_game_world`
5. `07_characters`
6. `04_mechanics`
7. `05_graphics_and_audio`

## Primary Source Basis
- `README.md`
- `docs/CURRENT_STATE.md`
- `docs/START_TO_FINISH_GAME_PLAN.md`
- `docs/MIGRATION_MAP.md`
- `data/plateaus.json`
- `data/narrative_beats.json`
- `data/area_catalog.json`
- `data/npc_registry.json`
- `data/quests.json`
- `..\shadow_ascent_integrated_package\README.md`
- `..\shadow_ascent_integrated_package\README_EXPANDED_NARRATIVE_REALISATION.md`
- `..\shadow_ascent_integrated_package\reference\indieniinja_source_reference\`

## Follow-Up Canonical Narrative Input
On 2026-05-09, the user supplied a stronger story thesis for the project under the framing of **Shadow Ascent / The Hollowed Ninja**. That input clarified several points that should now be treated as canonical GDD direction unless later revised:

- Aen starts already more than whole because he carries both Yin and Yang.
- The real flaw is emotional misrecognition: praise mistaken for love, being chosen mistaken for safety, and isolation mistaken for destiny.
- The campaign spine is explicitly emotional:
  `home -> praise -> isolation -> collapse -> numb survival -> grief -> support -> rebuilding -> ascent -> release -> future`
- The world map should be treated as Aen's inner life turned into level design.
- The Siren of Masks causes the wound, but the Hollow Reflection is the true final enemy.
- The ending is stronger when Yin and Yang remain stars and Aen becomes whole without forcing reversal.
