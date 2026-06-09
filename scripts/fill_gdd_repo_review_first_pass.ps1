Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$root = Join-Path (Get-Location) 'game_design_document'

function Write-Md {
    param(
        [string]$RelativePath,
        [string]$Content
    )

    $fullPath = Join-Path $root $RelativePath
    $dir = Split-Path -Parent $fullPath
    if (-not (Test-Path $dir)) {
        New-Item -ItemType Directory -Force -Path $dir | Out-Null
    }
    Set-Content -LiteralPath $fullPath -Value ($Content.Trim() + [Environment]::NewLine)
}

Write-Md 'README.md' @'
# Game Design Document

## Purpose
This tree is now partially populated from the live `shadow_ascent_clean_start` repository, the integrated narrative donor, and the embedded `indieniinja` source reference. It should be treated as a living GDD, not a fiction layer disconnected from implementation.

## How To Read This First Pass
- `repo-backed truth` means the current clean-start repo already implements or explicitly documents it.
- `donor signal` means the donor projects strongly indicate intended direction, but the clean-start repo may not fully implement it yet.
- `unresolved` means the projects do not currently justify a stronger claim without new design decisions.

## Current Use
- Start at `INDEX.md`.
- Use `00_governance/04_decision_log/2026-05-09-project-and-donor-review.md` for the detailed cross-project review that informed this pass.
- Treat topic folders with concrete text as seeded drafts, then refine them as the game direction hardens.
'@

Write-Md 'INDEX.md' @'
# Game Design Document Index

## Review Baseline
- `./00_governance/04_decision_log/2026-05-09-project-and-donor-review.md` - detailed project and donor review used to seed the GDD

## Most Populated Topics In This Pass
- `./01_introduction/01_scope_of_the_document/`
- `./01_introduction/02_elevator_pitch/`
- `./02_game_overview/01_game_concept/`
- `./02_game_overview/03_genre/`
- `./02_game_overview/04_setting/`
- `./02_game_overview/06_player/`
- `./02_game_overview/07_core_loop/`
- `./03_gameplay/01_objectives/`
- `./03_gameplay/02_progression/`
- `./04_mechanics/01_rules/`
- `./04_mechanics/03_physics/`
- `./04_mechanics/05_character_movement/`
- `./04_mechanics/08_saving/`
- `./05_graphics_and_audio/01_visual_system/`
- `./05_graphics_and_audio/05_audio_system/`
- `./06_story_and_narrative/01_backstory/`
- `./06_story_and_narrative/02_main_plot/`
- `./07_characters/01_main_characters/`
- `./07_characters/08_enemies/`
- `./08_game_world/02_locations/`
- `./08_game_world/04_levels/`

## Interpretation Rule
Prefer the live clean-start repo over old analysis docs when they conflict. Donor material explains intent and missing surface area; it does not override executable truth.
'@

Write-Md '00_governance/04_decision_log/2026-05-09-project-and-donor-review.md' @'
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
'@

Write-Md '01_introduction/01_scope_of_the_document/README.md' @'
# Scope of the Document

## Current Repo-Backed Truth
This GDD is primarily for the developer and any future collaborators who need one stable description of product intent, runtime truth, donor intent, and implementation boundaries. The clean-start repo already uses living docs, migration governance, and validation gates, so this document should sit beside those systems rather than duplicate them blindly.

## What This GDD Should Cover
- product vision and player-facing direction
- major gameplay, world, character, and narrative decisions
- the difference between implemented truth and future direction
- links back to canonical engineering docs when implementation evidence matters

## What This GDD Should Not Try To Do
- replace `docs/CURRENT_STATE.md` as the executable truth snapshot
- replace `docs/MIGRATION_MAP.md` as the donor import ledger
- pretend unresolved art, audio, or market questions are already solved

## Source Basis
- `README.md`
- `docs/CURRENT_STATE.md`
- `docs/DOC_MAINTENANCE_PLAN.md`
'@

Write-Md '01_introduction/02_elevator_pitch/README.md' @'
# Elevator Pitch

