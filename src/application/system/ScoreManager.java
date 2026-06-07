package application.system;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

public class ScoreManager {
	private static ScoreManager instance;
	private static final String SAVE_FILE = "highcore.txt";
	
	private int score;
	private int combo;
	private int multiplier;
	private int highscore;
	
	private ScoreManager() {
		multiplier = 1;
		highscore = loadHighScore();
	}
	
	public static ScoreManager getInstance() {
		if (instance == null)
			instance = new ScoreManager();
		return instance;
	}
	
	public void addPoints(int points) {
		score += points * multiplier;
		combo++;
		updateMultiplier();
	}
	
	public void resetCombo() {
		combo = 0;
	}
	
	public void reset() {
		combo = 0;
		score = 0;
	}
	
	public void saveHighScore() {
		if (score > highscore)
			highscore = score;
		
		try (PrintWriter pw = new PrintWriter(SAVE_FILE)) {
			pw.println(highscore);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public int loadHighScore() {
		File file = new File(SAVE_FILE);
		
		if (!file.exists())
			return 0;
		
		try (Scanner input = new Scanner(file)) {
			if (input.hasNextInt())
				return input.nextInt();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		return 0;
	}
	
	public int getScore() {
		return score;
	}
	
	public int getCombo() {
		return combo;
	}
	
	public int getMultiplier() {
		return multiplier;
	}
	
	public int getHighScore() {
		return highscore;
	}
	
	private void updateMultiplier() {
		multiplier = 1 + combo / 5;
	}
}
