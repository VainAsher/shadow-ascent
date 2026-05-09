You are a bounded inspection worker for Shadow Ascent.

Do not edit files.
Do not produce full file contents.
Do not speculate beyond evidence in the data.

Task: Audit the SUMMIT_SHRINE plateau to establish what M4 content decisions are needed
before authoring can begin. Report only evidence found in the data contracts.

Steps — run in order, recording the result of each:

1. Read: data/plateaus.json
   Find the SUMMIT_SHRINE entry. Record:
   - plateau ID, act assignment, tag ranges, any linked beats or flags.

2. Read: data/narrative_beats.json
   Search for beats that reference SUMMIT_SHRINE (by ID or plateau tag).
   Record: beat IDs found, their milestone_flag, unlock_condition, and act.
   Record: count of beats with no plateau reference (orphan beats available for assignment).

3. Read: data/story_flags.json
   Search for flags that contain "summit" or "shrine" in their key.
   Record each flag key found.

4. Read: data/quests.json
   Search for quests with plateau="SUMMIT_SHRINE" or area referencing summit/shrine.
   Record: quest IDs found, their act, required_flags, steps count.

5. Read: data/npc_registry.json
   Search for NPCs with eligible_plateaus including SUMMIT_SHRINE.
   Record: NPC IDs and roles that could appear on SUMMIT_SHRINE.

6. Read: data/chunk_grammar.json
   Search for grammar rules that reference summit or shrine tags.
   Record: any tag rules applicable to SUMMIT_SHRINE sections.

7. Cross-reference: For M4, a plateau needs at minimum:
   - At least 1 narrative beat with this plateau in its unlock_condition or act context
   - At least 1 quest with matching plateau or area_pool entries
   - At least 1 eligible NPC
   - Story flags to gate entry and completion
   - Section template tags that match the chunk grammar
   For each category, mark: PRESENT / MISSING / PARTIAL.

Do not output full file contents or lists longer than 10 items per section.

Return only:

# Codex Result

## Verdict
READY_TO_AUTHOR | NEEDS_DECISIONS | BLOCKED

## Files Read
- data/plateaus.json
- data/narrative_beats.json
- data/story_flags.json
- data/quests.json
- data/npc_registry.json
- data/chunk_grammar.json

## Files Changed
None.

## SUMMIT_SHRINE Current State
| Category | Status | Evidence |
|---|---|---|
| Plateau definition | PRESENT/MISSING | [one line] |
| Narrative beats | PRESENT/MISSING/PARTIAL | [count and IDs] |
| Story flags | PRESENT/MISSING/PARTIAL | [flag keys found] |
| Quests | PRESENT/MISSING/PARTIAL | [quest IDs or none] |
| Eligible NPCs | PRESENT/MISSING/PARTIAL | [NPC IDs or none] |
| Chunk grammar tags | PRESENT/MISSING/PARTIAL | [tag rules or none] |

## Authoring Decisions Required
1. [Decision — what content gap must be decided before authoring can begin]
(list only decisions that are MISSING or PARTIAL — omit PRESENT items)

## What Can Be Authored Now (without decisions)
1. [Any piece of content that could be started with existing data]

## Recommended Next Step
One narrow next action to unblock the largest M4 gap for SUMMIT_SHRINE.
