package application.entity;

import java.awt.*;
import java.awt.geom.*;

import application.core.Theme;

public class Ball {
	
	//Position & movement
	private double x;
	private double y;
	private double vx;
	private double vy;
	
	//Properties
	private int radius;
	private double speed;
	private boolean active;
	private boolean isPiercing;
	private int piercingTimer;
	
	//Constant
	private static final double MAX_ANGLE_RAD = Math.toRadians(60);
	
    // ─────────────────────────────────────────────────────────────────────────
    // Constructor
    // ─────────────────────────────────────────────────────────────────────────
	
	
	public Ball() {
		this(400, 500, 4.5);
	}
	
	public Ball(double startX, double startY, double speed) {
		this.x = startX;
		this.y = startY;
		this.speed = speed;
		this.radius = 8;
		this.active = true;
		this.isPiercing = false;
		this.piercingTimer = 0;
		
		double angle = Math.toRadians(-75 + Math.random() * 30);
		this.vx = speed * Math.cos(angle);
		this.vy = speed * Math.sin(angle);
	}
	
    // ─────────────────────────────────────────────────────────────────────────
    // Updates (called each frame)
    // ─────────────────────────────────────────────────────────────────────────
	
	public void update() {
		if(!active) return;
		x += vx;
		y += vy;
		
		if (piercingTimer > 0) {
			piercingTimer--;
			if (piercingTimer == 0)
				isPiercing = false;
		}
	}
	
    // ─────────────────────────────────────────────────────────────────────────
    // Bounces
    // ─────────────────────────────────────────────────────────────────────────
	
	public void bounceX() {
		vx = -vx;
	}
	
	public void bounceY() {
		vy = -vy;
	}
	
	public void bounceOffPaddle(double paddleCenterX, double paddleHalfWidth) {
		double offset = (x - paddleCenterX) / paddleHalfWidth;
		offset = Math.max(-1.0, Math.min(1.0, offset));
		
		double angle = offset * MAX_ANGLE_RAD;
		
		vx = speed * Math.sin(angle);
		vy = -speed * Math.cos(angle);
	}
	
    // ─────────────────────────────────────────────────────────────────────────
    // Limits & collisions
    // ─────────────────────────────────────────────────────────────────────────
	
	public boolean checkWalls(int screenWidth, int screenHeight) {
		if (!active) return false;
		
		if (x - radius < 0) {
			x = radius;
			bounceX();
		}
		
		if (x + radius > screenWidth) {
			x = screenWidth - radius;
			bounceX();
		}
		
		if (y - radius < 0) {
			y = radius;
			bounceY();
		}
		
		if (y - radius > screenHeight) {
			active = false;
			return true;
		}
		
		return false;
	}
	
	public Ellipse2D getBounds() {
		return new Ellipse2D.Double(x - radius, y - radius, radius * 2, radius * 2);
	}
	
    // ─────────────────────────────────────────────────────────────────────────
    // Rendering
    // ─────────────────────────────────────────────────────────────────────────
	
	public void render(Graphics2D g2) {
		if (!active) return;
		
		// Exterior Glow
		Ellipse2D glowShape = new Ellipse2D.Double(
				x - radius - 4, y - radius - 4,
				(radius + 4) * 2, (radius + 4) * 2);
		Theme.drawGlow(g2, glowShape, isPiercing ? Theme.BALL_PIERCING : Theme.BALL_NORMAL, 3);
		
		// Ball's Body
		g2.setColor(isPiercing ? Theme.BALL_PIERCING: Theme.BALL_NORMAL);
		g2.fillOval((int)(x - radius), (int)(y - radius), radius * 2, radius * 2);
		
		// Reflection
		g2.setColor(new Color(255, 255, 255, 180));
        g2.fillOval((int)(x - radius / 2), (int)(y - radius / 2), radius / 2, radius / 2);
	}
	
    // ─────────────────────────────────────────────────────────────────────────
    // Power-ups & utilites
    // ─────────────────────────────────────────────────────────────────────────
	
	public void increaseSpeed(double f) {
		speed *= f;
		normalizeVelocity();
	}
	
	public void setSpeed(double newSpeed) {
		this.speed = newSpeed;
		normalizeVelocity();
	}
	
	public void normalizeVelocity() {
		double currentSpeed = Math.hypot(vx, vy);
		if (currentSpeed == 0) return;
		vx = vx / currentSpeed * speed;
		vy = vy / currentSpeed * speed;
	}
	
	public Ball split(double angleOffsetDeg) {
		Ball copy = new Ball(x, y, speed);
        double rad = Math.toRadians(angleOffsetDeg);
        double cos = Math.cos(rad), sin = Math.sin(rad);
        copy.vx = vx * cos - vy * sin;
        copy.vy = vx * sin + vy * cos;
        copy.isPiercing = this.isPiercing;
        copy.piercingTimer = this.piercingTimer;
        
        return copy;
	}
	
	public boolean isNotActive() { 
		return !active; 
		}
	
    // ─────────────────────────────────────────────────────────────────────────
    // Getters / Setters
    // ─────────────────────────────────────────────────────────────────────────
	
	public double  getX()          { return x; }
    public double  getY()          { return y; }
    public double  getVx()         { return vx; }
    public double  getVy()		   { return vy; }
    public int     getRadius()     { return radius; }
    public double  getSpeed()      { return speed; }
    public boolean isActive()      { return active; }
    public boolean isPiercing()    { return isPiercing; }
 
    public void setX(double x)                  { this.x = x; }
    public void setY(double y)                  { this.y = y; }
    public void setActive(boolean active)       { this.active = active; }
    public void setPiercing(boolean piercing)   { 
    	this.isPiercing = piercing; 
    	if (piercing)
    		piercingTimer = 300;
    }
    public void setPiercingTimer(int t) { this.piercingTimer = t; }
}
