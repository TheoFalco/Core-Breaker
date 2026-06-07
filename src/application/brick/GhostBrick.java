package application.brick;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import application.core.Theme;
import application.scene.PlayScene;

/**
 * CORE BREAKER
 *
 * GhostBrick.java
 * A brick that alternates between visible and invisible states.
 * When invisible (alpha == 0), it cannot be hit by the ball.
 * When visible, it behaves like a NormalBrick.
 * Supports variable HP for higher difficulty levels.
 *
 * @version 1.0
 */
public class GhostBrick extends Brick {

    // ─────────────────────────────────────────────────────────────────────────
    // Attributes
    // ─────────────────────────────────────────────────────────────────────────

    /** Whether the brick is currently in its visible phase. */
    private boolean visible;

    /** Frame counter — resets each time visible state toggles. */
    private int timer;

    /** Number of frames between visibility toggles (~1.5s at 60fps). */
    private int interval;

    /** Current opacity (0 = fully invisible, 255 = fully visible). */
    private int alpha;

    // ─────────────────────────────────────────────────────────────────────────
    // Constructors
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Creates a GhostBrick with default HP of 1.
     */
    public GhostBrick(int x, int y) {
        this(x, y, 1);
    }

    /**
     * Creates a GhostBrick with variable HP for difficulty scaling.
     * Points are proportional to HP.
     */
    public GhostBrick(int x, int y, int hp) {
        super(x, y, 70, 25, hp, 15 * hp, Theme.GHOST_BRICK);
        this.visible  = true;
        this.timer    = 0;
        this.interval = 90; // toggles every 90 frames (~1.5 seconds at 60fps)
        this.alpha    = 255;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Game logic
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Updates the visibility animation each frame.
     * Increments the timer and toggles visible state every interval frames.
     * Alpha fades in/out smoothly rather than switching instantly.
     */
    @Override
    public void update() {
        timer++;

        // Toggle visibility state
        if (timer >= interval) {
            visible = !visible;
            timer   = 0;
        }

        // Fade in when becoming visible
        if (visible && alpha < 255)
            alpha = Math.min(255, alpha + 15);

        // Fade out when becoming invisible
        if (!visible && alpha > 0)
            alpha = Math.max(0, alpha - 15);
    }

    /**
     * No special behavior on destruction.
     * The brick simply disappears when HP reaches 0.
     */
    @Override
    public void onDestroy(PlayScene sc) { }

    // ─────────────────────────────────────────────────────────────────────────
    // Rendering
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Draws the brick with transparency based on current alpha.
     * Always resets the composite to 1.0f after drawing
     * to avoid affecting other rendered objects.
     */
    @Override
    public void render(Graphics2D g2) {
        if (destroyed) return;

        // Apply transparency
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha / 255f));

        // Body
        g2.setColor(getDamagedColor());
        g2.fillRoundRect(x, y, width, height, 6, 6);

        // Border
        g2.setColor(new Color(0, 0, 0, 80));
        g2.drawRoundRect(x, y, width, height, 6, 6);

        // Highlight
        g2.setColor(new Color(255, 255, 255, 60));
        g2.fillRoundRect(x + 2, y + 2, width - 4, height / 3, 4, 4);

        // Reset composite — mandatory to avoid transparency bleeding
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1.0f));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Collision
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns an empty rectangle when invisible (alpha == 0) or destroyed,
     * effectively disabling collision with the ball.
     * Returns the real bounds when visible.
     */
    @Override
    public Rectangle getBounds() {
        if (alpha == 0 || destroyed)
            return new Rectangle(0, 0, 0, 0);
        return new Rectangle(x, y, width, height);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Getter
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns whether the brick is currently in its visible phase.
     * Used by FireBrick to skip invisible GhostBricks during explosions.
     */
    public boolean isVisible() { return visible; }
}