/*
 * ─────────────────────────────────────────────────────────────────────────────
 * Banner.java  —  THE CINEMATIC BOSS-INTRO BANNER (TIME-STOP ENTRANCE)
 * ─────────────────────────────────────────────────────────────────────────────
 * Role:
 *   A self-animating actor that plays a three-phase cinematic slide-in when
 *   Dio uses time-stop.  Spawned by Dio.onPauseUpdate() during PausedState.
 *   Reads all its visual data (colour, sprites, sounds) from a BossConfig.
 *
 * Three animation phases (state machine within the banner):
 *
 *   STATE 0 — SLIDE IN:
 *     Eases in from the right toward x=GameConfig.s(400).
 *     Uses proportional interpolation: step = dist × 0.12  (ease-in feel).
 *     Plays a random battle-cry voice on the first frame.
 *
 *   STATE 1 — HOLD & SQUEEZE OUT:
 *     Accelerates rightward (speed increases each frame by 0.4 × SCALE).
 *     As the banner moves right of centre (x > s(200)), its height shrinks
 *     proportionally — creating a "squeeze" effect as it exits.
 *     Transitions to STATE 2 when speed reaches 0 (momentum reversal).
 *
 *   STATE 2 — SLIDE OUT:
 *     Continues accelerating right (by 1.0 × SCALE per frame).
 *     Height keeps shrinking and alpha fades out.
 *     Removes itself when off-screen (x > worldWidth + s(600)).
 *
 * render():
 *   Draws a large transparent canvas (s(1000) × s(400)), fills the coloured
 *   background strip, then overlays each BossConfig.SpriteOverlay at its
 *   scaled offset position.  The canvas size is oversized to allow the
 *   sprites (which extend beyond the strip) to be visible.
 *
 * SpriteOverlay (inner static class):
 *   Holds a pre-scaled GreenfootImage and an x/y offset from the canvas centre.
 *   Defined inside BossConfig and passed to Banner via BossConfig.overlays.
 *
 * All dimensions go through GameConfig.s() — the banner scales with SCALE.
 *
 * Interacts with:
 *   Dio.onPauseUpdate() (spawns this), BossConfig (reads visual data),
 *   AudioManager (plays battle cry), GameConfig.s() (scaling)
 * ─────────────────────────────────────────────────────────────────────────────
 */
import greenfoot.*;
import java.util.List;
import java.util.ArrayList;

public class Banner extends Actor {

    /** 0=sliding in, 1=holding/squeezing out, 2=sliding off-screen. */
    private int state = 0;

    /** General-purpose frame counter (currently unused in logic, kept for extension). */
    private int timer = 0;

    /** The base background image — filled with bgColor, drawn onto the canvas each frame. */
    private GreenfootImage baseImage;

    /** Current horizontal velocity.  Negative = moving left; positive = moving right. */
    private double speed = -12.0 * GameConfig.SCALE;

    /** Prevents the battle-cry sound from playing more than once. */
    private boolean playedSound = false;

    /** Configuration object — provides colour, sound key, and sprite overlays. */
    private BossConfig config;

    /** Sprite overlays drawn on top of the background strip each frame. */
    private List<SpriteOverlay> sprites = new ArrayList<>();

    // ─────────────────────────────────────────────────────────────────────────
    // INNER CLASS: SpriteOverlay
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * A pre-scaled image with a position offset from the banner canvas centre.
     * Defined here so BossConfig can reference it as Banner.SpriteOverlay.
     */
    public static class SpriteOverlay {
        GreenfootImage image;
        int offsetX;
        int offsetY;

