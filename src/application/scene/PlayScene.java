package application.scene;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import application.brick.Brick;
import application.core.Theme;
import application.entity.Ball;
import application.entity.Paddle;
import application.entity.Particle;
import application.powerup.PowerUp;
import application.system.CollisionSystem;
import application.system.DifficultyConfig;
import application.system.LevelLoader;
import application.system.ScoreManager;
import application.system.SoundManager;

/**
 * CORE BREAKER
 *
 * PlayScene.java
 * The main gameplay scene — orchestrates all game objects and systems.
 * Owns the ball list, paddle, bricks, power-ups and particles.
 * Delegates collision detection to CollisionSystem and score tracking
 * to ScoreManager. Handles level progression and all overlay screens
 * (Game Over, Victory, Pause).
 *
 * @version 1.0
 */
public class PlayScene {

    // ─────────────────────────────────────────────────────────────────────────
    // Constants
    // ─────────────────────────────────────────────────────────────────────────

    /** Maximum number of simultaneous particles — prevents FPS drops. */
    private static final int MAX_PARTICLES = 150;

    // ─────────────────────────────────────────────────────────────────────────
    // Attributes
    // ─────────────────────────────────────────────────────────────────────────

    private Paddle        paddle;       // Player-controlled paddle
    private List<Ball>    balls;        // Active balls (multi-ball supported)
    private List<Brick>   bricks;       // Current level's brick grid
    private List<PowerUp> powerUps;     // Falling power-up collectibles
    private List<Particle> particles;   // Visual destruction particles

    private ScoreManager scoreManager;  // Singleton — tracks score and combo

    private int     lives;          // Remaining lives (starts at 3)
    private int     level;          // Current level number (starts at 1)
    private boolean gameOver;       // True when lives reach 0
    private boolean levelCleared;   // True when all levels are completed
    private int     screenWidth;    // Used for wall detection and HUD layout
    private int     screenHeight;

