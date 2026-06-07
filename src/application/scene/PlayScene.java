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
import application.powerup.PowerUp.Type;
import application.system.CollisionSystem;
import application.system.DifficultyConfig;
import application.system.LevelLoader;
import application.system.ScoreManager;
import application.system.SoundManager;

public class PlayScene {
	private static final int MAX_PARTICLES = 150;
	private Paddle paddle;
	private List<Ball> balls;
	private List<Brick> bricks;
	private ScoreManager scoreManager;
	private int lives;
	private int level;
	private boolean gameOver;
	private boolean levelCleared;
	private int screenWidth;
	private int screenHeight;
	private List<PowerUp> powerUps;
	private double ballSpeed;
	private int paddleSpeed;
	private List<Particle> particles;
	
	public PlayScene(int screenW, int screenH, boolean[] keys) {
		this.screenWidth = screenW;
		
		this.screenHeight = screenH;
		this.lives = 3;
		this.level = 1;
		
		this.scoreManager = ScoreManager.getInstance();
		this.ballSpeed = DifficultyConfig.getBallSpeed(level);
		this.paddleSpeed = DifficultyConfig.getPaddleSpeed(level);
		this.bricks = new ArrayList<>();
		this.paddle = new Paddle(screenW / 2 - 50, screenH - 60, screenW, keys);
		this.paddle.setSpeed(paddleSpeed);
		this.balls = new ArrayList<>();
		balls.add(new Ball(screenW / 2.0, screenH - 60, ballSpeed));
		ScoreManager.getInstance().reset();
		this.powerUps = new ArrayList<>();
		this.particles = new ArrayList<>();
		bricks = LevelLoader.load(level);
	}
	
	public void update() {
		if (gameOver)
			return;
		
		for (Ball b : balls)
			b.update();
		
		paddle.update();
		
		CollisionSystem.checkBallBricks(balls, bricks, this);
		for (Ball b : balls)
			CollisionSystem.checkBallPaddle(b, paddle);
		
		balls.forEach(b -> b.checkWalls(screenWidth, screenHeight));
		balls.removeIf(Ball::isNotActive);
		
		if (balls.isEmpty()) {
			onBallLost();
			if (!gameOver)
				resetBall();
		}
		
	    for (Brick b: bricks)
	    	b.update();
	    	    
	    for (PowerUp p: powerUps) {
	    	p.update();
	    	if (p.getBounds().intersects(paddle.getBounds())) {
	    		p.apply(this);
	    		p.setCollected(true);
	    	}
	    }
	    
	    powerUps.removeIf(p -> p.isOffScreen(screenHeight) || p.isCollected());
	    
	    for (Particle p: particles)
	    	p.update();
	    
	    particles.removeIf(Particle::isDead);
	    
	    checkLevelCleared();
	}
	
	public void render(Graphics2D g2) {
		for (Brick b: bricks)
			b.render(g2);
		
		for (Ball b : balls)
			b.render(g2);
		
		for (Particle p : particles)
			p.render(g2);
		
		for (PowerUp p : powerUps)
			p.render(g2);
		
		paddle.render(g2);
		renderHUD(g2);
		
		if (gameOver)
			renderGameOver(g2);
		
		if (levelCleared)
			renderVictory(g2);
	}

	public boolean isGameOver() {
		if (lives == 0)
			gameOver = true;
		
		return gameOver;
	}
	
	public boolean isLevelCleared() {
		return levelCleared;
	}
	
	public void addBrick(Brick b) {
		bricks.add(b);
	}
	
	private void resetBall() {
		balls.clear();
		double speed = DifficultyConfig.getBallSpeed(level);
		balls.add(new Ball(screenWidth / 2.0, screenHeight - 60, speed));
		paddle.setSpeed(DifficultyConfig.getPaddleSpeed(level));
		scoreManager.resetCombo();	
	}
	
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
	
	private void checkLevelCleared() {
		boolean allDestroyed = bricks.stream().allMatch(Brick::isDestroyed);
		if (allDestroyed == true) {
			level++;
			if (level > LevelLoader.getLevelCount()) {
				levelCleared = true;
				SoundManager.getInstance().play(SoundManager.VICTORY);
			}
			else {
				bricks = LevelLoader.load(level);
				resetBall();
			}
		}
	}
	
