import greenfoot.*;
import java.util.List;

public class Ability_KingCrimson implements Ability {

    public static boolean ERASING = false;
    public static double erasurePercent = 0.0;
    
    private boolean standingInDanger = false;
    private GameTimer durationTimer = new GameTimer(1.5, false);
    private GameTimer cooldownTimer = new GameTimer(GameConfig.KC_COOLDOWN, false);
        
    private int slowMoTimer = 0;
    private int holdFrames = 0;
    private static final int HOLD_THRESHOLD = 8; 
    
    private FX_KingCrimsonOverlay overlay = null;
    private Time_FrameSnapshot savedReality = null; 
    
    // Performance State Pool
    private static final int POOL = GameConfig.MAX_REWIND_TIME + 10; 
    private int[][] statePool = new int[POOL][5]; 
    private int poolIdx = 0;

    @Override
    public void activate(Player p, MyWorld world) { }

    @Override
    public void update(Player p, MyWorld world) {
        if (cooldownTimer.isActive()) cooldownTimer.update(world);

        if (slowMoTimer > 0) {
            slowMoTimer--;
            if (slowMoTimer == 0) Greenfoot.setSpeed(50); 
        }

        // --- ZOMBIE PROTECTION ---
        // If for any reason ERASING is true but the timer isn't running, force a reset.
        if (ERASING && !durationTimer.isActive()) {
            ERASING = false;
            overlay = null;
        }

        boolean rightDown = Greenfoot.isKeyDown("right");
        boolean qDown = Greenfoot.isKeyDown(GameConfig.KC_BUTTON);
        
        // canActivate logic
        boolean canActivate = !ERASING 
                           && !durationTimer.isActive() 
                           && !cooldownTimer.isActive() 
                           && !world.isRewinding();

        if (rightDown && canActivate) {
            holdFrames++;
            if (holdFrames >= HOLD_THRESHOLD) {
                startErasure(world);
                holdFrames = 0;
            }
        } else if (!ERASING) {
            holdFrames = 0; 
        }

        if (ERASING && durationTimer.isActive()) {
            durationTimer.forceTick();
            erasurePercent = durationTimer.getPercentComplete();
            p.getImage().setTransparency(180);

            if (qDown) {
                endErasure(world, p, true);
                return;
            }
            if (!rightDown || durationTimer.isExpired()) {
                endErasure(world, p, false);
                return;
            }

            // --- FUTURE SIMULATION ---
            standingInDanger = false;
            GameState state = world.getGSM().peekState();
            
            if (state instanceof IActiveGameState) {
                IActiveGameState activeState = (IActiveGameState) state;
                SpawnManager sm = activeState.getSpawnManager();
                
                for (int i = 0; i < 3; i++) {
                    sm.update(world);
                    for(ScrollingRoad road : world.getObjects(ScrollingRoad.class)) road.fastForward(); 
                    for(Exclaimation ex : world.getObjects(Exclaimation.class)) ex.fastForward();
                    for(PathWarning pw : world.getObjects(PathWarning.class)) pw.fastForward();
                    
                    List<Obstacles> obstacles = world.getObjects(Obstacles.class);
                    for (Obstacles obs : obstacles) {
                        obs.fastForwardMove();
                        if (p.checkCustomHitbox(obs, 1.0)) {
                            standingInDanger = true; 
                            if (Greenfoot.getRandomNumber(100) < 15) world.addObject(new FX_ShatteredGlass(), p.getX(), p.getY());
                            if (Greenfoot.getRandomNumber(100) < 5) ScoreManager.addScore(1); 
                        }
                    }
                }
            }

            if (standingInDanger) {
                if (durationTimer.getRemainingFrames() % 4 < 2) p.getImage().setTransparency(50);
                else p.getImage().setTransparency(220);
            }
        }
    }

    private void startErasure(MyWorld world) {
        
        GameState state = world.getGSM().peekState();
        if (state instanceof IActiveGameState) {
            IActiveGameState activeState = (IActiveGameState) state;
            activeState.getRewindManager().record(world, activeState.getSpawnManager());
            savedReality = activeState.getRewindManager().getLastSnapshot();
        }
        
        ERASING = true;
        erasurePercent = 0.0;
        durationTimer.reset();
        durationTimer.start();
        SaveManager.addInt("use_Ability_KingCrimson", 1);
        
        overlay = new FX_KingCrimsonOverlay();
        world.addObject(overlay, world.getWidth() / 2, world.getHeight() / 2);
        AudioManager.playLoop("kingCrimsonDuration");
    }

    private void endErasure(MyWorld world, Player p, boolean commit) {
    AudioManager.stop("kingCrimsonDuration");
    ERASING = false;
    erasurePercent = 0.0;
    durationTimer.stop();
    if (overlay != null && overlay.getWorld() != null) world.removeObject(overlay);
    overlay = null; 
    p.getImage().setTransparency(255);

    if (commit) {
        AudioManager.playPool("skipTime"); 
        world.addObject(new FX_ErasureSnap(), world.getWidth() / 2, world.getHeight() / 2);
    } else {
        if (savedReality != null) {
            int currentX = p.getX();
            int currentY = p.getY();
            GameState state = world.getGSM().peekState();
            if (state instanceof IActiveGameState) {
                IActiveGameState activeState = (IActiveGameState) state;
                
                // 1. Restore the world to the past
                activeState.getRewindManager().forceRestore(savedReality, world, activeState.getSpawnManager());
                
                // 2. IMPORTANT: The restore just reset this ability's variables.
                // We MUST manually re-set the cooldown and ERASING state here
                // AFTER the restoration so they persist in the "new" reality.
                this.cooldownTimer.reset();
                this.cooldownTimer.start();
                this.ERASING = false; 
                this.holdFrames = -15; // Optional: add extra delay before it can charge again
            }
            p.setLocation(currentX, currentY);
            world.addObject(new FX_EpitaphRevert(), world.getWidth() / 2, world.getHeight() / 2);
        }
    }

    Greenfoot.setSpeed(35);
    slowMoTimer = 1; 
    
    // Move these inside the 'if (commit)' or handle carefully
    // If you keep them here, they only apply to the 'commit' branch 
    // because the 'else' branch does its own restore.
    if (commit) {
        cooldownTimer.reset();
        cooldownTimer.start();
    }
    
    savedReality = null;
}

    @Override public void cancel() { 
        AudioManager.stop("kingCrimsonDuration");
        ERASING = false; // RESET THE STATIC FLAG
        Greenfoot.setSpeed(50);
        slowMoTimer = 0;
        durationTimer.stop();
        cooldownTimer.stop();
        overlay = null;
        savedReality = null;
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
        s[4] = ERASING ? 1 : 0; // FIX: ACTUALLY SAVE THE STATE
        return s;
    }
    
    @Override public void restoreState(Object state) { 
        int[] d = (int[]) state; 
        durationTimer.setRemainingFrames(d[0]); 
        cooldownTimer.setRemainingFrames(d[1]);
        if (d[2] == 1) durationTimer.start(); else durationTimer.stop();
        if (d[3] == 1) cooldownTimer.start(); else cooldownTimer.stop();
        
        // FIX: Re-sync the static flag with the past
        ERASING = (d[4] == 1); 
        overlay = null;
    }
}