    // ─────────────────────────────────────────────────────────────────────────
    // Constructor
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Initializes a new game session at level 1 with 3 lives.
     * Resets the ScoreManager so each session starts from 0.
     *
     * @param screenW  game area width in pixels
     * @param screenH  game area height in pixels
     * @param keys     shared keyboard state array from GamePanel
     */
    public PlayScene(int screenW, int screenH, boolean[] keys) {
        this.screenWidth  = screenW;
        this.screenHeight = screenH;
        this.lives        = 3;
        this.level        = 1;

        this.scoreManager = ScoreManager.getInstance();
        ScoreManager.getInstance().reset();

        // Difficulty scaling
        double ballSpeed  = DifficultyConfig.getBallSpeed(level);
        int paddleSpeed   = DifficultyConfig.getPaddleSpeed(level);

        // Initialize objects
        this.paddle    = new Paddle(screenW / 2 - 50, screenH - 60, screenW, keys);
        this.paddle.setSpeed(paddleSpeed);
        this.balls     = new ArrayList<>();
        this.powerUps  = new ArrayList<>();
        this.particles = new ArrayList<>();
        this.bricks    = new ArrayList<>();

        balls.add(new Ball(screenW / 2.0, screenH - 60, ballSpeed));
        bricks = LevelLoader.load(level);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Update (called each frame)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Advances all game objects by one frame.
     * Order: ball → paddle → collisions → wall check → bricks → power-ups
     *        → particles → level cleared check.
     * Frozen when gameOver is true.
     */
    public void update() {
        if (gameOver) return;

        // ── Ball & paddle ────────────────────────────────────────────────────
        for (Ball b : balls)    b.update();
        paddle.update();

        // ── Collisions ───────────────────────────────────────────────────────
        CollisionSystem.checkBallBricks(balls, bricks, this);
        for (Ball b : balls) CollisionSystem.checkBallPaddle(b, paddle);

        // ── Wall detection & ball removal ────────────────────────────────────
        balls.forEach(b -> b.checkWalls(screenWidth, screenHeight));
        balls.removeIf(Ball::isNotActive);
        if (balls.isEmpty()) {
            onBallLost();
            if (!gameOver) resetBall();
        }

        // ── Bricks ───────────────────────────────────────────────────────────
        for (Brick b : bricks) b.update();

        // ── Power-ups ────────────────────────────────────────────────────────
        for (PowerUp p : powerUps) {
            p.update();
            if (p.getBounds().intersects(paddle.getBounds())) {
                p.apply(this);
                p.setCollected(true);
            }
        }
        powerUps.removeIf(p -> p.isOffScreen(screenHeight) || p.isCollected());

        // ── Particles ────────────────────────────────────────────────────────
        for (Particle p : particles) p.update();
        particles.removeIf(Particle::isDead);

        // ── Level progression ────────────────────────────────────────────────
        checkLevelCleared();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Rendering
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Draws all game objects in the correct layering order.
     * Overlays (Game Over, Victory) are drawn last on top of everything.
     */
    public void render(Graphics2D g2) {
        for (Brick b    : bricks)    b.render(g2);
        for (Ball b     : balls)     b.render(g2);
        for (Particle p : particles) p.render(g2);
        for (PowerUp p  : powerUps)  p.render(g2);
        paddle.render(g2);
        renderHUD(g2);

        if (gameOver)     renderGameOver(g2);
        if (levelCleared) renderVictory(g2);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Private game logic
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Resets the ball and paddle speed to current level values.
     * Clears all active balls and resets the combo counter.
     */
    private void resetBall() {
        balls.clear();
        double speed = DifficultyConfig.getBallSpeed(level);
        balls.add(new Ball(screenWidth / 2.0, screenHeight - 60, speed));
        paddle.setSpeed(DifficultyConfig.getPaddleSpeed(level));
        scoreManager.resetCombo();
    }

    /**
     * Decrements lives when all balls are lost.
     * Triggers game over and saves the high score when lives reach 0.
     */
    private boolean onBallLost() {
        lives--;
        SoundManager.getInstance().play(SoundManager.LIFE_LOST);
        ScoreManager.getInstance().resetCombo();
        if (lives == 0) {
            gameOver = true;
            SoundManager.getInstance().play(SoundManager.GAME_OVER);
            ScoreManager.getInstance().saveHighScore();
        }
        return true;
    }

    /**
     * Checks whether all bricks have been destroyed.
     * Loads the next level or sets levelCleared if all levels are done.
     */
    private void checkLevelCleared() {
        boolean allDestroyed = bricks.stream().allMatch(Brick::isDestroyed);
        if (allDestroyed) {
            level++;
            if (level > LevelLoader.getLevelCount()) {
                levelCleared = true;
                SoundManager.getInstance().play(SoundManager.VICTORY);
            } else {
                bricks = LevelLoader.load(level);
                resetBall();
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HUD & overlays
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Draws the bottom HUD bar with energy, lives, level, combo and record.
     * Uses a dark background strip with a glowing separator line.
     */
    private void renderHUD(Graphics2D g2) {
        // Background bar
        g2.setColor(Theme.HUD_BAR_BG);
        g2.fillRect(0, 568, screenWidth, 32);

        // Separator line
        g2.setColor(Theme.ELECTRIC_BLUE);
        g2.setStroke(new BasicStroke(1));
        g2.drawLine(0, 568, screenWidth, 568);

        // Labels
        g2.setFont(Theme.FONT_HUD_SMALL);
        g2.setColor(Theme.HUD_TEXT);
        g2.drawString("ENERGY", 20,  582);
        g2.drawString("CORES",  180, 582);
        g2.drawString("LEVEL",  340, 582);
        g2.drawString("COMBO",  500, 582);
        g2.drawString("RECORD", 680, 582);

        // Values
        g2.setFont(Theme.FONT_HUD);
        g2.setColor(Theme.CYAN_BRIGHT);
        g2.drawString(String.valueOf(scoreManager.getScore()),      20,  597);
        g2.drawString(String.valueOf(lives),                        180, 597);
        g2.drawString(String.valueOf(level),                        340, 597);
        g2.drawString("x" + scoreManager.getMultiplier(),          500, 597);
        g2.drawString(String.valueOf(scoreManager.getHighScore()),  680, 597);
    }

    /**
     * Draws the Game Over overlay with final score and restart prompt.
     * Displayed when all lives are lost.
     */
    private void renderGameOver(Graphics2D g2) {
        // Dark overlay
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(0, 0, screenWidth, screenHeight);

        Theme.drawGlowText(g2, "CORE DESTROYED",
            Theme.FONT_TITLE, Theme.FIRE_BRICK,
            screenWidth / 2, screenHeight / 2 - 60);

        Theme.drawGlowText(g2, "ENERGY COLLECTED: " + scoreManager.getScore(),
            Theme.FONT_SUBTITLE, Theme.CYAN_BRIGHT,
            screenWidth / 2, screenHeight / 2 + 10);

        Theme.drawGlowText(g2, "RECORD: " + scoreManager.getHighScore(),
            Theme.FONT_HUD, Theme.ELECTRIC_BLUE,
            screenWidth / 2, screenHeight / 2 + 50);

        Theme.drawGlowText(g2, "[ R ] RESTART",
            Theme.FONT_HUD, Theme.WHITE,
            screenWidth / 2, screenHeight / 2 + 100);
    }

    /**
     * Draws the Victory overlay with final score and restart prompt.
     * Displayed when all levels are completed.
     */
    private void renderVictory(Graphics2D g2) {
        // Dark overlay
        g2.setColor(new Color(0, 0, 0, 180));
        g2.fillRect(0, 0, screenWidth, screenHeight);

        Theme.drawGlowText(g2, "SYSTEM OVERLOADED",
            Theme.FONT_TITLE, Theme.CYAN_BRIGHT,
            screenWidth / 2, screenHeight / 2 - 60);

        Theme.drawGlowText(g2, "ENERGY COLLECTED: " + scoreManager.getScore(),
            Theme.FONT_SUBTITLE, Theme.ELECTRIC_BLUE,
            screenWidth / 2, screenHeight / 2 + 10);

        Theme.drawGlowText(g2, "RECORD: " + scoreManager.getHighScore(),
            Theme.FONT_HUD, Theme.ELECTRIC_BLUE,
            screenWidth / 2, screenHeight / 2 + 50);

        Theme.drawGlowText(g2, "[ R ] RESTART",
            Theme.FONT_HUD, Theme.WHITE,
            screenWidth / 2, screenHeight / 2 + 100);
    }

    /**
     * Draws the Pause overlay — shown when the game is paused.
     * Called from GamePanel when state is PAUSED.
     */
    public void renderPause(Graphics2D g2) {
        g2.setColor(new Color(0, 0, 0, 150));
        g2.fillRect(0, 0, screenWidth, screenHeight);

        Theme.drawGlowText(g2, "PAUSED",
            Theme.FONT_TITLE, Theme.ELECTRIC_BLUE,
            screenWidth / 2, screenHeight / 2 - 40);

        Theme.drawGlowText(g2, "[ ESC ] RESUME  |  [ R ] RESTART",
            Theme.FONT_HUD, Theme.WHITE,
            screenWidth / 2, screenHeight / 2 + 20);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Public API — used by CollisionSystem, PowerUp, FireBrick
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Splits all active balls into two at ±15° angle offset.
     * Capped at 20 balls total to prevent performance issues.
     */
    public void splitBall() {
        if (balls.size() >= 20) return;
        List<Ball> newBalls = new ArrayList<>();
        for (Ball b : balls) {
            newBalls.add(b.split( 15));
            newBalls.add(b.split(-15));
        }
        balls.addAll(newBalls);
        while (balls.size() > 20)
            balls.remove(balls.size() - 1);
    }

    /**
     * Activates piercing mode on all active balls.
     * Each ball handles its own 300-frame countdown timer.
     */
    public void setPiercingAllBalls() {
        for (Ball b : balls) b.setPiercing(true);
    }

    /**
     * Spawns a random power-up at the given position.
     * Called by NormalBrick.onDestroy() with a 30% probability.
     */
    public void spawnPowerUp(double x, double y) {
        PowerUp.Type[] types = PowerUp.Type.values();
        PowerUp.Type randomType = types[(int)(Math.random() * types.length)];
        powerUps.add(new PowerUp(x, y, randomType));
    }

    /**
     * Spawns 8–12 particles at the given position with the given color.
     * Capped at MAX_PARTICLES to prevent frame rate drops during chain explosions.
     */
    public void spawnParticles(double x, double y, Color color) {
        if (particles.size() >= MAX_PARTICLES) return;
        int count     = 8 + (int)(Math.random() * 6);
        int available = MAX_PARTICLES - particles.size();
        count         = Math.min(count, available);
        for (int i = 0; i < count; i++)
            particles.add(new Particle(x, y, color));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Getters
    // ─────────────────────────────────────────────────────────────────────────

    public boolean    isGameOver()     { return gameOver; }
    public boolean    isLevelCleared() { return levelCleared; }
    public List<Brick> getBricks()     { return bricks; }
    public Paddle     getPaddle()      { return paddle; }

    /** Returns the first active ball — used by PIERCING power-up. */
    public Ball getBall() { return balls.isEmpty() ? null : balls.get(0); }

    /** Adds a brick to the current level grid. */
    public void addBrick(Brick b) { bricks.add(b); }
}