## First-Pass Pitch
Shadow Ascent is a narrative Metroidvania about Aen crossing a lantern-lit mountain world that collapses into grief, isolation, and release. Players explore authored plateaus, restore movement and combat capability, confront illusion and memory, and push from intimate village life toward a final act of wholeness and beacon-light.

## Why This Pitch Is Defensible
The repo and donor data consistently describe:
- a full-campaign narrative Metroidvania
- plateau-based emotional progression
- ability-gated traversal and combat
- a protagonist-centered emotional arc from home, to collapse, to release

## Still Unresolved
The current repos do not yet justify a stronger market-facing claim about uniqueness, monetization, or target audience conversion. Keep the pitch emotionally clear, but do not oversell shipping surface that does not yet exist.

## Source Basis
- `docs/START_TO_FINISH_GAME_PLAN.md`
- `docs/CURRENT_STATE.md`
- `data/plateaus.json`
- `data/narrative_beats.json`
'@

Write-Md '02_game_overview/01_game_concept/README.md' @'
# Game Concept

## Current Repo-Backed Truth
The game is a full-campaign narrative Metroidvania built around authored critical progression, elastic optional content, contract-driven story state, and deterministic traversal/combat systems. The player explores plateaus, advances story beats, restores or unlocks abilities, fights symbolic enemies, and returns to evolving hubs and support spaces.

## Donor Signal
The donor repos show a broader long-term ambition: this campaign foundation is intended to grow toward a more systemic world model, not remain a single-route prototype forever.

## What Players Are Primarily Doing
- traversing authored and semi-elastic spaces
- talking to NPCs and advancing beats
- fighting enemies and bosses
- unlocking or restoring abilities that widen route options
- moving from emotional collapse toward release

## Source Basis
- `docs/START_TO_FINISH_GAME_PLAN.md`
- `docs/CURRENT_STATE.md`
- `data/plateaus.json`
- `data/quests.json`
'@

Write-Md '02_game_overview/03_genre/README.md' @'
# Genre

## Recommended Genre Label
Narrative Metroidvania.

## Secondary Framing
- action-platformer
- story-driven exploration game
- long-term expansion target: systemic open-world narrative RPG

## Why This Is The Right Label
The clean-start repo already centers:
- gated traversal and ability restoration
- plateau and route progression
- combat encounters and boss phases
- return loops through hub and world state
- authored narrative beats as campaign backbone

## Important Boundary
The open-world RPG language describes long-term architecture, not the current player-facing genre. The present game should still be described first as a narrative Metroidvania.

## Source Basis
- `docs/START_TO_FINISH_GAME_PLAN.md`
- `docs/CURRENT_STATE.md`
'@

Write-Md '02_game_overview/04_setting/README.md' @'
# Setting

## Current Repo-Backed Truth
The game takes place across a symbolic fantasy mountain world structured as emotional and narrative plateaus. The setting begins in a warm lantern village and moves through shrines, depths, monastery refuge, wind-road ascent, mirror confrontation, and final beacon release.

## Named Plateau Sequence
- Lantern Heights
- Summit Shrine
- The Hollow Depths
- Ember Monastery
- The Winding Skyroad
- Mirror Summit
- Beacon Cliff

## Setting Tone
The setting is not generic fantasy for its own sake. The donor data ties each region to emotional phase, light-state, and pacing bias, which means place and mood are part of the narrative system.

## Source Basis
- `data/plateaus.json`
- `data/area_catalog.json`
'@

Write-Md '02_game_overview/06_player/README.md' @'
# Player

## Current Repo-Backed Truth
The player controls Aen. The clean-start repo is currently built around single-player progression, even though some co-op simulation scaffolding exists in the core runtime.

## Product Position
Single-player should remain the primary claim in the GDD until a real multiplayer or co-op product path exists. Current co-op logic is engineering scaffolding, not a proven shipped feature.

## Narrative Position
Aen is framed as the central consciousness of the story, with Yin and Yang initially present in the world and later transformed into memory or symbolic motifs after the scripted loss.

## Source Basis
- `data/npc_registry.json`
- `docs/CURRENT_STATE.md`
'@

Write-Md '02_game_overview/07_core_loop/README.md' @'
# Core Loop

## Current Repo-Backed Truth
The strongest supported core loop is:

1. enter a route or area
2. traverse using movement abilities
3. fight or evade threats
4. interact with NPCs, triggers, altars, or objectives
5. advance story flags, missions, or ability state
6. return to a safer hub or move deeper into the next plateau

## Why This Loop Fits
This loop is visible in the playable client, mission runtime, contract-backed objectives, traversal gating, and narrative beat structure.

## Secondary Loop
Elastic or optional content can branch off the critical route for training, recovery, deepening, and side-quest progression without changing core story truth.

## Source Basis
- `docs/CURRENT_STATE.md`
- `data/plateaus.json`
- `data/quests.json`
'@

Write-Md '03_gameplay/01_objectives/README.md' @'
# Objectives

## Primary Objective
Complete the campaign by advancing through the plateau sequence, surviving the collapse, restoring capability, confronting the final release conflict, and lighting the ending path at Beacon Cliff.

## Secondary Objectives
- complete optional quest chains
- recover support-character outcomes
- deepen understanding of the world and its motifs
- strengthen Aen through rewards, restored abilities, and optional content

## Current Implementation Reality
The current build proves objective-driven progression and route gating, but only a bounded slice is directly playable. The broader campaign objective is strongly authored in the contracts even where the full runtime route is not yet surfaced in one continuous client experience.

## Source Basis
- `data/narrative_beats.json`
- `data/quests.json`
- `docs/CURRENT_STATE.md`
'@

Write-Md '03_gameplay/02_progression/README.md' @'
# Progression

## Current Repo-Backed Truth
Progression is plateau-based and flag-driven. Authored critical beats move the player through acts, while optional content can deepen or support the journey without invalidating campaign truth.

## Proven Progression Structure
- plateau transition by beat and flag state
- mission and side-quest gating by act, plateau, and required flags
- restored or unlocked abilities widening traversal and combat options
- evolving hub and NPC state based on progression context

## Likely Ability Arc
The current contracts and flags strongly imply a restoration arc involving dash, double jump, glide, grapple, wall cling, and air dodge across later content.

## Source Basis
- `data/plateaus.json`
- `data/narrative_beats.json`
- `data/quests.json`
- `docs/CURRENT_STATE.md`
'@

Write-Md '04_mechanics/01_rules/README.md' @'
# Rules

## Current Repo-Backed Truth
The game is built on explicit story flags, mission objectives, traversal gates, ability checks, and authored beat ordering. Progress is not meant to be freeform enough to break the critical path, even when optional content is elastic.

## Core Rule Set
- critical progression is controlled by beats and flags
- elastic content must not contradict campaign truth
- traversal access is ability-gated
- mission completion is objective-driven rather than auto-completed
- save/load must preserve authoritative story state

## Design Implication
This is a heavily authored game with controlled flexibility, not a sandbox where every systemic outcome is equally valid.

## Source Basis
- `data/plateaus.json`
- `data/narrative_beats.json`
- `docs/CURRENT_STATE.md`
'@

Write-Md '04_mechanics/03_physics/README.md' @'
# Physics

## Current Repo-Backed Truth
The clean-start repo already carries donor-calibrated movement constants and a deterministic controller model. The playable slices implement gravity, jump logic, coyote windows, wall interaction, dash behavior, and bounded collision geometry.

## What Is Proven
- run and precision-walk states
- jump, double-jump, coyote time
- wall slide, wall jump, wall exhaustion behavior
- dash timing and cooldown
- collision against authored geometry

## What Is Not Yet Final
The current physics stack is strong enough for QA and feel validation, but it is not yet the final full production surface for every world interaction or client context.

## Source Basis
- `docs/CURRENT_STATE.md`
- `java/core/src/main/java/com/shadowascent/core/physics/PhysicsConstants.java`
'@

Write-Md '04_mechanics/05_character_movement/README.md' @'
# Character Movement

## Current Repo-Backed Truth
Aen already supports the movement verbs most central to the game identity: running, precision walking, jumping, double-jumping, wall-jumping, wall-sliding, dashing, fast-falling, attacking, and interacting.

## Broader Movement Arc
The story and quest flags imply a larger restoration path that expands movement capability over the campaign, including glide, grapple, wall cling mastery, and air dodge.

