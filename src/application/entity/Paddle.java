package application.entity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;
import application.core.Theme;

/**
 * CORE BREAKER
 *
 * Paddle.java
 * The player-controlled paddle at the bottom of the screen.
 * Moves horizontally via keyboard input and bounces the ball
 * with an angle based on the hit position.
 * Supports two power-up effects:
 *   - Wide paddle (expand) — temporarily increases width for 5 seconds
 *   - Charge shot (fireCharge) — activates a special visual state
 *
 * @version 1.0
 */
public class Paddle {

    // ─────────────────────────────────────────────────────────────────────────
    // Attributes
    // ─────────────────────────────────────────────────────────────────────────

    private int     x, y;          // Position (top-left corner)
    private int     width, height; // Dimensions
    private int     speed;         // Movement speed in pixels per frame
    private int     screenWidth;   // Used to clamp position within screen bounds
    private int     charge;        // Charge level (0–100) for the special shot
    private boolean isCharging;    // True when charge shot is active
    private boolean[] keys;        // Shared keyboard state from GamePanel

    // Wide paddle power-up
    private int widthTimer;    // Countdown frames until width resets (300 = 5s)
    private int originalWidth; // Stored width before expansion

    // ─────────────────────────────────────────────────────────────────────────
    // Constructors
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Default constructor — sets base values for all attributes.
     * Called by the main constructor via this().
     */
    public Paddle() {
        this.width         = 100;
        this.height        = 15;
        this.speed         = 6;
        this.charge        = 0;
        this.widthTimer    = 0;
        this.originalWidth = this.width;
    }

    /**
     * Creates a paddle at the given position with screen bounds and key input.
     *
     * @param startX       initial X position (top-left)
     * @param startY       initial Y position (top-left)
     * @param screenWidth  used to prevent the paddle from leaving the screen
     * @param keys         shared keyboard state array from GamePanel
     */
    public Paddle(int startX, int startY, int screenWidth, boolean[] keys) {
        this();
        this.x           = startX;
        this.y           = startY;
        this.screenWidth = screenWidth;
        this.keys        = keys;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Update (called each frame)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Reads keyboard input and updates position, charge, and width timer.
     * Movement is smooth because keys[] is checked every frame.
     * Width resets to original after widthTimer reaches 0.
     */
    public void update() {
        // Movement
        if (keys[KeyEvent.VK_LEFT])  moveLeft();
        if (keys[KeyEvent.VK_RIGHT]) moveRight();

        // Charge accumulation
        if (keys[KeyEvent.VK_SPACE] && charge < 100) charge++;

        // Wide paddle countdown
        if (widthTimer > 0) {
            widthTimer--;
            if (widthTimer == 0) {
                // Recentre paddle when width resets
                x     = x + (width - originalWidth) / 2;
                width = originalWidth;
                clamp();
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Rendering
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Draws the paddle with three layers:
     * outer glow, rounded body (color shifts when charging), and a highlight.
     */
    public void render(Graphics2D g2) {
        // Outer glow
        RoundRectangle2D shape = new RoundRectangle2D.Double(x, y, width, height, 10, 10);
        Theme.drawGlow(g2, shape, Theme.PADDLE_NORMAL, 3);

        // Body — purple when charging, blue otherwise
        g2.setColor(isCharging ? Theme.PADDLE_CHARGING : Theme.PADDLE_NORMAL);
        g2.fillRoundRect(x, y, width, height, 10, 10);

        // Highlight reflection
        g2.setColor(new Color(255, 255, 255, 60));
        g2.fillRoundRect(x + 4, y + 2, width - 8, height / 3, 6, 6);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Power-ups
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Expands the paddle width by extraWidth pixels for 300 frames (~5 seconds).
     * Ignored if an expansion is already active.
     * Recentres the paddle to avoid visual drift.
     *
     * @param extraWidth  pixels to add to the current width
     */
    public void expand(int extraWidth) {
        if (widthTimer > 0) return; // already expanded — ignore
        originalWidth = width;
        x             = x - extraWidth / 2;
        width         = width + extraWidth;
        widthTimer    = 300;
        clamp();
    }

    /**
     * Activates the charge shot if charge is full (100).
     * Resets charge to 0 and sets isCharging to true.
     */
    public void fireCharge() {
        if (charge >= 100) {
            isCharging = true;
            charge     = 0;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Collision helpers
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns the bounding rectangle for collision detection.
     */
    public Rectangle getBounds() {
        return new Rectangle(x, y, width, height);
    }

    /**
     * Returns the horizontal center — used by Ball.bounceOffPaddle().
     */
    public double getCenterX() { return x + width / 2.0; }

    /**
     * Returns half the paddle width — used by Ball.bounceOffPaddle().
     */
    public double getHalfWidth() { return width / 2.0; }

    // ─────────────────────────────────────────────────────────────────────────
    // Private movement
    // ─────────────────────────────────────────────────────────────────────────

    /** Moves left by speed pixels then clamps to screen bounds. */
    private void moveLeft()  { x -= speed; clamp(); }

    /** Moves right by speed pixels then clamps to screen bounds. */
    private void moveRight() { x += speed; clamp(); }

    /**
     * Prevents the paddle from leaving the screen horizontally.
     */
    private void clamp() {
        if (x < 0)                   x = 0;
        if (x + width > screenWidth) x = screenWidth - width;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Getters / Setters
    // ─────────────────────────────────────────────────────────────────────────

    public int     getX()         { return x; }
    public int     getY()         { return y; }
    public int     getWidth()     { return width; }
    public int     getHeight()    { return height; }
    public int     getCharge()    { return charge; }
    public boolean isCharging()   { return isCharging; }

    /** Sets movement speed — called by DifficultyConfig on level change. */
    public void setSpeed(int speed) { this.speed = speed; }
}