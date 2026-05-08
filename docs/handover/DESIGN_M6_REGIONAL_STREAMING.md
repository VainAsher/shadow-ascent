---
handover_type: design_doc
milestone: M6
status: ready-for-implementation
created: 2026-05-08
---
# Design — M6 Regional Streaming Architecture

Workflow 2 (architecture lane), Step 1 output. Defines the new abstractions, layer boundaries,
data contracts, save implications, and acceptance-test targets for expanding the fixed
four-room traversal route into a constraint-driven regional streaming topology with
runtime mutation overlays.

---

## 1. Context

The existing authored four-room traversal route in `PlaytestClient` hardcodes geometry into
`initializeCollisionLayout`. M6 replaces this with a runtime model where regions are defined
by progression graph nodes, resolved to section templates, and populated with dynamic collision
geometry per load. Mutation overlays apply world simulation pressure signals (from
`WorldSimulationTick`) to modify `MutableZone` content within each region.

### Existing foundations M6 builds on

| Foundation | Location | What it provides |
|---|---|---|
| `WorldProgressionGraph` | `core.world.progression` | Macro graph: `ProgressionNode` (CENTRAL_HUB / REGION_HUB / DUNGEON), `RegionHub` portal/shortcut rules, critical-path list |
| `SectionTemplate` | `core.world.sections` | Authored pacing chunks: biome, kind, footprint, `MutableZone[]`, `Anchor[]`, required sockets |
| `SectionTemplateLibrary` | `core.world.sections` | `select(biome, kind, seed)` for deterministic template resolution |
| `SpatialHash` | `core.physics` | Chunk-based tile lookup with dynamic overlay support (`setDynamicTiles`) |
| `WorldSimulationPressureSample` | `core.simulation` | Per-region corruption/prosperity/routeRisk/tension signals |
| `SectionTemplate.MutableZone` | `core.world.sections` | Named addressable zones within a section that overlays can modify |
| `SectionTemplate.Anchor` | `core.world.sections` | Spawn hooks with `requires`/`forbids` constraints |

None of these need modification. M6 adds a new package above them.

---

## 2. New Package

All M6 streaming types live in **`com.shadowascent.core.world.streaming`**.

Layer contract: no imports from `client` or `server`. No Swing/AWT. `PlaytestClient` and
`UISubsystem` depend on this package; this package does not depend on either.

---

## 3. New Abstractions

### 3a. `RegionManifest` (record)

Serializable description of a resolved region. Stable round-trip contract for save/load.

```java
record RegionManifest(
    String regionId,               // matches ProgressionNode.id()
    String selectedTemplateId,     // SectionTemplate.id() chosen by RegionLoader
    String biome,
    String kind,
    long seed,                     // seed used for template selection + anchor placement
    List<SocketBinding> sockets,   // socket connections to adjacent regions
    List<ZoneOverride> zoneOverrides // active MutationOverlay state at save time
) {}

record SocketBinding(String socketId, String connectedRegionId) {}

record ZoneOverride(
    String zoneRole,               // MutableZone.role — NOT position-based
    String overlayKind,            // e.g. "CORRUPTION_SURGE", "ROUTE_HAZARD"
    Map<String, String> params     // overlay-kind-specific parameters
) {}
```

`zoneRole` addresses zones by role rather than grid position so that section-template
revisions do not break saved overlay state in existing save files.

### 3b. `RegionInstance` (class)

Runtime representation of a loaded region. Owns the resolved spatial hash for that
region's collision geometry and the active NPC/encounter anchors.

```java
class RegionInstance {
    String regionId()
    RegionManifest manifest()
    SpatialHash spatialHash()                     // populated at load; updated when overlays change
    List<TileRect> staticTiles()                  // collision tiles from template geometry
    List<TileRect> overlayTiles()                 // tiles injected by active mutation overlays
    List<ResolvedAnchor> anchors()               // encounter/NPC spawn points resolved from template
    boolean isLoaded()
}

record ResolvedAnchor(
    String anchorId,
    String kind,                                  // from SectionTemplate.Anchor.kind()
    float worldX, float worldY,
    List<String> satisfiedRequires,
    boolean active
) {}
```

`RegionInstance` is the object `PlaytestClient` (and later `UISubsystem`) consults for
collision tiles — it replaces the flat hardcoded lists in `initializeCollisionLayout`.

### 3c. `RegionLoader` (class)

Resolves a `ProgressionNode` from `WorldProgressionGraph` to a `RegionManifest` and then
to a live `RegionInstance`. Validates connectivity before returning.

```java
class RegionLoader {
    RegionLoader(SectionTemplateLibrary library, RegionalStreamingConstraintValidator validator)

    // Resolve a single region — template selection + constraint pre-check
    RegionInstance load(ProgressionNode node, long seed, List<ZoneOverride> savedOverrides)

    // Resolve a connected subgraph of nodes (e.g. player-adjacent rooms)
    List<RegionInstance> loadNeighborhood(
        WorldProgressionGraph graph,
        String centerNodeId,
        int radius,
        long seed,
        Map<String, List<ZoneOverride>> savedOverrides
    )
}
```

