package application.entity;

import java.awt.*;

/**
 * CORE BREAKER
 *
 * Particle.java
 * A short-lived visual particle emitted when a brick is destroyed.
 * Each particle has a random direction, speed and size.
 * It fades out and shrinks over ~21 frames until fully transparent.
 * Managed by PlayScene — removed automatically when dead.
 *
 * @version 1.0
 */
public class Particle {

    // ─────────────────────────────────────────────────────────────────────────
    // Attributes
    // ─────────────────────────────────────────────────────────────────────────

    private double x, y;   // Position
    private double vx, vy; // Velocity vector
    private int    alpha;  // Opacity (255 = fully visible, 0 = dead)
    private float  size;   // Current diameter in pixels
    private Color  color;  // Base color (inherited from the destroyed brick)

    // ─────────────────────────────────────────────────────────────────────────
    // Constructor
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Creates a particle at the given position with a random trajectory.
     * Speed varies between 1.5 and 4.5 px/frame.
     * Size varies between 4 and 8 px.
     *
     * @param x      spawn X position (usually brick center)
     * @param y      spawn Y position (usually brick center)
     * @param color  base color inherited from the destroyed brick
     */
    public Particle(double x, double y, Color color) {
        this.x     = x;
        this.y     = y;
        this.color = color;
        this.alpha = 255;
        this.size  = 4 + (float)(Math.random() * 4);

        // Random direction and speed
        double angle = Math.random() * Math.PI * 2;
        double speed = 1.5 + Math.random() * 3;
        this.vx = Math.cos(angle) * speed;
        this.vy = Math.sin(angle) * speed;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Update (called each frame)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Advances the particle by one frame.
     * Applies gravity, fades opacity, and shrinks size.
     * The particle lives for approximately 21 frames (255 / 12).
     */
    public void update() {
        x    += vx;
        y    += vy;
        vy   += 0.1;                       // Slight downward gravity
        alpha = Math.max(0, alpha - 12);   // Fade out
        size  = Math.max(0, size  - 0.15f); // Shrink
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Rendering
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Draws the particle as a small filled oval at its current opacity.
     * Skipped entirely when alpha reaches 0.
     */
    public void render(Graphics2D g2) {
        if (alpha <= 0) return;
        g2.setColor(new Color(color.getRed(), color.getGreen(),
                              color.getBlue(), alpha));
        g2.fillOval((int)x, (int)y, (int)size, (int)size);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // State
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns true when the particle is fully transparent.
     * Used with removeIf() in PlayScene to clean up dead particles.
     */
    public boolean isDead() { return alpha <= 0; }
}