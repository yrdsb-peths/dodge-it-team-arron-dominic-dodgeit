/*
 * ─────────────────────────────────────────────────────────────────────────────
 * CharacterConfig.java  —  THE CHARACTER ROSTER (DATA-DRIVEN DESIGN)
 * ─────────────────────────────────────────────────────────────────────────────
 * Role:
 *   An enum that defines every playable character.  Each enum constant (DIO,
 *   Dio2, etc.) is a single object that holds all the data needed to build that
 *   character at runtime — sprites, speed, music, abilities, and more.
 *
 * Why an enum?
 *   Enums in Java are a fixed list of named constants, but unlike simple
 *   integers, each constant can carry rich data (via a constructor).
 *   CharacterConfig.values() returns all characters, which is how
 *   CharacterSelectState shows the roster without knowing each character by name.
 *
 * HOW TO ADD A NEW CHARACTER:
 *   1. Add a comma after the last entry (e.g., after Dio2's closing parenthesis).
 *   2. Write KIRA("Kira Yoshikage", "Kira", new String[]{"Walk", "Die"}, ...) etc.
 *   3. The new character immediately appears in the character-select screen.
 *      No other files need to change.
 *
 * Ability loading:
 *   Abilities are listed as class-name Strings (e.g., "Ability_StandPunch").
 *   GenericPlayer uses Java Reflection (Class.forName()) to instantiate them
 *   at runtime.  This means you add abilities by listing their class name here
 *   — you do NOT modify GenericPlayer.
 *
 * Interacts with:
 *   GenericPlayer (reads all fields), CharacterSelectState (iterates roster),
 *   PlayingState (reads ACTIVE_CHARACTER to spawn the right player),
 *   Animator (uses folderName + animNames to load sprites)
 * ─────────────────────────────────────────────────────────────────────────────
 */
import greenfoot.*;
import java.util.List;
import java.util.ArrayList;

public enum CharacterConfig {
    
    MoonKnight(
        "Moon Knight",
        "MoonKnight",
        "moon_knight_full.png",                             //Portrait Image (new, for character select)
        "white_road.png",                             //CUstomised road(new, for character select)
        new String[]{"Idle", "Dash", "Lose", "DarkSpell_01", "DarkSpell_02"},
        "Dash",
        GameConfig.MOON_KNIGHT_MOVE_SPEED,
        1.2 * GameConfig.SCALE,
        "gothic_bgm",
        "dioLostVoices",
        "dioBattleCry",                             //Select sound key (new, for character select)
        BossConfig.DIO,
        new String[]{"Ability_DarkSpell01", // V - destroys all obstacles
                    "Ability_DarkSpell02",   // C - roots/freezes all obstacles
                    "Ability_StickyFingers"
        } 
    ),
    // =========================================================================
    // CHARACTER: DIO BRANDO
    // =========================================================================
    Testing_DIO(
        "Omnipotent Dio",                               // displayName  (shown in UI)
        "Dio",                                      // folderName   (images/Dio/)
        "dio_full.jpg",                             //Portrait Image (new, for character select)
        "standard_road.png",                             //CUstomised road(new, for character select)
        new String[]{"Idle", "Wry", "Dash", "Lose"}, // animNames  (sub-folders)
        "Dash",                                     // defaultAnim  (starts here)
        GameConfig.DIO_MOVE_SPEED,                  // moveSpeed    (pixels/frame)
        0.8 * GameConfig.SCALE,                     // scale        (sprite size)
        "dio_bgm",                                  // bgmKey       (AudioManager)
        "dioLostVoices",                            // deathSoundKey (voice pool)
        "dioBattleCry",                             //Select sound key (new, for character select)
        BossConfig.DIO,                             // bossConfig   (intro banner)
        new String[]{                               // abilityClassNames (loaded via Reflection)
            "Ability_StandPunch",   // E — summon TheWorldStand
            "Ability_MadeInHeaven", // S — speed boost
            "Ability_Mandom",       // R — time rewind (delegates to PlayingState)
            "Ability_StickyFingers" // F — portal wrap + hide underground
        }
    ),

