# Character Dossiers Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a new `15_character_dossiers/` branch to the GDD, seed full first-wave character dossiers plus lighter second-wave dossiers, and update the existing character summaries so the GDD explains what characters do in play and how they affect gameplay.

**Architecture:** Keep `07_characters/` as the cast-summary layer and introduce `15_character_dossiers/` as the per-character operational layer. Each dossier folder contains a prose `README.md` and a checklist-driven `INDEX.md`, with the first-wave dossiers fully written and the second-wave dossiers created as lighter operational briefs. Root and character-branch navigation are then updated to cross-link the new structure.

**Tech Stack:** Markdown documentation, PowerShell filesystem verification, ripgrep content checks, Git for focused documentation commits

---

### Task 1: Create The `15_character_dossiers` Root Branch

**Files:**
- Create: `game_design_document/15_character_dossiers/README.md`
- Create: `game_design_document/15_character_dossiers/INDEX.md`
- Modify: `game_design_document/README.md`
- Modify: `game_design_document/INDEX.md`

- [ ] **Step 1: Write the root branch README content**

```md
# Character Dossiers

## Purpose
Provides one dedicated operational design folder per major character so the GDD explains what each character does in play, how they affect systems, and what the player learns from them.

## Why This Branch Exists
`07_characters/` remains the cast-summary layer. This branch is the execution layer for named-character briefs that connect symbolic meaning to gameplay consequence.

## What Belongs Here
- one folder per major named character
- concrete behavior and encounter roles
- gameplay impact and system dependencies
- act-by-act presence and failure modes
```

- [ ] **Step 2: Write the root branch INDEX content**

```md
# Character Dossiers Index

## Tier 1 Dossiers
- `./01_aen/` - protagonist, movement/recovery core
- `./02_yin/` - stillness, absence, star-form guidance
- `./03_yang/` - courage, absence, star-form guidance
- `./04_veil_maiden_siren_of_masks/` - praise-as-possession manipulator
- `./05_hollow_reflection/` - true final enemy and mirrored self
- `./06_samson/` - brotherhood and combat honesty
- `./07_sophia/` - truth, mapping, and orientation
- `./08_marcel/` - repair, reinforcement, and forge consequence
- `./09_hazel/` - warmth, safe return, and Beacon preparation
- `./10_mentor_roga/` - endurance and wall-cling mastery

## Tier 2 Dossiers
- `./11_instructor_tai/`
- `./12_merchant_rilu/`
- `./13_smith_jenro/`
- `./14_shade_hermit/`
- `./15_smith_monk/`
- `./16_listening_elder/`
- `./17_advocate/`
- `./18_hearth_brother/`
- `./19_brother_kai/`
- `./20_brother_len/`
- `./21_brother_ash/`
- `./22_old_man_riku/`
- `./23_lantern_kid/`

## Authoring Notes
- Keep `07_characters/` as the summary branch.
- Use dossiers to answer what a character does, where they appear, and what changes for the player.
- Keep boss-combat template detail in `../14_appendices/03_boss_design_template/`.
```

- [ ] **Step 3: Update the root GDD README to mention the new branch**

Add this section near the existing extended branches text in `game_design_document/README.md`:

```md
- character dossiers

These dossiers exist so the cast is documented by concrete in-play behavior and gameplay consequence, not only by symbolic meaning.
```

- [ ] **Step 4: Update the root GDD INDEX to include the new branch**

Add this bullet under `## Extended Studio-Pack Branches` in `game_design_document/INDEX.md`:

```md
- `./15_character_dossiers/` - per-character operational briefs linking symbolism to gameplay effect
```

- [ ] **Step 5: Verify the root branch files exist and root navigation mentions the new branch**

Run: `Test-Path "game_design_document/15_character_dossiers/README.md"; Test-Path "game_design_document/15_character_dossiers/INDEX.md"; rg -n "15_character_dossiers" game_design_document/README.md game_design_document/INDEX.md`

Expected:
- first two commands print `True`
- `rg` returns matches from both root files

- [ ] **Step 6: Commit**

```bash
git add game_design_document/README.md game_design_document/INDEX.md game_design_document/15_character_dossiers
git commit -m "docs: add character dossiers branch"
```

### Task 2: Scaffold Tier 1 Dossier Folders And Shared Template

