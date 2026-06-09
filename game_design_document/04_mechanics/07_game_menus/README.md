# Game Menus

## Current Truth
The project already has an in-session panel structure even though it does not yet have a polished full game-flow menu stack. The implemented menu-like surfaces currently include:
- inventory
- shop
- crafting
- save/load access
- mission and HUD feedback

## Current Panel Roles
- **Inventory** manages consumables, weapon and armor state, and quick item reasoning.
- **Shop** supports buying and selling through an NPC-mediated economy surface.
- **Crafting** turns collected materials into practical advancement and utility.

## Intended Menu Philosophy
Menus in Shadow Ascent should feel purposeful and grounded, not like detached spreadsheet interruptions. They should support:
- continuity of play
- clear decision-making
- emotional readability
- low-friction return to movement and world exploration

## Missing But Expected Future Surfaces
The repo does not yet prove a finished:
- title screen
- pause menu hierarchy
- settings/options screen
- save-slot selection flow
- credits or ending flow UI

These should be treated as planned product surfaces, not current implementation truth.

## Player Path
The ideal menu path is:
- world exploration
- contextual interaction or pause
- fast, legible decision surface
- immediate return to movement

The menu system should preserve the game's pacing rather than stall it.

## Source Basis
- `docs/CURRENT_STATE.md`
