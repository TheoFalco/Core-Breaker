package application.core;

import java.awt.*;
import java.awt.geom.*;

/**
 * CORE BREAKER
 *
 * Theme.java
 * Centralized visual identity of the game.
 * Defines all colors, fonts, and rendering utilities used across the project.
 * All members are static — no instantiation needed.
 *
 * Color palette:
 *   Background  #0D1117  — deep dark navy
 *   Electric blue #00BFFF — primary accent
 *   Neon purple #8A2BE2  — secondary accent
 *   Cyan bright #00FFFF  — highlights and HUD
 *
 * @version 1.0
 */
public class Theme {

    // ─────────────────────────────────────────────────────────────────────────
    // Main Colors
    // ─────────────────────────────────────────────────────────────────────────

    public static final Color BACKGROUND    = new Color(0x0D, 0x11, 0x17);
    public static final Color ELECTRIC_BLUE = new Color(0x00, 0xBF, 0xFF);
    public static final Color NEON_PURPLE   = new Color(0x8A, 0x2B, 0xE2);
    public static final Color CYAN_BRIGHT   = new Color(0x00, 0xFF, 0xFF);
    public static final Color WHITE         = new Color(0xFF, 0xFF, 0xFF);

    // ─────────────────────────────────────────────────────────────────────────
    // Brick Colors
    // ─────────────────────────────────────────────────────────────────────────

    public static final Color NORMAL_BRICK = new Color(0x00, 0x8B, 0xB5);
    public static final Color FIRE_BRICK   = new Color(0xFF, 0x45, 0x00);
    public static final Color GHOST_BRICK  = new Color(0x8A, 0x2B, 0xE2);

    // ─────────────────────────────────────────────────────────────────────────
    // Power-up Colors
    // ─────────────────────────────────────────────────────────────────────────

    public static final Color POWERUP_MULTIBALL = CYAN_BRIGHT;
    public static final Color POWERUP_WIDE      = new Color(0x39, 0xFF, 0x14);
    public static final Color POWERUP_PIERCING  = new Color(0xFF, 0xA5, 0x00);

    // ─────────────────────────────────────────────────────────────────────────
    // Ball Colors
    // ─────────────────────────────────────────────────────────────────────────

    public static final Color BALL_NORMAL   = ELECTRIC_BLUE;
    public static final Color BALL_PIERCING = new Color(0xFF, 0xA5, 0x00);
    public static final Color BALL_GLOW     = new Color(0x00, 0xBF, 0xFF, 60);

    // ─────────────────────────────────────────────────────────────────────────
    // Paddle Colors
    // ─────────────────────────────────────────────────────────────────────────

    public static final Color PADDLE_NORMAL   = ELECTRIC_BLUE;
    public static final Color PADDLE_CHARGING = NEON_PURPLE;
    public static final Color PADDLE_GLOW     = new Color(0x00, 0xBF, 0xFF, 40);

    // ─────────────────────────────────────────────────────────────────────────
    // HUD Colors
    // ─────────────────────────────────────────────────────────────────────────

    public static final Color HUD_TEXT    = CYAN_BRIGHT;
    public static final Color HUD_VALUE   = WHITE;
    public static final Color HUD_BAR_BG  = new Color(0x1A, 0x1A, 0x2E);
    public static final Color HUD_BAR_FILL = ELECTRIC_BLUE;

    // ─────────────────────────────────────────────────────────────────────────
    // Fonts
    // ─────────────────────────────────────────────────────────────────────────

    public static final Font FONT_TITLE     = new Font("Courier New", Font.BOLD,  48);
    public static final Font FONT_SUBTITLE  = new Font("Courier New", Font.BOLD,  24);
    public static final Font FONT_HUD       = new Font("Courier New", Font.BOLD,  13);
    public static final Font FONT_HUD_SMALL = new Font("Courier New", Font.PLAIN, 11);

    // ─────────────────────────────────────────────────────────────────────────
    // Rendering utilities
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Enables antialiasing and high quality rendering.
     * Call at the start of every paintComponent().
     */
    public static void applyRenderingHints(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING,
                            RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING,
                            RenderingHints.VALUE_RENDER_QUALITY);
    }

    /**
     * Draws a layered glow effect around a shape.
     * Each layer is progressively thicker and more transparent.
     * Always resets the stroke to 1px after drawing.
     *
     * @param layers  number of glow layers (3-5 recommended)
     */
    public static void drawGlow(Graphics2D g2, Shape shape, Color color, int layers) {
        for (int i = layers; i >= 1; i--) {
            float alpha = 0.15f / i;
            g2.setColor(new Color(color.getRed(), color.getGreen(),
                                  color.getBlue(), (int)(alpha * 255)));
            g2.setStroke(new BasicStroke(i * 3));
            g2.draw(shape);
        }
        g2.setStroke(new BasicStroke(1)); // reset stroke
    }

    /**
     * Draws the game background with three layers:
     * solid fill, subtle scan-lines, and a vignette on the edges.
     */
    public static void drawBackground(Graphics2D g2, int width, int height) {
        // Solid background
        g2.setColor(BACKGROUND);
        g2.fillRect(0, 0, width, height);

        // Scan-lines — one every 4px, very subtle
        g2.setColor(new Color(255, 255, 255, 6));
        for (int y = 0; y < height; y += 4)
            g2.drawLine(0, y, width, y);

        // Vignette — darkens the edges for a cinematic feel
        RadialGradientPaint vignette = new RadialGradientPaint(
            width / 2f, height / 2f,
            Math.max(width, height) / 1.4f,
            new float[]{ 0.4f, 1.0f },
            new Color[]{ new Color(0, 0, 0, 0), new Color(0, 0, 0, 120) }
        );
        g2.setPaint(vignette);
        g2.fillRect(0, 0, width, height);
        g2.setPaint(null); // reset paint
    }

    /**
     * Draws centered text with a subtle glow halo behind it.
     * The glow is drawn as offset copies of the text at low opacity,
     * then the main text is drawn on top in full color.
     *
     * @param centerX  horizontal center point for alignment
     * @param y        baseline Y position of the text
     */
    public static void drawGlowText(Graphics2D g2, String text, Font font,
                                     Color color, int centerX, int y) {
        g2.setFont(font);
        FontMetrics fm = g2.getFontMetrics();
        int x = centerX - fm.stringWidth(text) / 2;

        // Glow — offset copies at low opacity
        g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), 60));
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                if (dx == 0 && dy == 0) continue;
                g2.drawString(text, x + dx, y + dy);
            }
        }

        // Main text — sharp and fully opaque
        g2.setColor(color);
        g2.drawString(text, x, y);
    }

    /**
     * Returns a darkened version of a color based on a damage ratio.
     * ratio 1.0 = full color, 0.0 = black.
     * Used by bricks to reflect remaining HP visually.
     *
     * @param ratio  value between 0.0 and 1.0
     */
    public static Color darken(Color base, float ratio) {
        return new Color(
            Math.max(0, (int)(base.getRed()   * ratio)),
            Math.max(0, (int)(base.getGreen() * ratio)),
            Math.max(0, (int)(base.getBlue()  * ratio))
        );
    }
}