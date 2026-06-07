package application.entity;

import java.awt.*;

public class Particle {
	private double x;
	private double y;
	private double vx;
	private double vy;
	private int alpha;
	private float size;
	private Color color;
	
	public Particle(double x, double y, Color color) {
		this.x = x;
		this.y = y;
		this.color = color;
		this.alpha = 255;
		this.size = 4 + (float)(Math.random() * 4);
		
		// Random trajectory
		double angle = Math.random() * Math.PI * 2;
		double speed = 1.5 + Math.random() * 3;
		this.vx = Math.cos(angle) * speed;
		this.vy = Math.sin(angle) * speed;
	}
	
	public void update() {
		x += vx;
		y += vy;
		vy += 0.1;
		alpha = Math.max(0, alpha - 12);
		size = Math.max(0, size - 0.15f);
	}
	
	public void render(Graphics2D g2) {
		if (alpha <= 0)
			return;
		
		g2.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha));
		g2.fillOval((int)x, (int)y, (int)size, (int)size);
	}
	
	public boolean isDead() {
		return alpha <= 0;
	}
}
