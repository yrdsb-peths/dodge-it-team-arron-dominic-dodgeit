/*
 * ─────────────────────────────────────────────────────────────────────────────
 * AudioManager.java  —  CENTRALISED AUDIO LOADING AND PLAYBACK
 * ─────────────────────────────────────────────────────────────────────────────
 * Role:
 *   A static utility class that pre-loads all sounds at startup (init()),
 *   then provides play/stop/loop/pause methods accessed from anywhere.
 *
 * Two sound types:
 *   Single sounds (HashMap<String, GreenfootSound>):
 *     One sound file per key.  Used for music (BGM) and one-shot SFX.
 *     play() stops and restarts; playLoop() loops continuously.
 *
 *   Voice pools (HashMap<String, List<GreenfootSound>>):
 *     Multiple sounds per key.  playPool() picks one at random.
 *     Used for death voice lines and boss battle cries.
 *
 * Master volume:
 *   Every sound has a "base volume" defined in init().
 *   Actual volume = (base × masterVolume) / 200.
 *   Changing masterVolume updates all sounds immediately.
 *
 * Pre-loading:
 *   init() calls play()/stop() on every sound immediately after loading.
 *   This forces Greenfoot to decode the audio file into memory so the first
 *   real playback has no lag spike.
 *
 * Rewind audio handling (setAllSoundsPaused):
 *   During rewind, all sounds are muted.  The BGM is paused (remembered),
 *   everything else is stopped (forgotten).  On resume, only the remembered
 *   BGM is restarted.
 *
 * ⚠ KNOWN ISSUE:
 *   setAllSoundsPaused() hardcodes "dio_bgm" instead of using
 *   GameConfig.ACTIVE_CHARACTER.bgmKey.  If a non-Dio character is active,
 *   the wrong BGM will be targeted.  Fix: replace "dio_bgm" with
 *   GameConfig.ACTIVE_CHARACTER.bgmKey in that method.
 *
 * Interacts with:
 *   PlayingState (music start/stop, rewind audio control),
 *   Ability_MadeInHeaven (speed_up_time sound),
 *   Ability_StandPunch (muda_barrage sound),
 *   Roadroller / Train (car_crash sound),
 *   GenericPlayer (death voice pool),
 *   GameConfig (MASTER_VOLUME), MyWorld (init() called in constructor)
 * ─────────────────────────────────────────────────────────────────────────────
 */
import greenfoot.*;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;

public class AudioManager {

    /** All single-sound entries, keyed by a short string identifier. */
    private static HashMap<String, GreenfootSound>        sounds      = new HashMap<>();
    /** All voice pool entries, keyed by pool name; each pool has multiple clips. */
    private static HashMap<String, List<GreenfootSound>>  voicePools  = new HashMap<>();

    /** Base (unscaled) volume for each single sound.  Stored so masterVolume rescaling works. */
    private static HashMap<String, Integer>               baseVolumes     = new HashMap<>();
    /** Base volume for each voice pool. */
    private static HashMap<String, Integer>               basePoolVolumes = new HashMap<>();

    /** Sounds that were playing just before a rewind pause — resumed after rewind. */
    private static List<GreenfootSound>                   activeBeforePause = new ArrayList<>();

    /** Global volume multiplier (0–200).  Set from GameConfig.MASTER_VOLUME. */
    private static int masterVolume = GameConfig.MASTER_VOLUME;

    // ─────────────────────────────────────────────────────────────────────────
    // INITIALISATION — called once from MyWorld constructor
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Pre-loads all game sounds into RAM.
     * Must be called ONCE before any sound is played.
     * Each sound is play()ed and immediately stop()ped to force Greenfoot
     * to decode it, eliminating first-play lag spikes.
     */
    public static void init() {
        // ── Single sounds ─────────────────────────────────────────────────────
        loadSound("dio_bgm",       "eye_of_heaven_dio_bgm.mp3",  60);
        loadSound("lost_bgm",      "brawl_stars_lost_bgm.mp3",   50);
        loadSound("car_crash",     "car_crash.mp3",               70);
        loadSound("speed_up_time", "speed_up_time.mp3",          100);
        loadSound("summon_stand",  "summon_stand.mp3",            200);
        loadSound("muda_barrage",  "muda_barrage.mp3",             90);
        loadSound("menu_bgm",  "soul_knight_menu.mp3",             120);

        // ── Voice pools ───────────────────────────────────────────────────────
        loadVoicePool("rewind",        new String[]{ "rewind1.mp3" }, 60);
        loadVoicePool("dioLostVoices", new String[]{ "dio_voiceline/dio_lost.mp3", "dio_voiceline/dio_lost2.mp3" }, 60);
        loadVoicePool("dioBattleCry",  new String[]{
            "dio_voiceline/wry.mp3", "dio_voiceline/high.mp3",
            "dio_voiceline/muda_muda.mp3", "dio_voiceline/Voicy_Timestop DiegoBrando.mp3"
        }, 100);

        updateAllVolumes(); // apply master volume to everything at startup
    }

