/*
 * ─────────────────────────────────────────────────────────────────────────────
 * BossConfig.java  —  DATA FOR THE BOSS-INTRO BANNER ANIMATION
 * ─────────────────────────────────────────────────────────────────────────────
 * Role:
 *   An enum that defines the visual and audio data for each character's
 *   boss-intro banner (the cinematic slide-in that plays when Dio enters
 *   time-stop mode).
 *
 *   Each constant carries:
 *     - bgColor     : the background colour of the banner strip.
 *     - soundsKey   : the AudioManager pool key for the battle-cry voice line.
 *     - overlays[]  : sprite images positioned relative to the banner centre.
 *
 * How Banner.java uses this:
 *   Banner reads bossConfig.bgColor to fill its background.
 *   It reads bossConfig.soundsKey and plays a random sound from that pool.
 *   It reads bossConfig.overlays and draws each image at its offset position.
 *
 * Interacts with:
 *   Banner (reads the data), CharacterConfig (stores a reference per character)
 * ─────────────────────────────────────────────────────────────────────────────
 */
import greenfoot.*;

public enum BossConfig {

    // =========================================================================
    // BOSS: DIO
    // =========================================================================
    DIO(
        Color.BLACK,          // bgColor — classic black banner
        "dioBattleCry",       // soundsKey — random voice from the "dioBattleCry" pool
        new Banner.SpriteOverlay[] {
            // Each SpriteOverlay: (filename, width, height, xOffset, yOffset)
            // All values go through GameConfig.s() inside SpriteOverlay's constructor.
            new Banner.SpriteOverlay("dio_full.png",  150, 150, -200, 0), // full-body sprite, left of centre
            new Banner.SpriteOverlay("dio_label.png", 300, 100,    0, 0)  // name label, centred
        }
    );

    // =========================================================================
    // FIELDS
    // =========================================================================
    /** The solid background colour of the banner strip. */
    public final Color bgColor;

    /** AudioManager pool key — a random voice from this pool plays on entry. */
    public final String soundsKey;

    /** Sprite images drawn on top of the banner, each at a specific offset. */
    public final Banner.SpriteOverlay[] overlays;

    // =========================================================================
    // CONSTRUCTOR
    // =========================================================================
    private BossConfig(Color color, String soundsKey, Banner.SpriteOverlay[] overlays) {
        this.bgColor   = color;
        this.soundsKey = soundsKey;
        this.overlays  = overlays;
    }
}
