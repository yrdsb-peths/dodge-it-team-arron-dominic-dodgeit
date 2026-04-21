/*
 * ─────────────────────────────────────────────────────────────────────────────
 * Ability_StickyFingers.java  —  PORTAL WRAP + HIDE IN ROAD  (F KEY)
 * ─────────────────────────────────────────────────────────────────────────────
 * Bruno Bucciarati's Stand — two independent mechanics on one keybind.
 *
 * ══════════════════════════════════════════════════════════════════════════════
 * MECHANIC 1 — PORTAL WRAP  (passive, fires automatically, no key needed)
 * ══════════════════════════════════════════════════════════════════════════════
 * When the player's Y position is within PORTAL_MARGIN pixels of the top or
 * bottom screen edge, they are teleported to the opposite edge.  A zipper
 * flash (FX_Portal) appears at both edges.  A brief invincibility window
 * (PORTAL_IFRAME_DURATION) and a cooldown (PORTAL_COOLDOWN_DURATION) prevent
 * instant re-triggering.
 *
 *   Top edge  → teleport to bottom   (fromTop = true  in doPortalWarp)
 *   Bottom edge → teleport to top    (fromTop = false in doPortalWarp)
 *
 * ══════════════════════════════════════════════════════════════════════════════
 * MECHANIC 2 — HIDE IN THE ROAD  (press F to toggle underground / emerge)
 * ══════════════════════════════════════════════════════════════════════════════
 * Press F to zip into the road:
 *   - hidden = true
 *   - Player sprite becomes fully transparent (animationLogic checks isHidden)
 *   - Player is invincible (die() checks shouldHidePlayer)
 *   - Score is NOT earned (Roadroller.checkRemove checks isHidden)
 *   - A FX_ZipperGround actor follows the player's position
 *
 * Press F again to emerge:
 *   - hidden = false
 *   - FX_ZipperGround is removed
 *   - 0.8 seconds of i-frames are granted
 *   - A 2-second hide cooldown begins
 *
 * ══════════════════════════════════════════════════════════════════════════════
 * DEBOUNCE — keyWasDown flag
 * ══════════════════════════════════════════════════════════════════════════════
 * Without debounce, activate() would be called 60 times/sec while F is held,
 * toggling hide on/off 60 times per second.
 * keyWasDown is set true on activate() and cleared in update() when the key
 * is released, ensuring only ONE toggle per physical key press.
 *
 * ══════════════════════════════════════════════════════════════════════════════
 * REWIND VISUAL BUG FIX — zipper re-link logic
 * ══════════════════════════════════════════════════════════════════════════════
 * After a rewind, the FX_ZipperGround actor reference stored here may point
 * to an object that was removed from the world (actors can be removed/re-added
 * by the rewind system).  update() detects this and either re-links to an
 * existing FX_ZipperGround in the world, or creates a new one if none exists.
 * This prevents the underground state from appearing without its visual.
 *
 * ══════════════════════════════════════════════════════════════════════════════
 * UI RINGS (UI_AbilityIcon)
 * ══════════════════════════════════════════════════════════════════════════════
 * Outer ring: hide cooldown (blue fills as cooldown progresses; shows orange=full while hidden)
 * Inner ring: portal warp cooldown (blue fills as portal cooldown progresses)
 *
 * Keybind: F  (GameConfig.STICKY_FINGER_BUTTON)
 * ─────────────────────────────────────────────────────────────────────────────
 */
import greenfoot.*;

public class Ability_StickyFingers implements Ability {

    // ─────────────────────────────────────────────────────────────────────────
    // STATE
    // ─────────────────────────────────────────────────────────────────────────

    /** True while the player is hidden underground. */
    private boolean hidden = false;

    /**
     * Debounce flag — set true when F is pressed; cleared when F is released.
     * Prevents activate() from firing more than once per physical key press.
     */
    private boolean keyWasDown = false;

    // ─────────────────────────────────────────────────────────────────────────
    // TIMERS
    // ─────────────────────────────────────────────────────────────────────────

    /** Cooldown after emerging from underground — prevents spamming hide. */
    private GameTimer hideCooldown   = new GameTimer(2.0, false);

    /** Cooldown after a portal warp — prevents instantly re-warping at the same edge. */
    private GameTimer portalCooldown = new GameTimer(GameConfig.PORTAL_COOLDOWN_DURATION, false);

    // ─────────────────────────────────────────────────────────────────────────
    // VISUAL ACTORS
    // ─────────────────────────────────────────────────────────────────────────

    /** The zipper-on-the-road graphic that follows the player while underground. */
    private FX_ZipperGround zipperGround = null;

