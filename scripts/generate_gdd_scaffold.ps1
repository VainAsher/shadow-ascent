Set-StrictMode -Version Latest
$ErrorActionPreference = 'Stop'

$root = Join-Path (Get-Location) 'game_design_document'

function Join-Lines {
    param([string[]]$Lines)
    return ($Lines -join [Environment]::NewLine) + [Environment]::NewLine
}

function New-DocPair {
    param(
        [string]$Directory,
        [string]$Readme,
        [string]$Index
    )

    New-Item -ItemType Directory -Force -Path $Directory | Out-Null
    Set-Content -LiteralPath (Join-Path $Directory 'README.md') -Value $Readme
    Set-Content -LiteralPath (Join-Path $Directory 'INDEX.md') -Value $Index
}

function Get-TopicReadme {
    param($Domain, $Topic)

    $lines = @(
        "# $($Topic.Title)"
        ""
        "## Purpose"
        $Topic.Prompt
        ""
        "## Indie Game Academy Summary"
        "This folder turns the original GDD prompt for **$($Topic.Title)** into a focused place to write decisions, constraints, and unresolved questions."
        ""
        "## What Belongs Here"
        "- concise design intent"
        "- game-specific decisions"
        "- assumptions, references, and open issues tied to this topic"
        ""
        "## Fill This In By"
        ('Write the actual design content for **{0}** in this folder. Keep the summary in `README.md`, track progress in `INDEX.md`, and add companion notes only when the topic needs more depth.' -f $Topic.Title)
    )

    if ($Topic.PSObject.Properties.Name -contains 'ReadmeExtra') {
        $lines += ''
        $lines += '## Working Structure'
        $lines += $Topic.ReadmeExtra
    }

    return Join-Lines $lines
}

function Get-TopicIndex {
    param($Domain, $Topic)

    $lines = @(
        "# $($Topic.Title) Index"
        ""
        "## Status"
        "- Current State: Draft"
        "- Review State: Needs Review"
        "- Implementation State: Not Implemented"
        ""
        "## Core Prompt"
        $Topic.Prompt
        ""
        "## Checklist"
        "- [ ] Answer the core prompt in project-specific terms"
        "- [ ] Record constraints and assumptions"
        '- [ ] Link related decisions in `../../00_governance/04_decision_log/`'
        '- [ ] Update lifecycle status in `../../00_governance/01_lifecycle_management/` when this topic changes state'
        ""
        "## Related Areas"
        '- Domain Overview: `../README.md`'
        '- Domain Index: `../INDEX.md`'
        '- Root Index: `../../INDEX.md`'
    )

    if ($Topic.PSObject.Properties.Name -contains 'IndexExtra') {
        $lines += ''
        $lines += '## Suggested Structure'
        $lines += $Topic.IndexExtra
    }

    return Join-Lines $lines
}

function Get-DomainReadme {
    param($Domain)

    return Join-Lines @(
        "# $($Domain.Title)"
        ""
        "## Purpose"
        $Domain.Summary
        ""
        "## Indie Game Academy Summary"
        "This domain groups the GDD topics that belong to **$($Domain.Title)** so contributors know where to place high-level design information."
        ""
        "## What Belongs Here"
        "- section-level framing for this part of the GDD"
        "- links to topic-level folders where detailed content should be authored"
        "- clarifications about boundaries with adjacent domains"
    )
}

function Get-DomainIndex {
    param($Domain)

    $lines = @(
        "# $($Domain.Title) Index"
        ""
        "## Topics"
    )

    foreach ($topic in $Domain.Topics) {
        $lines += ('- `./{0}_{1}/` - {2}' -f $topic.Prefix, $topic.Slug, $topic.Title)
    }

    $lines += ""
    $lines += "## Authoring Notes"
    $lines += '- Start with the topic `README.md` for context.'
    $lines += '- Use the topic `INDEX.md` to track completion and review status.'
    $lines += '- Link major changes back to governance when decisions or status shifts occur.'

    return Join-Lines $lines
}

