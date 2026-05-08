package com.shadowascent.core.world.streaming;

import java.util.Objects;

/** Directional connection from one region's socket to an adjacent region. */
public record SocketBinding(String socketId, String connectedRegionId) {
    public SocketBinding {
        Objects.requireNonNull(socketId, "socketId");
        Objects.requireNonNull(connectedRegionId, "connectedRegionId");
    }
}
