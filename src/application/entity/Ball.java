package application.entity;

import java.awt.*;
import java.awt.geom.*;
import application.core.Theme;

/**
 * CORE BREAKER
 *
 * Ball.java
 * Represents the game ball — handles movement, bouncing,
 * wall detection and rendering.
 * Supports piercing mode (passes through bricks) via a timed power-up.
 * Can be split into copies at a rotated angle for the multi-ball power-up.
 *
 * @version 1.0
 */
public class Ball {

    // ─────────────────────────────────────────────────────────────────────────
    // Attributes
    // ─────────────────────────────────────────────────────────────────────────

    private double  x, y;           // Position (center of the ball)
    private double  vx, vy;         // Velocity vector (pixels per frame)
    private int     radius;         // Ball radius in pixels
    private double  speed;          // Scalar speed — maintained after bounces
    private boolean active;         // False when the ball exits the bottom edge
    private boolean isPiercing;     // True when the piercing power-up is active
    private int     piercingTimer;  // Countdown frames until piercing expires

    // ─────────────────────────────────────────────────────────────────────────
    // Constants
    // ─────────────────────────────────────────────────────────────────────────

    /** Maximum bounce angle from the center of the paddle (±60°). */
    private static final double MAX_ANGLE_RAD = Math.toRadians(60);

    // ─────────────────────────────────────────────────────────────────────────
    // Constructors
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Default constructor — places the ball at a standard starting position.
     */
    public Ball() {
        this(400, 500, 4.5);
    }

