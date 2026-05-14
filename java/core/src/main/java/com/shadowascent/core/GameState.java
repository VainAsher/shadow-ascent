package com.shadowascent.core;

import com.shadowascent.core.data.ContractValidationMode;
import com.shadowascent.core.data.GameDataContracts;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

public final class GameState {
    private final GameDataContracts dataContracts;
    private final StoryState storyState;
    private final HubManager hubManager;
    private final MissionManager missionManager;
    private String currentRoomId;
    private String pendingRoomSpawnId;

    public GameState() {
        this.dataContracts = GameDataContracts.loadDefault();
        enforceContractValidationPolicy(dataContracts);
        this.storyState = new StoryState();
        this.hubManager = new HubManager(storyState, dataContracts);
        this.missionManager = new MissionManager(storyState, dataContracts);
    }

    public GameDataContracts getDataContracts() {
        return dataContracts;
    }

    public StoryState getStoryState() {
        return storyState;
    }

    public HubManager getHubManager() {
        return hubManager;
    }

    public MissionManager getMissionManager() {
        return missionManager;
    }

    public String getCurrentRoomId() {
        return currentRoomId;
    }

    public void setCurrentRoomId(String currentRoomId) {
        this.currentRoomId = currentRoomId == null || currentRoomId.isBlank() ? null : currentRoomId;
    }

    public String getPendingRoomSpawnId() {
        return pendingRoomSpawnId;
    }

    public void setPendingRoomSpawnId(String pendingRoomSpawnId) {
        this.pendingRoomSpawnId = pendingRoomSpawnId == null || pendingRoomSpawnId.isBlank() ? null : pendingRoomSpawnId;
    }

    public void update(float deltaTime) {
        missionManager.update(deltaTime);
    }

    public void save(Path savePath) throws IOException {
        save(savePath, "");
    }

    public void save(Path savePath, String overlaysB64) throws IOException {
        Objects.requireNonNull(savePath, "savePath");
        Path parent = savePath.getParent();
        if (parent != null) {
            parent.toFile().mkdirs();
        }
        String serialized = storyState.serialize();
        String envelope = SaveMigrationMatrix.encodeCurrentWithOverlays(serialized,
                overlaysB64 != null ? overlaysB64 : "");
        Files.writeString(savePath, envelope, StandardCharsets.UTF_8);
    }

    public String loadOverlaysB64(Path savePath) throws IOException {
        Objects.requireNonNull(savePath, "savePath");
        if (!Files.exists(savePath)) {
            return "";
        }
        String content = Files.readString(savePath, StandardCharsets.UTF_8);
        return SaveMigrationMatrix.decodeOverlaysB64(content);
    }

    public void load(Path savePath) throws IOException {
        Objects.requireNonNull(savePath, "savePath");
        if (!Files.exists(savePath)) {
            return;
        }
        String content = Files.readString(savePath, StandardCharsets.UTF_8);
        String storyPayload = SaveMigrationMatrix.decodeToCurrentStoryState(content);
        StoryState loaded = StoryState.deserialize(storyPayload);
        storyState.copyFrom(loaded);
        // Reinitialize managers with loaded state
        hubManager.updateHubState();
        missionManager.updateAvailableMissions();
    }

    static void enforceContractValidationPolicy(GameDataContracts contracts) {
        ContractValidationMode mode = ContractValidationMode.resolve();
        if (contracts == null || contracts.isValid()) {
            return;
        }

        if (mode == ContractValidationMode.FAIL_FAST) {
            StringBuilder failure = new StringBuilder();
            failure.append("[DataContracts] Validation failed in FAIL_FAST mode. ");
            failure.append("Configured mode=`").append(ContractValidationMode.configuredValue()).append("`. ");
            failure.append("Issue count=").append(contracts.validationIssues().size());
            for (String issue : contracts.validationIssues()) {
                failure.append(System.lineSeparator()).append("  - ").append(issue);
            }
            throw new IllegalStateException(failure.toString());
        }

        System.err.println("[DataContracts] Loaded with validation issues (WARN mode):");
        for (String issue : contracts.validationIssues()) {
            System.err.println("  - " + issue);
        }
    }
}
