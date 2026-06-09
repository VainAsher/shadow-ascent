# 2026-05-10 GDD Critical Review

## Purpose
Record a critical design review of `game_design_document/` focused on weak decision areas, research gaps, designer choices still needing lock-in, and sections that are not yet production-useful enough.

## Main Findings
- The GDD is now strong on narrative spine, character meaning, movement/combat philosophy, world logic, and structural organization.
- The weakest areas are still market validation, concrete production art/audio choices, true MVP scoping, measurable acceptance criteria, and prioritized open questions.
- Several sections are directionally good but still too soft for implementation handoff.

## Priority Gaps

### 1. MVP Scope Is Too Broad
Affected file:
- `../../../11_production_scope_and_roadmap/04_mvp_feature_set/README.md`

Issue:
- The current MVP reads more like a full campaign alpha or near-ship target than a minimum viable product.

Needed follow-up:
- split scope into vertical slice, campaign alpha, and ship target
- decide which systems are required to prove the fantasy versus required to ship

### 2. Acceptance Criteria Are Not Yet Pass/Fail
Affected file:
- `../../../12_qa_telemetry_and_acceptance_criteria/03_acceptance_criteria/README.md`

Issue:
- Many criteria are emotionally correct but not measurable enough to guide QA signoff.

Needed follow-up:
- convert key criteria into observable or time-bounded checks
- define what counts as acceptable for movement learning, difficulty readability, and narrative comprehension

### 3. Audience Is Positioning, Not Validation
Affected file:
- `../../../02_game_overview/02_audience/README.md`

Issue:
- The audience section describes who the game seems for, but not who has actually been validated through testing or external response.

Needed follow-up:
- add market-fit research, playtest evidence, or external reaction data before making stronger claims

### 4. Visual And Audio Direction Need Harder Production Choices
Affected files:
- `../../../05_graphics_and_audio/01_visual_system/README.md`
- `../../../05_graphics_and_audio/05_audio_system/README.md`

Issue:
- Both sections have strong emotional goals but still lack enough fixed choices for contractor or implementation guidance.

Needed follow-up:
- art format lock
- readability tests
- audio layer priorities
- motif ownership for key characters and regions

### 5. Open Questions Need Prioritization
Affected file:
- `../../../13_risks_open_questions_and_studio_recommendations/02_open_questions/README.md`

Issue:
- Questions of very different urgency are grouped together without sequencing.

Needed follow-up:
- split near-term production blockers from later design refinements

## Designer Questions Flagged For Review
- What is the actual MVP versus the vertical slice versus the campaign ship target?
- What is the final public-facing art format: strict pixel art or hybrid high-res 2D?
- What is the final functional role of Lantern: resource, tool, checkpoint amplifier, or mainly symbolic layer?
- What is Flow in systemic terms, and does it need UI exposure?
- How many ending states are truly worth producing?
- Is co-op a shipped promise, a validated future branch, or only engineering scaffolding?
- What minimum external demo slice is strong enough to show publicly?

## Research Areas Flagged
- audience validation and market-fit testing
- visual development and readability studies
- audio motif and implementation planning
- playtest evidence for emotional pacing, movement/combat readability, and stance comprehension

## Source Basis
- `../../../INDEX.md`
- `../../../02_game_overview/02_audience/README.md`
- `../../../05_graphics_and_audio/01_visual_system/README.md`
- `../../../05_graphics_and_audio/05_audio_system/README.md`
- `../../../11_production_scope_and_roadmap/04_mvp_feature_set/README.md`
- `../../../12_qa_telemetry_and_acceptance_criteria/03_acceptance_criteria/README.md`
- `../../../13_risks_open_questions_and_studio_recommendations/02_open_questions/README.md`
