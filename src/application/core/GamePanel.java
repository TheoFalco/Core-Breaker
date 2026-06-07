package application.core;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.*;
import application.scene.MenuScene;
import application.scene.PlayScene;
import application.system.SoundManager;

/**
 * CORE BREAKER
 *
 * GamePanel.java
 * Main game panel — extends JPanel and acts as the root of the game.
 * Manages the game loop (60fps via Swing Timer), state transitions
 * (MENU / PLAYING / PAUSED), keyboard input, and rendering delegation.
 *
 * @version 1.0
 */
public class GamePanel extends JPanel {

    // ─────────────────────────────────────────────────────────────────────────
    // Game states
    // ─────────────────────────────────────────────────────────────────────────

    /** The three possible states of the game. */
    private enum GameState { MENU, PLAYING, PAUSED }

    // ─────────────────────────────────────────────────────────────────────────
    // Attributes
    // ─────────────────────────────────────────────────────────────────────────

    private GameState state;    // Current game state
    private MenuScene menu;     // Main menu scene
    private PlayScene scene;    // Active play scene (null until first game start)
    private Timer     gameTimer; // Swing timer driving the game loop at ~60fps
    private boolean[] keys;     // Keyboard state — true when a key is held down
    private long      lastTime; // Timestamp of the last frame (for FPS calculation)
    private int       fps;      // Current frames per second

    // ─────────────────────────────────────────────────────────────────────────
    // Constructor
    // ─────────────────────────────────────────────────────────────────────────

    public GamePanel() {
        state = GameState.MENU;
        keys  = new boolean[256];

        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.BLACK);

        setupKeyListener();
        setFocusable(true);
        requestFocusInWindow();

        menu  = new MenuScene(800, 600);
        scene = null;

        startLoop();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Input
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Registers the keyboard listener.
     * Handles input differently depending on the current game state.
     *
     * MENU    → ENTER starts a new game
     * PAUSED  → ESC resumes, R restarts
     * PLAYING → ESC pauses, M mutes, R restarts on game over / victory
     */
    private void setupKeyListener() {
        addKeyListener(new KeyAdapter() {

            @Override
            public void keyPressed(KeyEvent e) {

                // ── Menu state ───────────────────────────────────────────────
                if (state == GameState.MENU) {
                    if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                        scene = new PlayScene(800, 600, keys);
                        state = GameState.PLAYING;
                        SoundManager.getInstance().playMusic();
                    }
                    return;
                }

                // ── Paused state ─────────────────────────────────────────────
                if (state == GameState.PAUSED) {
                    if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
                        state = GameState.PLAYING;
                    if (e.getKeyCode() == KeyEvent.VK_R) {
                        scene = new PlayScene(800, 600, keys);
                        state = GameState.PLAYING;
                    }
                    return;
                }

                // ── Playing state ────────────────────────────────────────────
                if (e.getKeyCode() < keys.length)
                    keys[e.getKeyCode()] = true;

                // Pause
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
                    state = GameState.PAUSED;

                // Mute / unmute music
                if (e.getKeyCode() == KeyEvent.VK_M)
                    SoundManager.getInstance().setMuted(!SoundManager.getInstance().isMuted());

                // Restart — only available on game over or victory
                if (e.getKeyCode() == KeyEvent.VK_R
                        && (scene.isGameOver() || scene.isLevelCleared())) {
                    scene = new PlayScene(800, 600, keys);
                    state = GameState.PLAYING;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() < keys.length)
                    keys[e.getKeyCode()] = false;
            }
        });
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Game loop
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Starts the Swing Timer at ~60fps.
     * Each tick calls update() then repaint().
     */
    private void startLoop() {
        gameTimer = new Timer(1000 / 60, e -> {
            update();
            repaint();
        });
        gameTimer.start();
    }

    /**
     * Updates the active scene each frame.
     * PlayScene is not updated when paused — the game is frozen.
     */
    private void update() {
        if (state == GameState.MENU)
            menu.update();
        else if (state == GameState.PLAYING)
            scene.update();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Rendering
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Renders the current state each frame.
     * Applies antialiasing and the futuristic background before delegating
     * to the active scene. Overlays the pause screen when paused.
     */
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;

        Theme.applyRenderingHints(g2);
        Theme.drawBackground(g2, getWidth(), getHeight());

        if (state == GameState.MENU) {
            menu.render(g2);
        } else if (state == GameState.PLAYING || state == GameState.PAUSED) {
            scene.render(g2);
            if (state == GameState.PAUSED)
                scene.renderPause(g2);
        }

        showFPS(g2);
    }

    /**
     * Displays the current FPS in the top-left corner.
     * Uses a subtle white color to stay unobtrusive.
     */
    private void showFPS(Graphics2D g2) {
        long actualTime = System.currentTimeMillis();
        if (lastTime != 0)
            fps = (int)(1000 / (actualTime - lastTime));
        lastTime = actualTime;

        g2.setFont(Theme.FONT_HUD_SMALL);
        g2.setColor(new Color(255, 255, 255, 80));
        g2.drawString("FPS: " + fps, 10, 20);
    }
}