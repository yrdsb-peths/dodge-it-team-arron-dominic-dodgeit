import greenfoot.*;
import java.util.List;

public class Ability_KingCrimson implements Ability {

    public static boolean ERASING = false;
    public static double erasurePercent = 0.0;

    private GameTimer durationTimer = new GameTimer(GameConfig.KC_DURATION, false);
    private GameTimer cooldownTimer = new GameTimer(GameConfig.KC_COOLDOWN, false);

    private int holdFrames = 0;
    private static final int HOLD_THRESHOLD = 12;

    private FX_KingCrimsonOverlay overlay = null;

    @Override
    public void activate(Player p, MyWorld world) { /* Handled in update */ }

    @Override
    public void update(Player p, MyWorld world) {
        if (cooldownTimer.isActive()) cooldownTimer.update(world);

        boolean keyDown = Greenfoot.isKeyDown(GameConfig.KC_BUTTON);
        boolean canActivate = !ERASING && !durationTimer.isActive() && !cooldownTimer.isActive() && !world.isRewinding();

        // Hold-to-activate logic
        if (keyDown && canActivate) {
            holdFrames++;
            if (holdFrames >= HOLD_THRESHOLD) {
                startErasure(world);
                holdFrames = 0;
            }
        } else if (!keyDown) {
            holdFrames = 0; 
        }

        // Erasure tick
        if (ERASING && durationTimer.isActive()) {
            durationTimer.update(world);
            erasurePercent = durationTimer.getPercentComplete();

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
        
        int skipFrames = durationTimer.getTotalFrames();
        durationTimer.stop();
        overlay = null; 

        // THE MAGIC: FAST-FORWARD ALL OBSTACLES BY 2 SECONDS
        List<Obstacles> obstacles = world.getObjects(Obstacles.class);
        for (int i = 0; i < skipFrames; i++) {
            for (Obstacles obs : obstacles) {
                obs.fastForwardMove(); // Simulates their movement without killing the player mid-skip
            }
        }

        // Fast-forward the SpawnManager so we don't spawn the exact same wave again
        if (world.getGSM().peekState() instanceof PlayingState) {
            PlayingState ps = (PlayingState) world.getGSM().peekState();
            int currentTimer = ps.getSpawnManager().getSpawnTimer();
            ps.getSpawnManager().setSpawnTimer(currentTimer + skipFrames);
        }

        // The Reality Snap Flash
        world.addObject(new FX_ErasureSnap(), world.getWidth() / 2, world.getHeight() / 2);
        AudioManager.play("kc_snap");

        cooldownTimer.reset();
        cooldownTimer.start();
    }

    @Override
    public void cancel() {
        if (ERASING) {
            ERASING = false;
            erasurePercent = 0.0;
            durationTimer.stop();
            overlay = null;
        }
        cooldownTimer.stop();
    }

    @Override public boolean isActive()         { return ERASING; }
    @Override public boolean isCooldownActive() { return cooldownTimer.isActive(); }
    @Override public double getActivePercent()  { return ERASING ? (1.0 - durationTimer.getPercentComplete()) : 0.0; }
    @Override public double getCooldownPercent() { return cooldownTimer.isActive() ? cooldownTimer.getPercentComplete() : 0.0; }
    @Override public String  getKeybind()       { return GameConfig.KC_BUTTON; }
    @Override public String  getDisplayLabel()  { return "Q"; }
    @Override public boolean shouldShowIcon()   { return true; }

    @Override
    public Object captureState() {
        return new int[]{ durationTimer.getRemainingFrames(), cooldownTimer.getRemainingFrames(), durationTimer.isActive() ? 1 : 0, cooldownTimer.isActive() ? 1 : 0, ERASING ? 1 : 0 };
    }

    @Override
    public void restoreState(Object state) {
        int[] d = (int[]) state;
        durationTimer.setRemainingFrames(d[0]);
        cooldownTimer.setRemainingFrames(d[1]);
        if (d[2] == 1) durationTimer.start(); else durationTimer.stop();
        if (d[3] == 1) cooldownTimer.start(); else cooldownTimer.stop();
        ERASING = (d[4] == 1);
        erasurePercent = ERASING ? durationTimer.getPercentComplete() : 0.0;
        overlay = null;
    }
}