/*
 * ─────────────────────────────────────────────────────────────────────────────
 * UI_RewindBar.java  —  THE "TIME" REWIND METER IN THE TOP-RIGHT CORNER
 * ─────────────────────────────────────────────────────────────────────────────
 * Role:
 *   Displays how much rewind history has been accumulated as a horizontal
 *   fill bar.  Redrawn every frame to reflect the current history size.
 *
 * Visual elements:
 *   - "TIME" label above the bar.
 *   - Dark background track.
 *   - Coloured fill bar (width proportional to history / MAX_HISTORY):
 *       Gray   → not enough history to rewind yet
 *       Purple → enough history ready (canRewind() would return true)
 *       Bright blue → currently rewinding
 *   - White tick mark at the REWIND_COST_FRAMES/MAX_HISTORY position,
 *     showing exactly where the minimum-to-rewind threshold is.
 *
 * All dimensions go through GameConfig.s() for correct scaling.
 *
 * Interacts with:
 *   Time_RewindManager (reads history size and state),
 *   PlayingState (creates this and places it top-right),
 *   GameConfig (REWIND_COST_FRAMES, MAX_HISTORY constants)
 * ─────────────────────────────────────────────────────────────────────────────
 */
import greenfoot.*;

public class UI_RewindBar extends Actor {

    private Time_RewindManager rewindManager;

    /** Design width and height of the bar (before scaling). */
    private static final int BAR_W = 150;
    private static final int BAR_H = 12;

    /**
     * @param rewindManager  The manager to read fill level and state from.
     */
    public UI_RewindBar(Time_RewindManager rewindManager) {
        this.rewindManager = rewindManager;
        redraw();
    }

    @Override
    public void act() {
        redraw();
    }

    /**
     * Redraws the entire bar image each frame.
     * Reads the current history size and rewind state from the manager.
     */
    private void redraw() {
        int w       = GameConfig.s(BAR_W);
        int h       = GameConfig.s(BAR_H);
        int padding = GameConfig.s(2);

        // Canvas tall enough for the "TIME" label above the bar
        GreenfootImage img = new GreenfootImage(w + padding * 2, h + GameConfig.s(18));

        // ── "TIME" label ──────────────────────────────────────────────────────
        GreenfootImage label = new GreenfootImage("TIME", GameConfig.s(10),
            Color.WHITE, new Color(0, 0, 0, 0));
        img.drawImage(label, 0, 0);

        int barTop = GameConfig.s(14); // Y position where the bar starts

        // ── Background track ──────────────────────────────────────────────────
        img.setColor(new Color(0, 0, 0, 160));
        img.fillRect(0, barTop, w + padding * 2, h + padding * 2);

        // ── Fill colour based on state ─────────────────────────────────────────
        double fill        = (double) rewindManager.getHistorySize() / Time_RewindManager.MAX_HISTORY;
        boolean canRewind  = rewindManager.canRewind();
        boolean isRewinding = rewindManager.isRewinding();

        Color fillColor;
        if      (isRewinding) fillColor = new Color(100, 200, 255);  // bright blue — rewinding
        else if (canRewind)   fillColor = new Color(160, 100, 255);  // purple — ready
        else                  fillColor = new Color(80,  80,  80);   // grey — not enough yet

        img.setColor(fillColor);
        img.fillRect(padding, barTop + padding, (int)(fill * w), h);

        // ── Minimum-cost tick mark ────────────────────────────────────────────
        // Shows exactly where the REWIND_COST_FRAMES threshold sits on the bar.
        // When the fill reaches this line, the player can rewind.
        double costFrac = (double) Time_RewindManager.REWIND_COST_FRAMES / Time_RewindManager.MAX_HISTORY;
        int    markerX  = (int)(costFrac * w) + padding;
        img.setColor(new Color(255, 255, 255, 180));
        img.fillRect(markerX, barTop, GameConfig.s(2), h + padding * 2);

        setImage(img);
    }
}