    /** Loads one sound file and pre-decodes it.  Stores base volume for scaling. */
    private static void loadSound(String key, String file, int volume) {
        GreenfootSound s = new GreenfootSound(file);
        s.play(); s.stop(); // pre-decode into memory
        sounds.put(key, s);
        baseVolumes.put(key, volume);
    }

    /** Loads a pool of sound files (one pool key, many clips) and pre-decodes all. */
    private static void loadVoicePool(String key, String[] files, int volume) {
        List<GreenfootSound> pool = new ArrayList<>();
        for (String f : files) {
            GreenfootSound s = new GreenfootSound(f);
            s.play(); s.stop(); // pre-decode
            pool.add(s);
        }
        voicePools.put(key, pool);
        basePoolVolumes.put(key, volume);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // MASTER VOLUME
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Sets the master volume and immediately updates all loaded sounds.
     * @param level  0 (silent) to 200 (full).
     */
    public static void setMasterVolume(int level) {
        masterVolume = Math.max(0, Math.min(200, level));
        updateAllVolumes();
    }

    /** Recalculates and applies actual volume for every sound using the master level. */
    private static void updateAllVolumes() {
        for (String key : sounds.keySet()) {
            sounds.get(key).setVolume((baseVolumes.get(key) * masterVolume) / 200);
        }
        for (String key : voicePools.keySet()) {
            for (GreenfootSound s : voicePools.get(key)) {
                s.setVolume((basePoolVolumes.get(key) * masterVolume) / 200);
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PLAYBACK METHODS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Plays a single sound once.  Stops and restarts it if already playing
     * so it never overlaps itself.
     */
    public static void play(String key) {
        if (sounds.containsKey(key)) {
            GreenfootSound s = sounds.get(key);
            if (s.isPlaying()) s.stop();
            s.play();
        }
    }

    /** Plays a random clip from the named voice pool. */
    public static void playPool(String poolKey) {
        if (voicePools.containsKey(poolKey)) {
            List<GreenfootSound> pool = voicePools.get(poolKey);
            pool.get(Greenfoot.getRandomNumber(pool.size())).play();
        }
    }

    /** Starts a sound looping if it is not already playing. */
    public static void playLoop(String key) {
        if (sounds.containsKey(key)) {
            GreenfootSound s = sounds.get(key);
            if (!s.isPlaying()) s.playLoop();
        }
    }

    /** Stops a sound entirely. */
    public static void stop(String key) {
        if (sounds.containsKey(key)) sounds.get(key).stop();
    }

    /** Stops every sound (single and pooled). */
    public static void stopAll() {
        for (GreenfootSound s : sounds.values()) s.stop();
        for (List<GreenfootSound> pool : voicePools.values()) {
            for (GreenfootSound s : pool) s.stop();
        }
    }

    /** Pauses a specific looping sound (position is preserved for resume). */
    public static void pause(String key) {
        if (sounds.containsKey(key)) sounds.get(key).pause();
    }

    /** Resumes (or starts) a looping sound. */
    public static void resume(String key) {
        if (sounds.containsKey(key)) sounds.get(key).playLoop();
    }

    /**
     * Pauses or resumes audio for the rewind system.
     *
     * pause=true  : saves and pauses the BGM; stops all other sounds.
     * pause=false : resumes only the sounds that were playing before pause.
     *
     * ⚠ NOTE: "dio_bgm" is hardcoded here.  If using a non-Dio character,
     * this should be GameConfig.ACTIVE_CHARACTER.bgmKey instead.
     */
    public static void setAllSoundsPaused(boolean pause) {
        if (pause) {
            activeBeforePause.clear();

            // Pause the BGM and remember it for resume
            if (sounds.get("dio_bgm").isPlaying()) {
                activeBeforePause.add(sounds.get("dio_bgm"));
                sounds.get("dio_bgm").pause();
            }

            // Stop (do not pause) all other sounds so they don't echo during rewind
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
        } else {
            // Resume only what was playing before the pause
            for (GreenfootSound s : activeBeforePause) s.play();
            activeBeforePause.clear();
        }
    }
}
