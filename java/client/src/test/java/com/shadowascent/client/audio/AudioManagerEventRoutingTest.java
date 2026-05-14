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

        assertEquals("audio/sfx/player_hurt.wav", audio.resolveSoundPath("player_hurt"));
        assertEquals("audio/music/title.wav", audio.resolveMusicPath("title"));
        assertEquals("audio/music/hollow_depths.wav", audio.resolveMusicPath("hollow_depths"));
        assertNull(audio.resolveSoundPath("missing"));
    }

    @Test
    void processEventsRecordsSafePlaybackAttemptWithoutAssets() {
        AudioManager audio = new AudioManager();

        audio.processEvents(List.of(new SimEvent("PLAYER_DAMAGED", "player1", Map.of())));

        assertEquals("player_hurt", audio.lastResolvedSoundKey());
        assertEquals("audio/sfx/player_hurt.wav", audio.lastResolvedSoundPath());
    }

    @Test
    void plateauAndTitleMusicRoutingChooseExpectedKeys() {
        AudioManager audio = new AudioManager();

        assertEquals("title", audio.selectMusicKeyForTitle());
        assertEquals("lantern_heights", audio.selectMusicKeyForPlateau("LANTERN_HEIGHTS"));
        assertEquals("hollow_depths", audio.selectMusicKeyForPlateau("HOLLOW_DEPTHS"));
        assertEquals("ember_monastery", audio.selectMusicKeyForPlateau("EMBER_MONASTERY"));
    }
}
