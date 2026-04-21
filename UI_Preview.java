/*
 * ─────────────────────────────────────────────────────────────────────────────
 * UI_Preview.java  —  ANIMATED CHARACTER PREVIEW IN THE SELECT SCREEN
 * ─────────────────────────────────────────────────────────────────────────────
 * Role:
 *   Displays an animated preview of a character in CharacterSelectState.
 *   Simply plays the character's default animation on a loop.
 *
 * Lifetime:
 *   Created by CharacterSelectState.updateScreen() when a new character
 *   is selected.  The previous UI_Preview is removed manually before
 *   the new one is added.  Removed in CharacterSelectState.exit().
 *
 * Interacts with:
 *   CharacterSelectState (creates and removes this),
 *   CharacterConfig (provides folderName, defaultAnim, scale),
 *   Animator (loads and plays the animation)
 * ─────────────────────────────────────────────────────────────────────────────
 */
import greenfoot.*;

public class UI_Preview extends Actor {

    private Animator anim;

    /**
     * Creates a preview for the given character, loading their default animation.
     * @param config  The character whose preview to display.
     */
    public UI_Preview(CharacterConfig config) {
        this.anim = new Animator(config.folderName, config.defaultAnim, config.scale);
    }

    @Override
    public void act() {
        setImage(anim.getCurrentFrame()); // advance and display the animation
    }
}
