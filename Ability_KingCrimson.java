import greenfoot.*;
import java.util.List;

/**
 * ─────────────────────────────────────────────────────────────────────────────
 * Ability_KingCrimson.java — THE EMPEROR OF TIME
 * ─────────────────────────────────────────────────────────────────────────────
 * 1. HOLD [RIGHT ARROW] to use EPITAPH: The world fast-forwards 400%. 
 *    You are a ghost; move to reposition.
 * 2. RELEASE [RIGHT] to REVERT: The world snaps back to the present, 
 *    but you keep your new physical position.
 * 3. PRESS [Q] during vision to ERASE: Commit to the future timeline.
 * 
 * SHIREN: Standing inside cars during the vision awards points and shatters reality.
 * SAFETY: Player pulses red if standing in a death zone when the skip ends.
 * ─────────────────────────────────────────────────────────────────────────────
 */
public class Ability_KingCrimson implements Ability {

    public static boolean ERASING = false;
    public static double erasurePercent = 0.0;
    
    private boolean standingInDanger = false;

    // The Preview (Epitaph) can last up to 1.5 real-time seconds (6 seconds of future)
    private GameTimer durationTimer = new GameTimer(1.5, false);
    private GameTimer cooldownTimer = new GameTimer(GameConfig.KC_COOLDOWN, false);
    
    private int slowMoTimer = 0;
    private int holdFrames = 0;
    private static final int HOLD_THRESHOLD = 8; 
    
    private FX_KingCrimsonOverlay overlay = null;
    private Time_FrameSnapshot savedReality = null; 
    
            
    private static final int POOL = GameConfig.MAX_REWIND_TIME + 10; // 370 slots
    private int[][] statePool = new int[POOL][5]; // 5 = however many values you store
    private int poolIdx = 0;

    @Override
    public void activate(Player p, MyWorld world) { /* Logic handled in update */ }

    @Override
    public void update(Player p, MyWorld world) {
        // 1. Tick Cooldown
        if (cooldownTimer.isActive()) cooldownTimer.update(world);

        // 2. Handle Reflex Window (Slow-Mo) recovery
        if (slowMoTimer > 0) {
            slowMoTimer--;
            if (slowMoTimer == 0) Greenfoot.setSpeed(50); 
        }

        // 3. Activation Check
        boolean rightDown = Greenfoot.isKeyDown("right");
        boolean qDown = Greenfoot.isKeyDown(GameConfig.KC_BUTTON);
        boolean canActivate = !ERASING && !durationTimer.isActive() && !cooldownTimer.isActive() && !world.isRewinding();

        if (rightDown && canActivate) {
            holdFrames++;
            if (holdFrames >= HOLD_THRESHOLD) {
                startErasure(world);
                holdFrames = 0;
            }
        } else if (!ERASING) {
            holdFrames = 0; 
        }

        // 4. Active Vision Logic
        if (ERASING && durationTimer.isActive()) {
            durationTimer.update(world);
            erasurePercent = durationTimer.getPercentComplete();
            p.getImage().setTransparency(180); // Look ghostly

            // --- INPUT HANDLING ---
            if (qDown) {
                endErasure(world, p, true); // COMMIT (King Crimson)
                return;
            }
            if (!rightDown || durationTimer.isExpired()) {
                endErasure(world, p, false); // REVERT (Epitaph)
                return;
            }

            // --- THE FUTURE SIMULATION ---
            standingInDanger = false;
            GameState state = world.getGSM().peekState();
            
            if (state instanceof IActiveGameState) {
                IActiveGameState activeState = (IActiveGameState) state;
                SpawnManager sm = activeState.getSpawnManager();
                
                for (int i = 0; i < 3; i++) {
                    sm.update(world);
                    
                    // 1. Fast forward road
                    for(ScrollingRoad road : world.getObjects(ScrollingRoad.class)) {
                        road.fastForward(); 
                    }
                    
                    // 2. NEW: Fast forward Warning Signs
                    for(Exclaimation ex : world.getObjects(Exclaimation.class)) ex.fastForward();
                    for(PathWarning pw : world.getObjects(PathWarning.class)) pw.fastForward();
                    
                    // 3. Fast forward obstacles
                    List<Obstacles> obstacles = world.getObjects(Obstacles.class);
                    for (Obstacles obs : obstacles) {
                        obs.fastForwardMove();
                        
                        // DANGER CHECK & SHIREN REWARD
                        if (p.checkCustomHitbox(obs, 1.0)) {
                            standingInDanger = true; 
                            if (Greenfoot.getRandomNumber(100) < 15) {
                                world.addObject(new FX_ShatteredGlass(), p.getX(), p.getY());
                            }
                            if (Greenfoot.getRandomNumber(100) < 5) ScoreManager.addScore(1); 
                        }
                    }
                }
            }

            // --- EPITAPH VISUAL FEEDBACK ---
            if (standingInDanger) {
                // Pulse bright red to warn the player
                if (durationTimer.getRemainingFrames() % 4 < 2) p.getImage().setTransparency(50);
                else p.getImage().setTransparency(220);
            }
        }
    }

