# GDD Scaffold Design

Date: 2026-05-09
Topic: Standalone `game_design_document/` scaffold
Status: Draft for review

## Goal

Create a standalone top-level `game_design_document/` directory that organizes the Game Design Document into a stable, three-level-deep hierarchy. Every folder in the hierarchy must contain both a `README.md` and an `INDEX.md`. These files should summarize the relevant Indie Game Academy GDD sections, explain what belongs in that folder, and direct future authors to the correct place to fill in content.

The scaffold must also provide a clear, explicit way to document the GDD lifecycle so the document can operate as a living design artifact rather than a static template dump.

## Scope

This work covers:

- creation of a new top-level `game_design_document/` tree
- stable content organization by subject rather than by status
- `README.md` and `INDEX.md` files at every level
- third-level topic folders that each represent one GDD topic
- governance and lifecycle documentation for drafting, review, approval, implementation, and supersession
- cross-linking between parent and child indexes where useful

This work does not cover:

- filling in game-specific design answers
- migrating current `docs/` content into the new GDD tree
- generating diagrams or non-Markdown assets
- building automation around the GDD

## Structure Decision

The scaffold will use a subject-based hierarchy with a dedicated governance branch.

Chosen approach:

`Domain -> Section -> Topic`, plus `00_governance/` for lifecycle and process.

Reasoning:

- content paths remain stable as the project evolves
- the structure maps cleanly to the Indie Game Academy template
- lifecycle concerns stay explicit without forcing folder moves when status changes
- navigation remains predictable for collaborators joining mid-project

## Directory Layout

Top level:

- `game_design_document/`
- `game_design_document/00_governance/`
- `game_design_document/01_introduction/`
- `game_design_document/02_game_overview/`
- `game_design_document/03_gameplay/`
- `game_design_document/04_mechanics/`
- `game_design_document/05_graphics_and_audio/`
- `game_design_document/06_story_and_narrative/`
- `game_design_document/07_characters/`
- `game_design_document/08_game_world/`

Second level:

- each numbered domain folder contains section folders mapped from the source template
- governance contains process-oriented sections such as lifecycle, reviews, decisions, and changes

Third level:

- each topic gets its own folder
- each topic folder contains `README.md` and `INDEX.md`

Example:

- `game_design_document/02_game_overview/07_core_loop/README.md`
- `game_design_document/02_game_overview/07_core_loop/INDEX.md`

## Content Model

Each folder will include both:

- `README.md`: narrative explanation of the folder purpose, how it relates to the larger GDD, and concise summaries of the covered topics
- `INDEX.md`: navigation file listing child folders, expected contents, authoring checklist items, and pointers to adjacent sections

### Root Files

Root `README.md` will:

- explain what the GDD folder is for
- explain how to use the structure
- explain numbering and depth conventions
- explain how governance and design content relate

Root `INDEX.md` will:

- act as the table of contents for the full tree
- link to all first-level folders
- describe the recommended authoring order
- link to lifecycle and review conventions

### Domain-Level Files

Each domain `README.md` will:

- summarize the relevant Indie Game Academy section
- explain the purpose of the domain in this project
- describe what decisions should be documented here versus elsewhere

Each domain `INDEX.md` will:

- link to all child section folders
- list the core questions that section should answer
- note dependencies on other domains

### Topic-Level Files

Each topic `README.md` will:

- restate the topic in practical terms
- summarize the Indie Game Academy guidance for that topic
- explain the kind of evidence or decisions that should live there

Each topic `INDEX.md` will:

- provide a fill-in checklist
- identify required subtopics or prompts
- note upstream/downstream dependencies
- include status placeholders such as `Draft`, `Needs Review`, `Approved`, `Implemented`, or `Superseded`

## Governance and Lifecycle

The scaffold will include a dedicated governance branch so the lifecycle of the GDD is explicit and easy to maintain.

Planned governance sections:

- `00_governance/01_lifecycle_management/`
- `00_governance/02_review_and_approval/`
- `00_governance/03_change_log/`
- `00_governance/04_decision_log/`
- `00_governance/05_contribution_workflow/`

Lifecycle states to document:

- `Draft`
- `In Review`
- `Approved`
- `Implemented`
- `Superseded`

Lifecycle guidance will define:

- who can edit design sections
- when a topic should move between states
- how to capture unresolved questions
- how to record approved changes without losing prior intent
- how to link implementation work back to design decisions

## Proposed Topic Mapping

The scaffold will map the source template into topic folders with stable numbering. Representative examples:

Introduction:

- `01_scope_of_the_document`
- `02_elevator_pitch`

Game Overview:

- `01_game_concept`
- `02_audience`
- `03_genre`
- `04_setting`
- `05_world_structure`
- `06_player`
- `07_core_loop`
- `08_look_and_feel`

Gameplay:

- `01_objectives`
- `02_progression`
- `03_difficulty_curve`
- `04_play_flow`
- `05_difficulty`

Mechanics:

- `01_rules`
- `02_game_universe`
- `03_physics`
- `04_economy`
- `05_character_movement`
- `06_player_interaction`
- `07_game_menus`
- `08_saving`
- `09_game_options`
- `10_assets`

Graphics and Audio:

- `01_visual_system`
- `02_player_camera`
- `03_landscape`
- `04_interface`
- `05_audio_system`
- `06_game_music`
- `07_audio_look_and_feel`

Story and Narrative:

- `01_backstory`
- `02_main_plot`
- `03_plot_progression`
- `04_cutscenes`

Characters:

- `01_main_characters`
- `02_character_backstory`
- `03_personality`
- `04_appearance`
- `05_abilities`
- `06_relationships`
- `07_supporting_characters`
- `08_enemies`

Game World:

- `01_world_look_and_feel`
- `02_locations`
- `03_connection_to_the_plot`
- `04_levels`
- `05_tutorial_levels`
- `06_main_levels`
- `07_optional_levels`

## Data Flow

Authoring flow:

1. Start at root `INDEX.md`.
2. Move into a domain `INDEX.md`.
3. Open the topic folder `README.md` for framing.
4. Use the topic `INDEX.md` checklist to fill in the actual content.
5. Update governance logs when decisions or status changes occur.

Status flow:

1. A topic begins as `Draft`.
2. Review expectations are captured under governance.
3. Once accepted, the topic becomes `Approved`.
4. When the game matches the documented design, mark it `Implemented`.
5. If replaced, mark it `Superseded` and point to the successor topic or decision.

## Error Handling and Maintenance

The scaffold should reduce ambiguity by making placement rules obvious:

- duplicate topics should be avoided by linking related folders instead of restating large blocks of content
- if a topic applies weakly, the folder should still exist but may contain `Not applicable` with reasoning
- if a design changes materially, record the change in governance instead of silently overwriting intent

## Verification

Scaffold verification should confirm:

- the folder depth is consistent with the requested three-level layout
- every folder contains `README.md` and `INDEX.md`
- the root index links to all first-level domains
- each domain index links to all child topics
- governance includes lifecycle documentation

## Implementation Notes

When implementation begins, generate the scaffold with concise Markdown placeholders rather than empty files. Each file should be useful on first read and should point the next author to the right place to continue.

The implementation should favor:

- concise summaries over long prose
- consistent naming and numbering
- predictable navigation
- minimal duplication

## Open Questions Resolved

- Location: standalone top-level folder named `game_design_document/`
- File depth: third-level topic folders each with their own `README.md` and `INDEX.md`
- Content style: every `README.md` and `INDEX.md` should already summarize the Indie Game Academy sections and point to where each topic should be filled in
- Lifecycle: explicit governance branch required
