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

    // =========================================================================
    // CHARACTER: DIO BRANDO
    // =========================================================================
    DIO(
        "Dio Brando",                               // displayName  (shown in UI)
        "Dio",                                      // folderName   (images/Dio/)
        new String[]{"Idle", "Wry", "Dash", "Lose"}, // animNames  (sub-folders)
        "Dash",                                     // defaultAnim  (starts here)
        GameConfig.DIO_MOVE_SPEED,                  // moveSpeed    (pixels/frame)
        0.8 * GameConfig.SCALE,                     // scale        (sprite size)
        "dio_bgm",                                  // bgmKey       (AudioManager)
        "dioLostVoices",                            // deathSoundKey (voice pool)
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
    HeroMaybe(
        "Someguy",
        "aguy",
        new String[]{"Idle", "Wry", "Dash", "Lose"},
        "Idle",
        GameConfig.AGUY_MOVE_SPEED,
        0.4 * GameConfig.SCALE,
        "gothicbgm",
        "dioLostVoices",
        BossConfig.DIO,
        new String[]{"Ability_StandPunch", "Ability_StickyFingers"} // fewer abilities than DIO
    );

    // =========================================================================
    // FIELDS  (every character carries these values)
    // =========================================================================
    /** The name displayed on the character-select screen. */
    public final String displayName;

    /** Name of the sprite sub-folder under images/ (e.g., "Dio" → images/Dio/). */
    public final String folderName;

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
            String displayName, String folderName, String[] animNames,
            String defaultAnim, int moveSpeed, double scale,
            String bgmKey, String deathSoundKey,
            BossConfig bossConfig, String[] abilities) {
        this.displayName      = displayName;
        this.folderName       = folderName;
        this.animNames        = animNames;
        this.defaultAnim      = defaultAnim;
        this.moveSpeed        = moveSpeed;
        this.scale            = scale;
        this.bgmKey           = bgmKey;
        this.deathSoundKey    = deathSoundKey;
        this.bossConfig       = bossConfig;
        this.abilityClassNames = abilities;
    }
}