**Files:**
- Create: `game_design_document/15_character_dossiers/01_aen/README.md`
- Create: `game_design_document/15_character_dossiers/01_aen/INDEX.md`
- Create: `game_design_document/15_character_dossiers/02_yin/README.md`
- Create: `game_design_document/15_character_dossiers/02_yin/INDEX.md`
- Create: `game_design_document/15_character_dossiers/03_yang/README.md`
- Create: `game_design_document/15_character_dossiers/03_yang/INDEX.md`
- Create: `game_design_document/15_character_dossiers/04_veil_maiden_siren_of_masks/README.md`
- Create: `game_design_document/15_character_dossiers/04_veil_maiden_siren_of_masks/INDEX.md`
- Create: `game_design_document/15_character_dossiers/05_hollow_reflection/README.md`
- Create: `game_design_document/15_character_dossiers/05_hollow_reflection/INDEX.md`
- Create: `game_design_document/15_character_dossiers/06_samson/README.md`
- Create: `game_design_document/15_character_dossiers/06_samson/INDEX.md`
- Create: `game_design_document/15_character_dossiers/07_sophia/README.md`
- Create: `game_design_document/15_character_dossiers/07_sophia/INDEX.md`
- Create: `game_design_document/15_character_dossiers/08_marcel/README.md`
- Create: `game_design_document/15_character_dossiers/08_marcel/INDEX.md`
- Create: `game_design_document/15_character_dossiers/09_hazel/README.md`
- Create: `game_design_document/15_character_dossiers/09_hazel/INDEX.md`
- Create: `game_design_document/15_character_dossiers/10_mentor_roga/README.md`
- Create: `game_design_document/15_character_dossiers/10_mentor_roga/INDEX.md`

- [ ] **Step 1: Create the shared Tier 1 INDEX template**

Use this exact `INDEX.md` template in each Tier 1 folder, changing only the title and dossier-specific drift risks:

```md
# <Character Name> Index

## Checklist
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

## Drift Risks
- <Risk 1>
- <Risk 2>
```

- [ ] **Step 2: Create the shared Tier 1 README section order**

Use this exact section order in each Tier 1 `README.md`:

```md
# <Character Name>

## Core Identity

## Narrative Function

## What They Represent

## How They Express That In Play

## Gameplay Impact

## Player Learning

## Act-by-Act Presence

## System Dependencies

## Failure Mode

## Source Basis
```

- [ ] **Step 3: Verify all Tier 1 folders have both required files**

Run:

```powershell
$tier1 = 1..10 | ForEach-Object { Get-ChildItem "game_design_document/15_character_dossiers" | Where-Object { $_.PSIsContainer -and $_.Name -match ('^{0:00}_' -f $_) } }
Get-ChildItem "game_design_document/15_character_dossiers" -Directory | Where-Object { $_.Name -match '^(0[1-9]|10)_' } | ForEach-Object {
  Test-Path (Join-Path $_.FullName 'README.md')
  Test-Path (Join-Path $_.FullName 'INDEX.md')
}
```

Expected: all outputs are `True`

- [ ] **Step 4: Commit**

```bash
git add game_design_document/15_character_dossiers
git commit -m "docs: scaffold tier 1 character dossiers"
```

### Task 3: Fully Seed Tier 1 Dossiers

**Files:**
- Modify: `game_design_document/15_character_dossiers/01_aen/README.md`
- Modify: `game_design_document/15_character_dossiers/01_aen/INDEX.md`
- Modify: `game_design_document/15_character_dossiers/02_yin/README.md`
- Modify: `game_design_document/15_character_dossiers/02_yin/INDEX.md`
- Modify: `game_design_document/15_character_dossiers/03_yang/README.md`
- Modify: `game_design_document/15_character_dossiers/03_yang/INDEX.md`
- Modify: `game_design_document/15_character_dossiers/04_veil_maiden_siren_of_masks/README.md`
- Modify: `game_design_document/15_character_dossiers/04_veil_maiden_siren_of_masks/INDEX.md`
- Modify: `game_design_document/15_character_dossiers/05_hollow_reflection/README.md`
- Modify: `game_design_document/15_character_dossiers/05_hollow_reflection/INDEX.md`
- Modify: `game_design_document/15_character_dossiers/06_samson/README.md`
- Modify: `game_design_document/15_character_dossiers/06_samson/INDEX.md`
- Modify: `game_design_document/15_character_dossiers/07_sophia/README.md`
- Modify: `game_design_document/15_character_dossiers/07_sophia/INDEX.md`
- Modify: `game_design_document/15_character_dossiers/08_marcel/README.md`
- Modify: `game_design_document/15_character_dossiers/08_marcel/INDEX.md`
- Modify: `game_design_document/15_character_dossiers/09_hazel/README.md`
- Modify: `game_design_document/15_character_dossiers/09_hazel/INDEX.md`
- Modify: `game_design_document/15_character_dossiers/10_mentor_roga/README.md`
- Modify: `game_design_document/15_character_dossiers/10_mentor_roga/INDEX.md`

