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
    private static final int SHEET_W = 256;
    private static final int SHEET_H = 64;

    // Region definitions: name, x, y, w, h, ARGB color
    private static final Object[][] REGIONS = {
        { "player_idle",   0,   0, 32, 40, new Color(0.20f, 0.80f, 0.30f, 1f) },
        { "player_dead",  32,   0, 32, 40, new Color(0.40f, 0.40f, 0.40f, 0.5f) },
        { "enemy_walk",   64,   0, 32, 40, new Color(0.90f, 0.20f, 0.20f, 1f) },
        { "npc_idle",     96,   0, 24, 40, new Color(0.30f, 0.60f, 0.90f, 1f) },
        { "tile_ground", 120,   0, 32, 32, new Color(0.55f, 0.55f, 0.60f, 1f) },
        { "tile_platform",152,  0, 32,  8, new Color(0.45f, 0.65f, 0.45f, 1f) },
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
        BufferedImage img = new BufferedImage(SHEET_W, SHEET_H, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setColor(new Color(0, 0, 0, 0));
        g.fillRect(0, 0, SHEET_W, SHEET_H);
        for (Object[] r : REGIONS) {
            Color c = (Color) r[5];
            g.setColor(c);
            g.fillRect((int) r[1], (int) r[2], (int) r[3], (int) r[4]);
        }
        g.dispose();
        ImageIO.write(img, "PNG", out);
    }

    private static void writeAtlas(File out) throws Exception {
        try (PrintWriter pw = new PrintWriter(out)) {
            pw.println();
            pw.println("sprites.png");
            pw.println("format: RGBA8888");
            pw.println("filter: Nearest,Nearest");
            pw.println("repeat: none");
            for (Object[] r : REGIONS) {
                pw.println();
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
