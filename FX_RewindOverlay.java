/*
 * ─────────────────────────────────────────────────────────────────────────────
 * FX_RewindOverlay.java  —  BLUE SCANLINE EFFECT DURING TIME REWIND
 * ─────────────────────────────────────────────────────────────────────────────
 * Role:
 *   A full-screen visual overlay placed over the world during a rewind.
 *   Two slightly different scanline images are pre-drawn statically and
 *   swapped every 4 frames to create a CRT-style flicker effect.
 *
 * Performance optimisation — static pre-drawing:
 *   screen1 and screen2 are static, meaning they are shared across ALL instances
 *   and kept in memory until the project closes.
 *   They are only drawn once (either in preLoad() or the first constructor call).
 *   This means no image creation happens when you press R — no lag spike.
 *
 * preLoad():
 *   Called from PlayingState.enter() to ensure the images are in memory
 *   BEFORE the player first presses R.  Without this, the first rewind
 *   would stutter while the images are created.
 *
 * This actor does NOT implement Time_Snapshottable.
 *   It is added and removed manually by PlayingState around the rewind session.
 *
 * Interacts with:
 *   PlayingState (adds on rewind start, removes on rewind end),
 *   GameConfig (world dimensions for image size)
 * ─────────────────────────────────────────────────────────────────────────────
 */
import greenfoot.*;
public class FX_RewindOverlay extends Actor {

    private int frame = 0;

    /**
     * Two pre-drawn overlay images, offset by 2 pixels vertically.
     * Static so they persist across all instances and are created only once.
     */
    private static GreenfootImage screen1;
    private static GreenfootImage screen2;

    public FX_RewindOverlay() {
        // Only draw if not already drawn (preLoad may have already done this)
        if (screen1 == null || screen2 == null) {
            screen1 = createOverlay(false);
            screen2 = createOverlay(true);
        }
        setImage(screen1);
    }

    @Override
    public void act() {
        frame++;
        // Alternate between the two scanline offsets every 4 frames for CRT flicker
        setImage(frame % 4 < 2 ? screen1 : screen2);
    }

    /**
     * Draws one version of the overlay image.
     * A semi-transparent blue tint with dark horizontal scanlines.
     *
     * @param scanlineOffset  If true, scanlines start 2px lower for the alternate frame.
     */
    
    private static GreenfootImage createOverlay(boolean scanlineOffset) {
        int w = GameConfig.WORLD_WIDTH;
        int h = GameConfig.WORLD_HEIGHT;
        GreenfootImage img = new GreenfootImage(w, h);
 
        // Semi-transparent blue tint over the whole screen
        img.setColor(new Color(60, 100, 200, 60));
        img.fill();
 
        // Dark horizontal scanlines evenly spaced across the screen
        img.setColor(new Color(0, 0, 0, 50));
        int startY = scanlineOffset ? GameConfig.s(2) : 0; // 2px offset creates the flicker
        for (int y = startY; y < h; y += GameConfig.s(4)) {
            img.fillRect(0, y, w, GameConfig.s(2));
        }
        return img;
    }
 
    /**
     * Pre-draws both overlay images into static memory.
     * Call this from PlayingState.enter() so the images exist before
     * the first rewind — prevents a lag spike on first R press.
     */
    public static void preLoad() {
        if (screen1 == null) {
            screen1 = createOverlay(false);
            screen2 = createOverlay(true);
        }
    }
}
