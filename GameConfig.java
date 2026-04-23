/*
 * ─────────────────────────────────────────────────────────────────────────────
 * GameConfig.java  —  THE CENTRAL CONTROL PANEL FOR ALL GAME CONSTANTS
 * ─────────────────────────────────────────────────────────────────────────────
 * Role:
 *   A pure configuration class.  All fields are static constants (or one
 *   mutable static for the active character).  You never create a GameConfig
 *   object — you just read its values: GameConfig.ROADROLLER_SPEED.
 *
 * THE MOST IMPORTANT THING HERE: the s() method.
 *   Every pixel measurement in the game goes through s().
 *   s(100) at SCALE=1.5 returns 150.
 *   Changing SCALE changes the entire game's visual size proportionally.
 *   NEVER hardcode raw pixel values — always wrap them in s().
 *
 * Interacts with:
 *   Literally every other class in the project.
 * ─────────────────────────────────────────────────────────────────────────────
 */
import greenfoot.*;

public class GameConfig {
    
    //Section -1: Debug mode
    public static final boolean DEBUG_MODE = false; // Set to false to hide hitboxes
    
    // =========================================================================
    // SECTION 0 — GLOBAL SCALE
    // =========================================================================
    /*
     * SCALE controls the visual size of the entire game.
     * At SCALE=1.0 the "design size" is used directly.
     * At SCALE=1.5 (the current value) everything is 1.5× larger.
     * To make the game bigger or smaller, change this one number.
     */
    public static final float SCALE = 1.5f;

    // =========================================================================
    // SECTION 1 — WORLD DIMENSIONS
    // =========================================================================
    /** Width of the game world in pixels (after scaling). Design value: 600px. */
    public static final int WORLD_WIDTH  = s(600);

    /** Height of the game world in pixels (after scaling). Design value: 400px. */
    public static final int WORLD_HEIGHT = s(400);

    /**
     * Vertical distance between lane centre lines.
     * Used by SpawnManager and PathWarning to size the danger zone.
     */
    public static final int LANE_HEIGHT = s(80);

    // =========================================================================
    // SECTION 2 — PLAYER (DIO) SETTINGS
    // =========================================================================
    
    //CHARACTER 1: DIO
    /** How many pixels per frame Dio moves when an arrow key is held. */
    
    public static final int DIO_MOVE_SPEED = s(5);
    /**
     * The scale factor applied to Dio's sprite images.
     * 0.8 × SCALE means the sprite is drawn at 80% of its source size,
     * then further scaled by the global SCALE multiplier.
     */
    public static final double DIO_BASE_SCALE = 0.8 * SCALE;
    
    //CHARACTER 2: MOONKNIGHT
    
    public static final int MOON_KNIGHT_MOVE_SPEED = s(6);
    public static final int MOON_KNIGHT_HITBOX_OFFSET = s(8);
    
    //Set hitbox for all characaters
    public static final int PLAYER_HITBOX_RADIUS = s(35);
        
    public static final int PLAYER_RADIUS     = s(22); 
    public static final int ROADROLLER_RADIUS = s(22); 
    public static final int TRAIN_RADIUS      = s(40);


    // =========================================================================
    // SECTION 3 — OBSTACLE DIMENSIONS
    // =========================================================================
    /** Width (and height, since it's square) of a Roadroller sprite in pixels. */
    public static final int ROADROLLER_WIDTH = s(80);

    /**
     * Width of a Train (Ambulance) sprite in pixels.
     * Named "TRAIN" because the obstacle behaves like a train (charges down a lane),
     * even though the sprite is an ambulance.
     */
    public static final int TRAIN_WIDTH = s(130);

    // =========================================================================
    // SECTION 4 — DIFFICULTY / SPAWN SETTINGS
    // =========================================================================
    /** How many frames between difficulty increases. 200 frames ≈ 3.3 seconds. */
    public static final int LEVEL_UP_TIME = 200;

