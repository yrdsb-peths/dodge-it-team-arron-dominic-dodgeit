/*
 * ─────────────────────────────────────────────────────────────────────────────
 * UIText.java  —  A SIMPLE TEXT LABEL ACTOR
 * ─────────────────────────────────────────────────────────────────────────────
 * Role:
 *   Renders a string of text as an Actor image with a transparent background.
 *   Used throughout all state UI (score display, menus, game-over screen).
 *
 * Optimisation:
 *   updateImage() only rebuilds the GreenfootImage if the text has actually
 *   changed.  This prevents recreating the image 60 times/sec for static labels.
 *
 * Scaling:
 *   Callers pass fontSize through GameConfig.s() so text scales with SCALE.
 *   e.g. new UIText("SCORE: 0", GameConfig.s(25), Color.WHITE)
 *
 * Interacts with:
 *   All state classes (MenuState, PlayingState, etc.),
 *   GameConfig.s() (font size scaling)
 * ─────────────────────────────────────────────────────────────────────────────
 */
import greenfoot.*;

public class UIText extends Actor {

    private String text;
    private int    fontSize;
    private Color  color;

    /**
     * Creates a text label.
     * @param text      The string to display.
     * @param fontSize  Font size in pixels — use GameConfig.s(size) for scaling.
     * @param color     Text colour.
     */
    public UIText(String text, int fontSize, Color color) {
        this.text     = text;
        this.fontSize = fontSize;
        this.color    = color;
        updateImage();
    }

    /**
     * Updates the displayed text.  Only rebuilds the image if the text changed
     * (avoids creating a new GreenfootImage every frame for the score label).
     *
     * @param newText  The new string to display.
     */
    public void setText(String newText) {
        if (!newText.equals(this.text)) {
            this.text = newText;
            updateImage();
        }
    }

    /** Rebuilds the GreenfootImage from the current text, font, and colour. */
    private void updateImage() {
        // Transparent background (alpha=0) so the road/actors behind are visible.
        GreenfootImage img = new GreenfootImage(text, fontSize, color, new Color(0, 0, 0, 0));
        setImage(img);
    }
}