$domains = @(
    @{
        Prefix = '00'
        Slug = 'governance'
        Title = 'Governance'
        Summary = 'Defines how the GDD is maintained, reviewed, updated, and connected to implementation.'
        Topics = @(
            @{
                Prefix = '01'; Slug = 'lifecycle_management'; Title = 'Lifecycle Management'; Prompt = 'Document how topics move through Draft, In Review, Approved, Implemented, and Superseded states.'
                ReadmeExtra = @(
                    '- Define each lifecycle state in one or two sentences.'
                    '- Explain who can change a topic status.'
                    '- Explain what evidence is required before a status change.'
                    '- Explain how superseded topics should point to replacement decisions or sections.'
                )
                IndexExtra = @(
                    '- `Lifecycle States`'
                    '- `Entry Criteria Per State`'
                    '- `Exit Criteria Per State`'
                    '- `Ownership and Review Roles`'
                    '- `Supersession Rules`'
                )
            },
            @{
                Prefix = '02'; Slug = 'review_and_approval'; Title = 'Review and Approval'; Prompt = 'Define who reviews GDD changes, what approval means, and what evidence is required.'
                ReadmeExtra = @(
                    '- Name the review roles.'
                    '- Describe what must be checked before approval.'
                    '- Distinguish editorial cleanup from design-significant changes.'
                )
                IndexExtra = @(
                    '- `Review Roles`'
                    '- `Approval Criteria`'
                    '- `Required Evidence`'
                    '- `Escalation Path for Disagreement`'
                )
            },
            @{
                Prefix = '03'; Slug = 'change_log'; Title = 'Change Log'; Prompt = 'Track meaningful design changes so the team can see what changed and why.'
                ReadmeExtra = @(
                    '- Use this folder to record meaningful edits to the GDD.'
                    '- Prefer dated entries with short rationale and impacted topics.'
                )
                IndexExtra = @(
                    '- `YYYY-MM-DD`'
                    '- `Changed Topic(s)`'
                    '- `Summary of Change`'
                    '- `Reason`'
                    '- `Linked Decision or Review`'
                )
            },
            @{
                Prefix = '04'; Slug = 'decision_log'; Title = 'Decision Log'; Prompt = 'Record major design decisions, alternatives considered, and final rationale.'
                ReadmeExtra = @(
                    '- Capture design decisions that affect more than one topic.'
                    '- Record rejected alternatives so future contributors do not re-litigate settled choices without context.'
                )
                IndexExtra = @(
                    '- `Decision`'
                    '- `Date`'
                    '- `Status`'
                    '- `Options Considered`'
                    '- `Rationale`'
                    '- `Affected GDD Topics`'
                )
            },
            @{
                Prefix = '05'; Slug = 'contribution_workflow'; Title = 'Contribution Workflow'; Prompt = 'Describe how contributors propose, revise, and land GDD updates.'
                ReadmeExtra = @(
                    '- Explain the expected path from proposal to accepted update.'
                    '- Make it clear where discussion happens and where the final written truth lives.'
                )
                IndexExtra = @(
                    '- `Proposal Intake`'
                    '- `Drafting Rules`'
                    '- `Review Loop`'
                    '- `Approval and Merge`'
                    '- `Post-Approval Maintenance`'
                )
            }
        )
    },
    @{
        Prefix = '01'
        Slug = 'introduction'
        Title = 'Introduction'
        Summary = 'Frames the document, its audience, and the short pitch for the game.'
        Topics = @(
            @{ Prefix = '01'; Slug = 'scope_of_the_document'; Title = 'Scope of the Document'; Prompt = 'State who this GDD is for, how it should be used, and what it intentionally does not cover.' },
            @{ Prefix = '02'; Slug = 'elevator_pitch'; Title = 'Elevator Pitch'; Prompt = 'Summarize the game in under 75 words, including what makes it compelling and commercially promising.' }
        )
    },
    @{
        Prefix = '02'
        Slug = 'game_overview'
        Title = 'Game Overview'
        Summary = 'Defines the core idea, target audience, framing, and high-level player experience.'
        Topics = @(
            @{ Prefix = '01'; Slug = 'game_concept'; Title = 'Game Concept'; Prompt = 'Describe the objective, main play activity, and intended emotional experience.' },
            @{ Prefix = '02'; Slug = 'audience'; Title = 'Audience'; Prompt = 'Define the target player, their genre preferences, comparable games, and why they would buy this game.' },
            @{ Prefix = '03'; Slug = 'genre'; Title = 'Genre'; Prompt = 'Name the genre labels that best fit the game and explain why they are accurate.' },
            @{ Prefix = '04'; Slug = 'setting'; Title = 'Setting'; Prompt = 'Describe where and when the game takes place and what makes that setting distinct.' },
            @{ Prefix = '05'; Slug = 'world_structure'; Title = 'World Structure'; Prompt = 'Explain how the player navigates the game world, from linear progression to open exploration.' },
            @{ Prefix = '06'; Slug = 'player'; Title = 'Player'; Prompt = 'Define who the player controls and whether the game is singleplayer or multiplayer.' },
            @{ Prefix = '07'; Slug = 'core_loop'; Title = 'Core Loop'; Prompt = 'List the repeatable player actions that define the game minute to minute.' },
            @{ Prefix = '08'; Slug = 'look_and_feel'; Title = 'Look and Feel'; Prompt = 'Capture the visual style, emotional tone, and how presentation supports player feeling.' }
        )
    },
    @{
        Prefix = '03'
        Slug = 'gameplay'
        Title = 'Gameplay'
        Summary = 'Covers player goals, progression, play flow, and challenge tuning.'
        Topics = @(
            @{ Prefix = '01'; Slug = 'objectives'; Title = 'Objectives'; Prompt = 'Define the main objective and any secondary goals worth pursuing.' },
            @{ Prefix = '02'; Slug = 'progression'; Title = 'Progression'; Prompt = 'Describe how the player advances through the game and what changes as they progress.' },
            @{ Prefix = '03'; Slug = 'difficulty_curve'; Title = 'Difficulty Curve'; Prompt = 'Explain how challenge scales over time and whether it adapts to the player.' },
            @{ Prefix = '04'; Slug = 'play_flow'; Title = 'Play Flow'; Prompt = 'Describe the expected sequence of activities from the player perspective.' },
            @{ Prefix = '05'; Slug = 'difficulty'; Title = 'Difficulty'; Prompt = 'Document difficulty modes, tuning levers, and gameplay consequences.' }
        )
    },
    @{
        Prefix = '04'
        Slug = 'mechanics'
        Title = 'Mechanics'
        Summary = 'Documents the rules and systems that govern interaction with the game.'
        Topics = @(
            @{ Prefix = '01'; Slug = 'rules'; Title = 'Rules'; Prompt = 'State the core limits on player actions and the foundational rules of play.' },
            @{ Prefix = '02'; Slug = 'game_universe'; Title = 'Game Universe'; Prompt = 'Explain systemic behavior that happens beyond the player direct viewpoint.' },
            @{ Prefix = '03'; Slug = 'physics'; Title = 'Physics'; Prompt = 'Describe the physical logic of the game world and how realistic or stylized it is.' },
            @{ Prefix = '04'; Slug = 'economy'; Title = 'Economy'; Prompt = 'Define currencies, acquisition, spending, sinks, and balance expectations.' },
            @{ Prefix = '05'; Slug = 'character_movement'; Title = 'Character Movement'; Prompt = 'Document movement verbs, restrictions, and traversal feel.' },
            @{ Prefix = '06'; Slug = 'player_interaction'; Title = 'Player Interaction'; Prompt = 'List what the player can interact with and how those interactions work.' },
            @{ Prefix = '07'; Slug = 'game_menus'; Title = 'Game Menus'; Prompt = 'Describe the game menus, their roles, and the player paths through them.' },
            @{ Prefix = '08'; Slug = 'saving'; Title = 'Saving'; Prompt = 'Explain how saving works, including frequency, limits, and player expectations.' },
            @{ Prefix = '09'; Slug = 'game_options'; Title = 'Game Options'; Prompt = 'Document configurable options available to players and why they matter.' },
            @{ Prefix = '10'; Slug = 'assets'; Title = 'Assets'; Prompt = 'List the main asset categories the game depends on and their intended use.' }
        )
    },
    @{
        Prefix = '05'
        Slug = 'graphics_and_audio'
        Title = 'Graphics and Audio'
        Summary = 'Defines visual presentation, interface, and the soundscape.'
        Topics = @(
            @{ Prefix = '01'; Slug = 'visual_system'; Title = 'Visual System'; Prompt = 'Describe the overall visual approach, rendering style, and guiding art direction.' },
            @{ Prefix = '02'; Slug = 'player_camera'; Title = 'Player Camera'; Prompt = 'Explain how the player sees the game and what camera behaviors exist.' },
            @{ Prefix = '03'; Slug = 'landscape'; Title = 'Landscape'; Prompt = 'Document environmental look, terrain assumptions, and platforming implications where relevant.' },
            @{ Prefix = '04'; Slug = 'interface'; Title = 'Interface'; Prompt = 'Describe the UI, how it communicates state, and how the player interacts with it.' },
            @{ Prefix = '05'; Slug = 'audio_system'; Title = 'Audio System'; Prompt = 'Explain the structure of the game audio and any special systems such as voice or reactive sound.' },
            @{ Prefix = '06'; Slug = 'game_music'; Title = 'Game Music'; Prompt = 'Describe the musical direction, usage, and any gameplay or story dependency on music.' },
            @{ Prefix = '07'; Slug = 'audio_look_and_feel'; Title = 'Audio Look and Feel'; Prompt = 'Describe the emotional effect the audio should create for the player.' }
        )
    },
    @{
        Prefix = '06'
        Slug = 'story_and_narrative'
        Title = 'Story and Narrative'
        Summary = 'Captures the backstory, main plot, pacing, and narrative delivery.'
        Topics = @(
            @{ Prefix = '01'; Slug = 'backstory'; Title = 'Backstory'; Prompt = 'Summarize the important events that occurred before the game begins.' },
            @{ Prefix = '02'; Slug = 'main_plot'; Title = 'Main Plot'; Prompt = 'Describe the main plot in compact form, focused on game-relevant events.' },
            @{ Prefix = '03'; Slug = 'plot_progression'; Title = 'Plot Progression'; Prompt = 'Explain how the plot unfolds across the game.' },
            @{ Prefix = '04'; Slug = 'cutscenes'; Title = 'Cutscenes'; Prompt = 'Describe how cutscenes are used and what role they play in gameplay delivery.' }
        )
    },
    @{
        Prefix = '07'
        Slug = 'characters'
        Title = 'Characters'
        Summary = 'Defines the main cast, support cast, enemies, and their narrative or gameplay roles.'
        Topics = @(
            @{ Prefix = '01'; Slug = 'main_characters'; Title = 'Main Characters'; Prompt = 'Identify the main characters and summarize why each matters.' },
            @{ Prefix = '02'; Slug = 'character_backstory'; Title = 'Character Backstory'; Prompt = 'Capture the relevant past of the main characters.' },
            @{ Prefix = '03'; Slug = 'personality'; Title = 'Personality'; Prompt = 'Describe how each major character behaves, speaks, and makes decisions.' },
            @{ Prefix = '04'; Slug = 'appearance'; Title = 'Appearance'; Prompt = 'Describe the visual identity of the major characters.' },
            @{ Prefix = '05'; Slug = 'abilities'; Title = 'Abilities'; Prompt = 'Document what major characters can do and how those abilities affect play or plot.' },
            @{ Prefix = '06'; Slug = 'relationships'; Title = 'Relationships'; Prompt = 'Explain the important connections between major characters.' },
            @{ Prefix = '07'; Slug = 'supporting_characters'; Title = 'Supporting Characters'; Prompt = 'Describe the support cast and their functional role in the game.' },
            @{ Prefix = '08'; Slug = 'enemies'; Title = 'Enemies'; Prompt = 'Describe the enemy groups or individuals and why they matter.' }
        )
    },
    @{
        Prefix = '08'
        Slug = 'game_world'
        Title = 'Game World'
        Summary = 'Describes the world itself, its key places, and the structure of its levels.'
        Topics = @(
            @{ Prefix = '01'; Slug = 'world_look_and_feel'; Title = 'World Look and Feel'; Prompt = 'Describe the atmosphere, identity, and presentation of the world itself.' },
            @{ Prefix = '02'; Slug = 'locations'; Title = 'Locations'; Prompt = 'List the most important locations and why they matter.' },
            @{ Prefix = '03'; Slug = 'connection_to_the_plot'; Title = 'Connection to the Plot'; Prompt = 'Explain how each key location supports plot development.' },
            @{ Prefix = '04'; Slug = 'levels'; Title = 'Levels'; Prompt = 'Describe the overall level structure and level taxonomy.' },
            @{ Prefix = '05'; Slug = 'tutorial_levels'; Title = 'Tutorial Levels'; Prompt = 'Document how tutorial content teaches the player and fits into the game.' },
            @{ Prefix = '06'; Slug = 'main_levels'; Title = 'Main Levels'; Prompt = 'Summarize the main levels and the purpose each serves.' },
            @{ Prefix = '07'; Slug = 'optional_levels'; Title = 'Optional Levels'; Prompt = 'Describe optional levels or side areas and what they add.' }
        )
    }
)

