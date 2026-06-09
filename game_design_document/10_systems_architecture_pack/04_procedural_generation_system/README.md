# Procedural Generation System

## Design Position
Procgen should operate as a constrained content pipeline, not as freeform randomness.

## Current Hierarchy
- world plan
- region plan
- dungeon plan
- room build
- zone pass
- tile pass
- feature pass
- validation pass

## Debug Expectations
The pack usefully implies that this system needs strong visibility:
- authored path versus optional generation
- ability gate overlays
- reachability views
- NPC and light-state matrices
- seed lineage
- room object inspection

## Source Basis
- `C:\Users\asher\assets for game\Shadow_Ascent_The_Hollowed_Ninja_GDD_Pack.md`
