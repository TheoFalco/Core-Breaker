package application.powerup;

import java.awt.*;
import application.core.Theme;
import application.scene.PlayScene;
import application.system.SoundManager;

/**
 * CORE BREAKER
 *
 * PowerUp.java
 * A collectible bonus that falls from destroyed NormalBricks.
 * The player catches it by touching it with the paddle.
 * Three types are available, each with a distinct color and effect.
 *
 * Types:
 *   MULTI_BALL   — splits all active balls in two (cyan)
 *   WIDE_PADDLE  — temporarily expands the paddle width (green)
 *   PIERCING     — makes all balls pass through bricks for 5 seconds (orange)
 *
 * @version 1.0
 */
public class PowerUp {

    // ─────────────────────────────────────────────────────────────────────────
    // Power-up types
    // ─────────────────────────────────────────────────────────────────────────

    /** The three available power-up effects. */
    public enum Type { MULTI_BALL, WIDE_PADDLE, PIERCING }

    // ─────────────────────────────────────────────────────────────────────────
    // Attributes
    // ─────────────────────────────────────────────────────────────────────────

    private double  x, y;       // Position (top-left)
    private int     width;      // Hitbox and visual width
    private int     height;     // Hitbox and visual height
    private int     fallSpeed;  // Pixels per frame falling speed
    private boolean collected;  // True when caught by the paddle
    private Type    type;       // Which effect this power-up applies

    // ─────────────────────────────────────────────────────────────────────────
    // Constructor
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Creates a power-up at the given position with the given type.
     * Spawned by NormalBrick.onDestroy() with a random type.
     *
     * @param x     spawn X position (usually brick center)
     * @param y     spawn Y position (usually brick top)
     * @param type  the effect applied when collected
     */
    public PowerUp(double x, double y, Type type) {
        this.x         = x;
        this.y         = y;
        this.type      = type;
        this.width     = 20;
        this.height    = 20;
        this.fallSpeed = 3;
        this.collected = false;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Update (called each frame)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Moves the power-up downward each frame.
     * Removed by PlayScene when it exits the bottom of the screen.
     */
    public void update() {
        y += fallSpeed;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Rendering
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Draws the power-up as a colored circle with a letter label.
     * Colors match the Theme palette:
     *   M (cyan)   → MULTI_BALL
     *   W (green)  → WIDE_PADDLE
     *   P (orange) → PIERCING
     */
    public void render(Graphics2D g2) {
        // Background circle — color by type
        switch (type) {
            case MULTI_BALL:  g2.setColor(Theme.POWERUP_MULTIBALL); break;
            case WIDE_PADDLE: g2.setColor(Theme.POWERUP_WIDE);      break;
            case PIERCING:    g2.setColor(Theme.POWERUP_PIERCING);  break;
        }
        g2.fillOval((int)x, (int)y, width, height);

        // Letter label
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Courier New", Font.BOLD, 12));
        String label = type == Type.MULTI_BALL  ? "M"
                     : type == Type.WIDE_PADDLE ? "W"
                     : "P";
        g2.drawString(label, (int)x + 6, (int)y + 14);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Effect
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Applies this power-up's effect to the current play scene.
     * Plays the power-up sound and delegates to the appropriate method.
     *
     * @param sc  the current PlayScene
     */
    public void apply(PlayScene sc) {
        SoundManager.getInstance().play(SoundManager.POWERUP);
        switch (type) {
            case MULTI_BALL:  sc.splitBall();            break;
            case WIDE_PADDLE: sc.getPaddle().expand(40); break;
            case PIERCING:    sc.setPiercingAllBalls();  break;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Collision & state
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns the bounding rectangle for collision detection with the paddle.
     */
    public Rectangle getBounds() {
        return new Rectangle((int)x, (int)y, width, height);
    }

    /**
     * Returns true when the power-up has been caught by the paddle.
     * Used with removeIf() in PlayScene.
     */
    public boolean isCollected() { return collected; }

    /**
     * Marks this power-up as collected — triggers removal next frame.
     */
    public void setCollected(boolean c) { collected = c; }

    /**
     * Returns true when the power-up has fallen below the screen.
     * Used with removeIf() in PlayScene.
     */
    public boolean isOffScreen(int screenHeight) { return y > screenHeight; }
}