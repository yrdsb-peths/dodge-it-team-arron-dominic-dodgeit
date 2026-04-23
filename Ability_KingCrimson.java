import greenfoot.*;
import java.util.List;

public class Ability_KingCrimson implements Ability {

    public static boolean ERASING = false;
    public static double erasurePercent = 0.0;
    
    private boolean standingInDanger = false;

    // The Preview (Epitaph) can last up to 1.5 real-time seconds (which equals 6 seconds of future)
    private GameTimer durationTimer = new GameTimer(1.5, false);
    private GameTimer cooldownTimer = new GameTimer(GameConfig.KC_COOLDOWN, false);
    
    private int slowMoTimer = 0;
    private int holdFrames = 0;
    private static final int HOLD_THRESHOLD = 8; // Very quick windup for the Right Arrow
    
    private FX_KingCrimsonOverlay overlay = null;
    private Time_FrameSnapshot savedReality = null; // Stores the present before we fast-forward

    @Override
    public void activate(Player p, MyWorld world) { }

    @Override
    public void update(Player p, MyWorld world) {
        if (cooldownTimer.isActive()) cooldownTimer.update(world);

        // --- SLOW-MO RECOVERY LOGIC ---
        if (slowMoTimer > 0) {
            slowMoTimer--;
            if (slowMoTimer == 0) Greenfoot.setSpeed(50); // Restore normal speed
        }

        // INPUTS
        boolean rightDown = Greenfoot.isKeyDown("right");
        boolean qDown = Greenfoot.isKeyDown(GameConfig.KC_BUTTON);
        
        boolean canActivate = !ERASING && !durationTimer.isActive() && !cooldownTimer.isActive() && !world.isRewinding();

        // RIGHT ARROW starts the future preview
        if (rightDown && canActivate) {
            holdFrames++;
            if (holdFrames >= HOLD_THRESHOLD) {
                startErasure(world);
                holdFrames = 0;
            }
        } else if (!ERASING) {
            holdFrames = 0; 
        }

        // --- THE FUTURE SIMULATION (EPITAPH) ---
        if (ERASING && durationTimer.isActive()) {
            durationTimer.update(world);
            erasurePercent = durationTimer.getPercentComplete();

            // IF Q PRESSED -> COMMIT TO THE FUTURE (KING CRIMSON)
            if (qDown) {
                endErasure(world, p, true);
                return;
            }

            // IF RIGHT RELEASED OR TIMER EXPIRED -> REVERT TO PRESENT
            if (!rightDown || durationTimer.isExpired()) {
                endErasure(world, p, false);
                return;
            }

            standingInDanger = false;
            GameState state = world.getGSM().peekState();
            if (state instanceof PlayingState) {
                PlayingState ps = (PlayingState) state;
                SpawnManager sm = ps.getSpawnManager();
                
                // Fast forward 3 ticks per actual frame
                for (int i = 0; i < 3; i++) {
                    sm.update(world);
                    for(ScrollingRoad road : world.getObjects(ScrollingRoad.class)) road.act();
                    
                    List<Obstacles> obstacles = world.getObjects(Obstacles.class);
                    for (Obstacles obs : obstacles) {
                        obs.fastForwardMove();
                        
                        // EPITAPH DANGER CHECK
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

            // VISUAL FEEDBACK: Pulse red if standing in a future death zone
            if (standingInDanger) {
                p.getImage().setColor(new Color(255, 0, 0));
                if (durationTimer.getRemainingFrames() % 4 < 2) p.getImage().setTransparency(100);
                else p.getImage().setTransparency(255);
            } else {
                p.getImage().setTransparency(255);
            }
        }
    }

    private void startErasure(MyWorld world) {
        ERASING = true;
        erasurePercent = 0.0;
        durationTimer.reset();
        durationTimer.start();
        
        // SAVE THE CURRENT REALITY
        GameState state = world.getGSM().peekState();
        if (state instanceof PlayingState) {
            PlayingState ps = (PlayingState) state;
            // Force record the exact frame we started holding right arrow
            ps.getRewindManager().record(world, ps.getSpawnManager());
            savedReality = ps.getRewindManager().getLastSnapshot();
        }

        overlay = new FX_KingCrimsonOverlay();
        world.addObject(overlay, world.getWidth() / 2, world.getHeight() / 2);
        AudioManager.play("kingCrimsonDuration");
    }

    private void endErasure(MyWorld world, Player p, boolean commit) {
        AudioManager.stop("kingCrimsonDuration"); 
        ERASING = false;
        erasurePercent = 0.0;
        durationTimer.stop();
        overlay = null; 
        p.getImage().setTransparency(255);

        if (commit) {
            // --- KING CRIMSON COMMIT ---
            // SFX: Play a heavy, bass-boosted "skip" sound here
            AudioManager.playPool("skipTime"); 
            
            world.addObject(new FX_ErasureSnap(), world.getWidth() / 2, world.getHeight() / 2);
        } else {
            // --- EPITAPH REVERT ---
            if (savedReality != null) {
                int finalX = p.getX();
                int finalY = p.getY();
                
                GameState state = world.getGSM().peekState();
                if (state instanceof PlayingState) {
                    PlayingState ps = (PlayingState) state;
                    ps.getRewindManager().forceRestore(savedReality, world, ps.getSpawnManager());
                }
                p.setLocation(finalX, finalY);
                
                // SFX: Play a "rewind" or "zipper" sound here
                //AudioManager.play("epitaph_revert"); 
                
                // Use the new Cyan visual for reverting!
                world.addObject(new FX_EpitaphRevert(), world.getWidth() / 2, world.getHeight() / 2);
            }
        }

        // SLOW MO
        Greenfoot.setSpeed(35);
        slowMoTimer = 5; 

        cooldownTimer.reset();
        cooldownTimer.start();
        savedReality = null;
    }

    // Standard methods...
    @Override public void cancel() { 
        if (ERASING) { ERASING = false; Greenfoot.setSpeed(50); } 
        cooldownTimer.stop(); 
    }
    @Override public boolean isActive() { return ERASING; }
    @Override public boolean isCooldownActive() { return cooldownTimer.isActive(); }
    @Override public double getActivePercent() { return ERASING ? (1.0 - durationTimer.getPercentComplete()) : 0.0; }
    @Override public double getCooldownPercent() { return cooldownTimer.isActive() ? cooldownTimer.getPercentComplete() : 0.0; }
    @Override public String getKeybind() { return "right"; }
    @Override public String getDisplayLabel() { return "->"; } // Custom UI Label
    @Override public boolean shouldShowIcon() { return true; }

    @Override public Object captureState() { return new int[]{ durationTimer.getRemainingFrames(), cooldownTimer.getRemainingFrames(), durationTimer.isActive() ? 1 : 0, cooldownTimer.isActive() ? 1 : 0, ERASING ? 1 : 0 }; }
    
    @Override public void restoreState(Object state) { 
        int[] d = (int[]) state; durationTimer.setRemainingFrames(d[0]); cooldownTimer.setRemainingFrames(d[1]);
        if (d[2] == 1) durationTimer.start(); else durationTimer.stop();
        if (d[3] == 1) cooldownTimer.start(); else cooldownTimer.stop();
        ERASING = (d[4] == 1); erasurePercent = ERASING ? durationTimer.getPercentComplete() : 0.0; overlay = null;
    }
}