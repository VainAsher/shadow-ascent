You are a bounded authoring worker for Shadow Ascent.

Do not edit files.
Do not produce full file contents.

Task: Draft the first dialogue pass for all HOLLOW_DEPTHS critical beats and
the echo NPC dialogue pool. These dialogue lines will be added to data/dialogue.json.

Context:
- HOLLOW_DEPTHS is ACT_2. Aen wakes hollowed — Yin and Yang have been taken by the
  Siren of Masks. Movement is heavy. The emotional arc is: grief → weight → survival
  → memory → first sparks.
- SHADE_HERMIT is the only consistent guide. He does not offer false hope.
  He walks beside Aen, not ahead of them.
- SMITH_MONK restores the dash. His register is sparse and practical. He believes
  in action over words.
- STONE_JUDGE is not malevolent — it is indifferent. Its dialogue sounds like a
  system delivering verdicts, not a villain taunting.
- Yin and Yang appear only as memory fragments at beat_final_cutoff_glide.
  They are warm, not accusatory.
- Echo NPCs (SAMSON_ECHO, SOPHIA_ECHO, MARCEL_ECHO, HAZEL_ECHO) are memory
  impressions of Act I characters. They do not condemn Aen. They carry grief
  and care in equal measure.
- Tone rule: do not write inspirational speeches. Write small, true things.
  A line that lands in 8 words beats a paragraph.

Steps — run in order:

1. Read: data/narrative_beats.json
   Find all beats with plateau="HOLLOW_DEPTHS".
   For each beat, record:
   - beat id, objective_prompt, cinematic_intent, dialogue_refs list, npcs list, constraints

2. Read: data/dialogue.json
   Record the schema structure (fields per entry: id, npc, context, beat, text, tags).
   Record 2 example entries verbatim as formatting templates.

3. Read: data/npc_registry.json
   Find entries for: SHADE_HERMIT, SMITH_MONK, STONE_JUDGE, WEIGHTBOUND_OGRE,
   SHATTER_MOTH_QUEEN, SAMSON_ECHO, SOPHIA_ECHO, MARCEL_ECHO, HAZEL_ECHO,
   YIN_MEMORY, YANG_MEMORY.
   Record each NPC's role field — use this to calibrate voice.

4. Draft dialogue entries for each dialogue_ref found in step 1, in this order:

   BEAT: beat_hollowing_intro
   - dlg_hermit_awaken_hollow_one  (SHADE_HERMIT)
   - dlg_aen_spirits_taken         (AEN, internal monologue or spoken aloud)
   - dlg_hermit_walk_dark          (SHADE_HERMIT)

   BEAT: beat_hollow_depths_weight_dialogue
   - dlg_aen_body_heavy            (AEN)
   - dlg_hermit_depression_weight  (SHADE_HERMIT)

   BEAT: beat_weightbound_ogre
   - dlg_hermit_weightbound_warning (SHADE_HERMIT, pre-boss)

   BEAT: beat_dash_restored
   - dlg_smith_monk_still_fight     (SMITH_MONK)

   BEAT: beat_shatter_moth_queen
   - dlg_hermit_shatter_moth_warning (SHADE_HERMIT, pre-boss)

   BEAT: beat_stone_judge
   - dlg_stone_judge_seek_justice   (STONE_JUDGE)
   - dlg_aen_seek_spirits           (AEN)
   - dlg_stone_judge_suffer         (STONE_JUDGE)

   BEAT: beat_final_cutoff_glide
   - dlg_aen_yin_yang_please        (AEN, memory call)
   - dlg_hermit_path_closes         (SHADE_HERMIT)

   ECHO POOL: beat_shadow_echo_fragments (elastic, one line per echo)
   - dlg_echo_samson_alone          (SAMSON_ECHO)
   - dlg_echo_sophia_help           (SOPHIA_ECHO)
   - dlg_echo_marcel_broke          (MARCEL_ECHO)
   - dlg_echo_hazel_worth_saving    (HAZEL_ECHO)

5. For each line, output one complete dialogue entry in valid JSON matching
   the dialogue.json schema exactly.

Dialogue rules:
- Max 2 sentences per line. Prefer 1.
- No character names used in the line itself (they speak, they don't announce themselves).
- STONE_JUDGE lines sound like decrees, not threats.
- SHADE_HERMIT lines are quiet and factual. He has seen this before.
- AEN lines are short — exhaustion limits words.
- Echo lines feel like incomplete memories, not full sentences where possible.
- YIN_MEMORY / YANG_MEMORY lines (if referenced): warm, present, not sorrowful.

Return only:

# Codex Result

## Verdict
COMPLETE | PARTIAL (list gaps) | BLOCKED

## Files Read
(list files read)

## Files Changed
None.

## Drafted Dialogue Entries

Output all drafted entries grouped by beat, each as a valid JSON block
matching the dialogue.json schema. One entry per dialogue_ref.

## Echo Pool Entries

Output the 4 echo pool entries as a separate JSON block.

## Voice Notes
For each NPC drafted, one sentence on the voice register used.
Flag any line where the constraint (tone rules above) was difficult to satisfy.

## Recommended Next Step
One narrow action: either commit these to dialogue.json or identify which
entries need a narrative decision before they can be committed.
