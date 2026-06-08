package application.system;

import java.awt.Rectangle;
import java.util.List;
import application.brick.Brick;
import application.brick.FireBrick;
import application.entity.Ball;
import application.entity.Paddle;
import application.scene.PlayScene;

/**
 * CORE BREAKER
 *
 * CollisionSystem.java
 * Static utility class — handles all collision detection and resolution.
 * No state, no instantiation — all methods are called directly.
 *
 * Handles three collision types:
 *   - Ball vs Bricks  → hit, score, particles, sound, optional bounce
 *   - Ball vs Paddle  → angle-based bounce, sound
 *   - Ball vs Walls   → managed by Ball.checkWalls() directly
 *
 * @version 1.0
 */
public class CollisionSystem {

    // ─────────────────────────────────────────────────────────────────────────
    // Ball vs Bricks
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Checks each ball against each brick for collisions.
     * When a collision is detected:
     *   - Applies hit() to the brick
     *   - If destroyed: awards points, spawns particles, plays sound, calls onDestroy()
     *   - If not piercing: resolves the overlap (bounce) and stops checking further bricks
     *   - If piercing: skips bounce and continues through remaining bricks
     *
     * @param balls   all active balls
     * @param bricks  current level's brick list
     * @param sc      the active PlayScene — passed to onDestroy() and spawnParticles()
     */
    public static void checkBallBricks(List<Ball> balls, List<Brick> bricks, PlayScene sc) {
        for (Ball b : balls) {
            for (Brick brick : bricks) {
                if (!brick.isDestroyed() && intersects(b, brick.getBounds())) {
                    brick.hit();

                    if (brick.isDestroyed()) {
                        // Award points and visual feedback
                        ScoreManager.getInstance().addPoints(brick.getPoints());
                        sc.spawnParticles(brick.getCenterX(), brick.getCenterY(), brick.getColor());

                        // Sound — explosion for FireBrick, hit for others
                        if (brick instanceof FireBrick)
                            SoundManager.getInstance().play(SoundManager.EXPLODE);
                        else
                            SoundManager.getInstance().play(SoundManager.HIT_BRICK);

                        brick.onDestroy(sc);
                    } else {
                        // Brick survived the hit — play hit sound
                        SoundManager.getInstance().play(SoundManager.HIT_BRICK);
                    }

                    // Piercing — skip bounce and continue through bricks
                    if (!b.isPiercing()) {
                        resolveOverlap(b, brick.getBounds());
                        break; // one brick per frame per ball
                    }
                }
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Ball vs Paddle
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Checks if the ball hits the top surface of the paddle.
     * Only triggers when the ball is moving downward (vy > 0)
     * and within the paddle's vertical range — prevents side-hit bugs.
     *
     * @param b  the ball to check
     * @param p  the paddle
     */
    public static void checkBallPaddle(Ball b, Paddle p) {
        if (intersects(b, p.getBounds())
                && b.getVy() > 0
                && b.getY() < p.getY() + p.getHeight()) {
            SoundManager.getInstance().play(SoundManager.HIT_PADDLE);
            b.bounceOffPaddle(p.getCenterX(), p.getHalfWidth());
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private — collision math
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Precise circle vs rectangle collision detection.
     * Finds the closest point on the rectangle to the ball center,
     * then checks if the distance is less than the ball's radius.
     * More accurate than bounding-box intersection.
     */
    private static boolean intersects(Ball b, Rectangle r) {
        double closestX = clamp(b.getX(), r.getX(), r.getX() + r.getWidth());
        double closestY = clamp(b.getY(), r.getY(), r.getY() + r.getHeight());
        double distance = Math.hypot(b.getX() - closestX, b.getY() - closestY);
        return distance < b.getRadius();
    }

    /**
     * Resolves a ball-brick overlap by bouncing in the axis of least penetration.
     * Calculates the four overlap distances and bounces on the smallest axis —
     * this determines whether the ball hit a horizontal or vertical face.
     */
    private static void resolveOverlap(Ball b, Rectangle r) {
        double overlapLeft   = (b.getX() + b.getRadius()) - r.getX();
        double overlapRight  = (r.getX() + r.getWidth())  - (b.getX() - b.getRadius());
        double overlapTop    = (b.getY() + b.getRadius()) - r.getY();
        double overlapBottom = (r.getY() + r.getHeight()) - (b.getY() - b.getRadius());

        double minH = Math.min(overlapLeft, overlapRight);
        double minV = Math.min(overlapTop,  overlapBottom);

        // Bounce on the axis with the smallest penetration
        if (minH < minV) b.bounceX();
        else             b.bounceY();
    }

    /**
     * Clamps a value between min and max.
     * Used to find the closest point on a rectangle to the ball center.
     */
    private static double clamp(double val, double min, double max) {
        return Math.max(min, Math.min(max, val));
    }
}