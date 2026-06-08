package application.scene;

import java.awt.*;
import application.core.Theme;
import application.system.ScoreManager;

/**
 * CORE BREAKER
 *
 * MenuScene.java
 * The main menu screen displayed when the game starts or the player
 * returns from a session. Features an animated pulsing title,
 * control hints, and the current high score.
 *
 * @version 1.0
 */
public class MenuScene {

    // ─────────────────────────────────────────────────────────────────────────
    // Attributes
    // ─────────────────────────────────────────────────────────────────────────

    private int screenWidth;
    private int screenHeight;

    /** Increments each frame — drives the title glow animation. */
    private int glowTimer;

    // ─────────────────────────────────────────────────────────────────────────
    // Constructor
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Creates the menu scene sized to the game window.
     */
    public MenuScene(int screenW, int screenH) {
        this.screenWidth  = screenW;
        this.screenHeight = screenH;
        this.glowTimer    = 0;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Update (called each frame)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Advances the glow timer each frame.
     * Used to animate the title brightness via a sine wave.
     */
    public void update() {
        glowTimer++;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Rendering
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Draws the full menu screen:
     *   - Animated pulsing title (sine wave brightness)
     *   - Decorative separator line
     *   - Control hints
     *   - Current high score
     *   - Version label
     */
    public void render(Graphics2D g2) {

        // ── Animated title color ─────────────────────────────────────────────
        // Brightness oscillates between 0.2 and 0.8 — never fully black or white
        float glowIntensity = (float)(Math.sin(glowTimer * 0.02) * 0.3 + 0.5);
        Color titleColor = new Color(
            (int)(Theme.CYAN_BRIGHT.getRed()   * glowIntensity),
            (int)(Theme.CYAN_BRIGHT.getGreen() * glowIntensity),
            (int)(Theme.CYAN_BRIGHT.getBlue()  * glowIntensity)
        );

        // ── Main title ───────────────────────────────────────────────────────
        Theme.drawGlowText(g2, "CORE BREAKER",
            Theme.FONT_TITLE, titleColor,
            screenWidth / 2, screenHeight / 2 - 120);

        // ── Decorative separator ─────────────────────────────────────────────
        g2.setColor(new Color(
            Theme.ELECTRIC_BLUE.getRed(),
            Theme.ELECTRIC_BLUE.getGreen(),
            Theme.ELECTRIC_BLUE.getBlue(), 80));
        g2.drawLine(screenWidth / 2 - 200, screenHeight / 2 - 45,
                    screenWidth / 2 + 200, screenHeight / 2 - 45);

        // ── Controls ─────────────────────────────────────────────────────────
        Theme.drawGlowText(g2, "[ ENTER ]  START",
            Theme.FONT_SUBTITLE, Theme.WHITE,
            screenWidth / 2, screenHeight / 2 + 20);

        Theme.drawGlowText(g2, "[ ESC ]  PAUSE IN GAME",
            Theme.FONT_HUD_SMALL, Theme.ELECTRIC_BLUE,
            screenWidth / 2, screenHeight / 2 + 70);

        // ── High score ───────────────────────────────────────────────────────
        Theme.drawGlowText(g2,
            "RECORD: " + ScoreManager.getInstance().getHighScore(),
            Theme.FONT_HUD, Theme.NEON_PURPLE,
            screenWidth / 2, screenHeight / 2 + 130);

        // ── Version label ────────────────────────────────────────────────────
        g2.setFont(Theme.FONT_HUD_SMALL);
        g2.setColor(new Color(255, 255, 255, 40));
        g2.drawString("v1.0", screenWidth - 40, screenHeight - 10);
    }
}