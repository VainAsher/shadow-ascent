You are a bounded validation worker for Shadow Ascent.

Do not edit files.
Do not produce long logs.
Do not speculate beyond evidence in the output.

Task: Verify that a newly authored plateau is consistent across all five core data contracts.
This check is run after Step 2 (Copilot JSON authoring) and before Step 5 (regression tests)
in Workflow 1 (New Plateau Addition).

You will need to know the plateau ID to check. If it is not provided in this prompt,
read data/plateaus.json and identify the most recently added entry (highest numeric ID
or last entry in the array).

Steps:
1. Read data/plateaus.json.
   Find the target plateau entry. Note its ID, act assignment, and declared tag ranges.

2. Read data/story_flags.json.
   Verify that every milestone flag declared for this plateau exists as a canonical key.
   Note any declared flags that are missing.

3. Read data/narrative_beats.json.
   Find all beats that reference this plateau ID.
   Verify that each beat's required_flags and sets_flags keys exist in story_flags.json.
   Verify that each beat's plateau_id matches the target plateau.

4. Read data/quests.json.
   Find all quests that reference this plateau ID.
   Verify required_flags, availability_gates, and reward_effects flag references
   all exist in story_flags.json.
   Verify that quest step dependencies form a valid chain (no step depends on a later step).

5. Read data/chunk_grammar.json.
   Verify that the plateau's declared tag ranges are represented in the grammar rules.
   Check for tag leakage: no tag from this plateau's range appears in a rule that belongs
   to a different plateau's grammar block.

6. Read data/npc_registry.json.
   Verify that any NPCs assigned to this plateau have eligible_plateau entries that
   include this plateau ID.

Do not output full file contents.

Return only:

# Codex Result

## Verdict
PASS | FAIL | PARTIAL

## Commands Run
None (file inspection only).

## Files Read
- data/plateaus.json
- data/story_flags.json
- data/narrative_beats.json
- data/quests.json
- data/chunk_grammar.json
- data/npc_registry.json

## Files Changed
None.

## Key Findings
1. [Plateau: ID, act, tag range]
2. [Milestone flags: all canonical / N unresolved — list unresolved]
3. [Narrative beats: N found, all valid / N issues — list issues]
4. [Quests: N found, all valid / N issues — list issues]
5. [Chunk grammar: tag range represented / tag leakage found — list if leakage]
6. [NPC assignments: consistent / N mismatches — list if mismatches]

## Risks / Uncertainties
1. [Any flag that appears in multiple plateau contexts (potential shared-flag collision)]
2. [Any beat or quest that references this plateau but also references flags from another
   act in a way that could break progression ordering]

## Recommended Next Step
One narrow next action for Claude Code to take.
