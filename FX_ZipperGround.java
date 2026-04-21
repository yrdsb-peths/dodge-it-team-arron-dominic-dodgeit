/*
 * ─────────────────────────────────────────────────────────────────────────────
 * FX_ZipperGround.java  —  THE ZIPPER ON THE ROAD (STICKY FINGERS HIDING)
 * ─────────────────────────────────────────────────────────────────────────────
 * Role:
 *   Placed at the player's feet by Ability_StickyFingers when the player
 *   hides underground.  Follows the player's position every frame.
 *   Pulses in brightness to look like an active zipper keeping the road sealed.
 *
 * Drawn procedurally (no sprite file):
 *   A pulsing purple spine (horizontal line) with triangular teeth above and
 *   below, plus a darker pull-tab rectangle at the right end.
 *   Brightness oscillates using Math.sin(frame * 0.1) for a smooth pulse.
 *
 * Visibility / act() guard:
 *   act() checks isState(PlayingState.class) and returns early otherwise.
 *   Position is managed by Ability_StickyFingers.update(), not here.
 *
 * Does NOT implement Time_Snapshottable — its reference is managed by
 * Ability_StickyFingers which handles rewind re-linking (see that class).
 *
 * Interacts with:
 *   Ability_StickyFingers (creates, moves, and removes this actor)
 * ─────────────────────────────────────────────────────────────────────────────
 */
import greenfoot.*;

public class FX_ZipperGround extends Actor {

    private int frame = 0;

    /** Design width of the zipper graphic (scaled by GameConfig.s). */
    private static final int W = 70;
    /** Design height of the zipper graphic (scaled by GameConfig.s). */
    private static final int H = 12;

    public FX_ZipperGround() {
        redraw(0);
    }

    @Override
    public void act() {
        MyWorld world = (MyWorld) getWorld();
        if (world == null || !world.getGSM().isState(IActiveGameState.class)) return;
        frame++;
        redraw(frame);
    }

    /**
     * Redraws the zipper graphic for the current frame.
     * Brightness pulses smoothly between 180 and 255 using a sine wave.
     *
     * @param f  Current frame counter — drives the sine-wave brightness pulse.
     */
    private void redraw(int f) {
        int w = GameConfig.s(W);
        int h = GameConfig.s(H);
        GreenfootImage img = new GreenfootImage(w, h);

        // Sine-wave pulse: brightness oscillates between 180 (dim) and 255 (bright)
        int brightness = 180 + (int)(75 * (0.5 + 0.5 * Math.sin(f * 0.1)));

        // ── Zipper spine and teeth (pulsing purple) ───────────────────────────
        img.setColor(new Color(brightness, 0, brightness));

        // Horizontal spine through the middle
        img.fillRect(0, h / 2 - GameConfig.s(1), w, GameConfig.s(2));

        // Triangular teeth above and below the spine
        int toothW = GameConfig.s(6);
        int toothH = GameConfig.s(4);
        for (int x = 0; x + toothW <= w; x += toothW * 2) {
            int[] xpts = {x, x + toothW / 2, x + toothW};
            // Upper teeth (pointing upward)
            int[] yUp   = {h / 2, h / 2 - toothH, h / 2};
            img.fillPolygon(xpts, yUp, 3);
            // Lower teeth (pointing downward)
            int[] yDown = {h / 2, h / 2 + toothH, h / 2};
            img.fillPolygon(xpts, yDown, 3);
        }

        // ── Pull-tab (darker purple rectangle at the right end) ───────────────
        img.setColor(new Color(120, 0, 150));
        int pullW = GameConfig.s(6);
        int pullH = GameConfig.s(8);
        img.fillRect(w - pullW, h / 2 - pullH / 2, pullW, pullH);

        setImage(img);
    }
}
