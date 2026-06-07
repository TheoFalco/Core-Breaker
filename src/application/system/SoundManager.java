package application.system;

import javax.sound.sampled.*;
import java.io.File;
import java.util.HashMap;
import java.util.Map;


public class SoundManager {
	// Singleton
	private static SoundManager instance;
	public static SoundManager getInstance() {
		if (instance == null)
			instance = new SoundManager();
		return instance;
	}
	
	// Attributes
	private Map<String, Clip> clips = new HashMap<>();
    private boolean muted = false;
    private Clip musicClip = null;
    
    // Key Songs
    public static final String HIT_BRICK   = "hit_brick";
    public static final String HIT_PADDLE  = "hit_paddle";
    public static final String EXPLODE     = "explode";
    public static final String POWERUP     = "powerup";
    public static final String LIFE_LOST   = "life_lost";
    public static final String GAME_OVER   = "game_over";
    public static final String VICTORY     = "victory";
    public static final String MUSIC       = "music";

    private SoundManager() {
        loadAll();
    }
    
    // Loading
    private void loadAll() {
        load(HIT_BRICK,  "sounds/hit_brick.wav");
        load(HIT_PADDLE, "sounds/hit_paddle.wav");
        load(EXPLODE,    "sounds/explode.wav");
        load(POWERUP,    "sounds/powerup.wav");
        load(LIFE_LOST,  "sounds/life_lost.wav");
        load(GAME_OVER,  "sounds/game_over.wav");
        load(VICTORY,    "sounds/victory.wav");
        load(MUSIC,      "sounds/music.wav");
    }
    
    public void load(String key, String path) {
        try {
            File file = new File(path);
            if (!file.exists()) {
                System.out.println("Sound not found: " + path);
                return;
            }
            AudioInputStream ais = AudioSystem.getAudioInputStream(file);
            Clip clip = AudioSystem.getClip();
            clip.open(ais);
            clips.put(key, clip);
        } catch (Exception e) {
            System.err.println("Sound load error [" + key + "]: " + e.getMessage());
        }
    }
    
    // Play
    public void play(String key) {
        if (muted) return;
        Clip clip = clips.get(key);
        if (clip == null) return;
        clip.stop();
        clip.setFramePosition(0);
        clip.start();
    }

    public void playMusic() {
        if (muted) return;
        Clip clip = clips.get(MUSIC);
        if (clip == null) return;
        clip.loop(Clip.LOOP_CONTINUOUSLY);
        clip.start();
    }

    public void stopMusic() {
        Clip clip = clips.get(MUSIC);
        if (clip != null) clip.stop();
    }

    public void setMuted(boolean muted) {
        this.muted = muted;
        if (muted) stopMusic();
        else playMusic();
    }

    public boolean isMuted() { return muted; }
}
