# Saving

## Current Repo-Backed Truth
Saving is already treated as a serious system. The clean-start repo implements a versioned save envelope with backward-compatible migration and checksum validation.

## Current Save Model
- current envelope: `SAVE_V3`
- backward-compatible support for older save formats
- checksum guard to detect tampering or truncation
- save/load available from the playable client

## Design Clarification
The studio pack sharpens what saving needs to mean at the GDD level:
- save points, checkpoints, and restore behavior must preserve narrative and ability state safely
- persistence must survive death, quit, and version migration without corrupting progression
- save rules need to respect story sequencing, boss state, region access, and hub changes
- save restoration should be reliable enough to support critical-path validation and procedural seed debugging

## GDD Implication
The GDD should describe saving as player-available continuity support, not as a loose prototype convenience. The project already expects persistent story state and migration discipline.

## Safe Design Position
It is reasonable to write this project as a migration-aware, progression-critical save system. It is not yet reasonable to promise final checkpoint UX details that the client has not fully stabilized.

## Source Basis
- `docs/CURRENT_STATE.md`
- `C:\Users\asher\assets for game\Shadow_Ascent_The_Hollowed_Ninja_GDD_Pack.md`
