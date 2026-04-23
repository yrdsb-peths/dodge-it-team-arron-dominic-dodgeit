/*
 * ─────────────────────────────────────────────────────────────────────────────
 * FX_Afterimage.java  —  MOTION-BLUR GHOST TRAIL (MADE IN HEAVEN)
 * ─────────────────────────────────────────────────────────────────────────────
 * Role:
 *   Spawned by Ability_MadeInHeaven every frame while active.
 *   Creates an inverted (colour-negative) copy of the player's sprite at the
 *   player's current position.  Fades out over ~20 frames.
 *
 * The negative effect:
 *   Each pixel's RGB values are inverted: (255-R, 255-G, 255-B).
 *   This gives the ghost a ghostly, "wrong colour" appearance distinct from
 *   the actual player, making motion very visually clear.
 *
 * Fading:
 *   Alpha starts at 150 (semi-transparent) and decreases by 7 each frame.
 *   After ~21 frames the actor removes itself.
 *
 * This actor does NOT implement Time_Snapshottable.
 *   Afterimages are purely cosmetic and are transient enough that the rewind
 *   system ignores them.  They will simply stop appearing during rewind.
 *
 * Interacts with:
 *   Ability_MadeInHeaven (spawns these), GenericPlayer (provides the image)
 * ─────────────────────────────────────────────────────────────────────────────
 */
import greenfoot.*;

public class FX_Afterimage extends Actor {

    /** Starting transparency (0=invisible, 255=opaque). Fades down to 0. */
    private int alpha = 150;

    /**
     * Creates an afterimage from the player's current sprite.
     * Pixel-by-pixel colour inversion is applied to distinguish it from the live player.
     *
     * @param playerImg  The player's current GreenfootImage.  A copy is made
     *                   so modifications do not affect the original.
     */
    public FX_Afterimage(GreenfootImage playerImg) {
        GreenfootImage img = new GreenfootImage(playerImg); // make a copy

        // Invert every non-transparent pixel
        for (int x = 0; x < img.getWidth(); x++) {
            for (int y = 0; y < img.getHeight(); y++) {
                Color c = img.getColorAt(x, y);
                if (c.getAlpha() > 0) { // skip fully transparent pixels
                    img.setColorAt(x, y, new Color(
                        255 - c.getRed(),
                        255 - c.getGreen(),
                        255 - c.getBlue(),
                        c.getAlpha()
                    ));
                }
            }
        }
        setImage(img);
    }

    @Override
    public void act() {
        alpha -= 7; // fade out
        if (alpha <= 0) {
            getWorld().removeObject(this);
        } else {
            getImage().setTransparency(alpha);
        }
    }
}