    /** Zipper flash at the top edge of the screen (appears briefly after a warp). */
    private FX_Portal topPortal    = null;
    /** Zipper flash at the bottom edge of the screen (appears briefly after a warp). */
    private FX_Portal bottomPortal = null;

    /** Counts down how many frames the portal flash visuals should remain. */
    private int portalVisualFrames = 0;

    /** How many frames the portal flash stays visible. 18 frames ≈ 0.3 seconds. */
    private static final int PORTAL_FLASH_DURATION = 18;

    // ═════════════════════════════════════════════════════════════════════════
    // ACTIVATE — toggle underground / emerge
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Called every frame the F key is held.  keyWasDown debounces this to once
     * per physical key press.
     *
     * If not hidden and not on cooldown → ZIP IN (go underground).
     * If already hidden               → POP OUT (emerge with i-frames).
     */
    @Override
    public void activate(Player p, MyWorld world) {
        if (keyWasDown) return; // debounce: only fire once per key press
        keyWasDown = true;

        if (!hidden && !hideCooldown.isActive()) {
            // ── ZIP INTO THE ROAD ───────────────────────────────────────────
            hidden = true;
            p.setAnimation("Idle"); // stand still while underground

            // Spawn the zipper graphic at the player's feet
            zipperGround = new FX_ZipperGround();
            world.addObject(zipperGround, p.getX(), p.getY());

        } else if (hidden) {
            // ── POP BACK OUT ────────────────────────────────────────────────
            hidden = false;

            // Remove the zipper graphic
            if (zipperGround != null && zipperGround.getWorld() != null) {
                world.removeObject(zipperGround);
            }
            zipperGround = null;

            // Grant brief invincibility so the player doesn't die the instant they emerge
            if (p instanceof GenericPlayer) ((GenericPlayer) p).startIFrame(0.8);
            p.setAnimation("Dash");

            // Start the cooldown before hiding again
            hideCooldown.reset();
            hideCooldown.start();
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // UPDATE — runs every frame
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Handles all per-frame logic:
     *   - Tick hide and portal cooldowns (only when NOT hidden, preventing exploit).
     *   - Release the keyWasDown debounce when F is released.
     *   - Re-link the FX_ZipperGround if the rewind system lost the reference.
     *   - Move the FX_ZipperGround to follow the player.
     *   - Count down the portal flash visual timer.
     *   - Check the portal warp trigger zone at screen edges.
     */
    @Override
    public void update(Player p, MyWorld world) {
        // Timers only progress while above ground.
        // This prevents: hide → timers freeze → emerge with full cooldowns instantly.
        if (!hidden) {
            hideCooldown.update(world);
            portalCooldown.update(world);
        }

        // Release the debounce flag when the F key is released
        if (!Greenfoot.isKeyDown(getKeybind())) {
            keyWasDown = false;
        }

        // ── REWIND FIX: re-link the zipper actor if the reference was lost ───
        // After a rewind, the FX_ZipperGround stored here may have been removed
        // and re-added by the rewind system, invalidating our reference.
        if (hidden) {
            if (zipperGround == null || zipperGround.getWorld() == null) {
                java.util.List<FX_ZipperGround> zippers = world.getObjects(FX_ZipperGround.class);
                if (!zippers.isEmpty()) {
                    zipperGround = zippers.get(0); // re-link to the existing actor
                } else {
                    // None found — create a fresh one
                    zipperGround = new FX_ZipperGround();
                    world.addObject(zipperGround, p.getX(), p.getY());
                }
            }
            // Keep the zipper glued to the player's feet
            zipperGround.setLocation(p.getX(), p.getY());
        }

        // ── Portal flash visual countdown ─────────────────────────────────────
        if (portalVisualFrames > 0) {
            portalVisualFrames--;
            if (portalVisualFrames <= 0) cleanupPortals(world); // remove flash actors
        }

        // ── PORTAL WRAP CHECK (passive, only when above ground and off cooldown) ──
        if (!hidden && !portalCooldown.isActive()) {
            int margin = GameConfig.PORTAL_MARGIN;
            if (p.getY() <= margin) {
                doPortalWarp(p, world, true);  // at top → warp to bottom
            } else if (p.getY() >= world.getHeight() - margin) {
                doPortalWarp(p, world, false); // at bottom → warp to top
            }
        }
    }

    // ═════════════════════════════════════════════════════════════════════════
    // PORTAL WARP — teleports the player to the opposite screen edge
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Teleports the player to the opposite screen edge, grants i-frames,
     * starts the portal cooldown, and spawns a zipper flash at both edges.
     *
     * @param p        The player to teleport.
     * @param world    The game world.
     * @param fromTop  True if warping from top to bottom; false for bottom to top.
     */
    private void doPortalWarp(Player p, MyWorld world, boolean fromTop) {
        // Calculate the exit Y so we land slightly inward from the edge
        // (prevents immediately re-triggering the warp on the next frame)
        int exitY = fromTop
            ? world.getHeight() - GameConfig.PORTAL_MARGIN - GameConfig.s(5) // top→bottom
            : GameConfig.PORTAL_MARGIN + GameConfig.s(5);                    // bottom→top

        p.setLocation(p.getX(), exitY);

        // Grant i-frames and start cooldown
        p.startIFrame(GameConfig.PORTAL_IFRAME_DURATION);
        portalCooldown.reset();
        portalCooldown.start();

        // Spawn zipper flashes at both edges simultaneously
        cleanupPortals(world); // remove any previous flash actors first
        topPortal    = new FX_Portal(world.getWidth(), false); // teeth face DOWN (top edge)
        bottomPortal = new FX_Portal(world.getWidth(), true);  // teeth face UP  (bottom edge)
        world.addObject(topPortal,    world.getWidth() / 2, GameConfig.s(10));
        world.addObject(bottomPortal, world.getWidth() / 2, world.getHeight() - GameConfig.s(10));

        portalVisualFrames = PORTAL_FLASH_DURATION; // they fade out over 18 frames
    }

    /** Removes both portal flash actors from the world and clears the references. */
    private void cleanupPortals(MyWorld world) {
        if (topPortal    != null && topPortal.getWorld()    != null) world.removeObject(topPortal);
        if (bottomPortal != null && bottomPortal.getWorld() != null) world.removeObject(bottomPortal);
        topPortal    = null;
        bottomPortal = null;
    }

    // ═════════════════════════════════════════════════════════════════════════
    // ABILITY INTERFACE
    // ═════════════════════════════════════════════════════════════════════════

    @Override
    public void cancel() {
        hidden = false;
        hideCooldown.stop();
    }

    /** True while underground — used by GenericPlayer, die(), checkRemove, animationLogic. */
    @Override public boolean isActive()         { return hidden; }
    /** True while underground — makes the player sprite invisible and invincible. */
    @Override public boolean shouldHidePlayer() { return hidden; }

    @Override
    public boolean isCooldownActive() {
        return hideCooldown.isActive() || portalCooldown.isActive();
    }

    /** Orange outer ring: full (1.0) while hidden; 0 when not underground. */
    @Override
    public double getActivePercent()   { return hidden ? 1.0 : 0.0; }

    /** Blue outer ring: shows progress of the HIDE cooldown. */
    @Override
    public double getCooldownPercent() { return hideCooldown.getPercentComplete(); }

    /** Blue inner ring: shows progress of the PORTAL cooldown. */
    @Override
    public double getSecondaryCooldownPercent() { return portalCooldown.getPercentComplete(); }

    @Override public String getKeybind()      { return GameConfig.STICKY_FINGER_BUTTON; }
    @Override public String getDisplayLabel() { return "F"; }

    // ═════════════════════════════════════════════════════════════════════════
    // TIME MACHINE
    // ═════════════════════════════════════════════════════════════════════════

    /**
     * Captures the complete state of this ability for the rewind history.
     * Format: int[6] = [hidden, hideCooldownFrames, hideCooldownActive,
     *                   portalCooldownFrames, portalCooldownActive, keyWasDown]
     */
    @Override
    public Object captureState() {
        return new int[]{
            hidden ? 1 : 0,
            hideCooldown.getRemainingFrames(),
            hideCooldown.isActive() ? 1 : 0,
            portalCooldown.getRemainingFrames(),
            portalCooldown.isActive() ? 1 : 0,
            keyWasDown ? 1 : 0
        };
    }

    /**
     * Restores this ability from a past snapshot.
     * Special case: if hidden was true but we're restoring to not-hidden,
     * we must remove the FX_ZipperGround actor (it should not exist above ground).
     */
    @Override
    public void restoreState(Object state) {
        int[] d     = (int[]) state;
        boolean wasHidden = hidden;

        hidden = (d[0] == 1);

        // If we just transitioned from hidden to not-hidden, remove the zipper actor
        if (wasHidden && !hidden) {
            if (zipperGround != null && zipperGround.getWorld() != null) {
                zipperGround.getWorld().removeObject(zipperGround);
            }
            zipperGround = null;
        }

        hideCooldown.setRemainingFrames(d[1]);
        if (d[2] == 1) hideCooldown.start(); else hideCooldown.stop();

        portalCooldown.setRemainingFrames(d[3]);
        if (d[4] == 1) portalCooldown.start(); else portalCooldown.stop();

        this.keyWasDown = (d[5] == 1);
    }
}
