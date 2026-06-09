# Interface

## Current Truth
The repo already has a meaningful gameplay interface surface. The current UI communicates:
- mission progress
- next-objective direction
- unlocked abilities
- traversal gate state
- interaction hints
- minimap state
- combat or encounter context
- inventory, shop, and crafting information

## Interface Philosophy
The interface should support player orientation rather than dominate the screen. In this project, UI has three main jobs:
- tell the player what matters now
- remind them what changed
- keep the emotional and spatial route readable

## Interface Layers
- **HUD**: immediate player state, mission context, ability status, feedback lines
- **Minimap**: route readability, entity and gate context, spatial reassurance
- **Interaction Hints**: nearby affordance clarity without constant noise
- **Panels**: inventory, shop, crafting, and future pause/settings flows

## Tone Rule
The UI should feel supportive, not sterile. Since the project is emotionally explicit, interface language should help the player orient without turning every moment into an abstract systems dashboard.

## Long-Term Gap
The current interface is still closer to an internal-quality readability layer than a final shippable presentation pass. It is doing the right jobs, but it will later need stronger visual identity, hierarchy, and polish.

## Source Basis
- `docs/CURRENT_STATE.md`
