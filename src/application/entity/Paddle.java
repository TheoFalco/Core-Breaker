package application.entity;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.awt.geom.RoundRectangle2D;

import application.core.Theme;

public class Paddle {
	private int x;
	private int y;
	private int width;
	private int height;
	private int speed;
	private int screenWidth;
	private int charge;
	private boolean isCharging;
	private boolean[] keys;
	private int widthTimer;
	private int originalWidth;
	
	public Paddle() {
		this.width = 100;
		this.height = 15;
		this.speed = 6;
		this.charge = 0;
		this.widthTimer = 0;
		this.originalWidth = this.width;
	}
	
	public Paddle(int startX, int startY, int screenWidth, boolean[] keys) {
		this();
		this.x = startX;
		this.y = startY;
		this.screenWidth = screenWidth;
		this.keys = keys;
	}
	
	public void update() {
		if (keys[KeyEvent.VK_LEFT])
			moveLeft();
		
		if (keys[KeyEvent.VK_RIGHT])
			moveRight();
		
		if (keys[KeyEvent.VK_SPACE] && charge < 100) 
			charge++;
		
		if(widthTimer > 0) {
			widthTimer--;
			if (widthTimer == 0) {
				x = x + (width - originalWidth) / 2;
				width = originalWidth;
				clamp();
			}
		}
	}
	
	public void  render(Graphics2D g2) {
		// Exterior Glow
		RoundRectangle2D shape = new RoundRectangle2D.Double(x, y, width, height, 10, 10);
		Theme.drawGlow(g2, shape, Theme.PADDLE_NORMAL, 3);
		
		// Paddle's Body
		g2.setColor(isCharging ? Theme.PADDLE_CHARGING : Theme.PADDLE_NORMAL);
		g2.fillRoundRect(x, y, width, height, 10, 10);
		
		// Reflection
		g2.setColor(new Color(255, 255, 255, 60));
		g2.fillRoundRect(x + 4, y + 2, width - 8, height / 3, 6, 6);
	}
	
	public Rectangle getBounds() {
		return new Rectangle(x, y, width, height);
	}
	
	public void expand(int extraWidth) {
		if (widthTimer > 0) return;
		originalWidth = width;
		x = x - extraWidth / 2;
		width = width +  extraWidth;
		widthTimer = 300;
		clamp();
	}
	
	public void fireCharge() {
		if (charge >= 100) {
			isCharging = true;
			charge = 0;
		}
	}
	
	public double getCenterX() {
		return x + width / 2.0;
	}
	
	public double getHalfWidth() {
		return width / 2.0;
	}
	
	private void moveLeft() {
		x -= speed;
		clamp();
	}
	
	private void moveRight() {
		x += speed;
		clamp();
	}
	
	private void clamp() {
		if (x < 0)
			x = 0;
		
		if (x + width > screenWidth)
			x = screenWidth - width;
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public int getCharge() {
		return charge;
	}
	
	public boolean isCharging() {
		return isCharging;
	}
	
	public void setSpeed(int speed) {
		this.speed = speed;
	}
}