    /**
     * Creates a ball at the given position with the given speed.
     * Launch angle is randomized between -75° and -60° (always upward).
     */
    public Ball(double startX, double startY, double speed) {
        this.x             = startX;
        this.y             = startY;
        this.speed         = speed;
        this.radius        = 8;
        this.active        = true;
        this.isPiercing    = false;
        this.piercingTimer = 0;

        // Random upward launch angle
        double angle = Math.toRadians(-75 + Math.random() * 30);
        this.vx = speed * Math.cos(angle);
        this.vy = speed * Math.sin(angle);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Update (called each frame)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Advances the ball by one frame.
     * Updates position and decrements the piercing timer if active.
     */
    public void update() {
        if (!active) return;
        x += vx;
        y += vy;

        // Count down piercing duration
        if (piercingTimer > 0) {
            piercingTimer--;
            if (piercingTimer == 0)
                isPiercing = false;
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Bounces
    // ─────────────────────────────────────────────────────────────────────────

    /** Reverses horizontal velocity (left/right wall or side of a brick). */
    public void bounceX() { vx = -vx; }

    /** Reverses vertical velocity (ceiling, top of a brick). */
    public void bounceY() { vy = -vy; }

    /**
     * Bounces off the paddle with an angle based on the hit position.
     * Center hit → straight up. Edge hit → up to ±60°.
     * Always preserves the current scalar speed.
     *
     * @param paddleCenterX  horizontal center of the paddle
     * @param paddleHalfWidth  half the paddle width
     */
    public void bounceOffPaddle(double paddleCenterX, double paddleHalfWidth) {
        double offset = (x - paddleCenterX) / paddleHalfWidth;
        offset = Math.max(-1.0, Math.min(1.0, offset)); // clamp to [-1, 1]

        double angle = offset * MAX_ANGLE_RAD;
        vx =  speed * Math.sin(angle);
        vy = -speed * Math.cos(angle); // always upward
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Wall detection
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Checks and resolves collisions with screen boundaries.
     * Marks the ball as inactive if it exits below the screen.
     *
     * @return true if the ball fell below the screen (life lost)
     */
    public boolean checkWalls(int screenWidth, int screenHeight) {
        if (!active) return false;

        if (x - radius < 0)            { x = radius;                 bounceX(); }
        if (x + radius > screenWidth)  { x = screenWidth - radius;   bounceX(); }
        if (y - radius < 0)            { y = radius;                 bounceY(); }

        if (y - radius > screenHeight) {
            active = false;
            return true; // ball lost
        }
        return false;
    }

    /**
     * Returns the elliptical bounding shape for collision detection.
     */
    public Ellipse2D getBounds() {
        return new Ellipse2D.Double(x - radius, y - radius, radius * 2, radius * 2);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Rendering
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Draws the ball with three layers:
     * outer glow, colored body, and a small highlight reflection.
     * Color shifts to orange when piercing is active.
     */
    public void render(Graphics2D g2) {
        if (!active) return;

        Color ballColor = isPiercing ? Theme.BALL_PIERCING : Theme.BALL_NORMAL;

        // Outer glow
        Ellipse2D glowShape = new Ellipse2D.Double(
            x - radius - 4, y - radius - 4,
            (radius + 4) * 2, (radius + 4) * 2);
        Theme.drawGlow(g2, glowShape, ballColor, 3);

        // Body
        g2.setColor(ballColor);
        g2.fillOval((int)(x - radius), (int)(y - radius), radius * 2, radius * 2);

        // Highlight reflection
        g2.setColor(new Color(255, 255, 255, 180));
        g2.fillOval((int)(x - radius / 2), (int)(y - radius / 2),
                    radius / 2, radius / 2);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Power-ups & utilities
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Multiplies the current speed by the given factor.
     * Normalizes velocity to match the new speed without changing direction.
     */
    public void increaseSpeed(double factor) {
        speed *= factor;
        normalizeVelocity();
    }

    /**
     * Sets speed to an exact value without changing direction.
     */
    public void setSpeed(double newSpeed) {
        this.speed = newSpeed;
        normalizeVelocity();
    }

    /**
     * Rescales vx and vy so their magnitude equals the current speed.
     * Prevents speed drift caused by floating-point accumulation after bounces.
     */public void normalizeVelocity() {
         double currentSpeed = Math.hypot(vx, vy);
         if (currentSpeed == 0) return;
         vx = vx / currentSpeed * speed;
         vy = vy / currentSpeed * speed;
     }

     /**
      * Creates a copy of this ball rotated by the given angle offset.
      * Used by the multi-ball power-up to split the ball in two directions.
      * Copies the piercing state and timer from the original.
      *
      * @param angleOffsetDeg  rotation angle in degrees (e.g. 15 or -15)
      */
     public Ball split(double angleOffsetDeg) {
         Ball copy = new Ball(x, y, speed);
         double rad = Math.toRadians(angleOffsetDeg);
         double cos = Math.cos(rad), sin = Math.sin(rad);
         copy.vx            = vx * cos - vy * sin;
         copy.vy            = vx * sin + vy * cos;
         copy.isPiercing    = this.isPiercing;
         copy.piercingTimer = this.piercingTimer;
         return copy;
     }

     /** Returns true when the ball is no longer active (used with removeIf). */
     public boolean isNotActive() { return !active; }

     // ─────────────────────────────────────────────────────────────────────────
     // Getters / Setters
     // ─────────────────────────────────────────────────────────────────────────

     public double  getX()       { return x; }
     public double  getY()       { return y; }
     public double  getVx()      { return vx; }
     public double  getVy()      { return vy; }
     public int     getRadius()  { return radius; }
     public double  getSpeed()   { return speed; }
     public boolean isActive()   { return active; }
     public boolean isPiercing() { return isPiercing; }

     public void setX(double x)          { this.x = x; }
     public void setY(double y)          { this.y = y; }
     public void setActive(boolean a)    { this.active = a; }
     public void setPiercingTimer(int t) { this.piercingTimer = t; }

     /**
      * Activates or deactivates piercing mode.
      * When activated, starts a 300-frame (~5 second) countdown timer.
      */
     public void setPiercing(boolean piercing) {
         this.isPiercing = piercing;
         if (piercing) piercingTimer = 300;
     }
}