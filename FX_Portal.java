/*
 * ─────────────────────────────────────────────────────────────────────────────
 * FX_Portal.java  —  ZIPPER FLASH AT THE SCREEN EDGE (STICKY FINGERS)
 * ─────────────────────────────────────────────────────────────────────────────
 * Role:
 *   A brief visual actor placed at the top or bottom screen edge when the
 *   player teleports through a Sticky Fingers portal.  Two instances appear
 *   simultaneously — one at each edge — and both fade out over ~18 frames.
 *
 * Drawn procedurally (no sprite file):
 *   A purple/gold strip with triangular zipper teeth pointing inward.
 *   teethFaceUp=true  → teeth point upward    (bottom edge portal)
 *   teethFaceUp=false → teeth point downward  (top edge portal)
 *
 * Lifetime:
 *   alpha starts at 255 and decreases by 14 each frame (~18 frames total).
 *   The actor removes itself when alpha reaches 0.
 *
 * Does NOT implement Time_Snapshottable — purely cosmetic, ignored by rewind.
 *
 * Interacts with:
 *   Ability_StickyFingers (creates two instances on portal warp),
 *   GameConfig.s() (all dimensions scale with SCALE)
 * ─────────────────────────────────────────────────────────────────────────────
 */
import greenfoot.*;

public class FX_Portal extends Actor {

    private int     alpha = 255;
    private int     width;
    private boolean teethFaceUp;

    /**
     * @param worldWidth   Width of the game world (portal spans the full width).
     * @param teethFaceUp  True = teeth point up (bottom-edge portal);
     *                     False = teeth point down (top-edge portal).
     */
    public FX_Portal(int worldWidth, boolean teethFaceUp) {
        this.width       = worldWidth;
        this.teethFaceUp = teethFaceUp;
        redraw();
    }

    @Override
    public void act() {
        alpha -= 14; // fades out in approximately 18 frames
        if (alpha <= 0) {
            if (getWorld() != null) getWorld().removeObject(this);
            return;
        }
        redraw();
    }

    /**
     * Redraws the portal strip at the current alpha level.
     * A purple background with golden triangular zipper teeth along the inner edge.
     */
    private void redraw() {
        int h = GameConfig.s(18);
        GreenfootImage img = new GreenfootImage(width, h);

        // Purple background strip
        img.setColor(new Color(160, 60, 220, Math.min(alpha, 200)));
        img.fill();

        // Golden triangular teeth along the inner edge
        int toothW = GameConfig.s(8);
        int toothH = GameConfig.s(7);
        img.setColor(new Color(255, 220, 0, alpha));

        for (int x = 0; x + toothW <= width; x += toothW) {
            int mid = x + toothW / 2;
            int[] xpts = {x, mid, x + toothW};
            // teethFaceUp=true → triangle tip points toward top of image (y=0)
            // teethFaceUp=false→ triangle tip points toward bottom of image (y=h)
            int[] ypts = {
                teethFaceUp ? toothH : h - toothH,
                teethFaceUp ? 0      : h,
                teethFaceUp ? toothH : h - toothH
            };
            img.fillPolygon(xpts, ypts, 3);
        }

        img.setTransparency(alpha);
        setImage(img);
    }
}
