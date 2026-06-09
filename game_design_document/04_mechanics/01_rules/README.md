# Rules

## Current Repo-Backed Truth
The game is built on explicit story flags, mission objectives, traversal gates, ability checks, and authored beat ordering. Progress is not meant to be freeform enough to break the critical path, even when optional content is elastic.

## Core Rule Set
- critical progression is controlled by beats and flags
- elastic content must not contradict campaign truth
- traversal access is ability-gated
- mission completion is objective-driven rather than auto-completed
- save/load must preserve authoritative story state

## Combat Rules
The combat layer should obey a few non-negotiable design rules:
- movement is the primary survival tool
- enemies threaten space before they threaten numbers
- unarmed and armed play are different emotional-combat identities, not simple damage variants
- stealth, open combat, and traversal pressure should coexist inside the same encounter language
- not every attack is parryable, because awareness must matter as much as timing

## Encounter Rule
An encounter should always be readable as a spatial problem with emotional pressure. A useful internal test is:
- what space does this enemy threaten
- what movement answer solves it
- what emotional pressure does it represent

If an encounter cannot answer those questions, it is probably too stat-driven for this project.

## Design Implication
This is a heavily authored game with controlled flexibility, not a sandbox where every systemic outcome is equally valid.

That applies to combat as well. The game should not drift into pure spectacle-combo action or passive tanking. Positional control, route reading, and emotional discipline are the intended mastery path.

## Source Basis
- combat and movement design review provided by user on 2026-05-10
- `data/plateaus.json`
- `data/narrative_beats.json`
- `docs/CURRENT_STATE.md`
