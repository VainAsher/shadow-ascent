You are a bounded authoring worker for Shadow Ascent.

Do not edit files.
Do not produce full file contents.
Do not speculate beyond what the existing data contracts support.

Task: Propose quest contract entries for the HOLLOW_DEPTHS plateau (ACT_2).
No quests currently target this plateau. Your job is to draft quest entries
in the exact schema of data/quests.json for four eligible NPCs:
SHADE_HERMIT, SMITH_MONK, LISTENING_ELDER, ADVOCATE.

Steps — run in order:

1. Read: data/quests.json
   Record the schema structure (fields: id, plateau, npc, title, required_flags,
   steps, reward_effects, availability_gate). Record 1 example quest entry verbatim
   to use as a formatting template.

2. Read: data/plateaus.json
   Find the HOLLOW_DEPTHS entry. Record:
   - allowed_chunk_tags
   - critical_route beat IDs
   - allowed_npcs list

3. Read: data/narrative_beats.json
   Find all beats with plateau="HOLLOW_DEPTHS". Record each beat's:
   - id, sets_flags, required_flags, area_id, route_order

4. Read: data/npc_registry.json
   Find entries for SHADE_HERMIT, SMITH_MONK, LISTENING_ELDER, ADVOCATE.
   Record each NPC's role and eligible_plateaus.

5. Read: data/story_flags.json
   Search for flags containing "hollow", "depth", "dash", "moth", "judge", "glide".
   Record each flag key found — these are the gating flags available for quest steps.

6. Draft quest entries for each NPC following these rules:
   - plateau must be "HOLLOW_DEPTHS"
   - required_flags must only reference flags confirmed in step 3 or step 5
   - area_pool entries must only reference area_ids confirmed in step 3
   - Each quest must have 2-3 steps maximum (M4 scope, not full campaign depth)
   - reward_effects must be small and mechanical (resource, hint, minor unlock) —
     no major story flags unless they are already defined in story_flags.json
   - availability_gate must reference a flag set before the quest NPC appears
     (use beat required_flags from step 3 as gates)
   - Quest IDs follow pattern: quest_hd_[npc_shortname]_[descriptor]

Return only:

# Codex Result

## Verdict
PROPOSED | BLOCKED_BY_MISSING_FLAGS | BLOCKED_BY_MISSING_AREAS

## Files Read
(list files read)

## Files Changed
None.

## Proposed Quest Entries

For each of the 4 NPCs, output one complete quest entry block in valid JSON,
matching the quests.json schema exactly. No commentary inside the JSON block.
Brief rationale (1-2 sentences) before each block explaining the narrative purpose.

## Flag or Area Gaps Found
List any flag keys or area IDs needed by the proposed quests that do not exist
in the current contracts. These must be resolved before the quest entries can be
committed.

## Recommended Next Step
One narrow action to move from PROPOSED to contract-ready.
