package application.system;

import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import application.brick.Brick;
import application.brick.FireBrick;
import application.brick.GhostBrick;
import application.brick.NormalBrick;

public class LevelLoader {
	public static final int BRICK_W = 72;
	public static final int BRICK_H = 20;
	public static final int PADDING_X = 3;
	public static final int PADDING_Y = 4;
	public static final int OFFSET_X = 11;
	public static final int OFFSET_Y = 50;
	
	public static List<Brick> load(int level){
		String path = "levels/level" + level + ".txt";
		int hp = DifficultyConfig.getBrickHp(level);
		return parseFile(path, hp);
	}
	
	public static int getLevelCount() {
		Path folder = Paths.get("levels");
		
		try (Stream<Path> flux = Files.list(folder)) {
			return (int) flux.filter(Files::isRegularFile).count();
		} catch (IOException e) {
			System.err.println("Reading error: " + e.getMessage());
			return 0;
		}
	}
	
	private static List<Brick> parseFile(String path, int hp) {
		List<Brick> bricks = new ArrayList<>();
		try (BufferedReader reader = new BufferedReader(new FileReader(path))) {
			String line;
			int row = 0;
			while ((line = reader.readLine()) != null) {
				String[] symbols = line.split(" ");
				for (int col = 0; col < symbols.length; col++) {
					int x = OFFSET_X + col * (BRICK_W + PADDING_X);
					int y = OFFSET_Y + row * (BRICK_H + PADDING_Y);
					Brick b = createBrick(symbols[col], x, y, hp);
					if (b != null)
						bricks.add(b);
				}
				row++;
			}
		} catch (IOException e) {
			System.err.println("Level load error: " + e.getMessage());
		}
		
		return bricks;
	}
	
	private static Brick createBrick(String symbol, int x, int y, int hp) {
		switch (symbol) {
			case "N": return new NormalBrick(x, y, hp);
			case "F": return new FireBrick(x, y);
			case "G": return new GhostBrick(x, y, hp);
			default: return null;
		}
	}
}
