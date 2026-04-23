import greenfoot.*;

/*
 * ─────────────────────────────────────────────────────────────────────────────
 * FX_KingCrimsonOverlay.java  —  THE CRIMSON ERASURE SCREEN
 * ─────────────────────────────────────────────────────────────────────────────
 * A full-screen actor placed over the world during King Crimson's time erasure.
 *
 * Visual behaviour:
 *   While Ability_KingCrimson.ERASING == true, the screen is washed in deep
 *   crimson.  The wash starts subtle (alpha ≈ 70) so the player can still read
 *   obstacle positions, then ramps dramatically to 220 in the final 0.4 seconds
 *   — signalling "the window is closing, move NOW."
 *
 *   Two pre-drawn frames alternate every 3 ticks, creating a slight flicker
 *   that differentiates the effect from Mandom's blue scanlines.
 *
 * Self-removal:
 *   When ERASING flips false (erasure ends), this actor removes itself from the
 *   world.  The ability does not need to track or clean up the overlay.
 *
 * Performance:
 *   Both frames are drawn once into static fields and reused for the lifetime
 *   of the game — no per-frame image allocation.
 *   Transparency is applied via setTransparency() on the actor's image copy.
 *
 * Interacts with:
 *   Ability_KingCrimson (reads ERASING and erasurePercent),
 *   PlayingState (added to world by Ability_KingCrimson.startErasure()),
 *   MyWorld (paint order: above obstacles, below UI)
 * ─────────────────────────────────────────────────────────────────────────────
 */
public class FX_KingCrimsonOverlay extends Actor {

    // ─────────────────────────────────────────────────────────────────────────
    // PRE-DRAWN FRAMES (static — created once, shared across all instances)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Two crimson overlays with slightly different vertical grain patterns.
     * Alternating between them every few frames creates the "static" feel.
     */
    private static GreenfootImage frame1;
    private static GreenfootImage frame2;

    // ─────────────────────────────────────────────────────────────────────────
    // INSTANCE STATE
    // ─────────────────────────────────────────────────────────────────────────

    /** Local mutable copy of the currently displayed frame (so we can call setTransparency). */
    private GreenfootImage displayImage;

    /** Counts up each act() — used to alternate frames. */
    private int frameTick = 0;

    // ─────────────────────────────────────────────────────────────────────────
    // CONSTRUCTOR
    // ─────────────────────────────────────────────────────────────────────────

    public FX_KingCrimsonOverlay() {
        if (frame1 == null || frame2 == null) {
            frame1 = buildOverlay(false);
            frame2 = buildOverlay(true);
        }
        // Start with a fresh mutable copy so setTransparency doesn't mutate the shared static
        displayImage = new GreenfootImage(frame1);
        setImage(displayImage);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ACT — update transparency and handle self-removal
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void act() {
        // Self-remove the moment erasure ends
        if (!Ability_KingCrimson.ERASING) {
            if (getWorld() != null) getWorld().removeObject(this);
            return;
        }

        frameTick++;

        // Alternate between the two grain variants every 3 frames
        GreenfootImage base = (frameTick % 6 < 3) ? frame1 : frame2;

        // Reuse the display image — copy current base frame in
        displayImage = new GreenfootImage(base);

        // ── Alpha ramp ────────────────────────────────────────────────────────
        // erasurePercent: 0.0 = window just opened, 1.0 = window about to close.
        //
        // Phase 1 (0.0 – 0.6): hold at a low 70 alpha — world is readable.
        // Phase 2 (0.6 – 1.0): ramp from 70 → 210 — urgent red wall closing in.
        double pct = Ability_KingCrimson.erasurePercent;
        int alpha;
        if (pct < 0.6) {
            alpha = 70;
        } else {
            double t = (pct - 0.6) / 0.4;      // 0 → 1 over the final 40%
            alpha = 70 + (int)(t * t * 140);    // quadratic ramp: eases in then accelerates
        }
        displayImage.setTransparency(Math.min(255, alpha));

        setImage(displayImage);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PRELOAD  (call from PlayingState.enter() to avoid first-use lag)
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Pre-draws both overlay frames into static memory.
     * Call this from PlayingState.enter() so images exist before the first Q press.
     */
    public static void preLoad() {
        if (frame1 == null) {
            frame1 = buildOverlay(false);
            frame2 = buildOverlay(true);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // IMAGE BUILDER
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Draws one crimson overlay frame.
     *
     * Layers (bottom → top):
     *   1. Deep red tint fill
     *   2. Thin vertical grain lines, spaced 6px apart
     *      (if offset=true, lines start 3px right — creates the flicker)
     *
     * Alpha is intentionally low here (50); it is overridden per-frame by
     * setTransparency() in act().  The actual opacity is controlled dynamically.
     *
     * @param offset  Whether to shift grain lines by half a period.
     */
    private static GreenfootImage buildOverlay(boolean offset) {
        int w = GameConfig.WORLD_WIDTH;
        int h = GameConfig.WORLD_HEIGHT;
        GreenfootImage img = new GreenfootImage(w, h);

        // Base crimson tint — deep blood red
        img.setColor(new Color(160, 5, 20, 50));
        img.fill();

        // Vertical grain lines — evenly spaced, slight offset variant
        img.setColor(new Color(220, 0, 0, 30));
        int grain = GameConfig.s(6);   // gap between lines
        int lineW = GameConfig.s(1);   // line width
        int startX = offset ? (grain / 2) : 0;

        for (int x = startX; x < w; x += grain) {
            img.fillRect(x, 0, lineW, h);
        }

        return img;
    }
}
