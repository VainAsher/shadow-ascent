package com.shadowascent.client.audio;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.shadowascent.core.simulation.SimEvent;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AudioManager {
    private static final String REGISTRY_RESOURCE = "/audio/audio_registry.json";
    private static final Pattern SECTION_PATTERN = Pattern.compile("\"(sfx|music)\"\\s*:\\s*\\{(.*?)\\}", Pattern.DOTALL);
    private static final Pattern ENTRY_PATTERN = Pattern.compile("\"([^\"]+)\"\\s*:\\s*\"([^\"]+)\"");

    private final Map<String, String> soundPaths;
    private final Map<String, String> musicPaths;
    private final Map<String, Sound> cachedSounds = new LinkedHashMap<>();

    private String lastResolvedSoundKey;
    private String lastResolvedSoundPath;

    public AudioManager() {
        Registry registry = loadRegistry();
        this.soundPaths = registry.soundPaths();
        this.musicPaths = registry.musicPaths();
    }

    public String resolveSoundKey(List<SimEvent> events) {
        for (SimEvent event : events) {
            switch (event.type()) {
                case "PLAYER_DAMAGED":
                    return "player_hurt";
                case "ENEMY_DEFEATED":
                    return "enemy_defeated";
                case "PORTAL_ACTIVATED":
                    return "portal_activate";
                default:
                    break;
            }
        }
        return null;
    }

    public String resolveSoundPath(String soundKey) {
        return soundPaths.get(soundKey);
    }

    public String resolveMusicPath(String musicKey) {
        return musicPaths.get(musicKey);
    }

    public String lastResolvedSoundKey() {
        return lastResolvedSoundKey;
    }

    public String lastResolvedSoundPath() {
        return lastResolvedSoundPath;
    }

    public void processEvents(List<SimEvent> events) {
        String soundKey = resolveSoundKey(events);
        if (soundKey != null) {
            lastResolvedSoundKey = soundKey;
            lastResolvedSoundPath = resolveSoundPath(soundKey);
            playSoundIfAvailable(soundKey, lastResolvedSoundPath);
        }
    }

    public void dispose() {
        for (Sound sound : cachedSounds.values()) {
            sound.dispose();
        }
        cachedSounds.clear();
    }

    private void playSoundIfAvailable(String soundKey, String soundPath) {
        if (soundKey == null || soundPath == null || soundPath.isBlank()) {
            return;
        }
        if (Gdx.app == null || Gdx.audio == null || Gdx.files == null) {
            return;
        }
        if (!Gdx.files.internal(soundPath).exists()) {
            return;
        }

        Sound sound = cachedSounds.computeIfAbsent(soundKey,
                ignored -> Gdx.audio.newSound(Gdx.files.internal(soundPath)));
        sound.play(0.45f);
    }

    private static Registry loadRegistry() {
        try (InputStream stream = AudioManager.class.getResourceAsStream(REGISTRY_RESOURCE)) {
            if (stream == null) {
                return Registry.empty();
            }
            String json = new String(stream.readAllBytes());
            return parseRegistry(json);
        } catch (IOException ex) {
            return Registry.empty();
        }
    }

    private static Registry parseRegistry(String json) {
        if (json == null || json.isBlank()) {
            return Registry.empty();
        }

        Map<String, String> sfx = new LinkedHashMap<>();
        Map<String, String> music = new LinkedHashMap<>();
        Matcher sectionMatcher = SECTION_PATTERN.matcher(json);
        while (sectionMatcher.find()) {
            Map<String, String> target = "music".equals(sectionMatcher.group(1)) ? music : sfx;
            Matcher entryMatcher = ENTRY_PATTERN.matcher(sectionMatcher.group(2));
            while (entryMatcher.find()) {
                target.put(entryMatcher.group(1), entryMatcher.group(2).trim());
            }
        }
        return new Registry(Map.copyOf(sfx), Map.copyOf(music));
    }

    private record Registry(Map<String, String> soundPaths, Map<String, String> musicPaths) {
        private static Registry empty() {
            return new Registry(Map.of(), Map.of());
        }
    }
}
