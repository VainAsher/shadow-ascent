package com.shadowascent.client.tools;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class SpritePackerToolTest {

    @Test
    void generatedAtlasParsesAsSinglePageWithTilePlatformRegion() throws Exception {
        Path outDir = Files.createTempDirectory("sprite-packer-test");

        SpritePackerTool.main(new String[]{outDir.toString()});

        TextureAtlas.TextureAtlasData data = new TextureAtlas.TextureAtlasData(
            new FileHandle(outDir.resolve("sprites.atlas").toFile()),
            new FileHandle(outDir.toFile()),
            false
        );

        assertEquals(1, data.getPages().size, "atlas should declare exactly one texture page");
        assertEquals("sprites.png", data.getPages().get(0).textureFile.name());
        Set<String> regionNames = new HashSet<>();
        for (TextureAtlas.TextureAtlasData.Region region : data.getRegions()) {
            regionNames.add(region.name);
        }
        assertTrue(regionNames.contains("tile_platform"), "atlas should expose tile_platform as a region");
        assertTrue(regionNames.contains("player_run"), "atlas should expose player_run as a region");
        assertTrue(regionNames.contains("player_jump"), "atlas should expose player_jump as a region");
        assertTrue(regionNames.contains("player_dash"), "atlas should expose player_dash as a region");
        assertTrue(regionNames.contains("player_attack"), "atlas should expose player_attack as a region");
        assertTrue(regionNames.contains("enemy_patrol"), "atlas should expose enemy_patrol as a region");
        assertTrue(regionNames.contains("enemy_alerted"), "atlas should expose enemy_alerted as a region");
        assertTrue(regionNames.contains("enemy_attack"), "atlas should expose enemy_attack as a region");
        assertTrue(regionNames.contains("enemy_stunned"), "atlas should expose enemy_stunned as a region");
        assertTrue(regionNames.contains("enemy_goblin_patrol"), "atlas should expose enemy_goblin_patrol as a region");
        assertTrue(regionNames.contains("enemy_bat_patrol"), "atlas should expose enemy_bat_patrol as a region");
        assertTrue(regionNames.contains("enemy_slime_patrol"), "atlas should expose enemy_slime_patrol as a region");
        assertTrue(regionNames.contains("enemy_skeleton_patrol"), "atlas should expose enemy_skeleton_patrol as a region");
        assertTrue(regionNames.contains("enemy_wolf_patrol"), "atlas should expose enemy_wolf_patrol as a region");
    }
}
