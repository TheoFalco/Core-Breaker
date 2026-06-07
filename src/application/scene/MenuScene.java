package application.scene;

import java.awt.*;

import application.core.Theme;
import application.system.ScoreManager;

public class MenuScene {
	private int screenWidth;
    private int screenHeight;
    private int glowTimer = 0;
    
    public MenuScene(int screenW, int screenH) {
    	this.screenWidth = screenW;
    	this.screenHeight = screenH;
    }
    
    public void update() {
    	glowTimer++;
    }
    
    public void render(Graphics2D g2) {
    	// Animated Title
    	float glowIntensity = (float)(Math.sin(glowTimer * 0.02) * 0.3 + 0.5);
    	Color titleColor = new Color(
                (int)(Theme.CYAN_BRIGHT.getRed()   * glowIntensity),
                (int)(Theme.CYAN_BRIGHT.getGreen() * glowIntensity),
                (int)(Theme.CYAN_BRIGHT.getBlue()  * glowIntensity)
        );
    	
    	// Main Title
    	Theme.drawGlowText(g2, "CORE BREAKER",
                Theme.FONT_TITLE, titleColor,
                screenWidth / 2, screenHeight / 2 - 120);
    	
    	//SubTitles
    	g2.setColor(new Color(Theme.ELECTRIC_BLUE.getRed(),
                Theme.ELECTRIC_BLUE.getGreen(),
                Theme.ELECTRIC_BLUE.getBlue(), 80));
    	g2.drawLine(screenWidth / 2 - 200, screenHeight / 2 - 45,
    			screenWidth / 2 + 200, screenHeight / 2 - 45);
    	
    	// Options
    	Theme.drawGlowText(g2, "[ ENTER ]  START",
                Theme.FONT_SUBTITLE, Theme.WHITE,
                screenWidth / 2, screenHeight / 2 + 20);

    	Theme.drawGlowText(g2, "[ ESC ]  PAUSE IN GAME",
    		    Theme.FONT_HUD_SMALL, Theme.ELECTRIC_BLUE,
    		    screenWidth / 2, screenHeight / 2 + 70);
        
     // High score
        Theme.drawGlowText(g2,
            "RECORD: " + ScoreManager.getInstance().getHighScore(),
            Theme.FONT_HUD, Theme.NEON_PURPLE,
            screenWidth / 2, screenHeight / 2 + 130);

        // Version
        g2.setFont(Theme.FONT_HUD_SMALL);
        g2.setColor(new Color(255, 255, 255, 40));
        g2.drawString("v1.0", screenWidth - 40, screenHeight - 10);
    }
}