        /**
         * Loads, scales, and stores one overlay sprite.
         * All dimensions go through GameConfig.s() here so BossConfig
         * can use raw design values.
         *
         * @param fileName  Image file name (in the images/ folder).
         * @param w         Design width in pixels.
         * @param h         Design height in pixels.
         * @param x         Horizontal offset from canvas centre (design pixels).
         * @param y         Vertical offset from canvas centre (design pixels).
         */
        public SpriteOverlay(String fileName, int w, int h, int x, int y) {
            this.image   = new GreenfootImage(fileName);
            this.image.scale(GameConfig.s(w), GameConfig.s(h));
            this.offsetX = GameConfig.s(x);
            this.offsetY = GameConfig.s(y);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CONSTRUCTOR
    // ─────────────────────────────────────────────────────────────────────────

    /**
     * Creates the Banner from the given BossConfig.
     * Pre-scales the background image and registers all sprite overlays.
     *
     * @param config  The BossConfig to read visual data from.
     */
    public Banner(BossConfig config) {
        this.config = config;
        baseImage = new GreenfootImage(GameConfig.s(810), GameConfig.s(150));
        baseImage.setColor(config.bgColor);
        baseImage.fill();

        for (SpriteOverlay s : config.overlays) {
            sprites.add(s);
        }

        render(150); // initial render at full height
    }

    // ─────────────────────────────────────────────────────────────────────────
    // ANIMATION LOOP
    // ─────────────────────────────────────────────────────────────────────────

    @Override
    public void act() {
        if      (state == 0) slideIn();
        else if (state == 1) hold();
        else if (state == 2) slideOut();
    }

    /**
     * STATE 0: Eases toward the target X using proportional interpolation.
     * Plays the battle-cry voice on the first frame.
     * Transitions to STATE 1 when close enough to the target.
     */
    private void slideIn() {
        if (!playedSound) {
            AudioManager.playPool(config.soundsKey);
            playedSound = true;
        }

        int targetX = GameConfig.s(400);
        int dist    = targetX - getX();

        if (Math.abs(dist) <= GameConfig.s(4)) {
            setLocation(targetX, getY());
            state = 1;
        } else {
            int step = (int)(dist * 0.12); // proportional easing
            if (step == 0) step = (dist > 0) ? 1 : -1; // minimum 1px per frame
            setLocation(getX() + step, getY());
        }
    }

    /**
     * STATE 1: Accelerates rightward while squeezing the banner height down.
     * The squeeze ratio is based on how far right of the centre the banner has moved.
     * Transitions to STATE 2 when speed crosses 0 (moving right).
     */
    private void hold() {
        speed += (0.4 * GameConfig.SCALE); // accelerate rightward
        setLocation(getX() + (int) speed, getY());

        // Squeeze height: as x moves from s(200) toward s(400), ratio goes 0→1
        int    startPoint = GameConfig.s(200);
        double ratio      = (getX() - startPoint) / (double) startPoint;
        int    newHeight  = GameConfig.s(30) + (int)(GameConfig.s(120) * ratio);
        render(newHeight);

        if (speed >= 0) {
            state = 2;
            speed = 0;
        }
    }

    /**
     * STATE 2: Accelerates off the right side of the screen with fading alpha.
     * Removes itself once fully off-screen.
     */
    private void slideOut() {
        speed += (1.0 * GameConfig.SCALE); // faster acceleration out
        setLocation(getX() + (int) speed, getY());

        int    startPoint = GameConfig.s(200);
        double ratio      = (getX() - startPoint) / (double) startPoint;
        int    newHeight  = GameConfig.s(30) + (int)(GameConfig.s(120) * ratio);
        int    alpha      = (int)(255 * (1.0 - ratio / 4.0));
        alpha = Math.max(0, Math.min(255, alpha));

        render(newHeight, alpha);

        if (getX() > getWorld().getWidth() + GameConfig.s(600)) {
            getWorld().removeObject(this);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // RENDERING
    // ─────────────────────────────────────────────────────────────────────────

    /** Renders at full opacity. */
    private void render(int currentHeight) {
        render(currentHeight, 255);
    }

    /**
     * Draws the banner onto an oversized transparent canvas so sprites that
     * extend beyond the coloured strip remain visible.
     *
     * @param currentHeight  The height of the coloured background strip this frame.
     * @param alpha          Opacity of the background strip (0–255).
     */
    private void render(int currentHeight, int alpha) {
        // Canvas is larger than the strip to accommodate sprite overhangs
        GreenfootImage canvas = new GreenfootImage(GameConfig.s(1000), GameConfig.s(400));
        canvas.setColor(new Color(0, 0, 0, 0));
        canvas.fill(); // fully transparent base

        Color c   = config.bgColor;
        int   bgH = Math.max(1, GameConfig.s(currentHeight));
        int   bgW = GameConfig.s(900);
        int   bgX = (canvas.getWidth()  - bgW) / 2;
        int   bgY = (canvas.getHeight() - bgH) / 2;

        // Draw the coloured background strip (centred on the canvas)
        canvas.setColor(new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha));
        canvas.fillRect(bgX, bgY, bgW, bgH);

        // Draw each sprite overlay centred on the canvas with its offset
        for (SpriteOverlay s : sprites) {
            int drawX = canvas.getWidth()  / 2 - s.image.getWidth()  / 2 + s.offsetX;
            int drawY = canvas.getHeight() / 2 - s.image.getHeight() / 2 + s.offsetY;
            canvas.drawImage(s.image, drawX, drawY);
        }

        setImage(canvas);
    }
}
