---
doc_type: gate
status: complete
owner: core-team
last_updated: 2026-05-15
milestone: M4 Full - Complete Campaign Spine Runtime Playability
---
# M4 Full Milestone Gate

## Closure Checklist

- [x] `ResolveRoomSpecMultiPlateauFallbackTest` green
- [x] `ActIVerticalSliceBootstrapTest` green
- [x] `SummitShrineVerticalSliceBootstrapTest` green
- [x] `SummitShrineOptionalContentTest` green
- [x] `HollowBossRoomSpecBindingTest` green
- [x] `HollowDepthsVerticalSliceBootstrapTest` green
- [x] `HollowDepthsOptionalContentTest` green
- [x] `CampaignContinuitySaveLoadBoundaryTest` green
- [x] `EmberAbilityUnlockTransitionTest` green
- [x] `EmberMonasteryVerticalSliceBootstrapTest` green
- [x] `EmberMonasteryOptionalContentTest` green
- [x] `WindingSkyRoadVerticalSliceBootstrapTest` green
- [x] `MirrorSummitVerticalSliceBootstrapTest` green
- [x] `BeaconCliffVerticalSliceBootstrapTest` green
- [x] `PostClimaxStatePersistenceTest` green
- [x] `PlateauGeometryFidelityTest` green
- [x] `FullCampaignRouteEndToEndTest` green
- [x] `runAuthoringDiagnostics` green
- [x] `Campaign Continuity` regression section green
- [x] Full `runRegressionTests` green

## Evidence

- Gate run date: `2026-05-15`
- Focused plateau and route verification:
  - `./gradlew.bat --console=plain :client:test --tests "com.shadowascent.client.world.ResolveRoomSpecMultiPlateauFallbackTest" --tests "com.shadowascent.client.world.SummitShrineVerticalSliceBootstrapTest" --tests "com.shadowascent.client.SummitShrineOptionalContentTest" --tests "com.shadowascent.client.world.HollowBossRoomSpecBindingTest" --tests "com.shadowascent.client.world.HollowDepthsVerticalSliceBootstrapTest" --tests "com.shadowascent.client.HollowDepthsOptionalContentTest" --tests "com.shadowascent.client.CampaignContinuitySaveLoadBoundaryTest" --tests "com.shadowascent.client.world.EmberMonasteryVerticalSliceBootstrapTest" --tests "com.shadowascent.client.EmberMonasteryOptionalContentTest" --tests "com.shadowascent.client.world.EmberAbilityUnlockTransitionTest" --tests "com.shadowascent.client.world.WindingSkyRoadVerticalSliceBootstrapTest" --tests "com.shadowascent.client.world.MirrorSummitVerticalSliceBootstrapTest" --tests "com.shadowascent.client.world.BeaconCliffVerticalSliceBootstrapTest" --tests "com.shadowascent.client.PostClimaxStatePersistenceTest" --tests "com.shadowascent.client.world.PlateauGeometryFidelityTest" --tests "com.shadowascent.client.FullCampaignRouteEndToEndTest"`
- Authoring diagnostics:
  - `./gradlew.bat --console=plain runAuthoringDiagnostics`
- Broader regression tranche:
  - `./gradlew.bat --console=plain runRegressionTests`

## Result

`runGame` is now the truthful runtime host for the currently authored campaign spine:

- all seven plateau families are room-spec staged,
- plateau-local optional content already present in contracts is surfaced on the runtime path,
- plateau slices demonstrate meaningful multi-axis traversal at the plateau level,
- cross-plateau continuity survives save/load,
- the campaign resolves into a bounded post-climax free-roam state on `BEACON_CLIFF`.
