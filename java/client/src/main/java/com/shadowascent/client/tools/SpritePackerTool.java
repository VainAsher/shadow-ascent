package com.shadowascent.client.tools;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Generates a placeholder sprite sheet (sprites.png) and LibGDX atlas file
 * (sprites.atlas) for the P3 asset pipeline scaffold.
 *
 * Each entity type gets a solid-color rectangle. Real art replaces these
 * by dropping frames into assets/sprites/raw/ and re-running packSprites.
 *
 * Usage: SpritePackerTool <outputDir>
 */
public final class SpritePackerTool {

    // Sprite sheet dimensions
    private static final int SHEET_W = 576;
    private static final int SHEET_H = 64;

    // Region definitions: name, x, y, w, h, ARGB color
    private static final Object[][] REGIONS = {
        { "player_idle",    0,   0, 32, 40, new Color(0.20f, 0.80f, 0.30f, 1f) },
        { "player_run",    32,   0, 32, 40, new Color(0.24f, 0.92f, 0.36f, 1f) },
        { "player_jump",   64,   0, 32, 40, new Color(0.12f, 0.68f, 0.92f, 1f) },
        { "player_dash",   96,   0, 32, 40, new Color(0.98f, 0.84f, 0.18f, 1f) },
        { "player_attack",128,   0, 32, 40, new Color(0.96f, 0.52f, 0.18f, 1f) },
        { "player_dead",  160,   0, 32, 40, new Color(0.40f, 0.40f, 0.40f, 0.5f) },
        { "enemy_patrol", 192,   0, 32, 40, new Color(0.90f, 0.20f, 0.20f, 1f) },
        { "enemy_alerted",224,   0, 32, 40, new Color(0.85f, 0.18f, 0.62f, 1f) },
        { "enemy_attack", 256,   0, 32, 40, new Color(0.98f, 0.44f, 0.08f, 1f) },
        { "enemy_stunned",288,   0, 32, 40, new Color(0.68f, 0.38f, 0.96f, 1f) },
        { "enemy_goblin_patrol",320, 0, 32, 40, new Color(0.76f, 0.24f, 0.24f, 1f) },
        { "enemy_bat_patrol",352,   0, 32, 40, new Color(0.40f, 0.40f, 0.48f, 1f) },
        { "enemy_slime_patrol",384, 0, 32, 40, new Color(0.24f, 0.74f, 0.36f, 1f) },
        { "enemy_skeleton_patrol",416, 0, 32, 40, new Color(0.86f, 0.86f, 0.78f, 1f) },
        { "enemy_wolf_patrol",448,  0, 32, 40, new Color(0.58f, 0.46f, 0.34f, 1f) },
        { "npc_idle",     480,   0, 24, 40, new Color(0.30f, 0.60f, 0.90f, 1f) },
        { "tile_ground",  504,   0, 32, 32, new Color(0.55f, 0.55f, 0.60f, 1f) },
        { "tile_platform",536,   0, 32,  8, new Color(0.45f, 0.65f, 0.45f, 1f) },
    };

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: SpritePackerTool <outputDir>");
            System.exit(1);
        }

        Path outDir = Path.of(args[0]);
        Files.createDirectories(outDir);

        writePng(outDir.resolve("sprites.png").toFile());
        writeAtlas(outDir.resolve("sprites.atlas").toFile());

        System.out.println("[packSprites] wrote " + outDir.resolve("sprites.png"));
        System.out.println("[packSprites] wrote " + outDir.resolve("sprites.atlas"));
    }

    private static void writePng(File out) throws Exception {
        BufferedImage img = new BufferedImage(SHEET_W, SHEET_H, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = img.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, SHEET_W, SHEET_H);
        for (Object[] r : REGIONS) {
            Color c = (Color) r[5];
            g.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue()));
            g.fillRect((int) r[1], (int) r[2], (int) r[3], (int) r[4]);
        }
        g.dispose();
        ImageIO.write(img, "PNG", out);
    }

    private static void writeAtlas(File out) throws Exception {
        try (PrintWriter pw = new PrintWriter(out)) {
            pw.println("sprites.png");
            pw.printf ("size: %d, %d%n", SHEET_W, SHEET_H);
            pw.println("format: RGBA8888");
            pw.println("filter: Nearest,Nearest");
            pw.println("repeat: none");
            for (Object[] r : REGIONS) {
                pw.println(r[0]);
                pw.println("  rotate: false");
                pw.printf ("  xy: %d, %d%n",  (int) r[1], (int) r[2]);
                pw.printf ("  size: %d, %d%n", (int) r[3], (int) r[4]);
                pw.printf ("  orig: %d, %d%n", (int) r[3], (int) r[4]);
                pw.println("  offset: 0, 0");
                pw.println("  index: -1");
            }
        }
    }
}
