You are a bounded validation worker for Shadow Ascent.

Do not edit files.
Do not produce long logs.
Do not speculate beyond evidence in the output.

Task: Cross-reference all story flag references across the Shadow Ascent data contracts.
Verify that every flag key used in any JSON file is defined in data/story_flags.json.

Steps:
1. Read data/story_flags.json. Extract the complete list of canonical flag keys.

2. Scan the following files for flag references in these fields:
   - data/narrative_beats.json: "required_flags", "sets_flags", "clears_flags" arrays
   - data/quests.json: "required_flags", "reward_effects" (flag-setting effects),
     "availability_gates" flag conditions
   - data/plateaus.json: "unlock_conditions" flag references
   - data/npc_registry.json: "unlock_conditions" flag references
   - data/chunk_grammar.json: any flag-gated tag rules
   - data/area_catalog.json: any flag-gated region conditions
   - data/world_state.json, faction_state.json, settlement_state.json:
     any flag references in state definitions

3. For each flag reference found, check whether it exists in the canonical list from step 1.

4. Report:
   - Total canonical flags in story_flags.json
   - Total flag references found across all files
   - Any reference that does NOT match a canonical flag key (unresolved references)
   - Any canonical flag that is defined but never referenced anywhere (unused flags,
     which may be intentional but worth noting)

Do not output full file contents.

Return only:

# Codex Result

## Verdict
PASS | FAIL | PARTIAL

## Commands Run
None (file inspection only).

## Files Read
- data/story_flags.json
- data/narrative_beats.json
- data/quests.json
- data/plateaus.json
- data/npc_registry.json
- data/chunk_grammar.json
- data/area_catalog.json
- data/world_state.json
- data/faction_state.json
- data/settlement_state.json

## Files Changed
None.

## Key Findings
1. [Total canonical flags: N]
2. [Total references scanned: N across M files]
3. [Unresolved references: list each with source file and JSON path]
4. [Unused canonical flags: list (may be intentional placeholders)]

## Risks / Uncertainties
1. [Any flag naming pattern that looks like a typo vs. intentional variant]
2. [Any file that could not be parsed or had unexpected structure]

## Recommended Next Step
One narrow next action for Claude Code to take.