    /** Starting interval (frames) between Roadroller spawns. Lower = more frequent. */
    public static final int ROADROLLER_RATE     = 30;
    /** Minimum interval: Roadrollers will never spawn faster than this. */
    public static final int ROADROLLER_MIN_RATE = 18;
    /** Starting speed of a Roadroller in pixels/frame. */
    public static final int ROADROLLER_SPEED    = s(6);
    /** Maximum speed a Roadroller can reach after many difficulty increases. */
    public static final int ROADROLLER_MAX_SPEED = s(10);

    /** Starting interval (frames) between Train spawns. ~3.3 seconds. */
    public static final int TRAIN_RATE     = 200;
    /** Minimum interval: Trains will never spawn faster than this. */
    public static final int TRAIN_MIN_RATE = 50;
    /** Starting speed of a Train in pixels/frame. Trains are much faster than Roadrollers. */
    public static final int TRAIN_SPEED    = s(25);
    /** Maximum speed a Train can reach after many difficulty increases. */
    public static final int TRAIN_MAX_SPEED = s(40);

    // =========================================================================
    // SECTION 5 — TIME REWIND SETTINGS
    // =========================================================================
    /**
     * Maximum number of frames of history the rewind system stores.
     * 360 frames at 60fps = 6 seconds of rewindable history.
     */
    public static final int MAX_REWIND_TIME = 360;
    public static final int REWIND_SPEED = 3;
    public static final int REWIND_MAX_SPEED = 5;

    /**
     * How many frames of history each rewind use consumes.
     * 120 frames = 2 seconds rewound per press of R.
     */
    public static final int REWIND_TIME = 120;

    // =========================================================================
    // SECTION 6 — ABILITY SETTINGS
    // =========================================================================

    // ── Made in Heaven (speed boost) ──────────────────────────────────────────
    /**
     * Greenfoot tick speed during Made in Heaven's effect.
     * Lower tick speed = fewer frames per second = game feels slower
     * while Dio's per-frame movement stays the same = Dio moves faster relatively.
     */
    public static final int MIH_TICK_SPEED    = 48;
    public static final int MIH_TICK_SPEED_MAX = 44;
    /** Normal Greenfoot tick speed used outside of any speed ability. */
    public static final int NORMAL_TICK_SPEED = 50;
    /** How long (seconds) the MiH cooldown lasts after use. */
    public static final int MIH_COOLDOWN = 3;

    // ── Stand Punch (TheWorldStand) ───────────────────────────────────────────
    /** How long (seconds) the Stand remains active after summoning. */
    public static final double WORLD_PUNCH_DURATION = 3.5;
    /** How long (seconds) you must wait before summoning the Stand again. */
    public static final double WORLD_PUNCH_COOLDOWN = 3.0;

    // ── Sticky Fingers (portal + hide) ───────────────────────────────────────
    /** The keyboard key that activates Sticky Fingers (toggle hide). */
    public static final String STICKY_FINGER_BUTTON = "f";
    /**
     * How close to the top/bottom edge (in pixels) before the portal wrap fires.
     * At s(35), the teleport triggers 35 design-pixels from the edge.
     */
    public static final int PORTAL_MARGIN = s(35);
    /** Seconds of invincibility granted after teleporting through a portal. */
    public static final double PORTAL_IFRAME_DURATION = 1;
    /** Seconds before the portal can fire again after a warp. */
    public static final double PORTAL_COOLDOWN_DURATION = 4;
    //---KING CRIMSON (SKIPS TIME)
    public static final String KC_BUTTON = "Q";
    public static final double KC_DURATION = 2.0; // How long time is frozen
    public static final double KC_COOLDOWN = 1.0;
    

    //--— DARK SPELL SETTINGS

    // Spell 01 (V) - Dark Nuke
    public static final String DS01_BUTTON = "v";
    public static final double DS01_DURATION = 0.9;   // Seconds
    public static final double DS01_COOLDOWN = 5.0;   // Seconds
    public static final int    DS01_IMAGE_SIZE = 1400; // Pixels
    public static final int    DS01_RADIUS = 500;     // Pixels (The blast radius)

