import greenfoot.*;

/*
 * STICKY FINGERS — Bruno Bucciarati's Stand
 * 
 * Two abilities in one key:
 * 
 * 1. PORTAL WRAP (passive, always active):
 *    Move to the top/bottom edge and you zip through a portal,
 *    emerging from the opposite side. Top → Bottom, Bottom → Top.
 *    A brief flash shows the zipper opening at both edges.
 * 
 * 2. HIDE IN THE ROAD (press F to toggle):
 *    Zip into the road itself. You become invisible and invincible.
 *    Obstacles pass through you. BUT you earn zero score while hidden.
 *    The difficulty keeps climbing while you hide — risky to stay too long!
 *    Press F again to emerge with a brief i-frame.
 * 
 * Both mechanics use the same zipper visual language.
 */
public class Ability_StickyFingers implements Ability {

    // ── State ──────────────────────────────────────────────────────────────────
    private boolean hidden = false;
    private boolean keyWasDown = false; // Debounce flag
    // ── Timers ─────────────────────────────────────────────────────────────────
    // Cooldown before you can hide again after emerging
    private GameTimer hideCooldown = new GameTimer(2.0, false);
    // Prevents the portal from re-firing every frame while at the edge
    private int portalCooldownFrames = 0;
    //Cooldown timer
    private GameTimer portalCooldown = new GameTimer(GameConfig.PORTAL_COOLDOWN_DURATION, false);

    // ── Actors we need to clean up ─────────────────────────────────────────────
    private FX_ZipperGround zipperGround = null;
    private FX_Portal topPortal   = null;
    private FX_Portal bottomPortal = null;
    private int portalVisualFrames = 0;
    private static final int PORTAL_FLASH_DURATION = 18; // frames the portal flash stays

