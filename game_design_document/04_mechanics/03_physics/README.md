# Physics

## Current Repo-Backed Truth
The clean-start repo already carries donor-calibrated movement constants and a deterministic controller model. The playable slices implement gravity, jump logic, coyote windows, wall interaction, dash behavior, and bounded collision geometry.

## What Is Proven
- run and precision-walk states
- jump, double-jump, coyote time
- wall slide, wall jump, wall exhaustion behavior
- dash timing and cooldown
- collision against authored geometry

## What Is Not Yet Final
The current physics stack is strong enough for QA and feel validation, but it is not yet the final full production surface for every world interaction or client context.

## Source Basis
- `docs/CURRENT_STATE.md`
- `java/core/src/main/java/com/shadowascent/core/physics/PhysicsConstants.java`
