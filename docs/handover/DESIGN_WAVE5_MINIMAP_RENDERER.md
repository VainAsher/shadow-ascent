---
handover_type: design
milestone: Wave5
topic: minimap_renderer
status: in_progress
created: 2026-05-08
---
# Design — Wave 5 MinimapRenderer Extraction

Extracts the `drawMinimap` private method from `UISubsystem` into a standalone
`MinimapRenderer` class in `com.shadowascent.client`. Improves UISubsystem decomposition
and gives the minimap logic a dedicated, testable home — aligning with the same extraction
pattern used for CombatSubsystem, TraversalSubsystem, and UISubsystem themselves.

The donor `indie-ninja-adventures` had a dedicated `MinimapRenderer` class using LibGDX.
This extraction adapts the structure (not the LibGDX rendering) to produce an equivalent
standalone Swing-based renderer.

---

## Scope

### In scope
- `MinimapRenderer.java` — new file in `com.shadowascent.client`
- `UISubsystem` — delegates `drawMinimap` to `MinimapRenderer`; `drawMinimap` method deleted
- `activeNpcsSorted()` helper moved from `UISubsystem` to `MinimapRenderer` (only used by minimap)
- Regression gate: `client:compileJava` proves extraction is correct; no new core regression section (core cannot import client classes)

### Out of scope
- Functional changes to minimap rendering behaviour
- Donor LibGDX MinimapRenderer port (rendering rewritten for Swing, as with all Wave 5 panels)

---

## Layer contract
- `MinimapRenderer` lives in `com.shadowascent.client` — no `core` simulation imports
- Allowed imports: `core.physics.TileRect`, `core.StoryState`, `core.NPC` (same as UISubsystem already uses)

---

## Files created / modified

| File | Change |
|---|---|
| `client/MinimapRenderer.java` | New file — extracted minimap drawing logic |
| `client/UISubsystem.java` | `drawMinimap` deleted; `MinimapRenderer` field added; delegated in `drawFrame`; `activeNpcsSorted()` removed |
| `core/RegressionTest.java` | `testMinimapRendererExtraction` section; dispatch; 43→44 tests |

---

## `MinimapRenderer` design

```java
package com.shadowascent.client;

final class MinimapRenderer {
    // Panel geometry constants
    static final int PANEL_W = 240;
    static final int PANEL_H = 165;
    static final int INNER_PAD = 10;
    static final int PANEL_MARGIN_RIGHT = 16;
    static final int PANEL_TOP = 102;

    private final WorldGeometry geometry;
    private final java.util.List<com.shadowascent.core.physics.TileRect> collisionTiles;
    private final TraversalSubsystem traversalSubsystem;
    private final CombatSubsystem combatSubsystem;
    private final com.shadowascent.core.StoryState storyState;
    private final java.util.Map<String, java.awt.geom.Point2D.Float> npcPositions;

    MinimapRenderer(WorldGeometry geometry,
                    java.util.List<com.shadowascent.core.physics.TileRect> collisionTiles,
                    TraversalSubsystem traversalSubsystem,
                    CombatSubsystem combatSubsystem,
                    com.shadowascent.core.StoryState storyState,
                    java.util.Map<String, java.awt.geom.Point2D.Float> npcPositions) { ... }

    void draw(java.awt.Graphics2D g, UISubsystem.RenderState state) { ... }

    private java.util.List<com.shadowascent.core.NPC> activeNpcsSorted() { ... }
}
```

## UISubsystem changes

1. Add field: `private final MinimapRenderer minimapRenderer;`
2. Constructor: initialise `minimapRenderer = new MinimapRenderer(geometry, collisionTiles, traversalSubsystem, combatSubsystem, storyState, npcPositions);`
3. Replace `drawMinimap(g, state)` call → `minimapRenderer.draw(g, state)`
4. Delete `private void drawMinimap(Graphics2D g, RenderState state)` method body
5. Delete `private List<NPC> activeNpcsSorted()` helper (moved to MinimapRenderer)

---

## Regression tests (3 sub-tests)

### `testMinimapRendererExtraction`

These are structural/smoke checks using `WorldGeometry` with representative bounds —
no real rendering, just verifying the extraction compiles and constants are sensible.

1. **panel constants** — `PANEL_W == 240`, `PANEL_H == 165`, `INNER_PAD == 10`
2. **constructor succeeds** — construct `MinimapRenderer` with empty collision tiles and null-safe stubs; no exception
3. **draw does not throw** — call `minimapRenderer.draw(g, state)` on a buffered image Graphics2D context; no exception

---

## Prior test count: 43 → Target: 44/44
