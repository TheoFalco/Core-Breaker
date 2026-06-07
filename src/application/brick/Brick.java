package application.brick;

import java.awt.*;
import application.core.Theme;
import application.scene.PlayScene;

/**
 * CORE BREAKER
 *
 * Brick.java
 * Abstract base class for all brick types.
 * Defines common attributes (position, HP, color) and shared behavior
 * (rendering, hit detection). Subclasses must implement onDestroy().
 *
 * @version 1.0
 */
public abstract class Brick {

    // ─────────────────────────────────────────────────────────────────────────
    // Attributes — accessible by subclasses
    // ─────────────────────────────────────────────────────────────────────────

    protected int x, y;           // Position on screen
    protected int width, height;  // Dimensions
    protected int hp;             // Current hit points
    protected int maxHp;          // Max hit points (used for damage ratio)
    protected int points;         // Score awarded on destruction
    protected boolean destroyed;  // True when hp reaches 0
    protected Color color;        // Base color (darkened on damage)

    // ─────────────────────────────────────────────────────────────────────────
    // Constructor
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Creates a brick with the given position, size, HP, points and color.
     * maxHp is set to hp so the damage ratio starts at 1.0 (full brightness).
     */
    public Brick(int x, int y, int w, int h, int hp, int pts, Color c) {
        this.x       = x;
        this.y       = y;
        this.width   = w;
        this.height  = h;
        this.hp      = hp;
        this.maxHp   = hp;
        this.points  = pts;
        this.color   = c;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Game logic
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Called when the ball hits this brick.
     * Decrements HP and marks as destroyed when HP reaches 0.
     */
    public void hit() {
        if (destroyed) return;
        hp--;
        if (hp <= 0)
            destroyed = true;
    }

    /**
     * Called every frame. Empty by default.
     * Overridden by GhostBrick for its visibility animation.
     */
    public void update() { }

    /**
     * Called when the brick is destroyed.
     * Subclasses define their specific behavior (explosion, fade, etc.).
     *
     * @param sc  the current PlayScene — used to interact with other objects
     */
    public abstract void onDestroy(PlayScene sc);

    // ─────────────────────────────────────────────────────────────────────────
    // Rendering
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Draws the brick with three layers:
     * body (damage-tinted), border (dark outline), highlight (top reflection).
     */
    public void render(Graphics2D g2) {
        if (destroyed) return;

        // Body — color darkens as HP decreases
        g2.setColor(getDamagedColor());
        g2.fillRoundRect(x, y, width, height, 6, 6);

        // Border — dark outline for depth
        g2.setColor(new Color(0, 0, 0, 80));
        g2.drawRoundRect(x, y, width, height, 6, 6);

        // Highlight — top reflection
        g2.setColor(new Color(255, 255, 255, 60));
        g2.fillRoundRect(x + 2, y + 2, width - 4, height / 3, 4, 4);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Collision
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns the bounding rectangle used for collision detection.
     */
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Getters
    // ─────────────────────────────────────────────────────────────────────────

    public boolean isDestroyed() { return destroyed; }
    public int     getPoints()   { return points; }
    public Color   getColor()    { return color; }
    public double  getCenterX()  { return x + width  / 2.0; }
    public double  getCenterY()  { return y + height / 2.0; }

    // ─────────────────────────────────────────────────────────────────────────
    // Private utilities
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns the brick color darkened proportionally to remaining HP.
     * ratio = hp / maxHp → 1.0 = full color, 0.0 = black.
     */
    protected Color getDamagedColor() {
        float ratio = (float) hp / maxHp;
        return Theme.darken(color, ratio);
    }
}