- [ ] **Step 1: Write Aen as the reference-quality full dossier**

`game_design_document/15_character_dossiers/01_aen/README.md` must explicitly cover:

```md
## Core Identity
Aen is the player character: a gifted young ninja who begins already whole but is made to believe he needs external validation to deserve love.

## How They Express That In Play
- starts with a warm, socially grounded movement/combat context in Lantern Heights
- accepts the Veil Maiden's attention and is drawn into isolation through mission structure
- loses Yin and Yang in the scripted Hollowing
- regains abilities through support-bearing encounters rather than destiny rewards
- can resolve late-game conflict through armed pressure or unarmed restraint

## Gameplay Impact
- every major movement unlock is attached to his recovery arc
- combat expression changes how the ending feels
- his state determines hub warmth, light-state reading, and player-facing progression tone
```

- [ ] **Step 2: Write Yin, Yang, Siren, and Hollow Reflection with concrete mechanics and encounter consequences**

Minimum required content:

```md
Yin:
- appears early as stillness, intuition, and spiritual balance made visible
- after loss, remains present only as absence, star-motif, or orientation symbol
- affects player interpretation of safety, memory, and the ending

Yang:
- appears early as courage, fire, and forward motion made visible
- after loss, remains present only as absence, star-motif, or hopeful orientation
- affects player interpretation of drive, risk, and the ending

Veil Maiden / Siren:
- uses praise, specialness, and mission framing to isolate Aen
- drives the Summit Shrine sequence and the taking of Yin and Yang
- affects quest framing, scene tone, and the wound that reorganizes the campaign

Hollow Reflection:
- mirrors Aen's recovered toolkit in the final confrontation
- turns each community-earned movement or combat skill into a hostile reflection
- determines whether the ending feels like domination or release
```

- [ ] **Step 3: Write Samson, Sophia, Marcel, Hazel, and Roga as support-through-gameplay dossiers**

Minimum required content:

```md
Samson:
- sparring, pressure honesty, low-health courage, rescue or support beats
- teaches courage without ownership

Sophia:
- map truth, false-path correction, star-ink logic, memory orientation
- restores context when the world has been manipulated

Marcel:
- forging, reinforcement, repair logic, gear meaning
- makes damage survivable without denying that it happened

Hazel:
- warmth, safe-return spaces, lantern silk, Beacon preparation
- carries future-facing hope without promising immediate reversal

Roga:
- endurance drills, wall-cling mastery, presence inside pain
- turns difficulty into trained persistence instead of cruelty
```

- [ ] **Step 4: Add dossier-specific drift risks to each Tier 1 INDEX**

Example exact risks to use:

```md
Aen:
- Aen becomes a generic chosen one if his "already whole" truth stops shaping systems
- combat expression loses meaning if armed and unarmed play collapse into the same emotional result

Sophia:
- Sophia becomes a generic map NPC if her truth-restoring role is not mechanical
- false-path and memory systems lose clarity if her evidence function is underwritten
```

- [ ] **Step 5: Verify Tier 1 dossiers mention both behavior and gameplay impact**

Run:

```powershell
rg -n "## How They Express That In Play|## Gameplay Impact" game_design_document/15_character_dossiers/01_aen game_design_document/15_character_dossiers/02_yin game_design_document/15_character_dossiers/03_yang game_design_document/15_character_dossiers/04_veil_maiden_siren_of_masks game_design_document/15_character_dossiers/05_hollow_reflection game_design_document/15_character_dossiers/06_samson game_design_document/15_character_dossiers/07_sophia game_design_document/15_character_dossiers/08_marcel game_design_document/15_character_dossiers/09_hazel game_design_document/15_character_dossiers/10_mentor_roga
```

Expected: every dossier folder returns both headings

- [ ] **Step 6: Commit**

```bash
git add game_design_document/15_character_dossiers
git commit -m "docs: seed tier 1 character dossiers"
```

### Task 4: Scaffold And Seed Tier 2 Dossiers

