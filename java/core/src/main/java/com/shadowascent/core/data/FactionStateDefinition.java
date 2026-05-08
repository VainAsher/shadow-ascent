package com.shadowascent.core.data;

import java.util.List;

/**
 * Contract-backed faction pressure and territory snapshot for world simulation.
 */
public record FactionStateDefinition(
        String id,
        String displayName,
        List<String> territoryPlateaus,
        double influence,
        double tension,
        double prosperityEffect,
        List<String> eventHooks) {
}