    // ══════════════════════════════════════════════════════════════════════════
    // ACTIVATE — toggle hide/unhide
    // ══════════════════════════════════════════════════════════════════════════
    public void activate(Player p, MyWorld world) {
        //do not trigger again when already key down
        if (keyWasDown) return; 
        keyWasDown = true;
        
        if (!hidden && !hideCooldown.isActive()) {
            // ── ZIP INTO THE ROAD ──
            hidden = true;
            p.setAnimation("Idle");

            zipperGround = new FX_ZipperGround();
            world.addObject(zipperGround, p.getX(), p.getY());

        } else if (hidden) {
            // ── POP BACK OUT ──
            hidden = false;

            if (zipperGround != null && zipperGround.getWorld() != null) {
                world.removeObject(zipperGround);
            }
            zipperGround = null;

            // Brief i-frame so you don't instantly die emerging
            if (p instanceof GenericPlayer) ((GenericPlayer) p).startIFrame(0.8);
            p.setAnimation("Dash");

            hideCooldown.reset();
            hideCooldown.start();
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // UPDATE — called every frame
    // ══════════════════════════════════════════════════════════════════════════
    public void update(Player p, MyWorld world) {
        // Timers only progress if we are NOT hidden underground.
        // This prevents the "Infinite I-Frame" abuse.
        if (!hidden) {
            hideCooldown.update(world);
            portalCooldown.update(world);
        }
        //Reset key bounce
        if (!Greenfoot.isKeyDown(getKeybind())) {
            keyWasDown = false;
        }
        if (portalCooldownFrames > 0) portalCooldownFrames--;
        
        //TO fix the visual bug where reinwing from above ground to underground doesnt show the zipper back
        if (hidden) {
            // If the reference is lost (common after rewind), find it in the world
            if (zipperGround == null || zipperGround.getWorld() == null) {
                java.util.List<FX_ZipperGround> zippers = world.getObjects(FX_ZipperGround.class);
                if (!zippers.isEmpty()) {
                    zipperGround = zippers.get(0); // Re-link the reference
                } else {
                    // If it's truly gone from the world, recreate it
                    zipperGround = new FX_ZipperGround();
                    world.addObject(zipperGround, p.getX(), p.getY());
                }
            }
            // Now that we definitely have a reference, move it
            zipperGround.setLocation(p.getX(), p.getY());
        }

        // Keep zipper glued to the player's position while hiding
        if (hidden && zipperGround != null && zipperGround.getWorld() != null) {
            zipperGround.setLocation(p.getX(), p.getY());
        }

        // Count down portal flash visuals
        if (portalVisualFrames > 0) {
            portalVisualFrames--;
            if (portalVisualFrames <= 0) cleanupPortals(world);
        }

        // ── PORTAL WRAP CHECK (passive, only when not hidden) ──
        if (!hidden && !portalCooldown.isActive()) {
            int margin = GameConfig.PORTAL_MARGIN;
            if (p.getY() <= margin) {
                // At top — warp to bottom
                doPortalWarp(p, world, true);
            } else if (p.getY() >= world.getHeight() - margin) {
                // At bottom — warp to top
                doPortalWarp(p, world, false);
            }
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // PORTAL WARP
    // ══════════════════════════════════════════════════════════════════════════
    private void doPortalWarp(Player p, MyWorld world, boolean fromTop) {
        // Teleport: top ↔ bottom with a small inset so we don't immediately re-trigger
        int exitY = fromTop
            ? world.getHeight() - GameConfig.PORTAL_MARGIN - GameConfig.s(5)
            : GameConfig.PORTAL_MARGIN + GameConfig.s(5);

        p.setLocation(p.getX(), exitY);

        //Grant i frames and start cooldown
        p.startIFrame(GameConfig.PORTAL_IFRAME_DURATION); 
        portalCooldown.reset();
        portalCooldown.start();
        // Spawn flash portals at both edges
        cleanupPortals(world);
        topPortal    = new FX_Portal(world.getWidth(), false); // top edge, teeth face down
        bottomPortal = new FX_Portal(world.getWidth(), true);  // bottom edge, teeth face up
        world.addObject(topPortal,    world.getWidth() / 2, GameConfig.s(10));
        world.addObject(bottomPortal, world.getWidth() / 2, world.getHeight() - GameConfig.s(10));

        portalVisualFrames = PORTAL_FLASH_DURATION;
    }

    private void cleanupPortals(MyWorld world) {
        if (topPortal    != null && topPortal.getWorld()    != null) world.removeObject(topPortal);
        if (bottomPortal != null && bottomPortal.getWorld() != null) world.removeObject(bottomPortal);
        topPortal = null;
        bottomPortal = null;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // ABILITY INTERFACE
    // ══════════════════════════════════════════════════════════════════════════
    public void cancel() {
        hidden = false;
        hideCooldown.stop();
    }

    public boolean isActive()         { return hidden; }
    
    public boolean shouldHidePlayer() { return hidden; }  
    
    public boolean isCooldownActive() {
        return hideCooldown.isActive() || portalCooldown.isActive();
    }
    
    public String getKeybind()        { return GameConfig.STICKY_FINGER_BUTTON; }

    public double getActivePercent() {
        // When hidden: show full orange (no drain — you can stay as long as you dare)
        return hidden ? 1.0 : 0.0;
    }

    public double getCooldownPercent() {
        // Main wheel shows the "Hide" cooldown
        return hideCooldown.getPercentComplete();
    }
    
    
    public double getSecondaryCooldownPercent() {
        // Inner wheel shows the "Portal" cooldown
        return portalCooldown.getPercentComplete();
    }

    
    public String getDisplayLabel() { return "F"; }
    
    // ── Time Machine ──────────────────────────────────────────────────────────
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

    public void restoreState(Object state) {
        int[] d = (int[]) state;
        boolean wasHidden = hidden;
        
        hidden = (d[0] == 1);
         if (wasHidden && !hidden) {
            if (zipperGround != null && zipperGround.getWorld() != null) {
                zipperGround.getWorld().removeObject(zipperGround);
            }
            zipperGround = null;
        } 
        
        hideCooldown.setRemainingFrames(d[1]);
        if (d[2] == 1) hideCooldown.start(); else hideCooldown.stop();
        
        // RESTORE THE PORTAL COOLDOWN
        portalCooldown.setRemainingFrames(d[3]);
        if (d[4] == 1) portalCooldown.start(); else portalCooldown.stop();
        
        this.keyWasDown = (d[5] == 1);
    }
}