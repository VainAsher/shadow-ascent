# Player Camera

## Current Truth
The production-client scaffold already includes a player-follow camera with world-bounds clamping. The current camera is functional rather than expressive, but it establishes the correct baseline: the camera follows Aen through authored traversal space and does not expose geometry beyond the bounded world.

## Camera Design Rule
The camera should support:
- movement readability
- traversal timing
- combat telegraph clarity
- emotional scale of each region

It should not feel floaty, disorienting, or overly cinematic at the cost of control precision.

## Region Feel
- **Lantern Heights** camera can feel steady, welcoming, and readable.
- **Summit Shrine** can narrow the sense of certainty through framing without becoming frustrating.
- **Hollow Depths** should support compression and weight.
- **Ember Monastery** should restore breathable clarity.
- **Winding Skyroad** should communicate exposure, height, and ascent.
- **Mirror Summit** should emphasize confrontation and self-focus.
- **Beacon Cliff** should allow more stillness and horizon.

## Technical Direction
The current Y-down orthographic approach and world-bounds clamping are valid foundations. As the visual layer improves, the camera can add more authored nuance, but it should remain subservient to platforming readability first.

## Source Basis
- `docs/CURRENT_STATE.md`
