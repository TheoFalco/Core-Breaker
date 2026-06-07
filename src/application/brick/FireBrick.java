package application.brick;

import java.awt.Color;
import application.core.Theme;
import application.scene.PlayScene;
import application.system.ScoreManager;

/**
 * CORE BREAKER
 *
 * FireBrick.java
 * An explosive brick that triggers a chain reaction on destruction.
 * When destroyed, it damages all adjacent bricks within its blast radius.
 * If neighboring bricks are also FireBricks, they explode recursively.
 *
 * @version 1.0
 */
public class FireBrick extends Brick {

    // ─────────────────────────────────────────────────────────────────────────
    // Attributes
    // ─────────────────────────────────────────────────────────────────────────

    /** Number of adjacent cells affected by the explosion (1 = all 8 neighbors). */
    private int blastRadius;

    // ─────────────────────────────────────────────────────────────────────────
    // Constructor
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Creates a FireBrick at the given position.
     * Always has 1 HP — it explodes on first hit.
     */
    public FireBrick(int x, int y) {
        super(x, y, 70, 25, 1, 10, Theme.FIRE_BRICK);
        this.blastRadius = 1;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Destruction behavior
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Triggers the explosion when this brick is destroyed.
     */
    @Override
    public void onDestroy(PlayScene sc) {
        explode(sc);
    }

    /**
     * Damages all bricks within the blast radius.
     * Uses pixel distance between brick centers to determine neighbors.
     * Destroyed neighbors trigger their own onDestroy() — enabling chain reactions.
     * GhostBricks that are invisible are ignored by the explosion.
     *
     * @param sc  the current PlayScene — provides access to the brick list
     */
    private void explode(PlayScene sc) {
        int centerX = x + width  / 2;
        int centerY = y + height / 2;

        // Thresholds — brick size + padding + tolerance
        int threshX = width  + 8;
        int threshY = height + 8;

        for (Brick b : sc.getBricks()) {
            // Skip already destroyed bricks and self
            if (b.isDestroyed() || b == this) continue;

            // Skip invisible GhostBricks — they cannot be hit
            if (b instanceof GhostBrick && !((GhostBrick) b).isVisible()) continue;

            int bCenterX = b.x + b.width  / 2;
            int bCenterY = b.y + b.height / 2;

            double distX = Math.abs(bCenterX - centerX);
            double distY = Math.abs(bCenterY - centerY);

            // Hit neighbor if within blast radius
            if (distX < threshX && distY < threshY) {
                b.hit();
                if (b.isDestroyed()) {
                    ScoreManager.getInstance().addPoints(b.getPoints());
                    b.onDestroy(sc); // recursive — triggers chain reaction
                }
            }
        }
    }
}