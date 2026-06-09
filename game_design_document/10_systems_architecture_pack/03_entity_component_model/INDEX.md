# Entity Component Model Index

## Checklist
- [ ] Keep components generic enough to be reusable but specific enough to guide implementation
- [ ] Track which components are required by player, NPC, enemy, boss, and interactable classes of entity
- [ ] Note where narrative tagging and gameplay logic need to meet
- [ ] Record any component split or merge that affects save, procgen, or tooling assumptions

## Drift Risks
- Entity design can become ad hoc if recurring concepts are not normalized early
- Over-granular component design can create complexity without clear gameplay payoff
