package com.shadowascent.client.audio;

import com.shadowascent.core.simulation.SimEvent;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

final class AudioManagerEventRoutingTest {

    @Test
    void eventRoutingSelectsExpectedSoundKey() {
        AudioManager audio = new AudioManager();

        String soundKey = audio.resolveSoundKey(List.of(new SimEvent("PLAYER_DAMAGED", "player1", Map.of())));

        assertEquals("player_hurt", soundKey);
    }

    @Test
    void registryBacksSoundAndMusicPathLookup() {
        AudioManager audio = new AudioManager();

        assertEquals("audio/sfx/player_hurt.ogg", audio.resolveSoundPath("player_hurt"));
        assertEquals("audio/music/hub.ogg", audio.resolveMusicPath("hub"));
        assertNull(audio.resolveSoundPath("missing"));
    }

    @Test
    void processEventsRecordsSafePlaybackAttemptWithoutAssets() {
        AudioManager audio = new AudioManager();

        audio.processEvents(List.of(new SimEvent("PLAYER_DAMAGED", "player1", Map.of())));

        assertEquals("player_hurt", audio.lastResolvedSoundKey());
        assertEquals("audio/sfx/player_hurt.ogg", audio.lastResolvedSoundPath());
    }
}
