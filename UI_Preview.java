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
    private GreenfootImage portrait;
    
    private double exactX;
    private int targetX;
    private boolean isRemoving = false;
    private int startX;

    public UI_Preview(CharacterConfig config, boolean fromRight, int targetX) {
        this.targetX = targetX;
        // Start one full screen-width off screen
        this.startX = fromRight ? targetX + GameConfig.WORLD_WIDTH : targetX - GameConfig.WORLD_WIDTH;
        this.exactX = startX;

        try {
            // 1. Try to load the big static Intro Portrait
            if (config.portraitImage == null || config.portraitImage.isEmpty()) throw new Exception();
            this.portrait = new GreenfootImage(config.portraitImage);
            
            // Scale the portrait to fit nicely in the top half of the screen
            int h = GameConfig.s(220);
            int w = (int)((double)portrait.getWidth() / portrait.getHeight() * h);
            this.portrait.scale(w, h);
            setImage(this.portrait);
            
        } catch (Exception e) {
            // 2. Fallback to a scaled-up animated sprite if portrait is missing
            this.anim = new Animator(config.folderName, config.defaultAnim, config.scale * 2.5);
            if(anim.hasFrames()) setImage(anim.getCurrentFrame());
        }
    }

    public int getStartX() { return startX; }

    /** Tells the preview to animate off the screen and delete itself. */
    public void slideOut(boolean toLeft) {
        this.targetX = toLeft ? -GameConfig.s(300) : GameConfig.WORLD_WIDTH + GameConfig.s(300);
        this.isRemoving = true;
    }

    @Override
    public void act() {
        // Continue fallback animation if running
        if (anim != null && portrait == null) {
            setImage(anim.getCurrentFrame());
        }
        
        // Smooth Slide interpolation
        if (Math.abs(targetX - exactX) > 1) {
            exactX += (targetX - exactX) * 0.2; // Ease-out speed
            setLocation((int)exactX, getY());
        } else {
            // Snap to target and delete if sliding out
            setLocation(targetX, getY());
            if (isRemoving && getWorld() != null) {
                getWorld().removeObject(this);
            }
        }
    }
}