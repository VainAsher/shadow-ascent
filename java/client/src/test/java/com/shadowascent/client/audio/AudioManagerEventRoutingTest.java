package com.shadowascent.client.audio;

import com.shadowascent.core.simulation.SimEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

final class AudioManagerEventRoutingTest {

    @Test
    void eventRoutingSelectsExpectedSoundKey() {
        AudioManager audio = new AudioManager();

        String soundKey = audio.resolveSoundKey(List.of(new SimEvent("PLAYER_DAMAGED", "player1", Map.of())));

        assertEquals("player_hurt", soundKey);
    }
}
