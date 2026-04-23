import greenfoot.*;
import java.util.List;

public class Ability_KingCrimson implements Ability {

    public static boolean ERASING = false;
    public static double erasurePercent = 0.0;

    // Max skip duration: 1.0 second of real time = 4 seconds of game time skipped
    private GameTimer durationTimer = new GameTimer(GameConfig.KC_DURATION, false);
    private GameTimer cooldownTimer = new GameTimer(GameConfig.KC_COOLDOWN, false);

    private int holdFrames = 0;
    private static final int HOLD_THRESHOLD = 12; // 0.2s wind-up
    private FX_KingCrimsonOverlay overlay = null;

    @Override
    public void activate(Player p, MyWorld world) { }

    @Override
    public void update(Player p, MyWorld world) {
        if (cooldownTimer.isActive()) cooldownTimer.update(world);

        String key = GameConfig.KC_BUTTON;
        boolean keyDown = Greenfoot.isKeyDown(key);
        
        // --- 1. ACTIVATION LOGIC (WIND-UP) ---
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

        // --- 2. ACTIVE ERASURE LOGIC ---
        if (ERASING && durationTimer.isActive()) {
            durationTimer.update(world);
            erasurePercent = durationTimer.getPercentComplete();

            // REVOLUTIONARY FIX: If the player releases Q, end the skip IMMEDIATELY
            if (!keyDown) {
                endErasure(world);
                return;
            }

            // THE VHS FAST FORWARD
            // We simulate 4x world speed while the player moves at normal speed
            GameState state = world.getGSM().peekState();
            if (state instanceof PlayingState) {
                SpawnManager sm = ((PlayingState) state).getSpawnManager();
                for (int i = 0; i < 3; i++) {
                    sm.update(world);
                    
                    for(ScrollingRoad road : world.getObjects(ScrollingRoad.class)) {
                        road.act();
                    }
                    
                    List<Obstacles> obstacles = world.getObjects(Obstacles.class);
                    for (Obstacles obs : obstacles) {
                        obs.fastForwardMove();
                        
                        // SHATTERED FATE REWARD:
                        if (p.checkCustomHitbox(obs, 0.8)) {
                            if (Greenfoot.getRandomNumber(100) < 15) {
                                world.addObject(new FX_ShatteredGlass(), p.getX(), p.getY());
                            }
                            if (Greenfoot.getRandomNumber(100) < 5) ScoreManager.addScore(1); 
                        }
                    }
                }
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

        // The Reality Snap Flash
        world.addObject(new FX_ErasureSnap(), world.getWidth() / 2, world.getHeight() / 2);
        AudioManager.play("kc_snap");

        // Start Cooldown
        cooldownTimer.reset();
        cooldownTimer.start();
    }

    // ... (rest of the interface methods remain the same)
    @Override public void cancel() { if (ERASING) { ERASING = false; erasurePercent = 0.0; durationTimer.stop(); overlay = null; } cooldownTimer.stop(); }
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
        ERASING = (d[4] == 1); erasurePercent = ERASING ? durationTimer.getPercentComplete() : 0.0; overlay = null;
    }
}