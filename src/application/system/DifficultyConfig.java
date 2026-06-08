package application.system;

/**
 * CORE BREAKER
 *
 * DifficultyConfig.java
 * Static configuration class — defines how difficulty scales with level.
 * All values are computed from the level number with no state.
 *
 * Scaling rules:
 *   - Ball speed   : +0.3 px/frame per level
 *   - Paddle speed : +0.3 px/frame per level (cast to int)
 *   - Brick HP     : +1 every 5 levels (level 1-5 = 1HP, 6-10 = 2HP, etc.)
 *
 * @version 1.0
 */
public class DifficultyConfig {

    // ─────────────────────────────────────────────────────────────────────────
    // Base values
    // ─────────────────────────────────────────────────────────────────────────

    private static final double BASE_BALL_SPEED   = 4.5;
    private static final double BASE_PADDLE_SPEED = 6.0;
    private static final double SPEED_INCREMENT   = 0.3;

    // ─────────────────────────────────────────────────────────────────────────
    // Scaling methods
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns the ball speed for the given level.
     * Increases linearly by SPEED_INCREMENT per level.
     *
     * @param level  current level (1-based)
     */
    public static double getBallSpeed(int level) {
        return BASE_BALL_SPEED + (level - 1) * SPEED_INCREMENT;
    }

    /**
     * Returns the paddle speed for the given level.
     * Increases linearly by SPEED_INCREMENT per level (cast to int).
     *
     * @param level  current level (1-based)
     */
    public static int getPaddleSpeed(int level) {
        return (int)(BASE_PADDLE_SPEED + (level - 1) * SPEED_INCREMENT);
    }

    /**
     * Returns the brick HP for the given level.
     * Increases by 1 every 5 levels:
     *   levels  1-5  → 1 HP
     *   levels  6-10 → 2 HP
     *   levels 11-15 → 3 HP
     *
     * @param level  current level (1-based)
     */
    public static int getBrickHp(int level) {
        return 1 + (level - 1) / 5;
    }
}