	private void renderHUD(Graphics2D g2) {
		// 
		g2.setColor(Theme.HUD_BAR_BG);
		g2.fillRect(0, 568, screenWidth, 32);
		
		//
		g2.setColor(Theme.ELECTRIC_BLUE);
		g2.setStroke(new BasicStroke(1));
		g2.drawLine(0, 568, screenWidth, 568);
		g2.setStroke(new BasicStroke(1));
		
		// Labels & Values
		g2.setFont(Theme.FONT_HUD_SMALL);
		g2.setColor(Theme.HUD_TEXT);
		g2.drawString("ENERGY", 20, 582);
		g2.drawString("CORES", 180, 582);
		g2.drawString("LEVEL", 340, 582);
		g2.drawString("COMBO", 500, 582);
		g2.drawString("RECORD", 680, 582);
		
		g2.setFont(Theme.FONT_HUD);
		g2.setColor(Theme.CYAN_BRIGHT);
		g2.drawString(String.valueOf(scoreManager.getScore()), 20, 597);
		g2.drawString(String.valueOf(lives), 180, 597);
		g2.drawString(String.valueOf(level), 340, 597);
		g2.drawString("x" + scoreManager.getMultiplier(), 500, 597);
		g2.drawString(String.valueOf(scoreManager.getHighScore()), 680, 597);	
	}
	
	public List<Brick> getBricks() {
		return bricks;
	}
	
	public Ball getBall() {
		return balls.isEmpty() ? null : balls.get(0);
	}
	
	public Paddle getPaddle() {
		return paddle;
	}
	
	public void splitBall() {
		if(balls.size() >= 20) 
			return;
		
		List<Ball> newBalls = new ArrayList<>();
		for (Ball b : balls) {
			newBalls.add(b.split(15));
			newBalls.add(b.split(-15));
		}
		balls.addAll(newBalls);
		
		while (balls.size() > 20)
			balls.remove(balls.size() - 1);
	}
	
	public void setPiercingAllBalls() {
	    for (Ball b : balls)
	        b.setPiercing(true);
	}
	
	public void spawnPowerUp(double x, double y) {
		PowerUp.Type[] types = PowerUp.Type.values();
		PowerUp.Type randomType = types[(int)(Math.random() * types.length)];
		powerUps.add(new PowerUp(x, y, randomType));
	}
	
	public void spawnParticles(double x, double y, Color color) {
		if (particles.size() >= MAX_PARTICLES) 
			return;
		
		int count = 8 + (int)(Math.random() * 6);
		
		int available = MAX_PARTICLES - particles.size();
		count = Math.min(count, available);
		
		for (int i = 0; i < count; i++)
			particles.add(new Particle(x, y, color));
	}
	
	private void renderVictory(Graphics2D g2) {
		// Black Overlay
		g2.setColor(new Color(0, 0, 0, 180));
	    g2.fillRect(0, 0, screenWidth, screenHeight);
	    
		// Title
	    Theme.drawGlowText(g2, "SYSTEM OVERLOADED",
	            Theme.FONT_TITLE, Theme.CYAN_BRIGHT,
	            screenWidth / 2, screenHeight / 2 - 60);
	    
		// Final Score
	    Theme.drawGlowText(g2, "ENERGY COLLECTED: " + scoreManager.getScore(),
	            Theme.FONT_SUBTITLE, Theme.ELECTRIC_BLUE,
	            screenWidth / 2, screenHeight / 2 + 10);
	    
		// Record
	    Theme.drawGlowText(g2, "RECORD: " + scoreManager.getHighScore(),
	            Theme.FONT_HUD, Theme.ELECTRIC_BLUE,
	            screenWidth / 2, screenHeight / 2 + 50);
	    
		// Restart
	    Theme.drawGlowText(g2, "[ R ] RESTART",
	            Theme.FONT_HUD, Theme.WHITE,
	            screenWidth / 2, screenHeight / 2 + 100);
	}

	private void renderGameOver(Graphics2D g2) {
		// Black Overlay
		g2.setColor(new Color(0, 0, 0, 180));
	    g2.fillRect(0, 0, screenWidth, screenHeight);
	    
		// Title
	    Theme.drawGlowText(g2, "CORE DESTROYED",
	            Theme.FONT_TITLE, Theme.FIRE_BRICK,
	            screenWidth / 2, screenHeight / 2 - 60);
	    
		// Final Score
	    Theme.drawGlowText(g2, "ENERGY COLLECTED: " + scoreManager.getScore(),
	            Theme.FONT_SUBTITLE, Theme.CYAN_BRIGHT,
	            screenWidth / 2, screenHeight / 2 + 10);
	    
		// Record
	    Theme.drawGlowText(g2, "RECORD: " + scoreManager.getHighScore(),
	            Theme.FONT_HUD, Theme.ELECTRIC_BLUE,
	            screenWidth / 2, screenHeight / 2 + 50);
	    
		// Restart
	    Theme.drawGlowText(g2, "[ R ] RESTART",
	            Theme.FONT_HUD, Theme.WHITE,
	            screenWidth / 2, screenHeight / 2 + 100);
	}
	
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
}
