# Character Dossiers Design

## Purpose
Add a dedicated character-dossier branch to the GDD so major characters are documented by what they do in play, how they affect systems, and what the player learns from them, not only by symbolic meaning.

## Problem
The current `07_characters/` branch explains what characters represent, but it does not consistently describe:
- what each character concretely does in scenes, hubs, quests, or encounters
- how that behavior changes gameplay, progression, or player understanding
- which systems each character depends on
- what is lost if a character is reduced, cut, or underwritten

This makes the cast emotionally legible but operationally weak.

## Design Goals
- preserve the current cross-character summaries in `07_characters/`
- add a stable place for per-character design briefs
- force each major character to connect symbolic role to gameplay consequence
- keep the structure consistent with the rest of the GDD: root -> branch -> topic folder, each with `README.md` and `INDEX.md`
- allow the first wave of major characters to be fully seeded while leaving room for lighter support-character dossiers later

## Non-Goals
- replacing the existing `07_characters/` branch
- writing implementation-engine details for every character system
- forcing every minor NPC to have the same depth as Aen, the Siren, or Hollow Reflection
- moving boss-combat template material out of the appendices

## Recommended Structure
Add a new top-level branch:

- `game_design_document/15_character_dossiers/`

This branch sits alongside `07_characters/` rather than replacing it.

### Why A Separate Branch
`07_characters/` remains valuable as the synthesis layer:
- main cast summary
- supporting cast summary
- cross-cutting topics like relationships, personality, and appearance

The new branch becomes the execution layer:
- one folder per named character
- one canonical operational brief per character
- direct links between story role and gameplay effect

## Branch Layout
Top-level files:
- `game_design_document/15_character_dossiers/README.md`
- `game_design_document/15_character_dossiers/INDEX.md`

First-wave dossier folders:
- `01_aen/`
- `02_yin/`
- `03_yang/`
- `04_veil_maiden_siren_of_masks/`
- `05_hollow_reflection/`
- `06_samson/`
- `07_sophia/`
- `08_marcel/`
- `09_hazel/`
- `10_mentor_roga/`

Second-wave dossier folders:
- `11_instructor_tai/`
- `12_merchant_rilu/`
- `13_smith_jenro/`
- `14_shade_hermit/`
- `15_smith_monk/`
- `16_listening_elder/`
- `17_advocate/`
- `18_hearth_brother/`
- `19_brother_kai/`
- `20_brother_len/`
- `21_brother_ash/`
- `22_old_man_riku/`
- `23_lantern_kid/`

Optional later wave if enemy-characters need the same treatment:
- `24_weightbound_ogre/`
- `25_shatter_moth_queen/`
- `26_stone_judge/`

Every folder in this branch gets:
- `README.md`
- `INDEX.md`

## Dossier README Template
Each major dossier `README.md` should follow the same section order.

### 1. Core Identity
One-sentence statement of who the character is in the game.

### 2. Narrative Function
What role they serve in Aen's arc and the campaign structure.

### 3. What They Represent
Short symbolic summary only. This section must stay concise.

### 4. How They Express That In Play
What the character actually does:
- scenes
- hub presence
- missions
- encounters
- training
- dialogue behavior
- gating or world-state behavior

### 5. Gameplay Impact
What changes for the player because of this character:
- traversal access
- upgrade flow
- map clarity
- combat learning
- safe-space function
- quest progression
- ending tone support

### 6. Player Learning
What the player is meant to understand through interacting with the character.

### 7. Act-by-Act Presence
How the character appears, disappears, echoes, returns, trains, or transforms across the campaign.

### 8. System Dependencies
Which GDD systems the character touches:
- dialogue
- quest state
- hub state
- traversal or ability gating
- combat expression
- save flags
- map logic
- ending-state logic

### 9. Failure Mode
What the game loses if this character is underwritten, misused, or cut down too far.

### 10. Source Basis
Repo-backed truth, donor signal, user-authored canon, or studio-pack clarification used for the dossier.

## Dossier INDEX Template
Each dossier `INDEX.md` should be a checklist and review surface rather than a prose brief.

### Required Checklist
- [ ] Core identity is stated clearly
- [ ] Narrative function is explicit
- [ ] Symbolic meaning is present but concise
- [ ] Observable behaviors are listed
- [ ] Gameplay consequences are explicit
- [ ] Linked quests, encounters, or tutorials are named
- [ ] Act-by-act state changes are tracked
- [ ] Related systems are named
- [ ] Overlap with nearby characters is controlled
- [ ] Failure mode is documented

### Drift Risks
Each dossier should also include 1-2 dossier-specific drift risks, for example:
- the character becomes symbolic-only and stops affecting play
- the character becomes a reward dispenser instead of a relationship-bearing mechanic

## First-Wave Content Standard
The first wave should be fully seeded, not just scaffolded.

### Full dossiers now
- Aen
- Yin
- Yang
- Veil Maiden / Siren of Masks
- Hollow Reflection
- Samson
- Sophia
- Marcel
- Hazel
- Mentor Roga

These characters are core enough that their gameplay and narrative coupling should be explicit immediately.

## Second-Wave Content Standard
The second wave should be created now but can begin as lighter operational briefs.

Minimum expectation for second-wave dossiers:
- core identity
- narrative function
- 2-4 concrete behaviors
- gameplay impact
- system dependencies
- failure mode

This keeps the branch complete without pretending every support figure has the same design weight as the primary cast.

## Cross-Linking Rules
- `07_characters/` remains the summary layer and should link into `15_character_dossiers/`
- dossier files should link back to `07_characters/` when a cross-cast view matters
- boss-related dossiers should reference `14_appendices/03_boss_design_template/`
- world or quest-heavy characters should point into `08_game_world/` or `09_level_and_content_plan/` where needed

## Authoring Rule
No dossier may stop at "what they represent." Every major character must answer:
- what do they do
- where do they show up
- what does that change for the player
- what breaks if they are weakly implemented

## Risks
- If dossiers duplicate `07_characters/` instead of deepening it, the branch adds noise instead of clarity.
- If every minor NPC gets full primary-character treatment immediately, the branch will become hard to maintain.
- If gameplay impact is not made explicit, the new branch will inherit the same weakness as the old one.

## Recommended Implementation Sequence
1. Create `15_character_dossiers/` root, `README.md`, and `INDEX.md`.
2. Create all first-wave and second-wave dossier folders with `README.md` and `INDEX.md`.
3. Fully seed the first-wave dossiers.
4. Add lighter first-pass content to second-wave dossiers.
5. Update `game_design_document/INDEX.md` and `README.md` to include the new branch.
6. Update `07_characters/README.md` and `07_characters/INDEX.md` to clarify summary-layer versus dossier-layer responsibilities.
7. Add links from key summary files into the new dossiers.

## Acceptance Criteria
- The GDD has a new top-level `15_character_dossiers/` branch.
- Every dossier folder includes both `README.md` and `INDEX.md`.
- First-wave dossiers explicitly connect symbolic meaning to behavior and gameplay impact.
- Second-wave dossiers exist and are operationally useful, even if lighter.
- `07_characters/` remains useful instead of being replaced or orphaned.
- The character branch now answers how characters impact play, not just what they mean.