    // Spell 02 (C) - Lane Freeze
    public static final String DS02_BUTTON = "c";
    public static final double DS02_DURATION = 1.0;   // Seconds
    public static final double DS02_COOLDOWN = 1.0;   // Seconds

    // =========================================================================
    // SECTION 7 — AUDIO
    // =========================================================================
    /**
     * Master volume level, applied as a multiplier to all sounds.
     * Range is 0 (silent) to 200 (full).  Individual sounds have their own
     * base volumes in AudioManager; actual volume = (base × master) / 200.
     */

    public static final int MASTER_VOLUME = 100;

    
    // =========================================================================
    // SECTION 8 — KEY BINDINGS
    // =========================================================================
    /** Key to trigger time-stop (pushes PausedState). */
    public static final String TIME_STOP_BUTTON   = "w";
    /** Key to trigger time-rewind (handled via Ability_Mandom → PlayingState). */
    public static final String REWIND_TIME_BUTTON = "r";
    /** Key to activate Made in Heaven (speed boost). */
    public static final String MIH_BUTTON         = "s";
    /** Key to summon The World Stand (punch stand). */
    public static final String STAND_PUNCH_BUTTON = "e";

    // =========================================================================
    // SECTION 9 — ROAD / LANES
    // =========================================================================
    /** How fast the scrolling road background moves per frame. */
    public static final int ROAD_SCROLL_SPEED = s(5);
    /** Maximum scroll speed the road can reach after difficulty increases. */
    public static final int ROAD_MAX_SPEED    = s(5);
    /**
     * The Y-coordinates (centre of each lane) where obstacles can spawn.
     * Five lanes are evenly spread across the 400-design-pixel world height.
     * SpawnManager picks one of these at random for each spawn event.
     */
    public static final int[] LANES = { s(40), s(120), s(200), s(280), s(360) };

    // =========================================================================
    // SECTION 10 — UI FONT SIZES (legacy; prefer using s() inline)
    // =========================================================================
    /** A large font size for headlines. */
    public static final int FONT_SIZE_LARGE = s(80);
    /** A small font size for body text. */
    public static final int FONT_SIZE_SMALL = s(30);

    // =========================================================================
    // SECTION 11 — ACTIVE CHARACTER (mutable — set by CharacterSelectState)
    // =========================================================================
    /**
     * The character the player selected in the character-select screen.
     * Defaults to DIO.  CharacterSelectState overwrites this before
     * pushing PlayingState, and PlayingState reads it when spawning the player.
     */
    public static CharacterConfig ACTIVE_CHARACTER = CharacterConfig.DIO;
    
    
    // =========================================================================
    // SECTION 12 — DEMO SANDBOX SETTINGS
    // =========================================================================
    /** 
     * The Y-coordinate where the sandbox floor ends. 
     * Tweak this! s(280) makes the UI deck exactly 30% of the screen.
     */
    public static final int DEMO_BOTTOM_BOUND = s(290);

    // =========================================================================
    // THE SCALING HELPER — the most important method in this file
    // =========================================================================

    /**
     * Scales any pixel value by the global SCALE factor.
     * ALWAYS use this instead of hardcoding pixel numbers.
     *
     * Example: s(100) at SCALE=1.5 returns 150.
     *
     * @param pixels  The "design-size" pixel value (what it would be at SCALE=1.0).
     * @return        The scaled pixel value, rounded to the nearest integer.
     */
    public static int s(int pixels) {
        return Math.round(pixels * SCALE);
    }
    
    public static Color getPathWarningColor(String roadImage) {
        // If the road image name is red, use Cyan
        if (roadImage.equals("red_road.png")) {
            return new Color(0, 255, 255); // Cyan
        }
        
        // If the road is white, maybe a deep Purple looks better
        if (roadImage.equals("white_road.png")) {
            return new Color(128, 0, 128); 
        }
    
        // Default: The classic semi-transparent red
        return new Color(255, 0, 0);
    }
}