    // =========================================================================
    // CHARACTER: DIO2  (test/placeholder — duplicate of DIO with fewer abilities)
    // =========================================================================
    /*
     * Dio2 exists only to verify that CharacterSelectState works with multiple
     * entries.  It is NOT a distinct character.  If you want a real second
     * character, replace this with proper data.
     */
    DIO(
        "Dio Brando",
        "Dio",
        "dio_full.jpg",                             //Portrait Image (new, for character select)
        "punk_road.png",                             //CUstomised road(new, for character select)
        new String[]{"Idle", "Wry", "Dash", "Lose"},
        "Dash",
        GameConfig.DIO_MOVE_SPEED,
        0.8 * GameConfig.SCALE,
        "dio_bgm",
        "dioLostVoices",
        "dioBattleCry",                             //Select sound key (new, for character select)
        BossConfig.DIO,
        new String[]{"Ability_StandPunch", "Ability_MadeInHeaven"} // fewer abilities than DIO
    ),
    
    MoonKnight2(
        "Moon Knight",
        "MoonKnight",
        "moon_knight_full.png",                             //Portrait Image (new, for character select)
        "red_road.png",                             //CUstomised road(new, for character select)
        new String[]{"Idle", "Dash", "Lose", "DarkSpell_01", "DarkSpell_02"},
        "Dash",
        GameConfig.MOON_KNIGHT_MOVE_SPEED,
        1.2 * GameConfig.SCALE,
        "gothic_bgm",
        "dioLostVoices",
        "dioBattleCry",                             //Select sound key (new, for character select)
        BossConfig.DIO,
        new String[]{"Ability_DarkSpell01", // V - destroys all obstacles
                    "Ability_DarkSpell02"   // C - roots/freezes all obstacles
        } 
    ),
    DIAVOLO(
        "Diavolo",
        "Dio",           // Reusing Dio's folder for now until you have Diavolo sprites
        "diavolo_full.jpg", // Add this to your images folder later
        "standard_road.png", 
        new String[]{"Idle", "Dash", "Lose"},
        "Dash",
        GameConfig.DIO_MOVE_SPEED,
        0.8 * GameConfig.SCALE,
        "dio_bgm", 
        "dioLostVoices",
        "diavoloLines", 
        BossConfig.DIO, 
        new String[]{"Ability_KingCrimson"});

    // =========================================================================
    // FIELDS  (every character carries these values)
    // =========================================================================
    /** The name displayed on the character-select screen. */
    public final String displayName;

    /** Name of the sprite sub-folder under images/ (e.g., "Dio" → images/Dio/). */
    public final String folderName;
    
     /** Name of theportrait image sub-folder under images*/
    public final String portraitImage;  
    
    public final String roadImage;
    
    /** Names of the animation sub-folders to load (e.g., "Idle", "Dash"). */
    public final String[] animNames;

    /** Which animation plays when the character is first created. */
    public final String defaultAnim;

    /** How many pixels per frame the character moves when an arrow key is held. */
    public final int moveSpeed;

    /**
     * The scale factor applied to all sprite images for this character.
     * Combines character-specific sizing with the global SCALE multiplier.
     */
    public final double scale;

    /** AudioManager key for the character's background music. */
    public final String bgmKey;

    /** AudioManager pool key played when the character dies. */
    public final String deathSoundKey;
    
    /** AudioManager pool key played when the character is selected. */    
     public final String selectSoundKey;
    /**
     * The boss-intro banner configuration.  If null, no banner plays.
     * Dio uses BossConfig.DIO which defines the black banner with his image.
     */
    public final BossConfig bossConfig;

    /**
     * Class name strings for each ability, loaded at runtime via Reflection.
     * Order matters: abilities are displayed as icons in this order.
     */
    public final String[] abilityClassNames;

    // =========================================================================
    // CONSTRUCTOR
    // =========================================================================
    private CharacterConfig(
            String displayName, String folderName,String portraitImage , String roadImage,
            String[] animNames,
            String defaultAnim, int moveSpeed, double scale,
            String bgmKey, String deathSoundKey, String selectSoundKey,
            BossConfig bossConfig, String[] abilities) {
        this.displayName      = displayName;
        this.folderName       = folderName;
        this.portraitImage    = portraitImage;
        this.roadImage = roadImage;
        this.animNames        = animNames;
        this.defaultAnim      = defaultAnim;
        this.moveSpeed        = moveSpeed;
        this.scale            = scale;
        this.bgmKey           = bgmKey;
        this.deathSoundKey    = deathSoundKey;
        this.selectSoundKey   = selectSoundKey;
        this.bossConfig       = bossConfig;
        this.abilityClassNames = abilities;
    }
}
