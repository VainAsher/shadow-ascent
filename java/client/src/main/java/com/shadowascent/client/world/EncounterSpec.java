package com.shadowascent.client.world;

import java.util.List;

public record EncounterSpec(
        String id,
        String clearType,
        List<String> enemyIds,
        List<String> setFlags) {

    public EncounterSpec {
        enemyIds = enemyIds == null ? List.of() : List.copyOf(enemyIds);
        setFlags = setFlags == null ? List.of() : List.copyOf(setFlags);
    }
}
