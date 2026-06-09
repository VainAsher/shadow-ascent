# Player Interaction

## Core Interaction Types
The player currently interacts with the game through:
- movement and traversal surfaces
- enemies and boss-state windows
- NPCs and dialogue triggers
- mission start points and objective progress hooks
- ability-gated blockers
- dash-pass sigils and interact altars
- moving platforms and route geometry
- pickups, inventory, shop, and crafting panels
- save/load triggers and route continuity systems

## Combat Interaction Modes
The player should be able to shift between two interaction identities:
- **unarmed**: stealth, control, non-lethal pressure, faster cancel rhythm, lower noise
- **armed**: direct confrontation, lethality, reach, stagger power, louder commitment

These should not behave like cosmetic weapon swaps. They are different movement-combat identities that shape stealth viability, encounter pressure, and ending tone.

## Design Principle
Interactions should not feel mechanically disconnected from the story. Each major interaction type should reinforce one of the game's core patterns:
- traversal as recovery
- dialogue as orientation
- combat as pressure or reckoning
- support interactions as rebuilding
- world triggers as proof that progression changes reality

## Stealth And Awareness
Stealth should be integrated into encounter interaction rather than split into a separate game mode. Enemy awareness should move through readable states:
- unaware
- suspicious
- alerted
- searching
- combat

Those states should respond to sight, sound, bodies, combat noise, and environmental disturbance. Armed interaction should generate more noise than unarmed interaction.

## NPC Interaction
NPC interaction should do more than deliver exposition. It should:
- clarify the next route or emotional state
- deepen relationships
- change as the world state changes
- make absence and return meaningful

## Defensive Interaction
Combat interaction should include layered defense:
- block as lower-risk posture management
- parry as high-risk timing defense that creates stagger or counter windows
- counters as payoff after parry, stagger, or stealth openings

Not every attack should be parryable. Grabs, behind-hits, ambushes, crush attacks, or designated boss patterns should bypass easy timing dominance and preserve awareness as a meaningful skill.

## Ability Interaction
The strongest interaction design in the project is currently ability-linked world response. Dash-pass and altar-style triggers already point in the right direction: the world should visibly answer recovered capability.

## Encounter Interaction Rule
Encounters should be authored as spatial pressure compositions, not just enemy piles. In practical terms, player interaction inside combat should revolve around:
- reading threatened space
- choosing movement answers
- preserving route control
- deciding between restraint and escalation

## Interaction Readability
The game should always help the player distinguish between:
- decorative world detail
- conversational contact
- mechanical affordance
- ability-gated affordance
- mission-critical affordance

The same clarity rule should apply to combat states: players should be able to read whether they are in a stealth opportunity, a stagger window, a route-preservation moment, or a collapse-risk scramble.

## Source Basis
- combat and movement design review provided by user on 2026-05-10
- `docs/CURRENT_STATE.md`
- `README.md`
