package application.powerup;

import java.awt.*;

import application.scene.PlayScene;
import application.system.SoundManager;

public class PowerUp {
	public enum Type { MULTI_BALL, WIDE_PADDLE, PIERCING }
	private double x;
	private double y;
	private int width;
	private int height;
	private int fallSpeed;
	private boolean collected;
	private Type type;
	
	public PowerUp(double x, double y, Type type) {
	    this.x         = x;
	    this.y         = y;
	    this.type      = type;
	    this.width     = 20;
	    this.height    = 20;
	    this.fallSpeed = 3;
	    this.collected = false;
	}
	
	public void update() {
		y += fallSpeed;
	}
	
	public void render(Graphics2D g2) {
		switch (type) {
		case MULTI_BALL: 
			g2.setColor(new Color(0, 220, 220));
			break;
		case WIDE_PADDLE:
			g2.setColor(new Color(0, 220, 0));
			break;
		case PIERCING:
			g2.setColor(new Color(255, 150, 0));
			break;
		}
		g2.fillOval((int)x, (int)y, width, height);
		
		g2.setColor(Color.WHITE);
		g2.setFont(new Font("Arial", Font.BOLD, 12));
		String label = type == Type.MULTI_BALL ? "M" 
					: type == Type.WIDE_PADDLE ? "W" 
					: "P";
		g2.drawString(label, (int)x + 6, (int)y + 14);
	}
	
	public void apply(PlayScene sc) {
		SoundManager.getInstance().play(SoundManager.POWERUP);
		switch (type) {
		case MULTI_BALL: 
			sc.splitBall();
			break;
		case WIDE_PADDLE:
			sc.getPaddle().expand(40);
			break;
		case PIERCING:
			sc.setPiercingAllBalls();
			break;
		}
	}
	
	public Rectangle getBounds() {
		return new Rectangle((int)x, (int)y, width, height);
	}
	
	public boolean isCollected() {
		return collected;
	}
	
	public void setCollected(boolean c) {
		collected = c;
	}
	
	public boolean isOffScreen(int screenHeight) {
		return y > screenHeight;
	}
}
