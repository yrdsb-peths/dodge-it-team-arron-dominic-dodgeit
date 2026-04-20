import greenfoot.*;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

public class AudioManager {
    private static HashMap<String, GreenfootSound> sounds = new HashMap<>();
    private static HashMap<String, List<GreenfootSound>> voicePools = new HashMap<>();
    
    // New: Storage for "Original" volumes
    private static HashMap<String, Integer> baseVolumes = new HashMap<>();
    private static HashMap<String, Integer> basePoolVolumes = new HashMap<>();
    
    // This remembers which sounds were active before we hit pause
    private static List<GreenfootSound> activeBeforePause = new ArrayList<>();

    private static int masterVolume = GameConfig.MASTER_VOLUME; // 0 to 200

    public static void init() {
        // Pre-load background music (using 100 as base, we scale it later)
        loadSound("dio_bgm", "eye_of_heaven_dio_bgm.mp3", 60);
        loadSound("lost_bgm", "brawl_stars_lost_bgm.mp3", 50);
        loadSound("car_crash", "car_crash.mp3", 70);
        loadSound("speed_up_time","speed_up_time.mp3", 100);
        loadSound("summon_stand","summon_stand.mp3", 200);
        loadSound("muda_barrage","muda_barrage.mp3", 90);
        
        // Pre-load voice pools
        
        String[] rewind = {
            "rewind1.mp3"
        };
        loadVoicePool("rewind", rewind, 60);
        
        String[] loseFiles = {"dio_voiceline/dio_lost.mp3", "dio_voiceline/dio_lost2.mp3"};
        loadVoicePool("dioLostVoices", loseFiles, 60);
        
        String[] dioBattleCry = {
            "dio_voiceline/wry.mp3", "dio_voiceline/high.mp3", 
            "dio_voiceline/muda_muda.mp3", "dio_voiceline/Voicy_Timestop DiegoBrando.mp3"
        };
        loadVoicePool("dioBattleCry", dioBattleCry, 100);
        
        updateAllVolumes(); // Set initial volumes based on master
    }

    private static void loadSound(String key, String file, int volume) {
        GreenfootSound s = new GreenfootSound(file);
        //Pre load
        s.play(); 
        s.stop();
        
        sounds.put(key, s);
        baseVolumes.put(key, volume);
    }
    
    private static void loadVoicePool(String key, String[] files, int volume) {
        List<GreenfootSound> pool = new ArrayList<>();
        for (String f : files) {
            GreenfootSound s = new GreenfootSound(f);
            //Pre-load
            s.play();
            s.stop();
            
            pool.add(s);
        }
        voicePools.put(key, pool);
        basePoolVolumes.put(key, volume);
    }

    // --- Master Volume Logic ---
    
    public static void setMasterVolume(int level) {
        masterVolume = level;
        if (masterVolume < 0) masterVolume = 0;
        if (masterVolume > 200) masterVolume = 200;
        updateAllVolumes();
    }

    private static void updateAllVolumes() {
        // Update single sounds
        for (String key : sounds.keySet()) {
            int base = baseVolumes.get(key);
            sounds.get(key).setVolume((base * masterVolume) / 200);
        }
        // Update pools
        for (String key : voicePools.keySet()) {
            int base = basePoolVolumes.get(key);
            for (GreenfootSound s : voicePools.get(key)) {
                s.setVolume((base * masterVolume) / 200);
            }
        }
    }

    // --- Special Control Logic ---

    /**
     * Stops everything currently playing.
     */
    public static void stopAll() {
        for (GreenfootSound s : sounds.values()) s.stop();
        for (List<GreenfootSound> pool : voicePools.values()) {
            for (GreenfootSound s : pool) s.stop();
        }
    }

    // --- Standard Methods ---

    public static void play(String key) {
        if (sounds.containsKey(key)) {
            GreenfootSound s = sounds.get(key);
            if (s.isPlaying()) s.stop();
            s.play();
        }
    }

    public static void playPool(String poolKey) {
        if (voicePools.containsKey(poolKey)) {
            List<GreenfootSound> pool = voicePools.get(poolKey);
            int index = Greenfoot.getRandomNumber(pool.size());
            pool.get(index).play(); 
        }
    }
    
    public static void playLoop(String key) {
        if (sounds.containsKey(key)) {
            GreenfootSound s = sounds.get(key);
            if (!s.isPlaying()) s.playLoop();
        }
    }
    
    public static void stop(String key) {
        if (sounds.containsKey(key)) sounds.get(key).stop();
    }
    
     /**
     * Pauses a specific looping sound.
     */
    public static void pause(String key) {
        if (sounds.containsKey(key)) {
            sounds.get(key).pause();
        }
    }

    /**
     * Resumes or Starts a looping sound.
     */
    public static void resume(String key) {
        if (sounds.containsKey(key)) {
            sounds.get(key).playLoop();
        }
    }
    
    /**
     * UNIVERSAL SOUND CONTROL (With Memory)
     * @param pause true to pause active sounds, false to resume ONLY those sounds.
     */
    public static void setAllSoundsPaused(boolean pause) {
        if (pause) {
            activeBeforePause.clear();

            // 1. Pause BGM (we want to resume this later)
            if (sounds.get("dio_bgm").isPlaying()) {
                activeBeforePause.add(sounds.get("dio_bgm"));
                sounds.get("dio_bgm").pause();
            }

            // 2. KILL everything else (One-shots like "crash" or "wry")
            // We STOP them, not pause, so they don't echo during rewind
            for (String key : sounds.keySet()) {
                if (!key.equals("dio_bgm") && sounds.get(key).isPlaying()) {
                    sounds.get(key).stop();
                }
            }
            
            for (List<GreenfootSound> pool : voicePools.values()) {
                for (GreenfootSound s : pool) {
                    if (s.isPlaying()) s.stop(); 
                }
            }
        } 
        else {
            // Resume only the BGM
            for (GreenfootSound s : activeBeforePause) {
                s.play(); 
            }
            activeBeforePause.clear();
        }
    }
}