$rootReadme = Join-Lines @(
    "# Game Design Document"
    ""
    "## Purpose"
    "This standalone folder contains the structured GDD for the project. It is organized for readability, maintenance, and team handoff."
    ""
    "## How To Use This Tree"
    '- Read `INDEX.md` first.'
    '- Move from domain folders to topic folders.'
    '- Use governance to track decisions, reviews, and lifecycle state changes.'
    ""
    "## Conventions"
    '- every folder contains `README.md` and `INDEX.md`'
    "- topic folders are the main writing surfaces"
    '- status and process rules live under `00_governance/`'
)

$rootIndexLines = @(
    "# Game Design Document Index"
    ""
    "## Top-Level Domains"
)

foreach ($domain in $domains) {
    $rootIndexLines += ('- `./{0}_{1}/` - {2}' -f $domain.Prefix, $domain.Slug, $domain.Title)
}

$rootIndexLines += ""
$rootIndexLines += "## Recommended Authoring Order"
$rootIndexLines += '- `00_governance/`'
$rootIndexLines += '- `01_introduction/`'
$rootIndexLines += '- `02_game_overview/`'
$rootIndexLines += '- `03_gameplay/` and `04_mechanics/`'
$rootIndexLines += '- `05_graphics_and_audio/`, `06_story_and_narrative/`, `07_characters/`, `08_game_world/`'

New-DocPair -Directory $root -Readme $rootReadme -Index (Join-Lines $rootIndexLines)

foreach ($domain in $domains) {
    $domainDir = Join-Path $root ("{0}_{1}" -f $domain.Prefix, $domain.Slug)
    New-DocPair -Directory $domainDir -Readme (Get-DomainReadme $domain) -Index (Get-DomainIndex $domain)

    foreach ($topic in $domain.Topics) {
        $topicDir = Join-Path $domainDir ("{0}_{1}" -f $topic.Prefix, $topic.Slug)
        New-DocPair -Directory $topicDir -Readme (Get-TopicReadme $domain $topic) -Index (Get-TopicIndex $domain $topic)
    }
}
