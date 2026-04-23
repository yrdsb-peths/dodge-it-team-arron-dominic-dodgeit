/*
 * ─────────────────────────────────────────────────────────────────────────────
 * Animator.java  —  FRAME-BASED SPRITE ANIMATION LOADER AND PLAYER
 * ─────────────────────────────────────────────────────────────────────────────
 * Role:
 *   Loads a folder of sequentially numbered PNG images into memory at
 *   construction time, then cycles through them on each call to getCurrentFrame().
 *
 * File naming convention expected:
 *   images/<baseFolder>/<folderName>/<folderName>_000.png
 *   images/<baseFolder>/<folderName>/<folderName>_001.png
 *   ...
 *   e.g. images/Dio/Dash/Dash_000.png, Dash_001.png, etc.
 *
 * Three constructors (most to least detailed):
 *   Full:   (baseFolder, folderName, prefix, frameCount, speed, scaleFactor)
 *   Medium: (baseFolder, folderName, speed, scaleFactor) — auto-counts frames
 *   Short:  (baseFolder, folderName, scaleFactor) — auto-counts frames, default speed
 *
 * Auto frame counting:
 *   countFrames() uses Java's File API to count .png files in the folder.
 *   Adding more frames to the folder automatically increases the animation length
 *   without any code change.
 *
 * Speed:
 *   'speed' is the number of game frames between image advances.
 *   FRAME_DELAY = 6 means 1 image change every 6 ticks ≈ 10 fps animation.
 *   Higher speed = slower animation.  Lower speed = faster (down to 1 = every frame).
 *
 * Interacts with:
 *   GenericPlayer (stores a HashMap of Animators, one per animation name),
 *   TheWorldStand (uses one Animator for the punch animation),
 *   UI_Preview (uses one Animator to show the character preview)
 * ─────────────────────────────────────────────────────────────────────────────
 */
import java.util.ArrayList;
import greenfoot.*;
import java.io.File;

public class Animator {

    /** Frames between image advances.  Higher = slower animation.  Default: 6 ticks/frame. */
    public static final int FRAME_DELAY = 6;

    /** The pre-loaded array of animation frames, scaled at load time. */
    private GreenfootImage[] frames;

    /** Index of the frame currently displayed. */
    private int currentFrame = 0;

    /** Internal counter: increments each tick until it reaches 'speed'. */
    private int timer = 0;

    /** Frames-per-image delay (higher = slower). Can be changed at runtime. */
    private int speed;
    
    private boolean loop = true; //LOOP 

    // ─────────────────────────────────────────────────────────────────────────
    // CONSTRUCTORS
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Full constructor — maximum control over every parameter.
     * Loads <frameCount> images from:
     *   images/<baseFolder>/<folderName>/<prefix>_000.png ... _NNN.png
     * and scales each to (originalWidth × scaleFactor, originalHeight × scaleFactor).
     *
     * @param baseFolder  Top-level folder (e.g., "Dio", "symbols").
     * @param folderName  Sub-folder and default prefix (e.g., "Dash", "exclaimation").
     * @param prefix      The filename prefix if different from folderName.
     * @param frameCount  How many frames to load.
     * @param speed       Ticks between frame advances (higher = slower).
     * @param scaleFactor Multiplier applied to each image at load time.
     */
    public Animator(String baseFolder, String folderName, String prefix,
                    int frameCount, int speed, double scaleFactor) {
        this.speed  = speed;
        frames = new GreenfootImage[frameCount];

        for (int i = 0; i < frameCount; i++) {
            String suffix   = String.format("%03d", i); // 3-digit zero-padded: 000, 001...
            String fileName = baseFolder + "/" + folderName + "/" + prefix + "_" + suffix + ".png";
            frames[i] = new GreenfootImage(fileName);
            int newWidth  = (int)(frames[i].getWidth()  * scaleFactor);
            int newHeight = (int)(frames[i].getHeight() * scaleFactor);
            frames[i].scale(newWidth, newHeight);
        }
    }

    /**
     * Short constructor — auto-counts frames in the folder, uses default speed.
     *
     * @param baseFolder  Top-level folder.
     * @param folderName  Sub-folder name (also used as the filename prefix).
     * @param scaleFactor Sprite size multiplier.
     */
    public Animator(String baseFolder, String folderName, double scaleFactor) {
        this(baseFolder, folderName, folderName,
             countFrames(baseFolder, folderName), FRAME_DELAY, scaleFactor);
    }

    /**
     * Medium constructor — auto-counts frames, custom speed.
     *
     * @param baseFolder  Top-level folder.
     * @param folderName  Sub-folder name.
     * @param speed       Ticks between frame advances.
     * @param scaleFactor Sprite size multiplier.
     */
    public Animator(String baseFolder, String folderName, int speed, double scaleFactor) {
        this(baseFolder, folderName, folderName,
             countFrames(baseFolder, folderName), speed, scaleFactor);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // FRAME COUNTING
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Counts the number of .png files in the given animation folder.
     * Greenfoot serves images from the "images/" sub-directory of the project.
     * Returns 0 if the folder does not exist or contains no png files.
     *
     * @param baseFolder  Top-level folder name.
     * @param folderName  Animation sub-folder name.
     * @return            Number of .png files found.
     */
    private static int countFrames(String baseFolder, String folderName) {
        File dir = new File("images/" + baseFolder + "/" + folderName);
        if (dir.exists() && dir.isDirectory()) {
            String[] files = dir.list((d, name) -> name.toLowerCase().endsWith(".png"));
            return (files != null) ? files.length : 0;
        }
        return 0;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PLAYBACK
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Returns the current animation frame and advances the internal timer.
     * When the timer reaches 'speed', moves to the next frame (looping).
     * Call this once per act() to display the correct frame.
     * Loop depending on whether is dying animation or not
     *
     * @return  The GreenfootImage for the current frame.
     */
       public GreenfootImage getCurrentFrame() {
            timer++;
            if (timer >= speed) {
                timer = 0;
                if (frames.length > 0) {
                    if (loop) {
                        // Standard looping logic
                        currentFrame = (currentFrame + 1) % frames.length;
                    } else {
                        // STOP at the last frame if loop is false
                        if (currentFrame < frames.length - 1) {
                            currentFrame++;
                        }
                    }
                }
            }
            return frames[currentFrame];
        }

    /**
     * Resets to frame 0 and clears the timer.
     * Call this when switching to this animation so it always starts fresh.
     */
    public void reset() {
        currentFrame = 0;
        timer        = 0;
    }

    /**
     * Changes the playback speed at runtime.
     * Useful for slow-motion or fast-forward animation effects.
     *
     * @param speed  New ticks-per-frame delay (higher = slower).
     */
    public void setSpeed(int speed) {
        this.speed = speed;
    }

    /** @return True if this animator has at least one frame loaded. */
    public boolean hasFrames() {
        return frames != null && frames.length > 0;
    }
    
    public void setLoop(boolean loop) {
        this.loop = loop;
    }
    
    public void scaleAllFrames(int w, int h) {
        for (int i = 0; i < frames.length; i++) {
            frames[i].scale(w, h);
        }
    }

}