## Design Reading
Movement is not just locomotion. It is a major emotional and progression language in the project, with restoration of ability tied to recovery, ascent, and release.

## Source Basis
- `README.md`
- `docs/CURRENT_STATE.md`
- `data/quests.json`
'@

Write-Md '04_mechanics/08_saving/README.md' @'
# Saving

## Current Repo-Backed Truth
Saving is already treated as a serious system. The clean-start repo implements a versioned save envelope with backward-compatible migration and checksum validation.

## Current Save Model
- current envelope: `SAVE_V3`
- backward-compatible support for older save formats
- checksum guard to detect tampering or truncation
- save/load available from the playable client

## GDD Implication
The GDD should describe saving as player-available continuity support, not as a loose prototype convenience. The project already expects persistent story state and migration discipline.

## Source Basis
- `docs/CURRENT_STATE.md`
'@

Write-Md '05_graphics_and_audio/01_visual_system/README.md' @'
# Visual System

## Current Repo-Backed Truth
The production client path has started, but the visible game is still in placeholder form. LibGDX scaffold, camera follow, and tile rendering exist, yet the current visual layer remains a development harness rather than final art direction.

## Strong Visual Signals
- lantern-lit mountain fantasy
- emotional shifts expressed through place and light
- movement-readability-first platform spaces
- symbolic confrontation spaces such as shrine, depths, skyroad, mirror summit, and beacon cliff

## Still Unresolved
The final art style is not yet proven by the repos. The GDD can safely document mood, contrast, and environmental identity, but should not lock in a final sprite or rendering style until the production art path is defined.

## Source Basis
- `docs/CURRENT_STATE.md`
- `data/plateaus.json`
- `data/area_catalog.json`
'@

Write-Md '05_graphics_and_audio/05_audio_system/README.md' @'
# Audio System

## Current Repo-Backed Truth
There is no finished audio system in the clean-start build. This is one of the clearest gaps between the engineering foundation and a player-facing product surface.

## Donor Signal
The donor engineering repo suggests a richer eventual production layer, but the clean-start repo has not yet imported or scheduled a final audio stack strongly enough to describe it as implemented.

## Safe GDD Position
- audio is strategically important
- event-driven hooks already exist in the simulation model
- music, SFX, ambience, and emotional reinforcement should be planned early
- final implementation details remain unresolved

## Source Basis
- `docs/analysis/STUDIO_REVIEW_2026_05_08.md`
- `docs/CURRENT_STATE.md`
'@

Write-Md '06_story_and_narrative/01_backstory/README.md' @'
# Backstory

## Current Repo-Backed Truth
The story begins from a place of home, ritual, companionship, and foreshadowed fracture. Aen starts in Lantern Heights with Yin and Yang still present, under the influence of village bonds, training, and the deceptive warmth that precedes collapse.

## Pre-Game / Early-Game Setup
- Aen belongs to Lantern Heights
- Yin and Yang are materially present before the scripted loss
- Veil Maiden appears in a false-warmth role before reveal
- the village and its people matter emotionally before the descent

## Important Constraint
After the scripted defeat, Yin and Yang must not simply return as normal companions. The plateau contracts explicitly force them into absence, echo, memory, background motif, star, or symbolic light states until later content says otherwise.

## Source Basis
- `data/plateaus.json`
- `data/npc_registry.json`
- `data/narrative_beats.json`
'@

Write-Md '06_story_and_narrative/02_main_plot/README.md' @'
# Main Plot

## Current Repo-Backed Truth
Aen begins in Lantern Heights, forms or reaffirms village bonds, accepts the Veil Maiden's call, and is drawn upward into Summit Shrine, where false guidance gives way to mask-truth and scripted loss. The story then descends through Hollow Depths and surviving recovery spaces before rebuilding capability, ascending again, confronting the Hollow Reflection, and reaching Beacon Cliff for the final act of release and wholeness.

## Plot Spine
- home and foreshadowing
- false guidance and collapse
- hollow survival and grief
- rebuilding and reforging
- ascent and acceptance
- mirror confrontation
- beacon-lit ending

