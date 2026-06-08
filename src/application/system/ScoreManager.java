package application.system;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 * CORE BREAKER
 *
 * ScoreManager.java
 * Singleton — tracks the player's score, combo and multiplier.
 * Persists the high score to a local text file between sessions.
 *
 * Scoring rules:
 *   - Each brick awards points * multiplier
 *   - Combo increments on every brick destroyed
 *   - Multiplier = 1 + combo / 5  (increases every 5 consecutive bricks)
 *   - Combo resets when a ball is lost
 *
 * @version 1.0
 */
public class ScoreManager {

    // ─────────────────────────────────────────────────────────────────────────
    // Singleton
    // ─────────────────────────────────────────────────────────────────────────

    private static ScoreManager instance;

    public static ScoreManager getInstance() {
        if (instance == null) instance = new ScoreManager();
        return instance;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Constants
    // ─────────────────────────────────────────────────────────────────────────

    /** Local file used to persist the high score between sessions. */
    private static final String SAVE_FILE = "highscore.txt";

    // ─────────────────────────────────────────────────────────────────────────
    // Attributes
    // ─────────────────────────────────────────────────────────────────────────

    private int score;       // Current session score
    private int combo;       // Consecutive bricks destroyed without losing a ball
    private int multiplier;  // Score multiplier — increases every 5 combos
    private int highscore;   // All-time best score — loaded from file on init

    // ─────────────────────────────────────────────────────────────────────────
    // Constructor
    // ─────────────────────────────────────────────────────────────────────────

    /** Private — enforces singleton pattern. Loads high score on first access. */
    private ScoreManager() {
        multiplier = 1;
        highscore  = loadHighScore();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Score logic
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Adds points to the current score, increments the combo,
     * and updates the multiplier accordingly.
     *
     * @param points  base points awarded by the destroyed brick
     */
    public void addPoints(int points) {
        score += points * multiplier;
        combo++;
        updateMultiplier();
    }

    /** Resets the combo counter (called when a ball is lost). */
    public void resetCombo() {
        combo = 0;
        updateMultiplier();
    }

    /** Resets score and combo for a new session. Does not affect the high score. */
    public void reset() {
        score = 0;
        combo = 0;
        updateMultiplier();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Persistence
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Saves the high score to disk if the current score exceeds it.
     * Called automatically when the game ends (lives = 0 or all levels cleared).
     */
    public void saveHighScore() {
        if (score > highscore) highscore = score;
        try (PrintWriter pw = new PrintWriter(SAVE_FILE)) {
            pw.println(highscore);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads the high score from disk.
     * Returns 0 if the file does not exist (first launch).
     */
    public int loadHighScore() {
        File file = new File(SAVE_FILE);
        if (!file.exists()) return 0;
        try (Scanner input = new Scanner(file)) {
            if (input.hasNextInt()) return input.nextInt();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private utilities
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Recalculates the multiplier based on the current combo.
     * Formula: 1 + combo / 5  (integer division).
     * Examples: combo 0-4 = x1, 5-9 = x2, 10-14 = x3, etc.
     */
    private void updateMultiplier() {
        multiplier = 1 + combo / 5;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Getters
    // ─────────────────────────────────────────────────────────────────────────

    public int getScore()      { return score; }
    public int getCombo()      { return combo; }
    public int getMultiplier() { return multiplier; }
    public int getHighScore()  { return highscore; }
}