**Files:**
- Create: `game_design_document/15_character_dossiers/11_instructor_tai/README.md`
- Create: `game_design_document/15_character_dossiers/11_instructor_tai/INDEX.md`
- Create: `game_design_document/15_character_dossiers/12_merchant_rilu/README.md`
- Create: `game_design_document/15_character_dossiers/12_merchant_rilu/INDEX.md`
- Create: `game_design_document/15_character_dossiers/13_smith_jenro/README.md`
- Create: `game_design_document/15_character_dossiers/13_smith_jenro/INDEX.md`
- Create: `game_design_document/15_character_dossiers/14_shade_hermit/README.md`
- Create: `game_design_document/15_character_dossiers/14_shade_hermit/INDEX.md`
- Create: `game_design_document/15_character_dossiers/15_smith_monk/README.md`
- Create: `game_design_document/15_character_dossiers/15_smith_monk/INDEX.md`
- Create: `game_design_document/15_character_dossiers/16_listening_elder/README.md`
- Create: `game_design_document/15_character_dossiers/16_listening_elder/INDEX.md`
- Create: `game_design_document/15_character_dossiers/17_advocate/README.md`
- Create: `game_design_document/15_character_dossiers/17_advocate/INDEX.md`
- Create: `game_design_document/15_character_dossiers/18_hearth_brother/README.md`
- Create: `game_design_document/15_character_dossiers/18_hearth_brother/INDEX.md`
- Create: `game_design_document/15_character_dossiers/19_brother_kai/README.md`
- Create: `game_design_document/15_character_dossiers/19_brother_kai/INDEX.md`
- Create: `game_design_document/15_character_dossiers/20_brother_len/README.md`
- Create: `game_design_document/15_character_dossiers/20_brother_len/INDEX.md`
- Create: `game_design_document/15_character_dossiers/21_brother_ash/README.md`
- Create: `game_design_document/15_character_dossiers/21_brother_ash/INDEX.md`
- Create: `game_design_document/15_character_dossiers/22_old_man_riku/README.md`
- Create: `game_design_document/15_character_dossiers/22_old_man_riku/INDEX.md`
- Create: `game_design_document/15_character_dossiers/23_lantern_kid/README.md`
- Create: `game_design_document/15_character_dossiers/23_lantern_kid/INDEX.md`

- [ ] **Step 1: Use the lighter operational brief format for all Tier 2 READMEs**

Each Tier 2 `README.md` should include these exact headings:

```md
# <Character Name>

## Core Identity

## Narrative Function

## Concrete Behaviors

## Gameplay Impact

## System Dependencies

## Failure Mode

## Source Basis
```

- [ ] **Step 2: Seed concrete behaviors for each Tier 2 character**

Required behavior anchors:

```md
Instructor Tai: basic movement lesson, baseline dojo discipline, early warning about shortcuts or pride
Merchant Rilu: market warning signs, shop routing, community drift observation
Smith Jenro: craft warning, posture/tool-reading, polish-versus-strength framing
Shade Hermit: survival witness, underworld orientation, grief without false cure
Smith Monk: practical support, Dash restoration, first movement return
Listening Elder: heard-without-pressure reflection point, memory or dialogue rest function
Advocate: defended dignity, anti-self-blame framing, institutional challenge language
Hearth Brother: first warm welcome into monastery structure, support hub presence
Brother Kai: physical push into motion, breath-through-action training
Brother Len: timing, patience, measured technical instruction
Brother Ash: survivor-support perspective, grief-aware discipline
Old Man Riku: memory of home, old warnings, pre-wound identity witness
Lantern Kid: inheritance, innocence, future shelter motif
```

- [ ] **Step 3: Verify every Tier 2 README includes concrete behaviors and gameplay impact**

Run:

```powershell
rg -n "## Concrete Behaviors|## Gameplay Impact" game_design_document/15_character_dossiers/11_instructor_tai game_design_document/15_character_dossiers/12_merchant_rilu game_design_document/15_character_dossiers/13_smith_jenro game_design_document/15_character_dossiers/14_shade_hermit game_design_document/15_character_dossiers/15_smith_monk game_design_document/15_character_dossiers/16_listening_elder game_design_document/15_character_dossiers/17_advocate game_design_document/15_character_dossiers/18_hearth_brother game_design_document/15_character_dossiers/19_brother_kai game_design_document/15_character_dossiers/20_brother_len game_design_document/15_character_dossiers/21_brother_ash game_design_document/15_character_dossiers/22_old_man_riku game_design_document/15_character_dossiers/23_lantern_kid
```

Expected: every dossier folder returns both headings

