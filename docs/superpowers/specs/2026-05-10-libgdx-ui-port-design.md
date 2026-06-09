---
doc_type: design_spec
status: draft
owner: core-team
last_updated: 2026-05-10
version_anchor: 0.0.1
---
# LibGDX UI Port Design

## Goal

Port donor-derived player-facing UI features from the Swing `runPlayableClient` surface into the LibGDX `runGame` surface while redesigning them into one coherent production-client overlay system.

In scope:

- persistent HUD for the LibGDX client
- toggleable minimap inside the HUD
- modal inventory overlay
- modal shop overlay
- modal crafting overlay
- shared input and lifecycle rules for all overlays

Out of scope:

- full art/polish pass
- screen-to-screen menu flow redesign
- narrative/content expansion
- broad gameplay rewrites
- full animation system work beyond what is required to support the UI shell

## Current Context

The repository already contains donor-derived UI features, but they are integrated into the Swing `runPlayableClient` path rather than the LibGDX `runGame` path.

Existing Swing-side sources:

- `UISubsystem`
- `HudRenderer`
- `MinimapRenderer`
- `InventoryPanel`
- `ShopPanel`
- `CraftingPanel`
- `PlaytestClient` panel orchestration and input routing

Existing LibGDX-side sources:

- `ShadowAscentGame`
- `HubScreen`
- `GameInputProcessor`
- `SpriteWorldRenderer`

The LibGDX client currently has simulation, camera, collision, input, and atlas-backed world/entity rendering, but no production-client HUD or overlay layer.

## Architecture

The LibGDX production client should use a single in-game overlay stack rather than switching full screens for gameplay-adjacent UI.

Layers:

1. `Gameplay layer`
   - world rendering
   - sprite/entity rendering
   - camera and simulation tick
2. `Persistent HUD layer`
   - always visible during gameplay
   - shows compact status, mission, hints, feed, and minimap
3. `Modal overlay layer`
   - exactly one active modal at a time
   - inventory, shop, or crafting
4. `Input router`
   - routes keys either to gameplay or to the active modal
5. `Overlay manager`
   - owns open/close/replace semantics

This keeps the player in one runtime surface, preserves context behind modals, and avoids duplicating screen-management logic.

## HUD Layout

Use a stable three-zone HUD tuned for 1280x720 first.

Zones:

- `Top-left status block`
  - health
  - act/plateau
  - active mission title
  - short objective/progress line
- `Top-right utility block`
  - minimap
  - overlay status
  - encounter focus when relevant
- `Bottom-left event feed`
  - recent runtime messages
  - ability unlocks
  - gate warnings
  - inventory/shop/crafting action feedback
- `Bottom-center contextual hint`
  - single-line prompt for combat/traversal/interaction guidance

Design intent:

- preserve route readability
- reduce the dense QA-dashboard look of the Swing runtime
- make the production client feel like one UI system rather than several imported tools

## Modal Overlay Design

All modals should share one visual language.

Shared characteristics:

- centered panel
- dark translucent backdrop
- clear title bar
- compact footer hint row
- world remains visible but dimmed behind the modal

Panel sizing:

- inventory: medium centered panel
- crafting: medium centered panel
- shop: wide centered panel with left/right buy-sell columns

Only one modal may be open at a time.

## Interaction Model

When no modal is open:

- gameplay input remains active
- HUD remains visible
- minimap toggle remains available

When a modal is open:

- movement/combat/world interaction input is suppressed
- camera and world continue rendering behind the modal
- the active modal receives directional/confirm/cancel input

Key rules:

- `M`: toggle minimap visibility
- `I`: open/close inventory modal
- `T`: open/close crafting modal
- `E` near merchant: open shop modal
- `Esc`: close the active modal first; otherwise no-op

Replacement semantics:

- opening one modal closes any existing modal
- `I` or `T` while another modal is open replaces it rather than stacking
- shop cannot be opened globally; it requires a valid interaction target

## Data and Responsibility Split

The Swing classes should not be copied wholesale into LibGDX rendering code. The port should separate reusable UI state and behavior from toolkit-specific drawing.

Preferred direction:

- reuse domain-side logic and panel state transitions where they are toolkit-agnostic
- replace Swing `Graphics2D` drawing with LibGDX rendering code
- centralize modal lifecycle in a LibGDX overlay manager instead of keeping it inside each panel

Expected LibGDX-side responsibilities:

- `HudOverlayRenderer`
  - draws the persistent HUD
- `MinimapOverlayRenderer`
  - draws the minimap inside the HUD
- `ModalOverlayManager`
  - owns active modal state
  - routes open/close/replace behavior
- one LibGDX overlay renderer/controller per modal
  - inventory
  - shop
  - crafting

If useful during implementation, shared panel navigation state may be extracted into small toolkit-neutral models.

## Port Sequence

Recommended order:

1. `HUD shell + minimap`
2. `Inventory`
3. `Shop`
4. `Crafting`
5. `Cleanup and unification`

Rationale:

- the HUD shell is foundational for later overlays
- inventory is the simplest modal proof
- shop adds contextual open rules and two-column focus
- crafting reuses the modal shell and event-feedback path
- cleanup comes after real integration pressure exposes the right seams

## Error Handling and Edge Cases

- opening a modal when another is active should replace it cleanly
- modal close should fully restore gameplay input
- absent merchant target should prevent shop open and leave gameplay active
- empty inventory/shop/crafting states must render gracefully
- feedback from modal actions should route into the shared event feed rather than creating isolated per-panel messaging

## Testing Strategy

Implementation must be test-backed in bounded slices.

Required verification classes:

- client-side tests for modal lifecycle rules
- client-side tests for input routing when modal state changes
- client-side tests for minimap/HUD state toggles
- regression-safe verification that existing `runGame` simulation/render path still compiles and runs through the asset pipeline

Command-level verification:

- `.\gradlew.bat :client:test`
- `.\gradlew.bat :client:compileJava`
- `.\gradlew.bat packSprites`

Docs verification after milestone-relevant changes:

- `python scripts/check_docs_freshness.py --emit-report`

## Success Criteria

This design is complete when:

- `runGame` has a persistent HUD
- minimap is visible and toggleable in the LibGDX client
- inventory, shop, and crafting are accessible through a shared modal system
- only one modal can be active at a time
- gameplay input is correctly suppressed while a modal is open
- documentation reflects the ported truth accurately

## Recommended First Implementation Slice

Start with the HUD shell and minimap.

That slice establishes:

- the overlay rendering pass
- the basic input-routing boundary between gameplay and UI
- the visual style the later modal panels should inherit

It is the highest-leverage first port and the lowest-risk way to validate the redesigned production-client UI direction.
