package application.system;

import java.awt.Rectangle;
import java.util.List;

import application.brick.Brick;
import application.brick.FireBrick;
import application.entity.Ball;
import application.entity.Paddle;
import application.scene.PlayScene;

public class CollisionSystem {
	public static void checkBallBricks(List<Ball> balls, List<Brick> bricks, PlayScene sc) {
	    for (Ball b : balls) {
	        for (Brick brick : bricks) {
	            if (!brick.isDestroyed() && intersects(b, brick.getBounds())) {
	                brick.hit();
	                if (brick.isDestroyed()) {
	                    ScoreManager.getInstance().addPoints(brick.getPoints());
	                    sc.spawnParticles(brick.getCenterX(), brick.getCenterY(), brick.getColor());
	                    
	                    if (brick instanceof FireBrick)
	                        SoundManager.getInstance().play(SoundManager.EXPLODE);
	                    else
	                        SoundManager.getInstance().play(SoundManager.HIT_BRICK);
	                    
	                    brick.onDestroy(sc);
	                } else {
	                    SoundManager.getInstance().play(SoundManager.HIT_BRICK);
	                }
	                if (!b.isPiercing()) {
	                    resolveOverlap(b, brick.getBounds());
	                    break;
	                }
	            }
	        }
	    }
	}
	
	public static void checkBallPaddle(Ball b, Paddle p) {
		if (intersects(b, p.getBounds()) && b.getVy() > 0 && b.getY() < p.getY() + p.getHeight()) {
			SoundManager.getInstance().play(SoundManager.HIT_PADDLE);
			b.bounceOffPaddle(p.getCenterX(), p.getHalfWidth());
		}
	}
	
	private static boolean intersects(Ball b, Rectangle r) {
		double closestX = clamp(b.getX(), r.getX(), r.getX() + r.getWidth());
		double closestY = clamp(b.getY(), r.getY(), r.getY() + r.getHeight());
		double distance = Math.hypot(b.getX() - closestX, b.getY() - closestY);
		return distance < b.getRadius();
	}
	
	private static void resolveOverlap(Ball b, Rectangle r) {
		double overlapLeft = (b.getX() + b.getRadius()) - r.getX();
		double overlapRight = (r.getX() + r.getWidth()) - (b.getX() - b.getRadius());
		double overlapTop = (b.getY() + b.getRadius()) - r.getY();
		double overlapBottom = (r.getY() + r.getHeight()) - (b.getY() - b.getRadius());
		
		double minH = Math.min(overlapLeft, overlapRight);
		double minV = Math.min(overlapTop, overlapBottom);
		
		if (minH < minV)
			b.bounceX();
		else
			b.bounceY();
	}
	
	private static double clamp(double val, double min, double max) {
		return Math.max(min,  Math.min(max, val));
	}
}