- [ ] **Step 4: Commit**

```bash
git add game_design_document/15_character_dossiers
git commit -m "docs: add tier 2 character dossiers"
```

### Task 5: Update Existing Character Summary Branch To Point At Dossiers

**Files:**
- Modify: `game_design_document/07_characters/README.md`
- Modify: `game_design_document/07_characters/INDEX.md`
- Modify: `game_design_document/07_characters/01_main_characters/README.md`
- Modify: `game_design_document/07_characters/07_supporting_characters/README.md`
- Modify: `game_design_document/07_characters/08_enemies/README.md`

- [ ] **Step 1: Update `07_characters/README.md` to define branch responsibilities**

Replace the current high-level summary with wording that includes:

```md
## Branch Rule
`07_characters/` is the cast-summary layer. Use `../15_character_dossiers/` for per-character operational briefs that connect symbolism to gameplay consequence.
```

- [ ] **Step 2: Update `07_characters/INDEX.md` to link into the new branch**

Add:

```md
## Related Branch
- `../15_character_dossiers/` - dedicated folders for named-character gameplay and behavior briefs
```

- [ ] **Step 3: Add dossier cross-links to the key summary READMEs**

Add explicit link lists such as:

```md
Main cast dossiers:
- `../../15_character_dossiers/01_aen/`
- `../../15_character_dossiers/02_yin/`
- `../../15_character_dossiers/03_yang/`
- `../../15_character_dossiers/04_veil_maiden_siren_of_masks/`
- `../../15_character_dossiers/05_hollow_reflection/`
```

```md
Support dossiers:
- `../../15_character_dossiers/06_samson/`
- `../../15_character_dossiers/07_sophia/`
- `../../15_character_dossiers/08_marcel/`
- `../../15_character_dossiers/09_hazel/`
- `../../15_character_dossiers/10_mentor_roga/`
```

- [ ] **Step 4: Verify the summary branch references the dossier branch**

Run: `rg -n "15_character_dossiers" game_design_document/07_characters`

Expected: matches appear in `README.md`, `INDEX.md`, and at least the three summary READMEs updated above

- [ ] **Step 5: Commit**

```bash
git add game_design_document/07_characters
git commit -m "docs: link character summaries to dossiers"
```

### Task 6: Final Structural Verification

**Files:**
- Verify only

- [ ] **Step 1: Check every dossier directory has both required files**

Run:

```powershell
$missing = Get-ChildItem "game_design_document/15_character_dossiers" -Recurse -Directory | ForEach-Object {
  $readme = Test-Path (Join-Path $_.FullName 'README.md')
  $index = Test-Path (Join-Path $_.FullName 'INDEX.md')
  if (-not $readme -or -not $index) {
    [PSCustomObject]@{ Path = $_.FullName; Readme = $readme; Index = $index }
  }
}
$missing
```

Expected: no output

- [ ] **Step 2: Check the dossier branch covers all first-wave and second-wave characters**

Run:

```powershell
Get-ChildItem "game_design_document/15_character_dossiers" -Directory | Select-Object -ExpandProperty Name
```

Expected:
- root lists `01_aen` through `23_lantern_kid`

- [ ] **Step 3: Check for missing gameplay-impact headings**

Run:

```powershell
rg -L "## Gameplay Impact" game_design_document/15_character_dossiers
```

Expected:
- no Tier 1 or Tier 2 `README.md` is returned

- [ ] **Step 4: Check the root and character-summary branches both reference the dossier branch**

Run:

```powershell
rg -n "15_character_dossiers" game_design_document/README.md game_design_document/INDEX.md game_design_document/07_characters
```

Expected:
- matches from root files and character summary files

- [ ] **Step 5: Commit**

```bash
git add game_design_document
git commit -m "docs: verify character dossier branch integration"
```

## Self-Review

### Spec Coverage
- New top-level branch: covered by Task 1
- Tier 1 dossiers: covered by Tasks 2 and 3
- Tier 2 dossiers: covered by Task 4
- Cross-linking with `07_characters/`: covered by Task 5
- Root integration and verification: covered by Tasks 1 and 6

### Placeholder Scan
- No `TODO`, `TBD`, or deferred content markers are used in tasks.
- All commands are explicit PowerShell or Git commands.
- All required headings and content anchors are spelled out.

### Type Consistency
- The branch name is consistently `15_character_dossiers`
- Tier naming is consistent across tasks
- `README.md` and `INDEX.md` requirements are consistent across every folder
