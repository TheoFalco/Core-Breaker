package application.brick;

import application.core.Theme;
import application.scene.PlayScene;

/**
 * CORE BREAKER
 *
 * NormalBrick.java
 * The standard brick of the game.
 * Has no special destruction behavior beyond a 30% chance
 * of spawning a power-up when destroyed.
 * Supports variable HP for difficulty scaling.
 *
 * @version 1.0
 */
public class NormalBrick extends Brick {

    // ─────────────────────────────────────────────────────────────────────────
    // Constructors
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Creates a NormalBrick with default HP of 1.
     */
    public NormalBrick(int x, int y) {
        this(x, y, 1);
    }

    /**
     * Creates a NormalBrick with variable HP for difficulty scaling.
     * Points are proportional to HP.
     */
    public NormalBrick(int x, int y, int hp) {
        super(x, y, 70, 25, hp, 10 * hp, Theme.NORMAL_BRICK);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Destruction behavior
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Called when this brick is destroyed.
     * Has a 30% chance of spawning a random power-up at the brick's center.
     *
     * @param sc  the current PlayScene — used to spawn the power-up
     */
    @Override
    public void onDestroy(PlayScene sc) {
        if (sc != null && Math.random() < 0.3)
            sc.spawnPowerUp(x + width / 2.0, y);
    }
}