    private void startErasure(MyWorld world) {
        ERASING = true;
        erasurePercent = 0.0;
        durationTimer.reset();
        durationTimer.start();
        
        // Use IActiveGameState to support both PlayingState and AbilityDemo
        GameState state = world.getGSM().peekState();
        if (state instanceof IActiveGameState) {
            IActiveGameState activeState = (IActiveGameState) state;
            activeState.getRewindManager().record(world, activeState.getSpawnManager());
            savedReality = activeState.getRewindManager().getLastSnapshot();
        }

        overlay = new FX_KingCrimsonOverlay();
        world.addObject(overlay, world.getWidth() / 2, world.getHeight() / 2);
        
        AudioManager.playLoop("kingCrimsonDuration"); // The vision hum
    }

    private void endErasure(MyWorld world, Player p, boolean commit) {
        AudioManager.stop("kingCrimsonDuration"); // Stop the vision hum immediately
        
        ERASING = false;
        erasurePercent = 0.0;
        durationTimer.stop();
        if (overlay != null && overlay.getWorld() != null) world.removeObject(overlay);
        overlay = null; 
        p.getImage().setTransparency(255);

        if (commit) {
            // --- COMMIT (Future remains) ---
            AudioManager.playPool("skipTime"); 
            world.addObject(new FX_ErasureSnap(), world.getWidth() / 2, world.getHeight() / 2);
        } else {
            // --- REVERT (Snap back to present) ---
            if (savedReality != null) {
                int currentX = p.getX();
                int currentY = p.getY();
                
                GameState state = world.getGSM().peekState();
                if (state instanceof IActiveGameState) {
                    IActiveGameState activeState = (IActiveGameState) state;
                    activeState.getRewindManager().forceRestore(savedReality, world, activeState.getSpawnManager());
                }
                
                p.setLocation(currentX, currentY); // Keep new physical position
                
                world.addObject(new FX_EpitaphRevert(), world.getWidth() / 2, world.getHeight() / 2);
            }
        }

        // TRIGGER REFLEX WINDOW (Slow-mo)
        Greenfoot.setSpeed(35);
        slowMoTimer = 5; 

        cooldownTimer.reset();
        cooldownTimer.start();
        savedReality = null;
    }

    @Override public void cancel() { 
        AudioManager.stop("kingCrimsonDuration");
        if (ERASING) { ERASING = false; Greenfoot.setSpeed(50); } 
        cooldownTimer.stop(); 
    }

    @Override public boolean isActive() { return ERASING; }
    @Override public boolean isCooldownActive() { return cooldownTimer.isActive(); }
    @Override public double getActivePercent() { return ERASING ? (1.0 - durationTimer.getPercentComplete()) : 0.0; }
    @Override public double getCooldownPercent() { return cooldownTimer.isActive() ? cooldownTimer.getPercentComplete() : 0.0; }
    @Override public String getKeybind() { return "right"; }
    @Override public String getDisplayLabel() { return "->"; }
    @Override public boolean shouldShowIcon() { return true; }

    @Override 
    public Object captureState() {
        int[] s = statePool[poolIdx++ % POOL];
        s[0] = durationTimer.getRemainingFrames();
        s[1] = cooldownTimer.getRemainingFrames();
        s[2] = durationTimer.isActive() ? 1 : 0;
        s[3] = cooldownTimer.isActive() ? 1 : 0;
        // KingCrimson also needs: s[4] = ERASING ? 1 : 0;
        return s;
    }
    
    @Override public void restoreState(Object state) { 
        int[] d = (int[]) state; 
        durationTimer.setRemainingFrames(d[0]); 
        cooldownTimer.setRemainingFrames(d[1]);
        if (d[2] == 1) durationTimer.start(); else durationTimer.stop();
        if (d[3] == 1) cooldownTimer.start(); else cooldownTimer.stop();
        ERASING = (d[4] == 1); 
        overlay = null;
    }
}