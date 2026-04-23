import greenfoot.*;
import java.util.List;

public class Ability_KingCrimson implements Ability {

    public static boolean ERASING = false;
    public static double erasurePercent = 0.0;
    
    // New: Tracks if the player is currently standing in a "Death Zone"
    private boolean standingInDanger = false;

    private GameTimer durationTimer = new GameTimer(GameConfig.KC_DURATION, false); // Slightly longer skip
    private GameTimer cooldownTimer = new GameTimer(GameConfig.KC_COOLDOWN, false);
    
    // Timer for the post-skip slow-motion window
    private int slowMoTimer = 0;

    private int holdFrames = 0;
    private static final int HOLD_THRESHOLD = 12;
    private FX_KingCrimsonOverlay overlay = null;

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

        String key = GameConfig.KC_BUTTON;
        boolean keyDown = Greenfoot.isKeyDown(key);
        
        boolean canActivate = !ERASING && !durationTimer.isActive() && !cooldownTimer.isActive() && !world.isRewinding();

        if (keyDown && canActivate) {
            holdFrames++;
            if (holdFrames >= HOLD_THRESHOLD) {
                startErasure(world);
                holdFrames = 0;
            }
        } else if (!ERASING) {
            holdFrames = 0; 
        }

        if (ERASING && durationTimer.isActive()) {
            durationTimer.update(world);
            erasurePercent = durationTimer.getPercentComplete();

            if (!keyDown) {
                endErasure(world);
                return;
            }

            // RESET Danger flag every frame
            standingInDanger = false;

            GameState state = world.getGSM().peekState();
            if (state instanceof PlayingState) {
                SpawnManager sm = ((PlayingState) state).getSpawnManager();
                // 3 skip-ticks per real tick
                for (int i = 0; i < 3; i++) {
                    sm.update(world);
                    for(ScrollingRoad road : world.getObjects(ScrollingRoad.class)) road.act();
                    
                    List<Obstacles> obstacles = world.getObjects(Obstacles.class);
                    for (Obstacles obs : obstacles) {
                        obs.fastForwardMove();
                        
                        // --- EPITAPH CHECK ---
                        // If we overlap ANY car during the skip, mark danger
                        if (p.checkCustomHitbox(obs, 1.0)) {
                            standingInDanger = true; 
                            
                            // Shattered Glass Reward
                            if (Greenfoot.getRandomNumber(100) < 15) {
                                world.addObject(new FX_ShatteredGlass(), p.getX(), p.getY());
                            }
                            if (Greenfoot.getRandomNumber(100) < 5) ScoreManager.addScore(1); 
                        }
                    }
                }
            }

            // VISUAL FEEDBACK: Pulse the player red if they are in danger
            if (standingInDanger) {
                p.getImage().setColor(new Color(255, 0, 0));
                // Make the player flicker red to say "DON'T RELEASE!"
                if (durationTimer.getRemainingFrames() % 4 < 2) {
                    p.getImage().setTransparency(100);
                } else {
                    p.getImage().setTransparency(255);
                }
            } else {
                p.getImage().setTransparency(255);
            }

            if (durationTimer.isExpired()) {
                endErasure(world);
            }
        }
    }

    private void startErasure(MyWorld world) {
        ERASING = true;
        erasurePercent = 0.0;
        durationTimer.reset();
        durationTimer.start();
        overlay = new FX_KingCrimsonOverlay();
        world.addObject(overlay, world.getWidth() / 2, world.getHeight() / 2);
        AudioManager.play("kc_activate");
    }

    private void endErasure(MyWorld world) {
        ERASING = false;
        erasurePercent = 0.0;
        durationTimer.stop();
        overlay = null; 

        world.addObject(new FX_ErasureSnap(), world.getWidth() / 2, world.getHeight() / 2);
        AudioManager.play("kc_snap");

        // --- SAFETY MECHANISM: THE REFLEX WINDOW ---
        // Slow down the game to 35 (about 60% speed) for 15 frames
        Greenfoot.setSpeed(35);
        slowMoTimer = 5; 

        cooldownTimer.reset();
        cooldownTimer.start();
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
    @Override public String getKeybind() { return GameConfig.KC_BUTTON; }
    @Override public String getDisplayLabel() { return "Q"; }
    @Override public Object captureState() { return new int[]{ durationTimer.getRemainingFrames(), cooldownTimer.getRemainingFrames(), durationTimer.isActive() ? 1 : 0, cooldownTimer.isActive() ? 1 : 0, ERASING ? 1 : 0 }; }
    @Override public void restoreState(Object state) { 
        int[] d = (int[]) state; durationTimer.setRemainingFrames(d[0]); cooldownTimer.setRemainingFrames(d[1]);
        if (d[2] == 1) durationTimer.start(); else durationTimer.stop();
        if (d[3] == 1) cooldownTimer.start(); else cooldownTimer.stop();
        ERASING = (d[4] == 1); overlay = null;
    }
}