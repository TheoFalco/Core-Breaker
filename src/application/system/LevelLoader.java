package application.system;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import application.brick.Brick;
import application.brick.FireBrick;
import application.brick.GhostBrick;
import application.brick.NormalBrick;

/**
 * CORE BREAKER
 *
 * LevelLoader.java
 * Static utility class — reads level layout files and converts them
 * into lists of Brick objects ready for PlayScene.
 *
 * Level files are plain text located in the levels/ folder:
 *   levels/level1.txt, levels/level2.txt, etc.
 *
 * Symbol map:
 *   N → NormalBrick (hp scales with level)
 *   F → FireBrick   (always 1 HP)
 *   G → GhostBrick  (hp scales with level)
 *   . → empty cell  (no brick)
 *
 * @version 1.0
 */
public class LevelLoader {

    // ─────────────────────────────────────────────────────────────────────────
    // Grid layout constants
    // ─────────────────────────────────────────────────────────────────────────

    /** Brick width in pixels. */
    public static final int BRICK_W   = 72;

    /** Brick height in pixels. */
    public static final int BRICK_H   = 20;

    /** Horizontal gap between bricks in pixels. */
    public static final int PADDING_X = 3;

    /** Vertical gap between bricks in pixels. */
    public static final int PADDING_Y = 4;

    /** Left margin offset in pixels. */
    public static final int OFFSET_X  = 11;

    /** Top margin offset in pixels. */
    public static final int OFFSET_Y  = 50;

    // ─────────────────────────────────────────────────────────────────────────
    // Public API
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Loads a level from file and returns its brick list.
     * HP is determined by DifficultyConfig based on the level number.
     *
     * @param level  level number (1-based) — maps to levels/levelN.txt
     * @return list of Brick objects ready to be added to PlayScene
     */
    public static List<Brick> load(int level) {
        String path = "levels/level" + level + ".txt";
        int    hp   = DifficultyConfig.getBrickHp(level);
        return parseFile(path, hp);
    }

    /**
     * Counts the number of level files in the levels/ folder.
     * Used by PlayScene to detect when all levels are completed.
     *
     * @return number of regular files in the levels/ directory
     */
    public static int getLevelCount() {
        Path folder = Paths.get("levels");
        try (Stream<Path> flux = Files.list(folder)) {
            return (int) flux.filter(Files::isRegularFile).count();
        } catch (IOException e) {
            System.err.println("Level count error: " + e.getMessage());
            return 0;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private — file parsing
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Reads the level file line by line and builds the brick list.
     * Each line = one row. Symbols are space-separated.
     * Empty cells (".") are skipped — no brick created.
     *
     * @param path  path to the level text file
     * @param hp    HP value to assign to scalable bricks
     */
    private static List<Brick> parseFile(String path, int hp) {
        List<Brick> bricks = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
            String line;
            int row = 0;
            while ((line = reader.readLine()) != null) {
                String[] symbols = line.split(" ");
                for (int col = 0; col < symbols.length; col++) {
                    int   x = OFFSET_X + col * (BRICK_W + PADDING_X);
                    int   y = OFFSET_Y + row * (BRICK_H + PADDING_Y);
                    Brick b = createBrick(symbols[col], x, y, hp);
                    if (b != null) bricks.add(b);
                }
                row++;
            }
        } catch (IOException e) {
            System.err.println("Level load error: " + e.getMessage());
        }
        return bricks;
    }

    /**
     * Creates the appropriate Brick subclass for the given symbol.
     * Returns null for "." (empty cell) — handled by the caller.
     *
     * @param symbol  single character from the level file
     * @param x       computed X position
     * @param y       computed Y position
     * @param hp      HP for scalable brick types
     */
    private static Brick createBrick(String symbol, int x, int y, int hp) {
        switch (symbol) {
            case "N": return new NormalBrick(x, y, hp);
            case "F": return new FireBrick(x, y);      // always 1 HP
            case "G": return new GhostBrick(x, y, hp);
            default:  return null;                      // "." = empty
        }
    }
}