`loadNeighborhood` is the primary entry point for streaming: load the current region plus
all nodes within `radius` edges, so transitions are ready before the player reaches them.

Compile-check + constraint validation runs on every `load` call — no lazy validation.
If `RegionalStreamingConstraintValidator` reports a blocking issue, `load` throws
`RegionLoadException` with the validation report attached.

### 3d. `MutationOverlay` (class)

Applies `WorldSimulationPressureSample` signals to a `RegionInstance`, producing
`ZoneOverride` records. Serializable state for save/load.

```java
class MutationOverlay {
    // Apply pressure sample → derive zone overrides for this region
    List<ZoneOverride> compute(RegionManifest manifest, WorldSimulationPressureSample sample)

    // Apply derived overrides to a live RegionInstance (rebuilds overlayTiles + spatialHash)
    void apply(RegionInstance instance, List<ZoneOverride> overrides)

    // Extract current overlay state for serialization into save envelope
    Map<String, List<ZoneOverride>> extractSaveState(List<RegionInstance> instances)
}
```

Overlay kinds (maps to `ZoneOverride.overlayKind`):
- `CORRUPTION_SURGE` — replaces PASSABLE zone tiles with BLOCKER variants; spawns hazard anchors
- `PROSPERITY_CRISIS` — deactivates service anchors; reduces NPC visibility in zone
- `ROUTE_HAZARD` — injects MOVING_HAZARD tiles into traversal zones
- `FACTION_CONFLICT` — enables combat-barrier anchors requiring ability unlock
- `STABILITY_WINDOW` — clears blockers; activates shortcut unlock anchors

These overlay kinds match `QuestOpportunity.OpportunityKind` from `QuestEcologyEngine` —
same vocabulary, separate application paths.

### 3e. `RegionalStreamingConstraintValidator` (class)

Core M6 acceptance gate. Validates a set of `RegionManifest` records for safety.

```java
class RegionalStreamingConstraintValidator {

    ValidationResult validate(
        WorldProgressionGraph graph,
        List<RegionManifest> manifests,
        List<String> playerAbilities   // for accessibility check
    )

    record ValidationResult(
        boolean valid,
        List<ValidationIssue> issues
    ) {}

    record ValidationIssue(
        String regionId,
        String kind,                   // DISCONNECTED | INACCESSIBLE | SOFTLOCK | SOCKET_MISMATCH
        String message,
        String repairAction
    ) {}
}
```

**Three checks:**

1. **Connectivity** — every `ProgressionNode` in the supplied graph has a path from
   `centralHub` through loaded manifests. No orphaned regions.

2. **Accessibility** — every node on `criticalPath` is reachable with `playerAbilities`.
   A node is accessible if all `ProgressionNode.requires` abilities are in `playerAbilities`
   AND all `PortalRule.requires` along the path are satisfied. Flags an INACCESSIBLE issue
   if any critical-path node cannot be reached with the default ability set (empty `requires`
   = always accessible).

3. **Softlock** — after applying all active `ZoneOverride` entries: no combination of
   `CORRUPTION_SURGE` + `ROUTE_HAZARD` overlays can simultaneously block all routes from
   a non-optional node back to the `centralHub`. Uses a flood-fill over the loaded
   `RegionInstance.spatialHash()` tiles to detect fully-sealed graph cuts.

---

## 4. Data Contract Additions

New authored data directory: `data/worldgen/regions/`

Each file: one region manifest fragment — binds a `ProgressionNode.id` to fixed overrides
(authored-only zones) and socket connection assignments. Procedural/simulation-driven
zones are NOT authored here; they are injected at runtime by `MutationOverlay`.

Example file shape (`data/worldgen/regions/hub_lantern_heights.json`):
```json
{
    "regionId": "hub_lantern_heights",
    "biome": "lantern_heights",
    "kind": "hub",
    "socketBindings": [
        { "socketId": "north", "connectedRegionId": "dungeon_forge_terrace_a" }
    ],
    "authoredZoneOverrides": []
}
```

Loader: `RegionLoader` reads these fragments to seed the manifest before resolving a
`SectionTemplate`. Fragments without a matching section template resolve to a minimal
stub region — `load` emits a WARNING but does not throw unless a critical-path node fails.

No new top-level contract file needed. Region fragments are validated by the new
`runRegionalStreamingDiagnostics` Gradle task (see §6).

---

## 5. Save / Load Implications

`MutationOverlay` state must survive save/load. This is the concrete trigger for `SAVE_V3`.

### SAVE_V3 envelope addition

```
SAVE_V3|story_state_b64=...|region_overlays_b64=...|encoding=utf8_base64
```

`region_overlays_b64` is a base64-encoded JSON map:
```json
{
    "regionId_1": [
        { "zoneRole": "traversal_main", "overlayKind": "ROUTE_HAZARD", "params": {} }
    ]
}
```

### Migration matrix update (when SAVE_V3 is implemented)