## Current Limitation
The contracts prove the broad arc. Fine-grain story presentation, scene sequencing, and final dialogue treatment will still need authored passes in the GDD.

## Source Basis
- `data/plateaus.json`
- `data/narrative_beats.json`
'@

Write-Md '07_characters/01_main_characters/README.md' @'
# Main Characters

## Current Repo-Backed Truth
The strongest supported main-character set is:
- Aen
- Yin
- Yang
- Veil Maiden / Siren of Masks
- Hollow Reflection

## Why These Characters Matter
- Aen is the protagonist and player vessel.
- Yin and Yang define companionship, loss, and symbolic continuity.
- Veil Maiden / Siren of Masks drives the early collapse and false guidance axis.
- Hollow Reflection is reserved as the final release boss and therefore closes the emotional and mechanical arc.

## Secondary Important Characters
Instructor Tai, Merchant Rilu, Smith Jenro, Samson, Sophia, Hazel, Marcel, Mentor Roga, Shade Hermit, Listening Elder, Advocate, and other support figures meaningfully shape plateau-specific tone and progression.

## Source Basis
- `data/npc_registry.json`
- `data/narrative_beats.json`
'@

Write-Md '07_characters/08_enemies/README.md' @'
# Enemies

## Current Repo-Backed Truth
The project already frames enemies as more than filler combatants. Major encounters are symbolic and tied to emotional or thematic pressure.

## Named Major Enemies
- Siren of Masks
- Weightbound Ogre
- Shatter Moth Queen
- Stone Judge
- Hollow Reflection

## Design Reading
These enemies suggest that bosses and major threats should embody narrative states such as exhaustion, gaslighting, indifference, false praise, and self-confrontation rather than existing only as stat checks.

## Current Limitation
The current runtime proves encounter logic and boss-pattern infrastructure more strongly than final encounter presentation or full enemy roster breadth.

## Source Basis
- `data/npc_registry.json`
- `docs/CURRENT_STATE.md`
'@

Write-Md '08_game_world/02_locations/README.md' @'
# Locations

## Current Repo-Backed Truth
The location structure is currently most coherent at the plateau and area-family level.

## Primary Locations
- Lantern Heights
- Summit Shrine
- The Hollow Depths
- Ember Monastery
- The Winding Skyroad
- Mirror Summit
- Beacon Cliff

## Supporting Area Signals
The area catalog already points to specific subspaces such as training dojo, mistwood path, hollow camp, echo galleries, ember forge, cloud bridges, hollow reflection arena, and ancient beacon.

## Design Reading
Locations are not interchangeable biomes. Each one is tied to an emotional phase, a traversal mood, and a narrative function.

## Source Basis
- `data/plateaus.json`
- `data/area_catalog.json`
'@

Write-Md '08_game_world/04_levels/README.md' @'
# Levels

## Current Repo-Backed Truth
The project has two level scales at once:

1. a currently playable bounded route used for QA and feel validation
2. a larger campaign structure defined through plateaus, beats, areas, region fragments, and section templates

## What This Means For The GDD
The GDD should not describe the game as only a four-room prototype, but it also should not pretend that every later level is fully playable in one continuous runtime today. It should document both the proven slice and the authored campaign plan.

## Level Taxonomy
- critical route spaces
- optional / elastic spaces
- training and hub spaces
- encounter arenas
- ascent and confrontation spaces

## Source Basis
- `docs/CURRENT_STATE.md`
- `data/area_catalog.json`
- `data/plateaus.json`
'@

Write-Md '08_game_world/05_tutorial_levels/README.md' @'
# Tutorial Levels

## Current Repo-Backed Truth
Lantern Heights appears to be the tutorial and onboarding plateau. It contains social onboarding, training spaces, and low-pressure route introduction before the collapse.

## Likely Tutorial Functions
- teach movement in a safe home context
- teach NPC interaction and mission uptake
- establish emotional attachment to the village
- foreshadow the loss that reframes the rest of the game

## Design Reading
The tutorial should not feel like a detached mechanics box. The donor structure suggests it is also the emotional baseline the rest of the game will wound and later transform.

## Source Basis
- `data/plateaus.json`
- `data/area_catalog.json`
- `data/narrative_beats.json`
'@
