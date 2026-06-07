package application.system;

public class DifficultyConfig {
	private static final double BASE_BALL_SPEED = 4.5;
	private static final double BASE_PADDLE_SPEED = 6;
	private static final double SPEED_INCREMENT = 0.3;
	
	public static double getBallSpeed(int level) {
		return BASE_BALL_SPEED + (level - 1) * SPEED_INCREMENT;
	}
	
	public static int getPaddleSpeed(int level) {
		return (int)(BASE_PADDLE_SPEED + (level - 1) * SPEED_INCREMENT);
	}
	
	public static int getBrickHp(int level) {
		return 1 + (level - 1) / 5;
	}
}