`SaveMigrationMatrix` needs a concrete `v2 → v3` migrator:
- reads `story_state_b64` from V2 envelope
- writes it unchanged into V3 envelope
- injects `region_overlays_b64 = "{}"` (empty — no overlay state from pre-V3 saves)

**Important:** Do not implement SAVE_V3 until `RegionLoader` and `MutationOverlay` are
shipping. The save version should advance when the new fields are real, not as a placeholder.

---

## 6. PlaytestClient Integration Point

After M6 streaming is in `core`, `PlaytestClient.initializeCollisionLayout` becomes:

```java
private void initializeCollisionLayout() {
    RegionLoader loader = new RegionLoader(
        SectionTemplateLibrary.loadDefault(),
        new RegionalStreamingConstraintValidator()
    );
    List<RegionInstance> loaded = loader.loadNeighborhood(
        progressionGraph,
        currentRegionId,
        /*radius*/ 1,
        worldSeed,
        savedOverlays
    );
    activeRegions.clear();
    activeRegions.addAll(loaded);
    refreshCollisionHashFromRegions();
}

private void refreshCollisionHashFromRegions() {
    collisionHash.clear();
    for (RegionInstance r : activeRegions) {
        r.staticTiles().forEach(collisionHash::insert);
    }
    collisionHash.setDynamicTiles(buildAllDynamicTiles());
}
```

`buildAllDynamicTiles()` combines `traversalSubsystem.buildDynamicTiles(...)` with
`activeRegions.stream().flatMap(r -> r.overlayTiles().stream()).toList()`.

This replaces the hardcoded geometry authoring in `initializeCollisionLayout` entirely.
The flat lists (`movingPlatforms`, `abilityGates`, etc.) remain under `TraversalSubsystem`
for authored dynamic elements; `RegionInstance` handles static + overlay geometry.

---

## 7. Acceptance Test Targets

Matching the M6 matrix row in `docs/NORTH_STAR_EXECUTION_MATRIX.md`:

### `RegionalStreamingConstraintTest` (new regression section)

```
Scenario: Load a 3-node subgraph (centralHub + 2 region hubs) from authored region fragments.
          Validate: connectivity OK, accessibility OK (default abilities), no softlock.
Expected: ValidationResult.valid() == true, zero issues.

Scenario: Apply CORRUPTION_SURGE overlay to the single traversal zone on the critical path.
          Validate: softlock detection fires for that node.
Expected: ValidationResult.valid() == false, issue kind == SOFTLOCK.

Scenario: Load with a missing socket binding between two adjacent nodes.
          Validate: SOCKET_MISMATCH issue reported.
Expected: ValidationResult.valid() == false, issue kind == SOCKET_MISMATCH.
```

### `MutationPersistenceMigrationTest` (new regression section)

```
Scenario: Apply two zone overrides to a RegionInstance.
          Serialize with MutationOverlay.extractSaveState().
          Reload via SaveMigrationMatrix v2→v3 migrator.
          Reconstruct overlay state and apply to fresh RegionInstance.
Expected: overlay tile sets match before and after round-trip.
```

### `CoopTraversalRecoveryValidationTest` (new regression section)

```
Scenario: Harness: two simulated player positions in different loaded regions.
          Player A activates a CombatBarrier-equivalent shortcut blocker.
          Validate: Player B still has an unblocked path to centralHub node.
Expected: connectivity check passes for Player B's reachable subgraph.
```

All three tests run under `runRegressionTests`. No client dependencies.

---

## 8. Implementation Order

Do each step, compile-check, then proceed:

1. Create `com.shadowascent.core.world.streaming` package stub
2. Implement `RegionManifest` record + `SocketBinding` + `ZoneOverride`
3. Implement `RegionInstance` class (stub — no spatial hash population yet)
4. Implement `RegionalStreamingConstraintValidator` (connectivity check only first)
5. Implement `RegionLoader.load()` for single-node case — template resolution + stub instance
6. Implement `RegionalStreamingConstraintValidator` accessibility + softlock checks
7. Implement `MutationOverlay.compute()` and `apply()`
8. Implement `RegionLoader.loadNeighborhood()`
9. Add authored region fragment files under `data/worldgen/regions/`
10. Add `runRegionalStreamingDiagnostics` Gradle task
11. Add three regression test sections above (stub passes on empty fixture, expand to real assertions)
12. Wire `PlaytestClient.initializeCollisionLayout` to `RegionLoader` (replaces hardcoded geometry)
13. Design SAVE_V3 envelope + v2→v3 migrator when overlay state is real
14. Compile + run full gate

---

## 9. Codex CLI Validation

After implementation, create `CODEX_M6_REGIONAL_STREAMING_VALIDATE.md` with:

```
.\gradlew.bat clean :core:compileJava :client:compileJava :server:compileJava runDataContractDiagnostics runRegionalStreamingDiagnostics runRegressionTests
```

Expected: BUILD SUCCESSFUL, regression tests pass including the three new M6 sections,
`runRegionalStreamingDiagnostics` reports zero validation issues for authored region fragments.
