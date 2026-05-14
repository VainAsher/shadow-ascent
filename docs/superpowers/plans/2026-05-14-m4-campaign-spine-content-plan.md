# M4 Campaign Spine Content Plan

**Purpose:** Make the remaining M4 campaign scope explicit in implementation-facing terms so content work is not left as a vague "later acts" bucket.

## Canonical Plateau Spine

1. `LANTERN_HEIGHTS`
2. `SUMMIT_SHRINE`
3. `HOLLOW_DEPTHS`
4. `EMBER_MONASTERY`
5. `WINDING_SKYROAD`
6. `MIRROR_SUMMIT`
7. `BEACON_CLIFF`

## Plateau Jobs

| Plateau | Primary emotional job | Primary mechanical job | Runtime delivery expectation |
| --- | --- | --- | --- |
| `LANTERN_HEIGHTS` | belonging, warning, first isolation | onboarding, first mission, social routing | hub readability, tutorial flow, first contract-backed mission |
| `SUMMIT_SHRINE` | false elevation, reveal, scripted loss | controlled point-of-no-return sequence | authored collapse route and loss presentation |
| `HOLLOW_DEPTHS` | grief, weight, survival, first sparks | recovery arc and movement restoration | authored dungeon chain plus side-quest support and survival feedback |
| `EMBER_MONASTERY` | support, rebuilding, community return | training and support-driven ability restoration | authored support hub, forge/dojo loops, return-of-allies beats |
| `WINDING_SKYROAD` | ascent, acceptance, exposed mastery | full-kit traversal integration | authored vertical ascent and route-legibility with endgame mobility |
| `MIRROR_SUMMIT` | self-confrontation and release | final ability gauntlet and boss lead-in | authored final-approach route and Hollow Reflection confrontation setup |
| `BEACON_CLIFF` | closure, wholeness, future-facing homecoming | ending-state traversal and final interaction beats | authored resolution route, final NPC reactions, beacon ending |

## Checkpoint Matrix

| Checkpoint | Plateau | Emotional function | Mechanical function | Status |
| --- | --- | --- | --- | --- |
| Lantern Heights First Missions | `LANTERN_HEIGHTS` | home, belonging, warning | onboarding, NPC interaction, first mission | partial runtime-present |
| Summit Shrine | `SUMMIT_SHRINE` | isolation, reveal, scripted loss | controlled critical-route sequence | authored in data, partial runtime-present |
| Weightbound Mines | `HOLLOW_DEPTHS` | exhaustion, burden | Dash recovery lane | authored in data, not yet clearly staged in `runGame` |
| Shatter Moth Nest | `HOLLOW_DEPTHS` | doubt, false pathing | Double Jump recovery lane | authored in data, not yet clearly staged in `runGame` |
| Stone Judge Maze | `HOLLOW_DEPTHS` | judgement, survival under pressure | Glide gate setup and flanking logic | authored in data, not yet clearly staged in `runGame` |
| Hearth of Brothers | `EMBER_MONASTERY` | support and belonging restored | support-hub transition | data-authored, runtime-pending |
| Mentor Roga's Dojo | `EMBER_MONASTERY` | disciplined rebuilding | Wall Cling progression | data-authored, runtime-pending |
| Winding Skyroad Ascent | `WINDING_SKYROAD` | acceptance through ascent | full-kit traversal | data-authored, runtime-pending |
| Mirror Summit | `MIRROR_SUMMIT` | confrontation and release | final boss lead-in | data-authored, runtime-pending |
| Beacon of Return | `BEACON_CLIFF` | closure and future | ending interaction path | data-authored, runtime-pending |

## What "M4 Done" Means

M4 should be considered complete only when:

1. Each plateau in the canonical spine has runtime-legible authored coverage, not only contract presence.
2. `runGame` or the explicitly chosen primary QA surface can express:
   - current authored area identity
   - current authored objective
   - relevant authored NPC presence
   - plateau transition readiness
3. Optional content remains plateau-valid and does not violate critical-route constraints.
4. The later plateaus (`EMBER_MONASTERY` through `BEACON_CLIFF`) are no longer "data only"; they are represented by visible runtime flow or a clearly bounded staged rollout plan.

## Immediate Recommended Tranche

The next best M4 content tranche is:

1. `EMBER_MONASTERY` support-and-training cluster
2. one explicitly staged `HOLLOW_DEPTHS` recovery dungeon if the runtime still needs a stronger transition into `EMBER_MONASTERY`

Reason:

- `EMBER_MONASTERY` is the first plateau after the currently strongest authored recovery arc.
- It contains support return, forge, dojo, and map/lantern preparation beats that improve both mission surfacing and authored NPC placement work.
